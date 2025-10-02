// File: /static/js/i18n.js
// Hệ thống đa ngôn ngữ cho toàn bộ website MyJob

// ===== BẢNG DỊCH TOÀN BỘ WEBSITE =====
const translations = {
    en: {
        // Header
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
        signing_in: "Signing you in automatically...",

        // Sign In Page
        signin_title: "Sign In",
        signin_subtitle: "Sign in to continue to MyJob",
        email_label: "Email",
        email_placeholder: "Enter your email",
        password_label: "Password",
        password_placeholder: "Enter your password",
        remember_me: "Remember me",
        forgot_password: "Forgot Password?",
        signin_button: "Sign In",
        no_account: "Don't have an account?",
        signup_link: "Sign Up",
        or_signin_with: "Or sign in with",

        // Sign Up Page
        signup_title: "Sign Up",
        signup_subtitle: "Create your MyJob account",
        fullname_label: "Full Name",
        fullname_placeholder: "Enter your full name",
        confirm_password_label: "Confirm Password",
        confirm_password_placeholder: "Confirm your password",
        signup_button: "Sign Up",
        have_account: "Already have an account?",
        signin_link: "Sign In",

        // Home Page
        hero_title: "Find Your Dream Job",
        hero_subtitle: "Discover thousands of opportunities from top companies",
        search_jobs: "Search Jobs",
        popular_categories: "Popular Categories",
        featured_jobs: "Featured Jobs",
        view_all: "View All",

        // Job Listing
        jobs_found: "jobs found",
        filter_by: "Filter by",
        job_type: "Job Type",
        location: "Location",
        salary_range: "Salary Range",
        experience: "Experience",
        full_time: "Full Time",
        part_time: "Part Time",
        remote: "Remote",
        apply_now: "Apply Now",
        save_job: "Save Job",

        // Job Detail
        job_description: "Job Description",
        requirements: "Requirements",
        responsibilities: "Responsibilities",
        benefits: "Benefits",
        about_company: "About Company",
        company_size: "Company Size",
        founded: "Founded",
        industry: "Industry",

        // Footer
        about_us: "About Us",
        contact: "Contact",
        privacy_policy: "Privacy Policy",
        terms_of_service: "Terms of Service",
        follow_us: "Follow Us",

        // Common
        loading: "Loading...",
        error: "Error",
        success: "Success",
        save: "Save",
        cancel: "Cancel",
        delete: "Delete",
        edit: "Edit",
        close: "Close",
        submit: "Submit",
        search: "Search",
        filter: "Filter",
        sort_by: "Sort by",
        newest: "Newest",
        oldest: "Oldest",
        salary_high_to_low: "Salary: High to Low",
        salary_low_to_high: "Salary: Low to High",

        // Index Page - Hero Section
        hero_title: "Find a job that suits your interest & skills.",
        hero_description: "Aliquam vitae turpis in diam convallis finibus in at risus. Nullam in scelerisque leo, eget sollicitudin velit bestibulum.",
        hero_search_job: "Job title, Keyword...",
        hero_search_location: "Your Location",
        hero_find_job: "Find Job",
        hero_suggestion: "Suggestion:",

        // Stats
        stat_live_job: "Live Job",
        stat_companies: "Companies",
        stat_candidates: "Candidates",
        stat_new_jobs: "New Jobs",

        // Categories
        section_popular_category: "Popular category",
        cat_graphics_design: "Graphics & Design",
        cat_code_programming: "Code & Programming",
        cat_digital_marketing: "Digital Marketing",
        cat_video_animation: "Video & Animation",
        cat_music_audio: "Music & Audio",
        cat_account_finance: "Account & Finance",
        cat_health_care: "Health & Care",
        cat_data_science: "Data & Science",
        cat_designer: "Designer",
        cat_programming: "Programming",
        cat_video: "Video",
        cat_animation: "Animation",
        open_position: "Open position",

        // Featured Jobs
        section_featured_job: "Featured job",
        job_senior_ux: "Senior UX Designer",
        job_software_engineer: "Software Engineer",
        job_junior_graphic: "Junior Graphic Designer",
        days_remaining: "Days Remaining",
        apply_now: "Apply Now",

        // Top Companies
        section_top_companies: "Top companies",
        location_us: "United States",
        location_australia: "Australia",
        location_china: "China",
        location_canada: "Canada",

        // CTA
        cta_become_candidate: "Become a Candidate",
        cta_candidate_desc: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras cursus a dolor convallis efficitur.",
        cta_become_employer: "Become a Employers",
        cta_employer_desc: "Cras in massa pellentesque, mollis ligula non, luctus dui. Morbi sed efficitur dolor. Pelque cursus risus, aliqu.",
        cta_register_now: "Register Now"
    },

    vi: {
        // Header
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
        signing_in: "Đang đăng nhập tự động...",

        // Sign In Page
        signin_title: "Đăng nhập",
        signin_subtitle: "Đăng nhập để tiếp tục sử dụng MyJob",
        email_label: "Email",
        email_placeholder: "Nhập email của bạn",
        password_label: "Mật khẩu",
        password_placeholder: "Nhập mật khẩu",
        remember_me: "Ghi nhớ đăng nhập",
        forgot_password: "Quên mật khẩu?",
        signin_button: "Đăng nhập",
        no_account: "Chưa có tài khoản?",
        signup_link: "Đăng ký",
        or_signin_with: "Hoặc đăng nhập với",

        // Sign Up Page
        signup_title: "Đăng ký",
        signup_subtitle: "Tạo tài khoản MyJob của bạn",
        fullname_label: "Họ và tên",
        fullname_placeholder: "Nhập họ và tên",
        confirm_password_label: "Xác nhận mật khẩu",
        confirm_password_placeholder: "Xác nhận mật khẩu",
        signup_button: "Đăng ký",
        have_account: "Đã có tài khoản?",
        signin_link: "Đăng nhập",

        // Home Page
        hero_title: "Tìm Công Việc Mơ Ước",
        hero_subtitle: "Khám phá hàng nghìn cơ hội từ các công ty hàng đầu",
        search_jobs: "Tìm việc",
        popular_categories: "Ngành nghề phổ biến",
        featured_jobs: "Việc làm nổi bật",
        view_all: "Xem tất cả",

        // Job Listing
        jobs_found: "việc làm",
        filter_by: "Lọc theo",
        job_type: "Loại hình",
        location: "Địa điểm",
        salary_range: "Mức lương",
        experience: "Kinh nghiệm",
        full_time: "Toàn thời gian",
        part_time: "Bán thời gian",
        remote: "Từ xa",
        apply_now: "Ứng tuyển",
        save_job: "Lưu tin",

        // Job Detail
        job_description: "Mô tả công việc",
        requirements: "Yêu cầu",
        responsibilities: "Trách nhiệm",
        benefits: "Quyền lợi",
        about_company: "Về công ty",
        company_size: "Quy mô",
        founded: "Thành lập",
        industry: "Ngành nghề",

        // Footer
        about_us: "Về chúng tôi",
        contact: "Liên hệ",
        privacy_policy: "Chính sách bảo mật",
        terms_of_service: "Điều khoản dịch vụ",
        follow_us: "Theo dõi chúng tôi",

        // Common
        loading: "Đang tải...",
        error: "Lỗi",
        success: "Thành công",
        save: "Lưu",
        cancel: "Hủy",
        delete: "Xóa",
        edit: "Sửa",
        close: "Đóng",
        submit: "Gửi",
        search: "Tìm kiếm",
        filter: "Lọc",
        sort_by: "Sắp xếp theo",
        newest: "Mới nhất",
        oldest: "Cũ nhất",
        salary_high_to_low: "Lương: Cao đến thấp",
        salary_low_to_high: "Lương: Thấp đến cao",

        // Index Page - Hero Section
        hero_title: "Tìm công việc phù hợp với sở thích & kỹ năng của bạn.",
        hero_description: "Khám phá hàng nghìn cơ hội việc làm từ các công ty hàng đầu. Tìm kiếm công việc mơ ước của bạn ngay hôm nay.",
        hero_search_job: "Tiêu đề công việc, Từ khóa...",
        hero_search_location: "Địa điểm của bạn",
        hero_find_job: "Tìm việc",
        hero_suggestion: "Gợi ý:",

        // Stats
        stat_live_job: "Việc làm đang tuyển",
        stat_companies: "Công ty",
        stat_candidates: "Ứng viên",
        stat_new_jobs: "Việc mới",

        // Categories
        section_popular_category: "Ngành nghề phổ biến",
        cat_graphics_design: "Thiết kế đồ họa",
        cat_code_programming: "Lập trình",
        cat_digital_marketing: "Marketing số",
        cat_video_animation: "Video & Hoạt hình",
        cat_music_audio: "Âm nhạc & Âm thanh",
        cat_account_finance: "Kế toán & Tài chính",
        cat_health_care: "Y tế & Chăm sóc",
        cat_data_science: "Dữ liệu & Khoa học",
        cat_designer: "Thiết kế",
        cat_programming: "Lập trình",
        cat_video: "Video",
        cat_animation: "Hoạt hình",
        open_position: "Vị trí tuyển dụng",

        // Featured Jobs
        section_featured_job: "Việc làm nổi bật",
        job_senior_ux: "Thiết kế UX cấp cao",
        job_software_engineer: "Kỹ sư phần mềm",
        job_junior_graphic: "Thiết kế đồ họa Junior",
        days_remaining: "Ngày còn lại",
        apply_now: "Ứng tuyển ngay",

        // Top Companies
        section_top_companies: "Công ty hàng đầu",
        location_us: "Hoa Kỳ",
        location_australia: "Úc",
        location_china: "Trung Quốc",
        location_canada: "Canada",

        // CTA
        cta_become_candidate: "Trở thành ứng viên",
        cta_candidate_desc: "Tạo hồ sơ của bạn và bắt đầu tìm kiếm công việc mơ ước. Kết nối với hàng nghìn nhà tuyển dụng hàng đầu.",
        cta_become_employer: "Trở thành nhà tuyển dụng",
        cta_employer_desc: "Đăng tin tuyển dụng và tìm kiếm ứng viên tài năng cho công ty của bạn. Tiếp cận hàng triệu ứng viên tiềm năng.",
        cta_register_now: "Đăng ký ngay"
    }
};

