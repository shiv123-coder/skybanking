<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");

    Long expiry = (Long) session.getAttribute("otpExpiry");
    long remainingTime = (expiry != null ? (expiry - System.currentTimeMillis()) / 1000 : 0);

    // Determine OTP type
    String otpType = request.getParameter("type");
    if (otpType == null) {
        otpType = (session.getAttribute("isSignup") != null && (Boolean) session.getAttribute("isSignup"))
                  ? "signup" : "profile";
    }

    // Resend & back page logic
    String resendAction, backPage;
    switch (otpType) {
        case "signup":
            resendAction = request.getContextPath() + "/sendotp";
            backPage = request.getContextPath() + "/signupPage?fromOTP=true";
            session.setAttribute("fromOTP", true);
            break;
        case "forgot":
            resendAction = request.getContextPath() + "/sendotp";
            backPage = "forgotpassword.jsp";
            break;
        case "profile":
            resendAction = request.getContextPath() + "/sendOtpProfile";
            backPage = "updateProfile.jsp";
            break;
        default:
            resendAction = request.getContextPath() + "/sendotp";
            backPage = "forgotpassword.jsp";
            break;
    }
%>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0">
        <div class="text-center mb-4">
            <div class="d-inline-flex bg-info bg-opacity-10 text-info rounded-circle p-3 mb-3 shadow-sm">
                <i class="bi bi-shield-check fs-1"></i>
            </div>
            <h2 class="fw-bold fs-3 text-dark">Verify OTP</h2>
            <p class="text-muted">We've sent a 6-digit code to your contact details</p>
        </div>

        <% if (error != null) { %>
            <div class="alert alert-danger alert-dismissible fade show border-0 d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill fs-4 me-3 text-danger"></i> 
                <div><%= error %></div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>
        <% if (message != null) { %>
            <div class="alert alert-success alert-dismissible fade show border-0 d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill fs-4 me-3 text-success"></i> 
                <div><%= message %></div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>

        <form id="otpForm" action="verifyotp" method="post" class="mt-4">
            <div class="mb-4">
                <label for="otp" class="form-label text-secondary small fw-bold text-center w-100">ENTER 6-DIGIT OTP</label>
                <input type="text" name="otp" id="otp" class="form-control form-control-lg text-center fw-bold fs-3 bg-light" required autofocus autocomplete="one-time-code" style="letter-spacing: 0.75rem;">
            </div>
            <input type="hidden" name="type" value="<%= otpType %>">
            
            <button type="submit" class="btn btn-primary btn-lg w-100 py-3 fw-bold shadow-sm d-flex justify-content-center align-items-center mt-2">
                Verify Code <i class="bi bi-patch-check ms-2"></i>
            </button>
        </form>

        <div class="mt-4 pt-2 d-flex flex-column align-items-center bg-light rounded-4 p-3 border shadow-sm">
            <p id="timer" class="mb-2 fw-bold text-danger fs-5 d-flex align-items-center">
                <i class="bi bi-stopwatch me-2"></i> <span>--:--</span>
            </p>
            <button type="button" id="resendBtn" class="btn btn-outline-dark btn-sm rounded-pill px-4 fw-medium" disabled>
                Resend OTP <i class="bi bi-arrow-clockwise ms-1"></i>
            </button>
        </div>

        <div class="text-center mt-4 pt-3 border-top">
            <a href="<%= backPage %>" class="text-decoration-none text-muted fw-semibold hover-scale d-inline-block transition-all fs-6">
                <i class="bi bi-arrow-left me-1"></i> Go Back
            </a>
        </div>
    </div>
</div>

<script>
    var timeLeft = <%= (remainingTime >= 0 ? remainingTime : 0) %>;
    var interval;

    function formatTime(seconds) {
        const m = Math.floor(seconds / 60).toString().padStart(2, '0');
        const s = (seconds % 60).toString().padStart(2, '0');
        return m + ":" + s;
    }

    function startTimer() {
        clearInterval(interval);
        var timerSpan = document.querySelector("#timer span");
        var verifyBtn = document.querySelector("#otpForm button[type='submit']");
        var resendBtn = document.getElementById("resendBtn");

        verifyBtn.disabled = false;
        resendBtn.disabled = true;

        interval = setInterval(function () {
            if (timeLeft <= 0) {
                clearInterval(interval);
                timerSpan.innerText = "Expired";
                verifyBtn.disabled = true;
                resendBtn.disabled = false;
            } else {
                timerSpan.innerText = formatTime(timeLeft);
            }
            timeLeft--;
        }, 1000);
    }

    window.onload = startTimer;

    document.getElementById("resendBtn").addEventListener("click", function() {
        this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Sending...';
        
        fetch('<%= resendAction %>', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'type=<%= otpType %>'
        })
        .then(response => response.text())
        .then(() => {
            this.innerHTML = 'Resend OTP <i class="bi bi-arrow-clockwise ms-1"></i>';
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-success alert-dismissible fade show shadow-sm border-0 d-flex align-items-center';
            alertDiv.innerHTML = '<i class="bi bi-check-circle-fill fs-4 me-3 text-success"></i> <div>OTP resent successfully!</div> <button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
            document.querySelector('.auth-card').insertBefore(alertDiv, document.getElementById('otpForm'));
            setTimeout(() => { alertDiv.classList.remove('show'); setTimeout(() => alertDiv.remove(), 250); }, 3000);
            
            this.disabled = true;
            timeLeft = 300;
            startTimer();
        })
        .catch(err => {
            this.innerHTML = 'Resend OTP <i class="bi bi-arrow-clockwise ms-1"></i>';
            alert("Error resending OTP: " + err);
        });
    });
</script>

<jsp:include page="WEB-INF/components/footer.jsp" />
