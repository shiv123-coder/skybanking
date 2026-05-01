<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0 text-center" style="max-width: 500px;">
        <div class="mb-4">
            <div class="d-inline-flex bg-success bg-opacity-10 text-success rounded-circle p-4 mb-3 shadow-sm">
                <i class="bi bi-check-circle-fill" style="font-size: 4rem;"></i>
            </div>
            <h2 class="text-dark fw-bold mt-3"><%= request.getAttribute("message") %></h2>
            <p class="text-muted fs-5">Welcome to SkyBanking, <b class="text-primary"><%= request.getAttribute("username") %></b>!</p>
        </div>

        <div class="p-4 rounded-4 bg-light border shadow-sm my-4">
            <p class="text-secondary small mb-1 text-uppercase fw-bold">Your Unique Account Code</p>
            <h1 class="fw-bold text-success m-0" style="letter-spacing: 4px; font-size: 3rem;">#<%= request.getAttribute("accountCode") %></h1>
            <p class="text-muted small mt-3 mb-0 d-flex align-items-center justify-content-center">
                <i class="bi bi-info-circle-fill text-primary me-2"></i> Please keep this code safe.
            </p>
        </div>

        <div class="mt-4">
            <a href="login.jsp" class="btn btn-primary btn-lg w-100 py-3 fw-bold shadow-sm d-flex justify-content-center align-items-center">
                Proceed to Login <i class="bi bi-arrow-right-circle ms-2 fs-5"></i>
            </a>
        </div>
    </div>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
