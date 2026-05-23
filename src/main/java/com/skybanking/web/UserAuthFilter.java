package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * User Authentication Filter.
 * Enforces session checks globally across the application.
 * Excludes public resources like login, signup, static assets, and admin routes.
 */
@WebFilter(urlPatterns = {"/*"})
public class UserAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Allow public and excluded paths
        if (isAllowedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check user session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            // Redirect to login
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // Authenticated
        chain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/admin/") ||
               path.startsWith("/stripe/webhook") ||
               path.equals("/") ||
               path.equals("/index.jsp") ||
               path.equals("/login") ||
               path.equals("/login.jsp") ||
               path.equals("/signup") ||
               path.equals("/signup.jsp") ||
               path.equals("/signup-success.jsp") ||
               path.equals("/forgotpassword") ||
               path.equals("/forgotpassword.jsp") ||
               path.equals("/resetpassword") ||
               path.equals("/resetpassword.jsp") ||
               path.equals("/verifyotp") ||
               path.equals("/verifyotp.jsp") ||
               path.equals("/resendotp") ||
               path.equals("/error.jsp") ||
               path.equals("/ping") ||
               path.equals("/logout") ||
               path.equals("/logout.jsp");
    }
}
