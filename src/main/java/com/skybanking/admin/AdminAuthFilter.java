package com.skybanking.admin;

import com.skybanking.util.LoggerUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/admin/*")
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        LoggerUtil.logSystem("AdminAuthFilter", "INIT", "Admin authentication filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());

        // Allow login page & static resources
        if (isAllowedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (session == null || session.getAttribute("admin") == null) {
            LoggerUtil.logSecurity(null, "ADMIN_ACCESS_DENIED",
                    getClientIP(httpRequest),
                    httpRequest.getHeader("User-Agent"),
                    "Unauthorized access attempt to: " + path);
            httpResponse.sendRedirect(contextPath + "/admin/login");
            return;
        }

        String adminUsername = (String) session.getAttribute("admin");
        Integer adminId = (Integer) session.getAttribute("admin_id");

        if (adminUsername == null || adminId == null) {
            session.invalidate();
            LoggerUtil.logSecurity(null, "ADMIN_SESSION_INVALID",
                    getClientIP(httpRequest),
                    httpRequest.getHeader("User-Agent"),
                    "Invalid admin session data");
            httpResponse.sendRedirect(contextPath + "/admin/login");
            return;
        }

        LoggerUtil.logAdmin(adminId, "ADMIN_ACCESS", null,
                "Accessed: " + path + " from IP: " + getClientIP(httpRequest));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LoggerUtil.logSystem("AdminAuthFilter", "DESTROY", "Admin authentication filter destroyed");
    }

    private boolean isAllowedPath(String path) {
        return path.equals("/admin/login") || path.equals("/admin/") ||
               path.startsWith("/admin/css/") ||
               path.startsWith("/admin/js/") ||
               path.startsWith("/admin/images/") ||
               path.startsWith("/admin/assets/");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) return xfHeader.split(",")[0].trim();
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) return xRealIP;
        return request.getRemoteAddr();
    }
}
