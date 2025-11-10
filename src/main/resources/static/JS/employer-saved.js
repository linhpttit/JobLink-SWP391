// ========== GLOBAL STATE ==========
let isLoading = false;
let selectedCandidate = null;

// ========== INITIALIZATION ==========
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing application...');

    // Initialize event listeners
    initializeEventListeners();

    // Initialize status dropdowns
    initializeStatusDropdowns();

    // Initialize dropdown actions
    initializeDropdownActions();

    console.log('Application initialized successfully');
});

// ========== EVENT LISTENERS ==========
function initializeEventListeners() {
    console.log('Setting up event listeners...');

    // Export CSV
    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', handleExportCSV);
        console.log('Export CSV listener added');
    }

    // Bulk email
    const bulkEmailBtn = document.getElementById('bulkEmailBtn');
    if (bulkEmailBtn) {
        bulkEmailBtn.addEventListener('click', handleBulkEmail);
        console.log('Bulk email listener added');
    }

    const closeModal = document.getElementById('closeModal');
    if (closeModal) {
        closeModal.addEventListener('click', handleCloseBulkModal);
    }

    const sendEmailBtn = document.getElementById('sendEmailBtn');
    if (sendEmailBtn) {
        sendEmailBtn.addEventListener('click', handleSendBulkEmail);
    }

    // Single email modal
    const closeSingleModal = document.getElementById('closeSingleModal');
    if (closeSingleModal) {
        closeSingleModal.addEventListener('click', handleCloseSingleModal);
    }

    const sendSingleEmailBtn = document.getElementById('sendSingleEmailBtn');
    if (sendSingleEmailBtn) {
        sendSingleEmailBtn.addEventListener('click', handleSendSingleEmail);
    }

    // Search and filters
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }

    ['posFilter', 'statusFilter', 'educationFilter', 'locationFilter', 'expFilter'].forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener('change', handleFilterChange);
        }
    });

    // Select all checkbox
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.addEventListener('change', handleSelectAll);
    }

    // Pagination
    const prevBtn = document.getElementById('prevBtn');
    if (prevBtn) {
        prevBtn.addEventListener('click', handlePrevPage);
    }

    const nextBtn = document.getElementById('nextBtn');
    if (nextBtn) {
        nextBtn.addEventListener('click', handleNextPage);
    }

    console.log('All event listeners initialized');
}

// ========== DROPDOWN ACTIONS ==========
function initializeDropdownActions() {
    // Event delegation for dropdown actions
    document.addEventListener('click', function(e) {
        // Send email action
        if (e.target.classList.contains('send-email-btn') || e.target.closest('.send-email-btn')) {
            e.preventDefault();
            const element = e.target.classList.contains('send-email-btn') ? e.target : e.target.closest('.send-email-btn');
            const appId = element.getAttribute('data-app-id');
            const candidateName = element.getAttribute('data-candidate-name');
            const candidateEmail = element.getAttribute('data-candidate-email');
            const position = element.getAttribute('data-position');
            handleSendEmail(appId, candidateName, candidateEmail, position);
        }

        // View CV action
        if (e.target.classList.contains('view-cv-btn') || e.target.closest('.view-cv-btn')) {
            e.preventDefault();
            const element = e.target.classList.contains('view-cv-btn') ? e.target : e.target.closest('.view-cv-btn');
            const appId = element.getAttribute('data-app-id');
            const cvUrl = element.getAttribute('data-cv-url');
            handleViewCV(appId, cvUrl);
        }

        // Bookmark action
        if (e.target.classList.contains('bookmark-btn') || e.target.closest('.bookmark-btn')) {
            e.preventDefault();
            const element = e.target.classList.contains('bookmark-btn') ? e.target : e.target.closest('.bookmark-btn');
            const appId = element.getAttribute('data-app-id');
            handleToggleBookmark(appId);
        }
    });
}

// ========== STATUS DROPDOWN FUNCTIONS ==========
function initializeStatusDropdowns() {
    document.querySelectorAll('.status-dropdown').forEach(select => {
        select.setAttribute('data-original-value', select.value);
    });
}

function handleStatusChange(selectEl) {
    if (isLoading) {
        showToast('Đang xử lý, vui lòng đợi...', 'error');
        selectEl.value = selectEl.getAttribute('data-original-value');
        return;
    }

    const applicationId = selectEl.getAttribute('data-id');
    const newStatus = selectEl.value;
    const originalStatus = selectEl.getAttribute('data-original-value');

    console.log('Updating status for application', applicationId, 'to', newStatus);

    // Show loading state
    selectEl.disabled = true;
    selectEl.classList.add('loading');

    // Call Spring Boot API
    fetch('/applications/' + applicationId + '/status', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'status=' + encodeURIComponent(newStatus)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // Update visual style
            selectEl.className = 'tag status-dropdown status-' + newStatus.toLowerCase().replace(/ /g, '-');
            selectEl.setAttribute('data-original-value', newStatus);
            showToast('Đã cập nhật trạng thái thành công', 'success');
        } else {
            showToast('Lỗi: ' + data.message, 'error');
            // Revert selection
            selectEl.value = originalStatus;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Lỗi khi cập nhật trạng thái', 'error');
        selectEl.value = originalStatus;
    })
    .finally(() => {
        selectEl.disabled = false;
        selectEl.classList.remove('loading');
    });
}

