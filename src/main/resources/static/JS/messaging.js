// Messaging Feature JavaScript

const SockJS = window.SockJS
const Stomp = window.Stomp

class MessagingApp {
  constructor() {
    this.stompClient = null
    this.currentUserId = null
    this.conversationId = null
    this.receiverUserId = null // optional for old flows
    this.seekerId = null
    this.employerId = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  init(currentUserId, conversationId, receiverUserId, seekerId, employerId) {
    this.currentUserId = currentUserId
    this.conversationId = conversationId || null
    this.receiverUserId = receiverUserId || null
    this.seekerId = seekerId || null
    this.employerId = employerId || null

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
        console.log("[v1] Connected:", frame)
        this.reconnectAttempts = 0
        this.subscribeToChannels()
      },
      (error) => {
        console.error("[v1] WS error:", error)
        this.handleConnectionError()
      }
    )
  }

  subscribeToChannels() {
    this.stompClient.subscribe(`/user/${this.currentUserId}/queue/messages`, (message) => {
      const msg = JSON.parse(message.body)
      if (!this.conversationId || msg.conversationId === this.conversationId) {
        this.appendMessage(msg)
      }
    })

    this.stompClient.subscribe(`/user/${this.currentUserId}/queue/recall`, (message) => {
      const data = JSON.parse(message.body)
      this.markMessageAsRecalled(data.messageId)
    })
  }

  handleConnectionError() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      setTimeout(() => this.connect(), 3000 * this.reconnectAttempts)
    } else {
      alert("Mất kết nối realtime. Hãy F5 trang.")
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
    const content = (input?.value || "").trim()
    if (!content) return

    // ƯU TIÊN: nếu đang ở trang hội thoại -> gửi theo conversationId
    if (this.conversationId) {
      fetch("/messages/send-in-conversation", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          conversationId: this.conversationId,
          content
        })
      })
        .then((r) => r.json())
        .then((data) => {
          if (data.success) {
            this.appendMessage(data.message)
            input.value = ""
            if (this.stompClient?.connected) {
              this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(data.message))
            }
          } else {
            alert(data.error || "Gửi tin nhắn thất bại")
          }
        })
        .catch((e) => {
          console.error(e)
          alert("Gửi tin nhắn thất bại")
        })
      return
    }

    // Flow cũ (nếu chưa có conversationId)
    fetch("/messages/send", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        receiverUserId: this.receiverUserId,
        content,
        messageType: "text",
        seekerId: this.seekerId,
        employerId: this.employerId
      })
    })
      .then((r) => r.json())
      .then((data) => {
        if (data.success) {
          this.appendMessage(data.message)
          input.value = ""
          if (this.stompClient?.connected) {
            this.stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(data.message))
          }
        } else {
          alert(data.error || "Gửi tin nhắn thất bại")
        }
      })
      .catch((e) => {
        console.error(e)
        alert("Gửi tin nhắn thất bại")
      })
  }

  async blockInConversation() {
    if (!this.conversationId) return
    if (!confirm("Chặn người này?")) return

    // Lấy otherUserId từ server
    const resp = await fetch(`/messages/api/conversation/${this.conversationId}/other-user`)
    const data = await resp.json()
    if (!data.otherUserId) {
      alert(data.error || "Không lấy được đối phương")
      return
    }

    fetch("/messages/block", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({ blockedUserId: data.otherUserId })
    })
      .then((r) => r.json())
      .then((d) => {
        if (d.success) {
          alert("Đã chặn người dùng")
          location.href = "/messages"
        } else {
          alert(d.error || "Chặn thất bại")
        }
      })
      .catch((e) => {
        console.error(e)
        alert("Chặn thất bại")
      })
  }

  recallMessage(messageId) {
    fetch(`/messages/recall/${messageId}`, { method: "POST" })
      .then((r) => r.json())
      .then((d) => {
        if (d.success) this.markMessageAsRecalled(messageId)
        else alert(d.error || "Thu hồi thất bại")
      })
      .catch((e) => {
        console.error(e)
        alert("Thu hồi thất bại")
      })
  }

  appendMessage(msg) {
    const container = document.getElementById("messagesContainer")
    if (!container) return
    const div = document.createElement("div")
    div.className = msg.senderUserId === this.currentUserId ? "message sent" : "message received"
    div.dataset.messageId = msg.messageId

    const time = msg.sentAt
      ? new Date(msg.sentAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
      : ""
    div.innerHTML = `
      <div class="message-bubble">
        <div>${this.escapeHtml(msg.messageContent || "")}</div>
        <div class="message-time">${time}</div>
      </div>
    `
    container.appendChild(div)
    this.scrollToBottom()
  }

  markMessageAsRecalled(messageId) {
    const node = document.querySelector(`[data-message-id="${messageId}"] .message-bubble`)
    if (node) node.innerHTML = `<em>Tin nhắn đã được thu hồi</em>`
  }

  scrollToBottom() {
    const c = document.getElementById("messagesContainer")
    if (c) c.scrollTop = c.scrollHeight
  }

  escapeHtml(s) {
    const d = document.createElement("div")
    d.textContent = s
    return d.innerHTML
  }
}

const messagingApp = new MessagingApp()
