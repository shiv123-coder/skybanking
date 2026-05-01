package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/transactions")
public class TransactionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        List<Map<String, Object>> transactions = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            // 1️⃣ Get logged-in user's account_id
            int accountId = 0;
            try (PreparedStatement psAcc = con.prepareStatement(
                    "SELECT account_id FROM accounts WHERE user_id=? AND is_active=true")) {
                psAcc.setInt(1, userId);
                try (ResultSet rs = psAcc.executeQuery()) {
                    if (rs.next()) {
                        accountId = rs.getInt("account_id");
                    } else {
                        throw new SQLException("No account found for user_id: " + userId);
                    }
                }
            }

            // 2️⃣ Fetch last 20 transactions where user is sender OR receiver
            String sql = "SELECT txn_id, account_id, txn_type, amount, receiver_account_id, txn_date " +
                         "FROM transactions WHERE account_id=? OR receiver_account_id=? " +
                         "ORDER BY txn_date DESC LIMIT 20";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, accountId);
                ps.setInt(2, accountId);

                try (ResultSet rs = ps.executeQuery()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                    while (rs.next()) {
                        Map<String, Object> txn = new HashMap<>();
                        Timestamp ts = rs.getTimestamp("txn_date");
                        txn.put("timestamp", sdf.format(ts));

                        int senderId = rs.getInt("account_id");
                        int receiverId = rs.getInt("receiver_account_id");
                        String type = rs.getString("txn_type");

                        // Determine type for receiver
                        if (receiverId == accountId && "TRANSFER".equals(type)) {
                            type = "Received";
                        }

                        txn.put("type", type);
                        txn.put("amount", rs.getBigDecimal("amount").toPlainString());

                        // 🔹 Counterparty column (Transferred To / Received From)
                        String counterparty = "-";
                        if ("TRANSFER".equals(type)) {
                            // Show receiver if current user is sender
                            if (senderId == accountId) {
                                counterparty = getAccountCode(con, receiverId);
                            }
                        } else if ("Received".equals(type)) {
                            // Show sender if current user is receiver
                            counterparty = getAccountCode(con, senderId);
                        }

                        txn.put("counterparty", counterparty);
                        transactions.add(txn);
                    }
                }
            }

            req.setAttribute("transactions", transactions);
            req.getRequestDispatcher("transactions.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("error", "Failed to load transactions: " + e.getMessage());
            req.getRequestDispatcher("transactions.jsp").forward(req, resp);
        }
    }

    // ✅ Helper method to fetch account_code
    private String getAccountCode(Connection con, int accId) throws SQLException {
        return String.valueOf(accId);
    }
}
