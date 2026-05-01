<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin - Loans</title>
    <link rel="stylesheet" href="../css/adminStyle.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="container mt-4">
    <h3>Loan Applications</h3>
    <table class="table table-striped">
        <thead>
        <tr>
            <th>ID</th>
            <th>User</th>
            <th>Principal</th>
            <th>Rate %</th>
            <th>Tenure</th>
            <th>EMI</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <%
            java.util.List<java.util.Map<String, Object>> loans = (java.util.List<java.util.Map<String, Object>>) request.getAttribute("loans");
            if (loans != null) for (var l : loans) {
        %>
        <tr>
            <td><%= l.get("loan_id") %></td>
            <td><%= l.get("username") %></td>
            <td>₹<%= l.get("principal") %></td>
            <td><%= l.get("interest_rate") %></td>
            <td><%= l.get("tenure_months") %> mo</td>
            <td>₹<%= l.get("emi") %></td>
            <td><span class="badge bg-info"><%= l.get("status") %></span></td>
            <td>
                <form action="loans" method="post" class="d-inline">
                    <input type="hidden" name="loan_id" value="<%= l.get("loan_id") %>">
                    <button name="action" value="approve" class="btn btn-success btn-sm">Approve</button>
                </form>
                <form action="loans" method="post" class="d-inline">
                    <input type="hidden" name="loan_id" value="<%= l.get("loan_id") %>">
                    <button name="action" value="reject" class="btn btn-warning btn-sm">Reject</button>
                </form>
                <form action="loans" method="post" class="d-inline">
                    <input type="hidden" name="loan_id" value="<%= l.get("loan_id") %>">
                    <button name="action" value="disburse" class="btn btn-primary btn-sm">Disburse</button>
                </form>
            </td>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>
    <a href="dashboard" class="btn btn-secondary">Back</a>
</body>
</html>


