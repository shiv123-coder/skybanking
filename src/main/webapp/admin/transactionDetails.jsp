<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.skybanking.model.Transaction"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Transaction Details - Admin</title>
    <link href="../css/style.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <a href="transactions" class="btn btn-secondary mb-3">&larr; Back to Transactions</a>

    <%
        Transaction txn = (Transaction) request.getAttribute("transaction");
        Map<String, Object> userDetails = (Map<String, Object>) request.getAttribute("userDetails");
        if(txn != null && userDetails != null) {
    %>
    <div class="card">
        <div class="card-header">
            Transaction ID: <%= txn.getTxnId() %> | Reference: <%= txn.getReferenceNumber() %>
        </div>
        <div class="card-body">
            <h5>User Details</h5>
            <p><strong>Full Name:</strong> <%= userDetails.get("fullname") %></p>
            <p><strong>Username:</strong> <%= userDetails.get("username") %></p>
            <p><strong>Email:</strong> <%= userDetails.get("email") %></p>
            <p><strong>Phone:</strong> <%= userDetails.get("phone") %></p>
            <p><strong>Account Type:</strong> <%= userDetails.get("accountType") %></p>
            <p><strong>Balance:</strong> ₹<%= userDetails.get("balance") %></p>

            <h5 class="mt-4">Transaction Details</h5>
            <p><strong>Type:</strong> <%= txn.getType() %></p>
            <p><strong>Amount:</strong> ₹<%= txn.getAmount() %></p>
            <p><strong>Tax Amount:</strong> ₹<%= txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00" %></p>
            <p><strong>Total Amount:</strong> ₹<%= txn.getTotalAmount() %></p>
            <p><strong>Status:</strong> <%= txn.getStatus() %></p>
            <p><strong>Date:</strong> <%= txn.getDate() %></p>
            <p><strong>Description:</strong> <%= txn.getDescription() != null ? txn.getDescription() : "N/A" %></p>
            <% if(txn.getReceiverAccountId() != null) { %>
                <p><strong>Receiver Account ID:</strong> <%= txn.getReceiverAccountId() %></p>
            <% } %>
        </div>
    </div>
    <% } else { %>
        <div class="alert alert-warning">Transaction not found.</div>
    <% } %>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
