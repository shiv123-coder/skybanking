<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="com.skybanking.model.User" %>
        <% User user=(User) request.getAttribute("user"); if (user==null) { response.sendRedirect("users"); return; } %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <title>Edit User - SkyBanking</title>
                <!-- App Icon / Favicon -->
                <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/favicon.svg">
                <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.svg">
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
                <!-- Bootstrap Icons -->
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/adminStyle.css">
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
                <div class="container mt-4">
                    <h2>Edit User</h2>
                    <form action="users" method="post">
                        <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="userId" value="<%= user.getId() %>">

                        <div class="mb-3">
                            <label for="fullname" class="form-label">Full Name</label>
                            <input type="text" class="form-control" id="fullname" name="fullname"
                                value="<%= user.getFullname() %>" required>
                        </div>
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email"
                                value="<%= user.getEmail() %>" required>
                        </div>
                        <div class="mb-3">
                            <label for="phone" class="form-label">Phone</label>
                            <input type="text" class="form-control" id="phone" name="phone"
                                value="<%= user.getPhone() %>" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Update User</button>
                        <a href="users" class="btn btn-secondary">Back to Users</a>
                    </form>
                </div>
            </body>

            </html>