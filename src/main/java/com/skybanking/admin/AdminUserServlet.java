package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.model.User;
import com.skybanking.model.Account;
import com.skybanking.model.Transaction;
import com.skybanking.util.LoggerUtil;
import com.skybanking.util.PdfUtil;
import com.skybanking.util.ValidationUtil;
import com.skybanking.web.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin user management servlet.
 * Handles user CRUD operations, account management, and user-related admin
 * functions.
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect("login");
            return;
        }

        String action = req.getParameter("action");
        int adminId = (Integer) session.getAttribute("admin_id");

        try {
            if (action == null || action.trim().isEmpty()) {
                handleListUsers(req, resp, adminId);
                return;
            }

            switch (action) {
                case "view":
                    handleViewUser(req, resp, adminId);
                    break;
                case "edit":
                    handleEditUser(req, resp, adminId);
                    break;
                case "transactions":
                    handleUserTransactions(req, resp, adminId);
                    break;
                case "export":
                    handleExportUserData(req, resp, adminId);
                    break;
                case "exportUsers":
                    handleExportUserList(req, resp, adminId);
                    break;
                default:
                    handleListUsers(req, resp, adminId);
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminUserServlet", "doGet", "User management operation failed", e);
            handleError(req, resp, "Operation failed: " + e.getMessage(), "/admin/users.jsp", e);
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
        int adminId = (Integer) session.getAttribute("admin_id");

        try {
            if (action == null || action.trim().isEmpty()) {
                resp.sendRedirect("users");
                return;
            }

            switch (action) {
                case "update":
                    handleUpdateUser(req, resp, adminId);
                    break;
                case "activate":
                    handleActivateUser(req, resp, adminId);
                    break;
                case "deactivate":
                    handleDeactivateUser(req, resp, adminId);
                    break;
                case "delete":
                    handleDeleteUser(req, resp, adminId);
                    break;
                case "search":
                    handleSearchUsers(req, resp, adminId);
                    break;
                default:
                    resp.sendRedirect("users");
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminUserServlet", "doPost", "User management operation failed", e);
            handleError(req, resp, "Operation failed: " + e.getMessage(), "/admin/users.jsp", e);
        }
    }

    /*
     * ============================
     * HELPER METHODS (EWFs preserved)
     * ============================
     */

    private void handleListUsers(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String search = req.getParameter("search");
        String status = req.getParameter("status");
        int page = 1;
        int limit = 20;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<User> users = getUsers(search, status, page, limit);
        int totalUsers = getTotalUsersCount(search, status);
        int totalPages = (int) Math.ceil((double) totalUsers / limit);

        req.setAttribute("users", users);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("search", search);
        req.setAttribute("status", status);

        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    private void handleViewUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        User user = getUserById(userId);
        Account account = getAccountByUserId(userId);

        if (user == null) {
            req.setAttribute("error", "User not found.");
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("user", user);
        req.setAttribute("account", account);
        req.getRequestDispatcher("/admin/userDetails.jsp").forward(req, resp);
    }

    private void handleEditUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        User user = getUserById(userId);

        if (user == null) {
            req.setAttribute("error", "User not found.");
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("user", user);
        req.getRequestDispatcher("/admin/editUser.jsp").forward(req, resp);
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        String fullname = req.getParameter("fullname");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");

        if (!ValidationUtil.isValidFullname(fullname)) {
            req.setAttribute("error", "Invalid full name.");
            req.getRequestDispatcher("/admin/editUser.jsp").forward(req, resp);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("error", "Invalid email.");
            req.getRequestDispatcher("/admin/editUser.jsp").forward(req, resp);
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            req.setAttribute("error", "Invalid phone.");
            req.getRequestDispatcher("/admin/editUser.jsp").forward(req, resp);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE users SET fullname = ?, email = ?, phone = ? WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, fullname);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setInt(4, userId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, "UPDATE_USER", userId, "Updated user profile");
        resp.sendRedirect("users?action=view&userId=" + userId + "&message=User+updated+successfully");
    }

    private void handleActivateUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        toggleUserActive(req, resp, adminId, true);
    }

    private void handleDeactivateUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        toggleUserActive(req, resp, adminId, false);
    }

    private void toggleUserActive(HttpServletRequest req, HttpServletResponse resp, int adminId, boolean activate)
            throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activate);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, activate ? "ACTIVATE_USER" : "DEACTIVATE_USER", userId,
                "User account " + (activate ? "activated" : "deactivated"));
        resp.sendRedirect("users?message=User+" + (activate ? "activated" : "deactivated") + "+successfully");
    }

    private void handleDeleteUser(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = con
                        .prepareStatement("UPDATE users SET is_active = false WHERE user_id = ?")) {
                    ps1.setInt(1, userId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = con
                        .prepareStatement("UPDATE accounts SET is_active = false WHERE user_id = ?")) {
                    ps2.setInt(1, userId);
                    ps2.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }

        LoggerUtil.logAdmin(adminId, "DELETE_USER", userId, "User deleted (soft delete)");
        resp.sendRedirect("users?message=User+deleted+successfully");
    }

    private void handleUserTransactions(HttpServletRequest req, HttpServletResponse resp, int adminId)
            throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        User user = getUserById(userId);
        Account account = getAccountByUserId(userId);

        // Read filter parameters
        String fromDateStr = req.getParameter("fromDate");
        String toDateStr = req.getParameter("toDate");
        String txnType = req.getParameter("txnType");

        List<Transaction> transactions = getUserTransactions(userId, fromDateStr, toDateStr, txnType);

        req.setAttribute("user", user);
        req.setAttribute("account", account);
        req.setAttribute("transactions", transactions);
        req.getRequestDispatcher("/admin/userTransactions.jsp").forward(req, resp);
    }

    private void handleExportUserData(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            resp.sendRedirect("users");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
        User user = getUserById(userId);
        Account account = getAccountByUserId(userId);

        if (user == null) {
            req.setAttribute("error", "User not found.");
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }

        // Handle null account safely
        if (account == null) {
            account = new Account();
            account.setAccountNumber(0);
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setAccountType("N/A");
            account.setActive(false);
        }

        // Get filter parameters for transactions (if any)
        String fromDateStr = req.getParameter("fromDate");
        String toDateStr = req.getParameter("toDate");
        String txnType = req.getParameter("txnType");

        // If no filter provided, default to last 1 month
        if ((fromDateStr == null || fromDateStr.isEmpty()) && (toDateStr == null || toDateStr.isEmpty())) {
            fromDateStr = java.time.LocalDate.now().minusMonths(1).toString();
            toDateStr = java.time.LocalDate.now().toString();
        }

        // Fetch transactions based on filters
        List<Transaction> transactions = getUserTransactions(userId, fromDateStr, toDateStr, txnType);

        // Fix transaction type mapping
        for (Transaction txn : transactions) {
            if (txn.getType().equalsIgnoreCase("WITHDRAW"))
                txn.setType("WITHDRAWAL");
            if (txn.getType().equalsIgnoreCase("PAYMENT"))
                txn.setType("TRANSFER"); // optional
        }

        // Generate PDF
        byte[] pdfBytes = PdfUtil.generateAccountStatement(user, account, transactions,
                java.time.LocalDateTime.parse(fromDateStr + "T00:00:00"),
                java.time.LocalDateTime.parse(toDateStr + "T23:59:59"));

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"User_" + userId + "_Statement.pdf\"");
        resp.setContentLength(pdfBytes.length);

        try {
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
        } finally {
            resp.getOutputStream().close();
        }

        LoggerUtil.logAdmin(adminId, "EXPORT_USER_DATA", userId, "Exported user data to PDF with filters");
    }

    private void handleSearchUsers(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String search = req.getParameter("search");
        String status = req.getParameter("status");

        resp.sendRedirect("users?search=" + (search != null ? search : "") +
                "&status=" + (status != null ? status : ""));
    }

    private void handleExportUserList(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        Connection conn = DBConnection.getConnection();
        List<User> allUsers = getAllUsers(); // You should already have this DAO method

        if (allUsers == null || allUsers.isEmpty()) {
            req.setAttribute("error", "No users found to export.");
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }

        // Generate one consolidated PDF
        byte[] pdfBytes = PdfUtil.generateUserList(allUsers);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"User_List.pdf\"");
        resp.setContentLength(pdfBytes.length);

        try {
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
        } finally {
            resp.getOutputStream().close();
        }

        LoggerUtil.logAdmin(adminId, "EXPORT_ALL_USERS", 0, "Exported all users list to PDF");
    }

    // =====================
    // DATA HELPER METHODS
    // =====================

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, fullname, username, email, phone, is_active FROM users";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id")); // ✅ maps correctly
                user.setFullname(rs.getString("fullname"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setActive(rs.getBoolean("is_active"));

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private List<User> getUsers(String search, String status, int page, int limit) throws SQLException {
        List<User> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (fullname LIKE ? OR username LIKE ? OR email LIKE ?)");
            String pattern = "%" + search + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            if ("active".equals(status))
                sql.append(" AND is_active = true");
            else if ("inactive".equals(status))
                sql.append(" AND is_active = false");
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    users.add(user);
                }
            }
        }

        return users;
    }

    private int getTotalUsersCount(String search, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (fullname LIKE ? OR username LIKE ? OR email LIKE ?)");
            String pattern = "%" + search + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            if ("active".equals(status))
                sql.append(" AND is_active = true");
            else if ("inactive".equals(status))
                sql.append(" AND is_active = false");
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

    private User getUserById(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE user_id = ?")) {
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
        return null;
    }

    private Account getAccountByUserId(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con
                        .prepareStatement("SELECT * FROM accounts WHERE user_id = ? AND is_active = true")) {
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
        return null;
    }

    private List<Transaction> getUserTransactions(int userId, String fromDateStr, String toDateStr, String txnType)
            throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT t.* FROM transactions t JOIN accounts a ON t.account_id = a.account_id WHERE a.user_id = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);

        if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
            sql.append(" AND DATE(t.txn_date) >= ?");
            parameters.add(Date.valueOf(fromDateStr));
        }

        if (toDateStr != null && !toDateStr.trim().isEmpty()) {
            sql.append(" AND DATE(t.txn_date) <= ?");
            parameters.add(Date.valueOf(toDateStr));
        }

        if (txnType != null && !txnType.trim().isEmpty()) {
            sql.append(" AND t.txn_type = ?");
            parameters.add(txnType);
        }

        sql.append(" ORDER BY t.txn_date DESC");

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction txn = new Transaction();
                    txn.setTxnId(rs.getInt("txn_id"));
                    txn.setAccountId(rs.getInt("account_id"));
                    txn.setType(rs.getString("txn_type"));
                    txn.setAmount(rs.getBigDecimal("amount"));
                    txn.setTaxType(rs.getString("tax_type"));
                    txn.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    txn.setTotalAmount(rs.getBigDecimal("total_amount"));
                    txn.setDate(rs.getTimestamp("txn_date").toLocalDateTime());
                    txn.setDescription(rs.getString("description"));
                    txn.setReceiverAccountId(rs.getObject("receiver_account_id", Integer.class));
                    txn.setStatus(rs.getString("status"));
                    txn.setReferenceNumber(rs.getString("reference_number"));
                    transactions.add(txn);
                }
            }
        }
        return transactions;
    }
}
