package com.skybanking.web;

import com.google.gson.JsonSyntaxException;
import com.skybanking.DBConnection;
import com.skybanking.service.LedgerService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/stripe/webhook")
public class StripeWebhookServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StripeWebhookServlet.class.getName());
    private static final String ENDPOINT_SECRET = System.getenv("STRIPE_WEBHOOK_SECRET");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = getBody(req);
        String sigHeader = req.getHeader("Stripe-Signature");
        Event event = null;

        try {
            if (ENDPOINT_SECRET != null && !ENDPOINT_SECRET.isEmpty() && !ENDPOINT_SECRET.equals("whsec_placeholder")) {
                // Verify webhook signature if real secret exists
                event = Webhook.constructEvent(payload, sigHeader, ENDPOINT_SECRET);
            } else {
                // Parse directly if no secret (e.g. testing)
                event = com.stripe.model.Event.GSON.fromJson(payload, com.stripe.model.Event.class);
            }
        } catch (JsonSyntaxException | SignatureVerificationException e) {
            LOGGER.log(Level.WARNING, "Stripe webhook signature/parsing failed", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Handle the event
        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                StripeObject stripeObject = dataObjectDeserializer.getObject().get();
                if (stripeObject instanceof Session) {
                    Session session = (Session) stripeObject;
                    handleSuccessfulCheckout(session, event.getId());
                }
            } else {
                LOGGER.warning("Deserialization failed for Stripe checkout object");
            }
        }

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleSuccessfulCheckout(Session session, String eventId) {
        if (!"paid".equals(session.getPaymentStatus())) {
            LOGGER.info("Checkout session completed but not paid. ID: " + session.getId());
            return;
        }

        try {
            String accountIdStr = session.getMetadata().get("account_id");
            if (accountIdStr == null) {
                LOGGER.severe("Account ID missing in Stripe metadata for session: " + session.getId());
                return;
            }

            int accountId = Integer.parseInt(accountIdStr);
            // Amount comes in cents
            BigDecimal amount = new BigDecimal(session.getAmountTotal()).divide(new BigDecimal("100"));
            
            // The Stripe Event ID is globally unique and guarantees we process this exactly once
            String idempotencyKey = "STRIPE_" + eventId;

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    LedgerService.deposit(con, accountId, amount, "Stripe wallet top-up via " + session.getPaymentMethodTypes().get(0), idempotencyKey);
                    con.commit();
                    LOGGER.info("Successfully processed Stripe deposit for account: " + accountId + " amount: " + amount);
                } catch (Exception e) {
                    con.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process Stripe checkout session: " + session.getId(), e);
        }
    }

    private String getBody(HttpServletRequest request) throws IOException {
        try (InputStream inputStream = request.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
