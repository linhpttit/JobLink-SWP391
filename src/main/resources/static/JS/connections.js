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
      document.getElementById("targetName").textContent = targetName
      modal.style.display = "block"
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
    const message = document.getElementById("connectionMessage").value

    try {
      const response = await fetch("/jobseeker/connections/request", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          targetSeekerId: Number.parseInt(selectedSeekerId),
          message: message,
        }),
      })

      const data = await response.json()

      if (data.success) {
        alert("Yêu cầu kết nối đã được gửi!")
        modal.style.display = "none"
        location.reload()
      } else {
        alert("Có lỗi xảy ra: " + (data.error || "Unknown error"))
      }
    } catch (error) {
      console.error("Error:", error)
      alert("Có lỗi xảy ra khi gửi yêu cầu")
    }
  })

  // Accept request
  document.querySelectorAll(".btn-accept").forEach((btn) => {
    btn.addEventListener("click", async function () {
      const requestId = this.getAttribute("data-request-id")

      try {
        const response = await fetch(`/jobseeker/connections/accept/${requestId}`, {
          method: "POST",
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
        alert("Có lỗi xảy ra")
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

      try {
        const response = await fetch(`/jobseeker/connections/reject/${requestId}`, {
          method: "POST",
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
        alert("Có lỗi xảy ra")
      }
    })
  })


})
