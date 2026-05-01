<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Apply for Loan - SkyBanking");
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content d-flex align-items-center justify-content-center">
        <div class="w-100" style="max-width: 700px;">
            <div class="glass-panel p-5 animate-fade-up">
                <div class="text-center mb-5">
                    <div class="d-inline-flex bg-primary bg-opacity-10 text-primary rounded-circle p-4 mb-3 shadow-sm">
                        <i class="bi bi-bank fs-1"></i>
                    </div>
                    <h3 class="fw-bold text-dark">Apply for a Loan</h3>
                    <p class="text-muted">Get fast approval and competitive interest rates.</p>
                </div>

                <jsp:include page="WEB-INF/components/alerts.jsp" />

                <form action="loan/apply" method="post">
                    <div class="row g-4 mb-4">
                        <div class="col-md-6">
                            <label class="form-label fw-semibold text-secondary small ms-1">PRINCIPAL AMOUNT (₹)</label>
                            <div class="position-relative">
                                <i class="bi bi-cash position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                <input id="principal" name="principal" type="number" step="0.01" class="form-control ps-5 form-control-lg fw-bold" oninput="calcEMI()" placeholder="Enter amount" required autofocus>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold text-secondary small ms-1">INTEREST RATE (% p.a.)</label>
                            <div class="position-relative">
                                <i class="bi bi-percent position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                <input id="rate" name="rate" type="number" step="0.01" class="form-control ps-5 form-control-lg fw-bold" oninput="calcEMI()" placeholder="e.g. 5.5" required>
                            </div>
                        </div>
                        <div class="col-md-12">
                            <label class="form-label fw-semibold text-secondary small ms-1">TENURE (MONTHS)</label>
                            <div class="position-relative">
                                <i class="bi bi-calendar3 position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                                <input id="tenure" name="tenure" type="number" class="form-control ps-5 form-control-lg fw-bold" oninput="calcEMI()" placeholder="e.g. 24" required>
                            </div>
                        </div>
                    </div>

                    <div class="bg-light p-4 rounded-4 text-center my-4 border shadow-sm">
                        <h6 class="text-muted fw-semibold mb-1">ESTIMATED EMI</h6>
                        <h2 class="text-primary fw-bold mb-0" id="emi">₹0.00</h2>
                    </div>
                    
                    <div class="d-flex flex-column flex-md-row gap-3 justify-content-center align-items-center mt-4 pt-2">
                        <button type="submit" class="btn btn-primary btn-lg px-5 py-3 fw-bold shadow-sm w-100 w-md-auto d-flex justify-content-center align-items-center">
                            Submit Application <i class="bi bi-check2-circle ms-2"></i>
                        </button>
                        <a href="loan/status" class="btn btn-outline-primary btn-lg px-4 py-3 fw-bold w-100 w-md-auto bg-white">View Status</a>
                    </div>
                </form>
            </div>
        </div>
    </main>
</div>

<script>
    function calcEMI() {
        const p = parseFloat(document.getElementById('principal').value || '0');
        const r = parseFloat(document.getElementById('rate').value || '0') / 1200;
        const n = parseInt(document.getElementById('tenure').value || '0');
        if (!p || !r || !n) { document.getElementById('emi').innerText = '₹0.00'; return; }
        const emi = p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        document.getElementById('emi').innerText = '₹' + emi.toFixed(2);
    }
</script>

<jsp:include page="WEB-INF/components/footer.jsp" />
