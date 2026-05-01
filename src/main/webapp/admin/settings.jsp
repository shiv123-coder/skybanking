<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings - Admin Panel</title>
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
                        <li class="nav-item"><a class="nav-link" href="dashboard"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
                        <li class="nav-item"><a class="nav-link" href="users"><i class="fas fa-users"></i> Users</a></li>
                        <li class="nav-item"><a class="nav-link" href="transactions"><i class="fas fa-exchange-alt"></i> Transactions</a></li>
                        <li class="nav-item"><a class="nav-link" href="logs"><i class="fas fa-list-alt"></i> Logs</a></li>
                        <li class="nav-item"><a class="nav-link active" href="settings"><i class="fas fa-cog"></i> Settings</a></li>
                    </ul>
                </div>
            </nav>

            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">Settings</h1>
                </div>

                <!-- Flash Messages -->
                <%
                    String error = (String) session.getAttribute("error");
                    String message = (String) session.getAttribute("message");
                    if (error != null) {
                %>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <%= error %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                        session.removeAttribute("error");
                    }
                    if (message != null) {
                %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <%= message %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <%
                        session.removeAttribute("message");
                    }
                %>

                <!-- Settings Tabs -->
                <ul class="nav nav-tabs" id="settingsTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="profile-tab" data-bs-toggle="tab" data-bs-target="#profile" type="button" role="tab"><i class="fas fa-user"></i> Admin Profile</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="password-tab" data-bs-toggle="tab" data-bs-target="#password" type="button" role="tab"><i class="fas fa-key"></i> Change Password</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="system-tab" data-bs-toggle="tab" data-bs-target="#system" type="button" role="tab"><i class="fas fa-cogs"></i> System Settings</button>
                    </li>
                </ul>

                <div class="tab-content" id="settingsTabsContent">

                    <!-- Admin Profile Tab -->
                    <div class="tab-pane fade show active" id="profile" role="tabpanel">
                        <div class="card mt-3">
                            <div class="card-header"><h5 class="mb-0">Admin Profile</h5></div>
                            <div class="card-body">
                                <% Map<String,Object> adminProfile = (Map<String,Object>) request.getAttribute("adminProfile"); %>
                                <% if (adminProfile != null) { %>
                                <form method="post" action="settings">
                                    <input type="hidden" name="action" value="update_profile">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Full Name</label>
                                                <input type="text" class="form-control" name="fullName" 
                                                    value="<%= adminProfile.get("fullName") != null ? adminProfile.get("fullName") : "" %>" required>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Email</label>
                                                <input type="email" class="form-control" name="email" 
                                                    value="<%= adminProfile.get("email") != null ? adminProfile.get("email") : "" %>" required>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Username</label>
                                                <input type="text" class="form-control" value="<%= adminProfile.get("username") %>" readonly>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Admin ID</label>
                                                <input type="text" class="form-control" value="<%= adminProfile.get("adminId") %>" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Created At</label>
                                                <input type="text" class="form-control" value="<%= adminProfile.get("createdAt") %>" readonly>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Last Login</label>
                                                <input type="text" class="form-control" value="<%= adminProfile.get("lastLogin") != null ? adminProfile.get("lastLogin") : "Never" %>" readonly>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Update Profile</button>
                                </form>
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <!-- Change Password Tab -->
                    <div class="tab-pane fade" id="password" role="tabpanel">
                        <div class="card mt-3">
                            <div class="card-header"><h5 class="mb-0">Change Password</h5></div>
                            <div class="card-body">
                                <form method="post" action="settings">
                                    <input type="hidden" name="action" value="change_password">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Current Password</label>
                                                <input type="password" class="form-control" name="currentPassword" required>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">New Password</label>
                                                <input type="password" class="form-control" name="newPassword" minlength="8" required>
                                                <div class="form-text">Password must be at least 8 characters with uppercase, lowercase, and digit.</div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3">
                                                <label class="form-label">Confirm New Password</label>
                                                <input type="password" class="form-control" name="confirmPassword" minlength="8" required>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" class="btn btn-warning"><i class="fas fa-key"></i> Change Password</button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- System Settings Tab -->
                    <div class="tab-pane fade" id="system" role="tabpanel">
                        <div class="card mt-3">
                            <div class="card-header"><h5 class="mb-0">System Settings</h5></div>
                            <div class="card-body">
                                <% Map<String,Object> systemSettings = (Map<String,Object>) request.getAttribute("systemSettings"); %>
                                <% if (systemSettings != null) { %>
                                <form method="post" action="settings">
                                    <input type="hidden" name="action" value="update_system_settings">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <h6 class="text-primary">Security Settings</h6>
                                            <div class="mb-3">
                                                <label class="form-label">OTP Expiry Time (minutes)</label>
                                                <input type="number" class="form-control" name="otpExpiryTime" value="<%= systemSettings.get("otp_expiry_minutes") %>" min="1" max="60">
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Max Login Attempts</label>
                                                <input type="number" class="form-control" name="maxLoginAttempts" value="<%= systemSettings.get("max_login_attempts") %>" min="1" max="10">
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Session Timeout (minutes)</label>
                                                <input type="number" class="form-control" name="sessionTimeout" value="<%= systemSettings.get("session_timeout_minutes") %>" min="5" max="480">
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <h6 class="text-primary">Tax & Fees</h6>
                                            <div class="mb-3">
                                                <label class="form-label">GST Rate (%)</label>
                                                <input type="number" class="form-control" name="gstRate" 
                                                       value="<%= systemSettings.get("gst_rate") != null ? Double.parseDouble(systemSettings.get("gst_rate").toString()) * 100 : 0 %>" 
                                                       min="0" max="100" step="0.01">
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">TDS Rate (%)</label>
                                                <input type="number" class="form-control" name="tdsRate" 
                                                       value="<%= systemSettings.get("tds_rate") != null ? Double.parseDouble(systemSettings.get("tds_rate").toString()) * 100 : 0 %>" 
                                                       min="0" max="100" step="0.01">
                                            </div>
                                            <div class="mb-3">
                                                <label class="form-label">Transfer Fee Rate (%)</label>
                                                <input type="number" class="form-control" name="transferFeeRate" 
                                                       value="<%= systemSettings.get("transfer_fee_rate") != null ? Double.parseDouble(systemSettings.get("transfer_fee_rate").toString()) * 100 : 0 %>" 
                                                       min="0" max="10" step="0.01">
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-md-12">
                                            <h6 class="text-primary">System Status</h6>
                                            <div class="form-check mb-3">
                                                <input class="form-check-input" type="checkbox" name="maintenanceMode" <%= "true".equals(systemSettings.get("maintenance_mode")) ? "checked" : "" %>>
                                                <label class="form-check-label">Maintenance Mode</label>
                                            </div>
                                        </div>
                                    </div>

                                    <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Update System Settings</button>
                                </form>
                                <% } %>
                            </div>
                        </div>
                    </div>

                </div>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>

    <script>
        // Password confirmation validation
        document.querySelector('input[name="confirmPassword"]').addEventListener('input', function() {
            const newPassword = document.querySelector('input[name="newPassword"]').value;
            const confirmPassword = this.value;
            if(newPassword !== confirmPassword){
                this.setCustomValidity('Passwords do not match');
            } else {
                this.setCustomValidity('');
            }
        });
    </script>
</body>
</html>
