<%@ page contentType="text/html;charset=UTF-8" %>
<%
    boolean loggedIn = (session != null && session.getAttribute("user_id") != null);
    if (loggedIn) {
        request.setAttribute("error", "You are already logged in. Logout to sign up again.");
    }

    // Check if user came from OTP flow
    boolean fromOTP = session.getAttribute("fromOTP") != null && (Boolean) session.getAttribute("fromOTP");
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0" style="max-width: 500px;">
        <div class="text-center mb-4">
            <div class="d-inline-flex bg-primary bg-opacity-10 text-primary rounded-circle p-3 mb-3 shadow-sm">
                <i class="bi bi-person-plus fs-1"></i>
            </div>
            <h2 class="fw-bold fs-3 text-dark">Create Account</h2>
            <p class="text-muted">Join SkyBanking for premium services</p>
        </div>

        <jsp:include page="WEB-INF/components/alerts.jsp" />

        <% if (!loggedIn) { %>
        <!-- action now points to /signup -->
        <form action="signup" method="post">
            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">FULL NAME</label>
                <div class="position-relative">
                    <i class="bi bi-person-badge position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="fullname" class="form-control ps-5 form-control-lg fw-medium" placeholder="John Doe" required
                           value="<%= request.getParameter("fullname") != null ? request.getParameter("fullname") : "" %>">
                </div>
            </div>
            
            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">USERNAME</label>
                <div class="position-relative">
                    <i class="bi bi-person position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="username" class="form-control ps-5 form-control-lg fw-medium" placeholder="Choose a username" required
                           value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>">
                </div>
            </div>
            
            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">EMAIL ADDRESS</label>
                <div class="position-relative">
                    <i class="bi bi-envelope position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="email" name="email" class="form-control ps-5 form-control-lg fw-medium" placeholder="john@example.com" required
                           value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>">
                </div>
            </div>
            
            <div class="mb-3">
                <label class="form-label text-secondary small fw-bold ms-1">MOBILE NUMBER</label>
                <div class="position-relative">
                    <i class="bi bi-telephone position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="text" name="mobile" class="form-control ps-5 form-control-lg fw-medium" placeholder="+1 234 567 8900" required
                           value="<%= request.getParameter("mobile") != null ? request.getParameter("mobile") : "" %>">
                </div>
            </div>
            
            <div class="mb-4">
                <label class="form-label text-secondary small fw-bold ms-1">PASSWORD</label>
                <div class="position-relative">
                    <i class="bi bi-lock position-absolute top-50 start-0 translate-middle-y ms-3 text-muted"></i>
                    <input type="password" name="password" id="signupPassword" class="form-control ps-5 form-control-lg fw-medium" placeholder="Create a secure password" required>
                    <i class="bi bi-eye position-absolute top-50 end-0 translate-middle-y me-3 text-muted" style="cursor:pointer; transition: color 0.2s;"
                       onclick="togglePassword('signupPassword', this)" onmouseover="this.classList.add('text-primary')" onmouseout="this.classList.remove('text-primary')"></i>
                </div>
            </div>
            
            <button type="submit" class="btn btn-primary btn-lg w-100 py-3 mt-2 fw-bold shadow-sm d-flex justify-content-center align-items-center">
                Sign Up <i class="bi bi-person-check ms-2 fs-5"></i>
            </button>
        </form>
        <% } %>

        <div class="text-center mt-4 pt-3 border-top">
            <p class="text-muted small mb-0">Already have an account?</p>
            <a href="login.jsp" class="text-decoration-none fw-bold text-primary px-3 py-2 rounded-pill hover-scale d-inline-block mt-1 transition-all" style="background: rgba(79, 70, 229, 0.05);">Sign in instead</a>
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
