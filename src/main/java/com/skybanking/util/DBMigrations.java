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
		} catch (SQLException e) {
			// Best-effort: do not block app startup
		}
	}
}