// ========== EMAIL FUNCTIONS ==========
function handleSendEmail(applicationId, candidateName, candidateEmail, position) {
    console.log('Sending email to:', candidateName, candidateEmail);

    selectedCandidate = { id: applicationId, name: candidateName, email: candidateEmail, position: position };

    // Update modal content
    document.getElementById('singleRecipient').textContent = `Người nhận: ${candidateName} (${candidateEmail})`;

    // Pre-fill email template
    const subject = `Thư mời phỏng vấn – Vị trí ${position} tại AI Premium`;
    document.getElementById('singleEmailSubject').value = subject;

    const body = document.getElementById('singleEmailBody').value
        .replace(/\[Tên ứng viên\]/g, candidateName)
        .replace(/\[Tên vị trí\]/g, position);
    document.getElementById('singleEmailBody').value = body;

    // Show modal
    document.getElementById('singleEmailModal').style.display = 'flex';
}

function handleSendSingleEmail() {
    if (!selectedCandidate) return;

    const subject = document.getElementById('singleEmailSubject').value || 'Thư mời phỏng vấn';
    const body = document.getElementById('singleEmailBody').value;

    // Open Gmail with pre-filled content
    const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${selectedCandidate.email}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    window.open(gmailUrl, '_blank');

    // Close modal and show success message
    document.getElementById('singleEmailModal').style.display = 'none';
    showToast(`Đã mở Gmail cho ${selectedCandidate.name}`, 'success');
}

function handleBulkEmail() {
    console.log('Bulk email button clicked');
    const selectedCandidates = getSelectedCandidates();

    if (selectedCandidates.length === 0) {
        showToast('Vui lòng chọn ít nhất 1 ứng viên', 'error');
        return;
    }

    // Update recipients preview
    document.getElementById('recipientsPreview').textContent =
        `Người nhận: ${selectedCandidates.length} ứng viên`;

    // Show modal
    document.getElementById('modalBackdrop').style.display = 'flex';
}

function handleSendBulkEmail() {
    const selectedCandidates = getSelectedCandidates();
    const subject = document.getElementById('emailSubject').value || 'Thư mời phỏng vấn';
    const body = document.getElementById('emailBody').value;

    if (selectedCandidates.length === 0) {
        showToast('Không có ứng viên nào được chọn', 'error');
        return;
    }

    // Get all emails
    const emails = selectedCandidates.map(c => c.email).join(',');

    // Open Gmail with pre-filled content
    const gmailUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${emails}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    window.open(gmailUrl, '_blank');

    // Close modal and show success message
    document.getElementById('modalBackdrop').style.display = 'none';
    showToast(`Đã mở Gmail với ${selectedCandidates.length} ứng viên`, 'success');
}

