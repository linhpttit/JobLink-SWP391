// ========================
// 🔹 JobLink - Tìm kiếm việc làm
// 🔹 Bản cải tiến toàn diện bởi ChatGPT 🔨🤖🔧
// ========================

// --- DOM Elements ---
const filterLeft = document.querySelector('.filter-left');
const keywordInput = document.querySelector('.search-keyword');
const locationInput = document.querySelector('.search-location');
const jobList = document.querySelector('.job-listings');
const modal = document.getElementById('advanceFilterModal');
const openBtn = document.getElementById('openAdvanceFilter');
const closeBtn = document.getElementById('closeModal');
const applyBtn = document.getElementById('applyFilters');
const resetBtn = document.getElementById('resetFilters');
const gridView = document.querySelector('.search-view-grid');
const listView = document.querySelector('.search-view-list');
const searchBtn = document.querySelector('.search-btn');
const sortSelect = document.querySelector('.search-sort');
const viewContainer = document.querySelector('.job-listings');

let tags = [];
let isFetching = false;
let jobsCache = []; // cache jobs sau khi fetch để sắp xếp lại không cần gọi API

// ========================
// 🔸 Quản lý Tag
// ========================
function addTag(value) {
    if (!value || tags.includes(value)) return;
    tags.push(value);
    renderTags();
}

function removeTag(value) {
    tags = tags.filter(tag => tag !== value);
    renderTags();
}

function renderTags() {
    filterLeft.querySelectorAll('.search-tag, .search-tag-more').forEach(e => e.remove());
    const visibleTags = tags.slice(0, 4);
    visibleTags.forEach(tag => {
        const span = document.createElement('span');
        span.className = 'search-tag';
        span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
        span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
        filterLeft.appendChild(span);
    });

    if (tags.length > 4) {
        const more = document.createElement('span');
        more.className = 'search-tag search-tag-more';
        more.textContent = `+${tags.length - 4}`;
        more.onclick = renderAllTags;
        filterLeft.appendChild(more);
    }
}

function renderAllTags() {
    filterLeft.querySelectorAll('.search-tag, .search-tag-more').forEach(e => e.remove());
    tags.forEach(tag => {
        const span = document.createElement('span');
        span.className = 'search-tag';
        span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
        span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
        filterLeft.appendChild(span);
    });
}

// ========================
// 🔸 Gọi API lấy danh sách job
// ========================
async function fetchJobs() {
    if (isFetching) return;
    isFetching = true;

    const keyword = keywordInput.value.trim();
    const location = locationInput.value.trim();

    const baseUrl = window.location.origin;
    const url = `${baseUrl}/api/jobs/search?skills=${encodeURIComponent(keyword)}&location=${encodeURIComponent(location)}`;

    console.log('🔍 Gọi API:', url);
    jobList.innerHTML = "<p style='text-align:center;padding:40px;'>Đang tải việc làm...</p>";

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`Lỗi server (${res.status})`);
        const jobs = await res.json();

        if (!jobs || jobs.length === 0) {
            jobList.innerHTML = "<p style='text-align:center;padding:40px;color:#666;'>Không tìm thấy việc làm.</p>";
            return;
        }

        jobsCache = jobs; // Lưu vào cache để có thể sắp xếp mà không gọi lại API
        renderJobList();
        console.log('✅ Đã tải', jobs.length, 'job');
    } catch (err) {
        console.error('❌ Lỗi khi tải job:', err);
        jobList.innerHTML = "<p style='text-align:center;padding:40px;color:#d32f2f;'>Không thể tải dữ liệu việc làm. Vui lòng thử lại.</p>";
    } finally {
        isFetching = false;
    }
}

// ========================
// 🔸 Render danh sách job
// ========================
function renderJobList() {
    jobList.innerHTML = "";

    // Lấy giá trị sắp xếp từ dropdown
    const sortOption = sortSelect.value;

    // Sắp xếp danh sách
    const sortedJobs = [...jobsCache];
    if (sortOption === "Latest") {
        sortedJobs.sort((a, b) => new Date(b.postedDate) - new Date(a.postedDate));
    } else if (sortOption === "Oldest") {
        sortedJobs.sort((a, b) => new Date(a.postedDate) - new Date(b.postedDate));
    } else if (sortOption === "Salary: High to Low") {
        sortedJobs.sort((a, b) => (b.salaryMax || 0) - (a.salaryMax || 0));
    } else if (sortOption === "Salary: Low to High") {
        sortedJobs.sort((a, b) => (a.salaryMin || 0) - (b.salaryMin || 0));
    }

    // Render từng job
    sortedJobs.forEach(job => renderJobCard(job));
}

