// Range Slider Functionality
function updateRangeColor() {
    const range = document.getElementById('experience-range');
    if (!range) return;

    const value = range.value;
    const percentage = (value / range.max) * 100;
    const rangeValue = document.getElementById('range-value');

    range.style.setProperty('--fill-percentage', percentage + '%');
    if (rangeValue) {
        rangeValue.innerHTML = `<span>${value}</span> năm`;
    }

    const maxExperienceInput = document.getElementById('maxExperience');
    if (maxExperienceInput) {
        maxExperienceInput.value = value;
    }
}

// Filter Toggle
function initializeFilterToggle() {
    const toggleButton = document.getElementById('toggle-filters');
    if (!toggleButton) return;

    toggleButton.addEventListener('click', function() {
        const filtersPanel = document.getElementById('filters-panel');
        const filterText = this.querySelector('span');
        const isHidden = !filtersPanel.classList.contains('show');

        if (isHidden) {
            filtersPanel.classList.add('show');
            filterText.textContent = 'Hide Filters';
        } else {
            filtersPanel.classList.remove('show');
            filterText.textContent = 'Show Filters';
        }
    });
}

// Clear Filters
function clearFilters() {
    window.location.href = '/applications';
}

// ========== BOOKMARK MANAGEMENT ==========
function saveBookmarkState(applicationId, isBookmarked) {
    try {
        const bookmarks = getBookmarks();
        if (isBookmarked) {
            bookmarks[applicationId] = true;
        } else {
            delete bookmarks[applicationId];
        }
        localStorage.setItem('employerBookmarks', JSON.stringify(bookmarks));
    } catch (error) {
        console.error('Error saving bookmark state:', error);
    }
}

function getBookmarks() {
    try {
        return JSON.parse(localStorage.getItem('employerBookmarks') || '{}');
    } catch (error) {
        console.error('Error getting bookmarks:', error);
        return {};
    }
}

function syncBookmarkStates() {
    try {
        const bookmarks = getBookmarks();
        document.querySelectorAll('.bookmark-btn').forEach(button => {
            const applicationId = button.getAttribute('data-app-id');
            const icon = button.querySelector('i');

            if (applicationId && bookmarks[applicationId]) {
                // Đã được bookmark - cập nhật UI
                icon.className = 'ri-bookmark-fill';
                button.innerHTML = '<i class="ri-bookmark-fill"></i> Bỏ lưu';
            } else {
                // Chưa được bookmark
                icon.className = 'ri-bookmark-line';
                button.innerHTML = '<i class="ri-bookmark-line"></i> Lưu';
            }
        });
    } catch (error) {
        console.error('Error syncing bookmark states:', error);
    }
}

// ========== STATUS MANAGEMENT ==========
function saveStatusState(applicationId, status) {
    try {
        const statuses = getStatuses();
        statuses[applicationId] = status;
        localStorage.setItem('employerStatuses', JSON.stringify(statuses));
    } catch (error) {
        console.error('Error saving status state:', error);
    }
}

function getStatuses() {
    try {
        return JSON.parse(localStorage.getItem('employerStatuses') || '{}');
    } catch (error) {
        console.error('Error getting statuses:', error);
        return {};
    }
}

function syncStatusStates() {
    try {
        const statuses = getStatuses();
        document.querySelectorAll('.status-dropdown').forEach(dropdown => {
            const form = dropdown.closest('.status-form');
            if (!form) return;

            const action = form.getAttribute('action');
            const match = action?.match(/\/applications\/(\d+)\/status/);
            const applicationId = match?.[1];

            if (applicationId && statuses[applicationId]) {
                dropdown.value = statuses[applicationId];
                updateStatusUI(dropdown, statuses[applicationId]);
            }
        });
    } catch (error) {
        console.error('Error syncing status states:', error);
    }
}

