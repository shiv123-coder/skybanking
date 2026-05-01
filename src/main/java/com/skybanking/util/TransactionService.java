package com.skybanking.util;

import com.skybanking.DBConnection;
import com.skybanking.model.Transaction;
import com.skybanking.service.AccountService;
import com.skybanking.service.LedgerService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Legacy service for handling banking transactions.
 * Now delegates core operations to LedgerService for ACID compliance.
 */
public class TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());

    public static Transaction processDeposit(int userId, BigDecimal amount, String description) throws Exception {
        if (!ValidationUtil.isValidTransactionAmount(amount)) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
                String idempotencyKey = "TXN-" + UUID.randomUUID().toString();
                
                LedgerService.deposit(con, accountId, amount, description, idempotencyKey);
                con.commit();
                
                LoggerUtil.logTransaction(userId, accountId, "DEPOSIT", amount.toString(), "SUCCESS", description);
                return getLatestTransaction(con, accountId);
            } catch (Exception e) {
                con.rollback();
                LOGGER.log(Level.SEVERE, "Deposit failed for user: " + userId, e);
                throw e;
            }
        }
    }

    public static Transaction processWithdrawal(int userId, BigDecimal amount, String description) throws Exception {
        if (!ValidationUtil.isValidTransactionAmount(amount)) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
                String idempotencyKey = "TXN-" + UUID.randomUUID().toString();
                
                LedgerService.withdraw(con, accountId, amount, description, idempotencyKey);
                con.commit();
                
                LoggerUtil.logTransaction(userId, accountId, "WITHDRAWAL", amount.toString(), "SUCCESS", description);
                return getLatestTransaction(con, accountId);
            } catch (Exception e) {
                con.rollback();
                LOGGER.log(Level.SEVERE, "Withdrawal failed for user: " + userId, e);
                throw e;
            }
        }
    }

    public static Transaction processTransfer(int senderUserId, int receiverAccountId, BigDecimal amount, String description) throws Exception {
        if (!ValidationUtil.isValidTransactionAmount(amount)) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int senderAccountId = AccountService.getOrCreatePrimaryAccount(con, senderUserId);
                String idempotencyKey = "TXN-" + UUID.randomUUID().toString();
                
                LedgerService.transfer(con, senderAccountId, receiverAccountId, amount, description, idempotencyKey);
                con.commit();
                
                LoggerUtil.logTransaction(senderUserId, senderAccountId, "TRANSFER", amount.toString(), "SUCCESS", "Transfer to account: " + receiverAccountId);
                return getLatestTransaction(con, senderAccountId);
            } catch (Exception e) {
                con.rollback();
                LOGGER.log(Level.SEVERE, "Transfer failed for user: " + senderUserId, e);
                throw e;
            }
        }
    }

    public static BigDecimal getAccountBalance(int userId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
            String sql = "SELECT balance FROM accounts WHERE account_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getBigDecimal("balance");
                }
            }
            return BigDecimal.ZERO;
        }
    }

    public static List<Transaction> getTransactionHistory(int userId, int limit) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            int accountId = AccountService.getOrCreatePrimaryAccount(con, userId);
            
            String sql = "SELECT * FROM transactions WHERE account_id = ? OR receiver_account_id = ? ORDER BY txn_date DESC LIMIT ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, accountId);
                ps.setInt(2, accountId); // Include received transfers
                ps.setInt(3, limit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.setTxnId(rs.getInt("txn_id"));
                        t.setAccountId(rs.getInt("account_id"));
                        t.setType(rs.getString("txn_type"));
                        t.setAmount(rs.getBigDecimal("amount"));
                        t.setTaxType(rs.getString("tax_type"));
                        t.setTaxAmount(rs.getBigDecimal("tax_amount"));
                        t.setTotalAmount(rs.getBigDecimal("total_amount"));
                        t.setDate(rs.getTimestamp("txn_date").toLocalDateTime());
                        t.setDescription(rs.getString("description"));
                        t.setReceiverAccountId(rs.getObject("receiver_account_id", Integer.class));
                        t.setStatus(rs.getString("status"));
                        t.setReferenceNumber(rs.getString("reference_number"));
                        transactions.add(t);
                    }
                }
            }
        }
        return transactions;
    }
    
    private static Transaction getLatestTransaction(Connection con, int accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY txn_id DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTxnId(rs.getInt("txn_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setType(rs.getString("txn_type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setStatus(rs.getString("status"));
                    t.setReferenceNumber(rs.getString("reference_number"));
                    return t;
                }
            }
        }
        return null;
    }
}
