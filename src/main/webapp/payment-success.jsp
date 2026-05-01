<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<% request.setAttribute("pageTitle", "Payment Successful - SkyBanking"); %>
<%@ include file="WEB-INF/components/header.jsp" %>
<div class="container-fluid page-transition">
    <div class="row">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 d-md-block sidebar-wrapper collapse" id="sidebarMenu">
            <%@ include file="WEB-INF/components/sidebar.jsp" %>
        </div>

        <!-- Main Content -->
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 main-content">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-4 pb-2 mb-4 border-bottom">
                <h1 class="h2 fw-bold text-dark">Transaction Status</h1>
            </div>

            <div class="row justify-content-center mt-5">
                <div class="col-lg-5 col-md-7 text-center">
                    <div class="card border-0 shadow-lg rounded-4 overflow-hidden glass-panel p-5">
                        <div class="mb-4">
                            <div class="d-inline-flex align-items-center justify-content-center bg-success bg-opacity-10 text-success rounded-circle" style="width: 100px; height: 100px;">
                                <i class="bi bi-check-circle-fill" style="font-size: 3rem;"></i>
                            </div>
                        </div>
                        <h2 class="fw-bold text-dark">Payment Successful!</h2>
                        <p class="text-muted mt-3 mb-4">
                            Your wallet has been topped up successfully.
                            The amount has been added to your SkyBanking account.
                        </p>
                        <a href="dashboard" class="btn btn-primary btn-lg rounded-pill px-5 py-3 hover-scale shadow fw-bold w-100">
                            Go to Dashboard
                        </a>
                        <a href="transactions" class="btn btn-link text-decoration-none text-muted mt-3">
                            View Transaction History
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
