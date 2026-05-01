<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.sql.*, java.math.BigDecimal, com.skybanking.DBConnection" %>

<%
    // Session Validation
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String username = (String) session.getAttribute("username");
    int userId = (Integer) session.getAttribute("user_id");
    BigDecimal currentBalance = BigDecimal.ZERO;

    // Fetch balance
    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT balance FROM accounts WHERE user_id=? AND is_active=true")) {
        ps.setInt(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                currentBalance = rs.getBigDecimal("balance");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    request.setAttribute("pageTitle", "Dashboard - SkyBanking");
%>

<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content">
        <!-- Header Section -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1 class="h3 mb-0 text-gray-800 fw-bold">Dashboard</h1>
                <p class="text-muted mb-0">Welcome back, <%= username %> 👋</p>
            </div>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="row g-4 animate-fade-up">
            <!-- Balance Card -->
            <div class="col-12 col-xl-8">
                <div class="balance-card">
                    <div class="d-flex justify-content-between align-items-start mb-4">
                        <div>
                            <h5 class="text-white-50 mb-1 fw-normal">Total Balance</h5>
                            <h1 class="display-4 fw-bold mb-0" id="current-balance">
                                ₹ <%= currentBalance != null ? currentBalance : 0 %>
                            </h1>
                        </div>
                        <div class="bg-white bg-opacity-25 rounded-circle p-3 d-flex align-items-center justify-content-center shadow-sm">
                            <i class="bi bi-wallet2 text-white fs-2"></i>
                        </div>
                    </div>
                    
                    <div class="d-flex gap-3 mt-5">
                        <a href="deposit.jsp" class="btn btn-light text-primary px-4 py-2 fw-bold rounded-pill shadow-sm d-flex align-items-center hover-scale">
                            <i class="bi bi-arrow-down-circle fs-5 me-2"></i> Deposit
                        </a>
                        <a href="withdraw.jsp" class="btn text-white border border-white px-4 py-2 fw-bold rounded-pill d-flex align-items-center transition-all" style="background: rgba(255,255,255,0.1)">
                            <i class="bi bi-arrow-up-circle fs-5 me-2"></i> Withdraw
                        </a>
                    </div>
                </div>
            </div>

            <!-- Quick Actions -->
            <div class="col-12 col-xl-4">
                <div class="row g-3 h-100">
                    <div class="col-6">
                        <a href="transfer.jsp" class="text-decoration-none">
                            <div class="action-card text-center h-100 d-flex flex-column align-items-center justify-content-center">
                                <div class="action-icon bg-primary bg-opacity-10 text-primary">
                                    <i class="bi bi-send"></i>
                                </div>
                                <h6 class="text-dark fw-bold mb-0">Transfer</h6>
                            </div>
                        </a>
                    </div>
                    <div class="col-6">
                        <a href="transactions" class="text-decoration-none">
                            <div class="action-card text-center h-100 d-flex flex-column align-items-center justify-content-center">
                                <div class="action-icon bg-success bg-opacity-10 text-success">
                                    <i class="bi bi-clock-history"></i>
                                </div>
                                <h6 class="text-dark fw-bold mb-0">History</h6>
                            </div>
                        </a>
                    </div>
                    <div class="col-6">
                        <a href="statement?action=mini" class="text-decoration-none">
                            <div class="action-card text-center h-100 d-flex flex-column align-items-center justify-content-center">
                                <div class="action-icon bg-info bg-opacity-10 text-info">
                                    <i class="bi bi-file-earmark-text"></i>
                                </div>
                                <h6 class="text-dark fw-bold mb-0">Statement</h6>
                            </div>
                        </a>
                    </div>
                    <div class="col-6">
                        <a href="loan" class="text-decoration-none">
                            <div class="action-card text-center h-100 d-flex flex-column align-items-center justify-content-center">
                                <div class="action-icon bg-warning bg-opacity-10 text-warning">
                                    <i class="bi bi-bank"></i>
                                </div>
                                <h6 class="text-dark fw-bold mb-0">Loans</h6>
                            </div>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <div class="mt-5 text-center px-3 py-4 glass-panel bg-white animate-fade-up border-0" style="animation-delay: 0.1s;">
            <p class="mb-3 text-muted">Need to manage your account settings?</p>
            <div class="d-flex justify-content-center gap-3">
                <a href="userinfo" class="btn btn-outline-primary px-4 rounded-pill fw-semibold">View Profile</a>
                <form action="deleteAccount" method="post" onsubmit="return confirm('Are you sure you want to delete your account? This action cannot be undone.');" class="d-inline">
                    <button type="submit" class="btn btn-outline-danger px-4 rounded-pill fw-semibold">Delete Account</button>
                </form>
            </div>
        </div>
    </main>
</div>

<script>
  async function refreshBalance(){
    try {
      const res = await fetch('getBalance');
      if (!res.ok) return;
      const data = await res.json();
      const el = document.querySelector('#current-balance');
      if (el && data.balance !== undefined){
        el.textContent = '₹ ' + parseFloat(data.balance).toFixed(2);
      }
    } catch(e) { }
  }
  setInterval(refreshBalance, 5000);
</script>

<jsp:include page="WEB-INF/components/footer.jsp" />
