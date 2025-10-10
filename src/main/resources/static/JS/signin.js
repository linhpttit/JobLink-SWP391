console.log("signin.js loaded");

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

// Save remember info on submit
document.getElementById('loginForm').addEventListener('submit', function () {
  const remember = document.getElementById('remember').checked;
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;

  if (remember && email && password) {
    localStorage.setItem('joblink_remembered_account', JSON.stringify({
      email,
      password,
      timestamp: Date.now()
    }));
  } else {
    localStorage.removeItem('joblink_remembered_account');
  }
});

// Auto-login
window.addEventListener('DOMContentLoaded', function () {
  const indicator = document.getElementById('autoLoginIndicator');
  const hasError = document.querySelector('.alert.alert-danger');
  const hasMsg = document.querySelector('.alert.alert-success');
  if (hasError || hasMsg) return;

  const remembered = localStorage.getItem('joblink_remembered_account');
  if (!remembered) return;

  try {
    const acc = JSON.parse(remembered);
    const fourteenDays = 14 * 24 * 60 * 60 * 1000;
    if (Date.now() - acc.timestamp > fourteenDays) {
      localStorage.removeItem('joblink_remembered_account');
      return;
    }
    if (sessionStorage.getItem('joblink_auto_logged')) return;

    if (indicator) indicator.style.display = 'block';

    const formData = new FormData();
    formData.append('email', acc.email);
    formData.append('password', acc.password);
    formData.append('remember', 'on');

    fetch('/login', { method: 'POST', body: formData })
      .then(res => {
        if (res.redirected) {
          sessionStorage.setItem('joblink_auto_logged', '1');
          window.location.href = res.url;
        } else {
          if (indicator) indicator.style.display = 'none';
        }
      })
      .catch(() => {
        if (indicator) indicator.style.display = 'none';
      });
  } catch {
    localStorage.removeItem('joblink_remembered_account');
  }
});

// Clear flag on logout
window.addEventListener('beforeunload', function () {
  if (window.location.href.includes('/logout')) {
    sessionStorage.removeItem('joblink_auto_logged');
  }
});
