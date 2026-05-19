<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.skybanking.model.User, com.skybanking.model.Account" %>
<%
    User user = (User) request.getAttribute("user");
    Account account = (Account) request.getAttribute("account");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>User Details - Sky Banking</title>
    <!-- App Icon / Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.svg">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link href="${pageContext.request.contextPath}/css/adminStyle.css" rel="stylesheet">
    <!-- Global Theme Script -->
    <script src="${pageContext.request.contextPath}/js/theme.js" defer></script>
    <script>
        // Immediate Theme Detection (to prevent flashing)
        (function() {
            const savedTheme = localStorage.getItem('theme');
            const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
            const themeToApply = savedTheme || systemTheme;
            document.documentElement.setAttribute('data-theme', themeToApply);
        })();
    </script>
</head>
<body class="bg-light">
<div class="container mt-4">
    <h2 class="mb-4">User Details</h2>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger">
            <%= request.getAttribute("error") %>
        </div>
    <% } else if (user != null) { %>

    <!-- User Info Card -->
    <div class="card mb-4 shadow-sm">
        <div class="card-header bg-primary text-white">
            <strong>User Information</strong>
        </div>
        <div class="card-body">
            <p><strong>ID:</strong> <%= user.getId() %></p>
            <p><strong>Full Name:</strong> <%= user.getFullname() %></p>
            <p><strong>Username:</strong> <%= user.getUsername() %></p>
            <p><strong>Email:</strong> <%= user.getEmail() %></p>
            <p><strong>Phone:</strong> <%= user.getPhone() %></p>
            <p><strong>Status:</strong> <%= user.isActive() ? "Active" : "Inactive" %></p>
            <p><strong>Created At:</strong> <%= user.getCreatedAt() %></p>
            <p><strong>Last Login:</strong> <%= user.getLastLogin() != null ? user.getLastLogin() : "Never" %></p>
        </div>
    </div>

    <!-- Account Info Card -->
    <div class="card mb-4 shadow-sm">
        <div class="card-header bg-success text-white">
            <strong>Account Information</strong>
        </div>
        <div class="card-body">
            <% if (account != null) { %>
                <p><strong>Account Number:</strong> <%= account.getAccountNumber() %></p>
                <p><strong>Type:</strong> <%= account.getAccountType() %></p>
                <p><strong>Balance:</strong> ₹<%= account.getBalance() %></p>
                <p><strong>Status:</strong> <%= account.isActive() ? "Active" : "Inactive" %></p>
                <p><strong>Created At:</strong> <%= account.getCreatedAt() %></p>
                <p><strong>Last Transaction:</strong> <%= account.getLastTransactionDate() != null ? account.getLastTransactionDate() : "N/A" %></p>
            <% } else { %>
                <p class="text-muted">No active account linked to this user.</p>
            <% } %>
        </div>
    </div>

    <!-- Actions -->
    <div class="d-flex gap-2">
        <a href="users" class="btn btn-secondary">Back to User List</a>
        <a href="users?action=edit&userId=<%= user.getId() %>" class="btn btn-warning">Edit User</a>
        <a href="users?action=transactions&userId=<%= user.getId() %>" class="btn btn-info">View Transactions</a>
        <a href="users?action=export&userId=<%= user.getId() %>" class="btn btn-success">Export Statement</a>
    </div>

    <% } %>
</div>
</body>
</html>
