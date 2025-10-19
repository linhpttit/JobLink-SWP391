// job-detail.js

document.addEventListener('DOMContentLoaded', function() {
    const applyModal = document.getElementById('applyModal');
    const applyNowBtn = document.getElementById('applyNowBtn');
    const closeModalBtn = document.getElementById('closeModal');
    const cancelBtn = document.getElementById('cancelBtn');
    const applyForm = document.getElementById('applyForm');
    const cvSelect = document.querySelector('.form-select');
    const coverLetterTextarea = document.querySelector('.form-textarea');

    // Mở modal khi click Apply Now
    if (applyNowBtn) {
        applyNowBtn.addEventListener('click', function() {
            // Kiểm tra đăng nhập (lấy từ Thymeleaf attribute)
            const isLoggedIn = document.body.dataset.isLoggedIn === 'true';

            if (!isLoggedIn) {
                // Chuyển đến trang đăng nhập
                window.location.href = '/signin?redirect=' + window.location.pathname;
                return;
            }

            // Hiển thị modal
            applyModal.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
    }

    // Đóng modal
    function closeModal() {
        applyModal.classList.remove('active');
        document.body.style.overflow = 'auto';

        // Reset form
        if (applyForm) {
            applyForm.reset();
        }
    }

    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }

    // Click outside modal để đóng
    if (applyModal) {
        applyModal.addEventListener('click', function(e) {
            if (e.target === applyModal) {
                closeModal();
            }
        });
    }

    // ===== XỬ LÝ SUBMIT FORM ỨNG TUYỂN =====
    const submitBtn = document.querySelector('.btn-submit');

    if (submitBtn) {
        submitBtn.addEventListener('click', async function(e) {
            e.preventDefault();

            // Validation
            if (!cvSelect || !cvSelect.value || cvSelect.value === 'Select...') {
                showNotification('Vui lòng chọn CV để ứng tuyển', 'error');
                return;
            }

            // Disable submit button
            submitBtn.disabled = true;
            const originalContent = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

            try {
                // Lấy jobId từ URL
                const jobId = getJobIdFromUrl();

                // Lấy cvId từ select (giả sử value là cvId)
                const cvId = cvSelect.value;

                // Lấy cover letter
                const coverLetter = coverLetterTextarea ? coverLetterTextarea.value.trim() : '';

                // Tạo FormData
                const formData = new FormData();
                formData.append('jobId', jobId);
                formData.append('cvId', cvId);
                formData.append('coverLetter', coverLetter);

                // Gửi request
                const response = await fetch('/job/apply', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.success) {
                    showNotification(result.message || 'Ứng tuyển thành công!', 'success');

                    // Đóng modal sau 1.5s
                    setTimeout(() => {
                        closeModal();

                        // Cập nhật button thành "Applied"
                        if (applyNowBtn) {
                            applyNowBtn.disabled = true;
                            applyNowBtn.innerHTML = '<i class="fas fa-check"></i> Đã ứng tuyển';
                            applyNowBtn.style.background = '#10b981';
                            applyNowBtn.style.cursor = 'not-allowed';
                        }
                    }, 1500);

                } else {
                    showNotification(result.message || 'Có lỗi xảy ra', 'error');

                    // Redirect nếu cần
                    if (result.redirect) {
                        setTimeout(() => {
                            window.location.href = result.redirect;
                        }, 1500);
                    }
                }

            } catch (error) {
                console.error('Error:', error);
                showNotification('Có lỗi xảy ra. Vui lòng thử lại.', 'error');
            } finally {
                // Enable submit button
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalContent;
            }
        });
    }

    // ===== BOOKMARK FUNCTIONALITY =====
    const bookmarkBtn = document.querySelector('.btn-bookmark-large');
    if (bookmarkBtn) {
        bookmarkBtn.addEventListener('click', async function() {
            const jobId = getJobIdFromUrl();
            const icon = this.querySelector('i');
            const isBookmarked = icon.classList.contains('fas');

            try {
                const response = await fetch('/job/bookmark', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `jobId=${jobId}&action=${isBookmarked ? 'remove' : 'add'}`
                });

                const result = await response.json();

                if (result.success) {
                    // Toggle icon
                    if (isBookmarked) {
                        icon.classList.remove('fas');
                        icon.classList.add('far');
                        showNotification('Đã bỏ lưu công việc', 'info');
                    } else {
                        icon.classList.remove('far');
                        icon.classList.add('fas');
                        showNotification('Đã lưu công việc', 'success');
                    }
                } else {
                    if (result.redirect) {
                        window.location.href = result.redirect;
                    } else {
                        showNotification(result.message, 'error');
                    }
                }
            } catch (error) {
                console.error('Error:', error);
                showNotification('Có lỗi xảy ra', 'error');
            }
        });
    }

    // ===== EDITOR TOOLBAR BUTTONS =====
    const editorBtns = document.querySelectorAll('.editor-btn');
    editorBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const title = this.getAttribute('title');

            // Xử lý các nút format text (tùy chọn)
            if (coverLetterTextarea) {
                coverLetterTextarea.focus();
            }
        });
    });

    // ===== HELPER FUNCTIONS =====
    function getJobIdFromUrl() {
        const pathParts = window.location.pathname.split('/');
        return pathParts[pathParts.length - 1];
    }

    function showNotification(message, type = 'info') {
        // Tạo notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${getIconByType(type)}"></i>
            <span>${message}</span>
        `;

        // Thêm styles
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            background: ${getColorByType(type)};
            color: white;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 10000;
            display: flex;
            align-items: center;
            gap: 10px;
            animation: slideIn 0.3s ease-out;
            font-weight: 500;
        `;

        document.body.appendChild(notification);

        // Tự động xóa sau 3s
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => {
                if (notification.parentNode) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }

    function getIconByType(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            info: 'info-circle',
            warning: 'exclamation-triangle'
        };
        return icons[type] || 'info-circle';
    }

    function getColorByType(type) {
        const colors = {
            success: '#10b981',
            error: '#ef4444',
            info: '#3b82f6',
            warning: '#f59e0b'
        };
        return colors[type] || '#3b82f6';
    }
});

// CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }

    .btn-primary-large:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }
`;
document.head.appendChild(style);