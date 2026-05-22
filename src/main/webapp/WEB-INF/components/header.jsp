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
    
    <!-- App Icon / Favicon -->
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/favicon.svg">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.svg">
    
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
    <jsp:include page="/WEB-INF/components/preloader.jsp" />

    <% boolean isLoggedIn = session != null && session.getAttribute("user_id") != null; %>
    <!-- Global Toggles (Fixed Top Right) -->
    <div class="position-fixed top-0 end-0 p-4 z-3 d-flex gap-2 align-items-center mt-2 me-2" style="pointer-events: auto;">
        <% if (isLoggedIn) { %>
        <!-- Notifications Toggle -->
        <button class="btn btn-link p-2 text-decoration-none border-0 transition-all rounded-circle glass-panel position-relative d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;" data-bs-toggle="offcanvas" data-bs-target="#notificationsOffcanvas" title="Notifications">
            <i class="bi bi-bell fs-5 text-dark"></i>
            <span id="notif-badge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none" style="font-size: 0.6rem;">0</span>
        </button>
        <% } %>
        
        <!-- Preloader Toggle -->
        <button id="preloader-toggle" class="btn btn-link p-2 text-decoration-none border-0 transition-all rounded-circle glass-panel d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;" title="Toggle Preloader Animation">
            <i class="bi bi-lightning-charge fs-5 text-success" id="preloader-icon"></i>
        </button>
        
        <!-- Theme Toggle -->
        <button id="theme-toggle" class="btn btn-link p-0 text-decoration-none border-0 transition-all rounded-circle glass-panel d-flex align-items-center justify-content-center shadow-sm" style="width: 42px; height: 42px; background: var(--glass-bg);" title="Switch to Dark/Light Mode">
            <div class="theme-icon-wrapper position-relative" style="width: 20px; height: 20px;">
                <i class="bi bi-sun-fill position-absolute start-50 top-50 translate-middle fs-5 text-warning theme-sun" style="transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1); opacity: 0; transform: translate(-50%, -50%) rotate(-90deg) scale(0);"></i>
                <i class="bi bi-moon-stars-fill position-absolute start-50 top-50 translate-middle fs-5 text-primary theme-moon" style="transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1); opacity: 0; transform: translate(-50%, -50%) rotate(90deg) scale(0);"></i>
            </div>
        </button>
    </div>
