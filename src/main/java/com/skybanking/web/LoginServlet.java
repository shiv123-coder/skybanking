package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.logging.*;

/**
 * User login servlet with BCrypt password verification
 * and automatic SHA-256 → BCrypt hash migration.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // Prevent login if OTP is still pending
        if (session != null && session.getAttribute("otpPending") != null) {
            req.setAttribute("error", "Please verify OTP before proceeding to Login.");
            req.getRequestDispatcher("signup.jsp").forward(req, resp);
            return;
        }

        req.getRequestDispatcher("login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty()) {
            req.setAttribute("error", "Username is required.");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Password is required.");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT user_id, password_hash FROM users WHERE username = ? AND is_active = true";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        int userId = rs.getInt("user_id");

                        if (PasswordUtil.verify(password, storedHash)) {
                            // Migrate legacy SHA-256 hash to BCrypt on successful login
                            if (PasswordUtil.needsUpgrade(storedHash)) {
                                upgradePasswordHash(con, userId, password);
                            }

                            // Update last_login
                            updateLastLogin(con, userId);

                            HttpSession session = req.getSession(true);
                            session.setAttribute("user_id", userId);
                            session.setAttribute("username", username.trim());
                            session.setMaxInactiveInterval(30 * 60); // 30 mins
                            resp.sendRedirect("dashboard");
                        } else {
                            req.setAttribute("error", "Incorrect password. Please try again.");
                            req.getRequestDispatcher("login.jsp").forward(req, resp);
                        }
                    } else {
                        req.setAttribute("error", "Username not found. Please check or sign up.");
                        req.getRequestDispatcher("login.jsp").forward(req, resp);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Login failed due to database error", e);
            req.setAttribute("error", "Server error. Please try again later.");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }

    /**
     * Upgrade a legacy SHA-256 hash to BCrypt.
     */
    private void upgradePasswordHash(Connection con, int userId, String plainPassword) {
        try {
            String bcryptHash = PasswordUtil.hash(plainPassword);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET password_hash = ? WHERE user_id = ?")) {
                ps.setString(1, bcryptHash);
                ps.setInt(2, userId);
                ps.executeUpdate();
                logger.info("Upgraded password hash to BCrypt for user_id=" + userId);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to upgrade password hash for user_id=" + userId, e);
        }
    }

    /**
     * Update last login timestamp.
     */
    private void updateLastLogin(Connection con, int userId) {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE users SET last_login = NOW() WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to update last_login for user_id=" + userId, e);
        }
    }
}