// ===== HÀM ÁP DỤNG DỊCH CHO TRANG =====
function applyTranslations(lang) {
    // Dịch các phần tử có data-i18n (text content)
    const elements = document.querySelectorAll('[data-i18n]');
    elements.forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang] && translations[lang][key]) {
            element.textContent = translations[lang][key];
        }
    });

    // Dịch các placeholder
    const placeholderElements = document.querySelectorAll('[data-i18n-placeholder]');
    placeholderElements.forEach(element => {
        const key = element.getAttribute('data-i18n-placeholder');
        if (translations[lang] && translations[lang][key]) {
            element.placeholder = translations[lang][key];
        }
    });

    // Dịch các title attribute
    const titleElements = document.querySelectorAll('[data-i18n-title]');
    titleElements.forEach(element => {
        const key = element.getAttribute('data-i18n-title');
        if (translations[lang] && translations[lang][key]) {
            element.title = translations[lang][key];
        }
    });

    // Dịch các aria-label
    const ariaElements = document.querySelectorAll('[data-i18n-aria]');
    ariaElements.forEach(element => {
        const key = element.getAttribute('data-i18n-aria');
        if (translations[lang] && translations[lang][key]) {
            element.setAttribute('aria-label', translations[lang][key]);
        }
    });

    // Cập nhật thuộc tính lang của HTML
    document.documentElement.lang = lang;

    // Trigger event để các component khác biết ngôn ngữ đã thay đổi
    window.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang } }));

    console.log(`Language changed to: ${lang}`);
}

