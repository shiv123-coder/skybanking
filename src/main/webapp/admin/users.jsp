<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.skybanking.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management - Admin Panel</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/adminStyle.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
</head>
<body>
    <!-- Navbar -->
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
            <!-- Sidebar -->
            <nav class="col-md-3 col-lg-2 d-md-block bg-light sidebar">
                <div class="position-sticky pt-3">
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link" href="dashboard">
                                <i class="fas fa-tachometer-alt"></i> Dashboard
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="users">
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

            <!-- Main Content -->
            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">User Management</h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <div class="btn-group me-2">
                            <!-- Fixed Export All button -->
                            <a href="<%= request.getContextPath() %>/admin/users?action=exportUsers" 
                               class="btn btn-sm btn-outline-secondary">
                                <i class="fas fa-download"></i> Export All
                            </a>
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

                <!-- Search & Filter -->
                <div class="search-filter-container mb-3">
                    <form method="post" action="users">
                        <input type="hidden" name="action" value="search">
                        <div class="row g-2">
                            <div class="col-md-4">
                                <input type="text" class="form-control" name="search" 
                                       placeholder="Search by name, username, or email" 
                                       value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                            </div>
                            <div class="col-md-3">
                                <select name="status" class="form-select">
                                    <option value="">All Status</option>
                                    <option value="active" <%= "active".equals(request.getAttribute("status")) ? "selected" : "" %>>Active</option>
                                    <option value="inactive" <%= "inactive".equals(request.getAttribute("status")) ? "selected" : "" %>>Inactive</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <button type="submit" class="btn btn-primary"><i class="fas fa-search"></i> Search</button>
                                <a href="users" class="btn btn-outline-secondary">Clear</a>
                            </div>
                        </div>
                    </form>
                </div>

                <!-- Users Table -->
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">Users List 
                            <span class="badge bg-primary"><%= request.getAttribute("totalUsers") != null ? request.getAttribute("totalUsers") : 0 %></span>
                        </h5>
                    </div>
                    <div class="card-body">
                        <% List<User> users = (List<User>) request.getAttribute("users"); %>
                        <% if (users != null && !users.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-striped table-hover align-middle">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>ID</th>
                                            <th>Full Name</th>
                                            <th>Username</th>
                                            <th>Email</th>
                                            <th>Phone</th>
                                            <th>Status</th>
                                            <th>Created</th>
                                            <th>Last Login</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (User user : users) { %>
                                            <tr>
                                                <td><%= user.getId() %></td>
                                                <td><%= user.getFullname() %></td>
                                                <td><%= user.getUsername() %></td>
                                                <td><%= user.getEmail() %></td>
                                                <td><%= user.getPhone() %></td>
                                                <td>
                                                    <span class="badge bg-<%= user.isActive() ? "success" : "danger" %>">
                                                        <%= user.isActive() ? "Active" : "Inactive" %>
                                                    </span>
                                                </td>
                                                <td><%= user.getCreatedAt() %></td>
                                                <td><%= user.getLastLogin() != null ? user.getLastLogin() : "Never" %></td>
                                                <td>
                                                    <div class="btn-group btn-group-sm">
                                                        <a href="users?action=view&userId=<%= user.getId() %>" class="btn btn-outline-info" title="View">
                                                            <i class="fas fa-eye"></i>
                                                        </a>
                                                        <a href="users?action=edit&userId=<%= user.getId() %>" class="btn btn-outline-warning" title="Edit">
                                                            <i class="fas fa-edit"></i>
                                                        </a>
                                                        <a href="users?action=transactions&userId=<%= user.getId() %>" class="btn btn-outline-primary" title="Transactions">
                                                            <i class="fas fa-exchange-alt"></i>
                                                        </a>
                                                        <a href="users?action=export&userId=<%= user.getId() %>" class="btn btn-outline-success" title="Export PDF">
                                                            <i class="fas fa-download"></i>
                                                        </a>

                                                        <% if (user.isActive()) { %>
                                                            <form method="post" action="users" style="display:inline;">
                                                                <input type="hidden" name="action" value="deactivate">
                                                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                                <button type="submit" class="btn btn-outline-warning btn-sm" title="Deactivate"
                                                                    onclick="return confirm('Are you sure you want to deactivate this user?')">
                                                                    <i class="fas fa-user-times"></i>
                                                                </button>
                                                            </form>
                                                        <% } else { %>
                                                            <form method="post" action="users" style="display:inline;">
                                                                <input type="hidden" name="action" value="activate">
                                                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                                <button type="submit" class="btn btn-outline-success btn-sm" title="Activate">
                                                                    <i class="fas fa-user-check"></i>
                                                                </button>
                                                            </form>
                                                        <% } %>

                                                        <form method="post" action="users" style="display:inline;">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                            <button type="submit" class="btn btn-outline-danger btn-sm" title="Delete"
                                                                onclick="return confirm('Are you sure you want to delete this user?')">
                                                                <i class="fas fa-trash"></i>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <% if (request.getAttribute("totalPages") != null && (Integer) request.getAttribute("totalPages") > 1) { %>
                                <nav aria-label="Users pagination">
                                    <ul class="pagination justify-content-center">
                                        <% int currentPage = (Integer) request.getAttribute("currentPage"); %>
                                        <% int totalPages = (Integer) request.getAttribute("totalPages"); %>

                                        <% if (currentPage > 1) { %>
                                            <li class="page-item">
                                                <a class="page-link" href="users?page=<%= currentPage - 1 %>&search=<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>&status=<%= request.getAttribute("status") != null ? request.getAttribute("status") : "" %>">Previous</a>
                                            </li>
                                        <% } %>

                                        <% for (int i = 1; i <= totalPages; i++) { %>
                                            <li class="page-item <%= i == currentPage ? "active" : "" %>">
                                                <a class="page-link" href="users?page=<%= i %>&search=<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>&status=<%= request.getAttribute("status") != null ? request.getAttribute("status") : "" %>"><%= i %></a>
                                            </li>
                                        <% } %>

                                        <% if (currentPage < totalPages) { %>
                                            <li class="page-item">
                                                <a class="page-link" href="users?page=<%= currentPage + 1 %>&search=<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>&status=<%= request.getAttribute("status") != null ? request.getAttribute("status") : "" %>">Next</a>
                                            </li>
                                        <% } %>
                                    </ul>
                                </nav>
                            <% } %>

                        <% } else { %>
                            <div class="text-center py-4">
                                <p class="text-muted">No users found.</p>
                            </div>
                        <% } %>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
