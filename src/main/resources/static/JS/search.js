 // Tag management
    const filterLeft = document.querySelector('.filter-left');
    const keywordInput = document.querySelector('.search-keyword');
    const locationInput = document.querySelector('.search-location');
    let tags = [];

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

        // Hiển thị tối đa 4 tag
        let visibleTags = tags.slice(0, 4);
        visibleTags.forEach(tag => {
            let span = document.createElement('span');
            span.className = 'search-tag';
            span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
            span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
            filterLeft.appendChild(span);
        });

        // Nếu có nhiều hơn 4 tag, hiển thị +N
        if (tags.length > 4) {
            let more = document.createElement('span');
            more.className = 'search-tag search-tag-more';
            more.innerHTML = `+${tags.length - 4}`;
            more.style.cursor = 'pointer';
            more.onclick = () => {
                // Hiển thị tất cả tag
                filterLeft.querySelectorAll('.search-tag').forEach(e => e.remove());
                tags.forEach(tag => {
                    let span = document.createElement('span');
                    span.className = 'search-tag';
                    span.innerHTML = `${tag} <i class="fa fa-times search-tag-close"></i>`;
                    span.querySelector('.search-tag-close').onclick = () => removeTag(tag);
                    filterLeft.appendChild(span);
                });
                more.remove();
            };
            filterLeft.appendChild(more);
        }
    }

    // Search button
    document.querySelector('.search-btn').addEventListener('click', function() {
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

        if (!keyword && !location) {
            alert('Please enter a job title or location to search!');
        }
    });

    // View toggle
    const gridView = document.querySelector('.search-view-grid');
    const listView = document.querySelector('.search-view-list');

    gridView.onclick = function() {
        this.classList.add('active');
        listView.classList.remove('active');
    };

    listView.onclick = function() {
        this.classList.add('active');
        gridView.classList.remove('active');
    };

    // Advanced Filter Modal
    const modal = document.getElementById('advanceFilterModal');
    const openBtn = document.getElementById('openAdvanceFilter');
    const closeBtn = document.getElementById('closeModal');
    const applyBtn = document.getElementById('applyFilters');
    const resetBtn = document.getElementById('resetFilters');

    // Mapping tags to filter inputs
    const filterMapping = {
        // Experience
        'Freshers': 'exp-fresh',
        '1 - 2 Years': 'exp-1-2',
        '2 - 4 Years': 'exp-2-4',
        '4 - 6 Years': 'exp-4-6',
        '6 - 8 Years': 'exp-6-8',
        '8 - 10 Years': 'exp-8-10',
        '10 - 15 Years': 'exp-10-15',
        '15+ Years': 'exp-15+',
        // Salary
        '$50 - $1000': 'sal-1',
        '$1000 - $2000': 'sal-2',
        '$3000 - $4000': 'sal-3',
        '$4000 - $6000': 'sal-4',
        '$6000 - $8000': 'sal-5',
        '$8000 - $10000': 'sal-6',
        '$10000 - $15000': 'sal-7',
        '$15000+': 'sal-8',
        // Job Type
        'All': 'type-all',
        'Full Time': 'type-full',
        'Part Time': 'type-part',
        'Internship': 'type-intern',
        'Remote': 'type-remote',
        'Temporary': 'type-temp',
        'Contract Base': 'type-contract',
        // Education
        'High School': 'edu-high',
        'Intermediate': 'edu-inter',
        'Graduation': 'edu-grad',
        'Master Degree': 'edu-master',
        'Bachelor Degree': 'edu-bach',
        // Job Level
        'Entry Level': 'level-entry',
        'Mid Level': 'level-mid',
        'Expert Level': 'level-expert'
    };

    openBtn.onclick = () => {
        modal.style.display = 'block';

        // Sync current tags with filter checkboxes/radios
        // First, uncheck all
        document.querySelectorAll('input[type="checkbox"]').forEach(cb => {
            if (!cb.id.includes('all')) cb.checked = false;
        });

        // Then check items that match current tags
        tags.forEach(tag => {
            const inputId = filterMapping[tag];
            if (inputId) {
                const input = document.getElementById(inputId);
                if (input) {
                    input.checked = true;
                }
            }
        });
    };

    closeBtn.onclick = () => modal.style.display = 'none';

    modal.onclick = (e) => {
        if (e.target === modal) modal.style.display = 'none';
    };

    applyBtn.onclick = () => {
        // Get selected filters
        const experience = document.querySelector('input[name="experience"]:checked')?.value;
        const salary = document.querySelector('input[name="salary"]:checked')?.value;
        const level = document.querySelector('input[name="level"]:checked')?.value;

        const jobTypes = Array.from(document.querySelectorAll('input[id^="type-"]:checked'))
            .map(cb => cb.value);

        const education = Array.from(document.querySelectorAll('input[id^="edu-"]:checked'))
            .map(cb => cb.value);

        // Add filters as tags
        if (experience) addTag(experience);
        if (salary) addTag(salary);
        if (level) addTag(level);
        jobTypes.forEach(type => addTag(type));
        education.forEach(edu => addTag(edu));

        modal.style.display = 'none';
        console.log('Filters applied');
    };

    resetBtn.onclick = () => {
        document.querySelectorAll('input[type="checkbox"]').forEach(cb => cb.checked = false);
        document.querySelectorAll('input[type="radio"]').forEach(rb => rb.checked = false);
        console.log('Filters reset');
    };