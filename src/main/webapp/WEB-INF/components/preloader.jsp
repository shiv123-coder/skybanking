<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="io.github.cdimascio.dotenv.Dotenv" %>
<%
    // Load environment variables
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String serverName = request.getServerName();
    boolean isLocal = "localhost".equalsIgnoreCase(serverName) || "127.0.0.1".equals(serverName);
    
    // Global Config Toggles
    String appEnv = dotenv.get("APP_ENV", "development");
    String enablePreloaderTag = dotenv.get("ENABLE_PRELOADER", "true");
    
    boolean isProd = "production".equalsIgnoreCase(appEnv);
    boolean isFeatureEnabled = "true".equalsIgnoreCase(enablePreloaderTag);
    
    // Final visibility logic: MUST be enabled in config AND (be production OR be forced for testing)
    boolean showLoader = isFeatureEnabled && (!isLocal && isProd);
%>

<!-- Global Loading State (Global Toggle Tag) -->
<script>
    (function() {
        window.SkyBankingConfig = window.SkyBankingConfig || {};
        window.SkyBankingConfig.preloaderEnabled = <%= isFeatureEnabled %>;
        window.SkyBankingConfig.isProduction = <%= isProd %>;
        
        // Honor local toggle
        const isLocallyDisabled = localStorage.getItem('disablePreloader') === 'true';
        if (isLocallyDisabled) {
            document.documentElement.classList.add('no-preloader');
        }
        
        // Global state for entire project
        window.AppState = window.AppState || {};
        window.AppState.isLoading = <%= showLoader %> && !isLocallyDisabled;
    })();
</script>

