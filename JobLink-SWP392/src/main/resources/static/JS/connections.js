document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("connectionModal")
  const closeButtons = document.querySelectorAll(".close")
  const sendRequestBtn = document.getElementById("sendRequestBtn")
  let selectedSeekerId = null

  // Connect buttons
  document.querySelectorAll(".btn-connect").forEach((btn) => {
    btn.addEventListener("click", function () {
      selectedSeekerId = this.getAttribute("data-seeker-id")
      const targetName = this.getAttribute("data-name")
      
      if (!selectedSeekerId) {
        console.error("No seeker ID found")
        alert("Không tìm thấy ID người dùng")
        return
      }
      
      const targetNameEl = document.getElementById("targetName")
      if (targetNameEl) {
        targetNameEl.textContent = targetName || "người dùng"
      }
      
      if (modal) {
        modal.style.display = "block"
      } else {
        console.error("Modal not found")
      }
    })
  })

  // Close modal
  closeButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      modal.style.display = "none"
      document.getElementById("connectionMessage").value = ""
    })
  })

  window.onclick = (event) => {
    if (event.target === modal) {
      modal.style.display = "none"
    }
  }

  // Send connection request
  sendRequestBtn.addEventListener("click", async () => {
    if (!selectedSeekerId) {
      alert("Vui lòng chọn người để kết nối")
      return
    }

    const message = document.getElementById("connectionMessage").value || ""

    try {
      const response = await fetch("/jobseeker/connections/request", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          targetSeekerId: parseInt(selectedSeekerId, 10),
          message: message,
        }),
      })

      const data = await response.json()

      if (data.success) {
        alert("Yêu cầu kết nối đã được gửi!")
        modal.style.display = "none"
        document.getElementById("connectionMessage").value = ""
        selectedSeekerId = null
        location.reload()
      } else {
        alert("Có lỗi xảy ra: " + (data.error || "Unknown error"))
      }
    } catch (error) {
      console.error("Error:", error)
      alert("Có lỗi xảy ra khi gửi yêu cầu: " + error.message)
    }
  })

  // Accept request
  document.querySelectorAll(".btn-accept").forEach((btn) => {
    btn.addEventListener("click", async function () {
      const requestId = this.getAttribute("data-request-id")
      
      if (!requestId) {
        alert("Không tìm thấy ID yêu cầu")
        return
      }

      try {
        const response = await fetch(`/jobseeker/connections/accept/${requestId}`, {
          method: "POST",
          credentials: "include",
        })

        const data = await response.json()

        if (data.success) {
          alert("Đã chấp nhận yêu cầu kết nối!")
          location.reload()
        } else {
          alert("Có lỗi xảy ra: " + (data.error || "Unknown error"))
        }
      } catch (error) {
        console.error("Error:", error)
        alert("Có lỗi xảy ra: " + error.message)
      }
    })
  })

  // Reject request
  document.querySelectorAll(".btn-reject").forEach((btn) => {
    btn.addEventListener("click", async function () {
      if (!confirm("Bạn có chắc chắn muốn từ chối yêu cầu này?")) {
        return
      }

      const requestId = this.getAttribute("data-request-id")
      
      if (!requestId) {
        alert("Không tìm thấy ID yêu cầu")
        return
      }

      try {
        const response = await fetch(`/jobseeker/connections/reject/${requestId}`, {
          method: "POST",
          credentials: "include",
        })

        const data = await response.json()

        if (data.success) {
          alert("Đã từ chối yêu cầu kết nối")
          location.reload()
        } else {
          alert("Có lỗi xảy ra: " + (data.error || "Unknown error"))
        }
      } catch (error) {
        console.error("Error:", error)
        alert("Có lỗi xảy ra: " + error.message)
      }
    })
  })


})