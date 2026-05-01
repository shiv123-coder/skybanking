package com.skybanking.admin;

import com.skybanking.util.LoggerUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Admin logout servlet to handle admin session termination.
 * Logs the logout activity and redirects to admin login page.
 */
@WebServlet("/admin/logout")
public class AdminLogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            String adminUsername = (String) session.getAttribute("admin");
            Integer adminId = (Integer) session.getAttribute("admin_id");
            
            // Log admin logout
            if (adminId != null) {
                LoggerUtil.logAdmin(adminId, "ADMIN_LOGOUT", null, 
                                  "Admin logout: " + adminUsername);
            }
            
            // Invalidate session
            session.invalidate();
        }
        
        // Redirect to admin login page
        resp.sendRedirect("login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