<% if (showLoader) { %>
<div id="sky-preloader" class="preloader-overlay">
    <div class="preloader-content">
        <div class="loader-3d-container">
            <div class="orb-shadow"></div>
            <div class="orb">
                <img src="${pageContext.request.contextPath}/images/favicon.svg" alt="SkyBanking Logo" style="width: 100%; height: 100%; padding: 12px; filter: drop-shadow(0 0 10px var(--loader-glow));">
            </div>
            <div class="ring ring-1"></div>
            <div class="ring ring-2"></div>
            <div class="ring ring-3"></div>
        </div>
        
        <div class="loading-text">
            <span class="brand">SkyBanking</span>
            <div class="status">Preparing your experience<span class="dots">...</span></div>
        </div>
    </div>
</div>

<style>
    :root {
        --loader-primary: #4f46e5;
        --loader-secondary: #0ea5e9;
        --loader-glow: rgba(79, 70, 229, 0.5);
    }

    .preloader-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: #0d1117; /* Solid dark start to prevent flash */
        background: radial-gradient(circle at center, #1e293b 0%, #0d1117 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 99999;
        overflow: hidden;
        transition: opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1), transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
        perspective: 1000px;
    }

    .preloader-overlay.fade-out {
        opacity: 0;
        transform: scale(1.05);
        pointer-events: none;
    }

    /* Support for global toggle button */
    .no-preloader .preloader-overlay {
        display: none !important;
    }

    .preloader-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        transform-style: preserve-3d;
    }

    /* 3D Loader Elements */
    .loader-3d-container {
        position: relative;
        width: 200px;
        height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
        transform-style: preserve-3d;
        animation: containerFloat 4s ease-in-out infinite;
    }

    .orb {
        position: relative;
        width: 60px;
        height: 60px;
        background: var(--loader-primary);
        border-radius: 50%;
        z-index: 10;
        box-shadow: 
            0 0 30px var(--loader-glow),
            inset -10px -10px 20px rgba(0,0,0,0.3),
            inset 10px 10px 20px rgba(255,255,255,0.2);
        animation: orbPulse 2s ease-in-out infinite;
    }

    .orb-inner {
        position: absolute;
        top: 15%;
        left: 15%;
        width: 30%;
        height: 30%;
        background: rgba(255, 255, 255, 0.4);
        border-radius: 50%;
        filter: blur(2px);
    }

    .orb-shadow {
        position: absolute;
        bottom: -20px;
        width: 40px;
        height: 10px;
        background: rgba(0, 0, 0, 0.4);
        border-radius: 50%;
        filter: blur(5px);
        transform: rotateX(70deg);
        animation: shadowPulse 2s ease-in-out infinite;
    }

    .ring {
        position: absolute;
        border: 2px solid transparent;
        border-radius: 50%;
        transform-style: preserve-3d;
    }

    .ring-1 {
        width: 100px;
        height: 100px;
        border-top-color: var(--loader-primary);
        border-bottom-color: var(--loader-primary);
        animation: rotate3D1 3s linear infinite;
    }

    .ring-2 {
        width: 140px;
        height: 140px;
        border-left-color: var(--loader-secondary);
        border-right-color: var(--loader-secondary);
        animation: rotate3D2 4s linear infinite;
        opacity: 0.7;
    }

    .ring-3 {
        width: 180px;
        height: 180px;
        border-top-color: rgba(255, 255, 255, 0.2);
        animation: rotate3D3 6s linear infinite;
        opacity: 0.4;
    }

    /* Text Styling */
    .loading-text {
        margin-top: 40px;
        text-align: center;
        color: white;
        font-family: 'Inter', sans-serif;
    }

    .brand {
        display: block;
        font-size: 1.8rem;
        font-weight: 800;
        letter-spacing: 2px;
        text-transform: uppercase;
        background: linear-gradient(135deg, #fff 0%, var(--loader-secondary) 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: 8px;
        animation: shimmer 3s infinite linear;
        background-size: 200% 100%;
    }

    .status {
        font-size: 0.9rem;
        color: rgba(255, 255, 255, 0.6);
        font-weight: 400;
        letter-spacing: 1px;
    }

    .dots {
        display: inline-block;
        width: 20px;
        text-align: left;
    }

    /* Animations */
    @keyframes orbPulse {
        0%, 100% { transform: scale(1); box-shadow: 0 0 30px var(--loader-glow); }
        50% { transform: scale(1.1); box-shadow: 0 0 60px var(--loader-glow); }
    }

    @keyframes shadowPulse {
        0%, 100% { transform: rotateX(70deg) scale(1); opacity: 0.4; }
        50% { transform: rotateX(70deg) scale(1.2); opacity: 0.2; }
    }

    @keyframes containerFloat {
        0%, 100% { transform: translateY(0) rotateY(0deg); }
        50% { transform: translateY(-15px) rotateY(10deg); }
    }

    @keyframes rotate3D1 {
        from { transform: rotateX(70deg) rotateZ(0deg); }
        to { transform: rotateX(70deg) rotateZ(360deg); }
    }

    @keyframes rotate3D2 {
        from { transform: rotateX(-60deg) rotateY(20deg) rotateZ(0deg); }
        to { transform: rotateX(-60deg) rotateY(20deg) rotateZ(360deg); }
    }

    @keyframes rotate3D3 {
        from { transform: rotateX(0deg) rotateY(70deg) rotateZ(0deg); }
        to { transform: rotateX(0deg) rotateY(70deg) rotateZ(360deg); }
    }

    @keyframes shimmer {
        0% { background-position: -200% 0; }
        100% { background-position: 200% 0; }
    }

    /* Prevent scrolling during load */
    html.loading, html.loading body {
        overflow: hidden !important;
        height: 100% !important;
    }
</style>

<script>
    (function() {
        // Immediately lock scroll
        document.documentElement.classList.add('loading');

        const hideLoader = () => {
            const loader = document.getElementById('sky-preloader');
            if (loader) {
                loader.classList.add('fade-out');
                setTimeout(() => {
                    loader.remove();
                    document.documentElement.classList.remove('loading');
                }, 600);
            }
        };

        // Smart Loading Logic
        // 1. Wait for full window load (images, scripts, etc)
        window.addEventListener('load', () => {
            // Add a very slight delay for visual smoothness even if load is instant
            setTimeout(hideLoader, 400);
        });

        // 2. Fallback: if window load takes too long (e.g. 5s), hide anyway to ensure UX
        setTimeout(hideLoader, 5000);

        // 3. Optional: Global method for manual control if needed by other scripts
        window.AppLoader = {
            hide: hideLoader
        };
    })();
</script>
<% } %>
