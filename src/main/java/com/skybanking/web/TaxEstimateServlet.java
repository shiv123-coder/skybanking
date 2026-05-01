package com.skybanking.web;

import com.skybanking.util.TaxManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/tax/estimate")
public class TaxEstimateServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String amountStr = req.getParameter("amount");
		String type = req.getParameter("type"); // deposit|withdraw
		if (amountStr == null || amountStr.trim().isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Amount required");
			return;
		}
		BigDecimal amount = new BigDecimal(amountStr);
		BigDecimal tax = "withdraw".equalsIgnoreCase(type) ? TaxManager.calculateTDS(amount) : TaxManager.calculateGST(amount);
		BigDecimal total = TaxManager.calculateTotalAmount(amount, tax);
		resp.setContentType("application/json");
		resp.getWriter().write("{\"tax\":" + tax + ",\"total\":" + total + "}");
	}
}


