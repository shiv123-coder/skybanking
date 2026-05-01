package com.skybanking.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging utility for the banking system.
 * Handles logging of errors, transactions, OTP attempts, and system events.
 */
public class LoggerUtil {
    
    private static final Logger logger = Logger.getLogger(LoggerUtil.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Log file paths
    private static final String ERROR_LOG_FILE = "logs/error.log";
    private static final String TRANSACTION_LOG_FILE = "logs/transaction.log";
    private static final String OTP_LOG_FILE = "logs/otp.log";
    private static final String SECURITY_LOG_FILE = "logs/security.log";
    private static final String SYSTEM_LOG_FILE = "logs/system.log";
    
    /**
     * Log error messages with timestamp and stack trace.
     * 
     * @param className The class where error occurred
     * @param methodName The method where error occurred
     * @param errorMessage The error message
     * @param exception The exception object (can be null)
     */
    public static void logError(String className, String methodName, String errorMessage, Exception exception) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] ERROR - %s.%s: %s", 
                                      timestamp, className, methodName, errorMessage);
        
        // Log to console
        logger.log(Level.SEVERE, logEntry, exception);
        
        // Log to file
        writeToFile(ERROR_LOG_FILE, logEntry, exception);
    }
    
    /**
     * Log transaction events.
     * 
     * @param userId User ID
     * @param accountId Account ID
     * @param transactionType Type of transaction
     * @param amount Transaction amount
     * @param status Transaction status
     * @param description Additional description
     */
    public static void logTransaction(int userId, int accountId, String transactionType, 
                                    String amount, String status, String description) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] TRANSACTION - User: %d, Account: %d, Type: %s, Amount: %s, Status: %s, Description: %s",
                                      timestamp, userId, accountId, transactionType, amount, status, description);
        
        logger.info(logEntry);
        writeToFile(TRANSACTION_LOG_FILE, logEntry, null);
    }
    
    /**
     * Log OTP-related events.
     * 
     * @param userId User ID
     * @param email User email
     * @param action OTP action (SENT, VERIFIED, FAILED, EXPIRED)
     * @param ipAddress User's IP address
     */
    public static void logOTP(int userId, String email, String action, String ipAddress) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] OTP - User: %d, Email: %s, Action: %s, IP: %s",
                                      timestamp, userId, email, action, ipAddress);
        
        logger.info(logEntry);
        writeToFile(OTP_LOG_FILE, logEntry, null);
    }
    
    /**
     * Log security-related events.
     * 
     * @param userId User ID (can be null for anonymous events)
     * @param action Security action (LOGIN, LOGOUT, PASSWORD_CHANGE, SUSPICIOUS_ACTIVITY)
     * @param ipAddress User's IP address
     * @param userAgent User's browser agent
     * @param details Additional security details
     */
    public static void logSecurity(Integer userId, String action, String ipAddress, 
                                 String userAgent, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String userInfo = (userId != null) ? "User: " + userId : "Anonymous";
        String logEntry = String.format("[%s] SECURITY - %s, Action: %s, IP: %s, UserAgent: %s, Details: %s",
                                      timestamp, userInfo, action, ipAddress, userAgent, details);
        
        logger.warning(logEntry);
        writeToFile(SECURITY_LOG_FILE, logEntry, null);
    }
    
    /**
     * Log system events.
     * 
     * @param component System component
     * @param action System action
     * @param message System message
     */
    public static void logSystem(String component, String action, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] SYSTEM - Component: %s, Action: %s, Message: %s",
                                      timestamp, component, action, message);
        
        logger.info(logEntry);
        writeToFile(SYSTEM_LOG_FILE, logEntry, null);
    }
    
    /**
     * Log admin activities.
     * 
     * @param adminId Admin ID
     * @param action Admin action
     * @param targetUser Target user ID (if applicable)
     * @param details Action details
     */
    public static void logAdmin(int adminId, String action, Integer targetUser, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String targetInfo = (targetUser != null) ? ", Target User: " + targetUser : "";
        String logEntry = String.format("[%s] ADMIN - Admin: %d, Action: %s%s, Details: %s",
                                      timestamp, adminId, action, targetInfo, details);
        
        logger.info(logEntry);
        writeToFile(SECURITY_LOG_FILE, logEntry, null);
    }
    
    /**
     * Write log entry to file.
     * 
     * @param filename Log file name
     * @param logEntry Log entry to write
     * @param exception Exception object (optional)
     */
    private static void writeToFile(String filename, String logEntry, Exception exception) {
        try {
            // Ensure logs directory exists
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
                writer.println(logEntry);
                if (exception != null) {
                    writer.println("Stack Trace:");
                    exception.printStackTrace(writer);
                }
                writer.println("---");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write to log file: " + filename, e);
        }
    }
    
    /**
     * Get current timestamp formatted for logging.
     * 
     * @return Formatted timestamp string
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
    
    /**
     * Log database operations.
     * 
     * @param operation Database operation (INSERT, UPDATE, DELETE, SELECT)
     * @param table Table name
     * @param userId User ID (if applicable)
     * @param details Operation details
     */
    public static void logDatabase(String operation, String table, Integer userId, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String userInfo = (userId != null) ? "User: " + userId : "System";
        String logEntry = String.format("[%s] DATABASE - %s, Operation: %s, Table: %s, Details: %s",
                                      timestamp, userInfo, operation, table, details);
        
        logger.fine(logEntry);
        writeToFile(SYSTEM_LOG_FILE, logEntry, null);
    }
}
