package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.model.User;
import com.skybanking.model.Account;
import com.skybanking.model.Transaction;
import com.skybanking.util.TransactionService;
import com.skybanking.util.PdfUtil;
import com.skybanking.util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servlet for generating account statements and mini statements.
 * Supports both HTML display and PDF export functionality.
 */
@WebServlet("/statement")
public class StatementServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String action = req.getParameter("action");
        String format = req.getParameter("format");

        try {
            if ("mini".equals(action)) {
                handleMiniStatement(req, resp, userId, format);
            } else if ("full".equals(action)) {
                handleFullStatement(req, resp, userId, format);
            } else {
                // Default: render mini statement
                handleMiniStatement(req, resp, userId, format);
            }
        } catch (Exception e) {
            LoggerUtil.logError("StatementServlet", "doGet", "Statement generation failed", e);
            handleError(req, resp, "Failed to generate statement: " + e.getMessage(), "dashboard.jsp", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Handle mini statement generation (last 10 transactions).
     */
    private void handleMiniStatement(HttpServletRequest req, HttpServletResponse resp, int userId, String format) throws Exception {
        List<Transaction> transactions = TransactionService.getTransactionHistory(userId, 10);
        User user = getUserDetails(userId);
        Account account = getAccountDetails(userId);

        if ("pdf".equals(format)) {
            generateStatementPDF(resp, user, account, transactions, "Mini Statement");
        } else {
            req.setAttribute("transactions", transactions);
            req.setAttribute("user", user);
            req.setAttribute("account", account);
            req.setAttribute("statementType", "Mini Statement");
            req.getRequestDispatcher("statement.jsp").forward(req, resp);
        }
    }

    /**
     * Handle full statement generation with date range.
     */
    private void handleFullStatement(HttpServletRequest req, HttpServletResponse resp, int userId, String format) throws Exception {
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
        } else {
            startDate = LocalDateTime.now().minusMonths(1);
        }

        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            endDate = LocalDateTime.parse(endDateStr + "T23:59:59");
        } else {
            endDate = LocalDateTime.now();
        }

        List<Transaction> transactions = getTransactionHistoryByDateRange(userId, startDate, endDate);
        User user = getUserDetails(userId);
        Account account = getAccountDetails(userId);

        if ("pdf".equals(format)) {
            generateStatementPDF(resp, user, account, transactions, "Full Statement");
        } else {
            req.setAttribute("transactions", transactions);
            req.setAttribute("user", user);
            req.setAttribute("account", account);
            req.setAttribute("statementType", "Full Statement");
            req.setAttribute("startDate", startDate);
            req.setAttribute("endDate", endDate);
            req.getRequestDispatcher("statement.jsp").forward(req, resp);
        }
    }

    /**
     * Generate and send statement PDF.
     */
    private void generateStatementPDF(HttpServletResponse resp, User user, Account account, 
                                    List<Transaction> transactions, String statementType) throws Exception {
        LocalDateTime startDate = transactions.isEmpty() ? LocalDateTime.now() : transactions.get(transactions.size() - 1).getDate();
        LocalDateTime endDate = transactions.isEmpty() ? LocalDateTime.now() : transactions.get(0).getDate();

        byte[] pdfBytes = PdfUtil.generateAccountStatement(user, account, transactions, startDate, endDate);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + statementType.replace(" ", "_") + "_" + 
                      LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf\"");
        resp.setContentLength(pdfBytes.length);

        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();

        LoggerUtil.logTransaction(user.getId(), account.getAccountNumber(), "STATEMENT_EXPORT", 
                                "PDF", "SUCCESS", statementType);
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
                        user.setLastLogin(rs.getTimestamp("last_login") != null ? 
                                        rs.getTimestamp("last_login").toLocalDateTime() : null);
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
                        account.setLastTransactionDate(rs.getTimestamp("last_transaction_date") != null ? 
                                                     rs.getTimestamp("last_transaction_date").toLocalDateTime() : null);
                        return account;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get transaction history by date range.
     */
    private List<Transaction> getTransactionHistoryByDateRange(int userId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        List<Transaction> transactions = new java.util.ArrayList<>();
        
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT t.* FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "WHERE a.user_id = ? AND t.txn_date BETWEEN ? AND ? " +
                        "ORDER BY t.txn_date DESC";
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setTimestamp(2, Timestamp.valueOf(startDate));
                ps.setTimestamp(3, Timestamp.valueOf(endDate));
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
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
                        transactions.add(transaction);
                    }
                }
            }
        }
        
        return transactions;
    }
}
