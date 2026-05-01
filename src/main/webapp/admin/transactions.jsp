<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.skybanking.model.Transaction" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transaction Management - Admin Panel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link href="../css/adminStyle.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="dashboard">Sky Banking Admin</a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="dashboard">Dashboard</a>
            <a class="nav-link" href="logout">Logout</a>
        </div>
    </div>
</nav>

<div class="container-fluid">
    <div class="row">
        <nav class="col-md-3 col-lg-2 d-md-block bg-light sidebar">
            <div class="position-sticky pt-3">
                <ul class="nav flex-column">
                    <li class="nav-item"><a class="nav-link" href="dashboard"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
                    <li class="nav-item"><a class="nav-link" href="users"><i class="fas fa-users"></i> Users</a></li>
                    <li class="nav-item"><a class="nav-link active" href="transactions"><i class="fas fa-exchange-alt"></i> Transactions</a></li>
                    <li class="nav-item"><a class="nav-link" href="logs"><i class="fas fa-list-alt"></i> Logs</a></li>
                    <li class="nav-item"><a class="nav-link" href="settings"><i class="fas fa-cog"></i> Settings</a></li>
                </ul>
            </div>
        </nav>

        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">Transaction Management</h1>
                <div class="btn-toolbar mb-2 mb-md-0">
                    <!-- Fixed Export Button Using POST Form -->
                    <div class="btn-group me-2">
                        <form method="post" action="transactions" id="exportForm">
                            <input type="hidden" name="action" value="bulk_export">
                            <input type="hidden" name="search" value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                            <input type="hidden" name="type" value="<%= request.getAttribute("type") != null ? request.getAttribute("type") : "" %>">
                            <input type="hidden" name="status" value="<%= request.getAttribute("status") != null ? request.getAttribute("status") : "" %>">
                            <input type="hidden" name="dateFrom" value="<%= request.getAttribute("dateFrom") != null ? request.getAttribute("dateFrom") : "" %>">
                            <input type="hidden" name="dateTo" value="<%= request.getAttribute("dateTo") != null ? request.getAttribute("dateTo") : "" %>">
                            <button type="submit" class="btn btn-sm btn-outline-secondary">
                                <i class="fas fa-download"></i> Export
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Alerts -->
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <%= request.getAttribute("error") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <% } %>
            <% if (request.getAttribute("message") != null) { %>
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <%= request.getAttribute("message") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <% } %>

            <!-- Transaction Statistics -->
            <% 
                Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats"); 
                if (stats != null) { 
            %>
                <div class="row mb-4">
                    <div class="col-md-3">
                        <div class="card border-left-primary">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Transactions</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.getOrDefault("totalTransactions", 0) %></div>
                                    </div>
                                    <div class="col-auto"><i class="fas fa-exchange-alt fa-2x text-gray-300"></i></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card border-left-success">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Total Amount</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800">₹<%= stats.getOrDefault("totalAmount", 0.00) %></div>
                                    </div>
                                    <div class="col-auto"><i class="fas fa-rupee-sign fa-2x text-gray-300"></i></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card border-left-info">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col">
                                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Today's Transactions</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.getOrDefault("todayTransactions", 0) %></div>
                                    </div>
                                    <div class="col-auto"><i class="fas fa-calendar-day fa-2x text-gray-300"></i></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card border-left-warning">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col">
                                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Flagged</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.getOrDefault("flaggedTransactions", 0) %></div>
                                    </div>
                                    <div class="col-auto"><i class="fas fa-flag fa-2x text-gray-300"></i></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            <% } %>

            <!-- Search & Filter -->
            <div class="search-filter-container mb-3">
                <form method="post" action="transactions">
                    <input type="hidden" name="action" value="search">
                    <div class="row g-2">
                        <div class="col-md-3">
                            <input type="text" class="form-control" name="search" placeholder="Search by reference, user name"
                                   value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                        </div>
                        <div class="col-md-2">
                            <select name="type" class="form-select">
                                <option value="">All Types</option>
                                <option value="DEPOSIT" <%= "DEPOSIT".equals(request.getAttribute("type")) ? "selected" : "" %>>Deposit</option>
                                <option value="WITHDRAWAL" <%= "WITHDRAWAL".equals(request.getAttribute("type")) ? "selected" : "" %>>Withdrawal</option>
                                <option value="TRANSFER" <%= "TRANSFER".equals(request.getAttribute("type")) ? "selected" : "" %>>Transfer</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <select name="status" class="form-select">
                                <option value="">All Status</option>
                                <option value="COMPLETED" <%= "COMPLETED".equals(request.getAttribute("status")) ? "selected" : "" %>>Completed</option>
                                <option value="PENDING" <%= "PENDING".equals(request.getAttribute("status")) ? "selected" : "" %>>Pending</option>
                                <option value="FAILED" <%= "FAILED".equals(request.getAttribute("status")) ? "selected" : "" %>>Failed</option>
                                <option value="FLAGGED" <%= "FLAGGED".equals(request.getAttribute("status")) ? "selected" : "" %>>Flagged</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <input type="date" class="form-control" name="dateFrom" value="<%= request.getAttribute("dateFrom") != null ? request.getAttribute("dateFrom") : "" %>">
                        </div>
                        <div class="col-md-2">
                            <input type="date" class="form-control" name="dateTo" value="<%= request.getAttribute("dateTo") != null ? request.getAttribute("dateTo") : "" %>">
                        </div>
                        <div class="col-md-1">
                            <button type="submit" class="btn btn-primary w-100"><i class="fas fa-search"></i></button>
                        </div>
                    </div>
                </form>
            </div>

            <!-- Transactions Table -->
            <%-- KEEP EVERYTHING ELSE UNTOUCHED --%>
            <% List<Transaction> transactions = (List<Transaction>) request.getAttribute("transactions"); %>
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0">Transactions 
                        <span class="badge bg-primary"><%= request.getAttribute("totalTransactions") != null ? request.getAttribute("totalTransactions") : 0 %></span>
                    </h5>
                </div>
                <div class="card-body">
                    <% if (transactions != null && !transactions.isEmpty()) { %>
                        <div class="table-responsive">
                            <table class="table table-striped table-hover">
                                <thead class="table-dark">
                                    <tr>
                                        <th>ID</th>
                                        <th>Type</th>
                                        <th>Amount</th>
                                        <th>Tax</th>
                                        <th>Total</th>
                                        <th>Date</th>
                                        <th>Status</th>
                                        <th>Reference</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <% for (Transaction txn : transactions) { %>
                                    <tr>
                                        <td><%= txn.getTxnId() %></td>
                                        <td>
                                            <span class="badge bg-<%= "DEPOSIT".equals(txn.getType()) ? "success" : 
                                                                     "WITHDRAWAL".equals(txn.getType()) ? "danger" : "info" %>">
                                                <%= txn.getType() %>
                                            </span>
                                        </td>
                                        <td>₹<%= txn.getAmount() %></td>
                                        <td>₹<%= txn.getTaxAmount() != null ? txn.getTaxAmount() : "0.00" %></td>
                                        <td>₹<%= txn.getTotalAmount() %></td>
                                        <td><%= txn.getDate() %></td>
                                        <td>
                                            <span class="badge bg-<%= "COMPLETED".equals(txn.getStatus()) ? "success" :
                                                                     "FLAGGED".equals(txn.getStatus()) ? "warning" :
                                                                     "PENDING".equals(txn.getStatus()) ? "info" : "danger" %>">
                                                <%= txn.getStatus() %>
                                            </span>
                                        </td>
                                        <td><small><%= txn.getReferenceNumber() %></small></td>
                                        <td>
                                            <div class="btn-group btn-group-sm">
                                                <a href="transactions?action=view&txnId=<%= txn.getTxnId() %>" class="btn btn-outline-info" title="View Details"><i class="fas fa-eye"></i></a>
                                                <a href="transactions?action=export&txnId=<%= txn.getTxnId() %>" class="btn btn-outline-success" title="Export PDF"><i class="fas fa-download"></i></a>
                                                <% if ("FLAGGED".equals(txn.getStatus())) { %>
                                                    <a href="transactions?action=unflag&txnId=<%= txn.getTxnId() %>" class="btn btn-outline-success" title="Unflag" onclick="return confirm('Are you sure to unflag this transaction?')"><i class="fas fa-flag"></i></a>
                                                <% } else { %>
                                                    <a href="transactions?action=flag&txnId=<%= txn.getTxnId() %>" class="btn btn-outline-warning" title="Flag" onclick="return confirm('Are you sure to flag this transaction?')"><i class="fas fa-flag"></i></a>
                                                <% } %>
                                            </div>
                                        </td>
                                    </tr>
                                <% } %>
                                </tbody>
                            </table>
                        </div>

                        <!-- Pagination UNTOUCHED -->
                        <% 
                            int currentPage = request.getAttribute("currentPage") != null ? (Integer) request.getAttribute("currentPage") : 1;
                            int totalPages = request.getAttribute("totalPages") != null ? (Integer) request.getAttribute("totalPages") : 1;
                            String search = request.getAttribute("search") != null ? (String) request.getAttribute("search") : "";
                            String type = request.getAttribute("type") != null ? (String) request.getAttribute("type") : "";
                            String status = request.getAttribute("status") != null ? (String) request.getAttribute("status") : "";
                            String dateFrom = request.getAttribute("dateFrom") != null ? (String) request.getAttribute("dateFrom") : "";
                            String dateTo = request.getAttribute("dateTo") != null ? (String) request.getAttribute("dateTo") : "";
                        %>
                        <% if (totalPages > 1) { %>
                            <nav aria-label="Transactions pagination">
                                <ul class="pagination justify-content-center">
                                    <% if (currentPage > 1) { %>
                                        <li class="page-item"><a class="page-link" href="transactions?page=<%= currentPage -1 %>&search=<%= search %>&type=<%= type %>&status=<%= status %>&dateFrom=<%= dateFrom %>&dateTo=<%= dateTo %>">Previous</a></li>
                                    <% } %>
                                    <% for (int i=1; i<=totalPages; i++) { %>
                                        <li class="page-item <%= i==currentPage ? "active" : "" %>">
                                            <a class="page-link" href="transactions?page=<%= i %>&search=<%= search %>&type=<%= type %>&status=<%= status %>&dateFrom=<%= dateFrom %>&dateTo=<%= dateTo %>"><%= i %></a>
                                        </li>
                                    <% } %>
                                    <% if (currentPage < totalPages) { %>
                                        <li class="page-item"><a class="page-link" href="transactions?page=<%= currentPage +1 %>&search=<%= search %>&type=<%= type %>&status=<%= status %>&dateFrom=<%= dateFrom %>&dateTo=<%= dateTo %>">Next</a></li>
                                    <% } %>
                                </ul>
                            </nav>
                        <% } %>
                    <% } else { %>
                        <div class="text-center py-4"><p class="text-muted">No transactions found matching your criteria.</p></div>
                    <% } %>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Auto-dismiss alerts after 5 seconds
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(alert => {
            if(alert) new bootstrap.Alert(alert).close();
        });
    }, 5000);
</script>
</body>
</html>