// ===== HÀM THAY ĐỔI NGÔN NGỮ =====
function changeLanguage(lang) {
    // Lưu lựa chọn vào localStorage
    localStorage.setItem('joblink_language', lang);

    // Cập nhật cờ và text trong header (nếu có)
    updateLanguageSelector(lang);

    // Áp dụng dịch cho toàn bộ trang
    applyTranslations(lang);
}

// ===== CẬP NHẬT LANGUAGE SELECTOR =====
function updateLanguageSelector(lang) {
    const flagImg = document.getElementById('currentLangFlag');
    const langText = document.getElementById('currentLangText');

    if (flagImg && langText) {
        if (lang === 'vi') {
            flagImg.src = 'https://flagcdn.com/vn.svg';
            langText.textContent = 'Tiếng Việt';
        } else {
            flagImg.src = 'https://flagcdn.com/us.svg';
            langText.textContent = 'English';
        }
    }
}

// ===== KHỞI TẠO NGÔN NGỮ KHI TẢI TRANG =====
function initializeLanguage() {
    const savedLang = localStorage.getItem('joblink_language') || 'en';
    changeLanguage(savedLang);
}

// ===== LẤY NGÔN NGỮ HIỆN TẠI =====
function getCurrentLanguage() {
    return localStorage.getItem('joblink_language') || 'en';
}

