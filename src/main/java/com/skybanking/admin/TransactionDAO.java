package com.skybanking.admin;

import com.skybanking.DBConnection;
import com.skybanking.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Fetch all transactions with optional filters (without pagination).
     * Used for bulk export.
     */
    public List<Transaction> getAllTransactions(String search, String type, String status,
                                                String dateFrom, String dateTo) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, u.fullname, u.username " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "JOIN users u ON a.user_id = u.user_id " +
                "WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (t.reference_number LIKE ? OR u.fullname LIKE ? OR u.username LIKE ?)");
            String s = "%" + search + "%";
            parameters.add(s);
            parameters.add(s);
            parameters.add(s);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND t.txn_type = ?");
            parameters.add(type);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND t.status = ?");
            parameters.add(status);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) >= ?");
            parameters.add(dateFrom);
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND DATE(t.txn_date) <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY t.txn_date DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

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

        return transactions;
    }
}
