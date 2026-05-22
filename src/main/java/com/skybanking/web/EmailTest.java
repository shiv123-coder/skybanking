package com.skybanking.web;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Quick standalone test to verify SMTP email sending.
 * Run: mvn compile exec:java -Dexec.mainClass="com.skybanking.web.EmailTest"
 * Or simply run this class directly with the classpath.
 */
public class EmailTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  SkyBanking Email Test");
        System.out.println("========================================");

        // Load .env
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
            System.out.println("✅ .env file loaded successfully");
        } catch (Exception e) {
            System.out.println("⚠️  Could not load .env file: " + e.getMessage());
        }

        // Check SMTP config
        String smtpEmail = getEnv("SMTP_EMAIL", dotenv);
        String smtpPassword = getEnv("SMTP_PASSWORD", dotenv);
        String smtpHost = getEnv("SMTP_HOST", dotenv);
        String smtpPort = getEnv("SMTP_PORT", dotenv);

        System.out.println("SMTP_EMAIL:    " + (smtpEmail != null ? smtpEmail : "NOT SET"));
        System.out.println("SMTP_PASSWORD: " + (smtpPassword != null ? maskPassword(smtpPassword) : "NOT SET"));
        System.out.println("SMTP_HOST:     " + (smtpHost != null ? smtpHost : "NOT SET (default: smtp.gmail.com)"));
        System.out.println("SMTP_PORT:     " + (smtpPort != null ? smtpPort : "NOT SET (default: 587)"));
        System.out.println("----------------------------------------");

        if (smtpEmail == null || smtpPassword == null) {
            System.out.println("❌ SMTP credentials are missing! Cannot send test email.");
            System.out.println("   Set SMTP_EMAIL and SMTP_PASSWORD in .env or environment variables.");
            System.exit(1);
        }

        // Try sending a test OTP
        System.out.println("📧 Sending test OTP email to: " + smtpEmail);
        int testOtp = 123456;

        try {
            boolean sent = EmailUtil.sendOtp(smtpEmail, "TestUser", testOtp);
            if (sent) {
                System.out.println("✅ Email sent SUCCESSFULLY!");
                System.out.println("   Check your inbox at: " + smtpEmail);
            } else {
                System.out.println("❌ Email sending FAILED (returned false).");
                System.out.println("   Check the logs above for detailed error info.");
            }
        } catch (Exception e) {
            System.out.println("❌ EmailUtil.sendOtp() threw an exception:");
            e.printStackTrace();
        }

        System.out.println("========================================");
        System.out.println("  Test Complete");
        System.out.println("========================================");
    }

    private static String getEnv(String key, Dotenv dotenv) {
        String value = System.getenv(key);
        if ((value == null || value.trim().isEmpty()) && dotenv != null) {
            value = dotenv.get(key);
        }
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    private static String maskPassword(String password) {
        if (password.length() <= 4) return "****";
        return password.substring(0, 4) + "****" + " (length=" + password.length() + ")";
    }
}
