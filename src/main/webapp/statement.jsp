<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h3 mb-0 text-gray-800 fw-bold">Account Statement</h2>
                <p class="text-muted mb-0"><%= request.getAttribute("statementType") != null ? request.getAttribute("statementType") : "Mini Statement" %></p>
            </div>
            <div class="d-flex gap-2 mt-3 mt-md-0">
                <a href="${pageContext.request.contextPath}/statement?action=mini&format=pdf" class="btn btn-primary rounded-pill px-4 fw-semibold shadow-sm d-flex align-items-center">
                    <i class="bi bi-file-earmark-pdf fs-5 me-2"></i> Download PDF
                </a>
                <a href="dashboard.jsp" class="btn btn-outline-secondary rounded-pill px-4 fw-semibold bg-white d-flex align-items-center">
                    Dashboard
                </a>
            </div>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="row g-4 animate-fade-up">
            <!-- Account Details -->
            <% if (request.getAttribute("user") != null && request.getAttribute("account") != null) { %>
            <div class="col-md-6">
                <div class="glass-panel p-4 h-100">
                    <h6 class="text-uppercase text-muted fw-bold small mb-4 d-flex align-items-center">
                        <i class="bi bi-person-badge text-primary me-2"></i> Account Holder
                    </h6>
                    <div class="mb-3 d-flex align-items-center bg-light p-3 rounded-3">
                        <div class="bg-white rounded-circle p-2 me-3 shadow-sm text-primary">
                            <i class="bi bi-person fs-5"></i>
                        </div>
                        <div>
                            <span class="d-block text-muted small">Name</span>
                            <span class="text-dark fw-bold">${user.fullname}</span>
                        </div>
                    </div>
                    <div class="mb-3 d-flex align-items-center bg-light p-3 rounded-3">
                        <div class="bg-white rounded-circle p-2 me-3 shadow-sm text-info">
                            <i class="bi bi-envelope fs-5"></i>
                        </div>
                        <div>
                            <span class="d-block text-muted small">Email</span>
                            <span class="text-dark fw-bold">${user.email}</span>
                        </div>
                    </div>
                    <div class="d-flex align-items-center bg-light p-3 rounded-3">
                        <div class="bg-white rounded-circle p-2 me-3 shadow-sm text-success">
                            <i class="bi bi-telephone fs-5"></i>
                        </div>
                        <div>
                            <span class="d-block text-muted small">Phone</span>
                            <span class="text-dark fw-bold">${user.phone}</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-6">
                <div class="glass-panel p-4 h-100 position-relative overflow-hidden">
                    <div class="position-absolute top-0 end-0 p-4 opacity-10">
                        <i class="bi bi-bank" style="font-size: 8rem;"></i>
                    </div>
                    <h6 class="text-uppercase text-muted fw-bold small mb-4 d-flex align-items-center position-relative z-1">
                        <i class="bi bi-info-circle text-info me-2"></i> Account Info
                    </h6>
                    <div class="mb-3 d-flex justify-content-between align-items-center border-bottom pb-3 position-relative z-1">
                        <span class="text-muted"><i class="bi bi-hash me-2"></i>Account Number</span>
                        <span class="text-dark fw-bold">${account.accountNumber}</span>
                    </div>
                    <div class="mb-3 d-flex justify-content-between align-items-center border-bottom pb-3 position-relative z-1">
                        <span class="text-muted"><i class="bi bi-diagram-3 me-2"></i>Account Type</span>
                        <span class="text-dark fw-bold">${account.accountType}</span>
                    </div>
                    <div class="mt-4 pt-2 position-relative z-1 bg-light p-3 rounded-4 border">
                        <span class="d-block text-muted small fw-semibold mb-1">CURRENT BALANCE</span>
                        <h3 class="text-success fw-bold mb-0">₹${account.balance}</h3>
                    </div>
                </div>
            </div>
            <% } %>

            <!-- Filter Form -->
            <div class="col-12">
                <div class="glass-panel p-4">
                    <h6 class="text-dark fw-bold mb-3 d-flex align-items-center">
                        <i class="bi bi-funnel text-primary me-2"></i> Filter Transactions
                    </h6>
                    <form action="${pageContext.request.contextPath}/statement" method="get">
                        <div class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-muted small fw-semibold ms-1">STATEMENT TYPE</label>
                                <select name="action" class="form-select form-select-lg">
                                    <option value="mini">Mini Statement</option>
                                    <option value="full">Full Statement</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-muted small fw-semibold ms-1">FROM DATE</label>
                                <input type="date" name="startDate" class="form-control form-control-lg">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-muted small fw-semibold ms-1">TO DATE</label>
                                <input type="date" name="endDate" class="form-control form-control-lg">
                            </div>
                            <div class="col-md-3">
                                <button type="submit" class="btn btn-primary btn-lg w-100 fw-bold shadow-sm d-flex align-items-center justify-content-center">
                                    Generate <i class="bi bi-arrow-right-circle ms-2"></i>
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Results Table -->
            <div class="col-12 mb-5">
                <div class="glass-panel p-0 overflow-hidden shadow-sm">
                    <div class="table-responsive">
                        <table class="premium-table mb-0">
                            <thead>
                                <tr>
                                    <th class="ps-4">DATE</th>
                                    <th>TYPE</th>
                                    <th class="text-end">AMOUNT</th>
                                    <th class="text-end">TAX</th>
                                    <th class="text-end">TOTAL</th>
                                    <th>DESCRIPTION</th>
                                    <th class="text-center">STATUS</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% List<com.skybanking.model.Transaction> transactions = (List<com.skybanking.model.Transaction>) request.getAttribute("transactions"); %>
                                <% if (transactions != null && !transactions.isEmpty()) { %>
                                    <% for (com.skybanking.model.Transaction txn : transactions) { %>
                                        <tr>
                                            <td class="ps-4 fw-medium text-secondary"><%= txn.getDate() %></td>
                                            <td>
                                                <span class="badge rounded-pill px-3 py-2 <%= "DEPOSIT".equals(txn.getType()) ? "bg-success bg-opacity-10 text-success border border-success border-opacity-25" : "WITHDRAWAL".equals(txn.getType()) ? "bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25" : "bg-info bg-opacity-10 text-info border border-info border-opacity-25" %>">
                                                    <%= txn.getType() %>
                                                </span>
                                            </td>
                                            <td class="text-end fw-bold text-dark fs-6">₹<%= txn.getAmount() %></td>
                                            <td class="text-end text-muted small">₹<%= txn.getTaxAmount() != null ? txn.getTaxAmount() : new java.math.BigDecimal("0.00") %></td>
                                            <td class="text-end fw-bold text-primary fs-6">₹<%= txn.getTotalAmount() %></td>
                                            <td class="text-muted small">
                                                <div class="text-truncate" style="max-width: 200px;"><%= txn.getDescription() != null ? txn.getDescription() : "-" %></div>
                                                <div class="text-secondary opacity-75" style="font-size:0.7rem;">REF: <%= txn.getReferenceNumber() %></div>
                                            </td>
                                            <td class="text-center">
                                                <i class="bi <%= "COMPLETED".equals(txn.getStatus()) ? "bi-check-circle-fill text-success" : "bi-arrow-repeat text-warning spin-animation" %> fs-5" title="<%= txn.getStatus() %>"></i>
                                            </td>
                                        </tr>
                                    <% } %>
                                <% } else { %>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted py-5">
                                            <div class="d-inline-flex bg-light rounded-circle p-4 mb-3">
                                                <i class="bi bi-inbox fs-1 text-muted opacity-50"></i>
                                            </div>
                                            <h6 class="fw-bold text-dark">No transaction records found</h6>
                                            <p class="small text-secondary mb-0">Adjust your date filters and try again.</p>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% if (transactions != null && !transactions.isEmpty()) { %>
                    <div class="bg-light p-3 text-end text-muted small border-top">
                        <i class="bi bi-clock-history me-1"></i> Generated on: <%= java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) %>
                    </div>
                    <% } %>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
