package com.skybanking.web;

import com.skybanking.DBConnection;
import com.skybanking.service.LedgerService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/payment-success")
public class PaymentSuccessServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PaymentSuccessServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getParameter("session_id");

        if (sessionId == null || sessionId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
            return;
        }

        try {
            // Retrieve the session from Stripe
            Session session = Session.retrieve(sessionId);

            if ("paid".equals(session.getPaymentStatus())) {
                String accountIdStr = session.getMetadata().get("account_id");
                if (accountIdStr != null) {
                    int accountId = Integer.parseInt(accountIdStr);
                    // Amount comes in cents
                    BigDecimal amount = new BigDecimal(session.getAmountTotal()).divide(new BigDecimal("100"));
                    
                    // Same idempotency key as the webhook to ensure exactly-once semantics
                    String idempotencyKey = "STRIPE_SESSION_" + session.getId();

                    try (Connection con = DBConnection.getConnection()) {
                        con.setAutoCommit(false);
                        try {
                            LedgerService.deposit(con, accountId, amount, "Stripe wallet top-up via " + session.getPaymentMethodTypes().get(0), idempotencyKey);
                            con.commit();
                            LOGGER.info("Successfully processed Stripe deposit synchronously for account: " + accountId + " amount: " + amount);
                        } catch (Exception e) {
                            con.rollback();
                            // If it's a duplicate transaction (e.g. webhook already fired), it will be ignored by LedgerService.
                            // If it's a real error, log it.
                            LOGGER.log(Level.SEVERE, "Failed to deposit money via synchronous success handler", e);
                        }
                    }
                }
            }

            // Redirect to the actual success UI page
            resp.sendRedirect(req.getContextPath() + "/payment-success.jsp");

        } catch (StripeException e) {
            LOGGER.log(Level.SEVERE, "Stripe session retrieval failed for session: " + sessionId, e);
            resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Payment verification failed");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal error in payment success verification", e);
            resp.sendRedirect(req.getContextPath() + "/addmoney.jsp?error=Server error during payment verification");
        }
    }
}
