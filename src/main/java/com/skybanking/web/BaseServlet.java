package com.skybanking.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base servlet for error handling and logging.
 */
public abstract class BaseServlet extends HttpServlet {
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    protected void handleError(HttpServletRequest req, HttpServletResponse resp, String message, String page,
            Exception e) throws IOException {
        logger.log(Level.SEVERE, message, e);
        try {
            req.setAttribute("error", message);
            req.getRequestDispatcher(page).forward(req, resp);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error forwarding to error page", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }
}