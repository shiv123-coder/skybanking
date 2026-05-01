package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/signupPage")
public class SignupPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();

        // Clear OTP/session flags for a fresh signup page
        session.removeAttribute("otp");
        session.removeAttribute("otpExpiry");
        session.removeAttribute("isSignup");

        // Set 'fromOTP' flag only if coming explicitly from verifyotp.jsp
        if ("true".equals(req.getParameter("fromOTP"))) {
            session.setAttribute("fromOTP", true);
        }
        
        // Clear 'fromOTP' flag if explicitly requested
        if ("true".equals(req.getParameter("clearOTP"))) {
            session.removeAttribute("fromOTP");
        }

        // If logged in AND not coming from OTP flow, redirect to dashboard
        if (session.getAttribute("user_id") != null && session.getAttribute("fromOTP") == null) {
            resp.sendRedirect("dashboard.jsp");
            return;
        }

        // Forward to signup.jsp
        req.getRequestDispatcher("signup.jsp").forward(req, resp);
    }
}