// ========== SELECTION MANAGEMENT ==========
function initializeSelectionManagement() {
    const selectAllCheckbox = document.getElementById('select-all');
    const headerCheckbox = document.getElementById('header-checkbox');
    const candidateCheckboxes = document.querySelectorAll('.candidate-checkbox');
    const bulkSendButton = document.getElementById('bulk-send-email');
    const selectedCount = document.getElementById('selected-count');

    function updateSelectionState() {
        const selectedCountValue = document.querySelectorAll('.candidate-checkbox:checked').length;

        if (selectedCount) {
            selectedCount.textContent = `${selectedCountValue} selected`;
        }

        if (bulkSendButton) {
            bulkSendButton.disabled = selectedCountValue === 0;
        }

        const allChecked = selectedCountValue === candidateCheckboxes.length;
        const someChecked = selectedCountValue > 0 && selectedCountValue < candidateCheckboxes.length;

        if (selectAllCheckbox) {
            selectAllCheckbox.checked = allChecked;
            selectAllCheckbox.indeterminate = someChecked;
        }

        if (headerCheckbox) {
            headerCheckbox.checked = allChecked;
            headerCheckbox.indeterminate = someChecked;
        }
    }

    // Select all functionality
    selectAllCheckbox?.addEventListener('change', function() {
        const isChecked = this.checked;
        candidateCheckboxes.forEach(checkbox => checkbox.checked = isChecked);
        updateSelectionState();
    });

    headerCheckbox?.addEventListener('change', function() {
        const isChecked = this.checked;
        candidateCheckboxes.forEach(checkbox => checkbox.checked = isChecked);
        updateSelectionState();
    });

    candidateCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', updateSelectionState);
    });

    updateSelectionState();
}

// ========== BULK EMAIL MODAL ==========
function initializeBulkEmailModal() {
    const bulkSendButton = document.getElementById('bulk-send-email');
    const bulkEmailModal = document.getElementById('bulk-email-modal');
    const modalClose = document.getElementById('modal-close');
    const cancelBulkEmail = document.getElementById('cancel-bulk-email');
    const confirmBulkEmail = document.getElementById('confirm-bulk-email');
    const selectedCandidatesCount = document.getElementById('selected-candidates-count');
    const selectedCandidatesList = document.getElementById('selected-candidates-list');

    if (!bulkSendButton || !bulkEmailModal) return;

    function openModal() {
        const selectedCandidates = Array.from(document.querySelectorAll('.candidate-checkbox:checked'));

        if (selectedCandidates.length === 0) {
            showNotification('Please select at least one candidate', 'error');
            return;
        }

        selectedCandidatesCount.textContent = selectedCandidates.length;

        selectedCandidatesList.innerHTML = '';
        selectedCandidates.forEach(checkbox => {
            const candidateItem = document.createElement('div');
            candidateItem.className = 'selected-candidate-item';
            candidateItem.innerHTML = `
                <div>
                    <strong>${checkbox.getAttribute('data-name')}</strong>
                    <div class="candidate-email">${checkbox.getAttribute('data-email')}</div>
                </div>
            `;
            selectedCandidatesList.appendChild(candidateItem);
        });

        bulkEmailModal.classList.add('active');
    }

    function closeModal() {
        bulkEmailModal.classList.remove('active');
    }

    function sendBulkEmail() {
        const selectedCandidates = Array.from(document.querySelectorAll('.candidate-checkbox:checked'));

        if (selectedCandidates.length === 0) {
            showNotification('No candidates selected', 'error');
            return;
        }

        const emailAddresses = selectedCandidates.map(checkbox => checkbox.getAttribute('data-email'));

        const subject = `📩 Thư mời phỏng vấn – Vị trí [Tên vị trí] tại [Tên công ty]`;
        const body = `
👋 Kính gửi [Tên ứng viên],

Cảm ơn bạn đã quan tâm và gửi hồ sơ ứng tuyển cho vị trí [Tên vị trí] tại [Tên công ty]. Sau khi xem xét hồ sơ, chúng tôi nhận thấy kỹ năng và kinh nghiệm của bạn rất phù hợp với yêu cầu tuyển dụng hiện tại.

Chúng tôi trân trọng mời bạn tham dự 💬 buổi phỏng vấn để trao đổi thêm về công việc và cơ hội phát triển tại công ty.

🗓️ Thông tin buổi phỏng vấn:

⏰ Thời gian: [Ngày, giờ cụ thể]

📍 Địa điểm: [Địa chỉ công ty hoặc link Google Meet/Zoom nếu phỏng vấn online]

👤 Người phỏng vấn: [Tên + chức vụ nếu có]

💻 Hình thức: [Trực tiếp / Online]

Trong buổi phỏng vấn, bạn sẽ có cơ hội tìm hiểu thêm về văn hóa làm việc, dự án hiện tại, và định hướng phát triển nghề nghiệp tại [Tên công ty].

Vui lòng 📧 phản hồi xác nhận tham dự trước [thời hạn xác nhận, ví dụ: 17h ngày 31/10/2025] để chúng tôi sắp xếp lịch phỏng vấn phù hợp.
Nếu bạn cần thay đổi thời gian hoặc có bất kỳ thắc mắc nào, hãy liên hệ qua [Email hoặc SĐT của HR] nhé.

Rất mong được gặp bạn trong buổi phỏng vấn sắp tới!

Trân trọng,
📝 [Tên người gửi]
[Chức vụ] – [Tên công ty]
📞 [Số điện thoại] | ✉️ [Email]
`;

        const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(emailAddresses.join(','))}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;

        window.open(gmailUrl, '_blank');

        closeModal();
        showNotification(`Gmail opened with ${selectedCandidates.length} recipients`, 'success');

        // Clear selection
        document.querySelectorAll('.candidate-checkbox:checked').forEach(checkbox => {
            checkbox.checked = false;
        });
        initializeSelectionManagement();
    }

    bulkSendButton.addEventListener('click', openModal);
    modalClose?.addEventListener('click', closeModal);
    cancelBulkEmail?.addEventListener('click', closeModal);
    confirmBulkEmail?.addEventListener('click', sendBulkEmail);

    bulkEmailModal.addEventListener('click', function(e) {
        if (e.target === bulkEmailModal) {
            closeModal();
        }
    });
}

