<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>System Logs - Admin Panel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
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
                        <li class="nav-item">
                            <a class="nav-link" href="dashboard">
                                <i class="fas fa-tachometer-alt"></i> Dashboard
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="users">
                                <i class="fas fa-users"></i> Users
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="transactions">
                                <i class="fas fa-exchange-alt"></i> Transactions
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="logs">
                                <i class="fas fa-list-alt"></i> Logs
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="settings">
                                <i class="fas fa-cog"></i> Settings
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>

            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">System Logs</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <div class="btn-group me-2">
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="refreshLogs()">
                                <i class="fas fa-sync-alt"></i> Refresh
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Log Type Tabs -->
                <ul class="nav nav-tabs" id="logTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link <%= "otp".equals(request.getAttribute("logType")) ? "active" : "" %>" 
                                id="otp-tab" data-bs-toggle="tab" data-bs-target="#otp" type="button" role="tab"
                                onclick="loadLogs('otp')">
                            <i class="fas fa-key"></i> OTP Logs
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link <%= "security".equals(request.getAttribute("logType")) ? "active" : "" %>" 
                                id="security-tab" data-bs-toggle="tab" data-bs-target="#security" type="button" role="tab"
                                onclick="loadLogs('security')">
                            <i class="fas fa-shield-alt"></i> Security Logs
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link <%= "transaction".equals(request.getAttribute("logType")) ? "active" : "" %>" 
                                id="transaction-tab" data-bs-toggle="tab" data-bs-target="#transaction" type="button" role="tab"
                                onclick="loadLogs('transaction')">
                            <i class="fas fa-exchange-alt"></i> Transaction Logs
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link <%= "system".equals(request.getAttribute("logType")) ? "active" : "" %>" 
                                id="system-tab" data-bs-toggle="tab" data-bs-target="#system" type="button" role="tab"
                                onclick="loadLogs('system')">
                            <i class="fas fa-cogs"></i> System Logs
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link <%= "admin".equals(request.getAttribute("logType")) ? "active" : "" %>" 
                                id="admin-tab" data-bs-toggle="tab" data-bs-target="#admin" type="button" role="tab"
                                onclick="loadLogs('admin')">
                            <i class="fas fa-user-shield"></i> Admin Logs
                        </button>
                    </li>
                </ul>

                <div class="tab-content" id="logTabsContent">
                    <!-- OTP Logs Tab -->
                    <div class="tab-pane fade <%= "otp".equals(request.getAttribute("logType")) ? "show active" : "" %>" 
                         id="otp" role="tabpanel">
                        <div class="card mt-3">
                            <div class="card-header">
                                <h5 class="mb-0">OTP Logs 
                                    <span class="badge bg-primary"><%= request.getAttribute("totalLogs") != null ? request.getAttribute("totalLogs") : 0 %></span>
                                </h5>
                            </div>
                            <div class="card-body">
                                <!-- Search and Filter for OTP Logs -->
                                <div class="search-filter-container mb-3">
                                    <form method="get" action="logs">
                                        <input type="hidden" name="type" value="otp">
                                        <div class="row">
                                            <div class="col-md-3">
                                                <input type="text" class="form-control" name="search" 
                                                       placeholder="Search by email or user ID" 
                                                       value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <select name="status" class="form-select">
                                                    <option value="">All Status</option>
                                                    <option value="PENDING" <%= "PENDING".equals(request.getAttribute("status")) ? "selected" : "" %>>Pending</option>
                                                    <option value="VERIFIED" <%= "VERIFIED".equals(request.getAttribute("status")) ? "selected" : "" %>>Verified</option>
                                                    <option value="EXPIRED" <%= "EXPIRED".equals(request.getAttribute("status")) ? "selected" : "" %>>Expired</option>
                                                    <option value="FAILED" <%= "FAILED".equals(request.getAttribute("status")) ? "selected" : "" %>>Failed</option>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <input type="date" class="form-control" name="dateFrom" 
                                                       value="<%= request.getAttribute("dateFrom") != null ? request.getAttribute("dateFrom") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <input type="date" class="form-control" name="dateTo" 
                                                       value="<%= request.getAttribute("dateTo") != null ? request.getAttribute("dateTo") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <button type="submit" class="btn btn-primary">
                                                    <i class="fas fa-search"></i> Search
                                                </button>
                                            </div>
                                        </div>
                                    </form>
                                </div>

                                <!-- OTP Logs Table -->
                                <% if ("otp".equals(request.getAttribute("logType"))) { %>
                                    <% List<Map<String, Object>> logs = (List<Map<String, Object>>) request.getAttribute("logs"); %>
                                    <% if (logs != null && !logs.isEmpty()) { %>
                                        <div class="table-responsive">
                                            <table class="table table-striped table-hover">
                                                <thead class="table-dark">
                                                    <tr>
                                                        <th>OTP ID</th>
                                                        <th>User ID</th>
                                                        <th>Email</th>
                                                        <th>Action</th>
                                                        <th>Status</th>
                                                        <th>Created</th>
                                                        <th>Expires</th>
                                                        <th>Verified</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% for (Map<String, Object> log : logs) { %>
                                                        <tr>
                                                            <td>${log.otpId}</td>
                                                            <td>${log.userId}</td>
                                                            <td>${log.email}</td>
                                                            <td>
                                                                <span class="badge bg-info">${log.action}</span>
                                                            </td>
                                                            <td>
                                                                <span class="badge bg-<%= "VERIFIED".equals(log.get("status")) ? "success" : 
                                                                                        "PENDING".equals(log.get("status")) ? "warning" : "danger" %>">
                                                                    ${log.status}
                                                                </span>
                                                            </td>
                                                            <td>${log.createdAt}</td>
                                                            <td>${log.expiresAt}</td>
                                                            <td>${log.verifiedAt != null ? log.verifiedAt : '-'}</td>
                                                        </tr>
                                                    <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    <% } else { %>
                                        <div class="text-center py-4">
                                            <p class="text-muted">No OTP logs found matching your criteria.</p>
                                        </div>
                                    <% } %>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <!-- Security Logs Tab -->
                    <div class="tab-pane fade <%= "security".equals(request.getAttribute("logType")) ? "show active" : "" %>" 
                         id="security" role="tabpanel">
                        <div class="card mt-3">
                            <div class="card-header">
                                <h5 class="mb-0">Security Logs 
                                    <span class="badge bg-primary"><%= request.getAttribute("totalLogs") != null ? request.getAttribute("totalLogs") : 0 %></span>
                                </h5>
                            </div>
                            <div class="card-body">
                                <!-- Search and Filter for Security Logs -->
                                <div class="search-filter-container mb-3">
                                    <form method="get" action="logs">
                                        <input type="hidden" name="type" value="security">
                                        <div class="row">
                                            <div class="col-md-3">
                                                <input type="text" class="form-control" name="search" 
                                                       placeholder="Search by IP, user agent, or details" 
                                                       value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <select name="action" class="form-select">
                                                    <option value="">All Actions</option>
                                                    <option value="LOGIN" <%= "LOGIN".equals(request.getAttribute("action")) ? "selected" : "" %>>Login</option>
                                                    <option value="LOGOUT" <%= "LOGOUT".equals(request.getAttribute("action")) ? "selected" : "" %>>Logout</option>
                                                    <option value="LOGIN_FAILED" <%= "LOGIN_FAILED".equals(request.getAttribute("action")) ? "selected" : "" %>>Login Failed</option>
                                                    <option value="PASSWORD_CHANGE" <%= "PASSWORD_CHANGE".equals(request.getAttribute("action")) ? "selected" : "" %>>Password Change</option>
                                                    <option value="SUSPICIOUS_ACTIVITY" <%= "SUSPICIOUS_ACTIVITY".equals(request.getAttribute("action")) ? "selected" : "" %>>Suspicious Activity</option>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <input type="date" class="form-control" name="dateFrom" 
                                                       value="<%= request.getAttribute("dateFrom") != null ? request.getAttribute("dateFrom") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <input type="date" class="form-control" name="dateTo" 
                                                       value="<%= request.getAttribute("dateTo") != null ? request.getAttribute("dateTo") : "" %>">
                                            </div>
                                            <div class="col-md-2">
                                                <button type="submit" class="btn btn-primary">
                                                    <i class="fas fa-search"></i> Search
                                                </button>
                                            </div>
                                        </div>
                                    </form>
                                </div>

                                <!-- Security Logs Table -->
                                <% if ("security".equals(request.getAttribute("logType"))) { %>
                                    <% List<Map<String, Object>> logs = (List<Map<String, Object>>) request.getAttribute("logs"); %>
                                    <% if (logs != null && !logs.isEmpty()) { %>
                                        <div class="table-responsive">
                                            <table class="table table-striped table-hover">
                                                <thead class="table-dark">
                                                    <tr>
                                                        <th>Log ID</th>
                                                        <th>User ID</th>
                                                        <th>Action</th>
                                                        <th>IP Address</th>
                                                        <th>User Agent</th>
                                                        <th>Details</th>
                                                        <th>Created</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% for (Map<String, Object> log : logs) { %>
                                                        <tr>
                                                            <td>${log.logId}</td>
                                                            <td>${log.userId != null ? log.userId : 'N/A'}</td>
                                                            <td>
                                                                <span class="badge bg-<%= "LOGIN".equals(log.get("action")) ? "success" : 
                                                                                        "LOGOUT".equals(log.get("action")) ? "info" : 
                                                                                        "LOGIN_FAILED".equals(log.get("action")) ? "danger" : "warning" %>">
                                                                    ${log.action}
                                                                </span>
                                                            </td>
                                                            <td><code>${log.ipAddress}</code></td>
                                                            <td><small>${log.userAgent != null ? log.userAgent.substring(0, Math.min(50, log.userAgent.length())) + "..." : 'N/A'}</small></td>
                                                            <td>${log.details != null ? log.details : '-'}</td>
                                                            <td>${log.createdAt}</td>
                                                        </tr>
                                                    <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    <% } else { %>
                                        <div class="text-center py-4">
                                            <p class="text-muted">No security logs found matching your criteria.</p>
                                        </div>
                                    <% } %>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <!-- Other log types would follow similar pattern -->
                </div>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    
    <script>
        function loadLogs(logType) {
            window.location.href = 'logs?type=' + logType;
        }
        
        function refreshLogs() {
            location.reload();
        }
    </script>
</body>
</html>
