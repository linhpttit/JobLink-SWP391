# 🚀 Quick Start - AI SQL Chatbot

## ✅ Đã tích hợp xong!

AI SQL Generator đã được tích hợp vào chatbot. Bây giờ chatbot có thể:
- 🤖 **Tự động generate SQL** từ câu hỏi tự nhiên
- 📊 **Thống kê phức tạp** (group by, top, count...)
- 🔍 **Vector search** cho tìm việc thông thường

---

## 🎯 Cách dùng

### Bước 1: Cài đặt dependencies

```bash
pip install groq openai
```

### Bước 2: Lấy API Key (FREE)

1. Truy cập: https://console.groq.com
2. Đăng ký tài khoản (miễn phí)
3. Lấy API key
4. Thêm vào file `.env`:

```env
GROQ_API_KEY=gsk_your_api_key_here
```

### Bước 3: Test chatbot

#### Option 1: Interactive Mode (Recommended)

```bash
python test_ai_sql_chatbot.py --mode interactive
```

Sau đó hỏi:
- "Danh sách job ở Hà Nội"
- "Top 5 job lương cao nhất"
- "Thống kê job theo địa điểm"
- "Có bao nhiêu job đang tuyển?"

#### Option 2: Auto Test

```bash
python test_ai_sql_chatbot.py --mode test
```

---

## 📝 Ví dụ câu hỏi

### ✅ Sẽ dùng AI SQL (LLM)

- "Danh sách việc làm ở Hà Nội"
- "Top 5 job lương cao nhất"
- "Thống kê job theo địa điểm"
- "Có bao nhiêu job đang tuyển?"
- "Liệt kê job theo danh mục"
- "Nhóm job theo vị trí"

### ✅ Sẽ dùng Vector Search (ChromaDB)

- "Tìm việc frontend developer"
- "Việc làm developer lương 20 triệu"
- "Job developer ở HCM"

### ✅ Sẽ dùng Trained Responses

- "Xin chào"
- "Cảm ơn"
- "Tạm biệt"

---

## 🎨 Demo Output

```
👤 You: Danh sách job ở Hà Nội

🤖 Bot (ai_sql_query):
💻 SQL: SELECT jp.job_id, jp.position, jp.title, ... WHERE p.province_name = N'Hà Nội'

🤖 AI SQL Query

❓ Câu hỏi: Danh sách job ở Hà Nội

📊 Kết quả (15 records):

**1. Frontend Developer** (ID: 123)
   📋 Senior Frontend Developer - React
   📍 Hà Nội
   💰 15-25 triệu VNĐ

**2. Backend Developer** (ID: 124)
   📋 Java Backend Developer
   📍 Hà Nội
   💰 20-30 triệu VNĐ

...
```

---

## 🔧 Troubleshooting

### Lỗi: "AI SQL Generator not available"

**Nguyên nhân:** Chưa cài package

**Giải pháp:**
```bash
pip install groq openai
```

### Lỗi: "AI SQL Generator failed to initialize"

**Nguyên nhân:** Chưa có API key

**Giải pháp:**
1. Lấy API key từ https://console.groq.com
2. Thêm vào `.env`:
   ```env
   GROQ_API_KEY=gsk_your_key_here
   ```
3. Restart chatbot

### Chatbot không dùng AI SQL

**Nguyên nhân:** Câu hỏi không match keywords

**Giải pháp:** Dùng keywords như:
- "danh sách", "list", "liệt kê"
- "thống kê", "statistics"
- "top", "cao nhất", "nhiều nhất"
- "theo địa điểm", "theo danh mục"

---

## 🎯 Logic Flow

```
User Question
    ↓
1. Check AI trained responses (greeting, thanks...)
    ↓
2. Check AI SQL keywords? (danh sách, top, thống kê...)
    ↓ YES
    AI SQL Generator (LLM) → Generate SQL → Execute → Format
    ↓ NO
3. Check position/salary/location context?
    ↓ YES
    Vector Search (ChromaDB)
    ↓ NO
4. Check job search keywords?
    ↓ YES
    Vector Search
    ↓ NO
5. Fallback response
```

---

## 💡 Tips

1. **Groq is FREE** - Không giới hạn requests
2. **Câu hỏi rõ ràng** - AI sẽ generate SQL chính xác hơn
3. **Check SQL** - Response sẽ show SQL được generate
4. **Fallback safe** - Nếu AI SQL fail, sẽ dùng vector search

---

## 📊 Performance

- **AI SQL (Groq):** ~500ms
- **Vector Search:** ~100ms
- **Trained Response:** ~10ms

---

## 🚀 Next Steps

1. ✅ Test với real data
2. ✅ Thu thập feedback
3. 🔄 Fine-tune prompts nếu cần
4. 🔄 Add caching cho common queries
5. 🔄 Monitor API usage

---

Done! 🎉

Bây giờ bạn có thể test chatbot với AI SQL Generator!
