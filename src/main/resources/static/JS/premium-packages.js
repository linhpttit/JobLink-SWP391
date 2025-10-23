document.addEventListener("DOMContentLoaded", () => {
  const purchaseButtons = document.querySelectorAll(".purchase-btn")

  purchaseButtons.forEach((button) => {
    button.addEventListener("click", async function () {
      const packageId = this.getAttribute("data-package-id")

      if (!confirm("Bạn có chắc chắn muốn mua gói Premium này?")) {
        return
      }

      try {
        button.disabled = true
        button.textContent = "Đang xử lý..."

        const response = await fetch(`/jobseeker/premium/purchase/${packageId}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
        })

        const data = await response.json()

        if (data.success) {
          // Simulate payment gateway redirect
          // In production, redirect to actual payment gateway
          const paymentUrl = `/jobseeker/premium/payment/callback?txRef=${data.txRef}&status=SUCCESS&packageId=${packageId}`
          window.location.href = paymentUrl
        } else {
          alert("Có lỗi xảy ra: " + (data.error || "Unknown error"))
          button.disabled = false
          button.textContent = "Mua Ngay"
        }
      } catch (error) {
        console.error("Error:", error)
        alert("Có lỗi xảy ra khi xử lý thanh toán")
        button.disabled = false
        button.textContent = "Mua Ngay"
      }
    })
  })
})
