<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String successMsg = request.getParameter("success");
    if (successMsg == null || successMsg.isEmpty()) successMsg = (String) request.getAttribute("success");
    if (successMsg == null || successMsg.isEmpty()) {
        String msg = request.getParameter("message");
        if (msg != null && !msg.isEmpty()) successMsg = msg;
    }
    
    String errorMsg = request.getParameter("error");
    if (errorMsg == null || errorMsg.isEmpty()) errorMsg = (String) request.getAttribute("error");
%>

<% if (successMsg != null && !successMsg.isEmpty()) { %>
    <div class="alert alert-success alert-dismissible fade show shadow-sm border-0 d-flex align-items-center animate-fade-up" role="alert">
        <i class="bi bi-check-circle-fill fs-4 me-3 text-success"></i> 
        <div><%= successMsg %></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>

<% if (errorMsg != null && !errorMsg.isEmpty()) { %>
    <div class="alert alert-danger alert-dismissible fade show shadow-sm border-0 d-flex align-items-center animate-fade-up" role="alert">
        <i class="bi bi-exclamation-triangle-fill fs-4 me-3 text-danger"></i> 
        <div><%= errorMsg %></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>
