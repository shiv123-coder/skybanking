package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for login and OTP endpoints.
 * Blocks IP after max attempts are reached within the time window.
 */
@WebFilter(urlPatterns = {"/login", "/admin/login", "/verifyotp", "/sendotp"})
public class RateLimitFilter implements Filter {

    // Simple IP-based limiter: maximum 10 requests per 1-minute window
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 60 * 1000;

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    private static class RequestInfo {
        AtomicInteger count = new AtomicInteger(1);
        long startTime = System.currentTimeMillis();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Apply rate limiting only to POST requests (submissions)
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String clientIp = getClientIPAddress(req);
            
            RequestInfo info = requestCounts.compute(clientIp, (key, existingInfo) -> {
                long now = System.currentTimeMillis();
                if (existingInfo == null || (now - existingInfo.startTime > TIME_WINDOW_MS)) {
                    return new RequestInfo();
                }
                existingInfo.count.incrementAndGet();
                return existingInfo;
            });

            if (info.count.get() > MAX_REQUESTS) {
                res.setStatus(429); // Too Many Requests
                req.setAttribute("error", "Too many attempts. Please try again later.");
                req.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIPAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}
