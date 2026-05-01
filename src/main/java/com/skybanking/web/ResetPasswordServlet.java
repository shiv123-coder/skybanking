package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Password reset servlet using centralized PasswordUtil (BCrypt).
 */
@WebServlet("/resetpassword")
public class ResetPasswordServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ResetPasswordServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("forgotUsername") == null) {
            request.setAttribute("error", "Session expired. Please try again.");
            request.getRequestDispatcher("forgotpassword.jsp").forward(request, response);
            return;
        }

        String username = (String) session.getAttribute("forgotUsername");

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match!");
            request.getRequestDispatcher("resetpassword.jsp").forward(request, response);
            return;
        }

        if (newPassword.length() < 8) {
            request.setAttribute("error", "Password must be at least 8 characters.");
            request.getRequestDispatcher("resetpassword.jsp").forward(request, response);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String hashedPassword = PasswordUtil.hash(newPassword);
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash=? WHERE username=?")) {
                ps.setString(1, hashedPassword);
                ps.setString(2, username);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    session.removeAttribute("forgotUsername");
                    request.setAttribute("message", "Password updated successfully. Please login.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                } else {
                    request.setAttribute("error", "User not found!");
                    request.getRequestDispatcher("resetpassword.jsp").forward(request, response);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error resetting password", e);
            request.setAttribute("error", "Something went wrong. Try again.");
            request.getRequestDispatcher("resetpassword.jsp").forward(request, response);
        }
    }
}
