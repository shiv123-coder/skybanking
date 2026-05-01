package com.skybanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account model class representing a user's bank account.
 * Contains account information, balance, and account type details.
 */
public class Account {
    private int accountNumber;
    private int userId;
    private BigDecimal balance;
    private String accountType;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionDate;

    // Default constructor
    public Account() {}

    // Constructor with basic fields
    public Account(int userId, BigDecimal balance, String accountType) {
        this.userId = userId;
        this.balance = balance;
        this.accountType = accountType;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public Account(int accountNumber, int userId, BigDecimal balance, String accountType, 
                   boolean isActive, LocalDateTime createdAt, LocalDateTime lastTransactionDate) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance;
        this.accountType = accountType;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastTransactionDate = lastTransactionDate;
    }

    // Getters and Setters
    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber=" + accountNumber +
                ", userId=" + userId +
                ", balance=" + balance +
                ", accountType='" + accountType + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", lastTransactionDate=" + lastTransactionDate +
                '}';
    }
}
