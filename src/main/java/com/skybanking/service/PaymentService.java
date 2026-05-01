package com.skybanking.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

/**
 * Service to handle Stripe payments for wallet top-ups.
 */
public class PaymentService {

    static {
        Stripe.apiKey = System.getenv("STRIPE_SECRET_KEY");
    }

    /**
     * Creates a Stripe Checkout Session for adding money to the wallet.
     *
     * @param userId      The user ID
     * @param accountId   The user's account ID
     * @param amount      The amount to add
     * @param baseUrl     The base URL of the application for redirects
     * @return The URL to redirect the user to Stripe Checkout
     * @throws StripeException If Stripe API fails
     */
    public static String createCheckoutSession(int userId, int accountId, BigDecimal amount, String baseUrl) throws StripeException {
        // Stripe expects amounts in cents for INR/USD.
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/payment-success.jsp")
                .setCancelUrl(baseUrl + "/payment-cancel.jsp")
                // Store internal identifiers in client_reference_id and metadata
                .setClientReferenceId("USER_" + userId)
                .putMetadata("account_id", String.valueOf(accountId))
                .putMetadata("user_id", String.valueOf(userId))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Wallet Top-up")
                                                                .setDescription("Add money to SkyBanking wallet")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
