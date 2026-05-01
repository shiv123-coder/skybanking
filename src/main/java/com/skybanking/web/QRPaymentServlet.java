package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.service.AccountService;
import com.skybanking.service.QRService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/qr")
public class QRPaymentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(QRPaymentServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        
        try (Connection con = DBConnection.getConnection()) {
            int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
            
            // Base URL
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String baseUrl = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + req.getContextPath();

            // Generate Static QR
            String staticUrl = QRService.generateStaticQRPayload(accountId, baseUrl);
            String staticQrBase64 = QRService.generateQRCodeImageBase64(staticUrl, 300, 300);
            
            req.setAttribute("staticQrCode", staticQrBase64);
            req.setAttribute("accountId", accountId);

            // Check if dynamic requested
            String amountStr = req.getParameter("amount");
            if (amountStr != null && !amountStr.isEmpty()) {
                BigDecimal amount = new BigDecimal(amountStr);
                String dynamicUrl = QRService.generateDynamicQRPayload(accountId, amount, baseUrl);
                String dynamicQrBase64 = QRService.generateQRCodeImageBase64(dynamicUrl, 300, 300);
                req.setAttribute("dynamicQrCode", dynamicQrBase64);
                req.setAttribute("dynamicAmount", amount);
            }

            req.getRequestDispatcher("/qrpayment.jsp").forward(req, resp);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load QR page", e);
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Failed to load QR Code generator");
        }
    }
}
