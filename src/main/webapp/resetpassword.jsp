<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    if (session == null || session.getAttribute("isOtpVerified") == null) {
        response.sendRedirect("forgotpassword.jsp");
        return;
    }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0">
        <div class="text-center mb-4">
            <div class="d-inline-flex bg-success bg-opacity-10 text-success rounded-circle p-3 mb-3 shadow-sm">
                <i class="bi bi-key-fill fs-1"></i>
            </div>
            <h2 class="fw-bold fs-3 text-dark">Reset Your Password</h2>
            <p class="text-muted">Enter a strong new password for your account</p>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <form action="${pageContext.request.contextPath}/resetpassword" method="post" class="mt-4">
            <div class="mb-4 position-relative">
                <label class="form-label text-secondary small fw-bold ms-1">NEW PASSWORD</label>
                <div class="input-group">
                    <span class="input-group-text bg-light border-end-0 border-secondary"><i class="bi bi-shield-lock text-muted"></i></span>
                    <input type="password" name="newPassword" id="newPassword" class="form-control form-control-lg border-start-0 ps-0 pe-5 bg-light" placeholder="Enter new password" required autofocus>
                </div>
                <i class="bi bi-eye text-muted position-absolute" style="right: 15px; top: 40px; cursor:pointer; z-index: 10; transition: color 0.2s;"
                   onclick="togglePassword('newPassword', this)" onmouseover="this.classList.add('text-primary')" onmouseout="this.classList.remove('text-primary')"></i>
            </div>

            <div class="mb-4 position-relative">
                <label class="form-label text-secondary small fw-bold ms-1">CONFIRM PASSWORD</label>
                <div class="input-group">
                    <span class="input-group-text bg-light border-end-0 border-secondary"><i class="bi bi-shield-check text-muted"></i></span>
                    <input type="password" name="confirmPassword" id="confirmPassword" class="form-control form-control-lg border-start-0 ps-0 pe-5 bg-light" placeholder="Confirm new password" required>
                </div>
                <i class="bi bi-eye text-muted position-absolute" style="right: 15px; top: 40px; cursor:pointer; z-index: 10; transition: color 0.2s;"
                   onclick="togglePassword('confirmPassword', this)" onmouseover="this.classList.add('text-primary')" onmouseout="this.classList.remove('text-primary')"></i>
            </div>

            <button type="submit" class="btn btn-success btn-lg w-100 py-3 mt-2 fw-bold shadow-sm d-flex justify-content-center align-items-center">
                Update Password <i class="bi bi-check2-circle ms-2 fs-5"></i>
            </button>
        </form>

        <div class="text-center mt-4 pt-3 border-top">
            <a href="login.jsp" class="text-decoration-none text-muted fw-semibold hover-scale d-inline-block transition-all fs-6"><i class="bi bi-arrow-left me-1"></i> Return to Login</a>
        </div>
    </div>
</div>

<script>
    function togglePassword(fieldId, icon) {
        const input = document.getElementById(fieldId);
        if (input.type === "password") {
            input.type = "text";
            icon.classList.remove("bi-eye");
            icon.classList.add("bi-eye-slash");
        } else {
            input.type = "password";
            icon.classList.add("bi-eye");
            icon.classList.remove("bi-eye-slash");
        }
    }
</script>

<jsp:include page="WEB-INF/components/footer.jsp" />
