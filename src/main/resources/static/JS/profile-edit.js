//////document.getElementById("avatarInput")?.addEventListener("change", (e) => {
//////  const file = e.target.files[0]
//////  if (!file) return
//////
//////  // Preview
//////  const reader = new FileReader()
//////  reader.onload = (e) => {
//////    document.getElementById("avatarPreview").src = e.target.result
//////  }
//////  reader.readAsDataURL(file)
//////
//////  // Upload
//////  const formData = new FormData()
//////  formData.append("avatar", file)
//////
//////  fetch("/jobseeker/profile/avatar", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => {
//////      if (response.ok) {
//////        showNotification("Avatar uploaded successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification("Failed to upload avatar", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error uploading avatar", "error")
//////    })
//////})
//////
//////// Word Counter for Self Introduction
//////const selfIntroTextarea = document.querySelector('textarea[name="about"]')
//////const wordCountDisplay = document.getElementById("wordCount")
//////
//////if (selfIntroTextarea && wordCountDisplay) {
//////  function updateWordCount() {
//////    const text = selfIntroTextarea.value.trim()
//////    const words = text ? text.split(/\s+/).length : 0
//////    wordCountDisplay.textContent = `${words} words (100-150 required)`
//////
//////    if (words < 100 || words > 150) {
//////      wordCountDisplay.style.color = "#dc2626"
//////    } else {
//////      wordCountDisplay.style.color = "#10b981"
//////    }
//////  }
//////
//////  selfIntroTextarea.addEventListener("input", updateWordCount)
//////  updateWordCount()
//////}
//////
//////// Modal Management
//////function createModal(title, content, onSave) {
//////  const overlay = document.createElement("div")
//////  overlay.className = "modal-overlay"
//////  overlay.innerHTML = `
//////        <div class="modal">
//////            <div class="modal-header">
//////                <h3>${title}</h3>
//////                <button class="modal-close" onclick="closeModal()">&times;</button>
//////            </div>
//////            <div class="modal-body">
//////                ${content}
//////            </div>
//////            <div class="modal-footer">
//////                <button class="btn-secondary" onclick="closeModal()">Cancel</button>
//////                <button class="btn-primary" onclick="handleModalSave()">
//////                    <i class="fas fa-save"></i> Save
//////                </button>
//////            </div>
//////        </div>
//////    `
//////
//////  document.getElementById("modalContainer").innerHTML = ""
//////  document.getElementById("modalContainer").appendChild(overlay)
//////
//////  window.handleModalSave = onSave
//////
//////  // Close on overlay click
//////  overlay.addEventListener("click", (e) => {
//////    if (e.target === overlay) closeModal()
//////  })
//////}
//////
//////function closeModal() {
//////  document.getElementById("modalContainer").innerHTML = ""
//////}
//////
//////// Education Functions
//////const educations = [] // Declare the educations variable
//////const universities = [] // Declare the universities variable
//////const degreeLevels = [] // Declare the degreeLevels variable
//////
//////function openEducationModal(educationId = null) {
//////  const education = educationId ? educations.find((e) => e.educationId === educationId) : null
//////  const isEdit = !!education
//////
//////  const content = `
//////        <form id="educationForm" class="profile-form">
//////            <input type="hidden" name="educationId" value="${education?.educationId || ""}">
//////
//////            <div class="form-group">
//////                <label>University <span class="required">*</span></label>
//////                <select name="university" required>
//////                    <option value="">Select University</option>
//////                    ${universities
//////                      .map(
//////                        (u) => `
//////                        <option value="${u}" ${education?.university === u ? "selected" : ""}>${u}</option>
//////                    `,
//////                      )
//////                      .join("")}
//////                </select>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Degree Level <span class="required">*</span></label>
//////                <select name="degreeLevel" required>
//////                    <option value="">Select Degree</option>
//////                    ${degreeLevels
//////                      .map(
//////                        (d) => `
//////                        <option value="${d}" ${education?.degreeLevel === d ? "selected" : ""}>${d}</option>
//////                    `,
//////                      )
//////                      .join("")}
//////                </select>
//////            </div>
//////
//////            <div class="form-grid">
//////                <div class="form-group">
//////                    <label>Start Date <span class="required">*</span></label>
//////                    <input type="date" name="startDate" value="${education?.startDate || ""}" required>
//////                </div>
//////
//////                <div class="form-group">
//////                    <label>Graduation Date</label>
//////                    <input type="date" name="graduationDate" value="${education?.graduationDate || ""}">
//////                </div>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Description</label>
//////                <textarea name="description" rows="4">${education?.description || ""}</textarea>
//////            </div>
//////        </form>
//////    `
//////
//////  createModal(isEdit ? "Edit Education" : "Add Education", content, () => saveEducation(isEdit))
//////}
//////
//////function saveEducation(isEdit) {
//////  const form = document.getElementById("educationForm")
//////  const formData = new FormData(form)
//////
//////  const url = isEdit ? "/jobseeker/profile/education/update" : "/jobseeker/profile/education/add"
//////
//////  fetch(url, {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Education saved successfully", "success")
//////        closeModal()
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to save education", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error saving education", "error")
//////    })
//////}
//////
//////function editEducation(educationId) {
//////  openEducationModal(educationId)
//////}
//////
//////function deleteEducation(educationId) {
//////  if (!confirm("Are you sure you want to delete this education?")) return
//////
//////  const formData = new FormData()
//////  formData.append("educationId", educationId)
//////
//////  fetch("/jobseeker/profile/education/delete", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Education deleted successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to delete education", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error deleting education", "error")
//////    })
//////}
//////
//////// Experience Functions
//////const experiences = [] // Declare the experiences variable
//////
//////function openExperienceModal(experienceId = null) {
//////  const experience = experienceId ? experiences.find((e) => e.experienceId === experienceId) : null
//////  const isEdit = !!experience
//////
//////  const content = `
//////        <form id="experienceForm" class="profile-form">
//////            <input type="hidden" name="experienceId" value="${experience?.experienceId || ""}">
//////
//////            <div class="form-group">
//////                <label>Job Title <span class="required">*</span></label>
//////                <input type="text" name="jobTitle" value="${experience?.jobTitle || ""}" required>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Company Name <span class="required">*</span></label>
//////                <input type="text" name="companyName" value="${experience?.companyName || ""}" required>
//////            </div>
//////
//////            <div class="form-grid">
//////                <div class="form-group">
//////                    <label>Start Date <span class="required">*</span></label>
//////                    <input type="date" name="startDate" value="${experience?.startDate || ""}" required>
//////                </div>
//////
//////                <div class="form-group">
//////                    <label>End Date</label>
//////                    <input type="date" name="endDate" value="${experience?.endDate || ""}">
//////                </div>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Project Link</label>
//////                <input type="url" name="projectLink" value="${experience?.projectLink || ""}" placeholder="https://...">
//////            </div>
//////        </form>
//////    `
//////
//////  createModal(isEdit ? "Edit Experience" : "Add Experience", content, () => saveExperience(isEdit))
//////}
//////
//////function saveExperience(isEdit) {
//////  const form = document.getElementById("experienceForm")
//////  const formData = new FormData(form)
//////
//////  const url = isEdit ? "/jobseeker/profile/experience/update" : "/jobseeker/profile/experience/add"
//////
//////  fetch(url, {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Experience saved successfully", "success")
//////        closeModal()
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to save experience", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error saving experience", "error")
//////    })
//////}
//////
//////function editExperience(experienceId) {
//////  openExperienceModal(experienceId)
//////}
//////
//////function deleteExperience(experienceId) {
//////  if (!confirm("Are you sure you want to delete this experience?")) return
//////
//////  const formData = new FormData()
//////  formData.append("experienceId", experienceId)
//////
//////  fetch("/jobseeker/profile/experience/delete", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Experience deleted successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to delete experience", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error deleting experience", "error")
//////    })
//////}
//////
//////// Skill Functions
//////const skills = [] // Declare the skills variable
//////const commonSkills = [] // Declare the commonSkills variable
//////
//////function openSkillModal(skillId = null) {
//////  const skill = skillId ? skills.find((s) => s.skillId === skillId) : null
//////  const isEdit = !!skill
//////
//////  const content = `
//////        <form id="skillForm" class="profile-form">
//////            <input type="hidden" name="skillId" value="${skill?.skillId || ""}">
//////
//////            <div class="form-group">
//////                <label>Skill Name <span class="required">*</span></label>
//////                <input type="text" name="skillName" value="${skill?.skillName || ""}"
//////                       list="skillSuggestions" required>
//////                <datalist id="skillSuggestions">
//////                    ${commonSkills.map((s) => `<option value="${s}">`).join("")}
//////                </datalist>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Years of Experience <span class="required">*</span></label>
//////                <input type="number" name="yearsOfExperience" value="${skill?.yearsOfExperience || 0}"
//////                       min="0" max="50" required>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Description</label>
//////                <textarea name="description" rows="4">${skill?.description || ""}</textarea>
//////            </div>
//////        </form>
//////    `
//////
//////  createModal(isEdit ? "Edit Skill" : "Add Skill", content, () => saveSkill(isEdit))
//////}
//////
//////function saveSkill(isEdit) {
//////  const form = document.getElementById("skillForm")
//////  const formData = new FormData(form)
//////
//////  const url = isEdit ? "/jobseeker/profile/skill/update" : "/jobseeker/profile/skill/add"
//////
//////  fetch(url, {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Skill saved successfully", "success")
//////        closeModal()
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to save skill", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error saving skill", "error")
//////    })
//////}
//////
//////function editSkill(skillId) {
//////  openSkillModal(skillId)
//////}
//////
//////function deleteSkill(skillId) {
//////  if (!confirm("Are you sure you want to delete this skill?")) return
//////
//////  const formData = new FormData()
//////  formData.append("skillId", skillId)
//////
//////  fetch("/jobseeker/profile/skill/delete", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Skill deleted successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to delete skill", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error deleting skill", "error")
//////    })
//////}
//////
//////// Language Functions
//////const languages = [] // Declare the languages variable
//////const recognizedLanguages = [] // Declare the recognizedLanguages variable
//////
//////function openLanguageModal(languageId = null) {
//////  const language = languageId ? languages.find((l) => l.languageId === languageId) : null
//////  const isEdit = !!language
//////
//////  const content = `
//////        <form id="languageForm" class="profile-form">
//////            <input type="hidden" name="languageId" value="${language?.languageId || ""}">
//////
//////            <div class="form-group">
//////                <label>Language <span class="required">*</span></label>
//////                <select name="languageName" id="languageSelect" required onchange="updateCertificateTypes()">
//////                    <option value="">Select Language</option>
//////                    ${recognizedLanguages
//////                      .map(
//////                        (l) => `
//////                        <option value="${l}" ${language?.languageName === l ? "selected" : ""}>${l}</option>
//////                    `,
//////                      )
//////                      .join("")}
//////                </select>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Certificate Type <span class="required">*</span></label>
//////                <select name="certificateType" id="certificateTypeSelect" required>
//////                    <option value="">Select Certificate</option>
//////                </select>
//////            </div>
//////        </form>
//////    `
//////
//////  createModal(isEdit ? "Edit Language" : "Add Language", content, () => saveLanguage(isEdit))
//////
//////  // Initialize certificate types
//////  setTimeout(() => {
//////    if (language?.languageName) {
//////      updateCertificateTypes(language.languageName, language.certificateType)
//////    }
//////  }, 100)
//////}
//////
//////function updateCertificateTypes(selectedLang = null, selectedCert = null) {
//////  const langSelect = document.getElementById("languageSelect")
//////  const certSelect = document.getElementById("certificateTypeSelect")
//////
//////  const language = selectedLang || langSelect?.value
//////  if (!language) {
//////    certSelect.innerHTML = '<option value="">Select Certificate</option>'
//////    return
//////  }
//////
//////  fetch(`/jobseeker/profile/certificate-types?language=${encodeURIComponent(language)}`)
//////    .then((response) => response.json())
//////    .then((types) => {
//////      certSelect.innerHTML =
//////        '<option value="">Select Certificate</option>' +
//////        types.map((t) => `<option value="${t}" ${t === selectedCert ? "selected" : ""}>${t}</option>`).join("")
//////    })
//////}
//////
//////function saveLanguage(isEdit) {
//////  const form = document.getElementById("languageForm")
//////  const formData = new FormData(form)
//////
//////  const url = isEdit ? "/jobseeker/profile/language/update" : "/jobseeker/profile/language/add"
//////
//////  fetch(url, {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Language saved successfully", "success")
//////        closeModal()
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to save language", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error saving language", "error")
//////    })
//////}
//////
//////function editLanguage(languageId) {
//////  openLanguageModal(languageId)
//////}
//////
//////function deleteLanguage(languageId) {
//////  if (!confirm("Are you sure you want to delete this language?")) return
//////
//////  const formData = new FormData()
//////  formData.append("languageId", languageId)
//////
//////  fetch("/jobseeker/profile/language/delete", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Language deleted successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to delete language", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error deleting language", "error")
//////    })
//////}
//////
//////// Certificate Functions
//////const certificates = [] // Declare the certificates variable
//////
//////function openCertificateModal(certificateId = null) {
//////  const certificate = certificateId ? certificates.find((c) => c.certificateId === certificateId) : null
//////  const isEdit = !!certificate
//////
//////  const content = `
//////        <form id="certificateForm" class="profile-form" enctype="multipart/form-data">
//////            <input type="hidden" name="certificateId" value="${certificate?.certificateId || ""}">
//////
//////            <div class="form-group">
//////                <label>Issuing Organization <span class="required">*</span></label>
//////                <input type="text" name="issuingOrganization" value="${certificate?.issuingOrganization || ""}" required>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Year of Completion <span class="required">*</span></label>
//////                <input type="number" name="yearOfCompletion" value="${certificate?.yearOfCompletion || new Date().getFullYear()}"
//////                       min="1950" max="${new Date().getFullYear()}" required>
//////            </div>
//////
//////            <div class="form-group">
//////                <label>Certificate Image</label>
//////                <input type="file" name="certificateImage" accept="image/*">
//////                ${certificate?.certificateImageUrl ? `<small>Current image will be kept if no new image is uploaded</small>` : ""}
//////            </div>
//////        </form>
//////    `
//////
//////  createModal(isEdit ? "Edit Certificate" : "Add Certificate", content, () => saveCertificate(isEdit))
//////}
//////
//////function saveCertificate(isEdit) {
//////  const form = document.getElementById("certificateForm")
//////  const formData = new FormData(form)
//////
//////  const url = isEdit ? "/jobseeker/profile/certificate/update" : "/jobseeker/profile/certificate/add"
//////
//////  fetch(url, {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Certificate saved successfully", "success")
//////        closeModal()
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to save certificate", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error saving certificate", "error")
//////    })
//////}
//////
//////function editCertificate(certificateId) {
//////  openCertificateModal(certificateId)
//////}
//////
//////function deleteCertificate(certificateId) {
//////  if (!confirm("Are you sure you want to delete this certificate?")) return
//////
//////  const formData = new FormData()
//////  formData.append("certificateId", certificateId)
//////
//////  fetch("/jobseeker/profile/certificate/delete", {
//////    method: "POST",
//////    body: formData,
//////  })
//////    .then((response) => response.json())
//////    .then((data) => {
//////      if (data.success) {
//////        showNotification("Certificate deleted successfully", "success")
//////        setTimeout(() => location.reload(), 1000)
//////      } else {
//////        showNotification(data.error || "Failed to delete certificate", "error")
//////      }
//////    })
//////    .catch((error) => {
//////      showNotification("Error deleting certificate", "error")
//////    })
//////}
//////
//////// Notification System
//////function showNotification(message, type = "info") {
//////  const notification = document.createElement("div")
//////  notification.className = `notification notification-${type}`
//////  notification.innerHTML = `
//////        <i class="fas fa-${type === "success" ? "check-circle" : "exclamation-circle"}"></i>
//////        <span>${message}</span>
//////    `
//////
//////  const style = document.createElement("style")
//////  style.textContent = `
//////        .notification {
//////            position: fixed;
//////            top: 20px;
//////            right: 20px;
//////            padding: 15px 20px;
//////            border-radius: 8px;
//////            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
//////            display: flex;
//////            align-items: center;
//////            gap: 10px;
//////            z-index: 10000;
//////            animation: slideIn 0.3s ease;
//////            font-weight: 500;
//////        }
//////        .notification-success {
//////            background: #dcfce7;
//////            color: #166534;
//////            border: 1px solid #86efac;
//////        }
//////        .notification-error {
//////            background: #fee2e2;
//////            color: #991b1b;
//////            border: 1px solid #fca5a5;
//////        }
//////        @keyframes slideIn {
//////            from {
//////                transform: translateX(400px);
//////                opacity: 0;
//////            }
//////            to {
//////                transform: translateX(0);
//////                opacity: 1;
//////            }
//////        }
//////    `
//////
//////  if (!document.querySelector("style[data-notification]")) {
//////    style.setAttribute("data-notification", "true")
//////    document.head.appendChild(style)
//////  }
//////
//////  document.body.appendChild(notification)
//////
//////  setTimeout(() => {
//////    notification.style.animation = "slideIn 0.3s ease reverse"
//////    setTimeout(() => notification.remove(), 300)
//////  }, 3000)
//////}
//////
//////// Keyboard shortcuts
//////document.addEventListener("keydown", (e) => {
//////  if (e.key === "Escape") {
//////    closeModal()
//////  }
//////})
////// ✅ Avatar upload
////document.getElementById("avatarInput")?.addEventListener("change", (e) => {
////  const file = e.target.files[0];
////  if (!file) return;
////
////  const reader = new FileReader();
////  reader.onload = (ev) => (document.getElementById("avatarPreview").src = ev.target.result);
////  reader.readAsDataURL(file);
////
////  const formData = new FormData();
////  formData.append("avatar", file);
////
////  fetch("/jobseeker/profile/avatar", { method: "POST", body: formData })
////    .then((res) => {
////      if (res.ok) showNotification("Avatar uploaded successfully", "success");
////      else showNotification("Failed to upload avatar", "error");
////      setTimeout(() => location.reload(), 1000);
////    })
////    .catch(() => showNotification("Error uploading avatar", "error"));
////});
////
////// ✅ Word counter
////const selfIntro = document.querySelector('textarea[name="about"]');
////const wordCount = document.getElementById("wordCount");
////if (selfIntro && wordCount) {
////  const updateCount = () => {
////    const words = selfIntro.value.trim().split(/\s+/).filter(Boolean).length;
////    wordCount.textContent = `${words} words (100-150 required)`;
////    wordCount.style.color = words < 100 || words > 150 ? "#dc2626" : "#10b981";
////  };
////  selfIntro.addEventListener("input", updateCount);
////  updateCount();
////}
////
////// ✅ Modal functions
////function createModal(title, content, onSave) {
////  const overlay = document.createElement("div");
////  overlay.className = "modal-overlay";
////  overlay.innerHTML = `
////    <div class="modal">
////      <div class="modal-header">
////        <h3>${title}</h3>
////        <button class="modal-close" onclick="closeModal()">&times;</button>
////      </div>
////      <div class="modal-body">${content}</div>
////      <div class="modal-footer">
////        <button class="btn-secondary" onclick="closeModal()">Cancel</button>
////        <button class="btn-primary" onclick="handleModalSave()">
////          <i class="fas fa-save"></i> Save
////        </button>
////      </div>
////    </div>`;
////  document.getElementById("modalContainer").innerHTML = "";
////  document.getElementById("modalContainer").appendChild(overlay);
////  window.handleModalSave = onSave;
////  overlay.addEventListener("click", (e) => e.target === overlay && closeModal());
////}
////
////function closeModal() {
////  document.getElementById("modalContainer").innerHTML = "";
////}
////
////// ✅ CRUD handlers — chỉ cần giữ hàm, KHÔNG khai báo biến const trùng
////// ---- Education ----
////function openEducationModal(id = null) {
////  const education = id ? educations.find((e) => e.educationId === id) : null;
////  const isEdit = !!education;
////  const content = `
////    <form id="educationForm">
////      <input type="hidden" name="educationId" value="${education?.educationId || ""}">
////      <div class="form-group">
////        <label>University *</label>
////        <select name="university" required>
////          <option value="">Select University</option>
////          ${universities.map((u) => `<option value="${u}" ${education?.university === u ? "selected" : ""}>${u}</option>`).join("")}
////        </select>
////      </div>
////      <div class="form-group">
////        <label>Degree Level *</label>
////        <select name="degreeLevel" required>
////          <option value="">Select Degree</option>
////          ${degreeLevels.map((d) => `<option value="${d}" ${education?.degreeLevel === d ? "selected" : ""}>${d}</option>`).join("")}
////        </select>
////      </div>
////      <div class="form-grid">
////        <div class="form-group"><label>Start Date *</label>
////          <input type="date" name="startDate" value="${education?.startDate || ""}" required></div>
////        <div class="form-group"><label>Graduation Date</label>
////          <input type="date" name="graduationDate" value="${education?.graduationDate || ""}"></div>
////      </div>
////      <div class="form-group"><label>Description</label>
////        <textarea name="description" rows="4">${education?.description || ""}</textarea>
////      </div>
////    </form>`;
////  createModal(isEdit ? "Edit Education" : "Add Education", content, () => saveEducation(isEdit));
////}
////
////function saveEducation(isEdit) {
////  const form = document.getElementById("educationForm");
////  const formData = new FormData(form);
////  const url = isEdit ? "/jobseeker/profile/education/update" : "/jobseeker/profile/education/add";
////  fetch(url, { method: "POST", body: formData })
////    .then((r) => r.json())
////    .then((d) => {
////      if (d.success) {
////        showNotification("Education saved", "success");
////        closeModal();
////        setTimeout(() => location.reload(), 1000);
////      } else showNotification(d.error || "Failed to save", "error");
////    });
////}
////
////function editEducation(id) { openEducationModal(id); }
////function deleteEducation(id) {
////  if (!confirm("Delete this education?")) return;
////  const fd = new FormData(); fd.append("educationId", id);
////  fetch("/jobseeker/profile/education/delete", { method: "POST", body: fd })
////    .then((r) => r.json()).then((d) => {
////      if (d.success) { showNotification("Deleted", "success"); setTimeout(() => location.reload(), 1000); }
////      else showNotification(d.error || "Failed", "error");
////    });
////}
////
////// ---- Notification ----
////function showNotification(msg, type = "info") {
////  const n = document.createElement("div");
////  n.className = `notification notification-${type}`;
////  n.innerHTML = `<i class="fas fa-${type === "success" ? "check-circle" : "exclamation-circle"}"></i><span>${msg}</span>`;
////  if (!document.querySelector("style[data-notification]")) {
////    const s = document.createElement("style");
////    s.setAttribute("data-notification", "true");
////    s.textContent = `
////      .notification{position:fixed;top:20px;right:20px;padding:15px 20px;border-radius:8px;
////      box-shadow:0 4px 12px rgba(0,0,0,0.15);display:flex;gap:10px;align-items:center;z-index:9999;
////      animation:slideIn .3s ease;font-weight:500}
////      .notification-success{background:#dcfce7;color:#166534;border:1px solid #86efac}
////      .notification-error{background:#fee2e2;color:#991b1b;border:1px solid #fca5a5}
////      @keyframes slideIn{from{transform:translateX(400px);opacity:0}to{transform:translateX(0);opacity:1}}
////    `;
////    document.head.appendChild(s);
////  }
////  document.body.appendChild(n);
////  setTimeout(() => (n.style.animation = "slideIn 0.3s ease reverse"), 2800);
////  setTimeout(() => n.remove(), 3100);
////}
////
////// Close modal by ESC
////document.addEventListener("keydown", (e) => e.key === "Escape" && closeModal());
//
///**********************
// *  Avatar uploader
// **********************/
//document.getElementById("avatarInput")?.addEventListener("change", (e) => {
//  const file = e.target.files?.[0];
//  if (!file) return;
//
//  // Preview
//  const reader = new FileReader();
//  reader.onload = (ev) => (document.getElementById("avatarPreview").src = ev.target.result);
//  reader.readAsDataURL(file);
//
//  // Upload
//  const formData = new FormData();
//  formData.append("avatar", file);
//
//  fetch("/jobseeker/profile/avatar", { method: "POST", body: formData })
//    .then((res) => {
//      if (res.ok) showNotification("Avatar uploaded successfully", "success");
//      else showNotification("Failed to upload avatar", "error");
//      setTimeout(() => location.reload(), 1000);
//    })
//    .catch(() => showNotification("Error uploading avatar", "error"));
//});
//
///**********************
// *  Word counter
// **********************/
//const selfIntro = document.querySelector('textarea[name="about"]');
//const wordCount = document.getElementById("wordCount");
//if (selfIntro && wordCount) {
//  const updateCount = () => {
//    const words = selfIntro.value.trim().split(/\s+/).filter(Boolean).length;
//    wordCount.textContent = `${words} words (100-150 required)`;
//    wordCount.style.color = words < 100 || words > 150 ? "#dc2626" : "#10b981";
//  };
//  selfIntro.addEventListener("input", updateCount);
//  updateCount();
//}
//
///**********************
// *  Modal helpers
// **********************/
//function createModal(title, content, onSave) {
//  const overlay = document.createElement("div");
//  overlay.className = "modal-overlay";
//  overlay.innerHTML = `
//    <div class="modal">
//      <div class="modal-header">
//        <h3>${title}</h3>
//        <button class="modal-close" onclick="closeModal()">&times;</button>
//      </div>
//      <div class="modal-body">${content}</div>
//      <div class="modal-footer">
//        <button class="btn-secondary" onclick="closeModal()">Cancel</button>
//        <button class="btn-primary" onclick="handleModalSave()">
//          <i class="fas fa-save"></i> Save
//        </button>
//      </div>
//    </div>
//  `;
//  const container = document.getElementById("modalContainer");
//  container.innerHTML = "";
//  container.appendChild(overlay);
//
//  // expose save handler
//  window.handleModalSave = onSave;
//
//  overlay.addEventListener("click", (e) => {
//    if (e.target === overlay) closeModal();
//  });
//}
//function closeModal() {
//  const container = document.getElementById("modalContainer");
//  if (container) container.innerHTML = "";
//}
//document.addEventListener("keydown", (e) => e.key === "Escape" && closeModal());
//
///**********************
// *  EDUCATION
// *  NOTE: các mảng educations, universities, degreeLevels
// *  được Thymeleaf inject ở HTML. KHÔNG khai báo lại ở đây.
// **********************/
//function openEducationModal(educationId = null) {
//  const education = educationId ? (educations || []).find((e) => e.educationId === educationId) : null;
//  const isEdit = !!education;
//
//  const content = `
//    <form id="educationForm" class="profile-form">
//      <input type="hidden" name="educationId" value="${education?.educationId || ""}">
//      <div class="form-group">
//        <label>University <span class="required">*</span></label>
//        <select name="university" required>
//          <option value="">Select University</option>
//          ${(universities || [])
//            .map((u) => `<option value="${u}" ${education?.university === u ? "selected" : ""}>${u}</option>`)
//            .join("")}
//        </select>
//      </div>
//      <div class="form-group">
//        <label>Degree Level <span class="required">*</span></label>
//        <select name="degreeLevel" required>
//          <option value="">Select Degree</option>
//          ${(degreeLevels || [])
//            .map((d) => `<option value="${d}" ${education?.degreeLevel === d ? "selected" : ""}>${d}</option>`)
//            .join("")}
//        </select>
//      </div>
//      <div class="form-grid">
//        <div class="form-group">
//          <label>Start Date <span class="required">*</span></label>
//          <input type="date" name="startDate" value="${education?.startDate || ""}" required>
//        </div>
//        <div class="form-group">
//          <label>Graduation Date</label>
//          <input type="date" name="graduationDate" value="${education?.graduationDate || ""}">
//        </div>
//      </div>
//      <div class="form-group">
//        <label>Description</label>
//        <textarea name="description" rows="4">${education?.description || ""}</textarea>
//      </div>
//    </form>
//  `;
//
//  createModal(isEdit ? "Edit Education" : "Add Education", content, () => saveEducation(isEdit));
//}
//function saveEducation(isEdit) {
//  const form = document.getElementById("educationForm");
//  const formData = new FormData(form);
//  const url = isEdit ? "/jobseeker/profile/education/update" : "/jobseeker/profile/education/add";
//
//  fetch(url, { method: "POST", body: formData })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Education saved successfully", "success");
//        closeModal();
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to save education", "error");
//    })
//    .catch(() => showNotification("Error saving education", "error"));
//}
//function editEducation(id) { openEducationModal(id); }
//function deleteEducation(id) {
//  if (!confirm("Are you sure you want to delete this education?")) return;
//  const fd = new FormData(); fd.append("educationId", id);
//  fetch("/jobseeker/profile/education/delete", { method: "POST", body: fd })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Education deleted", "success");
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to delete education", "error");
//    })
//    .catch(() => showNotification("Error deleting education", "error"));
//}
//
///**********************
// *  EXPERIENCE
// **********************/
//function openExperienceModal(experienceId = null) {
//  const experience = experienceId ? (experiences || []).find((e) => e.experienceId === experienceId) : null;
//  const isEdit = !!experience;
//
//  const content = `
//    <form id="experienceForm" class="profile-form">
//      <input type="hidden" name="experienceId" value="${experience?.experienceId || ""}">
//      <div class="form-group">
//        <label>Job Title <span class="required">*</span></label>
//        <input type="text" name="jobTitle" value="${experience?.jobTitle || ""}" required>
//      </div>
//      <div class="form-group">
//        <label>Company Name <span class="required">*</span></label>
//        <input type="text" name="companyName" value="${experience?.companyName || ""}" required>
//      </div>
//      <div class="form-grid">
//        <div class="form-group">
//          <label>Start Date <span class="required">*</span></label>
//          <input type="date" name="startDate" value="${experience?.startDate || ""}" required>
//        </div>
//        <div class="form-group">
//          <label>End Date</label>
//          <input type="date" name="endDate" value="${experience?.endDate || ""}">
//        </div>
//      </div>
//      <div class="form-group">
//        <label>Project Link</label>
//        <input type="url" name="projectLink" value="${experience?.projectLink || ""}" placeholder="https://...">
//      </div>
//    </form>
//  `;
//
//  createModal(isEdit ? "Edit Experience" : "Add Experience", content, () => saveExperience(isEdit));
//}
//function saveExperience(isEdit) {
//  const form = document.getElementById("experienceForm");
//  const formData = new FormData(form);
//  const url = isEdit ? "/jobseeker/profile/experience/update" : "/jobseeker/profile/experience/add";
//
//  fetch(url, { method: "POST", body: formData })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Experience saved successfully", "success");
//        closeModal();
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to save experience", "error");
//    })
//    .catch(() => showNotification("Error saving experience", "error"));
//}
//function editExperience(id) { openExperienceModal(id); }
//function deleteExperience(id) {
//  if (!confirm("Are you sure you want to delete this experience?")) return;
//  const fd = new FormData(); fd.append("experienceId", id);
//  fetch("/jobseeker/profile/experience/delete", { method: "POST", body: fd })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Experience deleted", "success");
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to delete experience", "error");
//    })
//    .catch(() => showNotification("Error deleting experience", "error"));
//}
//
///**********************
// *  SKILL
// **********************/
//function openSkillModal(skillId = null) {
//  const skill = skillId ? (skills || []).find((s) => s.skillId === skillId) : null;
//  const isEdit = !!skill;
//
//  const content = `
//    <form id="skillForm" class="profile-form">
//      <input type="hidden" name="skillId" value="${skill?.skillId || ""}">
//      <div class="form-group">
//        <label>Skill Name <span class="required">*</span></label>
//        <input type="text" name="skillName" value="${skill?.skillName || ""}" list="skillSuggestions" required>
//        <datalist id="skillSuggestions">
//          ${(commonSkills || []).map((s) => `<option value="${s}">`).join("")}
//        </datalist>
//      </div>
//      <div class="form-group">
//        <label>Years of Experience <span class="required">*</span></label>
//        <input type="number" name="yearsOfExperience" value="${skill?.yearsOfExperience ?? 0}" min="0" max="50" required>
//      </div>
//      <div class="form-group">
//        <label>Description</label>
//        <textarea name="description" rows="4">${skill?.description || ""}</textarea>
//      </div>
//    </form>
//  `;
//
//  createModal(isEdit ? "Edit Skill" : "Add Skill", content, () => saveSkill(isEdit));
//}
//function saveSkill(isEdit) {
//  const form = document.getElementById("skillForm");
//  const formData = new FormData(form);
//  const url = isEdit ? "/jobseeker/profile/skill/update" : "/jobseeker/profile/skill/add";
//
//  fetch(url, { method: "POST", body: formData })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Skill saved successfully", "success");
//        closeModal();
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to save skill", "error");
//    })
//    .catch(() => showNotification("Error saving skill", "error"));
//}
//function editSkill(id) { openSkillModal(id); }
//function deleteSkill(id) {
//  if (!confirm("Are you sure you want to delete this skill?")) return;
//  const fd = new FormData(); fd.append("skillId", id);
//  fetch("/jobseeker/profile/skill/delete", { method: "POST", body: fd })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Skill deleted", "success");
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to delete skill", "error");
//    })
//    .catch(() => showNotification("Error deleting skill", "error"));
//}
//
///**********************
// *  LANGUAGE
// **********************/
//function openLanguageModal(languageId = null) {
//  const language = languageId ? (languages || []).find((l) => l.languageId === languageId) : null;
//  const isEdit = !!language;
//
//  const content = `
//    <form id="languageForm" class="profile-form">
//      <input type="hidden" name="languageId" value="${language?.languageId || ""}">
//      <div class="form-group">
//        <label>Language <span class="required">*</span></label>
//        <select name="languageName" id="languageSelect" required>
//          <option value="">Select Language</option>
//          ${(recognizedLanguages || [])
//            .map((l) => `<option value="${l}" ${language?.languageName === l ? "selected" : ""}>${l}</option>`)
//            .join("")}
//        </select>
//      </div>
//      <div class="form-group">
//        <label>Certificate Type <span class="required">*</span></label>
//        <select name="certificateType" id="certificateTypeSelect" required>
//          <option value="">Select Certificate</option>
//        </select>
//      </div>
//    </form>
//  `;
//
//  createModal(isEdit ? "Edit Language" : "Add Language", content, () => saveLanguage(isEdit));
//
//  // load certificate types (nếu đang edit, chọn sẵn)
//  setTimeout(() => {
//    const selectedLang = language?.languageName || document.getElementById("languageSelect")?.value;
//    updateCertificateTypes(selectedLang, language?.certificateType || null);
//    document.getElementById("languageSelect")?.addEventListener("change", (ev) => {
//      updateCertificateTypes(ev.target.value, null);
//    });
//  }, 0);
//}
//function updateCertificateTypes(languageName, selectedCert) {
//  const certSelect = document.getElementById("certificateTypeSelect");
//  if (!languageName || !certSelect) {
//    if (certSelect) certSelect.innerHTML = '<option value="">Select Certificate</option>';
//    return;
//  }
//  fetch(`/jobseeker/profile/certificate-types?language=${encodeURIComponent(languageName)}`)
//    .then((r) => r.json())
//    .then((types) => {
//      certSelect.innerHTML =
//        '<option value="">Select Certificate</option>' +
//        (types || []).map((t) => `<option value="${t}" ${t === selectedCert ? "selected" : ""}>${t}</option>`).join("");
//    });
//}
//function saveLanguage(isEdit) {
//  const form = document.getElementById("languageForm");
//  const formData = new FormData(form);
//  const url = isEdit ? "/jobseeker/profile/language/update" : "/jobseeker/profile/language/add";
//
//  fetch(url, { method: "POST", body: formData })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Language saved successfully", "success");
//        closeModal();
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to save language", "error");
//    })
//    .catch(() => showNotification("Error saving language", "error"));
//}
//function editLanguage(id) { openLanguageModal(id); }
//function deleteLanguage(id) {
//  if (!confirm("Are you sure you want to delete this language?")) return;
//  const fd = new FormData(); fd.append("languageId", id);
//  fetch("/jobseeker/profile/language/delete", { method: "POST", body: fd })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Language deleted", "success");
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to delete language", "error");
//    })
//    .catch(() => showNotification("Error deleting language", "error"));
//}
//
///**********************
// *  CERTIFICATE
// **********************/
//function openCertificateModal(certificateId = null) {
//  const certificate = certificateId ? (certificates || []).find((c) => c.certificateId === certificateId) : null;
//  const isEdit = !!certificate;
//
//  const content = `
//    <form id="certificateForm" class="profile-form" enctype="multipart/form-data">
//      <input type="hidden" name="certificateId" value="${certificate?.certificateId || ""}">
//      <div class="form-group">
//        <label>Issuing Organization <span class="required">*</span></label>
//        <input type="text" name="issuingOrganization" value="${certificate?.issuingOrganization || ""}" required>
//      </div>
//      <div class="form-group">
//        <label>Year of Completion <span class="required">*</span></label>
//        <input type="number" name="yearOfCompletion"
//               value="${certificate?.yearOfCompletion || new Date().getFullYear()}"
//               min="1950" max="${new Date().getFullYear()}" required>
//      </div>
//      <div class="form-group">
//        <label>Certificate Image</label>
//        <input type="file" name="certificateImage" accept="image/*">
//        ${certificate?.certificateImageUrl ? `<small>Current image will be kept if no new image is uploaded</small>` : ""}
//      </div>
//    </form>
//  `;
//
//  createModal(isEdit ? "Edit Certificate" : "Add Certificate", content, () => saveCertificate(isEdit));
//}
//function saveCertificate(isEdit) {
//  const form = document.getElementById("certificateForm");
//  const formData = new FormData(form);
//  const url = isEdit ? "/jobseeker/profile/certificate/update" : "/jobseeker/profile/certificate/add";
//
//  fetch(url, { method: "POST", body: formData })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Certificate saved successfully", "success");
//        closeModal();
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to save certificate", "error");
//    })
//    .catch(() => showNotification("Error saving certificate", "error"));
//}
//function editCertificate(id) { openCertificateModal(id); }
//function deleteCertificate(id) {
//  if (!confirm("Are you sure you want to delete this certificate?")) return;
//  const fd = new FormData(); fd.append("certificateId", id);
//  fetch("/jobseeker/profile/certificate/delete", { method: "POST", body: fd })
//    .then((r) => r.json())
//    .then((d) => {
//      if (d.success) {
//        showNotification("Certificate deleted", "success");
//        setTimeout(() => location.reload(), 1000);
//      } else showNotification(d.error || "Failed to delete certificate", "error");
//    })
//    .catch(() => showNotification("Error deleting certificate", "error"));
//}
//
///**********************
// *  Notification
// **********************/
//function showNotification(message, type = "info") {
//  const n = document.createElement("div");
//  n.className = `notification notification-${type}`;
//  n.innerHTML = `
//    <i class="fas fa-${type === "success" ? "check-circle" : "exclamation-circle"}"></i>
//    <span>${message}</span>
//  `;
//
//  if (!document.querySelector("style[data-notification]")) {
//    const s = document.createElement("style");
//    s.setAttribute("data-notification", "true");
//    s.textContent = `
//      .notification{position:fixed;top:20px;right:20px;padding:15px 20px;border-radius:8px;
//      box-shadow:0 4px 12px rgba(0,0,0,0.15);display:flex;align-items:center;gap:10px;z-index:10000;
//      animation:slideIn .3s ease;font-weight:500}
//      .notification-success{background:#dcfce7;color:#166534;border:1px solid #86efac}
//      .notification-error{background:#fee2e2;color:#991b1b;border:1px solid #fca5a5}
//      @keyframes slideIn{from{transform:translateX(400px);opacity:0}to{transform:translateX(0);opacity:1}}
//    `;
//    document.head.appendChild(s);
//  }
//
//  document.body.appendChild(n);
//  setTimeout(() => { n.style.animation = "slideIn .3s ease reverse"; setTimeout(() => n.remove(), 300); }, 3000);
//}
//
///**********************
// *  Expose functions to global (fix inline onclick)
// **********************/
//window.openEducationModal   = openEducationModal;
//window.editEducation        = editEducation;
//window.deleteEducation      = deleteEducation;
//
//window.openExperienceModal  = openExperienceModal;
//window.editExperience       = editExperience;
//window.deleteExperience     = deleteExperience;
//
//window.openSkillModal       = openSkillModal;
//window.editSkill            = editSkill;
//window.deleteSkill          = deleteSkill;
//
//window.openLanguageModal    = openLanguageModal;
//window.editLanguage         = editLanguage;
//window.deleteLanguage       = deleteLanguage;
//
//window.openCertificateModal = openCertificateModal;
//window.editCertificate      = editCertificate;
//window.deleteCertificate    = deleteCertificate;
//
///**********************
// *  Optional: event delegation (phòng khi bỏ inline onclick)
// *  Ví dụ: <button data-action="add-education">Add Education</button>
// **********************/
//document.addEventListener("click", (e) => {
//  const el = e.target.closest("[data-action]");
//  if (!el) return;
//  const action = el.getAttribute("data-action");
//  if (action === "add-education") { e.preventDefault(); openEducationModal(); }
//  if (action === "add-experience") { e.preventDefault(); openExperienceModal(); }
//  if (action === "add-skill") { e.preventDefault(); openSkillModal(); }
//  if (action === "add-language") { e.preventDefault(); openLanguageModal(); }
//  if (action === "add-certificate") { e.preventDefault(); openCertificateModal(); }
//});
//