// ========== DROPDOWN ACTIONS ==========
function initializeDropdowns() {
    function closeAllDropdowns() {
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            menu.style.display = 'none';
        });
    }

    document.querySelectorAll('.actions-icon').forEach(icon => {
        icon.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const dropdownMenu = this.nextElementSibling;
            const isVisible = dropdownMenu.style.display === 'block';

            closeAllDropdowns();
            dropdownMenu.style.display = isVisible ? 'none' : 'block';
        });
    });

    document.addEventListener('click', function(e) {
        if (!e.target.closest('.actions-cell')) {
            closeAllDropdowns();
        }
    });

    document.querySelectorAll('.dropdown-item').forEach(item => {
        if (!item.hasAttribute('href')) {
            item.addEventListener('click', () => closeAllDropdowns());
        }
    });
}

// ========== EMAIL ACTIONS ==========
function initializeEmailActions() {
    document.querySelectorAll('.send-email-action').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const email = this.getAttribute('data-email');
            if (email) {
                const subject = `📩 Thư mời phỏng vấn – Vị trí [Tên vị trí] tại [Tên công ty]`;
                const body = `
👋 Kính gửi [Tên ứng viên],

Cảm ơn bạn đã quan tâm và gửi hồ sơ ứng tuyển cho vị trí [Tên vị trí] tại [Tên công ty]. Sau khi xem xét hồ sơ, chúng tôi nhận thấy kỹ năng và kinh nghiệm của bạn rất phù hợp với yêu cầu tuyển dụng hiện tại.

Chúng tôi trân trọng mời bạn tham dự 💬 buổi phỏng vấn để trao đổi thêm về công việc và cơ hội phát triển tại công ty.

🗓️ Thông tin buổi phỏng vấn:

⏰ Thời gian: [Ngày, giờ cụ thể]

📍 Địa điểm: [Địa chỉ công ty hoặc link Google Meet/Zoom nếu phỏng vấn online]

👤 Người phỏng vấn: [Tên + chức vụ nếu có]

💻 Hình thức: [Trực tiếp / Online]

Trong buổi phỏng vấn, bạn sẽ có cơ hội tìm hiểu thêm về văn hóa làm việc, dự án hiện tại, và định hướng phát triển nghề nghiệp tại [Tên công ty].

Vui lòng 📧 phản hồi xác nhận tham dự trước [thời hạn xác nhận, ví dụ: 17h ngày 31/10/2025] để chúng tôi sắp xếp lịch phỏng vấn phù hợp.
Nếu bạn cần thay đổi thời gian hoặc có bất kỳ thắc mắc nào, hãy liên hệ qua [Email hoặc SĐT của HR] nhé.

Rất mong được gặp bạn trong buổi phỏng vấn sắp tới!

Trân trọng,
📝 [Tên người gửi]
[Chức vụ] – [Tên công ty]
📞 [Số điện thoại] | ✉️ [Email]
`;

                const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(email)}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
                window.open(gmailUrl, '_blank');
                showNotification('Email opened for ' + email, 'success');
            } else {
                showNotification('No email address available', 'error');
            }
        });
    });
}