// ===== LẤY BẢN DỊCH THEO KEY =====
function t(key) {
    const lang = getCurrentLanguage();
    return translations[lang]?.[key] || key;
}

// ===== THÊM BẢN DỊCH MỚI (Để mở rộng) =====
function addTranslations(newTranslations) {
    Object.keys(newTranslations).forEach(lang => {
        if (!translations[lang]) {
            translations[lang] = {};
        }
        Object.assign(translations[lang], newTranslations[lang]);
    });
}

// ===== TỰ ĐỘNG KHỞI TẠO KHI DOM READY =====
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeLanguage);
} else {
    initializeLanguage();
}

// ===== EXPORT ĐỂ SỬ DỤNG GLOBAL =====
window.i18n = {
    changeLanguage,
    getCurrentLanguage,
    applyTranslations,
    t,
    addTranslations,
    translations
};
// File: /static/js/i18n.js  (THAY THẾ / GỘP với file hiện tại của bạn)
// giữ nguyên object translations (bạn đã có). Mình giả sử translations đã được định nghĩa ở đầu file.
// ---------- nếu bạn đã có translations, bỏ phần khai báo translations ở đây -----------
// const translations = { en: {...}, vi: {...} };
// ------------------------------------------------------------------------------

/* ---------- HELPER: get/set language ---------- */
function getCurrentLanguage() {
    return localStorage.getItem('joblink_language') || 'en';
}
function setCurrentLanguage(lang) {
    localStorage.setItem('joblink_language', lang);
}

/* ---------- APPLY TRANSLATIONS (static keys) ---------- */
function applyTranslations(lang) {
    // text content elements
    const elements = document.querySelectorAll('[data-i18n]');
    elements.forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang] && translations[lang][key]) {
            element.textContent = translations[lang][key];
        }
    });

    // placeholders
    const placeholderElements = document.querySelectorAll('[data-i18n-placeholder]');
    placeholderElements.forEach(element => {
        const key = element.getAttribute('data-i18n-placeholder');
        if (translations[lang] && translations[lang][key]) {
            element.placeholder = translations[lang][key];
        }
    });

    // title attributes
    const titleElements = document.querySelectorAll('[data-i18n-title]');
    titleElements.forEach(element => {
        const key = element.getAttribute('data-i18n-title');
        if (translations[lang] && translations[lang][key]) {
            element.title = translations[lang][key];
        }
    });

    // aria-label
    const ariaElements = document.querySelectorAll('[data-i18n-aria]');
    ariaElements.forEach(element => {
        const key = element.getAttribute('data-i18n-aria');
        if (translations[lang] && translations[lang][key]) {
            element.setAttribute('aria-label', translations[lang][key]);
        }
    });

    // html lang attribute
    document.documentElement.lang = lang;

    // event so other components can react
    window.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang } }));

    console.log(`Language changed to: ${lang}`);
}