/************** Avatar **************/
document.getElementById("avatarInput")?.addEventListener("change", (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (ev) => {
        // Cập nhật ảnh preview trên trang profile
        document.getElementById("avatarPreview").src = ev.target.result;
    };
    reader.readAsDataURL(file);

    const formData = new FormData();
    formData.append("avatar", file);

    fetch("/jobseeker/profile/avatar", {
        method: "POST",
        body: formData
    })
    .then((res) => {
        if (!res.ok) {
            // Nếu server báo lỗi (ví dụ: 500, 401)
            throw new Error("Upload failed");
        }
        return res.json(); // 1. Đọc response dưới dạng JSON
    })
    .then((data) => {
        // 2. Lấy URL mới từ response (giống key trong Map ở Java)
        const newAvatarUrl = data.newAvatarUrl;

        showNotification("Avatar uploaded successfully", "success");

        // 3. Cập nhật ảnh preview (để nó dùng URL thật thay vì dataURL)
        document.getElementById("avatarPreview").src = newAvatarUrl;

        // 4. Cập nhật TẤT CẢ các ảnh avatar khác trên trang (header, user menu)
        // (Hãy kiểm tra class/id trong header.html của bạn)
        const headerAvatars = document.querySelectorAll('.usermenu__btn img, .usermenu__avatar');
        headerAvatars.forEach(img => {
            img.src = newAvatarUrl;
            img.srcset = newAvatarUrl; // Cập nhật cả srcset nếu có
        });

        // 5. KHÔNG RELOAD LẠI TRANG
        // setTimeout(() => location.reload(), 1000); // <-- XÓA HOẶC CHÚ THÍCH DÒNG NÀY
    })
    .catch(() => {
        showNotification("Error uploading avatar", "error");
        // Nếu lỗi, có thể reload lại để lấy ảnh cũ từ server
        setTimeout(() => location.reload(), 1000);
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
