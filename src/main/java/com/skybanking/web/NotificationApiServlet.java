package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/notifications")
public class NotificationApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        Object adminAttr = session.getAttribute("admin");
        
        if (userId == null && adminAttr == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try (Connection con = DBConnection.getConnection();
             PrintWriter out = resp.getWriter()) {
            
            String query;
            PreparedStatement ps;
            
            if (userId != null) {
                query = "SELECT notification_id, title, message, type, created_at FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC LIMIT 10";
                ps = con.prepareStatement(query);
                ps.setInt(1, userId);
            } else {
                // Admin notifications (where admin_id is either their specific ID or we assume null means 'all admins')
                // For simplicity, let's say admin_id is null for global admin notifications
                query = "SELECT notification_id, title, message, type, created_at FROM notifications WHERE admin_id IS NULL AND user_id IS NULL AND is_read = false ORDER BY created_at DESC LIMIT 10";
                ps = con.prepareStatement(query);
            }
            
            List<Map<String, Object>> notifications = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("id", rs.getInt("notification_id"));
                    notif.put("title", rs.getString("title"));
                    notif.put("message", rs.getString("message"));
                    notif.put("type", rs.getString("type"));
                    notif.put("created_at", rs.getTimestamp("created_at").toString());
                    notifications.add(notif);
                }
            }

            // Simple manual JSON construction to avoid pulling in Gson if not needed globally
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < notifications.size(); i++) {
                Map<String, Object> n = notifications.get(i);
                json.append("{")
                    .append("\"id\":").append(n.get("id")).append(",")
                    .append("\"title\":\"").append(escapeJson(n.get("title").toString())).append("\",")
                    .append("\"message\":\"").append(escapeJson(n.get("message").toString())).append("\",")
                    .append("\"type\":\"").append(escapeJson(n.get("type").toString())).append("\",")
                    .append("\"created_at\":\"").append(escapeJson(n.get("created_at").toString())).append("\"")
                    .append("}");
                if (i < notifications.size() - 1) json.append(",");
            }
            json.append("]");
            
            out.print(json.toString());
            
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = req.getParameter("action");
        if ("mark_read".equals(action)) {
            String notifIdStr = req.getParameter("id");
            Integer userId = (Integer) session.getAttribute("user_id");
            
            try (Connection con = DBConnection.getConnection()) {
                if (notifIdStr != null && !notifIdStr.isEmpty()) {
                    // Mark specific as read
                    int notifId = Integer.parseInt(notifIdStr);
                    try (PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = true, read_at = NOW() WHERE notification_id = ?")) {
                        ps.setInt(1, notifId);
                        ps.executeUpdate();
                    }
                } else if (userId != null) {
                    // Mark all as read for user
                    try (PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = true, read_at = NOW() WHERE user_id = ?")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                } else {
                    // Mark all admin notifications as read
                    try (PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = true, read_at = NOW() WHERE admin_id IS NULL AND user_id IS NULL")) {
                        ps.executeUpdate();
                    }
                }
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace();
            }
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