/* ---------- UPDATE SELECTOR UI ---------- */
function updateLanguageSelectorUI(lang) {
    const flagImg = document.getElementById('currentLangFlag');
    const langText = document.getElementById('currentLangText');

    if (flagImg && langText) {
        if (lang === 'vi') {
            flagImg.src = 'https://flagcdn.com/vn.svg';
            langText.textContent = 'Tiếng Việt';
        } else {
            flagImg.src = 'https://flagcdn.com/us.svg';
            langText.textContent = 'English';
        }
    }
}

/* ---------- PUBLIC: changeLanguage ---------- */
function changeLanguage(lang) {
    setCurrentLanguage(lang);
    updateLanguageSelectorUI(lang);
    applyTranslations(lang);
}

/* ---------- INIT ---------- */
function initializeLanguage() {
    const saved = getCurrentLanguage();
    changeLanguage(saved);
}

/* ---------- TỰ ĐỘNG DỊCH NỘI DUNG MỚI: MutationObserver ---------- */
const mutationObserverConfig = { childList: true, subtree: true, characterData: true };

function translateElementIfNeeded(el, lang) {
    // 1) nếu element có key data-i18n => dịch theo key
    if (el.nodeType === Node.ELEMENT_NODE) {
        if (el.hasAttribute && el.hasAttribute('data-i18n')) {
            const key = el.getAttribute('data-i18n');
            if (translations[lang] && translations[lang][key]) {
                el.textContent = translations[lang][key];
                return;
            }
        }
        // placeholder
        if (el.hasAttribute && el.hasAttribute('data-i18n-placeholder')) {
            const key = el.getAttribute('data-i18n-placeholder');
            if (translations[lang] && translations[lang][key]) {
                el.placeholder = translations[lang][key];
                return;
            }
        }
        // If element has attribute data-auto-translate="true" -> translate its textContent (arbitrary text)
        if (el.hasAttribute && el.getAttribute('data-auto-translate') === 'true') {
            const original = el.dataset.originalText || el.textContent;
            // store original if not already
            if (!el.dataset.originalText) el.dataset.originalText = original;
            // call async translator
            translateTextAPI(original, getCurrentLanguage())
                .then(translated => {
                    if (translated) el.textContent = translated;
                })
                .catch(err => {
                    console.warn('Auto-translate failed:', err);
                });
            return;
        }
    }

    // 2) For text nodes without keys: attempt to translate if parent has data-auto-translate
    if (el.nodeType === Node.TEXT_NODE) {
        const parent = el.parentElement;
        if (parent && parent.getAttribute && parent.getAttribute('data-auto-translate') === 'true') {
            const original = el.data.trim();
            if (!original) return;
            // avoid translating supplied keys or code blocks
            translateTextAPI(original, getCurrentLanguage())
                .then(translated => {
                    if (translated) el.data = translated;
                })
                .catch(err => console.warn('Text node translate failed', err));
        }
    }
}

