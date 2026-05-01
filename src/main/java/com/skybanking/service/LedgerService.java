package com.skybanking.service;

import com.skybanking.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core ledger service for financial transactions.
 * Ensures ACID compliance, row-level locking (FOR UPDATE), and idempotency.
 */
public class LedgerService {

    private static final Logger LOGGER = Logger.getLogger(LedgerService.class.getName());

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }

    /**
     * Processes a deposit transaction.
     */
    public static void deposit(Connection con, int accountId, BigDecimal amount, String description, String idempotencyKey) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        if (isDuplicateTransaction(con, idempotencyKey)) {
            LOGGER.info("Idempotent deposit request ignored for key: " + idempotencyKey);
            return;
        }

        // Lock row to prevent race conditions (not strictly needed for pure addition, but good practice)
        lockAccount(con, accountId);

        // Update balance
        updateBalance(con, accountId, amount, true);

        // Record transaction
        recordTransaction(con, accountId, TransactionType.DEPOSIT, amount, null, amount, description, null, idempotencyKey);
    }

    /**
     * Processes a withdrawal transaction.
     */
    public static void withdraw(Connection con, int accountId, BigDecimal amount, String description, String idempotencyKey) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (isDuplicateTransaction(con, idempotencyKey)) {
            LOGGER.info("Idempotent withdrawal request ignored for key: " + idempotencyKey);
            return;
        }

        // Lock row to prevent race conditions and read balance
        BigDecimal currentBalance = lockAccountAndGetBalance(con, accountId);

        if (currentBalance.compareTo(amount) < 0) {
            throw new SQLException("Insufficient balance. Available: " + currentBalance);
        }

        // Update balance
        updateBalance(con, accountId, amount, false);

        // Record transaction
        recordTransaction(con, accountId, TransactionType.WITHDRAWAL, amount, null, amount, description, null, idempotencyKey);
    }

    /**
     * Processes a transfer between two accounts.
     * Prevents deadlocks by consistently ordering account locks.
     */
    public static void transfer(Connection con, int senderAccountId, int receiverAccountId, BigDecimal amount, String description, String idempotencyKey) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (senderAccountId == receiverAccountId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (isDuplicateTransaction(con, idempotencyKey)) {
            LOGGER.info("Idempotent transfer request ignored for key: " + idempotencyKey);
            return;
        }

        if (!AccountService.isAccountActive(con, receiverAccountId)) {
            throw new SQLException("Receiver account is invalid or inactive");
        }

        // To prevent deadlocks, always lock the account with the smaller ID first
        BigDecimal senderBalance;
        if (senderAccountId < receiverAccountId) {
            senderBalance = lockAccountAndGetBalance(con, senderAccountId);
            lockAccount(con, receiverAccountId);
        } else {
            lockAccount(con, receiverAccountId);
            senderBalance = lockAccountAndGetBalance(con, senderAccountId);
        }

        if (senderBalance.compareTo(amount) < 0) {
            throw new SQLException("Insufficient balance. Available: " + senderBalance);
        }

        // Update balances
        updateBalance(con, senderAccountId, amount, false);
        updateBalance(con, receiverAccountId, amount, true);

        // Record transaction for sender
        recordTransaction(con, senderAccountId, TransactionType.TRANSFER, amount, null, amount, description, receiverAccountId, idempotencyKey);
    }

    // --- Private Helper Methods ---

    /**
     * Applies a SELECT ... FOR UPDATE lock on the account row.
     */
    private static void lockAccount(Connection con, int accountId) throws SQLException {
        lockAccountAndGetBalance(con, accountId);
    }

    /**
     * Applies a SELECT ... FOR UPDATE lock and retrieves current balance.
     */
    private static BigDecimal lockAccountAndGetBalance(Connection con, int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                throw new SQLException("Account not found: " + accountId);
            }
        }
    }

    /**
     * Updates the balance of an account.
     */
    private static void updateBalance(Connection con, int accountId, BigDecimal amount, boolean isCredit) throws SQLException {
        String operator = isCredit ? "+" : "-";
        String sql = "UPDATE accounts SET balance = balance " + operator + " ?, last_transaction_date = NOW() WHERE account_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to update balance for account: " + accountId);
            }
        }
    }

    /**
     * Creates the transaction record.
     */
    private static void recordTransaction(Connection con, int accountId, TransactionType type, BigDecimal amount, String taxType, BigDecimal totalAmount, String description, Integer receiverAccountId, String referenceNumber) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, txn_type, amount, tax_type, total_amount, description, receiver_account_id, status, reference_number, txn_date) VALUES (?, ?, ?, ?, ?, ?, ?, 'COMPLETED', ?, NOW())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type.name());
            ps.setBigDecimal(3, amount);
            ps.setString(4, taxType);
            ps.setBigDecimal(5, totalAmount);
            ps.setString(6, description);
            ps.setObject(7, receiverAccountId);
            ps.setString(8, referenceNumber != null ? referenceNumber : UUID.randomUUID().toString());
            ps.executeUpdate();
        }
    }

    /**
     * Checks if a transaction with the given idempotency key (reference_number) already exists.
     */
    private static boolean isDuplicateTransaction(Connection con, String referenceNumber) throws SQLException {
        if (referenceNumber == null || referenceNumber.isEmpty()) return false;
        
        String sql = "SELECT 1 FROM transactions WHERE reference_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, referenceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