// ========================
// 🔸 Hiển thị từng thẻ Job
// ========================
function renderJobCard(job) {
    const card = document.createElement('div');
    card.className = "job-card";
    if (listView.classList.contains('active')) card.classList.add('list-view');

    const logo = job.companyName ? job.companyName[0].toUpperCase() : "?";
    const title = job.title || "Không có tiêu đề";
    const company = job.companyName || "Công ty chưa rõ";
    const location = job.location || "Không xác định";
    const industry = job.industry || "Chung";
    const desc = job.description ? job.description.substring(0, 120) + "..." : "Không có mô tả.";
    const skills = job.allSkills || "Không có kỹ năng";
    const salaryMin = job.salaryMin || 0;
    const salaryMax = job.salaryMax || 0;
    const jobId = job.jobId || 0;

    card.innerHTML = `
        <div class="job-card-header">
            <div class="job-logo" style="background:#1976d2;">${logo}</div>
            <div class="job-info">
                <h3>${title}</h3>
                <div class="company-info">
                    <i class="fas fa-building"></i> ${company}
                    <span class="job-location"><i class="fas fa-map-marker-alt"></i> ${location}</span>
                </div>
            </div>
        </div>
        <div class="job-tags">
            <span class="tag tag-type">${industry}</span>
        </div>
        <p class="job-description">${desc}</p>
        <div class="job-footer">
            <div class="job-meta">
                <span><i class="fas fa-dollar-sign"></i> ${salaryMin} - ${salaryMax}</span>
                <span><i class="fas fa-code"></i> ${skills}</span>
            </div>
            <button class="btn-apply" onclick="window.location.href='/job-detail/${jobId}'">
                Ứng tuyển <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;
    jobList.appendChild(card);
}

// ========================
// 🔸 Modal bộ lọc nâng cao
// ========================
const filterMapping = {
    'Freshers': 'exp-fresh', '1 - 2 Years': 'exp-1-2', '2 - 4 Years': 'exp-2-4', '4 - 6 Years': 'exp-4-6',
    '6 - 8 Years': 'exp-6-8', '8 - 10 Years': 'exp-8-10', '10 - 15 Years': 'exp-10-15', '15+ Years': 'exp-15+',
    '$50 - $1000': 'sal-1', '$1000 - $2000': 'sal-2', '$3000 - $4000': 'sal-3', '$4000 - $6000': 'sal-4',
    '$6000 - $8000': 'sal-5', '$8000 - $10000': 'sal-6', '$10000 - $15000': 'sal-7', '$15000+': 'sal-8',
    'All': 'type-all', 'Full Time': 'type-full', 'Part Time': 'type-part', 'Internship': 'type-intern',
    'Remote': 'type-remote', 'Temporary': 'type-temp', 'Contract Base': 'type-contract',
    'High School': 'edu-high', 'Intermediate': 'edu-inter', 'Graduation': 'edu-grad',
    'Master Degree': 'edu-master', 'Bachelor Degree': 'edu-bach',
    'Entry Level': 'level-entry', 'Mid Level': 'level-mid', 'Expert Level': 'level-expert'
};

openBtn.onclick = () => {
    modal.style.display = 'block';
    document.querySelectorAll('input[type="checkbox"], input[type="radio"]').forEach(el => el.checked = false);
    tags.forEach(tag => {
        const id = filterMapping[tag];
        if (id) document.getElementById(id)?.setAttribute('checked', true);
    });
};

closeBtn.onclick = () => modal.style.display = 'none';
modal.onclick = e => { if (e.target === modal) modal.style.display = 'none'; };
window.addEventListener('keydown', e => { if (e.key === 'Escape') modal.style.display = 'none'; });

applyBtn.onclick = () => {
    const experience = document.querySelector('input[name="experience"]:checked')?.value;
    const salary = document.querySelector('input[name="salary"]:checked')?.value;
    const level = document.querySelector('input[name="level"]:checked')?.value;
    const jobTypes = Array.from(document.querySelectorAll('input[id^="type-"]:checked')).map(cb => cb.value);
    const education = Array.from(document.querySelectorAll('input[id^="edu-"]:checked')).map(cb => cb.value);

    if (experience) addTag(experience);
    if (salary) addTag(salary);
    if (level) addTag(level);
    jobTypes.forEach(addTag);
    education.forEach(addTag);

    modal.style.display = 'none';
    fetchJobs(); // 🔥 Tự động load job khi áp dụng lọc
};

resetBtn.onclick = () => {
    document.querySelectorAll('input[type="checkbox"], input[type="radio"]').forEach(el => el.checked = false);
};

// ========================
// 🔸 Sự kiện tìm kiếm & sắp xếp
// ========================
searchBtn.addEventListener('click', () => {
    const keyword = keywordInput.value.trim();
    const location = locationInput.value.trim();
    if (!keyword && !location) {
        alert('Vui lòng nhập từ khóa hoặc địa điểm để tìm việc!');
        return;
    }
    if (keyword) addTag(keyword);
    if (location) addTag(location);
    fetchJobs();
    keywordInput.value = '';
    locationInput.value = '';
});

sortSelect.addEventListener('change', renderJobList);

// ========================
// 🔸 Chuyển đổi chế độ xem (Grid / List)
// ========================
gridView.onclick = () => {
    gridView.classList.add('active');
    listView.classList.remove('active');
    viewContainer.classList.remove('list-mode');
    renderJobList();
};

listView.onclick = () => {
    listView.classList.add('active');
    gridView.classList.remove('active');
    viewContainer.classList.add('list-mode');
    renderJobList();
};
