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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Admin log management servlet.
 * Handles viewing system logs, OTP logs, security logs, and admin activities.
 */
@WebServlet("/admin/logs")
public class AdminLogServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            resp.sendRedirect("login");
            return;
        }

        String logType = req.getParameter("type");
        if (logType == null || logType.trim().isEmpty()) {
            logType = "otp"; // Default to OTP logs
        }

        try {
            switch (logType) {
                case "otp":
                    handleOTPLogs(req, resp);
                    break;
                case "security":
                    handleSecurityLogs(req, resp);
                    break;
                case "transaction":
                    handleTransactionLogs(req, resp);
                    break;
                case "system":
                    handleSystemLogs(req, resp);
                    break;
                case "admin":
                    handleAdminLogs(req, resp);
                    break;
                default:
                    handleOTPLogs(req, resp);
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminLogServlet", "doGet", "Log retrieval failed", e);
            handleError(req, resp, "Failed to retrieve logs: " + e.getMessage(), "/admin/logs.jsp", e);
        }
    }

    /**
     * Handle OTP logs viewing.
     */
    private void handleOTPLogs(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String search = req.getParameter("search");
        String status = req.getParameter("status");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo = req.getParameter("dateTo");
        int page = 1;
        int limit = 50;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Map<String, Object>> otpLogs = getOTPLogs(search, status, dateFrom, dateTo, page, limit);
        int totalLogs = getOTPLogsCount(search, status, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        req.setAttribute("logs", otpLogs);
        req.setAttribute("logType", "otp");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("search", search);
        req.setAttribute("status", status);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);

        req.getRequestDispatcher("/admin/logs.jsp").forward(req, resp);
    }

    /**
     * Handle security logs viewing.
     */
    private void handleSecurityLogs(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String search = req.getParameter("search");
        String action = req.getParameter("action");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo = req.getParameter("dateTo");
        int page = 1;
        int limit = 50;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Map<String, Object>> securityLogs = getSecurityLogs(search, action, dateFrom, dateTo, page, limit);
        int totalLogs = getSecurityLogsCount(search, action, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        req.setAttribute("logs", securityLogs);
        req.setAttribute("logType", "security");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("search", search);
        req.setAttribute("action", action);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);

        req.getRequestDispatcher("/admin/logs.jsp").forward(req, resp);
    }

    /**
     * Handle transaction logs viewing.
     */
    private void handleTransactionLogs(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String search = req.getParameter("search");
        String type = req.getParameter("type");
        String status = req.getParameter("status");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo = req.getParameter("dateTo");
        int page = 1;
        int limit = 50;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Map<String, Object>> transactionLogs = getTransactionLogs(search, type, status, dateFrom, dateTo, page,
                limit);
        int totalLogs = getTransactionLogsCount(search, type, status, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        req.setAttribute("logs", transactionLogs);
        req.setAttribute("logType", "transaction");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("search", search);
        req.setAttribute("type", type);
        req.setAttribute("status", status);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);

        req.getRequestDispatcher("/admin/logs.jsp").forward(req, resp);
    }

    /**
     * Handle system logs viewing.
     */
    private void handleSystemLogs(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String search = req.getParameter("search");
        String component = req.getParameter("component");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo = req.getParameter("dateTo");
        int page = 1;
        int limit = 50;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Map<String, Object>> systemLogs = getSystemLogs(search, component, dateFrom, dateTo, page, limit);
        int totalLogs = getSystemLogsCount(search, component, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        req.setAttribute("logs", systemLogs);
        req.setAttribute("logType", "system");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("search", search);
        req.setAttribute("component", component);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);

        req.getRequestDispatcher("/admin/logs.jsp").forward(req, resp);
    }

    /**
     * Handle admin activity logs viewing.
     */
    private void handleAdminLogs(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String search = req.getParameter("search");
        String action = req.getParameter("action");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo = req.getParameter("dateTo");
        int page = 1;
        int limit = 50;

        try {
            String pageStr = req.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<Map<String, Object>> adminLogs = getAdminLogs(search, action, dateFrom, dateTo, page, limit);
        int totalLogs = getAdminLogsCount(search, action, dateFrom, dateTo);
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        req.setAttribute("logs", adminLogs);
        req.setAttribute("logType", "admin");
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalLogs", totalLogs);
        req.setAttribute("search", search);
        req.setAttribute("action", action);
        req.setAttribute("dateFrom", dateFrom);
        req.setAttribute("dateTo", dateTo);

        req.getRequestDispatcher("/admin/logs.jsp").forward(req, resp);
    }

    // Helper methods for retrieving different types of logs

    private List<Map<String, Object>> getOTPLogs(String search, String status, String dateFrom, String dateTo, int page,
            int limit) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM otp_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (email LIKE ? OR user_id = ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            try {
                parameters.add(Integer.parseInt(search));
            } catch (NumberFormatException e) {
                parameters.add(-1);
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("otpId", rs.getInt("otp_id"));
                        log.put("userId", rs.getInt("user_id"));
                        log.put("email", rs.getString("email"));
                        log.put("action", rs.getString("action"));
                        log.put("status", rs.getString("status"));
                        log.put("createdAt", rs.getTimestamp("created_at"));
                        log.put("expiresAt", rs.getTimestamp("expires_at"));
                        logs.add(log);
                    }
                }
            }
        }

        return logs;
    }

    private int getOTPLogsCount(String search, String status, String dateFrom, String dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM otp_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (email LIKE ? OR user_id = ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            try {
                parameters.add(Integer.parseInt(search));
            } catch (NumberFormatException e) {
                parameters.add(-1);
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }

    private List<Map<String, Object>> getSecurityLogs(String search, String action, String dateFrom, String dateTo,
            int page, int limit) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM security_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (ip_address LIKE ? OR user_agent LIKE ? OR details LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND action = ?");
            parameters.add(action);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("logId", rs.getInt("log_id"));
                        log.put("userId", rs.getObject("user_id"));
                        log.put("action", rs.getString("action"));
                        log.put("ipAddress", rs.getString("ip_address"));
                        log.put("userAgent", rs.getString("user_agent"));
                        log.put("details", rs.getString("details"));
                        log.put("createdAt", rs.getTimestamp("created_at"));
                        logs.add(log);
                    }
                }
            }
        }

        return logs;
    }

    private int getSecurityLogsCount(String search, String action, String dateFrom, String dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM security_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (ip_address LIKE ? OR user_agent LIKE ? OR details LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND action = ?");
            parameters.add(action);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }

    private List<Map<String, Object>> getTransactionLogs(String search, String type, String status, String dateFrom,
            String dateTo, int page, int limit) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transaction_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (description LIKE ? OR user_id = ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            try {
                parameters.add(Integer.parseInt(search));
            } catch (NumberFormatException e) {
                parameters.add(-1);
            }
        }

        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND transaction_type = ?");
            parameters.add(type);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("logId", rs.getInt("log_id"));
                        log.put("userId", rs.getInt("user_id"));
                        log.put("accountId", rs.getInt("account_id"));
                        log.put("transactionType", rs.getString("transaction_type"));
                        log.put("amount", rs.getString("amount"));
                        log.put("status", rs.getString("status"));
                        log.put("description", rs.getString("description"));
                        log.put("createdAt", rs.getTimestamp("created_at"));
                        logs.add(log);
                    }
                }
            }
        }

        return logs;
    }

    private int getTransactionLogsCount(String search, String type, String status, String dateFrom, String dateTo)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transaction_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (description LIKE ? OR user_id = ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            try {
                parameters.add(Integer.parseInt(search));
            } catch (NumberFormatException e) {
                parameters.add(-1);
            }
        }

        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND transaction_type = ?");
            parameters.add(type);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }

    private List<Map<String, Object>> getSystemLogs(String search, String component, String dateFrom, String dateTo,
            int page, int limit) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (message LIKE ? OR component LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (component != null && !component.trim().isEmpty()) {
            sql.append(" AND component = ?");
            parameters.add(component);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("logId", rs.getInt("log_id"));
                        log.put("component", rs.getString("component"));
                        log.put("action", rs.getString("action"));
                        log.put("message", rs.getString("message"));
                        log.put("createdAt", rs.getTimestamp("created_at"));
                        logs.add(log);
                    }
                }
            }
        }

        return logs;
    }

    private int getSystemLogsCount(String search, String component, String dateFrom, String dateTo)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM system_logs WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (message LIKE ? OR component LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (component != null && !component.trim().isEmpty()) {
            sql.append(" AND component = ?");
            parameters.add(component);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(created_at) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }

    private List<Map<String, Object>> getAdminLogs(String search, String action, String dateFrom, String dateTo,
            int page, int limit) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT al.*, a.username FROM admin_logs al " +
                "JOIN admins a ON al.admin_id = a.admin_id WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (a.username LIKE ? OR al.details LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND al.action = ?");
            parameters.add(action);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(al.created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(al.created_at) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY al.created_at DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add((page - 1) * limit);

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("logId", rs.getInt("log_id"));
                        log.put("adminId", rs.getInt("admin_id"));
                        log.put("adminUsername", rs.getString("username"));
                        log.put("action", rs.getString("action"));
                        log.put("targetUser", rs.getObject("target_user"));
                        log.put("details", rs.getString("details"));
                        log.put("createdAt", rs.getTimestamp("created_at"));
                        logs.add(log);
                    }
                }
            }
        }

        return logs;
    }

    private int getAdminLogsCount(String search, String action, String dateFrom, String dateTo) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM admin_logs al " +
                "JOIN admins a ON al.admin_id = a.admin_id WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (a.username LIKE ? OR al.details LIKE ?)");
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (action != null && !action.trim().isEmpty()) {
            sql.append(" AND al.action = ?");
            parameters.add(action);
        }

        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            sql.append(" AND DATE(al.created_at) >= ?");
            parameters.add(dateFrom);
        }

        if (dateTo != null && !dateTo.trim().isEmpty()) {
            sql.append(" AND DATE(al.created_at) <= ?");
            parameters.add(dateTo);
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }
}
