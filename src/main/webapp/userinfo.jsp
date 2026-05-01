<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    if(session == null || session.getAttribute("user_id") == null){
        response.sendRedirect("login.jsp");
        return;
    }

    String fullname = (String) request.getAttribute("fullname");
    String username = (String) request.getAttribute("username");
    String email = (String) request.getAttribute("email");
    String mobile = (String) request.getAttribute("mobile");
    String signupDate = (String) request.getAttribute("signupDate");
    String accountCode = (String) request.getAttribute("accountCode");
    Object balanceObj = request.getAttribute("balance");
    double balance = 0.0;
    if(balanceObj instanceof java.math.BigDecimal){
        balance = ((java.math.BigDecimal) balanceObj).doubleValue();
    } else if (balanceObj instanceof Number) {
        balance = ((Number) balanceObj).doubleValue();
    }
    
    request.setAttribute("pageTitle", "User Profile - SkyBanking");
%>

<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h3 mb-0 text-gray-800 fw-bold">User Profile</h2>
                <p class="text-muted mb-0">Manage your account details and information.</p>
            </div>
            <a href="dashboard.jsp" class="btn btn-outline-primary d-flex align-items-center px-4 rounded-pill fw-semibold bg-white">
                <i class="bi bi-arrow-left me-2"></i> Dashboard
            </a>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="row g-4 animate-fade-up">
            <div class="col-12 col-xl-4">
                <div class="glass-panel p-4 text-center h-100 d-flex flex-column align-items-center justify-content-center">
                    <div class="rounded-circle bg-gradient-primary text-white d-flex align-items-center justify-content-center shadow-lg mb-4" style="width: 120px; height: 120px; font-size: 3.5rem; border: 4px solid white;">
                        <%= username != null ? username.substring(0, 1).toUpperCase() : "U" %>
                    </div>
                    <h3 class="fw-bold mb-1"><%= fullname != null ? fullname : "User" %></h3>
                    <p class="text-muted mb-4">@<%= username %></p>
                    
                    <div class="bg-light w-100 p-4 rounded-4 mb-3 border shadow-sm">
                        <span class="d-block text-muted small fw-semibold mb-1">AVAILABLE BALANCE</span>
                        <h3 class="text-success fw-bold mb-0" id="balance">₹ <%= String.format("%.2f", balance) %></h3>
                    </div>
                </div>
            </div>
            
            <div class="col-12 col-xl-8">
                <div class="glass-panel p-5 h-100">
                    <h5 class="fw-bold mb-4 d-flex align-items-center">
                        <i class="bi bi-person-lines-fill text-primary me-3 fs-4"></i> Personal Information
                    </h5>
                    
                    <div class="row g-4 mb-5">
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">FULL NAME</label>
                            <div class="fs-5 fw-medium text-dark border-bottom pb-2 mt-1"><%= fullname != null ? fullname : "-" %></div>
                        </div>
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">EMAIL ADDRESS</label>
                            <div class="fs-5 fw-medium text-dark border-bottom pb-2 mt-1"><%= email != null ? email : "-" %></div>
                        </div>
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">MOBILE NUMBER</label>
                            <div class="fs-5 fw-medium text-dark border-bottom pb-2 mt-1"><%= mobile != null ? mobile : "-" %></div>
                        </div>
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">ACCOUNT CODE</label>
                            <div class="fs-5 fw-medium text-dark border-bottom pb-2 mt-1"><%= accountCode != null ? accountCode : "-" %></div>
                        </div>
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">JOINED DATE</label>
                            <div class="fs-5 fw-medium text-dark border-bottom pb-2 mt-1"><%= signupDate != null ? signupDate : "-" %></div>
                        </div>
                        <div class="col-md-6">
                            <label class="text-muted small fw-semibold">STATUS</label>
                            <div class="mt-1 pb-2">
                                <span class="badge bg-success bg-opacity-10 text-success rounded-pill px-3 py-2 border border-success border-opacity-25"><i class="bi bi-check-circle me-1"></i> Active</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="d-flex gap-3 mt-4 pt-3 border-top">
                        <a href="updateProfile.jsp" class="btn btn-primary px-4 py-2 rounded-pill fw-semibold shadow-sm">
                            <i class="bi bi-pencil-square me-2"></i> Update Profile
                        </a>
                        <a href="logout.jsp" class="btn btn-outline-danger px-4 py-2 rounded-pill fw-semibold ms-auto bg-white">
                            <i class="bi bi-box-arrow-right me-2"></i> Logout
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
let lastBalance = parseFloat(document.getElementById('balance').textContent.replace('₹', '').replace(',', '')) || 0;

async function fetchBalance() {
    try {
        const response = await fetch('getBalance');
        const data = await response.json();
        const balanceEl = document.getElementById('balance');
        const newBalance = parseFloat(data.balance) || 0;
        if (newBalance !== lastBalance) {
            balanceEl.style.color = newBalance > lastBalance ? '#10b981' : '#ef4444';
            setTimeout(()=>{ balanceEl.style.color = ''; }, 1500);
            lastBalance = newBalance;
        }
        balanceEl.textContent = '₹ ' + newBalance.toFixed(2);
    } catch(err) { }
}
setInterval(fetchBalance, 5000);
</script>

<jsp:include page="WEB-INF/components/footer.jsp" />
