# 🤖 HR Chatbot - AI Training System

Chatbot tuyển dụng với AI training - Train với messages có sẵn và trả lời thông minh.

## 📁 Cấu trúc (Đơn giản & Gọn gàng)

```
hr-chatbot-python/
├── data/
│   ├── training_data.json      # Training messages
│   ├── trained_model.pkl       # AI model đã train
│   └── vector_db/              # Job postings database
├── core/
│   ├── ai_trainer.py           # Train AI với messages
│   ├── chatbot.py              # Main chatbot logic
│   └── db_connector.py         # Database connection
├── api.py                      # FastAPI endpoints
├── train.py                    # Training script
├── run.py                      # Run chatbot
└── requirements.txt
```

## 🚀 Quick Start

### 1. Cài đặt

```bash
pip install -r requirements.txt
```

### 2. Cấu hình

Tạo file `.env`:
```bash
copy .env.example .env
```

### 3. Train AI

```bash
python train.py
```

Sẽ train:
- ✅ AI messages (từ `training_data.json`)
- ✅ Job postings (từ database)

### 4. Chạy Chatbot

**Interactive mode:**
```bash
python run.py
```

**API mode:**
```bash
python run.py api
```

hoặc

```bash
python api.py
```

## 💬 Cách hoạt động

### 1. AI Training với Messages

File: `data/training_data.json`

```json
{
  "conversations": [
    {
      "tag": "greeting",
      "patterns": ["xin chào", "hello", "hi"],
      "responses": ["Xin chào! Tôi là trợ lý AI..."]
    }
  ]
}
```

**Chatbot sẽ:**
1. Load training data
2. Tạo embeddings cho patterns
3. Khi user hỏi, tìm pattern giống nhất
4. Trả lời với response đã train

### 2. Job Search

Khi user hỏi về việc làm:
- Tìm kiếm trong vector database
- Trả về jobs phù hợp nhất

## 📝 Thêm Training Data

### Cách 1: Edit file JSON

Edit `data/training_data.json`:

```json
{
  "tag": "salary_info",
  "patterns": [
    "lương bao nhiêu",
    "mức lương",
    "thu nhập"
  ],
  "responses": [
    "Mức lương phụ thuộc vào vị trí và kinh nghiệm..."
  ]
}
```

Sau đó chạy lại training:
```bash
python train.py
```

### Cách 2: Qua API

```bash
curl -X POST http://localhost:8000/api/training/add \
  -H "Content-Type: application/json" \
  -d '{
    "tag": "new_tag",
    "patterns": ["pattern 1", "pattern 2"],
    "responses": ["response 1", "response 2"]
  }'
```

## 🎯 API Endpoints

### Chat
```bash
POST /api/chat
{
  "message": "xin chào"
}
```

### Train
```bash
POST /api/train
```

### Add Training Data
```bash
POST /api/training/add
{
  "tag": "greeting",
  "patterns": ["hello"],
  "responses": ["Hi there!"]
}
```

### Health Check
```bash
GET /api/health
```

## 📊 Response Types

Chatbot có 3 loại response:

### 1. Trained Response
```json
{
  "type": "trained",
  "tag": "greeting",
  "confidence": 0.95,
  "response": "Xin chào! ..."
}
```

### 2. Job Search
```json
{
  "type": "job_search",
  "jobs": [...],
  "response": "Tôi tìm thấy 5 công việc..."
}
```

### 3. Fallback
```json
{
  "type": "fallback",
  "response": "Xin lỗi, tôi chưa hiểu..."
}
```

## 🔧 Customize

### Thêm patterns mới

Edit `data/training_data.json` và thêm:

```json
{
  "tag": "your_tag",
  "patterns": [
    "câu hỏi 1",
    "câu hỏi 2",
    "câu hỏi 3"
  ],
  "responses": [
    "Câu trả lời 1",
    "Câu trả lời 2"
  ]
}
```

### Điều chỉnh confidence threshold

Edit `core/chatbot.py`:

```python
ai_result = self.ai_trainer.predict(message, threshold=0.7)  # ← Thay đổi ở đây
```

- `threshold` cao (0.8-0.9): Chỉ trả lời khi chắc chắn
- `threshold` thấp (0.5-0.6): Trả lời nhiều hơn nhưng có thể sai

## 📈 Ví dụ

### Test Interactive

```bash
python run.py

👤 You: xin chào
🤖 Bot: Xin chào! 👋 Tôi là trợ lý tuyển dụng AI...
   [Trained response - greeting - 98.5%]

👤 You: tìm việc làm developer
🤖 Bot: 🎯 Tôi tìm thấy 3 công việc phù hợp:
   [Job search - 3 results]

👤 You: cảm ơn
🤖 Bot: Không có gì! 😊 Rất vui được giúp đỡ bạn!
   [Trained response - thanks - 95.2%]
```

### Test API

```bash
# Start API
python run.py api

# Test chat
curl -X POST http://localhost:8000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "xin chào"}'
```

## 🎓 Training Flow

```
1. Load training_data.json
   ↓
2. Create embeddings cho patterns
   ↓
3. Save trained model (trained_model.pkl)
   ↓
4. Load job postings từ database
   ↓
5. Create embeddings cho jobs
   ↓
6. Save vào vector_db/
   ↓
7. ✅ Ready to chat!
```

## 💡 Tips

1. **Thêm nhiều patterns** cho mỗi tag để AI hiểu tốt hơn
2. **Viết responses đa dạng** để chatbot không nhàm chán
3. **Test thường xuyên** sau khi thêm training data
4. **Backup training_data.json** trước khi chỉnh sửa

## 🐛 Troubleshooting

### Lỗi: "Chưa có trained model"
```bash
python train.py
```

### Lỗi: "Không kết nối database"
- Check `.env` file
- Check SQL Server đang chạy

### Chatbot trả lời sai
- Thêm patterns vào training data
- Tăng số lượng examples
- Chạy lại training

## 📚 Docs

- API Docs: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

---

**Đơn giản, gọn gàng, dễ mở rộng! 🚀**
