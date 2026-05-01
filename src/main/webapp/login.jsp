<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%
    if (session != null && session.getAttribute("username") != null) {
        response.sendRedirect("dashboard.jsp");
        return;
    }
    request.setAttribute("pageTitle", "Login - SkyBanking");
%>

<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0">
        <div class="text-center mb-5">
            <div class="d-inline-flex bg-primary bg-opacity-10 text-primary rounded-circle p-3 mb-3 shadow-sm">
                <i class="bi bi-bank2 fs-1"></i>
            </div>
            <h2 class="fw-bold fs-3 text-dark">Welcome back</h2>
            <p class="text-muted">Enter your credentials to access your account</p>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="mb-4">
                <label class="form-label ms-1">Username</label>
                <div class="position-relative">
                    <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="username" class="form-control ps-5 form-control-lg" placeholder="Enter your username" required autofocus>
                </div>
            </div>
            
            <div class="mb-4">
                <div class="d-flex justify-content-between mb-1 ms-1">
                    <label class="form-label mb-0">Password</label>
                    <a href="forgotpassword.jsp" class="text-decoration-none small fw-semibold text-primary">Forgot Password?</a>
                </div>
                <div class="position-relative">
                    <i class="bi bi-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="password" name="password" id="loginPassword" class="form-control ps-5 form-control-lg" placeholder="Enter your password" required>
                    <i class="bi bi-eye position-absolute top-50 end-0 translate-middle-y me-3 text-muted" style="cursor:pointer; transition: color 0.2s;" 
                       onclick="togglePassword('loginPassword', this)" onmouseover="this.classList.add('text-primary')" onmouseout="this.classList.remove('text-primary')"></i>
                </div>
            </div>

            <button type="submit" class="btn btn-primary btn-lg w-100 py-3 mt-2 fw-bold shadow-sm">
                Sign In <i class="bi bi-arrow-right ms-2"></i>
            </button>
        </form>

        <div class="text-center mt-5">
            <p class="text-muted small mb-0">Don't have an account?</p>
            <a href="signup.jsp" class="text-decoration-none fw-bold text-primary px-3 py-2 rounded-pill hover-scale d-inline-block mt-1 transition-all" style="background: rgba(79, 70, 229, 0.05);">Create an account</a>
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
