package com.skybanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction model class representing a banking transaction.
 * Contains transaction details including amounts, taxes, and metadata.
 */
public class Transaction {
    private int txnId;
    private int accountId;
    private String type; // deposit, withdraw, transfer
    private BigDecimal amount;
    private String taxType; // GST, TDS, etc.
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDateTime date;
    private String description;
    private Integer receiverAccountId; // For transfer transactions
    private String status; // PENDING, COMPLETED, FAILED
    private String referenceNumber;

    // Default constructor
    public Transaction() {}

    // Constructor with basic fields
    public Transaction(int accountId, String type, BigDecimal amount, String description) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = LocalDateTime.now();
        this.status = "COMPLETED";
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = amount;
    }

    // Full constructor
    public Transaction(int txnId, int accountId, String type, BigDecimal amount, 
                       String taxType, BigDecimal taxAmount, BigDecimal totalAmount,
                       LocalDateTime date, String description, Integer receiverAccountId,
                       String status, String referenceNumber) {
        this.txnId = txnId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.taxType = taxType;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.date = date;
        this.description = description;
        this.receiverAccountId = receiverAccountId;
        this.status = status;
        this.referenceNumber = referenceNumber;
    }

    // Getters and Setters
    public int getTxnId() {
        return txnId;
    }

    public void setTxnId(int txnId) {
        this.txnId = txnId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(Integer receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txnId=" + txnId +
                ", accountId=" + accountId +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", taxType='" + taxType + '\'' +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", receiverAccountId=" + receiverAccountId +
                ", status='" + status + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                '}';
    }
}
