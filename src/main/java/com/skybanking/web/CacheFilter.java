package com.skybanking.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter to manage Cache-Control headers.
 * Static assets are cached for performance.
 * Dynamic pages (JSPs, servlets) are NOT cached for security and data freshness.
 */
@WebFilter(
    urlPatterns = {"/*"}, 
    dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.INCLUDE}
)
public class CacheFilter implements Filter {

    private static final String[] STATIC_EXTENSIONS = {
        ".css", ".js", ".jpg", ".jpeg", ".png", ".gif", 
        ".svg", ".ico", ".woff", ".woff2", ".ttf", ".eot"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String uri = req.getRequestURI().toLowerCase();
        
        if (isStaticAsset(uri)) {
            // Cache static assets for 1 year
            res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        } else {
            // Do not cache dynamic content
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);
        }

        chain.doFilter(request, response);
    }
    
    private boolean isStaticAsset(String uri) {
        for (String ext : STATIC_EXTENSIONS) {
            if (uri.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
