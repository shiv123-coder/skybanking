package com.skybanking.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validation utility class for banking system inputs.
 * Provides validation methods for various data types and formats.
 */
public class ValidationUtil {
    
    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
        "^[0-9]{6,12}$"
    );
    
    // Validation limits
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000.00");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 50;
    
    /**
     * Validate email format.
     * 
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format.
     * 
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate username format.
     * 
     * @param username Username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Validate account number format.
     * 
     * @param accountNumber Account number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }
    
    /**
     * Validate account number as integer.
     * 
     * @param accountNumber Account number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAccountNumber(int accountNumber) {
        return accountNumber > 0 && String.valueOf(accountNumber).length() >= 6;
    }
    
    /**
     * Validate transaction amount.
     * 
     * @param amount Amount to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(MIN_TRANSACTION_AMOUNT) >= 0 && 
               amount.compareTo(MAX_TRANSACTION_AMOUNT) <= 0;
    }
    
    /**
     * Validate transaction amount from string.
     * 
     * @param amountStr Amount string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTransactionAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            return isValidTransactionAmount(amount);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate password strength.
     * 
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || 
            password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        // Check for at least one uppercase, one lowercase, one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Validate full name format.
     * 
     * @param fullname Full name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFullname(String fullname) {
        if (fullname == null || fullname.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = fullname.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 50 && 
               trimmed.matches("^[a-zA-Z\\s]+$");
    }
    
    /**
     * Sanitize input string to prevent XSS attacks.
     * 
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("/", "&#x2F;");
    }
    
    /**
     * Validate OTP format (6-digit number).
     * 
     * @param otp OTP to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidOTP(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = otp.trim();
        return trimmed.matches("^[0-9]{6}$");
    }
    
    /**
     * Get minimum transaction amount.
     * 
     * @return Minimum transaction amount
     */
    public static BigDecimal getMinTransactionAmount() {
        return MIN_TRANSACTION_AMOUNT;
    }
    
    /**
     * Get maximum transaction amount.
     * 
     * @return Maximum transaction amount
     */
    public static BigDecimal getMaxTransactionAmount() {
        return MAX_TRANSACTION_AMOUNT;
    }
    
    /**
     * Get minimum password length.
     * 
     * @return Minimum password length
     */
    public static int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
}
