<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<% request.setAttribute("pageTitle", "QR Payments - SkyBanking"); %>
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
                <h1 class="h2 fw-bold text-dark"><i class="bi bi-qr-code-scan text-primary me-2"></i>QR Payments (Receive)</h1>
            </div>

            <div class="row justify-content-center">
                <!-- Static QR Code -->
                <div class="col-md-6 mb-4">
                    <div class="card border-0 shadow-lg rounded-4 overflow-hidden glass-panel h-100">
                        <div class="card-header bg-gradient-primary text-white p-3 text-center">
                            <h5 class="mb-0 fw-bold"><i class="bi bi-qr-code me-2"></i>My Static QR Code</h5>
                        </div>
                        <div class="card-body p-4 text-center">
                            <p class="text-muted small mb-4">Show this to anyone to receive payments directly to your account.</p>
                            
                            <div class="bg-white p-3 rounded-4 shadow-sm d-inline-block border">
                                <img src="data:image/png;base64,<%= request.getAttribute("staticQrCode") %>" alt="Static QR" class="img-fluid" style="width: 200px; height: 200px;">
                            </div>
                            
                            <h4 class="mt-4 fw-bold text-dark"><%= session.getAttribute("fullname") %></h4>
                            <p class="text-muted mb-0"><i class="bi bi-bank2 me-1"></i> Account: <strong>******<%= String.valueOf(request.getAttribute("accountId")).substring(Math.max(0, String.valueOf(request.getAttribute("accountId")).length() - 4)) %></strong></p>
                        </div>
                    </div>
                </div>

                <!-- Dynamic QR Code -->
                <div class="col-md-6 mb-4">
                    <div class="card border-0 shadow-lg rounded-4 overflow-hidden glass-panel h-100">
                        <div class="card-header bg-dark text-white p-3 text-center">
                            <h5 class="mb-0 fw-bold"><i class="bi bi-receipt me-2"></i>Generate Specific Amount QR</h5>
                        </div>
                        <div class="card-body p-4 text-center d-flex flex-column justify-content-center align-items-center">
                            
                            <% if (request.getAttribute("dynamicQrCode") != null) { %>
                                <p class="text-success small fw-bold mb-3"><i class="bi bi-check-circle-fill me-1"></i>QR Code for ₹<%= request.getAttribute("dynamicAmount") %> generated!</p>
                                <div class="bg-white p-3 rounded-4 shadow-sm d-inline-block border position-relative">
                                    <div class="position-absolute top-0 start-50 translate-middle badge rounded-pill bg-danger" style="margin-top:-5px;">Expires in 15m</div>
                                    <img src="data:image/png;base64,<%= request.getAttribute("dynamicQrCode") %>" alt="Dynamic QR" class="img-fluid" style="width: 180px; height: 180px;">
                                </div>
                                <h3 class="mt-3 fw-bold text-dark">₹<%= request.getAttribute("dynamicAmount") %></h3>
                                <a href="qr" class="btn btn-sm btn-outline-secondary mt-3 rounded-pill px-4">Generate Another</a>
                            <% } else { %>
                                <div class="opacity-50 mb-4">
                                    <i class="bi bi-qr-code-scan" style="font-size: 5rem;"></i>
                                </div>
                                <p class="text-muted small mb-3">Generate a secure QR code for a specific amount. The sender just scans and pays!</p>
                                
                                <form action="qr" method="GET" class="w-100 px-lg-4 needs-validation" novalidate>
                                    <div class="input-group mb-3 shadow-sm rounded-3 overflow-hidden">
                                        <span class="input-group-text bg-light border-0">₹</span>
                                        <input type="number" name="amount" class="form-control border-0" placeholder="Amount (e.g. 500)" required min="1">
                                    </div>
                                    <button class="btn btn-primary w-100 rounded-pill fw-bold hover-scale shadow" type="submit">
                                        <i class="bi bi-magic me-2"></i>Generate
                                    </button>
                                </form>
                            <% } %>
                            
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
