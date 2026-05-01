<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Transfer - SkyBanking");
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content d-flex align-items-center justify-content-center">
        <div class="w-100" style="max-width: 550px;">
            <div class="glass-panel p-5 animate-fade-up">
                <div class="text-center mb-4">
                    <div class="d-inline-flex bg-primary bg-opacity-10 text-primary rounded-circle p-3 mb-3 shadow-sm">
                        <i class="bi bi-send fs-1"></i>
                    </div>
                    <h3 class="fw-bold text-dark">Transfer Money</h3>
                    <% if (request.getParameter("sig") != null) { %>
                        <span class="badge bg-success mt-2"><i class="bi bi-qr-code-scan me-1"></i> QR Payment</span>
                    <% } %>
                    <p class="text-muted small">Instantly send funds to another SkyBanking user.</p>
                </div>

                <jsp:include page="WEB-INF/components/alerts.jsp" />

                <form action="transfer" method="post" class="mt-4">
                    <% if (request.getParameter("ts") != null) { %>
                        <input type="hidden" name="ts" value="<%= request.getParameter("ts") %>">
                    <% } %>
                    <% if (request.getParameter("sig") != null) { %>
                        <input type="hidden" name="sig" value="<%= request.getParameter("sig") %>">
                    <% } %>

                    <div class="mb-4">
                        <label for="receiver_account" class="form-label fw-semibold text-secondary small ms-1">RECEIVER ACCOUNT ID</label>
                        <div class="position-relative">
                            <i class="bi bi-person-badge position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                            <input type="number" class="form-control ps-5 form-control-lg" id="receiver_account" name="receiver_account" 
                                   value="<%= request.getParameter("receiver_account") != null ? request.getParameter("receiver_account") : "" %>"
                                   <%= request.getParameter("receiver_account") != null ? "readonly" : "required" %> placeholder="Enter Account ID">
                        </div>
                    </div>

                    <div class="mb-5">
                        <label for="amount" class="form-label fw-semibold text-secondary small ms-1">AMOUNT TO TRANSFER</label>
                        <div class="position-relative">
                            <i class="bi bi-currency-rupee position-absolute top-50 start-0 translate-middle-y ms-3 fs-5 text-muted"></i>
                            <input type="number" class="form-control ps-5 form-control-lg fw-bold fs-4 text-primary" id="amount" name="amount" step="0.01" min="0.01" 
                                   value="<%= request.getParameter("amount") != null ? request.getParameter("amount") : "" %>"
                                   <%= request.getParameter("sig") != null ? "readonly" : "required" %> placeholder="0.00">
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary btn-lg w-100 py-3 fw-bold shadow-sm d-flex justify-content-center align-items-center">
                        Send Money <i class="bi bi-send-fill ms-2 fs-5"></i>
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
