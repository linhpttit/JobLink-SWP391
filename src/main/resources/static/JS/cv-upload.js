// File upload handling
document.addEventListener("DOMContentLoaded", () => {
  const fileInput = document.getElementById("cvFile")
  const fileUploadArea = document.getElementById("fileUploadArea")
  const filePreview = document.getElementById("filePreview")
  const fileName = document.getElementById("fileName")
  const removeFileBtn = document.getElementById("removeFile")
  const form = document.getElementById("cvUploadForm")

  // File selection
  fileInput.addEventListener("change", (e) => {
    handleFileSelect(e.target.files[0])
  })

  // Drag and drop
  fileUploadArea.addEventListener("dragover", (e) => {
    e.preventDefault()
    fileUploadArea.classList.add("drag-over")
  })

  fileUploadArea.addEventListener("dragleave", (e) => {
    e.preventDefault()
    fileUploadArea.classList.remove("drag-over")
  })

  fileUploadArea.addEventListener("drop", (e) => {
    e.preventDefault()
    fileUploadArea.classList.remove("drag-over")

    const files = e.dataTransfer.files
    if (files.length > 0) {
      const file = files[0]
      // Set the file to the input
      const dataTransfer = new DataTransfer()
      dataTransfer.items.add(file)
      fileInput.files = dataTransfer.files
      handleFileSelect(file)
    }
  })

  // Remove file
  removeFileBtn.addEventListener("click", (e) => {
    e.stopPropagation()
    fileInput.value = ""
    filePreview.style.display = "none"
    document.querySelector(".file-upload-content").style.display = "block"
  })

  function handleFileSelect(file) {
    if (!file) return

    // Validate file type
    const validTypes = [
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ]
    if (!validTypes.includes(file.type)) {
      alert("Please upload a PDF, DOC, or DOCX file")
      fileInput.value = ""
      return
    }

    // Validate file size (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert("File size must be under 5MB")
      fileInput.value = ""
      return
    }

    // Show file preview
    fileName.textContent = file.name
    document.querySelector(".file-upload-content").style.display = "none"
    filePreview.style.display = "flex"
  }

  // Form validation
  form.addEventListener("submit", (e) => {
    const yearsOfExperience = Number.parseInt(document.getElementById("yearsOfExperience").value)

    if (yearsOfExperience < 0 || yearsOfExperience >= 100) {
      e.preventDefault()
      alert("Years of experience must be between 0 and 99")
      return false
    }

    if (!fileInput.files || fileInput.files.length === 0) {
      e.preventDefault()
      alert("Please upload your CV file")
      return false
    }

    // Show loading state
    const submitBtn = form.querySelector('button[type="submit"]')
    submitBtn.disabled = true
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...'
  })

  // Auto-dismiss alerts after 5 seconds
  const alerts = document.querySelectorAll(".alert")
  alerts.forEach((alert) => {
    setTimeout(() => {
      alert.style.opacity = "0"
      setTimeout(() => alert.remove(), 300)
    }, 5000)
  })

  // ===== CV Evaluation Modal & AJAX =====
  const modal = document.getElementById("cv-eval-modal")
  const btnClose = document.getElementById("cv-eval-close")
  const loadingEl = document.getElementById("cv-eval-loading")
  const errorEl = document.getElementById("cv-eval-error")
  const contentEl = document.getElementById("cv-eval-content")
  const scoreEl = document.getElementById("cv-eval-score")
  const strengthsEl = document.getElementById("cv-eval-strengths")
  const weaknessesEl = document.getElementById("cv-eval-weaknesses")
  const adviceEl = document.getElementById("cv-eval-advice")
  const skillsEl = document.getElementById("cv-eval-skills")
  const jobsEl = document.getElementById("cv-eval-jobs")

  function openModal() {
    if (!modal) return
    modal.style.display = "flex"
  }
  function closeModal() {
    if (!modal) return
    modal.style.display = "none"
  }
  if (btnClose) {
    btnClose.addEventListener("click", closeModal)
  }
  if (modal) {
    modal.addEventListener("click", (e) => {
      if (e.target === modal) closeModal()
    })
  }

  function setLoading(on) {
    if (!loadingEl || !contentEl || !errorEl) return
    loadingEl.style.display = on ? "block" : "none"
    contentEl.style.display = on ? "none" : "block"
    if (on) errorEl.style.display = "none"
  }

  function renderList(container, items) {
    container.innerHTML = ""
    if (!items || items.length === 0) {
      container.innerHTML = "<li>Không có dữ liệu</li>"
      return
    }
    items.forEach((text) => {
      const li = document.createElement("li")
      li.textContent = text
      container.appendChild(li)
    })
  }
  function renderChips(container, items) {
    container.innerHTML = ""
    if (!items || items.length === 0) {
      container.innerHTML = "<span>Không có dữ liệu</span>"
      return
    }
    items.forEach((text) => {
      const span = document.createElement("span")
      span.textContent = text
      span.style.border = "1px solid #ddd"
      span.style.borderRadius = "16px"
      span.style.padding = "6px 10px"
      span.style.fontSize = "12px"
      container.appendChild(span)
    })
  }
  function renderJobs(container, jobs) {
    container.innerHTML = ""
    if (!jobs || jobs.length === 0) {
      container.innerHTML = "<div>Chưa có gợi ý việc làm</div>"
      return
    }
    jobs.forEach((job) => {
      const a = document.createElement("a")
      a.href = `/job-detail/${job.jobId}`
      a.style.textDecoration = "none"
      a.style.color = "inherit"

      const card = document.createElement("div")
      card.style.border = "1px solid #eee"
      card.style.borderRadius = "8px"
      card.style.padding = "10px 12px"
      card.style.background = "#fafafa"
      card.style.cursor = "pointer"
      card.innerHTML = `
        <div style="font-weight:600; margin-bottom:6px;">${job.title || "Job"}</div>
        <div style="color:#555; font-size:13px; margin-bottom:4px;">${job.companyName || ""}</div>
        <div style="color:#777; font-size:12px;">${job.provinceName || ""}</div>
      `
      a.appendChild(card)
      container.appendChild(a)
    })
  }

  async function evaluateCV(cvId) {
    try {
      openModal()
      setLoading(true)
      const res = await fetch(`/jobseeker/cv/${cvId}/evaluate`, {
        method: "GET",
        headers: {
          "Accept": "application/json"
        },
        credentials: "include"
      })
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`)
      }
      const data = await res.json()
      if (data.error) {
        throw new Error(data.error)
      }
      const evalData = data.evaluation || {}
      const jobs = data.recommendedJobs || []

      scoreEl.textContent = (evalData.overallScore ?? "-")
      renderList(strengthsEl, evalData.strengths || [])
      renderList(weaknessesEl, evalData.weaknesses || [])
      renderList(adviceEl, evalData.improvementAdvice || [])
      renderChips(skillsEl, evalData.extractedSkills || [])
      renderJobs(jobsEl, jobs)
      setLoading(false)
    } catch (err) {
      if (errorEl) {
        errorEl.textContent = `Lỗi: ${err.message}`
        errorEl.style.display = "block"
      }
      if (contentEl) {
        contentEl.style.display = "none"
      }
      if (loadingEl) {
        loadingEl.style.display = "none"
      }
    }
  }

  document.querySelectorAll(".btn-evaluate").forEach((btn) => {
    btn.addEventListener("click", () => {
      const cvId = btn.getAttribute("data-cv-id")
      if (!cvId) return
      evaluateCV(cvId)
    })
  })
})
