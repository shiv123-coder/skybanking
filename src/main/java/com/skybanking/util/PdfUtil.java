package com.skybanking.util;

import com.skybanking.model.Transaction;
import com.skybanking.model.User;
import com.skybanking.model.Account;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF generation utility for banking system.
 * Generates PDF reports for transactions, statements, invoices, and admin
 * reports.
 */
public class PdfUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ---------------- Account Statement ---------------- //
    public static byte[] generateAccountStatement(User user, Account account, List<Transaction> transactions,
            LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - Account Statement",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph("Statement Period: " + startDate.format(DATE_ONLY_FORMATTER) + " to "
                    + endDate.format(DATE_ONLY_FORMATTER)));
            document.add(new Paragraph("Account Holder: " + user.getFullname()));
            document.add(new Paragraph("Account Number: " + account.getAccountNumber()));
            document.add(new Paragraph("Account Type: " + account.getAccountType()));
            document.add(new Paragraph("Current Balance: ₹" + account.getBalance()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Date");
            table.addCell("Type");
            table.addCell("Amount");
            table.addCell("Tax");
            table.addCell("Total");
            table.addCell("Description");

            BigDecimal totalDebits = BigDecimal.ZERO;
            BigDecimal totalCredits = BigDecimal.ZERO;

            for (Transaction txn : transactions) {
                table.addCell(txn.getDate().format(DATE_ONLY_FORMATTER));
                table.addCell(txn.getType());
                table.addCell("₹" + txn.getAmount());
                table.addCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00"));
                table.addCell("₹" + txn.getTotalAmount());
                table.addCell(txn.getDescription() != null ? txn.getDescription() : "");

                if ("WITHDRAWAL".equals(txn.getType()) || "TRANSFER".equals(txn.getType())) {
                    totalDebits = totalDebits.add(txn.getTotalAmount());
                } else {
                    totalCredits = totalCredits.add(txn.getTotalAmount());
                }
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Total Credits: ₹" + totalCredits));
            document.add(new Paragraph("Total Debits: ₹" + totalDebits));
            document.add(new Paragraph("Net Balance: ₹" + totalCredits.subtract(totalDebits)));

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate account statement PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Transaction Invoice ---------------- //
    public static byte[] generateTransactionInvoice(User user, Account account, Transaction transaction)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - Transaction Invoice",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Invoice Number: " + transaction.getReferenceNumber()));
            document.add(new Paragraph("Transaction Date: " + transaction.getDate().format(DATE_FORMATTER)));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Customer Details", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Name: " + user.getFullname()));
            document.add(new Paragraph("Account Number: " + account.getAccountNumber()));
            document.add(new Paragraph("Email: " + user.getEmail()));
            document.add(new Paragraph("Phone: " + user.getPhone()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Transaction Details", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Type: " + transaction.getType()));
            document.add(new Paragraph("Description: " + transaction.getDescription()));
            document.add(new Paragraph("Status: " + transaction.getStatus()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Amount Breakdown", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Base Amount: ₹" + transaction.getAmount()));
            if (transaction.getTaxAmount() != null && transaction.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Tax Type: " + transaction.getTaxType()));
                document.add(new Paragraph("Tax Amount: ₹" + transaction.getTaxAmount()));
            }
            document.add(new Paragraph("Total Amount: ₹" + transaction.getTotalAmount()));

            if (transaction.getReceiverAccountId() != null) {
                document.add(new Paragraph("To Account: " + transaction.getReceiverAccountId()));
            }

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate transaction invoice PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Admin Report ---------------- //
    public static byte[] generateAdminReport(String reportTitle, String reportData, String generatedBy)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(
                    new Paragraph("SkyBanking - Admin Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Report Title: " + reportTitle));
            document.add(new Paragraph("Generated By: " + generatedBy));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Report Content:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(reportData));

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate admin report PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- User List Report ---------------- //
    public static byte[] generateUserListReport(List<User> users, String generatedBy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - User List Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated By: " + generatedBy));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph("Total Users: " + users.size()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Full Name");
            table.addCell("Username");
            table.addCell("Email");
            table.addCell("Phone");
            table.addCell("Status");
            table.addCell("Created");

            for (User user : users) {
                String status = user.isActive() ? "Active" : "Inactive";
                String createdDate = user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_ONLY_FORMATTER)
                        : "N/A";

                table.addCell(String.valueOf(user.getId()));
                table.addCell(truncateString(user.getFullname(), 20));
                table.addCell(user.getUsername());
                table.addCell(truncateString(user.getEmail(), 25));
                table.addCell(user.getPhone());
                table.addCell(status);
                table.addCell(createdDate);
            }
            document.add(table);

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate user list report PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Transaction Summary ---------------- //
    public static byte[] generateTransactionSummary(List<Transaction> transactions, String summaryTitle,
            String generatedBy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - Transaction Summary",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Summary Title: " + summaryTitle));
            document.add(new Paragraph("Generated By: " + generatedBy));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph("Total Transactions: " + transactions.size()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Date");
            table.addCell("Type");
            table.addCell("Amount");
            table.addCell("Tax");
            table.addCell("Total");
            table.addCell("Description");

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

            for (Transaction txn : transactions) {
                table.addCell(txn.getDate().format(DATE_ONLY_FORMATTER));
                table.addCell(txn.getType());
                table.addCell("₹" + txn.getAmount());
                table.addCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00"));
                table.addCell("₹" + txn.getTotalAmount());
                table.addCell(txn.getDescription() != null ? txn.getDescription() : "");

                totalAmount = totalAmount.add(txn.getAmount());
                if (txn.getTaxAmount() != null) {
                    totalTax = totalTax.add(txn.getTaxAmount());
                }
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Totals", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Total Amount: ₹" + totalAmount));
            document.add(new Paragraph("Total Tax: ₹" + totalTax));
            document.add(new Paragraph("Grand Total: ₹" + totalAmount.add(totalTax)));

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate transaction summary PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Bulk Transaction Export ---------------- //
    public static byte[] generateBulkTransactionExport(List<Transaction> transactions,
            String generatedBy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            document.add(new Paragraph("SkyBanking - Bulk Transaction Export",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated By: " + generatedBy));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph("Total Transactions: " + transactions.size()));
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(8); // More detailed than summary
            table.setWidthPercentage(100);
            table.addCell("Date");
            table.addCell("Reference No.");
            table.addCell("Type");
            table.addCell("Status");
            table.addCell("Amount");
            table.addCell("Tax");
            table.addCell("Total");
            table.addCell("Description");

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

            for (Transaction txn : transactions) {
                table.addCell(txn.getDate().format(DATE_ONLY_FORMATTER));
                table.addCell(txn.getReferenceNumber() != null ? txn.getReferenceNumber() : "N/A");
                table.addCell(txn.getType());
                table.addCell(txn.getStatus());
                table.addCell("₹" + (txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO));
                table.addCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : BigDecimal.ZERO));
                table.addCell("₹" + (txn.getTotalAmount() != null ? txn.getTotalAmount() : BigDecimal.ZERO));
                table.addCell(txn.getDescription() != null ? truncateString(txn.getDescription(), 40) : "");

                if (txn.getAmount() != null) {
                    totalAmount = totalAmount.add(txn.getAmount());
                }
                if (txn.getTaxAmount() != null) {
                    totalTax = totalTax.add(txn.getTaxAmount());
                }
            }

            document.add(table);

            // Totals Section
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Totals", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("Total Amount: ₹" + totalAmount));
            document.add(new Paragraph("Total Tax: ₹" + totalTax));
            document.add(new Paragraph("Grand Total: ₹" + totalAmount.add(totalTax)));

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate bulk transaction export PDF", e);
        }

        return baos.toByteArray();
    }

    // ---------------- Dashboard Report ---------------- //
    public static byte[] generateDashboardReport(Map<String, Object> stats,
            Map<String, Object> recentActivities,
            Map<String, Object> transactionTrends) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - Admin Dashboard Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph(" "));

            // Stats
            document.add(new Paragraph("Dashboard Statistics", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            PdfPTable statsTable = new PdfPTable(2);
            statsTable.setWidthPercentage(100);
            statsTable.addCell("Metric");
            statsTable.addCell("Value");
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                statsTable.addCell(entry.getKey());
                statsTable.addCell(String.valueOf(entry.getValue()));
            }
            document.add(statsTable);
            document.add(new Paragraph(" "));

            // Activities
            document.add(new Paragraph("Recent Activities", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            if (recentActivities != null) {
                for (Map.Entry<String, Object> entry : recentActivities.entrySet()) {
                    document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
                }
            }
            document.add(new Paragraph(" "));

            // Trends
            document.add(new Paragraph("Transaction Trends (Last 7 Days)",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            if (transactionTrends != null) {
                for (Map.Entry<String, Object> entry : transactionTrends.entrySet()) {
                    document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
                }
            }

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate dashboard report PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Helper ---------------- //
    private static String truncateString(String str, int maxLength) {
        if (str == null)
            return "";
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    // ---------------- Fixed: Export All Users ---------------- //
    public static byte[] generateUserList(List<User> users) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - User List Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER)));
            document.add(new Paragraph("Total Users: " + users.size()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Full Name");
            table.addCell("Username");
            table.addCell("Email");
            table.addCell("Phone");
            table.addCell("Status");
            table.addCell("Created");

            for (User user : users) {
                table.addCell(String.valueOf(user.getId()));
                table.addCell(truncateString(user.getFullname(), 20));
                table.addCell(user.getUsername());
                table.addCell(truncateString(user.getEmail(), 25));
                table.addCell(user.getPhone());
                table.addCell(user.isActive() ? "Active" : "Inactive");
                table.addCell(user.getCreatedAt() != null
                        ? user.getCreatedAt().format(DATE_ONLY_FORMATTER)
                        : "N/A");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate user list PDF", e);
        }

        return baos.toByteArray();
    }

}
