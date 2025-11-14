document.addEventListener("DOMContentLoaded", () => {
    const menuToggle = document.getElementById("menuToggle");
    const sidebar = document.querySelector(".sidebar");
    const navItems = document.querySelectorAll(".nav-item");

    // Ẩn/hiện sidebar
    menuToggle.addEventListener("click", () => {
        sidebar.classList.toggle("collapsed");
        document.querySelector(".main-content")?.classList.toggle("expanded");
    });

    // Click menu để đổi active + load content
    navItems.forEach(item => {
        item.addEventListener("click", e => {
            e.preventDefault();

            // Xóa active ở menu
            navItems.forEach(i => i.classList.remove("active"));
            item.classList.add("active");

            // Lấy url từ thuộc tính data-url
            const url = item.getAttribute("data-url");
            if (url) {
                loadContent(url);
            }
        });
    });

    // Auto load Dashboard khi mở trang (use admin endpoint)
    loadContent("/admin/dashboard");

    // Event delegation for dynamic content
    document.addEventListener('click', function(e) {
        console.log('Click detected on:', e.target);
        
        // Handle pagination clicks
        if (e.target.closest('.pagination-link')) {
            console.log('Pagination link clicked');
            e.preventDefault();
            const page = e.target.closest('.pagination-link').getAttribute('data-page');
            if (page !== null) {
                loadPaymentPage(parseInt(page));
            }
        }

        // Handle filter button clicks
        if (e.target.closest('.btn-filter')) {
            console.log('Filter button clicked');
            e.preventDefault();
            filterPayments();
        }

        // Handle clear filter button clicks
        if (e.target.closest('.btn-clear')) {
            console.log('Clear button clicked');
            e.preventDefault();
            clearFilters();
        }
    });
});

// Toggle notification dropdown
function toggleNotifications() {
    document.getElementById("notificationDropdown").classList.toggle("show");
}

// Ẩn dropdown khi click ra ngoài
window.addEventListener("click", (event) => {
    if (!event.target.closest('.notification-icon')) {
        document.getElementById("notificationDropdown").classList.remove("show");
    }
});

// Hàm load nội dung động
function loadContent(url) {
    fetch(url, {
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => response.text())
        .then(html => {
            document.getElementById("content-area").innerHTML = html;
            // Execute any scripts in the loaded content
            executeScriptsInContent();
        })
        .catch(err => console.error("Lỗi khi load content:", err));
}

// Execute scripts in dynamically loaded content
function executeScriptsInContent() {
    const scripts = document.getElementById("content-area").querySelectorAll("script");
    scripts.forEach(script => {
        const newScript = document.createElement("script");
        if (script.src) {
            newScript.src = script.src;
        } else {
            newScript.textContent = script.textContent;
        }
        document.head.appendChild(newScript);
        document.head.removeChild(newScript);
    });
}

// Payment History Functions - Global scope
window.filterPayments = function() {
    console.log('filterPayments called');
    
    const searchEl = document.getElementById('search');
    const statusEl = document.getElementById('paymentStatus');
    const tierEl = document.getElementById('tierLevel');
    const methodEl = document.getElementById('paymentMethod');
    
    console.log('Elements found:', {
        search: searchEl,
        status: statusEl,
        tier: tierEl,
        method: methodEl
    });
    
    const filterData = {
        search: searchEl?.value || '',
        paymentStatus: statusEl?.value || '',
        tierLevel: tierEl?.value ? parseInt(tierEl.value) : null,
        paymentMethod: methodEl?.value || ''
    };
    
    console.log('Filter data:', filterData);
    loadPaymentPage(0, filterData);
};

window.clearFilters = function() {
    document.getElementById('search').value = '';
    document.getElementById('paymentStatus').value = '';
    document.getElementById('tierLevel').value = '';
    document.getElementById('paymentMethod').value = '';
    loadPaymentPage(0);
};

window.loadPaymentPage = function(page, filters = {}) {
    console.log('loadPaymentPage called with:', { page, filters });
    
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', 10);
    
    if (filters.search) params.append('search', filters.search);
    if (filters.paymentStatus) params.append('paymentStatus', filters.paymentStatus);
    if (filters.tierLevel !== null && filters.tierLevel !== undefined) params.append('tierLevel', filters.tierLevel);
    if (filters.paymentMethod) params.append('paymentMethod', filters.paymentMethod);
    
    const url = '/admin/payments?' + params.toString();
    console.log('Request URL:', url);
    
    fetch(url, {
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => {
            console.log('Response status:', response.status);
            return response.text();
        })
        .then(html => {
            console.log('Response received, updating content');
            document.getElementById('content-area').innerHTML = html;
        })
        .catch(err => {
            console.error('Error loading payment page:', err);
        });
};