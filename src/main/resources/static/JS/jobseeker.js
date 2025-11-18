document.addEventListener("DOMContentLoaded", () => {
  loadStats();
  loadJobSeekers();
});

async function loadStats() {
  const res = await fetch("/admin/api/jobseekers/stats"); // ✅ thêm /admin
  const data = await res.json();
  document.getElementById("totalJobSeekers").textContent = data.total;
  document.getElementById("activeJobSeekers").textContent = data.active;
  document.getElementById("lockedJobSeekers").textContent = data.locked;
  document.getElementById("totalCVs").textContent = data.cv;
}

async function loadJobSeekers() {
  const keyword = document.getElementById("searchInput").value;
  const exp = document.getElementById("experienceFilter").value || null;

  const res = await fetch(`/admin/api/jobseekers?keyword=${keyword || ""}&experience=${exp || ""}`); // ✅ thêm /admin
  const jobseekers = await res.json();

  const tbody = document.querySelector(".jobseeker-table tbody");
  tbody.innerHTML = "";

  if (jobseekers.length === 0) {
    tbody.innerHTML = `<tr class="no-data"><td colspan="10" style="text-align:center;">Không có dữ liệu</td></tr>`;
    return;
  }

  jobseekers.forEach(j => {
    const row = `
      <tr>
        <td>${j.seekerId}</td>
        <td>${j.fullName || ""}</td>
        <td>${j.email || ""}</td>
        <td>${j.gender || ""}</td>
        <td>${j.address || ""}</td>
        <td>${j.experienceYears || 0}</td>
        <td>${j.headline || ""}</td>
        <td>${j.receiveInvitations ? "Đang hoạt động" : "Đã khóa"}</td>
        <td>${j.updatedAt ? j.updatedAt.split("T")[0] : ""}</td>
        <td><button class="btn btn-sm btn-danger">Xóa</button></td>
      </tr>`;
    tbody.insertAdjacentHTML("beforeend", row);
  });
}
