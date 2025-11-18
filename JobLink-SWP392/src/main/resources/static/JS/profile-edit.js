

/************** Avatar **************/
document.getElementById("avatarInput")?.addEventListener("change", (e) => {
  const file = e.target.files?.[0];
  if (!file) return;

  // Validate file type
  if (!file.type.startsWith('image/')) {
    showNotification("File phải là hình ảnh (jpg, png, gif)", "error");
    e.target.value = ''; // Clear input
    return;
  }

  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    showNotification("Kích thước file không được vượt quá 5MB", "error");
    e.target.value = ''; // Clear input
    return;
  }

  const reader = new FileReader();
  reader.onload = (ev) => (document.getElementById("avatarPreview").src = ev.target.result);
  reader.readAsDataURL(file);

  const formData = new FormData();
  formData.append("avatar", file);

  // Show loading state
  const uploadBtn = document.querySelector('.btn-upload');
  const originalText = uploadBtn?.innerHTML;
  if (uploadBtn) {
    uploadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang tải lên...';
    uploadBtn.disabled = true;
  }

  fetch("/jobseeker/profile/avatar", { 
    method: "POST", 
    body: formData,
    credentials: 'include' // Đảm bảo gửi session cookie
  })
    .then(async (res) => {
      const data = await res.json();
      if (res.ok && data.success) {
        showNotification(data.message || "Avatar uploaded successfully", "success");
        
        // Update avatar preview với URL mới từ server
        if (data.newAvatarUrl) {
          document.getElementById("avatarPreview").src = data.newAvatarUrl;
          // Update all avatar elements
          document.querySelectorAll('.site-avatar, img[src*="avatar"]').forEach(img => {
            try { 
              if (img.src.includes('avatar') || img.classList.contains('site-avatar')) {
                img.src = data.newAvatarUrl;
              }
            } catch (e) { /* ignore */ }
          });
        }
        
        // Reload sau 1 giây để cập nhật header
        setTimeout(() => location.reload(), 1000);
      } else {
        const errorMsg = data.error || "Failed to upload avatar";
        showNotification(errorMsg, "error");
        e.target.value = ''; // Clear input on error
      }
    })
    .catch((error) => {
      console.error("Error uploading avatar:", error);
      showNotification("Lỗi upload avatar: " + error.message, "error");
      e.target.value = ''; // Clear input on error
    })
    .finally(() => {
      // Restore button state
      if (uploadBtn && originalText) {
        uploadBtn.innerHTML = originalText;
        uploadBtn.disabled = false;
      }
    });
});

/************** Word counter **************/
const selfIntro = document.querySelector('textarea[name="about"]');
const wordCount = document.getElementById("wordCount");
if (selfIntro && wordCount) {
  const updateCount = () => {
    const words = selfIntro.value.trim().split(/\s+/).filter(Boolean).length;
    wordCount.textContent = `${words} words (100-150 required)`;
    wordCount.style.color = words < 100 || words > 150 ? "#dc2626" : "#10b981";
  };
  selfIntro.addEventListener("input", updateCount);
  updateCount();
}