// ========== STATUS UPDATE ==========
function initializeStatusUpdates() {
    document.querySelectorAll('.status-dropdown').forEach(dropdown => {
        // Lưu giá trị ban đầu
        dropdown.setAttribute('data-original-value', dropdown.value);

        dropdown.addEventListener('change', function(e) {
            e.preventDefault();

            const form = this.closest('.status-form');
            if (!form) return;

            const action = form.getAttribute('action');
            const match = action?.match(/\/applications\/(\d+)\/status/);
            const applicationId = match?.[1];
            const newStatus = this.value;
            const originalColor = this.style.backgroundColor;

            // Show loading state
            this.style.backgroundColor = '#f0f0f0';
            this.disabled = true;

            fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: `status=${encodeURIComponent(newStatus)}`
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Network response was not ok');
                }
            })
            .then(data => {
                if (data.success) {
                    updateStatusUI(this, newStatus);
                    // Lưu vào localStorage
                    if (applicationId) {
                        saveStatusState(applicationId, newStatus);
                    }
                    this.style.backgroundColor = '#d4edda';
                    setTimeout(() => {
                        this.style.backgroundColor = originalColor;
                        this.disabled = false;
                    }, 1000);
                    showNotification('Status updated successfully!', 'success');
                } else {
                    throw new Error(data.message || 'Update failed');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                this.style.backgroundColor = '#f8d7da';
                setTimeout(() => {
                    this.style.backgroundColor = originalColor;
                    this.disabled = false;
                    // Revert to original value
                    this.value = this.getAttribute('data-original-value');
                }, 2000);
                showNotification('Failed to update status: ' + error.message, 'error');
            });
        });
    });
}

// ========== BOOKMARK ACTIONS ==========
function initializeBookmarks() {
    document.querySelectorAll('.bookmark-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault(); // QUAN TRỌNG: Ngăn form submit mặc định
            e.stopPropagation();

            const applicationId = this.getAttribute('action').match(/\/applications\/(\d+)\/bookmark/)?.[1];
            const button = this.querySelector('.save-button');
            const icon = button.querySelector('i');
            const isCurrentlyBookmarked = icon.classList.contains('fa-bookmark'); // Font Awesome

            // Lưu trạng thái hiện tại để có thể khôi phục nếu có lỗi
            const originalState = isCurrentlyBookmarked;
            const originalIconClass = icon.className;

            // Cập nhật UI ngay lập tức - ĐẢO NGƯỢC trạng thái
            if (isCurrentlyBookmarked) {
                icon.className = 'far fa-bookmark';
                button.classList.remove('saved');
            } else {
                icon.className = 'fas fa-bookmark';
                button.classList.add('saved');
            }

            // Vô hiệu hóa nút trong khi loading
            button.disabled = true;

            // LẤY CSRF TOKEN từ form
            const csrfToken = this.querySelector('input[name="_csrf"]')?.value;

            // Gửi request đến server
            fetch(this.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: `_csrf=${csrfToken}`
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    // Cập nhật localStorage với trạng thái MỚI
                    saveBookmarkState(applicationId, data.isBookmarked);
                    showNotification(data.message, 'success');

                    // Nếu đang ở trang saved và bỏ bookmark, xóa item khỏi DOM
                    if (!data.isBookmarked && window.location.pathname.includes('/saved')) {
                        const applicationItem = this.closest('tr');
                        if (applicationItem) {
                            applicationItem.style.opacity = '0';
                            setTimeout(() => {
                                applicationItem.remove();
                                updateApplicationCount();
                            }, 300);
                        }
                    }
                } else {
                    throw new Error(data.message || 'Update failed');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                // KHÔI PHỤC UI về trạng thái ban đầu nếu có lỗi
                if (originalState) {
                    icon.className = 'fas fa-bookmark';
                    button.classList.add('saved');
                } else {
                    icon.className = 'far fa-bookmark';
                    button.classList.remove('saved');
                }
                showNotification('Failed to update bookmark: ' + error.message, 'error');
            })
            .finally(() => {
                button.disabled = false;
            });
        });
    });
}
// ========== UPDATE APPLICATION COUNT ==========
function updateApplicationCount() {
    // Cập nhật số lượng trong banner (nếu có)
    const savedCountElement = document.getElementById('savedCount');
    if (savedCountElement) {
        const currentCount = parseInt(savedCountElement.textContent) || 0;
        savedCountElement.textContent = Math.max(0, currentCount - 1);
    }

    // Cập nhật thông tin pagination
    const paginationInfo = document.getElementById('pagination-info');
    if (paginationInfo) {
        const currentText = paginationInfo.textContent;
        const newCount = Math.max(0, parseInt(savedCountElement?.textContent || '0') - 1);

        // Cập nhật số lượng tổng
        const totalMatch = currentText.match(/of\s+(\d+)\s+results/);
        if (totalMatch) {
            const currentTotal = parseInt(totalMatch[1]) || 0;
            paginationInfo.textContent = currentText.replace(/of\s+\d+\s+results/, `of ${Math.max(0, currentTotal - 1)} results`);
        }
    }

    // Cập nhật text "Hiển thị X của Y ứng viên" (nếu có)
    const displayInfo = document.querySelector('.muted');
    if (displayInfo) {
        const text = displayInfo.textContent;
        const match = text.match(/Hiển thị (\d+) của (\d+) ứng viên/);
        if (match) {
            const currentCount = parseInt(match[2]) || 0;
            const newText = text.replace(/\d+ ứng viên$/, Math.max(0, currentCount - 1) + ' ứng viên');
            displayInfo.textContent = newText;
        }
    }

    // Kiểm tra nếu không còn ứng viên nào, hiển thị empty state
    setTimeout(() => {
        const remainingRows = document.querySelectorAll('tbody tr:not([style*="opacity: 0"])');
        if (remainingRows.length === 0) {
            showEmptyState();
        }
    }, 500);
}

