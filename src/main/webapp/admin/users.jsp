<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.skybanking.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management - Admin Panel</title>
    <!-- App Icon / Favicon -->
    <link rel="icon" type="image/svg+xml" href="<%= request.getContextPath() %>/images/favicon.svg">
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/images/favicon.svg">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <link href="<%= request.getContextPath() %>/css/adminStyle.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    <!-- Global Theme Script -->
    <script src="<%= request.getContextPath() %>/js/theme.js" defer></script>
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
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-body">
                        <form method="post" action="users" class="mb-0">
                            <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                            <input type="hidden" name="action" value="search">
                            <div class="row g-3 align-items-center">
                                <div class="col-md-5">
                                    <div class="input-group">
                                        <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
                                        <input type="text" class="form-control border-start-0 ps-0" name="search" 
                                               placeholder="Search by name, username, or email" 
                                               value="<%= request.getAttribute("search") != null ? request.getAttribute("search") : "" %>">
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <select name="status" class="form-select">
                                        <option value="">All Status</option>
                                        <option value="active" <%= "active".equals(request.getAttribute("status")) ? "selected" : "" %>>Active</option>
                                        <option value="inactive" <%= "inactive".equals(request.getAttribute("status")) ? "selected" : "" %>>Inactive</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <button type="submit" class="btn btn-primary px-4 fw-semibold shadow-sm">Search</button>
                                    <a href="users" class="btn btn-outline-secondary px-4 fw-semibold ms-2">Clear</a>
                                </div>
                            </div>
                        </form>
                    </div>
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
                            <div class="table-responsive px-2 pb-2">
                                <table class="table table-hover align-middle mb-0 premium-table" style="border-spacing: 0 10px;">
                                    <thead class="text-muted small fw-bold text-uppercase" style="letter-spacing: 0.5px;">
                                        <tr>
                                            <th class="border-0 ps-3">ID</th>
                                            <th class="border-0">Full Name</th>
                                            <th class="border-0">Username</th>
                                            <th class="border-0">Email</th>
                                            <th class="border-0">Phone</th>
                                            <th class="border-0 text-center">Status</th>
                                            <th class="border-0">Created</th>
                                            <th class="border-0">Last Login</th>
                                            <th class="border-0 text-center pe-3">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (User user : users) { %>
                                            <tr class="bg-white shadow-sm rounded-3 hover-scale-slight">
                                                <td class="border-0 ps-3 py-3 rounded-start fw-bold text-secondary">#<%= user.getId() %></td>
                                                <td class="border-0 py-3 fw-medium">
                                                    <div class="d-flex align-items-center">
                                                        <div class="bg-light rounded-circle p-2 me-2 border text-center" style="width: 32px; height: 32px;">
                                                            <i class="fas fa-user text-primary" style="font-size: 0.8rem;"></i>
                                                        </div>
                                                        <%= user.getFullname() %>
                                                    </div>
                                                </td>
                                                <td class="border-0 py-3"><span class="badge bg-light text-dark border px-2 py-1"><%= user.getUsername() %></span></td>
                                                <td class="border-0 py-3 text-muted"><%= user.getEmail() %></td>
                                                <td class="border-0 py-3 text-muted"><%= user.getPhone() %></td>
                                                <td class="border-0 py-3 text-center">
                                                    <span class="badge <%= user.isActive() ? "bg-success bg-opacity-10 text-success border border-success" : "bg-danger bg-opacity-10 text-danger border border-danger" %> px-3 py-2 rounded-pill">
                                                        <%= user.isActive() ? "Active" : "Inactive" %>
                                                    </span>
                                                </td>
                                                <td class="border-0 py-3 text-muted small"><%= user.getCreatedAt() %></td>
                                                <td class="border-0 py-3 text-muted small"><%= user.getLastLogin() != null ? user.getLastLogin() : "Never" %></td>
                                                <td class="border-0 py-3 rounded-end text-center pe-3">
                                                    <div class="d-flex justify-content-center gap-1">
                                                        <a href="users?action=view&userId=<%= user.getId() %>" class="btn btn-sm btn-light text-info rounded-circle shadow-sm hover-scale-slight" title="View" style="width:32px;height:32px;padding:4px;">
                                                            <i class="fas fa-eye"></i>
                                                        </a>
                                                        <a href="users?action=edit&userId=<%= user.getId() %>" class="btn btn-sm btn-light text-warning rounded-circle shadow-sm hover-scale-slight" title="Edit" style="width:32px;height:32px;padding:4px;">
                                                            <i class="fas fa-edit"></i>
                                                        </a>
                                                        <a href="users?action=transactions&userId=<%= user.getId() %>" class="btn btn-sm btn-light text-primary rounded-circle shadow-sm hover-scale-slight" title="Transactions" style="width:32px;height:32px;padding:4px;">
                                                            <i class="fas fa-exchange-alt"></i>
                                                        </a>
                                                        <a href="users?action=export&userId=<%= user.getId() %>" class="btn btn-sm btn-light text-success rounded-circle shadow-sm hover-scale-slight" title="Export PDF" style="width:32px;height:32px;padding:4px;">
                                                            <i class="fas fa-download"></i>
                                                        </a>

                                                        <% if (user.isActive()) { %>
                                                            <form method="post" action="users" style="display:inline;">
                                                                <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                                                                <input type="hidden" name="action" value="deactivate">
                                                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                                <button type="submit" class="btn btn-sm btn-light text-warning rounded-circle shadow-sm hover-scale-slight" title="Deactivate"
                                                                    onclick="return confirm('Are you sure you want to deactivate this user?')" style="width:32px;height:32px;padding:4px;">
                                                                    <i class="fas fa-user-times"></i>
                                                                </button>
                                                            </form>
                                                        <% } else { %>
                                                            <form method="post" action="users" style="display:inline;">
                                                                <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                                                                <input type="hidden" name="action" value="activate">
                                                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                                <button type="submit" class="btn btn-sm btn-light text-success rounded-circle shadow-sm hover-scale-slight" title="Activate" style="width:32px;height:32px;padding:4px;">
                                                                    <i class="fas fa-user-check"></i>
                                                                </button>
                                                            </form>
                                                        <% } %>

                                                        <form method="post" action="users" style="display:inline;">
                                                            <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="userId" value="<%= user.getId() %>">
                                                            <button type="submit" class="btn btn-sm btn-light text-danger rounded-circle shadow-sm hover-scale-slight" title="Delete"
                                                                onclick="return confirm('Are you sure you want to delete this user?')" style="width:32px;height:32px;padding:4px;">
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
