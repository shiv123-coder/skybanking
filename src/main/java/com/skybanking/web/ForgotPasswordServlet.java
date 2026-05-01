package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/forgotpassword")
public class ForgotPasswordServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("forgotpassword.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Boolean isOtpVerified = (Boolean) session.getAttribute("isOtpVerified");

        if (isOtpVerified != null && isOtpVerified) {
            resetPassword(req, resp, session);
        } else {
            sendOtp(req, resp, session);
        }
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String newPassword = req.getParameter("newpassword");

        if (isInvalid(username) || isInvalid(newPassword)) {
            handleError(req, resp, "All fields are required.", "forgotpassword.jsp", null);
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET password_hash=? WHERE username=?")) {

            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, username);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                session.removeAttribute("isOtpVerified");
                handleError(req, resp, "Password reset successfully!", "login.jsp", null);
            } else {
                handleError(req, resp, "User not found.", "forgotpassword.jsp", null);
            }

        } catch (Exception e) {
            handleError(req, resp, "Database error. Try again.", "forgotpassword.jsp", e);
        }
    }

    private void sendOtp(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String mobile = req.getParameter("mobile");

        if (isInvalid(username) || isInvalid(email) || isInvalid(mobile)) {
            handleError(req, resp, "All fields are required to request OTP.", "forgotpassword.jsp", null);
            return;
        }

        session.setAttribute("forgotUsername", username);
        session.setAttribute("forgotEmail", email);
        session.setAttribute("forgotMobile", mobile);

        int otp = 100000 + new java.util.Random().nextInt(900000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 5 * 60 * 1000);
        session.setAttribute("isOtpVerified", false);

        // Send OTP email
        EmailUtil.sendOtp(email, (username != null && !username.trim().isEmpty()) ? username : "Valued Customer", otp);

        resp.sendRedirect("verifyotp.jsp?type=forgot");
    }

    private boolean isInvalid(String input) {
        return input == null || input.trim().isEmpty();
    }


}
