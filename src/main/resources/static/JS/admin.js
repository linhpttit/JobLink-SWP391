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
    fetch(url)
        .then(response => response.text())
        .then(html => {
            document.getElementById("content-area").innerHTML = html;
        })
        .catch(err => console.error("Lỗi khi load content:", err));
}