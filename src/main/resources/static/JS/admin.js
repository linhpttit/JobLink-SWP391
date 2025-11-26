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
    const dropdown = document.getElementById("notificationDropdown");
    if (dropdown) {
        dropdown.classList.toggle("show");
    }
}

// Ẩn dropdown khi click ra ngoài
window.addEventListener("click", (event) => {
    if (!event.target.closest('.notification-icon')) {
        const dropdown = document.getElementById("notificationDropdown");
        if (dropdown) {
            dropdown.classList.remove("show");
        }
    }
});

// Hàm load nội dung động
function loadContent(url) {
    console.log("📥 Đang load content từ:", url);
    fetch(url)
        .then(response => response.text())
        .then(html => {
            const contentArea = document.getElementById("content-area");
            contentArea.innerHTML = html;
            console.log("✅ Đã inject HTML vào content-area");
            
            // QUAN TRỌNG: Execute script tags theo thứ tự, chờ external scripts load xong
            (async () => {
                const scripts = Array.from(contentArea.querySelectorAll('script'));
                console.log(`🔧 Tìm thấy ${scripts.length} script tag(s) trong content`);
                for (let i = 0; i < scripts.length; i++) {
                    const oldScript = scripts[i];
                    console.log(`🔧 Đang execute script ${i + 1}/${scripts.length}`);
                    const newScript = document.createElement('script');

                    // Copy attributes
                    Array.from(oldScript.attributes).forEach(attr => {
                        newScript.setAttribute(attr.name, attr.value);
                    });

                    if (oldScript.src) {
                        // External script: wait for load before proceeding
                        await new Promise((resolve, reject) => {
                            newScript.onload = resolve;
                            newScript.onerror = reject;
                            newScript.src = oldScript.src;
                            oldScript.parentNode.replaceChild(newScript, oldScript);
                        }).catch(err => console.error('❌ Lỗi load script:', oldScript.src, err));
                    } else {
                        // Inline script executes immediately when inserted
                        newScript.textContent = oldScript.textContent;
                        oldScript.parentNode.replaceChild(newScript, oldScript);
                    }
                    console.log(`✅ Đã execute script ${i + 1}`);
                }
                
                // Đợi một chút để đảm bảo scripts đã chạy xong
                await new Promise(resolve => setTimeout(resolve, 100));
                
                // Trigger custom event để các script khác biết content đã load xong
                contentArea.dispatchEvent(new CustomEvent('contentLoaded', { detail: { url } }));
                console.log("📢 Đã dispatch 'contentLoaded' event");
            })();

            // Sau khi content và scripts được load xong
        })
        .catch(err => console.error("❌ Lỗi khi load content:", err));
}