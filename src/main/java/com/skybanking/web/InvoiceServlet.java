package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.model.User;
import com.skybanking.model.Account;
import com.skybanking.model.Transaction;
import com.skybanking.util.PdfUtil;
import com.skybanking.util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

/**
 * Servlet for generating transaction invoices and receipts.
 * Supports PDF export functionality for invoices.
 */
@WebServlet("/invoice")
public class InvoiceServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String txnIdStr = req.getParameter("txnId");
        String format = req.getParameter("format");

        if (txnIdStr == null || txnIdStr.trim().isEmpty()) {
            handleError(req, resp, "Transaction ID is required.", "dashboard.jsp", null);
            return;
        }

        try {
            int txnId = Integer.parseInt(txnIdStr);
            Transaction transaction = getTransactionDetails(txnId, userId);

            if (transaction == null) {
                handleError(req, resp, "Transaction not found or access denied.", "dashboard.jsp", null);
                return;
            }

            User user = getUserDetails(userId);
            Account account = getAccountDetails(userId);

            if ("pdf".equals(format)) {
                generateInvoicePDF(resp, user, account, transaction);
            } else {
                req.setAttribute("transaction", transaction);
                req.setAttribute("user", user);
                req.setAttribute("account", account);
                req.getRequestDispatcher("invoice.jsp").forward(req, resp);
            }

        } catch (NumberFormatException e) {
            handleError(req, resp, "Invalid transaction ID format.", "dashboard.jsp", null);
        } catch (Exception e) {
            LoggerUtil.logError("InvoiceServlet", "doGet", "Invoice generation failed", e);
            handleError(req, resp, "Failed to generate invoice: " + e.getMessage(), "dashboard.jsp", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Generate and send invoice PDF.
     */
    private void generateInvoicePDF(HttpServletResponse resp, User user, Account account, Transaction transaction)
            throws Exception {
        byte[] pdfBytes = PdfUtil.generateTransactionInvoice(user, account, transaction);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"Invoice_" +
                transaction.getReferenceNumber() + ".pdf\"");
        resp.setContentLength(pdfBytes.length);

        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();

        LoggerUtil.logTransaction(user.getId(), account.getAccountNumber(), "INVOICE_EXPORT",
                "PDF", "SUCCESS", "Invoice for transaction: " + transaction.getTxnId());
    }

    /**
     * Get transaction details for a specific transaction ID and user.
     */
    private Transaction getTransactionDetails(int txnId, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT t.* FROM transactions t " +
                    "JOIN accounts a ON t.account_id = a.account_id " +
                    "WHERE t.txn_id = ? AND a.user_id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, txnId);
                ps.setInt(2, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Transaction transaction = new Transaction();
                        transaction.setTxnId(rs.getInt("txn_id"));
                        transaction.setAccountId(rs.getInt("account_id"));
                        transaction.setType(rs.getString("txn_type"));
                        transaction.setAmount(rs.getBigDecimal("amount"));
                        transaction.setTaxType(rs.getString("tax_type"));
                        transaction.setTaxAmount(rs.getBigDecimal("tax_amount"));
                        transaction.setTotalAmount(rs.getBigDecimal("total_amount"));
                        transaction.setDate(rs.getTimestamp("txn_date").toLocalDateTime());
                        transaction.setDescription(rs.getString("description"));
                        transaction.setReceiverAccountId(rs.getObject("receiver_account_id", Integer.class));
                        transaction.setStatus(rs.getString("status"));
                        transaction.setReferenceNumber(rs.getString("reference_number"));
                        return transaction;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get user details from database.
     */
    private User getUserDetails(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("user_id"));
                        user.setFullname(rs.getString("fullname"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setActive(rs.getBoolean("is_active"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        user.setLastLogin(
                                rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime()
                                        : null);
                        return user;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get account details from database.
     */
    private Account getAccountDetails(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE user_id = ? AND is_active = true";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account();
                        account.setAccountNumber(rs.getInt("account_id"));
                        account.setUserId(rs.getInt("user_id"));
                        account.setBalance(rs.getBigDecimal("balance"));
                        account.setAccountType(rs.getString("account_type"));
                        account.setActive(rs.getBoolean("is_active"));
                        account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        account.setLastTransactionDate(rs.getTimestamp("last_transaction_date") != null
                                ? rs.getTimestamp("last_transaction_date").toLocalDateTime()
                                : null);
                        return account;
                    }
                }
            }
        }
        return null;
    }
}
