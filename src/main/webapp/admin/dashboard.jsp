<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Sky Banking System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../css/adminStyle.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">Sky Banking Admin</a>
            <div class="navbar-nav ms-auto">
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
</body>
</html>
