 // Toggle job invitations
    function toggleInvitations(checkbox) {
        const section = document.getElementById('blockedCompaniesSection');
        if (checkbox.checked) {
            section.style.display = 'block';
            checkbox.nextElementSibling.nextElementSibling.textContent = 'Yes';
        } else {
            section.style.display = 'none';
            checkbox.nextElementSibling.nextElementSibling.textContent = 'No';
        }

        // Send to server
        fetch('/settings/toggle-invitations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ enabled: checkbox.checked })
        });
    }

    // Change Password Modal
    function openChangePasswordModal() {
        document.getElementById('changePasswordModal').style.display = 'flex';
    }

    function closeChangePasswordModal() {
        document.getElementById('changePasswordModal').style.display = 'none';
        document.getElementById('changePasswordForm').reset();
    }

    // Delete Account Modal
    function confirmDeleteAccount() {
        document.getElementById('deleteAccountModal').style.display = 'flex';
    }

    function closeDeleteAccountModal() {
        document.getElementById('deleteAccountModal').style.display = 'none';
        document.getElementById('deleteAccountForm').reset();
        document.getElementById('confirmDeleteBtn').disabled = true;
    }

    // Enable delete button only when "DELETE" is typed
    document.getElementById('deleteConfirm')?.addEventListener('input', function(e) {
        const btn = document.getElementById('confirmDeleteBtn');
        if (e.target.value === 'DELETE') {
            btn.disabled = false;
        } else {
            btn.disabled = true;
        }
    });

    // Handle delete account form submission
    document.getElementById('deleteAccountForm')?.addEventListener('submit', function(e) {
        e.preventDefault();

        fetch('/settings/delete-account', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Your account has been deactivated successfully.');
                window.location.href = '/logout';
            } else {
                alert('Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while deleting your account.');
        });
    });

    // Close modals when clicking outside
    window.onclick = function(event) {
        const passwordModal = document.getElementById('changePasswordModal');
        const deleteModal = document.getElementById('deleteAccountModal');

        if (event.target === passwordModal) {
            closeChangePasswordModal();
        }
        if (event.target === deleteModal) {
            closeDeleteAccountModal();
        }
    }

    // Company search functionality (placeholder)
    document.getElementById('companySearch')?.addEventListener('focus', function() {
        // TODO: Implement company search dropdown
        console.log('Company search focused');
    });