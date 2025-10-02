console.log("Auto-login script loaded");

    function togglePassword(inputId) {
        const input = document.getElementById(inputId);
        const icon = input.nextElementSibling.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }

    // Lưu thông tin khi submit form với Remember Me
    document.getElementById('loginForm').addEventListener('submit', function(e) {
        console.log("Form submitted");
        const rememberCheckbox = document.getElementById('remember');
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        console.log("Remember checked:", rememberCheckbox.checked);
        console.log("Email:", email);

        if (rememberCheckbox.checked && email && password) {
            console.log("Saving credentials to localStorage");
            localStorage.setItem('joblink_remembered_account', JSON.stringify({
                email: email,
                password: password,
                timestamp: Date.now()
            }));
        } else {
            console.log("Clearing saved credentials");
            localStorage.removeItem('joblink_remembered_account');
        }
    });

    // Auto-login khi load trang
    window.addEventListener('DOMContentLoaded', function() {
        console.log("DOM loaded, checking for remembered account");

        // Kiểm tra xem đã có session chưa (nếu có error/msg thì có nghĩa là đã từ server)
        const hasError = document.querySelector('.alert.alert-danger');
        const hasMsg = document.querySelector('.alert.alert-success');

        if (hasError || hasMsg) {
            console.log("Page has error/message, skipping auto-login");
            return;
        }

        const remembered = localStorage.getItem('joblink_remembered_account');
        if (remembered) {
            console.log("Found remembered account");
            try {
                const acc = JSON.parse(remembered);

                // Kiểm tra thời gian (14 ngày)
                const fourteenDays = 14 * 24 * 60 * 60 * 1000;
                if (Date.now() - acc.timestamp > fourteenDays) {
                    console.log("Remembered account expired");
                    localStorage.removeItem('joblink_remembered_account');
                    return;
                }

                // Kiểm tra xem đã auto-login trong session này chưa
                if (sessionStorage.getItem('joblink_auto_logged')) {
                    console.log("Already auto-logged in this session");
                    return;
                }

                console.log("Attempting auto-login for:", acc.email);

                // Hiện indicator
                const indicator = document.getElementById('autoLoginIndicator');
                indicator.style.display = 'block';

                // Tạo form data
                const formData = new FormData();
                formData.append('email', acc.email);
                formData.append('password', acc.password);
                formData.append('remember', 'on');

                // Submit auto-login
                fetch('/login', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    console.log("Auto-login response status:", response.status);
                    if (response.redirected) {
                        console.log("Auto-login successful, redirecting to:", response.url);
                        sessionStorage.setItem('joblink_auto_logged', '1');
                        window.location.href = response.url;
                    } else {
                        console.log("Auto-login failed - no redirect");
                        indicator.style.display = 'none';
                        // Có thể clear localStorage nếu login thất bại
                        // localStorage.removeItem('joblink_remembered_account');
                    }
                })
                .catch(error => {
                    console.error("Auto-login error:", error);
                    indicator.style.display = 'none';
                });

            } catch (e) {
                console.error("Error parsing remembered account:", e);
                localStorage.removeItem('joblink_remembered_account');
            }
        } else {
            console.log("No remembered account found");
        }
    });

    // Clear auto-logged flag khi logout
    window.addEventListener('beforeunload', function() {
        // Chỉ clear nếu đang navigate đến trang logout
        if (window.location.href.includes('/logout')) {
            sessionStorage.removeItem('joblink_auto_logged');
        }
    });