// ========== SHOW EMPTY STATE ==========
function showEmptyState() {
    const tbody = document.querySelector('tbody');
    if (!tbody) return;

    const emptyStateHTML = `
        <tr>
            <td colspan="9">
                <div style="text-align: center; padding: 60px 20px; color: var(--muted);">
                    <i class="fas fa-user-search" style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;"></i>
                    <h3 style="margin: 0 0 8px;">Không có ứng viên nào được lưu</h3>
                    <p style="margin: 0;">Khi bạn lưu ứng viên, họ sẽ xuất hiện ở đây</p>
                </div>
            </td>
        </tr>
    `;

    tbody.innerHTML = emptyStateHTML;

    // Ẩn các phần tử khác không cần thiết
    const bulkActions = document.querySelector('.bulk-actions');
    const pagination = document.querySelector('.pagination');
    if (bulkActions) bulkActions.style.display = 'none';
    if (pagination) pagination.style.display = 'none';
}
// Hàm cập nhật số lượng ứng viên (nếu cần)
function updateApplicationCount() {
    const countElement = document.querySelector('.total-elements');
    if (countElement) {
        const currentCount = parseInt(countElement.textContent) || 0;
        countElement.textContent = Math.max(0, currentCount - 1);
    }

    // Cập nhật thông tin pagination
    const paginationInfo = document.getElementById('pagination-info');
    if (paginationInfo) {
        const currentText = paginationInfo.textContent;
        const newCount = Math.max(0, parseInt(countElement?.textContent || '0') - 1);
        paginationInfo.textContent = currentText.replace(/\d+ results/, newCount + ' results');
    }
}

// ========== HELPER FUNCTIONS ==========
function updateStatusUI(dropdown, newStatus) {
    // Remove all status classes
    dropdown.classList.remove('status-submitted', 'status-reviewed', 'status-hired', 'status-rejected');
    // Add new status class
    dropdown.classList.add(`status-${newStatus.toLowerCase()}`);
}

function showNotification(message, type) {
    // Remove existing notifications
    document.querySelectorAll('.custom-toast').forEach(toast => toast.remove());

    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;
    toast.style.cssText = `
        position: fixed;
        top: 24px;
        right: 24px;
        padding: 14px 24px;
        border-radius: 6px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        transition: all 0.3s ease;
        background-color: ${type === 'success' ? '#4caf50' : '#f44336'};
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    toast.textContent = message;
    document.body.appendChild(toast);

    // Animate in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 10);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ========== MAIN INITIALIZATION ==========
document.addEventListener('DOMContentLoaded', function() {
    // Initialize all components
    initializeFilterToggle();
    initializeSelectionManagement();
    initializeBulkEmailModal();
    initializeDropdowns();
    initializeEmailActions();
    initializeStatusUpdates();
    updateApplicationCount();
    initializeBookmarks();

    // Sync states from localStorage
    syncBookmarkStates();
    syncStatusStates();

    // Initialize range slider
    updateRangeColor();
    document.getElementById('experience-range')?.addEventListener('input', updateRangeColor);
});

// Sync when page fully loads
window.addEventListener('load', function() {
    syncBookmarkStates();
    syncStatusStates();
});