const observer = new MutationObserver((mutationsList) => {
    const lang = getCurrentLanguage();
    for (const mutation of mutationsList) {
        if (mutation.type === 'childList') {
            // new nodes added
            mutation.addedNodes.forEach(node => {
                // if element node -> translate subtree
                if (node.nodeType === Node.ELEMENT_NODE) {
                    node.querySelectorAll('*').forEach(child => translateElementIfNeeded(child, lang));
                    translateElementIfNeeded(node, lang); // also for the root added node
                } else {
                    translateElementIfNeeded(node, lang);
                }
            });
        } else if (mutation.type === 'characterData') {
            // text content changed
            translateElementIfNeeded(mutation.target, lang);
        }
    }
});

/* start observing document.body after DOM ready */
function startAutoObserve() {
    if (document.body && !window.__i18n_observer_started) {
        observer.observe(document.body, mutationObserverConfig);
        window.__i18n_observer_started = true;
    }
}

/* ---------- ARBITRARY TEXT TRANSLATION (calls external API) ---------- */
/* Example uses LibreTranslate public endpoint. Replace with your own server-side proxy in production. */

async function translateTextAPI(text, targetLang) {
    if (!text || !text.trim()) return '';
    const mappedTarget = targetLang === 'vi' ? 'vi' : 'en'; // simple mapping
    // If the text is likely already in target language, you might want to skip — but we skip that optimization here.
    try {
        // NOTE: PUBLIC libretranslate instance may have rate limits. For production, set up a server-side proxy + API key.
        const res = await fetch('https://libretranslate.de/translate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                q: text,
                source: 'auto',
                target: mappedTarget,
                format: 'text'
            })
        });

        if (!res.ok) {
            console.warn('Translate API non-ok', res.status);
            return null;
        }
        const data = await res.json();
        return data.translatedText;
    } catch (err) {
        console.error('translateTextAPI error', err);
        return null;
    }
}

/* ---------- UTILITY: translate newly appended chat message --------- */
/* Example usage: when your app appends a user message node, mark it with data-auto-translate="true"
   and the MutationObserver + translateElementIfNeeded will call the API and replace content.
   Alternatively, you can directly call translateTextAPI and set innerText yourself.
*/

/* ---------- BẮT EVENT languageChanged (để re-translate nội dung động khi user đổi language) ---------- */
window.addEventListener('languageChanged', (ev) => {
    const lang = ev.detail.lang;
    // re-apply static translations
    applyTranslations(lang);

    // re-translate any elements marked with data-auto-translate
    document.querySelectorAll('[data-auto-translate="true"]').forEach(el => {
        const original = el.dataset.originalText || el.textContent;
        if (!el.dataset.originalText) el.dataset.originalText = original;
        translateTextAPI(original, lang).then(t => {
            if (t) el.textContent = t;
        });
    });
});

/* ---------- INTERCEPT FORM SUBMIT or new user message append ----------
   If you want to translate user input *before* sending to server (not recommended for privacy),
   you can call translateTextAPI on blur/submit and use translated text.
   Better approach: send original to server, and show translated copy locally or store translated text.
*/

/* Example helper: translateAndShowForNewMessage(container, messageText)
   Append message DOM with data-auto-translate="true" and original in data-original-text to be auto-translated.
*/
function appendTranslatedMessage(containerSelector, messageText, opts = {}) {
    const container = document.querySelector(containerSelector);
    if (!container) return;
    const wrapper = document.createElement('div');
    wrapper.className = opts.className || 'message';
    wrapper.setAttribute('data-auto-translate', 'true');
    wrapper.dataset.originalText = messageText;
    wrapper.textContent = messageText; // show original immediately
    container.appendChild(wrapper);
    // MutationObserver will pick it up and translate.
}

/* ---------- BOOTSTRAP ON DOMContentLoaded ---------- */
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        initializeLanguage();
        startAutoObserve();
    });
} else {
    initializeLanguage();
    startAutoObserve();
}

/* ---------- EXPORT to window for global use ---------- */
window.i18n = {
    changeLanguage,
    getCurrentLanguage,
    applyTranslations,
    translations,
    translateTextAPI,
    appendTranslatedMessage
};
