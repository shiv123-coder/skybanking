<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login - Sky Banking System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../css/adminStyle.css" rel="stylesheet">
</head>
<body class="admin-login-body">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
                <div class="admin-login-card">
                    <div class="text-center mb-4">
                        <h2 class="admin-login-title">Admin Login</h2>
                        <p class="admin-login-subtitle">Sky Banking System</p>
                    </div>
                    
                    <% if (request.getAttribute("error") != null) { %>
                        <div id="errorAlert" class="alert alert-danger alert-dismissible fade show" role="alert">
                            <%= request.getAttribute("error") %>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    <% } %>
                    
                    <form action="${pageContext.request.contextPath}/admin/login" method="post">
                        <div class="mb-3">
                            <label for="username" class="form-label">Username</label>
                            <input type="text" class="form-control" id="username" name="username" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="password" name="password" required>
                        </div>
                        
                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary admin-login-btn">Login</button>
                        </div>
                    </form>
                    
                    <div class="text-center mt-3">
                        <a href="../login.jsp" class="admin-back-link">← Back to User Login</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Auto-hide alert after 5 seconds (5000 ms)
        window.addEventListener('DOMContentLoaded', () => {
            const alertBox = document.getElementById('errorAlert');
            if(alertBox){
                setTimeout(() => {
                    const bsAlert = new bootstrap.Alert(alertBox);
                    bsAlert.close(); // dismiss alert
                }, 5000);
            }
        });
    </script>
</body>
</html>
