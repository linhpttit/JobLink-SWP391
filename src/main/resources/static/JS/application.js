document.addEventListener("DOMContentLoaded", function () {
    // ====== DANH SÁCH TRẠNG THÁI ======
    const STATUS_OPTIONS = [
        { name: "Chấp nhận", class: "green", icon: "fas fa-check-circle" },
        { name: "Từ chối", class: "red", icon: "fas fa-times-circle" },
    ];

    // ====== STATUS DROPDOWN ======
    function populateStatusMenu(menuElement) {
        let html = "";
        STATUS_OPTIONS.forEach(status => {
            html += `
                <div class="status-option" data-status-name="${status.name}" data-status-class="${status.class}">
                    <i class="${status.icon}"></i> ${status.name}
                </div>
            `;
        });
        menuElement.innerHTML = html;
    }

    document.querySelectorAll(".status-dropdown-container").forEach(container => {
        const toggle = container.querySelector(".status-toggle");
        const menu = container.querySelector(".status-menu");
        populateStatusMenu(menu);

        toggle.addEventListener("click", e => {
            e.stopPropagation();
            document.querySelectorAll(".status-menu.show, .dropdown-menu.show").forEach(openMenu => {
                if (openMenu !== menu) openMenu.classList.remove("show");
            });
            menu.classList.toggle("show");
        });

        menu.addEventListener("click", e => {
            const option = e.target.closest(".status-option");
            if (!option) return;
            const newStatus = option.dataset.statusName;
            const newClass = option.dataset.statusClass;
            const row = toggle.closest("tr");
            const candidateName = row.getAttribute("data-candidate-name");

            toggle.textContent = newStatus;
            toggle.className = `status-badge status-toggle status-${newClass}`;
            menu.classList.remove("show");

            console.log(`✅ ${candidateName} → ${newStatus}`);
            alert(`Đã cập nhật trạng thái cho ${candidateName} thành "${newStatus}"!`);
        });
    });

    // Click ngoài đóng menu
    document.addEventListener("click", e => {
        if (!e.target.closest(".status-dropdown-container")) {
            document.querySelectorAll(".status-menu.show").forEach(m => m.classList.remove("show"));
        }
    });

    // ====== ACTION DROPDOWN (3 chấm) ======
    document.querySelectorAll(".action-dropdown").forEach(dropdown => {
        const toggle = dropdown.querySelector(".action-toggle");
        const menu = dropdown.querySelector(".dropdown-menu");

        toggle.addEventListener("click", e => {
            e.stopPropagation();
            document.querySelectorAll(".dropdown-menu.show").forEach(openMenu => {
                if (openMenu !== menu) openMenu.classList.remove("show");
            });
            document.querySelectorAll(".status-menu.show").forEach(m => m.classList.remove("show"));
            menu.classList.toggle("show");
        });

        document.addEventListener("click", e => {
            if (!dropdown.contains(e.target)) menu.classList.remove("show");
        });
    });

    // ====== ACTION: VIEW CV ======
    document.querySelectorAll(".action-view-cv").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            const row = link.closest("tr");
            const name = row.getAttribute("data-candidate-name");
            alert(`Mô phỏng: Đang mở CV của ${name}...`);
            link.closest(".dropdown-menu").classList.remove("show");
        });
    });

    // ====== ACTION: SEND MAIL ======
    document.querySelectorAll(".action-send-mail").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            const name = link.closest("tr").getAttribute("data-candidate-name");
            alert(`Mô phỏng: Đang mở mail đến ${name}...`);
            link.closest(".dropdown-menu").classList.remove("show");
        });
    });

    // ====== BOOKMARK ICON ======
    document.querySelectorAll(".bookmark-icon").forEach(icon => {
        icon.addEventListener("click", () => {
            if (icon.classList.contains("fas")) {
                icon.classList.remove("fas", "saved");
                icon.classList.add("far");
            } else {
                icon.classList.remove("far");
                icon.classList.add("fas", "saved");
            }
        });
    });

    // ====== FILTER STATUS ======
    const statusFilter = document.getElementById("status-filter");
    if (statusFilter) {
        statusFilter.addEventListener("change", () => {
            const selected = statusFilter.value.toLowerCase();
            document.querySelectorAll(".candidate-table tbody tr").forEach(row => {
                const status = row.querySelector(".status-badge")?.textContent.toLowerCase();
                row.style.display = selected === "" || status.includes(selected) ? "" : "none";
            });
        });
    }

    // ====== SEARCH BOX (Chức năng tìm kiếm bình thường) ======
        const searchInput = document.querySelector(".search-box .search-input"); // Lấy input với class mới
        if (searchInput) {
            searchInput.addEventListener("keyup", () => {
                const keyword = searchInput.value.toLowerCase().trim();
                document.querySelectorAll(".candidate-table tbody tr").forEach(row => {
                    const name = row.querySelector(".candidate-name")?.textContent.toLowerCase();
                    const position = row.querySelector("td:nth-child(2)")?.textContent.toLowerCase();
                    // Lọc hàng dựa trên tên hoặc vị trí
                    const isMatch = name.includes(keyword) || position.includes(keyword);
                    // Áp dụng bộ lọc trạng thái nếu có
                    const statusFilter = document.getElementById("status-filter")?.value.toLowerCase() || "";
                    const rowStatus = row.querySelector(".status-badge")?.textContent.toLowerCase();
                    const isStatusMatch = statusFilter === "" || rowStatus.includes(statusFilter);

                    row.style.display = (isMatch && isStatusMatch) ? "" : "none";
                });
            });
        }

    // ====== MODAL LOADER (model.html) ======
   // ====== MODAL LOADER (model.html) ======
const modalOverlay = document.getElementById("modal-overlay");
const modalContent = document.getElementById("modal-content");
const dashboard = document.getElementById("dashboard-content"); // Lấy dashboard
const searchBox = document.querySelector(".search-box"); // Vẫn dùng search-box như trigger

if (searchBox && modalOverlay && modalContent) {
    searchBox.addEventListener("click", async () => {
        // MỞ MODAL
        modalOverlay.classList.remove("hidden"); // Giả định dùng class "hidden"
        dashboard?.classList.add("blurred"); // THÊM CLASS LÀM MỜ NỀN

        try {
            const res = await fetch("model.html");
            modalContent.innerHTML = await res.text();
        } catch (err) {
            modalContent.innerHTML = "<p>Lỗi: Không thể tải model.html</p>";
        }
    });

    modalOverlay.addEventListener("click", e => {
        // ĐÓNG MODAL
        if (e.target.classList.contains("modal-background") || e.target === modalOverlay) {
            modalOverlay.classList.add("hidden");
            dashboard?.classList.remove("blurred"); // GỠ CLASS LÀM MỜ NỀN
        }
    });
}
});