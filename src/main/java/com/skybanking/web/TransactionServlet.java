package com.skybanking.web;

import com.skybanking.DBConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * Handles banking transactions: Deposit, Withdraw, and Transfer.
 */
@WebServlet("/transaction")
public class TransactionServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String action = req.getParameter("action");

        try {
            switch (action) {
                case "deposit": handleDeposit(req, resp, userId); break;
                case "withdraw": handleWithdraw(req, resp, userId); break;
                case "transfer": handleTransfer(req, resp, userId); break;
                default: handleError(req, resp, "Invalid transaction type.", "dashboard.jsp", null);
            }
        } catch (Exception e) {
            handleError(req, resp, "Transaction failed: " + e.getMessage(), "dashboard.jsp", e);
        }
    }

    private int getAccountId(Connection con, int userId) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT account_id FROM accounts WHERE user_id=? AND is_active=true");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("account_id");

        // If no account exists, create one with 0 balance
        ps = con.prepareStatement("INSERT INTO accounts (user_id, balance, account_type, is_active, created_at) VALUES (?, 0, 'SAVINGS', true, NOW())", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, userId);
        ps.executeUpdate();
        rs = ps.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);
        throw new SQLException("Failed to create account for user: " + userId);
    }

    private void handleDeposit(HttpServletRequest req, HttpServletResponse resp, int userId) throws Exception {
        String amountStr = req.getParameter("amount");
        if (amountStr == null || amountStr.trim().isEmpty()) {
            handleError(req, resp, "Amount is required.", "deposit.jsp", null);
            return;
        }

        BigDecimal amount;
        try { 
            amount = new BigDecimal(amountStr); 
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException(); 
        } catch (NumberFormatException e) { 
            handleError(req, resp, "Invalid amount format.", "deposit.jsp", null); 
            return; 
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int accountId = getAccountId(con, userId);

                // Update balance
                PreparedStatement psUpdate = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + ?, last_transaction_date = NOW() WHERE account_id=?"
                );
                psUpdate.setBigDecimal(1, amount);
                psUpdate.setInt(2, accountId);
                psUpdate.executeUpdate();

                // Insert transaction
                PreparedStatement psTxn = con.prepareStatement(
                    "INSERT INTO transactions (account_id, txn_type, amount, tax_type, tax_amount, total_amount) VALUES (?, 'DEPOSIT', ?, NULL, 0, ?)"
                );
                psTxn.setInt(1, accountId);
                psTxn.setBigDecimal(2, amount);
                psTxn.setBigDecimal(3, amount);
                psTxn.executeUpdate();

                con.commit();
                req.setAttribute("message", "Deposit successful: ₹" + amount);
                req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    private void handleWithdraw(HttpServletRequest req, HttpServletResponse resp, int userId) throws Exception {
        String amountStr = req.getParameter("amount");
        if (amountStr == null || amountStr.trim().isEmpty()) { 
            handleError(req, resp, "Amount is required.", "withdraw.jsp", null); 
            return; 
        }

        BigDecimal amount;
        try { 
            amount = new BigDecimal(amountStr); 
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException(); 
        } catch (NumberFormatException e) { 
            handleError(req, resp, "Invalid amount format.", "withdraw.jsp", null); 
            return; 
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int accountId = getAccountId(con, userId);

                // Check balance
                PreparedStatement psBal = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id=?"
                );
                psBal.setInt(1, accountId);
                ResultSet rs = psBal.executeQuery();
                BigDecimal balance = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                if (balance.compareTo(amount) < 0) { 
                    handleError(req, resp, "Insufficient balance: ₹" + balance, "withdraw.jsp", null); 
                    return; 
                }

                // Update balance
                PreparedStatement psUpdate = con.prepareStatement(
                    "UPDATE accounts SET balance = balance - ?, last_transaction_date = NOW() WHERE account_id=?"
                );
                psUpdate.setBigDecimal(1, amount);
                psUpdate.setInt(2, accountId);
                psUpdate.executeUpdate();

                // Insert transaction
                PreparedStatement psTxn = con.prepareStatement(
                    "INSERT INTO transactions (account_id, txn_type, amount, tax_type, tax_amount, total_amount) VALUES (?, 'WITHDRAWAL', ?, 'TDS', 0, ?)"
                );
                psTxn.setInt(1, accountId);
                psTxn.setBigDecimal(2, amount);
                psTxn.setBigDecimal(3, amount);
                psTxn.executeUpdate();

                con.commit();
                req.setAttribute("message", "Withdrawal successful: ₹" + amount);
                req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    private void handleTransfer(HttpServletRequest req, HttpServletResponse resp, int userId) throws Exception {
        String amountStr = req.getParameter("amount");
        String receiverAccountStr = req.getParameter("receiver_account");
        if (amountStr == null || amountStr.trim().isEmpty() || receiverAccountStr == null || receiverAccountStr.trim().isEmpty()) {
            handleError(req, resp, "All fields are required.", "transfer.jsp", null); 
            return;
        }

        BigDecimal amount;
        try { 
            amount = new BigDecimal(amountStr); 
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException(); 
        } catch (NumberFormatException e) { 
            handleError(req, resp, "Invalid amount format.", "transfer.jsp", null); 
            return; 
        }

        int receiverAccountId;
        try { 
            receiverAccountId = Integer.parseInt(receiverAccountStr); 
        } catch (NumberFormatException e) {
            handleError(req, resp, "Invalid receiver account.", "transfer.jsp", null); 
            return; 
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int senderAccountId = getAccountId(con, userId);

                // Check sender balance
                PreparedStatement psBal = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_id=?"
                );
                psBal.setInt(1, senderAccountId);
                ResultSet rs = psBal.executeQuery();
                BigDecimal balance = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
                if (balance.compareTo(amount) < 0) { 
                    handleError(req, resp, "Insufficient balance: ₹" + balance, "transfer.jsp", null); 
                    return; 
                }

                // Deduct sender
                PreparedStatement psDeduct = con.prepareStatement(
                    "UPDATE accounts SET balance = balance - ?, last_transaction_date = NOW() WHERE account_id=?"
                );
                psDeduct.setBigDecimal(1, amount);
                psDeduct.setInt(2, senderAccountId);
                psDeduct.executeUpdate();

                // Add to receiver
                PreparedStatement psAdd = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + ?, last_transaction_date = NOW() WHERE account_id=?"
                );
                psAdd.setBigDecimal(1, amount);
                psAdd.setInt(2, receiverAccountId);
                int rows = psAdd.executeUpdate();
                if (rows == 0) { 
                    con.rollback(); 
                    handleError(req, resp, "Receiver account not found.", "transfer.jsp", null); 
                    return; 
                }

                // Insert transaction
                PreparedStatement psTxn = con.prepareStatement(
                    "INSERT INTO transactions (account_id, txn_type, amount, receiver_account_id, tax_type, tax_amount, total_amount) VALUES (?, 'TRANSFER', ?, ?, 'TRANSFER_FEE', 0, ?)"
                );
                psTxn.setInt(1, senderAccountId);
                psTxn.setBigDecimal(2, amount);
                psTxn.setInt(3, receiverAccountId);
                psTxn.setBigDecimal(4, amount);
                psTxn.executeUpdate();

                con.commit();
                req.setAttribute("message", "Transfer successful: ₹" + amount + " to account " + receiverAccountId);
                req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }
}
