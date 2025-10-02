  // Modal functionality
    const applyModal = document.getElementById('applyModal');
    const applyNowBtn = document.getElementById('applyNowBtn');
    const closeModal = document.getElementById('closeModal');
    const cancelBtn = document.getElementById('cancelBtn');

    // Open modal
    applyNowBtn.addEventListener('click', function() {
        applyModal.classList.add('active');
        document.body.style.overflow = 'hidden';
    });

    // Close modal
    function closeApplyModal() {
        applyModal.classList.remove('active');
        document.body.style.overflow = 'auto';
    }

    closeModal.addEventListener('click', closeApplyModal);
    cancelBtn.addEventListener('click', closeApplyModal);

    // Close when clicking outside
    applyModal.addEventListener('click', function(e) {
        if (e.target === applyModal) {
            closeApplyModal();
        }
    });

    // Close with ESC key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && applyModal.classList.contains('active')) {
            closeApplyModal();
        }
    });

    // Bookmark functionality
    const bookmarkBtn = document.querySelector('.btn-bookmark-large');
    let isBookmarked = false;

    bookmarkBtn.addEventListener('click', function() {
        isBookmarked = !isBookmarked;
        const icon = this.querySelector('i');

        if (isBookmarked) {
            icon.classList.remove('far');
            icon.classList.add('fas');
            this.style.color = '#1976d2';
            this.style.borderColor = '#1976d2';
        } else {
            icon.classList.remove('fas');
            icon.classList.add('far');
            this.style.color = '';
            this.style.borderColor = '';
        }
    });

    // Social share buttons
    const socialButtons = document.querySelectorAll('.social-btn');
    socialButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const jobTitle = 'Senior UX Designer';
            const jobUrl = window.location.href;

            if (this.classList.contains('facebook')) {
                window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(jobUrl)}`, '_blank');
            } else if (this.classList.contains('twitter')) {
                window.open(`https://twitter.com/intent/tweet?text=${encodeURIComponent(jobTitle)}&url=${encodeURIComponent(jobUrl)}`, '_blank');
            } else if (this.classList.contains('linkedin')) {
                window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(jobUrl)}`, '_blank');
            } else if (this.classList.contains('whatsapp')) {
                window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(jobTitle + ' ' + jobUrl)}`, '_blank');
            } else if (this.classList.contains('pinterest')) {
                window.open(`https://pinterest.com/pin/create/button/?url=${encodeURIComponent(jobUrl)}&description=${encodeURIComponent(jobTitle)}`, '_blank');
            }
        });
    });

    // Form submit
    const applyForm = document.getElementById('applyForm');
    const submitBtn = document.querySelector('.btn-submit');

    submitBtn.addEventListener('click', function(e) {
        e.preventDefault();

        // Add loading state
        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
        this.disabled = true;

        // Simulate form submission
        setTimeout(() => {
            alert('Application submitted successfully!');
            closeApplyModal();
            this.innerHTML = 'Apply Now <i class="fas fa-arrow-right"></i>';
            this.disabled = false;
            applyForm.reset();
        }, 1500);
    });

    // Editor toolbar buttons (basic functionality)
    const editorButtons = document.querySelectorAll('.editor-btn');
    editorButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            // Visual feedback
            this.style.background = '#e2e8f0';
            setTimeout(() => {
                this.style.background = '';
            }, 200);
        });
    });