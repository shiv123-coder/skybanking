package com.skybanking.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Health check endpoint to diagnose email/SMTP configuration on Render.
 * Access: GET /email-health (only shows config status, never sends email)
 *
 * This is safe to expose — it only checks if env vars are present,
 * never reveals actual values.
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

        // Check individual env vars (presence only, not values)
        String smtpEmailStatus = System.getenv("SMTP_EMAIL") != null ? "SET (system env)" : "NOT in system env";
        String smtpPasswordStatus = System.getenv("SMTP_PASSWORD") != null ? "SET (system env)" : "NOT in system env";
        String smtpHostStatus = System.getenv("SMTP_HOST") != null ? "SET (system env)" : "NOT in system env (using default)";
        String smtpPortStatus = System.getenv("SMTP_PORT") != null ? "SET (system env)" : "NOT in system env (using default)";

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"status\": \"").append(configured ? "OK" : "MISCONFIGURED").append("\",\n");
        json.append("  \"emailConfigured\": ").append(configured).append(",\n");
        json.append("  \"envVars\": {\n");
        json.append("    \"SMTP_EMAIL\": \"").append(smtpEmailStatus).append("\",\n");
        json.append("    \"SMTP_PASSWORD\": \"").append(smtpPasswordStatus).append("\",\n");
        json.append("    \"SMTP_HOST\": \"").append(smtpHostStatus).append("\",\n");
        json.append("    \"SMTP_PORT\": \"").append(smtpPortStatus).append("\"\n");
        json.append("  },\n");
        json.append("  \"note\": \"If SMTP env vars show 'NOT in system env', set them in Render Dashboard > Environment\"\n");
        json.append("}\n");

        resp.setStatus(configured ? 200 : 503);
        out.print(json.toString());
        out.flush();
    }
}
