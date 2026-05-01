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
import com.skybanking.service.QRService;

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String amountStr = req.getParameter("amount");
        String receiverAccountStr = req.getParameter("receiver_account");
        
        // Dynamic QR security fields
        String tsStr = req.getParameter("ts");
        String sigStr = req.getParameter("sig");

        if (amountStr == null || amountStr.isEmpty() || receiverAccountStr == null || receiverAccountStr.isEmpty()) {
            resp.sendRedirect("transfer.jsp?error=All fields are required");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            BigDecimal amount = new BigDecimal(amountStr);
            int receiverAccountId = Integer.parseInt(receiverAccountStr);
            
            // If paying via Dynamic QR, verify HMAC signature and timestamp
            if (tsStr != null && sigStr != null && !tsStr.isEmpty() && !sigStr.isEmpty()) {
                long timestamp = Long.parseLong(tsStr);
                if (!QRService.verifyDynamicPayload(receiverAccountId, amountStr, timestamp, sigStr)) {
                    resp.sendRedirect("transfer.jsp?error=Invalid or expired QR payment request. Please ask for a new QR code.");
                    return;
                }
            }

            con.setAutoCommit(false);
            try {
                int senderAccountId = AccountService.getOrCreatePrimaryAccount(con, userId);
                String idempotencyKey = "TXN-" + UUID.randomUUID().toString();
                
                LedgerService.transfer(con, senderAccountId, receiverAccountId, amount, "Fund transfer", idempotencyKey);
                
                con.commit();
                resp.sendRedirect("dashboard?message=Transfer Successful");
            } catch (Exception e) {
                con.rollback();
                resp.sendRedirect("transfer.jsp?error=Transfer Failed: " + e.getMessage().replace("java.sql.SQLException: ", "").replace("java.lang.IllegalArgumentException: ", ""));
            }

        } catch (NumberFormatException e) {
            resp.sendRedirect("transfer.jsp?error=Invalid amount or account number format");
        } catch (Exception e) {
            resp.sendRedirect("transfer.jsp?error=Server Error. Try again.");
        }
    }
}
