<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Deposit - SkyBanking");
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content d-flex align-items-center justify-content-center">
        <div class="w-100" style="max-width: 500px;">
            <div class="glass-panel p-5 animate-fade-up">
                <div class="text-center mb-4">
                    <div class="d-inline-flex bg-success bg-opacity-10 text-success rounded-circle p-3 mb-3 shadow-sm">
                        <i class="bi bi-arrow-down-circle fs-1"></i>
                    </div>
                    <h3 class="fw-bold text-dark">Deposit Funds</h3>
                    <p class="text-muted small">Add money to your SkyBanking account securely.</p>
                </div>

                <jsp:include page="WEB-INF/components/alerts.jsp" />

                <form action="deposit" method="post" class="mt-4">
                    <div class="mb-4">
                        <label for="amount" class="form-label fw-semibold text-secondary small ms-1">AMOUNT TO DEPOSIT</label>
                        <div class="position-relative">
                            <i class="bi bi-currency-rupee position-absolute top-50 start-0 translate-middle-y ms-3 fs-5 text-muted"></i>
                            <input type="number" class="form-control ps-5 form-control-lg fw-bold fs-3 text-success" id="amount" name="amount" step="0.01" min="0.01" placeholder="0.00" required autofocus>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-success btn-lg w-100 py-3 mt-2 fw-bold shadow-sm d-flex justify-content-center align-items-center">
                        Confirm Deposit <i class="bi bi-check2-circle ms-2 fs-5"></i>
                    </button>
                    <div class="text-center mt-4">
                        <a href="dashboard.jsp" class="text-decoration-none text-muted hover-scale d-inline-block transition-all"><i class="bi bi-arrow-left me-1"></i> Return to Dashboard</a>
                    </div>
                </form>
            </div>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
