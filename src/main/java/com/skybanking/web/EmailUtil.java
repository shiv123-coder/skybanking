package com.skybanking.web;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Email utility for sending OTPs and notifications.
 * Supports two modes:
 *
 * 1. SMTP mode (local development) — uses Gmail SMTP via port 587
 *    Required env vars: SMTP_EMAIL, SMTP_PASSWORD
 *
 * 2. Brevo HTTP API mode (Render/production) — uses HTTPS (port 443)
 *    Required env vars: BREVO_API_KEY, SMTP_EMAIL (as sender)
 *    Render blocks outbound SMTP ports (25, 465, 587), so this mode
 *    sends email via Brevo's REST API over HTTPS which is not blocked.
 *
 * Set EMAIL_PROVIDER=brevo in env to use Brevo mode.
 * Default is "smtp" for backward compatibility.
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

    // Lazy-loaded config
    private static String fromEmail;
    private static String password;
    private static String smtpHost;
    private static String smtpPort;
    private static String emailProvider; // "smtp" or "brevo"
    private static String brevoApiKey;
    private static boolean configLoaded = false;

    private EmailUtil() {
        // Utility class
    }

    /**
     * Load email configuration lazily (at first use, not at class load time).
     */
    private static synchronized void loadConfig() {
        if (configLoaded) return;

        emailProvider = getEnvOrDefault("EMAIL_PROVIDER", "smtp").toLowerCase();
        fromEmail = getEnvOrDefault("SMTP_EMAIL", "");
        password = getEnvOrDefault("SMTP_PASSWORD", "");
        smtpHost = getEnvOrDefault("SMTP_HOST", "smtp.gmail.com");
        smtpPort = getEnvOrDefault("SMTP_PORT", "587");
        brevoApiKey = getEnvOrDefault("BREVO_API_KEY", "");
        configLoaded = true;

        // Log configuration status at startup
        LOGGER.info("📧 EmailUtil config loaded:");
        LOGGER.info("   EMAIL_PROVIDER: " + emailProvider);
        LOGGER.info("   SMTP_EMAIL:     " + (fromEmail.isEmpty() ? "❌ NOT SET" : fromEmail));

        if ("brevo".equals(emailProvider)) {
            LOGGER.info("   BREVO_API_KEY:  " + (brevoApiKey.isEmpty() ? "❌ NOT SET" : "✅ SET (length=" + brevoApiKey.length() + ")"));
            LOGGER.info("   Mode: Brevo HTTP API (HTTPS, works on Render)");
        } else {
            LOGGER.info("   SMTP_PASSWORD:  " + (password.isEmpty() ? "❌ NOT SET" : "✅ SET (length=" + password.length() + ")"));
            LOGGER.info("   SMTP_HOST:      " + smtpHost);
            LOGGER.info("   SMTP_PORT:      " + smtpPort);
            LOGGER.info("   Mode: SMTP (direct connection, for local dev)");
        }
        LOGGER.info("   ENV source:     " + (DOTENV != null ? ".env file available" : "System env only (Render/Docker)"));
    }

    /**
     * Send an OTP email to the specified address.
     * Automatically uses the configured provider (SMTP or Brevo).
     *
     * @param toEmail  recipient email address
     * @param username recipient's display name
     * @param otp      the one-time password to send
     * @return true if email was sent successfully, false otherwise
     */
    public static boolean sendOtp(String toEmail, String username, int otp) {
        loadConfig();

        String displayName = (username != null && !username.trim().isEmpty()) ? username.trim() : "Valued Customer";

        String subject = "Your SkyBanking OTP";
        String body = "Hello " + displayName + ",\n\nYour OTP is: " + otp +
                "\nIt will expire in 5 minutes.\n\n" +
                "If you did not request this OTP, please ignore this email.\n\n" +
                "Thank you,\nSkyBanking Team";

        if ("brevo".equals(emailProvider)) {
            return sendViaBrevo(toEmail, subject, body);
        } else {
            return sendViaSmtp(toEmail, subject, body);
        }
    }

    // ==================== BREVO HTTP API MODE ====================

    /**
     * Send email via Brevo (Sendinblue) REST API over HTTPS.
     * This works on Render because it uses port 443 (HTTPS), not SMTP ports.
     *
     * Brevo free tier: 300 emails/day — more than enough for OTPs.
     * API docs: https://developers.brevo.com/reference/sendtransacemail
     */
    private static boolean sendViaBrevo(String toEmail, String subject, String body) {
        if (brevoApiKey.isEmpty()) {
            LOGGER.severe("❌ BREVO_API_KEY not configured. Set it in environment variables.");
            LOGGER.severe("   Sign up at https://www.brevo.com (free: 300 emails/day)");
            LOGGER.severe("   Then go to: SMTP & API > API Keys > Generate a new API key");
            return false;
        }

        if (fromEmail.isEmpty()) {
            LOGGER.severe("❌ SMTP_EMAIL not configured (used as sender address for Brevo).");
            return false;
        }

        // Escape JSON special characters in body
        String escapedBody = body.replace("\\", "\\\\")
                                 .replace("\"", "\\\"")
                                 .replace("\n", "\\n")
                                 .replace("\r", "\\r")
                                 .replace("\t", "\\t");

        String escapedSubject = subject.replace("\\", "\\\\")
                                       .replace("\"", "\\\"");

        String jsonPayload = "{"
                + "\"sender\":{\"name\":\"SkyBanking\",\"email\":\"" + fromEmail + "\"},"
                + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                + "\"subject\":\"" + escapedSubject + "\","
                + "\"textContent\":\"" + escapedBody + "\""
                + "}";

        try {
            URL url = URI.create("https://api.brevo.com/v3/smtp/email").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("content-type", "application/json");
            conn.setRequestProperty("api-key", brevoApiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000); // 15s
            conn.setReadTimeout(15000);    // 15s

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 201 || responseCode == 200) {
                LOGGER.info("✅ OTP sent via Brevo to " + maskEmail(toEmail) + " (HTTP " + responseCode + ")");
                return true;
            } else {
                // Read error response
                String errorBody = "";
                try (java.io.InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                } catch (Exception ignored) {}

                LOGGER.severe("❌ Brevo API returned HTTP " + responseCode + ": " + errorBody);

                if (responseCode == 401) {
                    LOGGER.severe("   → Invalid BREVO_API_KEY. Regenerate at: SMTP & API > API Keys");
                } else if (responseCode == 400) {
                    LOGGER.severe("   → Bad request. Check sender email is verified in Brevo.");
                }
                return false;
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to call Brevo API: " + e.getMessage(), e);
            return false;
        }
    }

    // ==================== SMTP MODE (LOCAL DEV) ====================

    /**
     * Send email via SMTP (Gmail).
     * Works for local development but NOT on Render (ports 587/465 blocked).
     */
    private static boolean sendViaSmtp(String toEmail, String subject, String body) {
        if (fromEmail.isEmpty() || password.isEmpty()) {
            LOGGER.severe("❌ SMTP credentials not configured. Set SMTP_EMAIL and SMTP_PASSWORD.");
            return false;
        }

        if (smtpHost.isEmpty() || smtpPort.isEmpty()) {
            LOGGER.severe("❌ SMTP_HOST or SMTP_PORT is empty.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        // Timeouts
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

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
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            LOGGER.info("✅ OTP sent via SMTP to " + maskEmail(toEmail));
            return true;
        } catch (AuthenticationFailedException e) {
            LOGGER.log(Level.SEVERE, "❌ SMTP Auth failed! Use a Gmail App Password. Error: " + e.getMessage(), e);
            return false;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "❌ SMTP send failed to " + maskEmail(toEmail) +
                    " via " + smtpHost + ":" + smtpPort + ". Error: " + e.getMessage(), e);

            // Detect Render's port-blocking and suggest fix
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Connect timed out") || msg.contains("Connection refused"))) {
                LOGGER.severe("⚠️  This looks like SMTP port blocking (common on Render free tier).");
                LOGGER.severe("   FIX: Set EMAIL_PROVIDER=brevo and BREVO_API_KEY in your Render env vars.");
                LOGGER.severe("   Brevo sends via HTTPS (port 443) which is NOT blocked.");
                LOGGER.severe("   Sign up free at: https://www.brevo.com");
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Unexpected error sending email to " + maskEmail(toEmail), e);
            return false;
        }
    }

    // ==================== HELPERS ====================

    /**
     * Force reload config from environment variables.
     */
    public static synchronized void reloadConfig() {
        configLoaded = false;
        loadConfig();
    }

    /**
     * Check if email is properly configured.
     */
    public static boolean isConfigured() {
        loadConfig();
        if ("brevo".equals(emailProvider)) {
            return !fromEmail.isEmpty() && !brevoApiKey.isEmpty();
        }
        return !fromEmail.isEmpty() && !password.isEmpty();
    }

    /**
     * Get the current email provider mode.
     */
    public static String getProvider() {
        loadConfig();
        return emailProvider;
    }

    /**
     * Mask email for logging.
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
        if ((value == null || value.trim().isEmpty()) && DOTENV != null) {
            value = DOTENV.get(key);
        }
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}
