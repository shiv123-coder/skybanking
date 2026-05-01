package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.util.LoggerUtil;
import com.skybanking.web.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin dashboard servlet providing system statistics and overview.
 * Displays key metrics for admin monitoring and management.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

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
            if ("export".equals(action)) {
                handleExportDashboard(req, resp, adminId);
            } else {
                // Get dashboard statistics
                Map<String, Object> stats = getDashboardStatistics();
                req.setAttribute("stats", stats);
                
                // Get recent activities
                Map<String, Object> recentActivities = getRecentActivities();
                req.setAttribute("recentActivities", recentActivities);
                
                // Get transaction trends (last 7 days)
                Map<String, Object> transactionTrends = getTransactionTrends();
                req.setAttribute("transactionTrends", transactionTrends);
                
                req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
            }
            
        } catch (Exception e) {
            LoggerUtil.logError("AdminDashboardServlet", "doGet", "Dashboard data retrieval failed", e);
            handleError(req, resp, "Failed to load dashboard data", "/admin/dashboard.jsp", e);
        }
    }

    /**
     * Handle exporting dashboard data to PDF.
     */
    private void handleExportDashboard(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        // Get dashboard statistics
        Map<String, Object> stats = getDashboardStatistics();
        
        // Get recent activities
        Map<String, Object> recentActivities = getRecentActivities();
        
        // Get transaction trends
        Map<String, Object> transactionTrends = getTransactionTrends();

        // Generate PDF
        byte[] pdfBytes = com.skybanking.util.PdfUtil.generateDashboardReport(stats, recentActivities, transactionTrends);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"Dashboard_Report_" + 
                      java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf\"");
        resp.setContentLength(pdfBytes.length);

        resp.getOutputStream().write(pdfBytes);
        resp.getOutputStream().flush();

        LoggerUtil.logAdmin(adminId, "EXPORT_DASHBOARD", null, "Exported dashboard report to PDF");
    }

    /**
     * Get comprehensive dashboard statistics.
     */
    private Map<String, Object> getDashboardStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection con = DBConnection.getConnection()) {
            // Total users
            stats.put("totalUsers", getTotalUsers(con));
            
            // Active users (logged in last 30 days)
            stats.put("activeUsers", getActiveUsers(con));
            
            // Total accounts
            stats.put("totalAccounts", getTotalAccounts(con));
            
            // Active accounts
            stats.put("activeAccounts", getActiveAccounts(con));
            
            // Total transactions
            stats.put("totalTransactions", getTotalTransactions(con));
            
            // Total transaction amount
            stats.put("totalTransactionAmount", getTotalTransactionAmount(con));
            
            // Today's transactions
            stats.put("todayTransactions", getTodayTransactions(con));
            
            // Today's transaction amount
            stats.put("todayTransactionAmount", getTodayTransactionAmount(con));
            
            // Pending OTPs
            stats.put("pendingOTPs", getPendingOTPs(con));
            
            // System status
            stats.put("systemStatus", "OPERATIONAL");
        }
        
        return stats;
    }

    /**
     * Get recent system activities.
     */
    private Map<String, Object> getRecentActivities() throws SQLException {
        Map<String, Object> activities = new HashMap<>();
        
        try (Connection con = DBConnection.getConnection()) {
            // Recent user registrations
            activities.put("recentRegistrations", getRecentRegistrations(con));
            
            // Recent transactions
            activities.put("recentTransactions", getRecentTransactions(con));
            
            // Recent OTP activities
            activities.put("recentOTPActivities", getRecentOTPActivities(con));
        }
        
        return activities;
    }

    /**
     * Get transaction trends for charts.
     */
    private Map<String, Object> getTransactionTrends() throws SQLException {
        Map<String, Object> trends = new HashMap<>();
        
        try (Connection con = DBConnection.getConnection()) {
            // Daily transaction counts for last 7 days
            trends.put("dailyTransactionCounts", getDailyTransactionCounts(con));
            
            // Daily transaction amounts for last 7 days
            trends.put("dailyTransactionAmounts", getDailyTransactionAmounts(con));
            
            // Transaction types distribution
            trends.put("transactionTypesDistribution", getTransactionTypesDistribution(con));
        }
        
        return trends;
    }

    // Statistics helper methods
    
    private int getTotalUsers(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getActiveUsers(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE last_login >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getTotalAccounts(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getActiveAccounts(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE is_active = true";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getTotalTransactions(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double getTotalTransactionAmount(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    private int getTodayTransactions(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE DATE(txn_date) = CURDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double getTodayTransactionAmount(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM transactions WHERE DATE(txn_date) = CURDATE()";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    private int getPendingOTPs(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM otp_logs WHERE status = 'PENDING' AND created_at > DATE_SUB(NOW(), INTERVAL 10 MINUTE)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Recent activities helper methods
    
    private java.util.List<Map<String, Object>> getRecentRegistrations(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> registrations = new java.util.ArrayList<>();
        String sql = "SELECT user_id, fullname, username, email, created_at FROM users ORDER BY created_at DESC LIMIT 5";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("userId", rs.getInt("user_id"));
                user.put("fullname", rs.getString("fullname"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("createdAt", rs.getTimestamp("created_at"));
                registrations.add(user);
            }
        }
        return registrations;
    }

    private java.util.List<Map<String, Object>> getRecentTransactions(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> transactions = new java.util.ArrayList<>();
        String sql = "SELECT t.txn_id, t.txn_type, t.amount, t.txn_date, u.fullname " +
                    "FROM transactions t " +
                    "JOIN accounts a ON t.account_id = a.account_id " +
                    "JOIN users u ON a.user_id = u.user_id " +
                    "ORDER BY t.txn_date DESC LIMIT 10";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("txnId", rs.getInt("txn_id"));
                transaction.put("type", rs.getString("txn_type"));
                transaction.put("amount", rs.getBigDecimal("amount"));
                transaction.put("date", rs.getTimestamp("txn_date"));
                transaction.put("userName", rs.getString("fullname"));
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private java.util.List<Map<String, Object>> getRecentOTPActivities(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> otpActivities = new java.util.ArrayList<>();
        String sql = "SELECT otp_id, user_id, email, action, status, created_at FROM otp_logs ORDER BY created_at DESC LIMIT 10";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> otp = new HashMap<>();
                otp.put("otpId", rs.getInt("otp_id"));
                otp.put("userId", rs.getInt("user_id"));
                otp.put("email", rs.getString("email"));
                otp.put("action", rs.getString("action"));
                otp.put("status", rs.getString("status"));
                otp.put("createdAt", rs.getTimestamp("created_at"));
                otpActivities.add(otp);
            }
        }
        return otpActivities;
    }

    // Transaction trends helper methods
    
    private java.util.List<Map<String, Object>> getDailyTransactionCounts(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> dailyCounts = new java.util.ArrayList<>();
        String sql = "SELECT DATE(txn_date) as date, COUNT(*) as count " +
                    "FROM transactions " +
                    "WHERE txn_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(txn_date) " +
                    "ORDER BY date";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", rs.getDate("date"));
                dayData.put("count", rs.getInt("count"));
                dailyCounts.add(dayData);
            }
        }
        return dailyCounts;
    }

    private java.util.List<Map<String, Object>> getDailyTransactionAmounts(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> dailyAmounts = new java.util.ArrayList<>();
        String sql = "SELECT DATE(txn_date) as date, COALESCE(SUM(total_amount), 0) as amount " +
                    "FROM transactions " +
                    "WHERE txn_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(txn_date) " +
                    "ORDER BY date";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", rs.getDate("date"));
                dayData.put("amount", rs.getDouble("amount"));
                dailyAmounts.add(dayData);
            }
        }
        return dailyAmounts;
    }

    private java.util.List<Map<String, Object>> getTransactionTypesDistribution(Connection con) throws SQLException {
        java.util.List<Map<String, Object>> typeDistribution = new java.util.ArrayList<>();
        String sql = "SELECT txn_type, COUNT(*) as count, COALESCE(SUM(total_amount), 0) as amount " +
                    "FROM transactions " +
                    "GROUP BY txn_type";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("type", rs.getString("txn_type"));
                typeData.put("count", rs.getInt("count"));
                typeData.put("amount", rs.getDouble("amount"));
                typeDistribution.add(typeData);
            }
        }
        return typeDistribution;
    }
}
