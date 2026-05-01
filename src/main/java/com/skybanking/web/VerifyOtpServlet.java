package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/verifyotp")
public class VerifyOtpServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(VerifyOtpServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("otp") == null) {
            String type = req.getParameter("type");
            if ("forgot".equals(type)) {
                req.setAttribute("error", "Session expired. Please request OTP again.");
                req.getRequestDispatcher("forgotpassword.jsp").forward(req, resp);
            } else if ("profile".equals(type)) {
                req.setAttribute("error", "Session expired. Please try updating again.");
                req.getRequestDispatcher("updateProfile.jsp").forward(req, resp);
            } else {
                req.setAttribute("error", "Signup data missing. Please sign up again.");
                req.getRequestDispatcher("signup.jsp").forward(req, resp);
            }
            return;
        }

        req.getRequestDispatcher("verifyotp.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("otp") == null) {
            forwardWithError(req, resp, "forgotpassword.jsp",
                    "Session expired. Please request OTP again.");
            return;
        }

        // Retrieve OTP and expiry
        Integer generatedOtp = (Integer) session.getAttribute("otp");
        Long expiry = (Long) session.getAttribute("otpExpiry");
        String inputOtp = req.getParameter("otp");

        if (generatedOtp == null || expiry == null || inputOtp == null || inputOtp.trim().isEmpty()) {
            forwardWithError(req, resp, "verifyotp.jsp",
                    "Invalid OTP submission. Try again.");
            return;
        }

        // Check OTP expiry
        if (System.currentTimeMillis() > expiry) {
            session.removeAttribute("otp");
            session.removeAttribute("otpExpiry");
            forwardWithError(req, resp, "verifyotp.jsp",
                    "OTP expired. Please request a new OTP.");
            return;
        }

        // Check OTP correctness
        if (!String.valueOf(generatedOtp).equals(inputOtp.trim())) {
            forwardWithError(req, resp, "verifyotp.jsp",
                    "Incorrect OTP. Please try again.");
            return;
        }

        // Determine OTP type
        String type = req.getParameter("type");
        if (type == null) type = "signup"; // default fallback

        switch (type) {
            case "signup" -> handleSignupOtp(session, req, resp);
            case "forgot" -> handleForgotPasswordOtp(session, req, resp);
            case "profile" -> handleProfileOtp(session, req, resp);
            default -> forwardWithError(req, resp, "forgotpassword.jsp",
                    "Unknown OTP operation type.");
        }

        // Cleanup OTP after success (only for non-signup flows)
        if (!"signup".equals(type)) {
            session.removeAttribute("otp");
            session.removeAttribute("otpExpiry");
            // For forgot/profile we can also clear otpPending if ever set
            session.removeAttribute("otpPending");
        }
    }

    // ---------------- Helper Methods ----------------

    private void handleSignupOtp(HttpSession session, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullname = (String) session.getAttribute("fullname");
        String username = (String) session.getAttribute("username");
        String email = (String) session.getAttribute("email");
        String mobile = (String) session.getAttribute("mobile");
        String password = (String) session.getAttribute("password");

        if (username == null || password == null || fullname == null) {
            forwardWithError(req, resp, "signup.jsp",
                    "Signup data missing. Please sign up again.");
            return;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            logger.info("Starting signup transaction for user: " + username);

            // Check if username already exists
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM users WHERE username=?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        forwardWithError(req, resp, "signup.jsp",
                                "Username already exists. Choose a different one.");
                        return;
                    }
                }
            }

            // Insert user (schema: users has phone, not mobile)
            int userId;
            String insertUserSql = "INSERT INTO users (fullname, username, email, phone, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fullname);
                ps.setString(2, username);
                ps.setString(3, email);
                ps.setString(4, mobile); // store mobile into phone column
                ps.setString(5, PasswordUtil.hash(password));
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain user_id.");
                    }
                }
            }

            // Create default account for user (schema: accounts table)
            int accountId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO accounts (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        accountId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain account_id.");
                    }
                }
            }

            con.commit();

            // ✅ Clear OTP Pending after success
            session.removeAttribute("otpPending");

            // Invalidate session AFTER signup
            session.invalidate();

            // Prepare friendly display code
            String displayAccountCode = username.substring(0, Math.min(4, username.length())).toUpperCase()
                    + String.format("%04d", accountId);

            req.setAttribute("message", "Signup successful! Your account was created.");
            req.setAttribute("username", username);
            req.setAttribute("accountCode", displayAccountCode);
            req.getRequestDispatcher("signup-success.jsp").forward(req, resp);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during signup", e);
            logger.log(Level.SEVERE, "Error details: " + e.getMessage());
            logger.log(Level.SEVERE, "SQL State: " + e.getSQLState());
            logger.log(Level.SEVERE, "Error Code: " + e.getErrorCode());
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
            }
            forwardWithError(req, resp, "signup.jsp",
                    "Database error during signup: " + e.getMessage() + ". Please try again.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during signup", e);
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error during rollback", rollbackEx);
            }
            forwardWithError(req, resp, "signup.jsp",
                    "Unexpected error during signup. Please try again.");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
    }

    private void handleForgotPasswordOtp(HttpSession session, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        session.setAttribute("isOtpVerified", true);
        req.setAttribute("message", "OTP verified successfully! You can now reset your password.");
        req.getRequestDispatcher("resetpassword.jsp").forward(req, resp);
    }

    private void handleProfileOtp(HttpSession session, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer userIdObj = (Integer) session.getAttribute("user_id");
        String newEmail = (String) session.getAttribute("profileEmailToVerify");

        if (userIdObj == null || newEmail == null) {
            forwardWithError(req, resp, "updateProfile.jsp",
                    "Profile update data missing. Try again.");
            return;
        }

        int userId = userIdObj;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET email=? WHERE user_id=?")) {
            ps.setString(1, newEmail);
            ps.setInt(2, userId);
            ps.executeUpdate();

            session.setAttribute("isOtpVerified", true);
            session.removeAttribute("profileEmailToVerify");

            req.setAttribute("message", "Email updated successfully!");
            req.getRequestDispatcher("updateProfile.jsp").forward(req, resp);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB error updating profile email", e);
            forwardWithError(req, resp, "updateProfile.jsp",
                    "Database error while updating email.");
        }
    }



    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
                                  String page, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.getRequestDispatcher(page).forward(req, resp);
    }
}
