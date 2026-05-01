package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/deleteAccount")
public class DeleteAccountServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 1️⃣ Fetch all account_ids for this user (from accounts table per schema)
            List<Integer> accountIds = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT account_id FROM accounts WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        accountIds.add(rs.getInt("account_id"));
                    }
                }
            }

            // 2️⃣ Delete all transactions linked to these accounts
            if (!accountIds.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < accountIds.size(); i++) {
                    sb.append("?");
                    if (i != accountIds.size() - 1) sb.append(",");
                }
                String inClause = sb.toString();
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM transactions WHERE account_id IN (" + inClause + ")")) {
                    for (int i = 0; i < accountIds.size(); i++) {
                        ps.setInt(i + 1, accountIds.get(i));
                    }
                    ps.executeUpdate();
                }
            }

            // 3️⃣ Delete all accounts for this user (will cascade transactions via FK)
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM accounts WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 4️⃣ Delete user record
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 5️⃣ Commit transaction
            con.commit();

            // 6️⃣ Invalidate session and redirect with success
            session.invalidate();
            response.sendRedirect("signup.jsp?success=Account+deleted+successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            response.sendRedirect("dashboard.jsp?error=Unable+to+delete+account");
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