// ========== CSV EXPORT ==========
function handleExportCSV() {
    console.log('Export CSV button clicked');

    const headers = ['Tên', 'Email', 'Số điện thoại', 'Vị trí', 'Kinh nghiệm', 'Học vấn', 'Địa điểm', 'Trạng thái', 'Ngày ứng tuyển'];

    const rows = Array.from(document.querySelectorAll('tbody tr')).map(row => {
        const cells = row.querySelectorAll('td');
        return [
            cells[1].querySelector('.name div').textContent.trim(),
            cells[1].querySelector('.name small').textContent.trim(),
            cells[1].querySelector('.name small:nth-child(3)').textContent.trim(),
            cells[2].textContent.trim(),
            cells[3].textContent.trim(),
            cells[4].textContent.trim(),
            cells[5].textContent.trim(),
            cells[6].querySelector('select').value.trim(),
            cells[7].textContent.trim()
        ];
    });

    const csvContent = [
        headers.join(','),
        ...rows.map(row => row.map(field => `"${field.replace(/"/g, '""')}"`).join(','))
    ].join('\n');

    // Create and download file
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `ung_vien_da_luu_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('Đã xuất file CSV thành công', 'success');
}

// ========== BOOKMARK FUNCTIONS ==========
function handleToggleBookmark(applicationId) {
    if (isLoading) {
        showToast('Đang xử lý, vui lòng đợi...', 'error');
        return;
    }

    console.log('Toggling bookmark for application:', applicationId);

    // Show loading state
    isLoading = true;
    const button = document.querySelector(`.bookmark-btn[data-app-id="${applicationId}"]`);
    const originalHTML = button.innerHTML;
    button.innerHTML = '<div class="spinner"></div>';
    button.style.pointerEvents = 'none';

    // Call Spring Boot API
    fetch('/applications/' + applicationId + '/bookmark', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            if (data.isBookmarked) {
                showToast('Đã lưu ứng viên', 'success');
            } else {
                showToast('Đã bỏ lưu ứng viên', 'success');
                // Remove row from table
                const row = document.querySelector(`tr[data-id="${applicationId}"]`);
                if (row) {
                    row.style.opacity = '0';
                    setTimeout(() => {
                        row.remove();
                        // Update saved count
                        const currentCount = parseInt(document.getElementById('savedCount').textContent);
                        document.getElementById('savedCount').textContent = currentCount - 1;
                    }, 300);
                }
            }
        } else {
            showToast('Lỗi: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Lỗi khi cập nhật bookmark', 'error');
    })
    .finally(() => {
        isLoading = false;
        if (button) {
            button.innerHTML = originalHTML;
            button.style.pointerEvents = 'auto';
        }
    });
}

// ========== FILTER AND SEARCH FUNCTIONS ==========
let searchTimeout;
function handleSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        applyFilters();
    }, 500);
}

function handleFilterChange() {
    applyFilters();
}

function applyFilters() {
    const search = document.getElementById('searchInput').value;
    const position = document.getElementById('posFilter').value;
    const status = document.getElementById('statusFilter').value;
    const education = document.getElementById('educationFilter').value;
    const location = document.getElementById('locationFilter').value;
    const experience = document.getElementById('expFilter').value;

    const url = new URL(window.location.href);

    // Update URL parameters
    if (search) {
        url.searchParams.set('search', search);
    } else {
        url.searchParams.delete('search');
    }

    if (position) {
        url.searchParams.set('positions', position);
    } else {
        url.searchParams.delete('positions');
    }

    if (status) {
        url.searchParams.set('statuses', status);
    } else {
        url.searchParams.delete('statuses');
    }

    if (education) {
        url.searchParams.set('educationLevels', education);
    } else {
        url.searchParams.delete('educationLevels');
    }

    if (location) {
        url.searchParams.set('location', location);
    } else {
        url.searchParams.delete('location');
    }

    if (experience) {
        // Map experience filter to minExperience
        const minExp = parseInt(experience);
        url.searchParams.set('minExperience', minExp.toString());
        if (minExp === 5) {
            url.searchParams.set('maxExperience', '20'); // 5+ years
        } else {
            url.searchParams.delete('maxExperience');
        }
    } else {
        url.searchParams.delete('minExperience');
        url.searchParams.delete('maxExperience');
    }

    // Reset to first page when filtering
    url.searchParams.set('page', '0');

    console.log('Applying filters, redirecting to:', url.toString());
    window.location.href = url.toString();
}

// ========== SELECTION FUNCTIONS ==========
function handleSelectAll(e) {
    const checkboxes = document.querySelectorAll('.rowCheckbox');
    checkboxes.forEach(cb => cb.checked = e.target.checked);
}

function getSelectedCandidates() {
    const selected = [];
    document.querySelectorAll('.rowCheckbox:checked').forEach(checkbox => {
        const row = checkbox.closest('tr');
        const name = row.querySelector('.name div').textContent.trim();
        const email = row.querySelector('.name small').textContent.trim();
        const position = row.querySelector('td:nth-child(3)').textContent.trim();
        selected.push({ name, email, position });
    });
    return selected;
}

// ========== PAGINATION FUNCTIONS ==========
function handlePrevPage() {
    const currentPage = parseInt(document.querySelector('#pageInfo span:nth-child(1)').textContent) - 1;
    if (currentPage > 0) {
        changePage(currentPage - 1);
    }
}

function handleNextPage() {
    const currentPage = parseInt(document.querySelector('#pageInfo span:nth-child(1)').textContent) - 1;
    const totalPages = parseInt(document.querySelector('#pageInfo span:nth-child(2)').textContent);
    if (currentPage < totalPages - 1) {
        changePage(currentPage + 1);
    }
}

function changePage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', page);
    window.location.href = url.toString();
}

// ========== CV VIEW FUNCTION ==========
function handleViewCV(applicationId, cvUrl) {
    if (cvUrl && cvUrl !== '' && cvUrl !== 'null') {
        window.open(cvUrl, '_blank');
        showToast('Đang mở CV...', 'success');
    } else {
        showToast('Ứng viên chưa tải lên CV', 'error');
    }
}

// ========== MODAL FUNCTIONS ==========
function handleCloseBulkModal() {
    document.getElementById('modalBackdrop').style.display = 'none';
}

function handleCloseSingleModal() {
    document.getElementById('singleEmailModal').style.display = 'none';
}

// ========== UTILITY FUNCTIONS ==========
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const icon = type === 'success' ? 'ri-checkbox-circle-line' : 'ri-error-warning-line';
    const backgroundColor = type === 'success' ? '#111827' : '#7f1d1d';

    toast.innerHTML = `<i class="${icon}"></i> ${message}`;
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '6px';
    toast.style.background = backgroundColor;

    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}