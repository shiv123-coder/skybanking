<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0">
        <div class="text-center mb-4">
            <div class="d-inline-flex bg-warning bg-opacity-10 text-warning rounded-circle p-3 mb-3 shadow-sm">
                <i class="bi bi-shield-lock fs-1"></i>
            </div>
            <h2 class="fw-bold fs-3 text-dark">Reset Password</h2>
            <p class="text-muted">Verify your identity to reset your password</p>
        </div>

        <%
            Boolean isOtpVerified = (Boolean) session.getAttribute("isOtpVerified");
            if (isOtpVerified == null) isOtpVerified = false;

            Long otpExpiry = (Long) session.getAttribute("otpExpiry");
            long remainingTime = (otpExpiry != null ? (otpExpiry - System.currentTimeMillis()) / 1000 : 0);
        %>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <form id="forgotForm" action="sendotp" method="post">
            <input type="hidden" name="type" value="forgot">

            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">USERNAME</label>
                <div class="position-relative">
                    <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="username" class="form-control ps-5 form-control-lg fw-medium" placeholder="Enter your username" required autofocus>
                </div>
            </div>
            
            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">REGISTERED EMAIL</label>
                <div class="position-relative">
                    <i class="bi bi-envelope position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="email" name="email" class="form-control ps-5 form-control-lg fw-medium" placeholder="john@example.com" required>
                </div>
            </div>
            
            <div class="mb-4">
                <label class="form-label text-secondary small fw-bold ms-1">REGISTERED MOBILE</label>
                <div class="position-relative">
                    <i class="bi bi-telephone position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="mobile" class="form-control ps-5 form-control-lg fw-medium" placeholder="+1 234 567 890" required>
                </div>
            </div>

            <button type="submit" class="btn btn-warning btn-lg w-100 py-3 mt-2 fw-bold shadow-sm d-flex justify-content-center align-items-center text-dark" style="background: linear-gradient(135deg, #fcd34d, #f59e0b);">
                Request OTP <i class="bi bi-send ms-2"></i>
            </button>
        </form>

        <div class="text-center mt-4 pt-3 border-top">
            <p class="text-muted small mb-0">Remembered your password?</p>
            <a href="login.jsp" class="text-decoration-none fw-bold text-primary hover-scale d-inline-block mt-1 transition-all">Back to Login</a>
        </div>
    </div>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />
