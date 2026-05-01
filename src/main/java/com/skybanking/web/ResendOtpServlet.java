package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.Random;

@WebServlet("/resendotp")
public class ResendOtpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            resp.sendRedirect("signup.jsp");
            return;
        }

        String email = (String) session.getAttribute("email");
        String username = (String) session.getAttribute("username"); // may be null for profile update
        boolean isSignup = session.getAttribute("isSignup") != null && (boolean) session.getAttribute("isSignup");

        // --- Generate new OTP ---
        int otp = 100000 + new Random().nextInt(900000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + (5 * 60 * 1000)); // 5 min

        // --- Send OTP ---
        EmailUtil.sendOtp(email, username != null ? username : "User", otp);

        if (isSignup) {
            // For signup flow, redirect to verify page
            resp.sendRedirect("verifyotp.jsp");
        } else {
            // For profile update (userinfo.jsp), respond with message (AJAX-friendly)
            resp.getWriter().write("A new OTP has been sent to your email.");
        }
    }
}
