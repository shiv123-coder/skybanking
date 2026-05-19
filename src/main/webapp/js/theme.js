(function() {
    // 1. Immediate Theme Detection (run before page render to prevent flash)
    const savedTheme = localStorage.getItem('theme');
    const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    const themeToApply = savedTheme || systemTheme;
    document.documentElement.setAttribute('data-theme', themeToApply);

    // 2. Wait for DOM content to load to inject and bind the modern floating toggle
    window.addEventListener('DOMContentLoaded', () => {
        // Create the global floating theme toggle button
        const toggleBtn = document.createElement('button');
        toggleBtn.id = 'global-theme-toggle';
        toggleBtn.className = 'global-theme-toggle btn-link';
        toggleBtn.setAttribute('title', 'Switch to Dark/Light Mode');
        toggleBtn.innerHTML = `
            <div class="theme-icon-wrapper">
                <i class="bi bi-sun-fill theme-sun"></i>
                <i class="bi bi-moon-stars-fill theme-moon"></i>
            </div>
        `;
        document.body.appendChild(toggleBtn);

        // Function to update all toggles and save state
        function setTheme(theme) {
            document.documentElement.setAttribute('data-theme', theme);
            localStorage.setItem('theme', theme);
            
            // Sync sidebar icon (if present on the page)
            const sidebarToggle = document.getElementById('theme-toggle');
            if (sidebarToggle) {
                const sunIcon = sidebarToggle.querySelector('.theme-sun');
                const moonIcon = sidebarToggle.querySelector('.theme-moon');
                if (sunIcon && moonIcon) {
                    if (theme === 'dark') {
                        sunIcon.style.opacity = '1';
                        sunIcon.style.transform = 'translate(-50%, -50%) rotate(0deg) scale(1)';
                        moonIcon.style.opacity = '0';
                        moonIcon.style.transform = 'translate(-50%, -50%) rotate(90deg) scale(0)';
                    } else {
                        sunIcon.style.opacity = '0';
                        sunIcon.style.transform = 'translate(-50%, -50%) rotate(-90deg) scale(0)';
                        moonIcon.style.opacity = '1';
                        moonIcon.style.transform = 'translate(-50%, -50%) rotate(0deg) scale(1)';
                    }
                }
            }
        }

        // Toggle action on click
        toggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            setTheme(newTheme);
            
            // Small click ripple effect/bounce
            toggleBtn.style.transform = 'scale(0.9) translateY(2px)';
            setTimeout(() => {
                toggleBtn.style.transform = '';
            }, 150);
        });

        // Sync click listener for sidebar toggle button if it exists
        const sidebarToggle = document.getElementById('theme-toggle');
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', () => {
                const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
                const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
                setTheme(newTheme);
            });
        }
    });
})();
