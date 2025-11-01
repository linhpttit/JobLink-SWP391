// Translations object
        const translations = {
            en: {
                home: "Home",
                find_job: "Find Job",
                employers: "Employers",
                candidates: "Candidates",
                pricing_plans: "Pricing Plans",
                customer_support: "Customer Supports",
                sign_in: "Sign In",
                log_out: "Log Out",
                post_job: "Post A Job",
                search_placeholder: "Find job by job title, keyword, company",
                welcome_back: "Welcome back!",
                signing_in: "Signing you in automatically..."
            },
            vi: {
                home: "Trang chủ",
                find_job: "Tìm việc",
                employers: "Nhà tuyển dụng",
                candidates: "Ứng viên",
                pricing_plans: "Bảng giá",
                customer_support: "Hỗ trợ khách hàng",
                sign_in: "Đăng nhập",
                log_out: "Đăng xuất",
                post_job: "Đăng tin",
                search_placeholder: "Tìm việc theo tiêu đề, từ khóa, công ty",
                welcome_back: "Chào mừng trở lại!",
                signing_in: "Đang đăng nhập tự động..."
            }
        };

        // Change language function
        function changeLanguage(lang) {
            // Save language preference
            localStorage.setItem('joblink_language', lang);

            // Update flag and text
            const flagImg = document.getElementById('currentLangFlag');
            const langText = document.getElementById('currentLangText');

            if (lang === 'vi') {
                flagImg.src = 'https://flagcdn.com/vn.svg';
                langText.textContent = 'Tiếng Việt';
            } else {
                flagImg.src = 'https://flagcdn.com/us.svg';
                langText.textContent = 'English';
            }

            // Update all text elements
            applyTranslations(lang);
        }

        // Apply translations to page
        function applyTranslations(lang) {
            const elements = document.querySelectorAll('[data-i18n]');
            elements.forEach(element => {
                const key = element.getAttribute('data-i18n');
                if (translations[lang] && translations[lang][key]) {
                    element.textContent = translations[lang][key];
                }
            });

            // Update placeholders
            const placeholderElements = document.querySelectorAll('[data-i18n-placeholder]');
            placeholderElements.forEach(element => {
                const key = element.getAttribute('data-i18n-placeholder');
                if (translations[lang] && translations[lang][key]) {
                    element.placeholder = translations[lang][key];
                }
            });

            // Update HTML lang attribute
            document.documentElement.lang = lang;
        }

        // Initialize language on page load
        function initializeLanguage() {
            const savedLang = localStorage.getItem('joblink_language') || 'en';
            changeLanguage(savedLang);
        }

function initializeDropdownMenu() {
    const menuBtn = document.getElementById('usermenuBtn');
    const dropdown = document.getElementById('usermenuDropdown');

    if (!menuBtn || !dropdown) {
        return; // Thoát nếu không tìm thấy các element cần thiết
    }

    // Sự kiện MỞ/ĐÓNG menu khi click vào nút avatar
    menuBtn.addEventListener('click', function(event) {
        event.stopPropagation(); // Ngăn sự kiện click lan ra ngoài
        const isExpanded = menuBtn.getAttribute('aria-expanded') === 'true';
        menuBtn.setAttribute('aria-expanded', !isExpanded);
        dropdown.classList.toggle('show');
    });

    // Sự kiện ĐÓNG menu khi click ra BÊN NGOÀI
    document.addEventListener('click', function(event) {
        // Kiểm tra xem menu có đang mở VÀ người dùng có click ra ngoài cả nút và menu không
        if (dropdown.classList.contains('show') && !menuBtn.contains(event.target) && !dropdown.contains(event.target)) {
            menuBtn.setAttribute('aria-expanded', 'false');
            dropdown.classList.remove('show');
        }
    });
}
        // Auto-login function
        function attemptHeaderAutoLogin() {
            console.log("Checking for remembered account in header");

            const remembered = localStorage.getItem('joblink_remembered_account');
            if (!remembered) {
                console.log("No remembered account found");
                return;
            }

            try {
                const acc = JSON.parse(remembered);

                // Check expiration (14 days)
                const fourteenDays = 14 * 24 * 60 * 60 * 1000;
                if (Date.now() - acc.timestamp > fourteenDays) {
                    console.log("Remembered account expired");
                    localStorage.removeItem('joblink_remembered_account');
                    return;
                }

                // Check if already auto-logged in this session
                if (sessionStorage.getItem('joblink_auto_logged')) {
                    console.log("Already auto-logged in this session");
                    return;
                }

                // Skip if we're already on signin page
                if (window.location.pathname === '/signin') {
                    console.log("Already on signin page, skipping header auto-login");
                    return;
                }

                console.log("Attempting header auto-login for:", acc.email);

                // Show loading overlay
                const overlay = document.getElementById('autoLoginOverlay');
                if (overlay) {
                    overlay.style.display = 'flex';
                }

                // Create form data
                const formData = new FormData();
                formData.append('email', acc.email);
                formData.append('password', acc.password);
                formData.append('remember', 'on');

                // Submit auto-login
                fetch('/login', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    console.log("Header auto-login response status:", response.status);
                    if (response.redirected) {
                        console.log("Header auto-login successful, redirecting to:", response.url);
                        sessionStorage.setItem('joblink_auto_logged', '1');
                        window.location.href = response.url;
                    } else {
                        console.log("Header auto-login failed - no redirect");
                        if (overlay) overlay.style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error("Header auto-login error:", error);
                    if (overlay) overlay.style.display = 'none';
                });

            } catch (e) {
                console.error("Error parsing remembered account in header:", e);
                localStorage.removeItem('joblink_remembered_account');
            }
        }

        // Run on page load
        document.addEventListener('DOMContentLoaded', function() {
            // Initialize language first
            initializeLanguage();

            // Then attempt auto-login
            setTimeout(attemptHeaderAutoLogin, 200);
        });

        // Clear auto-logged flag when navigating to logout
        window.addEventListener('beforeunload', function() {
            if (window.location.href.includes('/logout')) {
                sessionStorage.removeItem('joblink_auto_logged');
            }
        });

document.addEventListener('DOMContentLoaded', function() {
    // 1. Khởi tạo đa ngôn ngữ
    initializeLanguage();

    // 2. Khởi tạo menu dropdown
    initializeDropdownMenu();

    // 3. Thử tự động đăng nhập (chạy sau một chút)
    setTimeout(attemptHeaderAutoLogin, 200);
});

// Clear auto-logged flag when navigating to logout
window.addEventListener('beforeunload', function() {
    if (window.location.href.includes('/logout')) {
        sessionStorage.removeItem('joblink_auto_logged');
    }
});