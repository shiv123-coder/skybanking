<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<% request.setAttribute("pageTitle", "Add Money - SkyBanking"); %>
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
                <h1 class="h2 fw-bold text-dark"><i class="bi bi-wallet2 text-primary me-2"></i>Add Money</h1>
            </div>

            <% if (request.getParameter("error") != null) { %>
                <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i> <%= request.getParameter("error") %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <% } %>

            <div class="row justify-content-center">
                <div class="col-lg-6 col-md-8">
                    <div class="card border-0 shadow-lg rounded-4 overflow-hidden glass-panel">
                        <div class="card-header bg-gradient-primary text-white p-4 border-0 text-center relative overflow-hidden">
                            <div class="position-absolute top-0 end-0 opacity-25 p-3">
                                <i class="bi bi-stripe fs-1"></i>
                            </div>
                            <h4 class="mb-0 fw-bold relative z-1"><i class="bi bi-credit-card me-2"></i>Top-up Wallet</h4>
                            <p class="mb-0 mt-2 small opacity-75 relative z-1">Secure payment via Stripe</p>
                        </div>
                        <div class="card-body p-5">
                            <form action="addmoney/checkout" method="POST" class="needs-validation" novalidate id="addMoneyForm">
                                <input type="hidden" name="csrf_token" value="<%= request.getAttribute("csrf_token") != null ? request.getAttribute("csrf_token") : "" %>">
                                
                                <div class="mb-4 text-center">
                                    <label for="amount" class="form-label text-muted fw-semibold mb-3">Enter Amount to Add</label>
                                    <div class="input-group input-group-lg shadow-sm rounded-3 overflow-hidden border">
                                        <span class="input-group-text bg-light border-0 text-dark fw-bold fs-4 pe-2">₹</span>
                                        <input type="number" class="form-control border-0 fs-3 fw-bold text-center" id="amount" name="amount" min="10" step="1" required placeholder="0.00">
                                    </div>
                                    <div class="invalid-feedback text-start mt-2">
                                        Please enter a valid amount (Minimum ₹10).
                                    </div>
                                </div>
                                
                                <!-- Quick Amount Selection -->
                                <div class="d-flex justify-content-center gap-2 mb-5 flex-wrap">
                                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4 qty-btn" data-val="100">+ ₹100</button>
                                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4 qty-btn" data-val="500">+ ₹500</button>
                                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4 qty-btn" data-val="1000">+ ₹1,000</button>
                                    <button type="button" class="btn btn-outline-secondary rounded-pill px-4 qty-btn" data-val="5000">+ ₹5,000</button>
                                </div>

                                <div class="d-grid mt-4">
                                    <button type="submit" class="btn btn-primary btn-lg rounded-pill shadow hover-scale fw-bold d-flex align-items-center justify-content-center py-3">
                                        <i class="bi bi-lock-fill me-2 fs-5"></i> Proceed to Pay Securely
                                    </button>
                                </div>
                            </form>
                            
                            <div class="text-center mt-4">
                                <p class="text-muted small mb-0"><i class="bi bi-shield-check text-success me-1"></i> Payments are 100% secure and encrypted.</p>
                                <div class="d-flex justify-content-center gap-3 mt-2 fs-4 text-muted opacity-50">
                                    <i class="bi bi-credit-card-2-front"></i>
                                    <i class="bi bi-bank"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
    // Form validation
    (() => {
        'use strict'
        const forms = document.querySelectorAll('.needs-validation')
        Array.from(forms).forEach(form => {
            form.addEventListener('submit', event => {
                const amountInput = document.getElementById('amount');
                if (!form.checkValidity() || amountInput.value < 10) {
                    event.preventDefault()
                    event.stopPropagation()
                }
                form.classList.add('was-validated')
            }, false)
        })

        // Quick amount buttons logic
        const buttons = document.querySelectorAll('.qty-btn');
        const amountInput = document.getElementById('amount');
        buttons.forEach(btn => {
            btn.addEventListener('click', () => {
                let currentVal = parseInt(amountInput.value || 0);
                let toAdd = parseInt(btn.getAttribute('data-val'));
                amountInput.value = currentVal + toAdd;
            });
        });
    })()
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
