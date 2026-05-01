package com.skybanking.web;

import com.skybanking.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet({"/loan", "/loan/apply", "/loan/status"})
public class LoanServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String path = req.getServletPath();
        if ("/loan/status".equals(path)) {
            loadStatus(req);
            // Fixed: forward to JSP directly under webapp
            req.getRequestDispatcher("/loanStatus.jsp").forward(req, resp);
            return;
        }

        // Fixed: forward to JSP directly under webapp
        req.getRequestDispatcher("/loan.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String path = req.getServletPath();
        if ("/loan/apply".equals(path)) {
            applyLoan(req, resp, (Integer) session.getAttribute("user_id"));
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void applyLoan(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        String principalStr = req.getParameter("principal");
        String rateStr = req.getParameter("rate");
        String tenureStr = req.getParameter("tenure");

        if (principalStr == null || rateStr == null || tenureStr == null) {
            resp.sendRedirect(req.getContextPath() + "/loan?error=Missing fields");
            return;
        }

        BigDecimal principal = new BigDecimal(principalStr);
        BigDecimal rate = new BigDecimal(rateStr);
        int tenure = Integer.parseInt(tenureStr);

        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal onePlusRPowerN = monthlyRate.add(BigDecimal.ONE).pow(tenure);
        BigDecimal emi = principal.multiply(monthlyRate).multiply(onePlusRPowerN)
                .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                    "INSERT INTO loans (user_id, principal, interest_rate, tenure_months, emi) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setBigDecimal(2, principal);
            ps.setBigDecimal(3, rate);
            ps.setInt(4, tenure);
            ps.setBigDecimal(5, emi);
            ps.executeUpdate();
        } catch (SQLException e) {
            resp.sendRedirect(req.getContextPath() + "/loan?error=Database error");
            return;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }

        resp.sendRedirect(req.getContextPath() + "/loan/status");
    }

    private void loadStatus(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        int userId = (Integer) session.getAttribute("user_id");

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM loans WHERE user_id = ? ORDER BY created_at DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            java.util.List<java.util.Map<String, Object>> loans = new java.util.ArrayList<>();
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("loan_id", rs.getInt("loan_id"));
                row.put("principal", rs.getBigDecimal("principal"));
                row.put("interest_rate", rs.getBigDecimal("interest_rate"));
                row.put("tenure_months", rs.getInt("tenure_months"));
                row.put("emi", rs.getBigDecimal("emi"));
                row.put("status", rs.getString("status"));
                row.put("created_at", rs.getTimestamp("created_at"));
                loans.add(row);
            }

            req.setAttribute("loans", loans);
        } catch (Exception ignored) {}
        finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored2) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored2) {}
            try { if (con != null) con.close(); } catch (Exception ignored2) {}
        }
    }
}
