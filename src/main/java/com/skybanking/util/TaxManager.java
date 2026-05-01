package com.skybanking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tax calculation utility for banking transactions.
 * Handles GST, TDS, and other tax calculations.
 */
public class TaxManager {
    
    // Tax rates (configurable)
    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
    private static final BigDecimal TDS_RATE = new BigDecimal("0.10"); // 10% TDS
    private static final BigDecimal SERVICE_TAX_RATE = new BigDecimal("0.12"); // 12% Service Tax
    
    /**
     * Calculate GST (Goods and Services Tax) for a given amount.
     * 
     * @param amount The base amount
     * @param rate The GST rate (optional, uses default if null)
     * @return GST amount
     */
    public static BigDecimal calculateGST(BigDecimal amount, BigDecimal rate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal gstRate = (rate != null) ? rate : GST_RATE;
        return amount.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate GST using default rate.
     * 
     * @param amount The base amount
     * @return GST amount
     */
    public static BigDecimal calculateGST(BigDecimal amount) {
        return calculateGST(amount, null);
    }
    
    /**
     * Calculate TDS (Tax Deducted at Source) for a given amount.
     * 
     * @param amount The base amount
     * @param rate The TDS rate (optional, uses default if null)
     * @return TDS amount
     */
    public static BigDecimal calculateTDS(BigDecimal amount, BigDecimal rate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tdsRate = (rate != null) ? rate : TDS_RATE;
        return amount.multiply(tdsRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate TDS using default rate.
     * 
     * @param amount The base amount
     * @return TDS amount
     */
    public static BigDecimal calculateTDS(BigDecimal amount) {
        return calculateTDS(amount, null);
    }
    
    /**
     * Calculate Service Tax for a given amount.
     * 
     * @param amount The base amount
     * @param rate The service tax rate (optional, uses default if null)
     * @return Service tax amount
     */
    public static BigDecimal calculateServiceTax(BigDecimal amount, BigDecimal rate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal serviceTaxRate = (rate != null) ? rate : SERVICE_TAX_RATE;
        return amount.multiply(serviceTaxRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Service Tax using default rate.
     * 
     * @param amount The base amount
     * @return Service tax amount
     */
    public static BigDecimal calculateServiceTax(BigDecimal amount) {
        return calculateServiceTax(amount, null);
    }
    
    /**
     * Calculate total amount including tax.
     * 
     * @param baseAmount The base amount
     * @param taxAmount The tax amount
     * @return Total amount (base + tax)
     */
    public static BigDecimal calculateTotalAmount(BigDecimal baseAmount, BigDecimal taxAmount) {
        if (baseAmount == null) baseAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        
        return baseAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate net amount after deducting tax.
     * 
     * @param grossAmount The gross amount
     * @param taxAmount The tax amount to deduct
     * @return Net amount (gross - tax)
     */
    public static BigDecimal calculateNetAmount(BigDecimal grossAmount, BigDecimal taxAmount) {
        if (grossAmount == null) grossAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        
        return grossAmount.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get the default GST rate.
     * 
     * @return GST rate as BigDecimal
     */
    public static BigDecimal getDefaultGSTRate() {
        return GST_RATE;
    }
    
    /**
     * Get the default TDS rate.
     * 
     * @return TDS rate as BigDecimal
     */
    public static BigDecimal getDefaultTDSRate() {
        return TDS_RATE;
    }
    
    /**
     * Get the default Service Tax rate.
     * 
     * @return Service Tax rate as BigDecimal
     */
    public static BigDecimal getDefaultServiceTaxRate() {
        return SERVICE_TAX_RATE;
    }
}
