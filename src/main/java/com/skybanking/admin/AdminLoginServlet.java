package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.util.LoggerUtil;
import com.skybanking.web.BaseServlet;
import com.skybanking.web.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

/**
 * Admin login servlet with BCrypt password verification
 * and automatic SHA-256 → BCrypt hash migration.
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String ipAddress = getClientIPAddress(req);
        String userAgent = req.getHeader("User-Agent");

        if (username == null || username.trim().isEmpty()) {
            req.setAttribute("error", "Username is required.");
            req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Password is required.");
            req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT admin_id, password_hash FROM admins WHERE username = ? AND is_active = true";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        int adminId = rs.getInt("admin_id");

                        if (PasswordUtil.verify(password, storedHash)) {
                            // Migrate legacy hash to BCrypt
                            if (PasswordUtil.needsUpgrade(storedHash)) {
                                upgradeAdminPasswordHash(con, adminId, password);
                            }

                            HttpSession session = req.getSession(true);
                            session.setAttribute("admin", username.trim());
                            session.setAttribute("admin_id", adminId);
                            session.setMaxInactiveInterval(60 * 60); // 1 hour

                            LoggerUtil.logSecurity(null, "ADMIN_LOGIN", ipAddress, userAgent,
                                    "Admin login successful: " + username);

                            resp.sendRedirect("dashboard");
                        } else {
                            req.setAttribute("error", "Invalid admin credentials.");
                            LoggerUtil.logSecurity(null, "ADMIN_LOGIN_FAILED", ipAddress, userAgent,
                                    "Failed admin login attempt: " + username);
                            req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
                        }
                    } else {
                        req.setAttribute("error", "Invalid admin credentials.");
                        LoggerUtil.logSecurity(null, "ADMIN_LOGIN_FAILED", ipAddress, userAgent,
                                "Failed admin login attempt: " + username);
                        req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError("AdminLoginServlet", "doPost", "Admin login error", e);
            req.setAttribute("error", "Server error. Please try again later.");
            req.getRequestDispatcher("/admin/adminLogin.jsp").forward(req, resp);
        }
    }

    private void upgradeAdminPasswordHash(Connection con, int adminId, String plainPassword) {
        try {
            String bcryptHash = PasswordUtil.hash(plainPassword);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE admins SET password_hash = ? WHERE admin_id = ?")) {
                ps.setString(1, bcryptHash);
                ps.setInt(2, adminId);
                ps.executeUpdate();
                logger.info("Upgraded admin password hash to BCrypt for admin_id=" + adminId);
            }
        } catch (Exception e) {
            logger.warning("Failed to upgrade admin password hash for admin_id=" + adminId);
        }
    }

    private String getClientIPAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
