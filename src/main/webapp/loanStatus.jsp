<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h3 mb-0 text-gray-800 fw-bold">My Loans</h2>
                <p class="text-muted mb-0">Track your active and past loan applications</p>
            </div>
            <div class="d-flex gap-2 mt-3 mt-md-0">
                <a href="<%= request.getContextPath() %>/loan" class="btn btn-primary rounded-pill px-4 fw-semibold shadow-sm text-white d-flex align-items-center">
                    <i class="bi bi-plus-circle me-2 fs-5"></i> Apply New
                </a>
                <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-secondary rounded-pill px-4 fw-semibold bg-white">
                    Dashboard
                </a>
            </div>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="row g-4">
            <%
                java.util.List<java.util.Map<String, Object>> loans =
                        (java.util.List<java.util.Map<String, Object>>) request.getAttribute("loans");
                        
                if (loans != null && !loans.isEmpty()) {
                    for (java.util.Map<String, Object> l : loans) {
                        String status = (String) l.get("status");
                        String adminReason = (String) l.get("admin_reason");
                        
                        String badgeClass = "bg-info text-dark";
                        String iconClass = "bi-hourglass-split";
                        int progress = 25;
                        String progressColor = "bg-info";

                        if ("APPROVED".equalsIgnoreCase(status)) { 
                            badgeClass = "bg-success"; iconClass="bi-check-circle-fill"; progress = 75; progressColor = "bg-success";
                        } else if ("REJECTED".equalsIgnoreCase(status)) { 
                            badgeClass = "bg-danger"; iconClass="bi-x-circle-fill"; progress = 100; progressColor = "bg-danger";
                        } else if ("DISBURSED".equalsIgnoreCase(status)) { 
                            badgeClass = "bg-primary"; iconClass="bi-cash-coin"; progress = 100; progressColor = "bg-primary";
                        } else if ("CLOSED".equalsIgnoreCase(status)) { 
                            badgeClass = "bg-secondary"; iconClass="bi-check-all"; progress = 100; progressColor = "bg-secondary";
                        }
            %>
            <div class="col-12 col-xl-6">
                <div class="card shadow-sm border-0 rounded-4 overflow-hidden h-100 transition-all hover-scale" style="background: var(--glass-bg); backdrop-filter: blur(10px);">
                    <div class="card-header border-0 bg-transparent pt-4 pb-0 px-4 d-flex justify-content-between align-items-center">
                        <h5 class="fw-bold mb-0 text-dark d-flex align-items-center">
                            <i class="bi bi-bank me-2 text-primary"></i> Loan #<%= l.get("loan_id") %>
                        </h5>
                        <span class="badge rounded-pill px-3 py-2 <%= badgeClass %> bg-opacity-25 border border-opacity-50 fs-6">
                            <i class="bi <%= iconClass %> me-1"></i> <%= status %>
                        </span>
                    </div>
                    <div class="card-body px-4 pt-3 pb-4">
                        <div class="row mb-4">
                            <div class="col-6 mb-3">
                                <p class="text-muted small mb-1 fw-semibold text-uppercase">Principal Amount</p>
                                <h4 class="text-dark fw-bold mb-0">₹<%= l.get("principal") %></h4>
                            </div>
                            <div class="col-6 mb-3 text-end">
                                <p class="text-muted small mb-1 fw-semibold text-uppercase">Monthly EMI</p>
                                <h4 class="text-primary fw-bold mb-0">₹<%= l.get("emi") %></h4>
                            </div>
                            <div class="col-6">
                                <p class="text-muted small mb-1 fw-semibold text-uppercase">Interest Rate</p>
                                <p class="text-dark fw-semibold mb-0"><i class="bi bi-percent me-1"></i><%= l.get("interest_rate") %> p.a.</p>
                            </div>
                            <div class="col-6 text-end">
                                <p class="text-muted small mb-1 fw-semibold text-uppercase">Tenure</p>
                                <p class="text-dark fw-semibold mb-0"><i class="bi bi-calendar3 me-1"></i><%= l.get("tenure_months") %> Months</p>
                            </div>
                        </div>

                        <!-- Progress Bar -->
                        <div class="position-relative mb-4">
                            <div class="progress" style="height: 8px; border-radius: 4px;">
                                <div class="progress-bar <%= progressColor %> progress-bar-striped <%= progress < 100 && !status.equalsIgnoreCase("REJECTED") ? "progress-bar-animated" : "" %>" role="progressbar" style="width: <%= progress %>%;" aria-valuenow="<%= progress %>" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                            <div class="d-flex justify-content-between mt-2">
                                <small class="text-muted fw-semibold">Applied</small>
                                <small class="text-muted fw-semibold <%= progress >= 75 && !status.equalsIgnoreCase("REJECTED") ? "text-dark" : "" %>">Approved</small>
                                <small class="text-muted fw-semibold <%= progress == 100 && status.equalsIgnoreCase("DISBURSED") ? "text-primary" : "" %>">Disbursed</small>
                            </div>
                        </div>

                        <% if (adminReason != null && !adminReason.trim().isEmpty()) { %>
                        <div class="alert alert-<%= status.equalsIgnoreCase("REJECTED") ? "danger" : "info" %> bg-opacity-10 border-0 rounded-3 mb-0">
                            <div class="d-flex">
                                <i class="bi <%= status.equalsIgnoreCase("REJECTED") ? "bi-exclamation-triangle" : "bi-info-circle" %> fs-5 me-3 mt-1"></i>
                                <div>
                                    <h6 class="alert-heading fw-bold mb-1">Bank Remarks</h6>
                                    <p class="mb-0 small"><%= adminReason %></p>
                                </div>
                            </div>
                        </div>
                        <% } else if (status.equalsIgnoreCase("PENDING")) { %>
                        <div class="alert alert-secondary bg-opacity-10 border-0 rounded-3 mb-0 text-muted small">
                            <i class="bi bi-clock-history me-2"></i> Your application is currently under review by our officers.
                        </div>
                        <% } %>
                    </div>
                    <div class="card-footer bg-light bg-opacity-50 border-0 px-4 py-3 text-muted small d-flex justify-content-between">
                        <span><i class="bi bi-calendar-check me-1"></i> Applied on: <%= l.get("created_at") %></span>
                    </div>
                </div>
            </div>
            <%
                    }
                } else {
            %>
            <div class="col-12">
                <div class="text-center bg-white p-5 rounded-4 shadow-sm border mt-3">
                    <div class="d-inline-flex bg-light rounded-circle p-4 mb-4">
                        <i class="bi bi-bank fs-1 text-primary opacity-75"></i>
                    </div>
                    <h4 class="fw-bold text-dark">No Active Loans Found</h4>
                    <p class="text-secondary mb-4 w-50 mx-auto">You currently don't have any active or past loan applications with SkyBank. Apply now for personal, home, or vehicle loans at competitive interest rates.</p>
                    <a href="<%= request.getContextPath() %>/loan" class="btn btn-primary btn-lg rounded-pill px-5 shadow-sm">
                        <i class="bi bi-plus-circle me-2"></i>Apply for a Loan Now
                    </a>
                </div>
            </div>
            <%
                }
            %>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
