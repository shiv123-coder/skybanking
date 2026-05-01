<%@ page isErrorPage="true" %>
<jsp:include page="WEB-INF/components/header.jsp" />

<div class="auth-layout">
    <div class="glass-panel auth-card animate-fade-up shadow-lg border-0 text-center" style="max-width: 600px;">
        <div class="d-inline-flex align-items-center justify-content-center bg-danger bg-opacity-10 rounded-circle text-danger mb-4 p-4 shadow-sm">
            <i class="bi bi-exclamation-octagon fs-1"></i>
        </div>
        
        <h2 class="fw-bold text-dark mb-3">Something broke ☹</h2>
        <p class="text-muted mb-4 fs-5">We're sorry, but an unexpected error has occurred.</p>
        
        <div class="bg-light border shadow-sm rounded-4 p-4 text-start mb-4 overflow-auto" style="max-height: 250px;">
            <h6 class="text-danger fw-bold"><i class="bi bi-bug me-2"></i>Error Details:</h6>
            <p class="text-dark font-monospace small mb-3 border-bottom pb-2"><%= exception != null ? exception.getMessage() : "Unknown error" %></p>
            <h6 class="text-secondary fw-bold mt-3">Stack Trace:</h6>
            <pre class="text-muted font-monospace small m-0 shadow-inner p-3 bg-white rounded border border-light" style="white-space: pre-wrap; word-wrap: break-word;"><% 
                if(exception != null) {
                    exception.printStackTrace(new java.io.PrintWriter(out));
                }
            %></pre>
        </div>
        
        <a href="dashboard.jsp" class="btn btn-outline-primary rounded-pill px-5 py-3 fw-bold w-100 d-flex justify-content-center align-items-center hover-scale">
            <i class="bi bi-arrow-left me-2 fs-5"></i> Return to Dashboard
        </a>
    </div>
</div>

<jsp:include page="WEB-INF/components/footer.jsp" />