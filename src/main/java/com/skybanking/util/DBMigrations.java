package com.skybanking.util;

import com.skybanking.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMigrations {

	public static void ensureSchema() {
		try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS loans (\n" +
				"  loan_id SERIAL PRIMARY KEY,\n" +
				"  user_id INT NOT NULL,\n" +
				"  principal DECIMAL(15,2) NOT NULL,\n" +
				"  interest_rate DECIMAL(5,2) NOT NULL,\n" +
				"  tenure_months INT NOT NULL,\n" +
				"  emi DECIMAL(15,2) NOT NULL,\n" +
				"  status VARCHAR(20) CHECK (status IN ('PENDING','APPROVED','REJECTED','DISBURSED','CLOSED')) DEFAULT 'PENDING',\n" +
				"  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
				")");

			st.executeUpdate("CREATE TABLE IF NOT EXISTS loan_repayments (\n" +
				"  repayment_id SERIAL PRIMARY KEY,\n" +
				"  loan_id INT NOT NULL,\n" +
				"  due_date DATE NOT NULL,\n" +
				"  amount DECIMAL(15,2) NOT NULL,\n" +
				"  status VARCHAR(20) CHECK (status IN ('DUE','PAID','LATE')) DEFAULT 'DUE',\n" +
				"  paid_at TIMESTAMP NULL\n" +
				")");

			// Ensure admin 'Shiv' has a known password (Shiv@123)
			try (java.sql.PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM admins WHERE username = ?")) {
				ps.setString(1, "Shiv");
				try (java.sql.ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String hash = com.skybanking.web.PasswordUtil.hash("Shiv@123");
						if (rs.getInt(1) == 0) {
							try (java.sql.PreparedStatement insertPs = con.prepareStatement(
								"INSERT INTO admins (username, password_hash, full_name, email, is_active) VALUES (?, ?, ?, ?, ?)")) {
								insertPs.setString(1, "Shiv");
								insertPs.setString(2, hash);
								insertPs.setString(3, "Shiv Mali");
								insertPs.setString(4, "Shiv@skybanking.com");
								insertPs.setBoolean(5, true);
								insertPs.executeUpdate();
								System.out.println("✅ Seeded admin user 'Shiv' with password 'Shiv@123'");
							}
						} else {
							try (java.sql.PreparedStatement updatePs = con.prepareStatement(
								"UPDATE admins SET password_hash = ? WHERE username = ?")) {
								updatePs.setString(1, hash);
								updatePs.setString(2, "Shiv");
								updatePs.executeUpdate();
								System.out.println("✅ Updated admin user 'Shiv' password to 'Shiv@123'");
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			// Best-effort: do not block app startup
			System.err.println("⚠️ Admin seeding/migration check failed: " + e.getMessage());
		}
	}
}


