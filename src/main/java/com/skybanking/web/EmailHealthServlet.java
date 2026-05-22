package com.skybanking.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Health check endpoint to diagnose email configuration on Render.
 * Access: GET /email-health
 * Safe to expose — only shows config status, never reveals actual values.
 */
@WebServlet("/email-health")
public class EmailHealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();

        boolean configured = EmailUtil.isConfigured();
        String provider = EmailUtil.getProvider();

        // Check individual env vars (presence only, not values)
        String smtpEmailStatus = System.getenv("SMTP_EMAIL") != null ? "SET" : "NOT SET";
        String emailProviderStatus = System.getenv("EMAIL_PROVIDER") != null ? System.getenv("EMAIL_PROVIDER") : "not set (default: smtp)";

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"status\": \"").append(configured ? "OK" : "MISCONFIGURED").append("\",\n");
        json.append("  \"emailConfigured\": ").append(configured).append(",\n");
        json.append("  \"provider\": \"").append(provider).append("\",\n");
        json.append("  \"envVars\": {\n");
        json.append("    \"EMAIL_PROVIDER\": \"").append(emailProviderStatus).append("\",\n");
        json.append("    \"SMTP_EMAIL\": \"").append(smtpEmailStatus).append("\",\n");

        if ("brevo".equals(provider)) {
            String brevoStatus = System.getenv("BREVO_API_KEY") != null ? "SET" : "NOT SET";
            json.append("    \"BREVO_API_KEY\": \"").append(brevoStatus).append("\"\n");
        } else {
            String smtpPasswordStatus = System.getenv("SMTP_PASSWORD") != null ? "SET" : "NOT SET";
            String smtpHostStatus = System.getenv("SMTP_HOST") != null ? "SET" : "using default";
            String smtpPortStatus = System.getenv("SMTP_PORT") != null ? "SET" : "using default";
            json.append("    \"SMTP_PASSWORD\": \"").append(smtpPasswordStatus).append("\",\n");
            json.append("    \"SMTP_HOST\": \"").append(smtpHostStatus).append("\",\n");
            json.append("    \"SMTP_PORT\": \"").append(smtpPortStatus).append("\"\n");
        }

        json.append("  },\n");

        if (!configured) {
            if ("brevo".equals(provider)) {
                json.append("  \"fix\": \"Set BREVO_API_KEY and SMTP_EMAIL in Render Dashboard > Environment\"\n");
            } else {
                json.append("  \"fix\": \"On Render, SMTP ports are blocked. Set EMAIL_PROVIDER=brevo and BREVO_API_KEY in env vars. See: https://www.brevo.com\"\n");
            }
        } else {
            json.append("  \"note\": \"Email is configured and ready\"\n");
        }

        json.append("}\n");

        resp.setStatus(configured ? 200 : 503);
        out.print(json.toString());
        out.flush();
    }
}
