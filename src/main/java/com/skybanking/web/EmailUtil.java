package com.skybanking.web;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Email utility for sending OTPs and notifications.
 * All credentials are loaded from environment variables (System env first, then .env file).
 *
 * Required env vars:
 *   SMTP_EMAIL    — sender email address
 *   SMTP_PASSWORD — app password (NOT your main password)
 *   SMTP_HOST     — SMTP server hostname (default: smtp.gmail.com)
 *   SMTP_PORT     — SMTP server port (default: 587)
 */
public class EmailUtil {

    private static final Logger LOGGER = Logger.getLogger(EmailUtil.class.getName());

    private static final Dotenv DOTENV = loadDotenv();

    private static Dotenv loadDotenv() {
        try {
            return Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            LOGGER.warning("Could not load .env file, relying on system environment variables.");
            return null;
        }
    }

    // Lazy-loaded SMTP config to ensure env vars from Render are available at runtime
    private static String fromEmail;
    private static String password;
    private static String smtpHost;
    private static String smtpPort;
    private static boolean configLoaded = false;

    private EmailUtil() {
        // Utility class
    }

    /**
     * Load SMTP configuration lazily (at first use, not at class load time).
     * This ensures Render's environment variables are available when the servlet starts.
     */
    private static synchronized void loadConfig() {
        if (configLoaded) return;

        fromEmail = getEnvOrDefault("SMTP_EMAIL", "");
        password = getEnvOrDefault("SMTP_PASSWORD", "");
        smtpHost = getEnvOrDefault("SMTP_HOST", "smtp.gmail.com");
        smtpPort = getEnvOrDefault("SMTP_PORT", "587");
        configLoaded = true;

        // Log configuration status at startup (mask sensitive data)
        LOGGER.info("📧 EmailUtil config loaded:");
        LOGGER.info("   SMTP_EMAIL:    " + (fromEmail.isEmpty() ? "❌ NOT SET" : fromEmail));
        LOGGER.info("   SMTP_PASSWORD: " + (password.isEmpty() ? "❌ NOT SET" : "✅ SET (length=" + password.length() + ")"));
        LOGGER.info("   SMTP_HOST:     " + smtpHost);
        LOGGER.info("   SMTP_PORT:     " + smtpPort);
        LOGGER.info("   ENV source:    " + (DOTENV != null ? ".env file available" : "System env only (Render/Docker)"));
    }

    /**
     * Send an OTP email to the specified address.
     *
     * @param toEmail  recipient email address
     * @param username recipient's display name
     * @param otp      the one-time password to send
     * @return true if email was sent successfully, false otherwise
     */
    public static boolean sendOtp(String toEmail, String username, int otp) {
        // Ensure config is loaded
        loadConfig();

        if (fromEmail.isEmpty() || password.isEmpty()) {
            LOGGER.severe("❌ SMTP credentials not configured. Set SMTP_EMAIL and SMTP_PASSWORD in environment variables. Email will NOT be sent.");
            LOGGER.severe("   Current env check — SMTP_EMAIL from System.getenv: " + (System.getenv("SMTP_EMAIL") != null ? "present" : "null"));
            LOGGER.severe("   Current env check — SMTP_PASSWORD from System.getenv: " + (System.getenv("SMTP_PASSWORD") != null ? "present" : "null"));
            LOGGER.severe("   Current env check — DOTENV loaded: " + (DOTENV != null));
            return false;
        }
        
        if (smtpHost.isEmpty() || smtpPort.isEmpty()) {
            LOGGER.severe("❌ SMTP_HOST or SMTP_PORT is empty. Cannot send email.");
            return false;
        }

        String displayName = (username != null && !username.trim().isEmpty()) ? username.trim() : "Valued Customer";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        // Also allow TLSv1.3 for newer environments (Render uses modern JDK)
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        // ✅ Production Hardening: Set explicit timeouts to prevent thread blocks
        // Increased from 5s to 10s for cloud environments (Render) where network latency is higher
        props.put("mail.smtp.connectiontimeout", "10000"); // 10s connection timeout
        props.put("mail.smtp.timeout", "10000");           // 10s read timeout
        props.put("mail.smtp.writetimeout", "10000");      // 10s write timeout

        // Enable SMTP debug logging when issues are suspected
        boolean debug = "true".equalsIgnoreCase(getEnvOrDefault("SMTP_DEBUG", "false"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        session.setDebug(debug);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "SkyBanking"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your SkyBanking OTP");
            message.setText("Hello " + displayName + ",\n\nYour OTP is: " + otp +
                    "\nIt will expire in 5 minutes.\n\n" +
                    "If you did not request this OTP, please ignore this email.\n\n" +
                    "Thank you,\nSkyBanking Team");

            Transport.send(message);
            LOGGER.info("✅ OTP sent successfully to " + maskEmail(toEmail));
            return true;
        } catch (AuthenticationFailedException e) {
            LOGGER.log(Level.SEVERE, "❌ SMTP Authentication failed! Check SMTP_EMAIL and SMTP_PASSWORD. " +
                    "For Gmail, use an App Password (not your regular password). " +
                    "Email: " + fromEmail + ", Error: " + e.getMessage(), e);
            return false;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to send OTP email to " + maskEmail(toEmail) + 
                    ". Host: " + smtpHost + ":" + smtpPort + 
                    ". Error: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Unexpected error sending OTP email to " + maskEmail(toEmail), e);
            return false;
        }
    }

    /**
     * Force reload SMTP configuration from environment variables.
     * Useful for testing or when env vars change at runtime.
     */
    public static synchronized void reloadConfig() {
        configLoaded = false;
        loadConfig();
    }

    /**
     * Check if SMTP is properly configured.
     * @return true if SMTP credentials are available
     */
    public static boolean isConfigured() {
        loadConfig();
        return !fromEmail.isEmpty() && !password.isEmpty();
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
     * Checks System.getenv() first (for Render/Docker), then .env file (for local dev).
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if ((value == null || value.trim().isEmpty()) && DOTENV != null) {
            value = DOTENV.get(key);
        }
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}
