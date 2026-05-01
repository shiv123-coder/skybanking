package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.service.AccountService;
import com.skybanking.service.PaymentService;

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

@WebServlet("/addmoney/checkout")
public class StripeCheckoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StripeCheckoutServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        int userId = (Integer) session.getAttribute("user_id");
        String amountStr = req.getParameter("amount");

        if (amountStr == null || amountStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Amount is required");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Amount must be positive");
                return;
            }

            int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);

            // Construct base URL for success/cancel redirects
            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String contextPath = req.getContextPath();
            String baseUrl = scheme + "://" + serverName + (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + contextPath;

            // Create Stripe Checkout Session
            String checkoutUrl = PaymentService.createCheckoutSession(userId, accountId, amount, baseUrl);

            // Redirect to Stripe's hosted payment page
            resp.sendRedirect(checkoutUrl);

        } catch (com.stripe.exception.StripeException se) {
            LOGGER.log(Level.SEVERE, "Stripe checkout failed", se);
            resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Payment gateway error: " + se.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal error initiating payment", e);
            resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Server Error. Try again.");
        }
    }
}
