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
})
