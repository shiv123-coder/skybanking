package com.skybanking.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for generating secure QR codes for payments.
 */
public class QRService {

    private static final Logger LOGGER = Logger.getLogger(QRService.class.getName());
    // In production, this should be stored securely in an environment variable or keystore
    private static final String HMAC_SECRET = System.getenv("APP_SECRET_KEY") != null ? 
            System.getenv("APP_SECRET_KEY") : "default_secure_hmac_key_1234567890";

    /**
     * Generates a QR code image as a Base64-encoded PNG string.
     */
    public static String generateQRCodeImageBase64(String text, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Remove white border

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    /**
     * Generates a payload string for the QR code securely signed with HMAC.
     * Format: accountId|amount|timestamp|hmacSignature
     */
    public static String generateStaticQRPayload(int accountId, String baseUrl) throws Exception {
        // A static QR simply contains the link to transfer to this account
        return baseUrl + "/transfer.jsp?receiver_account=" + accountId;
    }

    public static String generateDynamicQRPayload(int accountId, BigDecimal amount, String baseUrl) throws Exception {
        long timestamp = System.currentTimeMillis();
        String data = accountId + "|" + amount.toPlainString() + "|" + timestamp;
        String signature = generateHMAC(data);
        
        // Includes amount and signature for verification
        return baseUrl + "/transfer.jsp?receiver_account=" + accountId + "&amount=" + amount.toPlainString() + "&ts=" + timestamp + "&sig=" + signature;
    }

    /**
     * Verifies the authenticity of a dynamic QR code payload.
     */
    public static boolean verifyDynamicPayload(int accountId, String amountStr, long timestamp, String signature) {
        try {
            // Check expiry (e.g., 15 minutes)
            if (System.currentTimeMillis() - timestamp > 15 * 60 * 1000) {
                return false;
            }
            
            String data = accountId + "|" + amountStr + "|" + timestamp;
            String expectedSignature = generateHMAC(data);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "QR verification failed", e);
            return false;
        }
    }

    private static String generateHMAC(String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
