package com.skybanking.service;

import com.skybanking.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for account management.
 */
public class AccountService {

    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());

    /**
     * Retrieves the primary active account ID for a user.
     * If no account exists, a default SAVINGS account is created.
     */
    public static int getOrCreatePrimaryAccount(Connection con, int userId) throws SQLException {
        String selectSql = "SELECT account_id FROM accounts WHERE user_id = ? AND is_active = true ORDER BY account_id ASC LIMIT 1";
        
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("account_id");
                }
            }
        }
        
        // No active account found, create one
        String insertSql = "INSERT INTO accounts (user_id, balance, account_type, is_active, created_at) VALUES (?, 0, 'SAVINGS', true, NOW())";
        try (PreparedStatement ps = con.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int accountId = rs.getInt(1);
                    LOGGER.info("Created default SAVINGS account " + accountId + " for user " + userId);
                    return accountId;
                }
                throw new SQLException("Failed to retrieve generated account ID");
            }
        }
    }

    /**
     * Checks if an account exists and is active.
     */
    public static boolean isAccountActive(Connection con, int accountId) throws SQLException {
        String sql = "SELECT 1 FROM accounts WHERE account_id = ? AND is_active = true";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
