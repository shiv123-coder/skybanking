<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.*, com.skybanking.model.Transaction" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <title>User Transactions - SkyBanking</title>
                <!-- App Icon / Favicon -->
                <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/favicon.svg">
                <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.svg">
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
                <!-- Bootstrap Icons -->
                <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/adminStyle.css">
                <!-- Global Theme Script -->
                <script src="${pageContext.request.contextPath}/js/theme.js" defer></script>
                <script>
                    // Immediate Theme Detection (to prevent flashing)
                    (function() {
                        const savedTheme = localStorage.getItem('theme');
                        const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
                        const themeToApply = savedTheme || systemTheme;
                        document.documentElement.setAttribute('data-theme', themeToApply);
                    })();
                </script>
            </head>

            <body>
                <div class="container mt-4">
                    <h2>User Transactions</h2>

                    <c:if test="${not empty user}">
                        <p><strong>User:</strong> ${user.fullname} (${user.username})</p>
                        <p><strong>Email:</strong> ${user.email}</p>
                        <p><strong>Phone:</strong> ${user.phone}</p>
                        <p><strong>Account Number:</strong> ${account.accountNumber}</p>
                    </c:if>

                    <!-- Filter Form -->
                    <form method="get" class="row g-3 mb-3">
                        <input type="hidden" name="action" value="transactions" />
                        <input type="hidden" name="userId" value="${user.id}" />

                        <div class="col-md-3">
                            <label for="fromDate" class="form-label">From Date</label>
                            <input type="date" class="form-control" id="fromDate" name="fromDate"
                                value="${param.fromDate}">
                        </div>
                        <div class="col-md-3">
                            <label for="toDate" class="form-label">To Date</label>
                            <input type="date" class="form-control" id="toDate" name="toDate" value="${param.toDate}">
                        </div>
                        <div class="col-md-3">
                            <label for="txnType" class="form-label">Transaction Type</label>
                            <select class="form-select" id="txnType" name="txnType">
                                <option value="">All</option>
                                <option value="DEPOSIT" ${param.txnType=='DEPOSIT' ?'selected':''}>Deposit</option>
                                <option value="WITHDRAWAL" ${param.txnType=='WITHDRAWAL' ?'selected':''}>Withdrawal
                                </option>
                                <option value="TRANSFER" ${param.txnType=='TRANSFER' ?'selected':''}>Transfer</option>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary me-2">Filter</button>
                            <a href="<c:url value='/admin/users?action=export&userId=${user.id}'/>"
                                class="btn btn-success">Export PDF</a>
                        </div>
                    </form>

                    <!-- Back Button -->
                    <div class="mb-3">
                        <a href="<c:url value='/admin/users'/>" class="btn btn-secondary">Back to Users</a>
                    </div>

                    <!-- Transactions Table -->
                    <c:if test="${not empty transactions}">
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped">
                                <thead class="table-dark">
                                    <tr>
                                        <th>Date</th>
                                        <th>Type</th>
                                        <th>Amount (₹)</th>
                                        <th>Tax (₹)</th>
                                        <th>Total (₹)</th>
                                        <th>Description</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="txn" items="${transactions}">
                                        <tr>
                                            <td>${txn.date}</td>
                                            <td>${txn.type}</td>
                                            <td>${txn.amount}</td>
                                            <td>${txn.taxAmount != null ? txn.taxAmount : '0.00'}</td>
                                            <td>${txn.totalAmount}</td>
                                            <td>${txn.description != null ? txn.description : ''}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>

                    <c:if test="${empty transactions}">
                        <div class="alert alert-info">No transactions found for this user with the selected filters.
                        </div>
                    </c:if>
                </div>
            </body>

            </html>