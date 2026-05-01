package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.Random;

@WebServlet("/sendOtpProfile")
public class SendOtpProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // Read parameters from JSP
        String email = req.getParameter("email"); // matches input name in JSP
        String name = req.getParameter("name");   // keep name for OTP email
        if (name == null || name.trim().isEmpty()) {
            Object sessionUsername = session.getAttribute("username");
            if (sessionUsername != null) name = sessionUsername.toString();
        }

        // Validate email
        if(email == null || email.trim().isEmpty()){
            req.setAttribute("error", "Email is required for profile verification!");
            req.getRequestDispatcher("updateprofile.jsp").forward(req, resp);
            return;
        }

        // Save email/name for profile OTP
        session.setAttribute("profileEmailToVerify", email);
        session.setAttribute("profileNameToVerify", name);
        session.setAttribute("isSignup", false); // important for VerifyOtpServlet

        // Generate OTP
        int otp = 100000 + new Random().nextInt(900000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 5 * 60 * 1000); // 5 min expiry

        // Send OTP
        EmailUtil.sendOtp(email, name, otp);

        // Redirect to verify page
        resp.sendRedirect("verifyotp.jsp?type=profile");
    }
}
