<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Sky Banking System</title>
    <!-- App Icon / Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.svg">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link href="../css/adminStyle.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">Sky Banking Admin</a>
            <div class="navbar-nav ms-auto align-items-center">
                <!-- Admin Notifications Toggle -->
                <a class="nav-link position-relative me-3" href="#" data-bs-toggle="offcanvas" data-bs-target="#adminNotifOffcanvas">
                    <i class="bi bi-bell-fill fs-5"></i>
                    <span id="admin-notif-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none" style="font-size: 0.6rem;">0</span>
                </a>
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
                            <a class="nav-link active" href="dashboard">
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
                            <a class="nav-link" href="logs">
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
                    <h1 class="h2">Dashboard</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <div class="btn-group me-2">
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="exportDashboard()">
                                <i class="fas fa-download"></i> Export
                            </button>
                        </div>
                    </div>
                </div>

                <% String error = (String) request.getAttribute("error"); %>
                <% if (error != null && !error.isEmpty()) { %>
                    <div class="alert alert-danger alert-dismissible fade show mt-2 shadow-sm" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i><strong>Error:</strong> <%= error %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <% } %>

                <% Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats"); %>
                <% if (stats != null) { %>
                <!-- Statistics Cards -->
                <div class="row mb-4">
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Users</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.get("totalUsers") %></div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-users fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Active Users</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.get("activeUsers") %></div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-user-check fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-info shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Total Transactions</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.get("totalTransactions") %></div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-exchange-alt fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-warning shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Today's Transactions</div>
                                        <div class="h5 mb-0 font-weight-bold text-gray-800"><%= stats.get("todayTransactions") %></div>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-calendar-day fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Charts Row -->
                <div class="row">
                    <div class="col-lg-8">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">Transaction Trends (Last 7 Days)</h6>
                            </div>
                            <div class="card-body">
                                <div class="chart-area">
                                    <canvas id="transactionChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">Transaction Types</h6>
                            </div>
                            <div class="card-body">
                                <div class="chart-pie pt-4 pb-2">
                                    <canvas id="transactionTypesChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Activities -->
                <div class="row">
                    <div class="col-lg-6">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">Recent User Registrations</h6>
                            </div>
                            <div class="card-body">
                                <% Map<String, Object> recentActivities = (Map<String, Object>) request.getAttribute("recentActivities"); %>
                                <% if (recentActivities != null) { %>
                                    <% List<Map<String, Object>> recentRegistrations = (List<Map<String, Object>>) recentActivities.get("recentRegistrations"); %>
                                    <% if (recentRegistrations != null && !recentRegistrations.isEmpty()) { %>
                                        <div class="table-responsive">
                                            <table class="table table-sm">
                                                <thead>
                                                    <tr>
                                                        <th>Name</th>
                                                        <th>Username</th>
                                                        <th>Date</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% for (Map<String, Object> user : recentRegistrations) { %>
                                                    <tr>
                                                        <td><%= user.get("fullname") %></td>
                                                        <td><%= user.get("username") %></td>
                                                        <td><%= user.get("createdAt") %></td>
                                                    </tr>
                                                    <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    <% } else { %>
                                        <p class="text-muted">No recent registrations</p>
                                    <% } %>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-6">
                        <div class="card shadow mb-4">
                            <div class="card-header py-3">
                                <h6 class="m-0 font-weight-bold text-primary">Recent Transactions</h6>
                            </div>
                            <div class="card-body">
                                <% if (recentActivities != null) { %>
                                    <% List<Map<String, Object>> recentTransactions = (List<Map<String, Object>>) recentActivities.get("recentTransactions"); %>
                                    <% if (recentTransactions != null && !recentTransactions.isEmpty()) { %>
                                        <div class="table-responsive">
                                            <table class="table table-sm">
                                                <thead>
                                                    <tr>
                                                        <th>User</th>
                                                        <th>Type</th>
                                                        <th>Amount</th>
                                                        <th>Date</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <% for (Map<String, Object> txn : recentTransactions) { %>
                                                    <tr>
                                                        <td><%= txn.get("userName") %></td>
                                                        <td><%= txn.get("type") %></td>
                                                        <td>₹<%= txn.get("amount") %></td>
                                                        <td><%= txn.get("date") %></td>
                                                    </tr>
                                                    <% } %>
                                                </tbody>
                                            </table>
                                        </div>
                                    <% } else { %>
                                        <p class="text-muted">No recent transactions</p>
                                    <% } %>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </div>
                <% } else { %>
                    <div class="alert alert-warning mt-4 shadow-sm" role="alert">
                        <h4 class="alert-heading"><i class="fas fa-exclamation-triangle"></i> No Dashboard Data Available</h4>
                        <p>We are unable to load the dashboard statistics at this moment. This might be because there are no users, accounts, or transactions registered in the system yet.</p>
                        <hr>
                        <p class="mb-0">Please register new users or verify database connectivity/logs for any underlying issues.</p>
                    </div>
                <% } %>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    
    <script>
        // Transaction Trends Chart
        <% if (stats != null) { %>
        <% Map<String, Object> transactionTrends = (Map<String, Object>) request.getAttribute("transactionTrends"); %>
        <% if (transactionTrends != null) { %>
        var ctx = document.getElementById("transactionChart").getContext('2d');
        var transactionChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [
                    <% List<Map<String, Object>> dailyCounts = (List<Map<String, Object>>) transactionTrends.get("dailyTransactionCounts"); %>
                    <% if (dailyCounts != null) { %>
                        <% for (int i = 0; i < dailyCounts.size(); i++) { %>
                            '<%= dailyCounts.get(i).get("date") %>'<%= i < dailyCounts.size() - 1 ? "," : "" %>
                        <% } %>
                    <% } %>
                ],
                datasets: [{
                    label: 'Transactions',
                    data: [
                        <% if (dailyCounts != null) { %>
                            <% for (int i = 0; i < dailyCounts.size(); i++) { %>
                                <%= dailyCounts.get(i).get("count") %><%= i < dailyCounts.size() - 1 ? "," : "" %>
                            <% } %>
                        <% } %>
                    ],
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });

        // Transaction Types Pie Chart
        var ctx2 = document.getElementById("transactionTypesChart").getContext('2d');
        var transactionTypesChart = new Chart(ctx2, {
            type: 'doughnut',
            data: {
                labels: [
                    <% List<Map<String, Object>> typeDistribution = (List<Map<String, Object>>) transactionTrends.get("transactionTypesDistribution"); %>
                    <% if (typeDistribution != null) { %>
                        <% for (int i = 0; i < typeDistribution.size(); i++) { %>
                            '<%= typeDistribution.get(i).get("type") %>'<%= i < typeDistribution.size() - 1 ? "," : "" %>
                        <% } %>
                    <% } %>
                ],
                datasets: [{
                    data: [
                        <% if (typeDistribution != null) { %>
                            <% for (int i = 0; i < typeDistribution.size(); i++) { %>
                                <%= typeDistribution.get(i).get("count") %><%= i < typeDistribution.size() - 1 ? "," : "" %>
                            <% } %>
                        <% } %>
                    ],
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0'
                    ]
                }]
            },
            options: {
                responsive: true
            }
        });
        <% } %>
        <% } %>
        
        function exportDashboard() {
            // Export dashboard data as PDF
            window.location.href = 'dashboard?action=export';
        }
    </script>

    <!-- Admin Notifications Offcanvas -->
    <div class="offcanvas offcanvas-end" tabindex="-1" id="adminNotifOffcanvas" aria-labelledby="adminNotifOffcanvasLabel">
        <div class="offcanvas-header bg-dark text-white">
            <h5 class="offcanvas-title" id="adminNotifOffcanvasLabel"><i class="bi bi-bell-fill me-2"></i>Admin Alerts</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>
        <div class="offcanvas-body p-0">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-light">
                <span class="text-muted small fw-bold text-uppercase">Recent Alerts</span>
                <button class="btn btn-sm btn-outline-secondary" onclick="markAllAdminNotificationsRead()"><i class="bi bi-check2-all me-1"></i>Mark All Read</button>
            </div>
            <div id="admin-notifications-list" class="list-group list-group-flush">
                <div class="p-4 text-center text-muted">
                    <div class="spinner-border spinner-border-sm text-primary mb-2" role="status"></div>
                    <div>Loading alerts...</div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Admin Notifications Logic
        window.addEventListener('DOMContentLoaded', () => {
            fetchAdminNotifications();
            setInterval(fetchAdminNotifications, 60000);
        });

        function fetchAdminNotifications() {
            fetch('<%= request.getContextPath() %>/api/notifications')
                .then(res => {
                    if (!res.ok) throw new Error("Not authorized");
                    return res.json();
                })
                .then(data => {
                    const badge = document.getElementById('admin-notif-badge');
                    const list = document.getElementById('admin-notifications-list');
                    
                    if (data.length > 0) {
                        badge.textContent = data.length;
                        badge.classList.remove('d-none');
                    } else {
                        badge.classList.add('d-none');
                    }
                    
                    if (data.length === 0) {
                        list.innerHTML = `
                            <div class="p-5 text-center text-muted">
                                <i class="bi bi-bell-slash fs-1 d-block mb-3 opacity-50"></i>
                                <h6 class="fw-bold">No new alerts</h6>
                                <small>System is quiet!</small>
                            </div>
                        `;
                        return;
                    }
                    
                    list.innerHTML = '';
                    data.forEach(n => {
                        let iconClass = 'bi-info-circle text-primary bg-primary bg-opacity-10';
                        if (n.type === 'SUCCESS') iconClass = 'bi-check-circle text-success bg-success bg-opacity-10';
                        if (n.type === 'WARNING') iconClass = 'bi-exclamation-triangle text-warning bg-warning bg-opacity-10';
                        if (n.type === 'ERROR') iconClass = 'bi-x-circle text-danger bg-danger bg-opacity-10';

                        list.innerHTML += `
                            <a href="javascript:void(0)" class="list-group-item list-group-item-action p-3 border-bottom transition-all" onclick="markAdminNotificationRead(${n.id})">
                                <div class="d-flex w-100">
                                    <div class="rounded-circle p-2 d-flex align-items-center justify-content-center me-3" style="width:40px; height:40px;">
                                        <i class="bi ${iconClass} fs-5"></i>
                                    </div>
                                    <div class="flex-grow-1">
                                        <div class="d-flex w-100 justify-content-between align-items-center mb-1">
                                            <h6 class="mb-0 fw-bold text-dark text-truncate" style="max-width:180px;">${n.title}</h6>
                                            <small class="text-muted" style="font-size:0.7rem;">${n.created_at}</small>
                                        </div>
                                        <p class="mb-0 text-secondary small lh-sm">${n.message}</p>
                                    </div>
                                </div>
                            </a>
                        `;
                    });
                })
                .catch(err => console.error("Failed to load admin notifications", err));
        }

        function markAdminNotificationRead(id) {
            fetch('<%= request.getContextPath() %>/api/notifications', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=mark_read&id=' + id
            }).then(() => fetchAdminNotifications());
        }

        function markAllAdminNotificationsRead() {
            fetch('<%= request.getContextPath() %>/api/notifications', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=mark_read'
            }).then(() => {
                fetchAdminNotifications();
                const offcanvas = bootstrap.Offcanvas.getInstance(document.getElementById('adminNotifOffcanvas'));
                if(offcanvas) offcanvas.hide();
            });
        }
    </script>
</body>
</html>
