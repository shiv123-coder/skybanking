<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Transaction History - SkyBanking");
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="app-layout">
    <jsp:include page="WEB-INF/components/sidebar.jsp" />

    <main class="main-content">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="h3 mb-0 text-gray-800 fw-bold">Transaction History</h2>
                <p class="text-muted mb-0">View all your recent deposits, withdrawals, and transfers.</p>
            </div>
            <a href="dashboard.jsp" class="btn btn-outline-primary d-flex align-items-center px-4 rounded-pill fw-semibold bg-white">
                <i class="bi bi-arrow-left me-2"></i> Dashboard
            </a>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="glass-panel p-4 animate-fade-up">
            <div class="table-responsive px-2 pb-2">
                <table class="table table-hover align-middle mb-0 premium-table" style="border-spacing: 0 12px; border-collapse: separate;">
                    <thead class="text-muted small fw-bold text-uppercase" style="letter-spacing: 0.5px;">
                        <tr>
                            <th class="border-0 ps-4">Date & Time</th>
                            <th class="border-0">Transaction Type</th>
                            <th class="border-0">Amount</th>
                            <th class="border-0">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Map<String, Object>> transactions = (List<Map<String, Object>>) request.getAttribute("transactions");
                            if(transactions != null && !transactions.isEmpty()){
                                for(Map<String, Object> txn : transactions){
                                    String type = (String) txn.get("type");
                                    String counterparty = txn.get("counterparty") != null ? txn.get("counterparty").toString() : "-";

                                    String typeIcon = "";
                                    String typeColor = "";
                                    String amountColor = "text-dark";
                                    String amountPrefix = "";
                                    
                                    if("Deposit".equals(type)) {
                                        typeIcon = "bi-arrow-down-circle-fill"; typeColor = "text-success";
                                        amountColor = "text-success"; amountPrefix = "+ ";
                                    } else if("Withdraw".equals(type)) {
                                        typeIcon = "bi-arrow-up-circle-fill"; typeColor = "text-warning";
                                        amountColor = "text-dark"; amountPrefix = "- ";
                                    } else if("Transfer".equals(type)) {
                                        typeIcon = "bi-send-fill"; typeColor = "text-danger";
                                        amountColor = "text-danger"; amountPrefix = "- ";
                                    } else if("Received".equals(type)) {
                                        typeIcon = "bi-arrow-down-left-circle-fill"; typeColor = "text-info";
                                        amountColor = "text-success"; amountPrefix = "+ ";
                                    }
                        %>
                        <tr class="bg-white shadow-sm rounded-4 transition-all hover-scale-slight" style="transform: scale(1); cursor: pointer;">
                            <td class="ps-4 rounded-start border-0 py-3">
                                <div class="d-flex align-items-center">
                                    <div class="bg-light rounded p-2 me-3 d-flex border">
                                        <i class="bi bi-calendar-event text-secondary fs-6"></i>
                                    </div>
                                    <span class="fw-semibold text-secondary"><%= txn.get("timestamp") %></span>
                                </div>
                            </td>
                            <td class="border-0 py-3">
                                <span class="badge rounded-pill bg-light border px-3 py-2 text-dark d-inline-flex align-items-center shadow-sm">
                                    <i class="bi <%= typeIcon %> <%= typeColor %> me-2 fs-6"></i> <%= type %>
                                </span>
                            </td>
                            <td class="fw-bold <%= amountColor %> fs-5 border-0 py-3"><%= amountPrefix %>₹<%= txn.get("amount") %></td>
                            <td class="fw-medium text-secondary border-0 rounded-end py-3">
                                <div class="d-flex align-items-center">
                                    <% if (!"-".equals(counterparty)) { %>
                                        <div class="bg-light rounded-circle p-1 me-2 border text-center" style="width: 32px; height: 32px;">
                                            <i class="bi bi-person-fill text-muted"></i>
                                        </div>
                                    <% } else { %>
                                        <div class="bg-light rounded-circle p-1 me-2 border text-center" style="width: 32px; height: 32px;">
                                            <i class="bi bi-building text-muted"></i>
                                        </div>
                                    <% } %>
                                    <%= counterparty %>
                                </div>
                            </td>
                        </tr>
                        <%      }
                            } else { %>
                        <tr>
                            <td colspan="4" class="text-center py-5">
                                <div class="d-inline-flex bg-light rounded-circle p-4 mb-3">
                                    <i class="bi bi-inbox fs-1 text-muted opacity-50"></i>
                                </div>
                                <h5 class="text-muted fw-bold">No transactions found</h5>
                                <p class="text-secondary small">Your account activity will appear here.</p>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            
            <!-- Pagination Controls -->
            <% 
                Integer currentPage = (Integer) request.getAttribute("currentPage");
                Boolean hasNextPage = (Boolean) request.getAttribute("hasNextPage");
                if (currentPage != null) { 
            %>
            <div class="d-flex justify-content-between align-items-center mt-4">
                <div class="text-muted small">
                    Page <%= currentPage %>
                </div>
                <div class="btn-group shadow-sm">
                    <% if (currentPage > 1) { %>
                        <a href="transactions?page=<%= currentPage - 1 %>" class="btn btn-outline-secondary bg-white"><i class="bi bi-chevron-left"></i> Previous</a>
                    <% } else { %>
                        <button class="btn btn-outline-secondary bg-light text-muted" disabled><i class="bi bi-chevron-left"></i> Previous</button>
                    <% } %>
                    
                    <% if (hasNextPage != null && hasNextPage) { %>
                        <a href="transactions?page=<%= currentPage + 1 %>" class="btn btn-outline-secondary bg-white">Next <i class="bi bi-chevron-right"></i></a>
                    <% } else { %>
                        <button class="btn btn-outline-secondary bg-light text-muted" disabled>Next <i class="bi bi-chevron-right"></i></button>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
