<%@ page contentType="text/html;charset=UTF-8" %>
<% if (session.getAttribute("user_id") != null) { 
    String requestUri = request.getRequestURI();
%>
<nav class="sidebar glass-panel d-flex flex-column p-4 m-3 shadow-lg">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <a href="<%= request.getContextPath() %>/dashboard.jsp" class="d-flex align-items-center text-decoration-none transition-transform hover-scale">
            <div class="bg-primary text-white rounded p-2 me-3 shadow-sm">
                <i class="bi bi-bank2 fs-4"></i>
            </div>
            <span class="fs-4 fw-bold gradient-text">SkyBank</span>
        </a>
    </div>
    <hr class="text-muted opacity-25">
    <ul class="nav nav-pills flex-column mb-auto gap-1">
        <li class="nav-item">
            <a href="<%= request.getContextPath() %>/dashboard.jsp" class="nav-link <%= requestUri.contains("dashboard") ? "active" : "" %>">
                <i class="bi bi-grid me-3"></i> Dashboard
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/addmoney.jsp" class="nav-link <%= requestUri.contains("addmoney") ? "active" : "" %>">
                <i class="bi bi-stripe me-3 text-primary"></i> Add Money
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/qr" class="nav-link <%= requestUri.contains("qr") ? "active" : "" %>">
                <i class="bi bi-qr-code-scan me-3 text-success"></i> Receive (QR)
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/deposit.jsp" class="nav-link <%= requestUri.contains("deposit") ? "active" : "" %>">
                <i class="bi bi-wallet2 me-3"></i> Deposit
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/withdraw.jsp" class="nav-link <%= requestUri.contains("withdraw") ? "active" : "" %>">
                <i class="bi bi-cash-stack me-3"></i> Withdraw
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/transfer.jsp" class="nav-link <%= requestUri.contains("transfer") ? "active" : "" %>">
                <i class="bi bi-send me-3"></i> Transfer
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/transactions" class="nav-link <%= requestUri.contains("transactions") ? "active" : "" %>">
                <i class="bi bi-clock-history me-3"></i> History
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/statement?action=mini" class="nav-link <%= requestUri.contains("statement") ? "active" : "" %>">
                <i class="bi bi-file-earmark-text me-3"></i> Statement
            </a>
        </li>
        <li>
            <a href="<%= request.getContextPath() %>/loan" class="nav-link <%= requestUri.contains("loan") ? "active" : "" %>">
                <i class="bi bi-bank me-3"></i> Loans
            </a>
        </li>
    </ul>
    
    <div class="mt-auto pt-3">
        <div class="glass-panel p-3 rounded-4 d-flex align-items-center justify-content-between">
            <a href="<%= request.getContextPath() %>/userinfo" class="d-flex align-items-center text-decoration-none profile-link flex-grow-1">
                <div class="rounded-circle bg-gradient-primary text-white d-flex align-items-center justify-content-center me-3 shadow-sm fw-bold fs-5" style="width: 40px; height: 40px; border: 2px solid white;">
                    <%= session.getAttribute("username").toString().substring(0, 1).toUpperCase() %>
                </div>
                <div class="d-flex flex-column text-truncate">
                    <strong class="text-dark small"><%= session.getAttribute("username") %></strong>
                    <span class="text-muted" style="font-size: 0.75rem;">Verified User</span>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/logout.jsp" class="btn btn-sm btn-light text-danger border-0 rounded-circle d-flex align-items-center justify-content-center ms-2" style="width:36px; height:36px;" title="Logout">
                <i class="bi bi-box-arrow-right fs-5"></i>
            </a>
        </div>
    </div>
</nav>
<% } %>

<script>
    // Preloader Toggle Logic
    window.addEventListener('DOMContentLoaded', () => {
        const preloaderToggle = document.getElementById('preloader-toggle');
        const preloaderIcon = document.getElementById('preloader-icon');

        function updatePreloaderIcon(enabled) {
            if (enabled) {
                preloaderIcon.classList.replace('bi-lightning', 'bi-lightning-charge');
                preloaderIcon.classList.add('text-success');
                preloaderIcon.classList.remove('text-muted');
            } else {
                preloaderIcon.classList.replace('bi-lightning-charge', 'bi-lightning');
                preloaderIcon.classList.remove('text-success');
                preloaderIcon.classList.add('text-muted');
            }
        }

        // Initialize state
        const isPreloaderDisabled = localStorage.getItem('disablePreloader') === 'true';
        updatePreloaderIcon(!isPreloaderDisabled);

        preloaderToggle.addEventListener('click', () => {
            const currentDisabled = localStorage.getItem('disablePreloader') === 'true';
            const newDisabled = !currentDisabled;
            localStorage.setItem('disablePreloader', newDisabled);
            updatePreloaderIcon(!newDisabled);
            
            // Show alert for feedback
            const status = newDisabled ? 'disabled' : 'enabled';
            alert(`Preloader has been ${status}. It will take effect on next refresh.`);
        });
    });
