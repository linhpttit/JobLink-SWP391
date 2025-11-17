(function(){
  const btn = document.getElementById('usermenuBtn');
  const dropdown = document.getElementById('usermenuDropdown');

  if (!btn || !dropdown) return;

  const close = () => {
    dropdown.classList.remove('open');
    btn.setAttribute('aria-expanded','false');
  };
  const open = () => {
    dropdown.classList.add('open');
    btn.setAttribute('aria-expanded','true');
  };

  btn.addEventListener('click', (e) => {
    e.stopPropagation();
    dropdown.classList.contains('open') ? close() : open();
  });

  document.addEventListener('click', (e) => {
    if (!dropdown.contains(e.target) && e.target !== btn) close();
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') close();
  });
})();
