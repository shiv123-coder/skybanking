package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;
import org.json.JSONObject;

@WebServlet("/getBalance")
public class GetBalanceServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json");

        try (PrintWriter out = resp.getWriter()) {
            if (session == null || session.getAttribute("user_id") == null) {
                JSONObject errorJson = new JSONObject();
                errorJson.put("error", "User not logged in");
                out.print(errorJson.toString());
                return;
            }

            int userId = (Integer) session.getAttribute("user_id");

            try (Connection con = DBConnection.getConnection()) {
                String sql = "SELECT account_id, balance FROM accounts WHERE user_id=? AND is_active=true LIMIT 1";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            JSONObject json = new JSONObject();
                            json.put("accountId", rs.getInt("account_id"));
                            json.put("balance", rs.getBigDecimal("balance"));
                            out.print(json.toString());
                        } else {
                            JSONObject json = new JSONObject();
                            json.put("accountId", "Not Created Yet");
                            json.put("balance", BigDecimal.ZERO);
                            out.print(json.toString());
                        }
                    }
                }
            } catch (Exception e) {
                handleError(req, resp, "Failed to fetch balance", "userinfo.jsp", e);
            }
        }
    }
}