</script>

<!-- Notifications Offcanvas -->
<div class="offcanvas offcanvas-end" tabindex="-1" id="notificationsOffcanvas" aria-labelledby="notificationsOffcanvasLabel">
    <div class="offcanvas-header bg-primary text-white">
        <h5 class="offcanvas-title" id="notificationsOffcanvasLabel"><i class="bi bi-bell-fill me-2"></i>Notifications</h5>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-0">
        <div class="d-flex justify-content-between align-items-center p-3 border-bottom bg-light">
            <span class="text-muted small fw-bold text-uppercase">Recent Alerts</span>
            <button class="btn btn-sm btn-outline-secondary" onclick="markAllNotificationsRead()"><i class="bi bi-check2-all me-1"></i>Mark All Read</button>
        </div>
        <div id="notifications-list" class="list-group list-group-flush">
            <div class="p-4 text-center text-muted">
                <div class="spinner-border spinner-border-sm text-primary mb-2" role="status"></div>
                <div>Loading notifications...</div>
            </div>
        </div>
    </div>
</div>

<script>
    // Notifications Logic
    window.addEventListener('DOMContentLoaded', () => {
        fetchNotifications();
        // refresh every 15 seconds for snappier updates
        setInterval(fetchNotifications, 15000);
    });

    function timeAgo(dateString) {
        if (!dateString) return "Just now";
        const date = new Date(dateString);
        const seconds = Math.floor((new Date() - date) / 1000);
        let interval = seconds / 31536000;
        if (interval > 1) return Math.floor(interval) + " years ago";
        interval = seconds / 2592000;
        if (interval > 1) return Math.floor(interval) + " months ago";
        interval = seconds / 86400;
        if (interval > 1) return Math.floor(interval) + " days ago";
        interval = seconds / 3600;
        if (interval > 1) return Math.floor(interval) + " hours ago";
        interval = seconds / 60;
        if (interval > 1) return Math.floor(interval) + " mins ago";
        return "Just now";
    }

    function fetchNotifications() {
        fetch('<%= request.getContextPath() %>/api/notifications')
            .then(res => {
                if (!res.ok) throw new Error("Not authorized");
                return res.json();
            })
            .then(data => {
                const badge = document.getElementById('notif-badge');
                const list = document.getElementById('notifications-list');
                
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
                            <h6 class="fw-bold">No new notifications</h6>
                            <small>You're all caught up!</small>
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
                        <a href="javascript:void(0)" class="list-group-item list-group-item-action p-3 border-bottom transition-all hover-bg-light" onclick="markNotificationRead(\${n.id})">
                            <div class="d-flex w-100">
                                <div class="rounded-circle p-2 d-flex align-items-center justify-content-center me-3" style="width:40px; height:40px;">
                                    <i class="bi \${iconClass} fs-5"></i>
                                </div>
                                <div class="flex-grow-1">
                                    <div class="d-flex w-100 justify-content-between align-items-center mb-1">
                                        <h6 class="mb-0 fw-bold text-dark text-truncate" style="max-width:180px;">\${n.title || 'Alert'}</h6>
                                        <small class="text-muted" style="font-size:0.7rem;">\${timeAgo(n.created_at)}</small>
                                    </div>
                                    <p class="mb-0 text-secondary small lh-sm">\${n.message || 'No details available.'}</p>
                                </div>
                            </div>
                        </a>
                    `;
                });
            })
            .catch(err => {
                // Not logged in or error
                console.error("Failed to load notifications", err);
            });
    }

    function markNotificationRead(id) {
        fetch('<%= request.getContextPath() %>/api/notifications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=mark_read&id=' + id
        }).then(() => fetchNotifications());
    }

    function markAllNotificationsRead() {
        fetch('<%= request.getContextPath() %>/api/notifications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=mark_read'
        }).then(() => {
            fetchNotifications();
            const offcanvas = bootstrap.Offcanvas.getInstance(document.getElementById('notificationsOffcanvas'));
            if(offcanvas) offcanvas.hide();
        });
    }
</script>
