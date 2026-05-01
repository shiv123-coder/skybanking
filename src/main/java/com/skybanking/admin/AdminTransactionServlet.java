package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.model.Transaction;
import com.skybanking.util.LoggerUtil;
import com.skybanking.util.PdfUtil;
import com.skybanking.web.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin transaction management servlet.
 * Handles transaction viewing, filtering, export, and management operations.
 */
@WebServlet("/admin/transactions")
public class AdminTransactionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect("login");
            return;
        }

        String action = req.getParameter("action");
        int adminId = session.getAttribute("admin_id") != null ? (Integer) session.getAttribute("admin_id") : 0;

        try {
            if (action == null)
                action = "";

            switch (action) {
                case "view":
                    handleViewTransaction(req, resp, adminId);
                    break;
                case "export":
                    handleExportTransactions(req, resp, adminId);
                    break;
                case "flag":
                    handleFlagTransaction(req, resp, adminId);
                    break;
                case "unflag":
                    handleUnflagTransaction(req, resp, adminId);
                    break;
                default:
                    handleListTransactions(req, resp, adminId);
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminTransactionServlet", "doGet", "Transaction management operation failed", e);
            handleError(req, resp, "Operation failed: " + e.getMessage(), "/admin/transactions.jsp", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect("login");
            return;
        }

        String action = req.getParameter("action");
        int adminId = session.getAttribute("admin_id") != null ? (Integer) session.getAttribute("admin_id") : 0;

        try {
            if (action == null)
                action = "";

            switch (action) {
                case "search":
                    handleSearchTransactions(req, resp, adminId);
                    break;
                case "filter":
                    handleFilterTransactions(req, resp, adminId);
                    break;
                case "export":
                    handleExportTransaction(req, resp, adminId);
                    break;
                case "bulk_export":
                    handleBulkExportTransactions(req, resp, adminId);
                    break;
                default:
                    resp.sendRedirect("transactions");
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminTransactionServlet", "doPost", "Transaction management operation failed", e);
            handleError(req, resp, "Operation failed: " + e.getMessage(), "/admin/transactions.jsp", e);
        }
    }

    // =================== GET HANDLERS ===================

    private void handleExportTransaction(HttpServletRequest req, HttpServletResponse resp, int adminId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleExportTransaction'");
    }

    private void handleListTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws Exception {
        String search = safeParam(req.getParameter("search"));
        String type = safeParam(req.getParameter("type"));
        String status = safeParam(req.getParameter("status"));
        String dateFrom = safeParam(req.getParameter("dateFrom"));
        String dateTo = safeParam(req.getParameter("dateTo"));
        int page = parsePage(req.getParameter("page"));
        int limit = 50;

        List<Transaction> transactions = getTransactions(search, type, status, dateFrom, dateTo, page, limit);
        int totalTransactions = getTotalTransactionsCount(search, type, status, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalTransactions / limit);

        Map<String, Object> stats = getTransactionStatistics();

        req.setAttribute("transactions", transactions);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalTransactions", totalTransactions);
        req.setAttribute("search", search);
        req.setAttribute("type", type);
        req.setAttribute("status", status);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);
        req.setAttribute("stats", stats);

        req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
    }

    private void handleViewTransaction(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String txnIdStr = safeParam(req.getParameter("txnId"));
        if (txnIdStr.isEmpty()) {
            resp.sendRedirect("transactions");
            return;
        }

        int txnId = Integer.parseInt(txnIdStr);
        Transaction transaction = getTransactionById(txnId);

        if (transaction == null) {
            req.setAttribute("error", "Transaction not found.");
            req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
            return;
        }

        Map<String, Object> userDetails = getUserDetailsForTransaction(transaction.getAccountId());

        req.setAttribute("transaction", transaction);
        req.setAttribute("userDetails", userDetails);
        req.getRequestDispatcher("/admin/transactionDetails.jsp").forward(req, resp);
    }

    private void handleExportTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws Exception {
        String txnIdStr = safeParam(req.getParameter("txnId"));
        if (txnIdStr.isEmpty()) {
            resp.sendRedirect("transactions");
            return;
        }

        int txnId = Integer.parseInt(txnIdStr);
        Transaction transaction = getTransactionById(txnId);

        if (transaction == null) {
            req.setAttribute("error", "Transaction not found.");
            req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
            return;
        }

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        byte[] pdfBytes = PdfUtil.generateTransactionSummary(transactions,
                "Transaction Details - " + transaction.getReferenceNumber(), "Admin");

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"Transaction_" + txnId + "_Details.pdf\"");
        resp.setContentLength(pdfBytes.length);

        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();

        LoggerUtil.logAdmin(adminId, "EXPORT_TRANSACTION", null, "Exported transaction " + txnId + " to PDF");
    }

    private void handleFlagTransaction(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String txnIdStr = safeParam(req.getParameter("txnId"));
        if (txnIdStr.isEmpty()) {
            resp.sendRedirect("transactions");
            return;
        }

        int txnId = Integer.parseInt(txnIdStr);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE transactions SET status = 'FLAGGED' WHERE txn_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, txnId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, "FLAG_TRANSACTION", null, "Flagged transaction " + txnId + " as suspicious");
        resp.sendRedirect("transactions?action=view&txnId=" + txnId);
    }

    private void handleUnflagTransaction(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws Exception {
        String txnIdStr = safeParam(req.getParameter("txnId"));
        if (txnIdStr.isEmpty()) {
            resp.sendRedirect("transactions");
            return;
        }

        int txnId = Integer.parseInt(txnIdStr);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE transactions SET status = 'COMPLETED' WHERE txn_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, txnId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, "UNFLAG_TRANSACTION", null, "Unflagged transaction " + txnId);
        resp.sendRedirect("transactions?action=view&txnId=" + txnId);
    }

    // =================== POST HANDLERS ===================

    private void handleSearchTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws IOException {
        StringBuilder redirectUrl = new StringBuilder("transactions?");
        appendParam(redirectUrl, "search", req.getParameter("search"));
        appendParam(redirectUrl, "type", req.getParameter("type"));
        appendParam(redirectUrl, "status", req.getParameter("status"));
        appendParam(redirectUrl, "dateFrom", req.getParameter("dateFrom"));
        appendParam(redirectUrl, "dateTo", req.getParameter("dateTo"));

        resp.sendRedirect(redirectUrl.toString());
    }

    private void handleFilterTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws IOException {
        handleSearchTransactions(req, resp, adminId);
    }

    private void handleBulkExportTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws Exception {
        TransactionDAO dao = new TransactionDAO();
        List<Transaction> transactions = dao.getAllTransactions(
                safeParam(req.getParameter("search")),
                safeParam(req.getParameter("type")),
                safeParam(req.getParameter("status")),
                safeParam(req.getParameter("dateFrom")),
                safeParam(req.getParameter("dateTo")));

        if (transactions == null || transactions.isEmpty()) {
            req.setAttribute("error", "No transactions found to export.");
            req.getRequestDispatcher("/admin/transactions.jsp").forward(req, resp);
            return;
        }

        // ✅ Use new PdfUtil bulk export
        byte[] pdfBytes = PdfUtil.generateBulkTransactionExport(transactions, "Admin");

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"All_Transactions_" +
                        java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        + ".pdf\"");
        resp.setContentLength(pdfBytes.length);
        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();

        LoggerUtil.logAdmin(adminId, "BULK_EXPORT_TRANSACTIONS", null,
                "Exported " + transactions.size() + " transactions to PDF");
    }

    // =================== HELPER METHODS ===================

    private String safeParam(String param) {
        return param != null ? param.trim() : "";
    }

    private int parsePage(String pageStr) {
        try {
            return pageStr != null && !pageStr.trim().isEmpty() ? Integer.parseInt(pageStr) : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void appendParam(StringBuilder sb, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(key).append("=").append(value.trim()).append("&");
        }
    }

    // =================== DATABASE METHODS ===================

    private List<Transaction> getTransactions(String search, String type, String status,
            String dateFrom, String dateTo, int page, int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, u.fullname, u.username FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "JOIN users u ON a.user_id = u.user_id WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (!search.isEmpty()) {
            sql.append(" AND (t.reference_number LIKE ? OR u.fullname LIKE ? OR u.username LIKE ?)");
            String s = "%" + search + "%";
            parameters.add(s);
            parameters.add(s);
            parameters.add(s);
        }
        if (!type.isEmpty()) {
            sql.append(" AND t.txn_type = ?");
            parameters.add(type);
        }
        if (!status.isEmpty()) {
            sql.append(" AND t.status = ?");
            parameters.add(status);
        }
        if (!dateFrom.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) >= ?");
            parameters.add(dateFrom);
        }
        if (!dateTo.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY t.txn_date DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTxnId(rs.getInt("txn_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setType(rs.getString("txn_type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setTaxType(rs.getString("tax_type"));
                    t.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    t.setTotalAmount(rs.getBigDecimal("total_amount"));
                    t.setDate(rs.getTimestamp("txn_date").toLocalDateTime());
                    t.setDescription(rs.getString("description"));
                    t.setReceiverAccountId(rs.getObject("receiver_account_id", Integer.class));
                    t.setStatus(rs.getString("status"));
                    t.setReferenceNumber(rs.getString("reference_number"));
                    transactions.add(t);
                }
            }
        }

        return transactions;
    }

    private int getTotalTransactionsCount(String search, String type, String status,
            String dateFrom, String dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.account_id " +
                        "JOIN users u ON a.user_id = u.user_id WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (!search.isEmpty()) {
            String s = "%" + search + "%";
            sql.append(" AND (t.reference_number LIKE ? OR u.fullname LIKE ? OR u.username LIKE ?)");
            parameters.add(s);
            parameters.add(s);
            parameters.add(s);
        }
        if (!type.isEmpty()) {
            sql.append(" AND t.txn_type = ?");
            parameters.add(type);
        }
        if (!status.isEmpty()) {
            sql.append(" AND t.status = ?");
            parameters.add(status);
        }
        if (!dateFrom.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) >= ?");
            parameters.add(dateFrom);
        }
        if (!dateTo.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Map<String, Object> getTransactionStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection con = DBConnection.getConnection()) {
            String sql1 = "SELECT COUNT(*) FROM transactions";
            try (PreparedStatement ps = con.prepareStatement(sql1);
                    ResultSet rs = ps.executeQuery()) {
                stats.put("totalTransactions", rs.next() ? rs.getInt(1) : 0);
            }

            String sql2 = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions";
            try (PreparedStatement ps = con.prepareStatement(sql2);
                    ResultSet rs = ps.executeQuery()) {
                stats.put("totalAmount", rs.next() ? rs.getDouble(1) : 0.0);
            }

            String sql3 = "SELECT COUNT(*) FROM transactions WHERE DATE(txn_date) = CURDATE()";
            try (PreparedStatement ps = con.prepareStatement(sql3);
                    ResultSet rs = ps.executeQuery()) {
                stats.put("todayTransactions", rs.next() ? rs.getInt(1) : 0);
            }

            String sql4 = "SELECT COUNT(*) FROM transactions WHERE status = 'FLAGGED'";
            try (PreparedStatement ps = con.prepareStatement(sql4);
                    ResultSet rs = ps.executeQuery()) {
                stats.put("flaggedTransactions", rs.next() ? rs.getInt(1) : 0);
            }
        }

        return stats;
    }

    private Transaction getTransactionById(int txnId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM transactions WHERE txn_id = ?")) {
            ps.setInt(1, txnId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTxnId(rs.getInt("txn_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setType(rs.getString("txn_type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setTaxType(rs.getString("tax_type"));
                    t.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    t.setTotalAmount(rs.getBigDecimal("total_amount"));
                    t.setDate(rs.getTimestamp("txn_date").toLocalDateTime());
                    t.setDescription(rs.getString("description"));
                    t.setReceiverAccountId(rs.getObject("receiver_account_id", Integer.class));
                    t.setStatus(rs.getString("status"));
                    t.setReferenceNumber(rs.getString("reference_number"));
                    return t;
                }
            }
        }

        return null;
    }

    private Map<String, Object> getUserDetailsForTransaction(int accountId) throws SQLException {
        Map<String, Object> userDetails = new HashMap<>();

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT u.*, a.balance, a.account_type FROM users u " +
                                "JOIN accounts a ON u.user_id = a.user_id WHERE a.account_id = ?")) {
            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userDetails.put("userId", rs.getInt("user_id"));
                    userDetails.put("fullname", rs.getString("fullname"));
                    userDetails.put("username", rs.getString("username"));
                    userDetails.put("email", rs.getString("email"));
                    userDetails.put("phone", rs.getString("phone"));
                    userDetails.put("balance", rs.getBigDecimal("balance"));
                    userDetails.put("accountType", rs.getString("account_type"));
                }
            }
        }

        return userDetails;
    }
}
