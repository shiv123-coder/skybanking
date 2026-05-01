package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Security Headers Filter.
 * Adds production-ready HTTP security headers to all responses.
 */
@WebFilter(urlPatterns = {"/*"})
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse res = (HttpServletResponse) response;
        
        // Prevent clickjacking
        res.setHeader("X-Frame-Options", "DENY");
        
        // Enable XSS filtering
        res.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Prevent MIME sniffing
        res.setHeader("X-Content-Type-Options", "nosniff");
        
        // Ensure HTTPS (Optional - enable when behind full SSL termination)
        // res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Clear site data (on logout or specific responses)
        // res.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");

        chain.doFilter(request, response);
    }
}
