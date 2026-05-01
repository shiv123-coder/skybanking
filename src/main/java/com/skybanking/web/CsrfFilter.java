package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF Protection Filter.
 * Generates a CSRF token for the session and validates it on state-changing requests (POST, PUT, DELETE).
 */
@WebFilter(urlPatterns = {"/*"})
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_NAME = "csrf_token";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(true);

        // Generate CSRF token if one doesn't exist
        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_NAME);
        if (sessionToken == null) {
            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            sessionToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            session.setAttribute(CSRF_TOKEN_NAME, sessionToken);
        }

        // Make token available to JSP as a request attribute (for hidden form fields)
        req.setAttribute(CSRF_TOKEN_NAME, sessionToken);

        String method = req.getMethod();
        // Check CSRF token for methods that change state
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            // Bypass CSRF for Stripe Webhooks (they use their own signature validation)
            if (req.getRequestURI().contains("/stripe/webhook")) {
                chain.doFilter(request, response);
                return;
            }

            String requestToken = req.getParameter(CSRF_TOKEN_NAME);
            if (requestToken == null || !requestToken.equals(sessionToken)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
