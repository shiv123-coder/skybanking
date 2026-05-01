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
            <div class="table-responsive">
                <table class="premium-table">
                    <thead>
                        <tr>
                            <th>Date & Time</th>
                            <th>Transaction Type</th>
                            <th>Amount</th>
                            <th>Details</th>
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
                        <tr>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="bg-light rounded p-2 me-3 d-flex shadow-sm border border-white">
                                        <i class="bi bi-calendar-event text-secondary fs-6"></i>
                                    </div>
                                    <span class="fw-semibold text-secondary"><%= txn.get("timestamp") %></span>
                                </div>
                            </td>
                            <td>
                                <span class="badge rounded-pill bg-white shadow-sm border px-3 py-2 text-dark d-inline-flex align-items-center">
                                    <i class="bi <%= typeIcon %> <%= typeColor %> me-2 fs-6"></i> <%= type %>
                                </span>
                            </td>
                            <td class="fw-bold <%= amountColor %> fs-5"><%= amountPrefix %>₹<%= txn.get("amount") %></td>
                            <td class="fw-medium text-secondary d-flex align-items-center">
                                <% if (!"-".equals(counterparty)) { %>
                                    <i class="bi bi-person-circle me-2 text-muted fs-5"></i>
                                <% } %>
                                <%= counterparty %>
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
        </div>
    </main>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
