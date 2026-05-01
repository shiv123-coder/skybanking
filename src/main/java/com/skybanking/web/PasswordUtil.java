package com.skybanking.web;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized password hashing utility using BCrypt.
 * Supports migration from legacy SHA-256 hashes.
 *
 * ALL password operations MUST go through this class.
 */
public class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final int BCRYPT_ROUNDS = 12;

    private PasswordUtil() {
        // Utility class — no instantiation
    }

    /**
     * Hash a password using BCrypt.
     *
     * @param password plaintext password
     * @return BCrypt hash string
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verify a password against a stored hash.
     * Supports dual-check: tries BCrypt first, then falls back to SHA-256
     * for legacy hash migration.
     *
     * @param password   plaintext password to verify
     * @param storedHash the hash stored in the database
     * @return true if the password matches
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        // Try BCrypt first (hashes start with "$2a$" or "$2b$")
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, storedHash);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "BCrypt verification failed", e);
                return false;
            }
        }

        // Fallback: Legacy SHA-256 check (64-char hex string)
        if (storedHash.length() == 64 && storedHash.matches("^[a-f0-9]+$")) {
            String sha256Hash = sha256(password);
            return sha256Hash != null && sha256Hash.equals(storedHash);
        }

        return false;
    }

    /**
     * Check if a stored hash needs to be upgraded to BCrypt.
     *
     * @param storedHash the hash from the database
     * @return true if it's a legacy SHA-256 hash that should be re-hashed
     */
    public static boolean needsUpgrade(String storedHash) {
        if (storedHash == null) return false;
        // SHA-256 hashes are exactly 64 hex characters
        return storedHash.length() == 64 && storedHash.matches("^[a-f0-9]+$");
    }

    /**
     * Legacy SHA-256 hash (for backward compatibility only).
     * DO NOT use for new passwords.
     */
    private static String sha256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SHA-256 hashing failed", e);
            return null;
        }
    }
}