/************** Modal helpers (render theo section) **************/
function createModal(title, content, onSave, targetContainerId = "modalContainer") {
  const overlay = document.createElement("div");
  overlay.className = "modal-overlay";
  // style inline để hiện ngay trong section (không phủ toàn màn hình)
  overlay.style.position = "relative";
  overlay.style.background = "#fff";
  overlay.style.padding = "16px";
  overlay.style.border = "1px solid #eee";
  overlay.style.borderRadius = "12px";
  overlay.style.marginTop = "12px";

  overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header" style="display:flex;justify-content:space-between;align-items:center;gap:12px;">
        <h3 style="margin:0">${title}</h3>
        <button class="modal-close" onclick="closeModal('${targetContainerId}')" aria-label="Close">&times;</button>
      </div>
      <div class="modal-body">${content}</div>
      <div class="modal-footer" style="margin-top:12px;display:flex;gap:8px;justify-content:flex-end">
        <button class="btn-secondary" onclick="closeModal('${targetContainerId}')">Cancel</button>
        <button class="btn-primary" onclick="handleModalSave()"><i class="fas fa-save"></i> Save</button>
      </div>
    </div>
  `;

  const container = document.getElementById(targetContainerId);
  container.innerHTML = "";
  container.appendChild(overlay);

  // expose save handler
  window.handleModalSave = onSave;

  overlay.addEventListener("click", (e) => {
    if (e.target === overlay) closeModal(targetContainerId);
  });
}

function closeModal(targetContainerId = "modalContainer") {
  const container = document.getElementById(targetContainerId);
  if (container) container.innerHTML = "";
}

document.addEventListener("keydown", (e) => e.key === "Escape" && closeModal());

/************** EDUCATION **************/
function openEducationModal(educationId = null) {
  const education = educationId ? (educations || []).find((e) => e.educationId === educationId) : null;
  const isEdit = !!education;

  const content = `
    <form id="educationForm" class="profile-form">
      <input type="hidden" name="educationId" value="${education?.educationId || ""}">

      <div class="form-group">
        <label>University <span class="required">*</span></label>
        <select name="university" required>
          <option value="">Select University</option>
          ${(universities || []).map(u => `<option value="${u}" ${education?.university===u?"selected":""}>${u}</option>`).join("")}
        </select>
      </div>

      <div class="form-group">
        <label>Degree Level <span class="required">*</span></label>
        <select name="degreeLevel" required>
          <option value="">Select Degree</option>
          ${(degreeLevels || []).map(d => `<option value="${d}" ${education?.degreeLevel===d?"selected":""}>${d}</option>`).join("")}
        </select>
      </div>

      <div class="form-grid">
        <div class="form-group">
          <label>Start Date <span class="required">*</span></label>
          <input type="date" name="startDate" value="${education?.startDate || ""}" required>
        </div>
        <div class="form-group">
          <label>Graduation Date</label>
          <input type="date" name="graduationDate" value="${education?.graduationDate || ""}">
        </div>
      </div>

      <div class="form-group">
        <label>Description</label>
        <textarea name="description" rows="4">${education?.description || ""}</textarea>
      </div>
    </form>
  `;

  createModal(isEdit ? "Edit Education" : "Add Education", content, () => saveEducation(isEdit), "educationModalContainer");
}

function saveEducation(isEdit) {
  const form = document.getElementById("educationForm");
  const formData = new FormData(form);
  const url = isEdit ? "/jobseeker/profile/education/update" : "/jobseeker/profile/education/add";

  fetch(url, { method: "POST", body: formData })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Education saved successfully", "success");
        closeModal("educationModalContainer");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to save education", "error");
    })
    .catch(() => showNotification("Error saving education", "error"));
}

function editEducation(id) { openEducationModal(id); }
function deleteEducation(id) {
  if (!confirm("Are you sure you want to delete this education?")) return;
  const fd = new FormData(); fd.append("educationId", id);
  fetch("/jobseeker/profile/education/delete", { method: "POST", body: fd })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Education deleted", "success");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to delete education", "error");
    })
    .catch(() => showNotification("Error deleting education", "error"));
}

/************** EXPERIENCE **************/
function openExperienceModal(experienceId = null) {
  const experience = experienceId ? (experiences || []).find((e) => e.experienceId === experienceId) : null;
  const isEdit = !!experience;

  const content = `
    <form id="experienceForm" class="profile-form">
      <input type="hidden" name="experienceId" value="${experience?.experienceId || ""}">
      <div class="form-group">
        <label>Job Title <span class="required">*</span></label>
        <input type="text" name="jobTitle" value="${experience?.jobTitle || ""}" required>
      </div>
      <div class="form-group">
        <label>Company Name <span class="required">*</span></label>
        <input type="text" name="companyName" value="${experience?.companyName || ""}" required>
      </div>
      <div class="form-grid">
        <div class="form-group">
          <label>Start Date <span class="required">*</span></label>
          <input type="date" name="startDate" value="${experience?.startDate || ""}" required>
        </div>
        <div class="form-group">
          <label>End Date</label>
          <input type="date" name="endDate" value="${experience?.endDate || ""}">
        </div>
      </div>
      <div class="form-group">
        <label>Project Link</label>
        <input type="url" name="projectLink" value="${experience?.projectLink || ""}" placeholder="https://...">
      </div>
    </form>
  `;

  createModal(isEdit ? "Edit Experience" : "Add Experience", content, () => saveExperience(isEdit), "experienceModalContainer");
}

function saveExperience(isEdit) {
  const form = document.getElementById("experienceForm");
  const formData = new FormData(form);
  const url = isEdit ? "/jobseeker/profile/experience/update" : "/jobseeker/profile/experience/add";

  fetch(url, { method: "POST", body: formData })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Experience saved successfully", "success");
        closeModal("experienceModalContainer");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to save experience", "error");
    })
    .catch(() => showNotification("Error saving experience", "error"));
}

function editExperience(id) { openExperienceModal(id); }
function deleteExperience(id) {
  if (!confirm("Are you sure you want to delete this experience?")) return;
  const fd = new FormData(); fd.append("experienceId", id);
  fetch("/jobseeker/profile/experience/delete", { method: "POST", body: fd })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Experience deleted", "success");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to delete experience", "error");
    })
    .catch(() => showNotification("Error deleting experience", "error"));
}

/************** SKILL **************/
function openSkillModal(skillId = null) {
  const skill = skillId ? (skills || []).find((s) => s.skillId === skillId) : null;
  const isEdit = !!skill;

  const content = `
    <form id="skillForm" class="profile-form">
      <input type="hidden" name="skillId" value="${skill?.skillId || ""}">
      <div class="form-group">
        <label>Skill Name <span class="required">*</span></label>
        <input type="text" name="skillName" value="${skill?.skillName || ""}" list="skillSuggestions" required>
        <datalist id="skillSuggestions">
          ${(commonSkills || []).map(s => `<option value="${s}">`).join("")}
        </datalist>
      </div>
      <div class="form-group">
        <label>Years of Experience <span class="required">*</span></label>
        <input type="number" name="yearsOfExperience" value="${skill?.yearsOfExperience ?? 0}" min="0" max="50" required>
      </div>
      <div class="form-group">
        <label>Description</label>
        <textarea name="description" rows="4">${skill?.description || ""}</textarea>
      </div>
    </form>
  `;

  createModal(isEdit ? "Edit Skill" : "Add Skill", content, () => saveSkill(isEdit), "skillsModalContainer");
}

function saveSkill(isEdit) {
  const form = document.getElementById("skillForm");
  const formData = new FormData(form);
  const url = isEdit ? "/jobseeker/profile/skill/update" : "/jobseeker/profile/skill/add";

  fetch(url, { method: "POST", body: formData })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Skill saved successfully", "success");
        closeModal("skillsModalContainer");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to save skill", "error");
    })
    .catch(() => showNotification("Error saving skill", "error"));
}

function editSkill(id) { openSkillModal(id); }
function deleteSkill(id) {
  if (!confirm("Are you sure you want to delete this skill?")) return;
  const fd = new FormData(); fd.append("skillId", id);
  fetch("/jobseeker/profile/skill/delete", { method: "POST", body: fd })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Skill deleted", "success");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to delete skill", "error");
    })
    .catch(() => showNotification("Error deleting skill", "error"));
}

/************** LANGUAGE **************/
function openLanguageModal(languageId = null) {
  const language = languageId ? (languages || []).find((l) => l.languageId === languageId) : null;
  const isEdit = !!language;

  const content = `
    <form id="languageForm" class="profile-form">
      <input type="hidden" name="languageId" value="${language?.languageId || ""}">
      <div class="form-group">
        <label>Language <span class="required">*</span></label>
        <select name="languageName" id="languageSelect" required>
          <option value="">Select Language</option>
          ${(recognizedLanguages || []).map(l => `<option value="${l}" ${language?.languageName===l?"selected":""}>${l}</option>`).join("")}
        </select>
      </div>
      <div class="form-group">
        <label>Certificate Type <span class="required">*</span></label>
        <select name="certificateType" id="certificateTypeSelect" required>
          <option value="">Select Certificate</option>
        </select>
      </div>
    </form>
  `;

  createModal(isEdit ? "Edit Language" : "Add Language", content, () => saveLanguage(isEdit), "languagesModalContainer");

  setTimeout(() => {
    const selectedLang = language?.languageName || document.getElementById("languageSelect")?.value;
    updateCertificateTypes(selectedLang, language?.certificateType || null);
    document.getElementById("languageSelect")?.addEventListener("change", (ev) => {
      updateCertificateTypes(ev.target.value, null);
    });
  }, 0);
}

function updateCertificateTypes(languageName, selectedCert) {
  const certSelect = document.getElementById("certificateTypeSelect");
  if (!languageName || !certSelect) {
    if (certSelect) certSelect.innerHTML = '<option value="">Select Certificate</option>';
    return;
  }
  fetch(`/jobseeker/profile/certificate-types?language=${encodeURIComponent(languageName)}`)
    .then(r => r.json())
    .then(types => {
      certSelect.innerHTML =
        '<option value="">Select Certificate</option>' +
        (types || []).map(t => `<option value="${t}" ${t===selectedCert?"selected":""}>${t}</option>`).join("");
    });
}

function saveLanguage(isEdit) {
  const form = document.getElementById("languageForm");
  const formData = new FormData(form);
  const url = isEdit ? "/jobseeker/profile/language/update" : "/jobseeker/profile/language/add";

  fetch(url, { method: "POST", body: formData })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Language saved successfully", "success");
        closeModal("languagesModalContainer");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to save language", "error");
    })
    .catch(() => showNotification("Error saving language", "error"));
}

function editLanguage(id) { openLanguageModal(id); }
function deleteLanguage(id) {
  if (!confirm("Are you sure you want to delete this language?")) return;
  const fd = new FormData(); fd.append("languageId", id);
  fetch("/jobseeker/profile/language/delete", { method: "POST", body: fd })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Language deleted", "success");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to delete language", "error");
    })
    .catch(() => showNotification("Error deleting language", "error"));
}

/************** CERTIFICATE **************/
function openCertificateModal(certificateId = null) {
  const certificate = certificateId ? (certificates || []).find((c) => c.certificateId === certificateId) : null;
  const isEdit = !!certificate;

  const content = `
    <form id="certificateForm" class="profile-form" enctype="multipart/form-data">
      <input type="hidden" name="certificateId" value="${certificate?.certificateId || ""}">
      <div class="form-group">
        <label>Issuing Organization <span class="required">*</span></label>
        <input type="text" name="issuingOrganization" value="${certificate?.issuingOrganization || ""}" required>
      </div>
      <div class="form-group">
        <label>Year of Completion <span class="required">*</span></label>
        <input type="number" name="yearOfCompletion"
               value="${certificate?.yearOfCompletion || new Date().getFullYear()}"
               min="1950" max="${new Date().getFullYear()}" required>
      </div>
      <div class="form-group">
        <label>Certificate Image</label>
        <input type="file" name="certificateImage" accept="image/*">
        ${certificate?.certificateImageUrl ? `<small>Current image will be kept if no new image is uploaded</small>` : ""}
      </div>
    </form>
  `;

  createModal(isEdit ? "Edit Certificate" : "Add Certificate", content, () => saveCertificate(isEdit), "certificatesModalContainer");
}

function saveCertificate(isEdit) {
  const form = document.getElementById("certificateForm");
  const formData = new FormData(form);
  const url = isEdit ? "/jobseeker/profile/certificate/update" : "/jobseeker/profile/certificate/add";

  fetch(url, { method: "POST", body: formData })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Certificate saved successfully", "success");
        closeModal("certificatesModalContainer");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to save certificate", "error");
    })
    .catch(() => showNotification("Error saving certificate", "error"));
}

function editCertificate(id) { openCertificateModal(id); }
function deleteCertificate(id) {
  if (!confirm("Are you sure you want to delete this certificate?")) return;
  const fd = new FormData(); fd.append("certificateId", id);
  fetch("/jobseeker/profile/certificate/delete", { method: "POST", body: fd })
    .then(r => r.json())
    .then(d => {
      if (d.success) {
        showNotification("Certificate deleted", "success");
        setTimeout(() => location.reload(), 600);
      } else showNotification(d.error || "Failed to delete certificate", "error");
    })
    .catch(() => showNotification("Error deleting certificate", "error"));
}

/************** Notification **************/
function showNotification(message, type = "info") {
  const n = document.createElement("div");
  n.className = `notification notification-${type}`;
  n.innerHTML = `
    <i class="fas fa-${type === "success" ? "check-circle" : "exclamation-circle"}"></i>
    <span>${message}</span>
  `;

  if (!document.querySelector("style[data-notification]")) {
    const s = document.createElement("style");
    s.setAttribute("data-notification", "true");
    s.textContent = `
      .notification{position:fixed;top:20px;right:20px;padding:15px 20px;border-radius:8px;
      box-shadow:0 4px 12px rgba(0,0,0,.15);display:flex;gap:10px;align-items:center;z-index:10000;
      animation:slideIn .3s ease;font-weight:500}
      .notification-success{background:#dcfce7;color:#166534;border:1px solid #86efac}
      .notification-error{background:#fee2e2;color:#991b1b;border:1px solid #fca5a5}
      @keyframes slideIn{from{transform:translateX(400px);opacity:0}to{transform:translateX(0);opacity:1}}
    `;
    document.head.appendChild(s);
  }
  document.body.appendChild(n);
  setTimeout(() => { n.style.animation = "slideIn .3s ease reverse"; setTimeout(() => n.remove(), 300); }, 2800);
}

/************** Expose to global (cho inline onclick) **************/
window.openEducationModal   = openEducationModal;
window.editEducation        = editEducation;
window.deleteEducation      = deleteEducation;

window.openExperienceModal  = openExperienceModal;
window.editExperience       = editExperience;
window.deleteExperience     = deleteExperience;

window.openSkillModal       = openSkillModal;
window.editSkill            = editSkill;
window.deleteSkill          = deleteSkill;

window.openLanguageModal    = openLanguageModal;
window.editLanguage         = editLanguage;
window.deleteLanguage       = deleteLanguage;

window.openCertificateModal = openCertificateModal;
window.editCertificate      = editCertificate;
window.deleteCertificate    = deleteCertificate;

/************** (Tuỳ chọn) Event delegation nếu bỏ inline onclick **************/
document.addEventListener("click", (e) => {
  const el = e.target.closest("[data-action]");
  if (!el) return;
  const action = el.getAttribute("data-action");
  if (action === "add-education")     { e.preventDefault(); openEducationModal(); }
  if (action === "add-experience")    { e.preventDefault(); openExperienceModal(); }
  if (action === "add-skill")         { e.preventDefault(); openSkillModal(); }
  if (action === "add-language")      { e.preventDefault(); openLanguageModal(); }
  if (action === "add-certificate")   { e.preventDefault(); openCertificateModal(); }
});
