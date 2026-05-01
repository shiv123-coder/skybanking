package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.service.AccountService;
import com.skybanking.service.LedgerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;

@WebServlet("/withdraw")
public class WithdrawServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String amountStr = req.getParameter("amount");

        if (amountStr == null || amountStr.isEmpty()) {
            resp.sendRedirect("withdraw.jsp?error=Amount is required");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            BigDecimal amount = new BigDecimal(amountStr);

            con.setAutoCommit(false);
            try {
                int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
                String idempotencyKey = "TXN-" + UUID.randomUUID().toString();
                
                LedgerService.withdraw(con, accountId, amount, "Cash withdrawal", idempotencyKey);
                
                con.commit();
                resp.sendRedirect("dashboard?message=Withdrawal Successful");
            } catch (Exception e) {
                con.rollback();
                resp.sendRedirect("withdraw.jsp?error=Withdrawal Failed: " + e.getMessage().replace("java.sql.SQLException: ", ""));
            }

        } catch (Exception e) {
            resp.sendRedirect("withdraw.jsp?error=Server Error. Try again.");
        }
    }
}