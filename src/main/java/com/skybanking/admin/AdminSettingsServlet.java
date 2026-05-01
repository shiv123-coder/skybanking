package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.util.LoggerUtil;
import com.skybanking.util.ValidationUtil;
import com.skybanking.web.BaseServlet;
import com.skybanking.web.PasswordUtil;

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
 * Admin settings servlet for system configuration and admin management.
 * Handles profile update, password change, and system settings.
 */
@WebServlet("/admin/settings")
public class AdminSettingsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin_id") == null) {
            resp.sendRedirect("login");
            return;
        }

        try {
            int adminId = (Integer) session.getAttribute("admin_id");

            // Load profile + system settings for tabs
            Map<String, Object> adminProfile = getAdminProfile(adminId);
            Map<String, Object> systemSettings = getSystemSettings();

            req.setAttribute("adminProfile", adminProfile);
            req.setAttribute("systemSettings", systemSettings);

            req.getRequestDispatcher("/admin/settings.jsp").forward(req, resp);

        } catch (Exception e) {
            LoggerUtil.logError("AdminSettingsServlet", "doGet", "Settings load failed", e);
            handleError(req, resp, "Failed to load settings: " + e.getMessage(), "/admin/settings.jsp", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin_id") == null) {
            resp.sendRedirect("login");
            return;
        }

        int adminId = (Integer) session.getAttribute("admin_id");
        String action = req.getParameter("action");

        try {
            if ("change_password".equals(action)) {
                handleChangePassword(req, resp, adminId);
            } else if ("update_system_settings".equals(action)) {
                handleUpdateSystemSettings(req, resp, adminId);
            } else if ("update_profile".equals(action)) {
                handleUpdateProfile(req, resp, adminId);
            } else {
                resp.sendRedirect("settings");
                return;
            }

            // Reload values after update
            req.setAttribute("adminProfile", getAdminProfile(adminId));
            req.setAttribute("systemSettings", getSystemSettings());
            req.getRequestDispatcher("/admin/settings.jsp").forward(req, resp);

        } catch (Exception e) {
            LoggerUtil.logError("AdminSettingsServlet", "doPost", "Settings update failed", e);
            handleError(req, resp, "Update failed: " + e.getMessage(), "/admin/settings.jsp", e);
        }
    }

    /**
     * Handle changing admin password.
     */
    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        if (currentPassword == null || currentPassword.isEmpty()) {
            req.setAttribute("error", "Current password is required.");
            return;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            req.setAttribute("error", "New password is required.");
            return;
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            req.setAttribute("error", "Password must be at least 8 chars, with uppercase, lowercase, and digit.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "New password and confirmation do not match.");
            return;
        }
        if (!verifyCurrentPassword(adminId, currentPassword)) {
            req.setAttribute("error", "Current password is incorrect.");
            return;
        }

        // Update password
        String hashedNewPassword = PasswordUtil.hash(newPassword);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE admins SET password_hash=? WHERE admin_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hashedNewPassword);
                ps.setInt(2, adminId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, "CHANGE_PASSWORD", null, "Password changed");
        req.setAttribute("message", "Password changed successfully.");
    }

    /**
     * Handle updating system settings.
     */
    private void handleUpdateSystemSettings(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String otpExpiryTime = req.getParameter("otpExpiryTime");
        String maxLoginAttempts = req.getParameter("maxLoginAttempts");
        String sessionTimeout = req.getParameter("sessionTimeout");
        String maintenanceMode = req.getParameter("maintenanceMode");

        // New settings
        String gstRate = req.getParameter("gstRate");
        String tdsRate = req.getParameter("tdsRate");
        String transferFeeRate = req.getParameter("transferFeeRate");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (otpExpiryTime != null && !otpExpiryTime.isEmpty()) {
                    updateSystemSetting(con, "otp_expiry_minutes", otpExpiryTime);
                }
                if (maxLoginAttempts != null && !maxLoginAttempts.isEmpty()) {
                    updateSystemSetting(con, "max_login_attempts", maxLoginAttempts);
                }
                if (sessionTimeout != null && !sessionTimeout.isEmpty()) {
                    updateSystemSetting(con, "session_timeout_minutes", sessionTimeout);
                }
                updateSystemSetting(con, "maintenance_mode", (maintenanceMode != null ? "true" : "false"));

                // Handle new fields (GST, TDS, Transfer Fee)
                if (gstRate != null && !gstRate.isEmpty()) {
                    updateSystemSetting(con, "gst_rate", gstRate);
                }
                if (tdsRate != null && !tdsRate.isEmpty()) {
                    updateSystemSetting(con, "tds_rate", tdsRate);
                }
                if (transferFeeRate != null && !transferFeeRate.isEmpty()) {
                    updateSystemSetting(con, "transfer_fee_rate", transferFeeRate);
                }

                con.commit();
                LoggerUtil.logAdmin(adminId, "UPDATE_SYSTEM_SETTINGS", null, "System settings updated");
                req.setAttribute("message", "System settings updated successfully.");
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Handle updating admin profile.
     */
    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, int adminId) throws Exception {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");

        if (fullName == null || fullName.isEmpty()) {
            req.setAttribute("error", "Full name is required.");
            return;
        }
        if (email == null || email.isEmpty() || !ValidationUtil.isValidEmail(email)) {
            req.setAttribute("error", "Valid email is required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE admins SET full_name=?, email=? WHERE admin_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setInt(3, adminId);
                ps.executeUpdate();
            }
        }

        LoggerUtil.logAdmin(adminId, "UPDATE_PROFILE", null, "Profile updated");
        req.setAttribute("message", "Profile updated successfully.");
    }

    // ---------------- Helper Methods ---------------- //

    private Map<String, Object> getAdminProfile(int adminId) throws SQLException {
        Map<String, Object> profile = new HashMap<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM admins WHERE admin_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, adminId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        profile.put("adminId", rs.getInt("admin_id"));
                        profile.put("username", rs.getString("username"));
                        profile.put("fullName", rs.getString("full_name"));
                        profile.put("email", rs.getString("email"));
                        profile.put("isActive", rs.getBoolean("is_active"));
                        profile.put("createdAt", rs.getTimestamp("created_at"));
                        profile.put("lastLogin", rs.getTimestamp("last_login"));
                    }
                }
            }
        }
        return profile;
    }

    private Map<String, Object> getSystemSettings() throws SQLException {
        Map<String, Object> settings = new HashMap<>();
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT setting_key, setting_value FROM system_settings";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
                }
            }
        }
        return settings;
    }

    private void updateSystemSetting(Connection con, String key, String value) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

    private boolean verifyCurrentPassword(int adminId, String currentPassword) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT password_hash FROM admins WHERE admin_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, adminId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return PasswordUtil.verify(currentPassword, rs.getString("password_hash"));
                    }
                }
            }
        }
        return false;
    }
}
