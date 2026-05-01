<%@ page contentType="text/html;charset=UTF-8" %>
<% if (session.getAttribute("user_id") != null) { 
    String requestUri = request.getRequestURI();
%>
<nav class="sidebar glass-panel d-flex flex-column p-4 m-3 shadow-lg">
    <a href="dashboard.jsp" class="d-flex align-items-center mb-4 me-md-auto text-decoration-none transition-transform hover-scale">
        <div class="bg-primary text-white rounded p-2 me-3 shadow-sm">
            <i class="bi bi-bank2 fs-4"></i>
        </div>
        <span class="fs-4 fw-bold gradient-text">SkyBank</span>
    </a>
    <hr class="text-muted opacity-25">
    <ul class="nav nav-pills flex-column mb-auto gap-1">
        <li class="nav-item">
            <a href="dashboard.jsp" class="nav-link <%= requestUri.contains("dashboard") ? "active" : "" %>">
                <i class="bi bi-grid me-3"></i> Dashboard
            </a>
        </li>
        <li>
            <a href="addmoney.jsp" class="nav-link <%= requestUri.contains("addmoney") ? "active" : "" %>">
                <i class="bi bi-stripe me-3 text-primary"></i> Add Money
            </a>
        </li>
        <li>
            <a href="qr" class="nav-link <%= requestUri.contains("qr") ? "active" : "" %>">
                <i class="bi bi-qr-code-scan me-3 text-success"></i> Receive (QR)
            </a>
        </li>
        <li>
            <a href="deposit.jsp" class="nav-link <%= requestUri.contains("deposit") ? "active" : "" %>">
                <i class="bi bi-wallet2 me-3"></i> Deposit
            </a>
        </li>
        <li>
            <a href="withdraw.jsp" class="nav-link <%= requestUri.contains("withdraw") ? "active" : "" %>">
                <i class="bi bi-cash-stack me-3"></i> Withdraw
            </a>
        </li>
        <li>
            <a href="transfer.jsp" class="nav-link <%= requestUri.contains("transfer") ? "active" : "" %>">
                <i class="bi bi-send me-3"></i> Transfer
            </a>
        </li>
        <li>
            <a href="transactions" class="nav-link <%= requestUri.contains("transactions") ? "active" : "" %>">
                <i class="bi bi-clock-history me-3"></i> History
            </a>
        </li>
        <li>
            <a href="statement?action=mini" class="nav-link <%= requestUri.contains("statement") ? "active" : "" %>">
                <i class="bi bi-file-earmark-text me-3"></i> Statement
            </a>
        </li>
        <li>
            <a href="loan" class="nav-link <%= requestUri.contains("loan") ? "active" : "" %>">
                <i class="bi bi-bank me-3"></i> Loans
            </a>
        </li>
    </ul>
    
    <div class="mt-auto pt-3">
        <div class="glass-panel p-3 rounded-4 d-flex align-items-center justify-content-between">
            <a href="userinfo" class="d-flex align-items-center text-decoration-none profile-link flex-grow-1">
                <div class="rounded-circle bg-gradient-primary text-white d-flex align-items-center justify-content-center me-3 shadow-sm fw-bold fs-5" style="width: 40px; height: 40px; border: 2px solid white;">
                    <%= session.getAttribute("username").toString().substring(0, 1).toUpperCase() %>
                </div>
                <div class="d-flex flex-column text-truncate">
                    <strong class="text-dark small"><%= session.getAttribute("username") %></strong>
                    <span class="text-muted" style="font-size: 0.75rem;">Verified User</span>
                </div>
            </a>
            <a href="logout.jsp" class="btn btn-sm btn-light text-danger border-0 rounded-circle d-flex align-items-center justify-content-center ms-2" style="width:36px; height:36px;" title="Logout">
                <i class="bi bi-box-arrow-right fs-5"></i>
            </a>
        </div>
    </div>
</nav>
<% } %>
