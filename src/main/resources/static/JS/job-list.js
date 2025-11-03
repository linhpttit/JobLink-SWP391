document.addEventListener('click', async (e) => {
  const btn = e.target.closest('.wish');
  if (!btn) return;

  const id = btn.getAttribute('data-id');
  btn.disabled = true;

  try {
    const res = await fetch(`/employer/jobs/${id}/wish`, {
      method: 'POST',
      headers: { 'X-Requested-With': 'fetch', 'Content-Type': 'application/json' },
      body: JSON.stringify({})
    });
    if (!res.ok) throw new Error('Network error');
    const json = await res.json();
    btn.classList.toggle('saved', json.wished === true);
    btn.setAttribute('aria-label', json.wished ? 'Bỏ lưu việc này' : 'Lưu việc này');
  } catch (err) {
    alert('Không thể lưu công việc. Vui lòng thử lại.');
  } finally {
    btn.disabled = false;
  }
});
