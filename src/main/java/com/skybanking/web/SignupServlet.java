package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullname = req.getParameter("fullname");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String mobile = req.getParameter("mobile");

        if (isInvalid(fullname) || isInvalid(username) || isInvalid(password)
                || isInvalid(email) || isInvalid(mobile)) {
            req.setAttribute("error", "All fields are required.");
            req.getRequestDispatcher("signup.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("fullname", fullname);
        session.setAttribute("username", username);
        session.setAttribute("password", password);
        session.setAttribute("email", email);
        session.setAttribute("mobile", mobile);

        int otp = generateOtp();
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 5 * 60 * 1000); // 5 min expiry

        // 🔑 Mark OTP as pending → blocks login until verified
        session.setAttribute("otpPending", true);

        // ✅ Forward instead of redirect (keeps POST)
        req.setAttribute("type", "signup");
        req.getRequestDispatcher("sendotp").forward(req, resp);
    }

    private boolean isInvalid(String input) {
        return input == null || input.trim().isEmpty();
    }

    private int generateOtp() {
        return 100000 + (int) (Math.random() * 900000); // 6-digit OTP
    }
}
