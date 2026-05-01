package com.skybanking.web;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Email utility for sending OTPs and notifications.
 * All credentials are loaded from environment variables.
 *
 * Required env vars:
 *   SMTP_EMAIL    — sender email address
 *   SMTP_PASSWORD — app password (NOT your main password)
 *   SMTP_HOST     — SMTP server hostname (default: smtp.gmail.com)
 *   SMTP_PORT     — SMTP server port (default: 587)
 */
public class EmailUtil {

    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());

    private static final String FROM_EMAIL = getEnvOrDefault("SMTP_EMAIL", "");
    private static final String PASSWORD = getEnvOrDefault("SMTP_PASSWORD", "");
    private static final String SMTP_HOST = getEnvOrDefault("SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = getEnvOrDefault("SMTP_PORT", "587");

    private EmailUtil() {
        // Utility class
    }

    /**
     * Send an OTP email to the specified address.
     *
     * @param toEmail  recipient email address
     * @param username recipient's display name
     * @param otp      the one-time password to send
     */
    public static void sendOtp(String toEmail, String username, int otp) {
        if (FROM_EMAIL.isEmpty() || PASSWORD.isEmpty()) {
            LOGGER.severe("SMTP credentials not configured. Set SMTP_EMAIL and SMTP_PASSWORD environment variables.");
            return;
        }

        String displayName = (username != null && !username.trim().isEmpty()) ? username.trim() : "Valued Customer";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, "SkyBanking"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your SkyBanking OTP");
            message.setText("Hello " + displayName + ",\n\nYour OTP is: " + otp +
                    "\nIt will expire in 5 minutes.\n\n" +
                    "If you did not request this OTP, please ignore this email.\n\n" +
                    "Thank you,\nSkyBanking Team");

            Transport.send(message);
            LOGGER.info("OTP sent successfully to " + maskEmail(toEmail));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send OTP email to " + maskEmail(toEmail), e);
        }
    }

    /**
     * Mask email for logging (show first 2 chars + domain).
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * Get environment variable with fallback default.
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
