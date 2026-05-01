<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "SkyBanking" %></title>
    
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    
    <!-- Premium Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/premium.css">
    
    <script>
        // Auto-dismiss alerts after 5 seconds
        window.addEventListener('DOMContentLoaded', () => {
            const alerts = document.querySelectorAll('.alert-dismissible');
            alerts.forEach(alertNode => {
                setTimeout(() => {
                    alertNode.classList.remove('show');
                    setTimeout(() => alertNode.remove(), 250);
                }, 5000);
            });
        });
    </script>
</head>
<body>
