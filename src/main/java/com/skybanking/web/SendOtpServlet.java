package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/sendotp")
public class SendOtpServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SendOtpServlet.class.getName());

    // ✅ Fix: allow GET (from redirects) by calling doPost()
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        String type = req.getParameter("type"); // signup, forgot, profile
        if (type == null) type = "signup";

        String fullname = null, username = null, email = null, mobile = null, password = null;

        try (Connection con = DBConnection.getConnection()) {

            switch (type) {
                case "signup" -> {
                    fullname = req.getParameter("fullname");
                    username = req.getParameter("username");
                    email = req.getParameter("email");
                    mobile = req.getParameter("mobile");
                    password = req.getParameter("password");

                    // If null → use stored session values (Resend case)
                    if (fullname == null) fullname = (String) session.getAttribute("fullname");
                    if (username == null) username = (String) session.getAttribute("username");
                    if (email == null) email = (String) session.getAttribute("email");
                    if (mobile == null) mobile = (String) session.getAttribute("mobile");
                    if (password == null) password = (String) session.getAttribute("password");

                    if (fullname == null || username == null || email == null || mobile == null || password == null) {
                        req.setAttribute("error", "Signup details missing. Please try again.");
                        req.getRequestDispatcher("signup.jsp").forward(req, resp);
                        return;
                    }

                    // Save signup info in session (for later OTP verification)
                    session.setAttribute("fullname", fullname);
                    session.setAttribute("username", username);
                    session.setAttribute("email", email);
                    session.setAttribute("mobile", mobile);
                    session.setAttribute("password", password);

                    logger.info("Signup OTP request for user: " + username + ", email: " + email);
                }

                case "forgot" -> {
                    username = req.getParameter("username");
                    email = req.getParameter("email");
                    mobile = req.getParameter("mobile");

                    if (username != null && email != null && mobile != null) {
                        // First-time forgot request
                        try (PreparedStatement ps = con.prepareStatement(
                                "SELECT user_id FROM users WHERE username=? AND email=? AND phone=?")) {
                            ps.setString(1, username);
                            ps.setString(2, email);
                            ps.setString(3, mobile);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next()) {
                                    req.setAttribute("error", "User details do not match!");
                                    req.getRequestDispatcher("forgotpassword.jsp").forward(req, resp);
                                    return;
                                } else {
                                    session.setAttribute("forgotUserId", rs.getInt("user_id"));
                                }
                            }
                        }
                        session.setAttribute("forgotUsername", username);
                        logger.info("Forgot password OTP request for user: " + username);
                    } else {
                        // Resend case → pull from session
                        Integer userId = (Integer) session.getAttribute("forgotUserId");
                        username = (String) session.getAttribute("forgotUsername");
                        if (userId == null || username == null) {
                            req.setAttribute("error", "Session expired. Please try again.");
                            req.getRequestDispatcher("forgotpassword.jsp").forward(req, resp);
                            return;
                        }
                        try (PreparedStatement ps = con.prepareStatement(
                                "SELECT email FROM users WHERE user_id=?")) {
                            ps.setInt(1, userId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    email = rs.getString("email");
                                }
                            }
                        }
                        logger.info("Forgot password OTP resend for user: " + username);
                    }
                }

                case "profile" -> {
                    email = req.getParameter("email");
                    if (email == null) {
                        email = (String) session.getAttribute("profileEmailToVerify");
                    }
                    if (email == null) {
                        req.setAttribute("error", "Email missing. Please try again.");
                        req.getRequestDispatcher("updateProfile.jsp").forward(req, resp);
                        return;
                    }
                    session.setAttribute("profileEmailToVerify", email);

                    logger.info("Profile email verification OTP request for email: " + email);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during OTP send", e);
            req.setAttribute("error", "Database error. Try again.");
            String page = switch (type) {
                case "signup" -> "signup.jsp";
                case "forgot" -> "forgotpassword.jsp";
                case "profile" -> "updateProfile.jsp";
                default -> "login.jsp";
            };
            req.getRequestDispatcher(page).forward(req, resp);
            return;
        }

        try {
            // ✅ Generate OTP and store in session
            int otp = 100000 + new Random().nextInt(900000);
            session.setAttribute("otp", otp);
            session.setAttribute("otpExpiry", System.currentTimeMillis() + 5 * 60 * 1000);
            session.setAttribute("isOtpVerified", false);

            // ✅ Send OTP email (with professional fallback name)
            String displayName = username;
            if (displayName == null || displayName.trim().isEmpty()) {
                Object sessionName = session.getAttribute("fullname");
                if (sessionName == null || sessionName.toString().trim().isEmpty()) {
                    displayName = "Valued Customer";
                } else {
                    displayName = sessionName.toString();
                }
            }
            EmailUtil.sendOtp(email, displayName, otp);

            logger.info("OTP sent successfully to: " + email + " [Type=" + type + "]");

            // Redirect back to verify page
            resp.sendRedirect("verifyotp.jsp?type=" + type);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending OTP email", e);
            req.setAttribute("error", "Failed to send OTP. Please try again later.");
            String page = switch (type) {
                case "signup" -> "signup.jsp";
                case "forgot" -> "forgotpassword.jsp";
                case "profile" -> "updateProfile.jsp";
                default -> "login.jsp";
            };
            req.getRequestDispatcher(page).forward(req, resp);
        }
    }
}
