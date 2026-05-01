package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@WebServlet("/userinfo")
public class UserInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");

        try (Connection con = DBConnection.getConnection()) {

            // 🔹 Fetch user info
            String userSql = "SELECT fullname, username, email, phone, created_at FROM users WHERE user_id=?";
            try (PreparedStatement ps = con.prepareStatement(userSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        req.setAttribute("fullname", rs.getString("fullname"));
                        req.setAttribute("username", rs.getString("username"));
                        req.setAttribute("email", rs.getString("email"));
                        req.setAttribute("mobile", rs.getString("phone"));

                        Timestamp createdAt = rs.getTimestamp("created_at");
                        if (createdAt != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                            req.setAttribute("signupDate", sdf.format(createdAt));
                        } else {
                            req.setAttribute("signupDate", "Not Available");
                        }
                    }
                }
            }

            // 🔹 Fetch account info
            String accountSql = "SELECT account_id, balance FROM accounts WHERE user_id=? AND is_active=true LIMIT 1";
            try (PreparedStatement psAcc = con.prepareStatement(accountSql)) {
                psAcc.setInt(1, userId);
                try (ResultSet rsAcc = psAcc.executeQuery()) {
                    if (rsAcc.next()) {
                        req.setAttribute("accountId", rsAcc.getString("account_id"));
                        req.setAttribute("accountCode", rsAcc.getString("account_id"));
                        req.setAttribute("balance", rsAcc.getBigDecimal("balance"));
                    } else {
                        req.setAttribute("accountId", "Not Created Yet");
                        req.setAttribute("accountCode", "Not Assigned");
                        req.setAttribute("balance", BigDecimal.ZERO);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to fetch user info.");
        }

        req.getRequestDispatcher("userinfo.jsp").forward(req, resp);
    }
}
