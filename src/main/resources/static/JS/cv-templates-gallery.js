document.addEventListener("DOMContentLoaded", () => {
  const categoryFilter = document.getElementById("categoryFilter")
  const searchInput = document.getElementById("searchTemplate")
  const templateCards = document.querySelectorAll(".template-card")
  const emptyState = document.querySelector(".empty-state")

  // Filter by category
  categoryFilter.addEventListener("change", () => {
    filterTemplates()
  })

  // Search templates
  searchInput.addEventListener("input", () => {
    filterTemplates()
  })

  function filterTemplates() {
    const category = categoryFilter.value
    const searchTerm = searchInput.value.toLowerCase()
    let visibleCount = 0

    templateCards.forEach((card) => {
      const cardCategory = card.getAttribute("data-category")
      const cardName = card.querySelector(".template-name").textContent.toLowerCase()
      const cardDescription = card.querySelector(".template-description").textContent.toLowerCase()

      const matchesCategory = category === "all" || cardCategory === category
      const matchesSearch = cardName.includes(searchTerm) || cardDescription.includes(searchTerm)

      if (matchesCategory && matchesSearch) {
        card.style.display = "block"
        visibleCount++
      } else {
        card.style.display = "none"
      }
    })

    // Show/hide empty state
    if (visibleCount === 0) {
      emptyState.style.display = "block"
    } else {
      emptyState.style.display = "none"
    }
  }

  // Add animation on scroll
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.style.opacity = "0"
          entry.target.style.transform = "translateY(20px)"
          setTimeout(() => {
            entry.target.style.transition = "all 0.5s"
            entry.target.style.opacity = "1"
            entry.target.style.transform = "translateY(0)"
          }, 100)
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.1 },
  )

  templateCards.forEach((card) => {
    observer.observe(card)
  })
})
