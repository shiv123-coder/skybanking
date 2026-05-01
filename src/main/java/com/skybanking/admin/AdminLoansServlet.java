package com.skybanking.admin;

import com.skybanking.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/loans")
public class AdminLoansServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		if (session == null || session.getAttribute("admin") == null) {
			resp.sendRedirect("login");
			return;
		}
		try (Connection con = DBConnection.getConnection();
			 Statement st = con.createStatement();
			 ResultSet rs = st.executeQuery("SELECT l.*, u.username FROM loans l JOIN users u ON u.user_id = l.user_id ORDER BY l.created_at DESC")) {
			java.util.List<java.util.Map<String, Object>> loans = new java.util.ArrayList<>();
			while (rs.next()) {
				java.util.Map<String, Object> row = new java.util.HashMap<>();
				row.put("loan_id", rs.getInt("loan_id"));
				row.put("username", rs.getString("username"));
				row.put("principal", rs.getBigDecimal("principal"));
				row.put("interest_rate", rs.getBigDecimal("interest_rate"));
				row.put("tenure_months", rs.getInt("tenure_months"));
				row.put("emi", rs.getBigDecimal("emi"));
				row.put("status", rs.getString("status"));
				row.put("created_at", rs.getTimestamp("created_at"));
				loans.add(row);
			}
			req.setAttribute("loans", loans);
			req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp);
		} catch (SQLException e) {
			resp.sendError(500, "Failed to load loans");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		String loanIdStr = req.getParameter("loan_id");
		if (loanIdStr == null) {
			resp.sendError(400, "loan_id required");
			return;
		}
		int loanId = Integer.parseInt(loanIdStr);
		try (Connection con = DBConnection.getConnection()) {
			if ("approve".equals(action)) {
				try (PreparedStatement ps = con.prepareStatement("UPDATE loans SET status='APPROVED' WHERE loan_id=?")) {
					ps.setInt(1, loanId);
					ps.executeUpdate();
				}
			} else if ("reject".equals(action)) {
				try (PreparedStatement ps = con.prepareStatement("UPDATE loans SET status='REJECTED' WHERE loan_id=?")) {
					ps.setInt(1, loanId);
					ps.executeUpdate();
				}
			} else if ("disburse".equals(action)) {
				// Mark as disbursed and credit to user's account
				con.setAutoCommit(false);
				try (PreparedStatement ps = con.prepareStatement("UPDATE loans SET status='DISBURSED' WHERE loan_id=?")) {
					ps.setInt(1, loanId);
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement("SELECT user_id, principal FROM loans WHERE loan_id=?")) {
					ps.setInt(1, loanId);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							int userId = rs.getInt(1);
							java.math.BigDecimal principal = rs.getBigDecimal(2);
							try (PreparedStatement acc = con.prepareStatement("UPDATE accounts SET balance=balance+? WHERE user_id=? AND is_active=true")) {
								acc.setBigDecimal(1, principal);
								acc.setInt(2, userId);
								acc.executeUpdate();
							}
						}
					}
				}
				con.commit();
			}
		} catch (SQLException e) {
			resp.sendError(500, "Failed to update loan");
			return;
		}
		resp.sendRedirect("/BankingWebApp/admin/loans");
	}
}


