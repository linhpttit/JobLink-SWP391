const tagContainer = document.querySelector('.search-filter-row');
    const keywordInput = document.querySelector('.search-keyword');
    const locationInput = document.querySelector('.search-location');
    let tags = [];

    function addTag(value) {
        if (!value || tags.includes(value)) return;
        tags.push(value);
        renderTags();

        // Hiệu ứng feedback khi thêm tag
        console.log('Tag added:', value);
    }

    function removeTag(value) {
        tags = tags.filter(tag => tag !== value);
        renderTags();
        console.log('Tag removed:', value);
    }

    function renderTags() {
        // Xóa tag cũ
        tagContainer.querySelectorAll('.search-tag, .search-tag-more').forEach(e => e.remove());

        // Hiển thị tối đa 4 tag
        let visibleTags = tags.slice(0, 4);
        visibleTags.forEach(tag => {
            let span = document.createElement('span');
            span.className = 'search-tag';
            span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
            span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
            tagContainer.insertBefore(span, tagContainer.firstChild);
        });

        // Nếu có nhiều hơn 4 tag
        if (tags.length > 4) {
            let more = document.createElement('span');
            more.className = 'search-tag search-tag-more';
            more.innerHTML = `+${tags.length - 4}`;
            more.onclick = () => {
                // Hiện tất cả tag bị ẩn
                tagContainer.querySelectorAll('.search-tag').forEach(e => e.remove());
                tags.forEach(tag => {
                    let span = document.createElement('span');
                    span.className = 'search-tag';
                    span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
                    span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
                    tagContainer.insertBefore(span, tagContainer.firstChild);
                });
                more.remove();
            };
            tagContainer.insertBefore(more, tagContainer.firstChild);
        }
    }

    // Thêm tag khi nhập vào ô tìm kiếm và nhấn Enter
    keywordInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            addTag(this.value.trim());
            this.value = '';
            e.preventDefault();
        }
    });

    locationInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            addTag(this.value.trim());
            this.value = '';
            e.preventDefault();
        }
    });

    // ✅ Sửa lỗi: Sử dụng selector chính xác
    // Đợi DOM load xong
    document.addEventListener('DOMContentLoaded', function() {
        const findJobButton = document.querySelector('.search-btn');

        if (findJobButton) {
            findJobButton.addEventListener('click', function(e) {
                e.preventDefault(); // Ngăn form submit

                // Thêm hiệu ứng visual feedback
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 150);

                let keyword = keywordInput.value.trim();
                let location = locationInput.value.trim();

                if (keyword) {
                    addTag(keyword);
                    keywordInput.value = '';
                }
                if (location) {
                    addTag(location);
                    locationInput.value = '';
                }

                // Feedback cho người dùng
                if (!keyword && !location) {
                    alert('Please enter a job title or location to search!');
                }
            });
        }
    });

    // Chuyển đổi định dạng xem với hiệu ứng
    document.addEventListener('DOMContentLoaded', function() {
        const gridView = document.querySelector('.search-view-grid');
        const listView = document.querySelector('.search-view-list');

        if (gridView) {
            gridView.onclick = function() {
                // Hiệu ứng click
                this.style.transform = 'scale(0.9)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);

                this.classList.add('active');
                if (listView) listView.classList.remove('active');
                console.log('Switched to grid view');
            };
        }

        if (listView) {
            listView.onclick = function() {
                // Hiệu ứng click
                this.style.transform = 'scale(0.9)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);

                this.classList.add('active');
                if (gridView) gridView.classList.remove('active');
                console.log('Switched to list view');
            };
        }
    });

    // Thêm hiệu ứng cho dropdown
    document.addEventListener('DOMContentLoaded', function() {
        const dropdowns = document.querySelectorAll('.search-sort, .search-page');

        dropdowns.forEach(dropdown => {
            dropdown.addEventListener('change', function() {
                // Hiệu ứng khi thay đổi giá trị
                this.style.background = '#e3f2fd';
                setTimeout(() => {
                    this.style.background = '';
                }, 200);

                console.log(`${this.className} changed to:`, this.value);
            });
        });
    });