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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.awt.Color;

/**
 * PDF generation utility for banking system.
 * Generates modernized, highly structured PDF reports.
 */
public class PdfUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Corporate Color Theme (Navy & Slate Blue)
    private static final Color COLOR_PRIMARY = new Color(30, 58, 138); // #1E3A8A Navy
    private static final Color COLOR_SECONDARY = new Color(15, 23, 42); // #0F172A Slate 900
    private static final Color COLOR_LIGHT_BORDER = new Color(226, 232, 240); // Slate 200
    private static final Color COLOR_ZEBRA = new Color(248, 250, 252); // Slate 50
    private static final Color COLOR_ROW_BORDER = new Color(241, 245, 249); // Slate 100
    private static final Color COLOR_TEXT = new Color(51, 65, 85); // Slate 700

    private static PdfPCell createHeaderCell(String text) {
        Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE));
        PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderColor(COLOR_LIGHT_BORDER);
        return cell;
    }

    private static PdfPCell createDataCell(String text, boolean alignRight, Color bgColor) {
        Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT));
        PdfPCell cell = new PdfPCell(p);
        cell.setHorizontalAlignment(alignRight ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        cell.setBorderColor(COLOR_ROW_BORDER);
        return cell;
    }

    // ---------------- Account Statement ---------------- //
    public static byte[] generateAccountStatement(User user, Account account, List<Transaction> transactions,
            LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Section
            Paragraph title = new Paragraph("SkyBanking - Account Statement",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY));
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            Paragraph subtitle = new Paragraph("Statement Period: " + startDate.format(DATE_ONLY_FORMATTER) + " to "
                    + endDate.format(DATE_ONLY_FORMATTER) + " | Generated On: " + LocalDateTime.now().format(DATE_FORMATTER),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT));
            document.add(subtitle);
            document.add(new Paragraph(" "));

            // Customer details in 2 column table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(15);
            float[] widths = {50f, 50f};
            detailsTable.setWidths(widths);

            // Left side
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("ACCOUNT HOLDER DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
            leftCell.addElement(new Paragraph("Name: " + user.getFullname(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            leftCell.addElement(new Paragraph("Email: " + user.getEmail(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            leftCell.addElement(new Paragraph("Phone: " + user.getPhone(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            detailsTable.addCell(leftCell);

            // Right side
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.addElement(new Paragraph("ACCOUNT SUMMARY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
            rightCell.addElement(new Paragraph("Account Number: " + account.getAccountNumber(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            rightCell.addElement(new Paragraph("Account Type: " + account.getAccountType(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            rightCell.addElement(new Paragraph("Current Balance: ₹" + account.getBalance(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            detailsTable.addCell(rightCell);

            detailsTable.setSpacingAfter(15);
            document.add(detailsTable);
            document.add(new Paragraph(" "));

            // Transactions Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            float[] colWidths = {15f, 15f, 15f, 15f, 15f, 25f};
            table.setWidths(colWidths);

            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Type"));
            table.addCell(createHeaderCell("Amount"));
            table.addCell(createHeaderCell("Tax"));
            table.addCell(createHeaderCell("Total"));
            table.addCell(createHeaderCell("Description"));

            BigDecimal totalDebits = BigDecimal.ZERO;
            BigDecimal totalCredits = BigDecimal.ZERO;

            int index = 0;
            for (Transaction txn : transactions) {
                Color rowBg = (index % 2 == 0) ? null : COLOR_ZEBRA;
                table.addCell(createDataCell(txn.getDate().format(DATE_ONLY_FORMATTER), false, rowBg));
                table.addCell(createDataCell(txn.getType(), false, rowBg));
                table.addCell(createDataCell("₹" + txn.getAmount(), true, rowBg));
                table.addCell(createDataCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00"), true, rowBg));
                table.addCell(createDataCell("₹" + txn.getTotalAmount(), true, rowBg));
                table.addCell(createDataCell(txn.getDescription() != null ? txn.getDescription() : "", false, rowBg));

                if ("WITHDRAWAL".equals(txn.getType()) || "TRANSFER".equals(txn.getType())) {
                    totalDebits = totalDebits.add(txn.getTotalAmount());
                } else {
                    totalCredits = totalCredits.add(txn.getTotalAmount());
                }
                index++;
            }
            document.add(table);

            document.add(new Paragraph(" "));
            
            // Statement Summary Section
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.addCell(createHeaderCell("Statement Summary"));
            summaryTable.addCell(createHeaderCell("Amount"));

            summaryTable.addCell(createDataCell("Total Credits", false, null));
            summaryTable.addCell(createDataCell("₹" + totalCredits, true, null));
            summaryTable.addCell(createDataCell("Total Debits", false, null));
            summaryTable.addCell(createDataCell("₹" + totalDebits, true, null));
            summaryTable.addCell(createDataCell("Net Activity", false, COLOR_ZEBRA));
            summaryTable.addCell(createDataCell("₹" + totalCredits.subtract(totalDebits), true, COLOR_ZEBRA));
            document.add(summaryTable);

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

            // Decorative title
            Paragraph title = new Paragraph("SkyBanking - Transaction Invoice",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY));
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph meta = new Paragraph("Invoice Number: " + transaction.getReferenceNumber() + 
                    " | Date: " + transaction.getDate().format(DATE_FORMATTER),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT));
            meta.setSpacingAfter(20);
            document.add(meta);
            document.add(new Paragraph(" "));

            // Two columns for Customer & Transaction details
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(20);
            float[] widths = {50f, 50f};
            detailsTable.setWidths(widths);

            // Left: Customer details
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("CUSTOMER DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
            leftCell.addElement(new Paragraph("Name: " + user.getFullname(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            leftCell.addElement(new Paragraph("Account Number: " + account.getAccountNumber(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            leftCell.addElement(new Paragraph("Email: " + user.getEmail(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            leftCell.addElement(new Paragraph("Phone: " + user.getPhone(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            detailsTable.addCell(leftCell);

            // Right: Transaction details
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.addElement(new Paragraph("TRANSACTION DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY)));
            rightCell.addElement(new Paragraph("Type: " + transaction.getType(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            rightCell.addElement(new Paragraph("Description: " + (transaction.getDescription() != null ? transaction.getDescription() : "N/A"), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            rightCell.addElement(new Paragraph("Status: " + transaction.getStatus(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            if (transaction.getReceiverAccountId() != null) {
                rightCell.addElement(new Paragraph("To Account ID: " + transaction.getReceiverAccountId(), FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            }
            detailsTable.addCell(rightCell);

            document.add(detailsTable);
            document.add(new Paragraph(" "));

            // Amount breakdown table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell(createHeaderCell("Description"));
            table.addCell(createHeaderCell("Amount"));

            table.addCell(createDataCell("Base Amount", false, null));
            table.addCell(createDataCell("₹" + transaction.getAmount(), true, null));
            
            if (transaction.getTaxAmount() != null && transaction.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                table.addCell(createDataCell("Tax (" + (transaction.getTaxType() != null ? transaction.getTaxType() : "GST") + ")", false, null));
                table.addCell(createDataCell("₹" + transaction.getTaxAmount(), true, null));
            }

            table.addCell(createDataCell("Total Amount Paid", false, COLOR_ZEBRA));
            table.addCell(createDataCell("₹" + transaction.getTotalAmount(), true, COLOR_ZEBRA));

            document.add(table);
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

            document.add(new Paragraph("SkyBanking - Admin Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY)));
            document.add(new Paragraph("Title: " + reportTitle + 
                    " | Generated By: " + generatedBy + 
                    " | On: " + LocalDateTime.now().format(DATE_FORMATTER),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph contentHeader = new Paragraph("Report Content", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY));
            contentHeader.setSpacingAfter(10);
            document.add(contentHeader);

            // Card-like table for content
            PdfPTable contentTable = new PdfPTable(1);
            contentTable.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell(new Paragraph(reportData, FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            cell.setPadding(15);
            cell.setBackgroundColor(COLOR_ZEBRA);
            cell.setBorderColor(COLOR_LIGHT_BORDER);
            contentTable.addCell(cell);
            document.add(contentTable);

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
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY)));
            document.add(new Paragraph("Generated By: " + generatedBy + 
                    " | Generated On: " + LocalDateTime.now().format(DATE_FORMATTER) +
                    " | Total Users: " + users.size(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            float[] colWidths = {8f, 22f, 15f, 23f, 12f, 10f, 10f};
            table.setWidths(colWidths);

            table.addCell(createHeaderCell("ID"));
            table.addCell(createHeaderCell("Full Name"));
            table.addCell(createHeaderCell("Username"));
            table.addCell(createHeaderCell("Email"));
            table.addCell(createHeaderCell("Phone"));
            table.addCell(createHeaderCell("Status"));
            table.addCell(createHeaderCell("Created"));

            int index = 0;
            for (User user : users) {
                Color rowBg = (index % 2 == 0) ? null : COLOR_ZEBRA;
                String status = user.isActive() ? "Active" : "Inactive";
                String createdDate = user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_ONLY_FORMATTER) : "N/A";

                table.addCell(createDataCell(String.valueOf(user.getId()), false, rowBg));
                table.addCell(createDataCell(truncateString(user.getFullname(), 20), false, rowBg));
                table.addCell(createDataCell(user.getUsername(), false, rowBg));
                table.addCell(createDataCell(truncateString(user.getEmail(), 25), false, rowBg));
                table.addCell(createDataCell(user.getPhone(), false, rowBg));
                table.addCell(createDataCell(status, false, rowBg));
                table.addCell(createDataCell(createdDate, false, rowBg));
                index++;
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
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY)));
            document.add(new Paragraph("Summary Title: " + summaryTitle + 
                    " | Generated By: " + generatedBy + 
                    " | On: " + LocalDateTime.now().format(DATE_FORMATTER) +
                    " | Total Transactions: " + transactions.size(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            float[] colWidths = {15f, 15f, 15f, 15f, 15f, 25f};
            table.setWidths(colWidths);

            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Type"));
            table.addCell(createHeaderCell("Amount"));
            table.addCell(createHeaderCell("Tax"));
            table.addCell(createHeaderCell("Total"));
            table.addCell(createHeaderCell("Description"));

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

            int index = 0;
            for (Transaction txn : transactions) {
                Color rowBg = (index % 2 == 0) ? null : COLOR_ZEBRA;
                table.addCell(createDataCell(txn.getDate().format(DATE_ONLY_FORMATTER), false, rowBg));
                table.addCell(createDataCell(txn.getType(), false, rowBg));
                table.addCell(createDataCell("₹" + txn.getAmount(), true, rowBg));
                table.addCell(createDataCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00"), true, rowBg));
                table.addCell(createDataCell("₹" + txn.getTotalAmount(), true, rowBg));
                table.addCell(createDataCell(txn.getDescription() != null ? txn.getDescription() : "", false, rowBg));

                totalAmount = totalAmount.add(txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO);
                if (txn.getTaxAmount() != null) {
                    totalTax = totalTax.add(txn.getTaxAmount());
                }
                index++;
            }
            document.add(table);

            document.add(new Paragraph(" "));

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.addCell(createHeaderCell("Totals"));
            summaryTable.addCell(createHeaderCell("Amount"));

            summaryTable.addCell(createDataCell("Total Amount", false, null));
            summaryTable.addCell(createDataCell("₹" + totalAmount, true, null));
            summaryTable.addCell(createDataCell("Total Tax", false, null));
            summaryTable.addCell(createDataCell("₹" + totalTax, true, null));
            summaryTable.addCell(createDataCell("Grand Total", false, COLOR_ZEBRA));
            summaryTable.addCell(createDataCell("₹" + totalAmount.add(totalTax), true, COLOR_ZEBRA));
            document.add(summaryTable);

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

            document.add(new Paragraph("SkyBanking - Bulk Transaction Export",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY)));
            document.add(new Paragraph("Generated By: " + generatedBy + 
                    " | On: " + LocalDateTime.now().format(DATE_FORMATTER) +
                    " | Total Transactions: " + transactions.size(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            float[] colWidths = {12f, 15f, 12f, 10f, 12f, 10f, 12f, 17f};
            table.setWidths(colWidths);

            table.addCell(createHeaderCell("Date"));
            table.addCell(createHeaderCell("Reference No."));
            table.addCell(createHeaderCell("Type"));
            table.addCell(createHeaderCell("Status"));
            table.addCell(createHeaderCell("Amount"));
            table.addCell(createHeaderCell("Tax"));
            table.addCell(createHeaderCell("Total"));
            table.addCell(createHeaderCell("Description"));

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;

            int index = 0;
            for (Transaction txn : transactions) {
                Color rowBg = (index % 2 == 0) ? null : COLOR_ZEBRA;
                table.addCell(createDataCell(txn.getDate().format(DATE_ONLY_FORMATTER), false, rowBg));
                table.addCell(createDataCell(txn.getReferenceNumber() != null ? txn.getReferenceNumber() : "N/A", false, rowBg));
                table.addCell(createDataCell(txn.getType(), false, rowBg));
                table.addCell(createDataCell(txn.getStatus(), false, rowBg));
                table.addCell(createDataCell("₹" + (txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO), true, rowBg));
                table.addCell(createDataCell("₹" + (txn.getTaxAmount() != null ? txn.getTaxAmount() : BigDecimal.ZERO), true, rowBg));
                table.addCell(createDataCell("₹" + (txn.getTotalAmount() != null ? txn.getTotalAmount() : BigDecimal.ZERO), true, rowBg));
                table.addCell(createDataCell(txn.getDescription() != null ? truncateString(txn.getDescription(), 30) : "", false, rowBg));

                if (txn.getAmount() != null) {
                    totalAmount = totalAmount.add(txn.getAmount());
                }
                if (txn.getTaxAmount() != null) {
                    totalTax = totalTax.add(txn.getTaxAmount());
                }
                index++;
            }
            document.add(table);

            document.add(new Paragraph(" "));

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(40);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.addCell(createHeaderCell("Totals"));
            summaryTable.addCell(createHeaderCell("Amount"));

            summaryTable.addCell(createDataCell("Total Base Amount", false, null));
            summaryTable.addCell(createDataCell("₹" + totalAmount, true, null));
            summaryTable.addCell(createDataCell("Total Tax Paid", false, null));
            summaryTable.addCell(createDataCell("₹" + totalTax, true, null));
            summaryTable.addCell(createDataCell("Grand Total", false, COLOR_ZEBRA));
            summaryTable.addCell(createDataCell("₹" + totalAmount.add(totalTax), true, COLOR_ZEBRA));
            document.add(summaryTable);

            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to generate bulk transaction export PDF", e);
        }
        return baos.toByteArray();
    }

    // ---------------- Dashboard Report ---------------- //
    @SuppressWarnings("unchecked")
    public static byte[] generateDashboardReport(Map<String, Object> stats,
            Map<String, Object> recentActivities,
            Map<String, Object> transactionTrends) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("SkyBanking - Executive Dashboard Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY)));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DATE_FORMATTER),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT)));
            document.add(new Paragraph(" "));

            // Stats Section
            document.add(new Paragraph("Key Metrics Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
            document.add(new Paragraph(" "));

            PdfPTable statsTable = new PdfPTable(2);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingAfter(15);
            statsTable.setWidths(new float[]{60f, 40f});

            statsTable.addCell(createHeaderCell("Metric / Indicator"));
            statsTable.addCell(createHeaderCell("Value"));

            Map<String, String> labelMapping = new HashMap<>();
            labelMapping.put("totalUsers", "Total Registered Users");
            labelMapping.put("activeUsers", "Active Users (Last 30 Days)");
            labelMapping.put("totalAccounts", "Total Bank Accounts");
            labelMapping.put("activeAccounts", "Active Accounts");
            labelMapping.put("totalTransactions", "Total Transactions Count");
            labelMapping.put("totalAmount", "Total Transaction Value");
            labelMapping.put("todayTransactions", "Today's Transaction Count");
            labelMapping.put("todayTransactionAmount", "Today's Transaction Value");
            labelMapping.put("pendingOtps", "Pending Security OTPs");

            int statIndex = 0;
            if (stats != null) {
                for (Map.Entry<String, Object> entry : stats.entrySet()) {
                    String label = labelMapping.getOrDefault(entry.getKey(), entry.getKey());
                    String valueStr = String.valueOf(entry.getValue());
                    if (entry.getKey().toLowerCase().contains("amount") || entry.getKey().equals("totalAmount") || entry.getKey().equals("todayTransactionAmount")) {
                        valueStr = "₹" + valueStr;
                    }
                    Color rowBg = (statIndex % 2 == 0) ? null : COLOR_ZEBRA;
                    statsTable.addCell(createDataCell(label, false, rowBg));
                    statsTable.addCell(createDataCell(valueStr, true, rowBg));
                    statIndex++;
                }
            }
            document.add(statsTable);
            document.add(new Paragraph(" "));

            // Transaction Types Distribution
            if (transactionTrends != null && transactionTrends.containsKey("transactionTypesDistribution")) {
                document.add(new Paragraph("Transaction Volume & Distribution by Type", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
                document.add(new Paragraph(" "));

                PdfPTable distTable = new PdfPTable(3);
                distTable.setWidthPercentage(100);
                distTable.setSpacingAfter(15);
                distTable.setWidths(new float[]{40f, 30f, 30f});

                distTable.addCell(createHeaderCell("Transaction Type"));
                distTable.addCell(createHeaderCell("Count"));
                distTable.addCell(createHeaderCell("Total Value"));

                List<Map<String, Object>> typeDistribution = (List<Map<String, Object>>) transactionTrends.get("transactionTypesDistribution");
                int distIndex = 0;
                for (Map<String, Object> item : typeDistribution) {
                    Color rowBg = (distIndex % 2 == 0) ? null : COLOR_ZEBRA;
                    distTable.addCell(createDataCell(String.valueOf(item.get("txn_type")), false, rowBg));
                    distTable.addCell(createDataCell(String.valueOf(item.get("count")), true, rowBg));
                    distTable.addCell(createDataCell("₹" + item.get("amount"), true, rowBg));
                    distIndex++;
                }
                document.add(distTable);
                document.add(new Paragraph(" "));
            }

            // Daily trends summary
            if (transactionTrends != null && transactionTrends.containsKey("dailyTransactionCounts")) {
                document.add(new Paragraph("Daily Transaction Activity (Last 7 Days)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
                document.add(new Paragraph(" "));

                PdfPTable trendsTable = new PdfPTable(2);
                trendsTable.setWidthPercentage(100);
                trendsTable.setSpacingAfter(15);
                trendsTable.setWidths(new float[]{50f, 50f});

                trendsTable.addCell(createHeaderCell("Date"));
                trendsTable.addCell(createHeaderCell("Transaction Count"));

                List<Map<String, Object>> dailyCounts = (List<Map<String, Object>>) transactionTrends.get("dailyTransactionCounts");
                int trendsIndex = 0;
                for (Map<String, Object> item : dailyCounts) {
                    Color rowBg = (trendsIndex % 2 == 0) ? null : COLOR_ZEBRA;
                    trendsTable.addCell(createDataCell(String.valueOf(item.get("date")), false, rowBg));
                    trendsTable.addCell(createDataCell(String.valueOf(item.get("count")), true, rowBg));
                    trendsIndex++;
                }
                document.add(trendsTable);
                document.add(new Paragraph(" "));
            }

            // Recent Activities Section
            if (recentActivities != null) {
                // Recent Registrations
                if (recentActivities.containsKey("recentRegistrations")) {
                    List<Map<String, Object>> registrations = (List<Map<String, Object>>) recentActivities.get("recentRegistrations");
                    if (registrations != null && !registrations.isEmpty()) {
                        document.add(new Paragraph("Recent User Registrations", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
                        document.add(new Paragraph(" "));

                        PdfPTable regTable = new PdfPTable(4);
                        regTable.setWidthPercentage(100);
                        regTable.setSpacingAfter(15);
                        regTable.setWidths(new float[]{15f, 30f, 30f, 25f});

                        regTable.addCell(createHeaderCell("User ID"));
                        regTable.addCell(createHeaderCell("Full Name"));
                        regTable.addCell(createHeaderCell("Username"));
                        regTable.addCell(createHeaderCell("Created At"));

                        int regIndex = 0;
                        for (Map<String, Object> item : registrations) {
                            Color rowBg = (regIndex % 2 == 0) ? null : COLOR_ZEBRA;
                            regTable.addCell(createDataCell(String.valueOf(item.get("user_id")), false, rowBg));
                            regTable.addCell(createDataCell(String.valueOf(item.get("fullname")), false, rowBg));
                            regTable.addCell(createDataCell(String.valueOf(item.get("username")), false, rowBg));
                            regTable.addCell(createDataCell(String.valueOf(item.get("createdAt")), false, rowBg));
                            regIndex++;
                        }
                        document.add(regTable);
                        document.add(new Paragraph(" "));
                    }
                }

                // Recent Transactions
                if (recentActivities.containsKey("recentTransactions")) {
                    List<Map<String, Object>> txns = (List<Map<String, Object>>) recentActivities.get("recentTransactions");
                    if (txns != null && !txns.isEmpty()) {
                        document.add(new Paragraph("Recent Transactions", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
                        document.add(new Paragraph(" "));

                        PdfPTable txnTable = new PdfPTable(4);
                        txnTable.setWidthPercentage(100);
                        txnTable.setSpacingAfter(15);
                        txnTable.setWidths(new float[]{30f, 20f, 25f, 25f});

                        txnTable.addCell(createHeaderCell("User"));
                        txnTable.addCell(createHeaderCell("Type"));
                        txnTable.addCell(createHeaderCell("Amount"));
                        txnTable.addCell(createHeaderCell("Date"));

                        int txnIndex = 0;
                        for (Map<String, Object> item : txns) {
                            Color rowBg = (txnIndex % 2 == 0) ? null : COLOR_ZEBRA;
                            txnTable.addCell(createDataCell(String.valueOf(item.get("userName")), false, rowBg));
                            txnTable.addCell(createDataCell(String.valueOf(item.get("type")), false, rowBg));
                            txnTable.addCell(createDataCell("₹" + item.get("amount"), true, rowBg));
                            txnTable.addCell(createDataCell(String.valueOf(item.get("date")), false, rowBg));
                            txnIndex++;
                        }
                        document.add(txnTable);
                    }
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
        return generateUserListReport(users, "Admin System");
    }
}
