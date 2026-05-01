<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="row justify-content-center print-area">
    <div class="col-md-10 col-lg-8">
        <div class="glass-card mb-4 d-print-none">
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center">
                    <div class="bg-primary bg-opacity-10 rounded-circle p-3 me-3 text-center">
                        <i class="bi bi-file-earmark-text text-primary fs-3"></i>
                    </div>
                    <div>
                        <h4 class="fw-bold text-white mb-0">Transaction Invoice</h4>
                        <p class="text-muted small mb-0">Receipt for your transaction</p>
                    </div>
                </div>
                <div class="d-flex gap-2">
                    <a href="transactions" class="btn btn-outline-secondary rounded-pill pe-4 ps-3"><i class="bi bi-arrow-left me-2"></i> Transactions</a>
                </div>
            </div>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <div class="glass-card invoice-card overflow-hidden">
            <!-- Header -->
            <div class="row mb-5 pb-4 border-bottom border-secondary">
                <div class="col-sm-6">
                    <h2 class="text-gradient fw-bold mb-1"><i class="bi bi-bank me-2"></i> SkyBanking</h2>
                    <p class="text-muted small">Digital Transaction Receipt</p>
                </div>
                <div class="col-sm-6 text-sm-end mt-4 mt-sm-0">
                    <h6 class="text-white fw-bold">INVOICE #${transaction.referenceNumber}</h6>
                    <div class="text-muted small mb-1">Date: ${transaction.date}</div>
                    <div class="text-muted small">Generated: <%= java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) %></div>
                </div>
            </div>

            <!-- Entities -->
            <div class="row mb-5 g-4">
                <div class="col-sm-6">
                    <div class="bg-dark bg-opacity-25 rounded-4 p-4 h-100 border border-secondary">
                        <h6 class="text-uppercase text-muted fw-bold small mb-3">Billed To (Customer)</h6>
                        <div class="text-white fw-bold fs-5 mb-1">${user.fullname}</div>
                        <div class="text-muted small mb-1"><i class="bi bi-envelope me-2"></i>${user.email}</div>
                        <div class="text-muted small"><i class="bi bi-telephone me-2"></i>${user.phone}</div>
                    </div>
                </div>
                <div class="col-sm-6">
                    <div class="bg-dark bg-opacity-25 rounded-4 p-4 h-100 border border-secondary">
                        <h6 class="text-uppercase text-muted fw-bold small mb-3">Account Details</h6>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted small">A/C Number:</span>
                            <span class="text-white fw-semibold">${account.accountNumber}</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted small">A/C Type:</span>
                            <span class="text-white fw-semibold">${account.accountType}</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span class="text-muted small">Current Balance:</span>
                            <span class="text-info fw-bold">₹${account.balance}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Transaction Info -->
            <h6 class="text-uppercase text-muted fw-bold small mb-3">Transaction Summary</h6>
            <div class="table-responsive mb-5">
                <table class="table table-bordered border-secondary table-dark bg-transparent">
                    <thead class="bg-dark bg-opacity-50">
                        <tr>
                            <th class="py-3 ps-3 text-muted small">TXN ID</th>
                            <th class="py-3 text-muted small">TYPE</th>
                            <th class="py-3 text-muted small">DESCRIPTION</th>
                            <th class="py-3 text-center text-muted small">STATUS</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="py-3 ps-3 text-white font-monospace">${transaction.txnId}</td>
                            <td class="py-3">
                                <span class="badge ${transaction.type == 'DEPOSIT' ? 'bg-success' : transaction.type == 'WITHDRAWAL' ? 'bg-warning text-dark' : 'bg-info text-dark'}">
                                    ${transaction.type}
                                </span>
                            </td>
                            <td class="py-3 text-muted">${transaction.description != null ? transaction.description : 'Standard Transaction'}
                                <% if (request.getAttribute("transaction") != null) { %>
                                    <% Object txn = request.getAttribute("transaction"); %>
                                    <% try { if (txn.getClass().getMethod("getReceiverAccountId").invoke(txn) != null) { %>
                                        <br><small class="text-info">Transfer To: A/C #${transaction.receiverAccountId}</small>
                                    <% } } catch(Exception e) {} %>
                                <% } %>
                            </td>
                            <td class="py-3 text-center">
                                <span class="badge ${transaction.status == 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}">
                                    ${transaction.status}
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Totals -->
            <div class="row justify-content-end mb-5">
                <div class="col-sm-6 col-md-5">
                    <div class="bg-dark bg-opacity-50 rounded-4 p-4 border border-secondary">
                        <div class="d-flex justify-content-between mb-3 border-bottom border-secondary pb-3">
                            <span class="text-muted">Base Amount:</span>
                            <span class="text-white fw-bold">₹${transaction.amount}</span>
                        </div>
                        
                        <% if (request.getAttribute("transaction") != null) { %>
                            <% Object txn = request.getAttribute("transaction"); %>
                            <% try { Object taxAmt = txn.getClass().getMethod("getTaxAmount").invoke(txn); %>
                                <% if (taxAmt != null && !taxAmt.toString().equals("0.00") && !taxAmt.toString().equals("0.0")) { %>
                                    <div class="d-flex justify-content-between mb-3 border-bottom border-secondary pb-3">
                                        <span class="text-muted">${transaction.taxType} Tax:</span>
                                        <span class="text-warning">₹${transaction.taxAmount}</span>
                                    </div>
                                <% } %>
                            <% } catch(Exception e) {} %>
                        <% } %>
                        
                        <div class="d-flex justify-content-between align-items-center mt-2">
                            <span class="text-uppercase fw-bold text-white">Total Amount:</span>
                            <h3 class="text-gradient fw-bold mb-0">₹${transaction.totalAmount}</h3>
                        </div>
                    </div>
                </div>
            </div>

            <div class="text-center text-muted small border-top border-secondary pt-4 mt-5">
                <p class="mb-1"><i class="bi bi-info-circle me-1"></i> This invoice is generated automatically for your transaction.</p>
                <p class="mb-0">Please keep this invoice for your records. For any discrepancies, contact support within 30 days.</p>
            </div>
        </div>

        <div class="d-flex justify-content-center gap-3 mt-4 d-print-none">
            <a href="invoice?txnId=${transaction.txnId}&format=pdf" class="btn btn-success rounded-pill px-4 shadow-sm">
                <i class="bi bi-file-earmark-pdf me-2"></i> Download PDF
            </a>
            <button onclick="window.print()" class="btn btn-primary rounded-pill px-4 shadow-sm">
                <i class="bi bi-printer me-2"></i> Print Invoice
            </button>
        </div>
    </div>
</div>

<style>
    @media print {
        body { background: white !important; color: black !important; }
        .glass-card { background: none !important; backdrop-filter: none !important; border: none !important; box-shadow: none !important; padding: 0 !important; }
        .text-white { color: black !important; }
        .text-muted { color: #555 !important; }
        .badge { border: 1px solid #ccc; color: black !important; background: transparent !important; }
        .bg-dark, .border-secondary { border-color: #ddd !important; background: transparent !important; }
        .d-print-none { display: none !important; }
        .text-gradient { background: none !important; -webkit-text-fill-color: black !important; color: black !important; }
    }
</style>

<jsp:include page="WEB-INF/components/footer.jsp" />
