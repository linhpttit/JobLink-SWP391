// Messaging Feature JavaScript

// Declare SockJS and Stomp variables
const SockJS = window.SockJS
const Stomp = window.Stomp

class MessagingApp {
  constructor() {
    this.stompClient = null
    this.currentUserId = null
    this.conversationId = null
    this.receiverUserId = null
    this.seekerId = null
    this.employerId = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  init(currentUserId, conversationId, receiverUserId, seekerId, employerId) {
    this.currentUserId = currentUserId
    this.conversationId = conversationId
    this.receiverUserId = receiverUserId
    this.seekerId = seekerId
    this.employerId = employerId

    this.connect()
    this.setupEventListeners()
    this.scrollToBottom()
  }

  connect() {
    const socket = new SockJS("/ws-messaging")
    this.stompClient = Stomp.over(socket)

    this.stompClient.connect(
      {},
      (frame) => {
        console.log("[v0] Connected to WebSocket:", frame)
        this.reconnectAttempts = 0
        this.subscribeToChannels()
      },
      (error) => {
        console.error("[v0] WebSocket connection error:", error)
        this.handleConnectionError()
      },
    )
  }

  subscribeToChannels() {
    // Subscribe to personal message queue
    this.stompClient.subscribe(`/user/${this.currentUserId}/queue/messages`, (message) => {
      const msg = JSON.parse(message.body)
      if (msg.conversationId === this.conversationId) {
        this.appendMessage(msg)
        this.playNotificationSound()
      }
    })

    // Subscribe to recall notifications
    this.stompClient.subscribe(`/user/${this.currentUserId}/queue/recall`, (message) => {
      const data = JSON.parse(message.body)
      this.markMessageAsRecalled(data.messageId)
    })
  }

  handleConnectionError() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`[v0] Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      setTimeout(() => this.connect(), 3000 * this.reconnectAttempts)
    } else {
      console.error("[v0] Max reconnection attempts reached")
      this.showError("Connection lost. Please refresh the page.")
    }
  }

  setupEventListeners() {
    const messageInput = document.getElementById("messageInput")
    if (messageInput) {
      messageInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault()
          this.sendMessage()
        }
      })
    }
  }

  sendMessage() {
    const input = document.getElementById("messageInput")
    const content = input.value.trim()

    if (!content) return

    fetch("/messages/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        receiverUserId: this.receiverUserId,
        content: content,
        messageType: "text",
        seekerId: this.seekerId,
        employerId: this.employerId,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          this.appendMessage(data.message)
          input.value = ""

          // Send via WebSocket for real-time delivery
          if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(data.message))
          }
        } else {
          this.showError(data.error)
        }
      })
      .catch((error) => {
        console.error("[v0] Error sending message:", error)
        this.showError("Failed to send message. Please try again.")
      })
  }

  sendAddress(address) {
    if (!address) return

    fetch("/messages/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        receiverUserId: this.receiverUserId,
        content: address,
        messageType: "address",
        seekerId: this.seekerId,
        employerId: this.employerId,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          this.appendMessage(data.message)

          if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(data.message))
          }
        } else {
          this.showError(data.error)
        }
      })
      .catch((error) => {
        console.error("[v0] Error sending address:", error)
        this.showError("Failed to send address. Please try again.")
      })
  }

  recallMessage(messageId) {
    if (!confirm("Are you sure you want to recall this message?")) return

    fetch(`/messages/recall/${messageId}`, {
      method: "POST",
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          this.markMessageAsRecalled(messageId)

          if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send(
              "/app/chat.recallMessage",
              {},
              JSON.stringify({
                messageId: messageId,
                receiverUserId: this.receiverUserId,
              }),
            )
          }
        } else {
          this.showError(data.error)
        }
      })
      .catch((error) => {
        console.error("[v0] Error recalling message:", error)
        this.showError("Failed to recall message. Please try again.")
      })
  }

  blockUser(userId, reason) {
    fetch("/messages/block", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        blockedUserId: userId,
        reason: reason || "",
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          alert("User blocked successfully")
          window.location.href = "/messages"
        } else {
          this.showError(data.error)
        }
      })
      .catch((error) => {
        console.error("[v0] Error blocking user:", error)
        this.showError("Failed to block user. Please try again.")
      })
  }

  appendMessage(msg) {
    const container = document.getElementById("messagesContainer")
    const messageDiv = document.createElement("div")
    messageDiv.className = msg.senderUserId === this.currentUserId ? "message sent" : "message received"
    messageDiv.setAttribute("data-message-id", msg.messageId)

    let content = ""

    // Add avatar for received messages
    if (msg.senderUserId !== this.currentUserId && msg.senderAvatar) {
      content += `<img src="${msg.senderAvatar}" class="avatar-small" alt="Avatar">`
    }

    content += `<div class="message-bubble">`

    if (msg.messageType === "address") {
      content += `<div class="message-type-address">
                <i class="fas fa-map-marker-alt"></i>
                <strong>Address:</strong>
                <div>${this.escapeHtml(msg.messageContent)}</div>
            </div>`
    } else {
      content += `<div>${this.escapeHtml(msg.messageContent)}</div>`
    }

    const time = msg.sentAt
      ? new Date(msg.sentAt).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })
      : new Date().toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })
    content += `<div class="message-time">${time}</div>`

    if (msg.senderUserId === this.currentUserId) {
      content += `<div class="message-actions">
                <button class="btn btn-sm btn-danger" onclick="messagingApp.recallMessage(${msg.messageId})">
                    <i class="fas fa-undo"></i>
                </button>
            </div>`
    }

    content += `</div>`

    messageDiv.innerHTML = content
    container.appendChild(messageDiv)
    this.scrollToBottom()
  }

  markMessageAsRecalled(messageId) {
    const messageDiv = document.querySelector(`[data-message-id="${messageId}"]`)
    if (messageDiv) {
      const bubble = messageDiv.querySelector(".message-bubble")
      bubble.innerHTML = `<div class="message-recalled">
                <i class="fas fa-undo"></i> Message recalled
            </div>`
    }
  }

  scrollToBottom() {
    const container = document.getElementById("messagesContainer")
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }

  escapeHtml(text) {
    const div = document.createElement("div")
    div.textContent = text
    return div.innerHTML
  }

  playNotificationSound() {
    // Optional: Add notification sound
    // const audio = new Audio('/sounds/notification.mp3');
    // audio.play().catch(e => console.log('Could not play sound:', e));
  }

  showError(message) {
    alert(message)
  }

  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect()
      console.log("[v0] Disconnected from WebSocket")
    }
  }
}

// Global instance
const messagingApp = new MessagingApp()
