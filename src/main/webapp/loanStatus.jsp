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
                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-outline-secondary rounded-pill px-4 fw-semibold bg-white">
                    Dashboard
                </a>
            </div>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="glass-panel p-4 animate-fade-up">
            <div class="table-responsive">
                <table class="premium-table">
                    <thead>
                        <tr>
                            <th class="ps-4">LOAN ID</th>
                            <th class="text-end">PRINCIPAL</th>
                            <th class="text-center">RATE</th>
                            <th class="text-center">TENURE</th>
                            <th class="text-end">MONTHLY EMI</th>
                            <th class="text-center">STATUS</th>
                            <th class="ps-3">APPLIED ON</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            java.util.List<java.util.Map<String, Object>> loans =
                                    (java.util.List<java.util.Map<String, Object>>) request.getAttribute("loans");
                                    
                            if (loans != null && !loans.isEmpty()) {
                                for (java.util.Map<String, Object> l : loans) {
                                    String status = (String) l.get("status");
                                    String badgeClass = "bg-info bg-opacity-10 text-info border border-info border-opacity-25";
                                    String iconClass = "bi-hourglass-split";
                                    
                                    if ("Approved".equalsIgnoreCase(status)) { badgeClass = "bg-success bg-opacity-10 text-success border border-success border-opacity-25"; iconClass="bi-check-circle"; }
                                    else if ("Rejected".equalsIgnoreCase(status)) { badgeClass = "bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25"; iconClass="bi-x-circle"; }
                                    else if ("Disbursed".equalsIgnoreCase(status)) { badgeClass = "bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25"; iconClass="bi-cash-coin"; }
                        %>
                        <tr>
                            <td class="ps-4 fw-bold text-secondary">#<%= l.get("loan_id") %></td>
                            <td class="text-end fw-bold text-dark fs-6">₹<%= l.get("principal") %></td>
                            <td class="text-center text-secondary fw-semibold"><%= l.get("interest_rate") %>%</td>
                            <td class="text-center text-secondary fw-semibold"><%= l.get("tenure_months") %> mo</td>
                            <td class="text-end fw-bold text-primary fs-6">₹<%= l.get("emi") %></td>
                            <td class="text-center">
                                <span class="badge rounded-pill px-3 py-2 <%= badgeClass %>"><i class="bi <%= iconClass %> me-1"></i> <%= status %></span>
                            </td>
                            <td class="ps-3 text-secondary small fw-medium"><i class="bi bi-calendar fw-normal me-1"></i> <%= l.get("created_at") %></td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="7" class="text-center text-muted py-5">
                                <div class="d-inline-flex bg-light rounded-circle p-4 mb-3">
                                    <i class="bi bi-bank fs-1 text-muted opacity-50"></i>
                                </div>
                                <h6 class="fw-bold text-dark">No active loans found</h6>
                                <p class="small text-secondary mb-0">Apply for a new loan to get started.</p>
                                <a href="<%= request.getContextPath() %>/loan" class="btn btn-outline-primary mt-3 rounded-pill px-4">Apply Now</a>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
