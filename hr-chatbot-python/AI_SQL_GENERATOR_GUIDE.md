# 🤖 AI SQL Generator - Hướng dẫn

## 📖 Giới thiệu

**AI SQL Generator** sử dụng **Large Language Models (LLM)** để tự động chuyển đổi câu hỏi tự nhiên thành SQL queries. AI sẽ **tự học** từ database schema và **tự generate** SQL phù hợp.

### ✨ Tính năng

- ✅ **Text-to-SQL tự động** - AI tự generate SQL từ câu hỏi
- ✅ **Hỗ trợ nhiều LLM providers**: OpenAI GPT, Groq (FREE), Local LLM
- ✅ **Tự học database schema** - AI hiểu cấu trúc database
- ✅ **SQL injection prevention** - Bảo mật tự động
- ✅ **Smart formatting** - Format kết quả đẹp mắt

---

## 🚀 Quick Start

### 1. Cài đặt dependencies

```bash
pip install openai groq
```

Hoặc:

```bash
pip install -r requirements.txt
```

### 2. Cấu hình API Key

Chọn **1 trong 3 options**:

#### **Option 1: Groq (RECOMMENDED - FREE & FAST)**

1. Đăng ký tài khoản miễn phí tại: https://console.groq.com
2. Lấy API key
3. Thêm vào `.env`:

```env
GROQ_API_KEY=gsk_your_api_key_here
```

**Ưu điểm:**
- ✅ **Miễn phí**
- ✅ **Rất nhanh** (Llama 3.1 70B)
- ✅ **Không giới hạn requests**

#### **Option 2: OpenAI (GPT-3.5/GPT-4)**

1. Đăng ký tại: https://platform.openai.com
2. Lấy API key
3. Thêm vào `.env`:

```env
OPENAI_API_KEY=sk-your_api_key_here
```

**Ưu điểm:**
- ✅ Chất lượng cao (GPT-4)
- ❌ Tốn phí (~$0.002/request)

#### **Option 3: Local LLM (Ollama) - Coming Soon**

```env
OLLAMA_HOST=http://localhost:11434
```

---

## 💻 Sử dụng

### 1. Test AI SQL Generator

```python
from core.ai_sql_generator import AISQLGenerator, SmartSQLExecutor, add_execute_methods_to_db_connector
from core.db_connector import DatabaseConnector

# Add execute methods to DatabaseConnector
add_execute_methods_to_db_connector()

# Initialize
db = DatabaseConnector()
executor = SmartSQLExecutor(db, provider='groq')  # hoặc 'openai'

# Query
result = executor.query("Có bao nhiêu job đang tuyển?")
response = executor.format_results(result)
print(response)
```

### 2. Tích hợp vào Chatbot

```python
from core.chatbot import HRChatbot
from core.ai_sql_generator import SmartSQLExecutor, add_execute_methods_to_db_connector

# Add execute methods
add_execute_methods_to_db_connector()

class HRChatbotWithAI(HRChatbot):
    def __init__(self):
        super().__init__()
        # Initialize AI SQL Executor
        self.ai_sql_executor = SmartSQLExecutor(self.db, provider='groq')
    
    def chat(self, message):
        # Try AI SQL first
        if self._should_use_ai_sql(message):
            result = self.ai_sql_executor.query(message)
            if result['success']:
                response = self.ai_sql_executor.format_results(result)
                return {
                    'success': True,
                    'message': message,
                    'response': response,
                    'type': 'ai_sql_query'
                }
        
        # Fallback to original chatbot logic
        return super().chat(message)
    
    def _should_use_ai_sql(self, message):
        """Check if should use AI SQL"""
        keywords = ['danh sách', 'list', 'thống kê', 'top', 'theo', 'chi tiết']
        return any(kw in message.lower() for kw in keywords)
```

---

## 📝 Ví dụ

### Câu hỏi đơn giản

```python
# COUNT query
executor.query("Có bao nhiêu job đang tuyển?")
# SQL: SELECT COUNT(*) as total FROM JobsPosting WHERE status = 'ACTIVE'

# LIST query
executor.query("Danh sách việc làm ở Hà Nội")
# SQL: SELECT jp.job_id, jp.position, jp.title, ... WHERE p.province_name = N'Hà Nội'
```

### Câu hỏi phức tạp

```python
# Filter by multiple conditions
executor.query("Việc làm developer ở HCM lương trên 20 triệu")
# SQL: SELECT ... WHERE position LIKE '%developer%' AND province_name = N'Hồ Chí Minh' AND salary_max >= 20000000

# GROUP BY query
executor.query("Thống kê job theo địa điểm")
# SQL: SELECT province_name, COUNT(*) as job_count FROM ... GROUP BY province_name

# TOP query
executor.query("Top 10 job lương cao nhất")
# SQL: SELECT TOP 10 ... ORDER BY salary_max DESC
```

### Câu hỏi tiếng Việt tự nhiên

```python
executor.query("Cho tôi xem những công việc frontend developer tại Hà Nội")
executor.query("Tìm việc làm part-time lương từ 10 đến 15 triệu")
executor.query("Có bao nhiêu công ty đang tuyển dụng?")
executor.query("Việc làm nào mới đăng hôm nay?")
```

---

## 🎯 Cách hoạt động

### 1. **AI học database schema**

```python
schema_context = """
Table: JobsPosting
- job_id (INT): ID công việc
- position (NVARCHAR): Vị trí
- salary_min, salary_max (DECIMAL): Lương
...
"""
```

### 2. **AI generate SQL từ câu hỏi**

```
User: "Có bao nhiêu job đang tuyển?"
↓
LLM (Groq/OpenAI)
↓
SQL: SELECT COUNT(*) FROM JobsPosting WHERE status = 'ACTIVE'
```

### 3. **Execute SQL và format kết quả**

```python
result = db.execute_query(sql)
formatted_response = format_results(result)
```

---

## 🔒 Bảo mật

### SQL Injection Prevention

AI SQL Generator tự động **block các lệnh nguy hiểm**:

```python
dangerous_keywords = ['DROP', 'DELETE', 'TRUNCATE', 'ALTER', 'CREATE', 'INSERT', 'UPDATE', 'EXEC']
```

Nếu AI generate SQL chứa keywords trên → **Reject ngay lập tức**

---

## 🆚 So sánh Providers

| Provider | Model | Speed | Cost | Quality |
|----------|-------|-------|------|---------|
| **Groq** | Llama 3.1 70B | ⚡⚡⚡ Very Fast | 💰 FREE | ⭐⭐⭐⭐ |
| **OpenAI** | GPT-3.5 Turbo | ⚡⚡ Fast | 💰💰 ~$0.002/req | ⭐⭐⭐⭐ |
| **OpenAI** | GPT-4 | ⚡ Slow | 💰💰💰 ~$0.03/req | ⭐⭐⭐⭐⭐ |
| **Local** | Ollama | ⚡⚡ Fast | 💰 FREE | ⭐⭐⭐ |

**Recommendation:** Dùng **Groq** (FREE + FAST + Good quality)

---

## 📊 Performance

### Groq (Llama 3.1 70B)
- **Latency:** ~500ms
- **Accuracy:** ~95% cho SQL queries đơn giản
- **Cost:** FREE

### OpenAI (GPT-3.5)
- **Latency:** ~1-2s
- **Accuracy:** ~98%
- **Cost:** ~$0.002/request

---

## 🐛 Troubleshooting

### Lỗi: "LLM client chưa được khởi tạo"

**Nguyên nhân:** Chưa cấu hình API key

**Giải pháp:**
1. Kiểm tra file `.env`:
   ```env
   GROQ_API_KEY=gsk_your_key_here
   ```
2. Restart Python

### Lỗi: "AI không thể generate SQL query hợp lệ"

**Nguyên nhân:** Câu hỏi quá phức tạp hoặc không rõ ràng

**Giải pháp:**
- Đặt câu hỏi rõ ràng hơn
- VD: "Tìm job" → "Danh sách việc làm developer"

### Lỗi: "Lỗi execute SQL"

**Nguyên nhân:** SQL syntax error hoặc table không tồn tại

**Giải pháp:**
- Kiểm tra database schema
- Update schema context trong `ai_sql_generator.py`

---

## 🎓 Advanced Usage

### Custom Prompt Engineering

```python
generator = AISQLGenerator(provider='groq')

# Customize prompt
custom_prompt = f"""
{generator.schema_context}

CUSTOM RULES:
- Always include company_name in results
- Sort by posted_at DESC by default

Question: {question}
SQL:
"""

sql = generator._call_llm(custom_prompt)
```

### Add Few-Shot Examples

Thêm examples vào prompt để AI học tốt hơn:

```python
EXAMPLES:
Question: "Việc làm mới nhất"
SQL: SELECT TOP 10 ... ORDER BY posted_at DESC

Question: "Job có lương cao"
SQL: SELECT ... WHERE salary_max > 30000000 ORDER BY salary_max DESC
```

---

## 📚 Resources

- **Groq API Docs:** https://console.groq.com/docs
- **OpenAI API Docs:** https://platform.openai.com/docs
- **Text-to-SQL Papers:** https://arxiv.org/abs/2208.13629

---

## 🚀 Next Steps

1. ✅ Tích hợp vào chatbot
2. ✅ Test với real users
3. 🔄 Fine-tune prompts
4. 🔄 Add caching cho common queries
5. 🔄 Implement local LLM (Ollama)

---

## 💡 Tips

1. **Groq is FREE** - Dùng Groq cho development
2. **Cache common queries** - Giảm API calls
3. **Validate SQL** - Always validate trước khi execute
4. **Monitor costs** - Track API usage nếu dùng OpenAI
5. **Few-shot learning** - Thêm examples để AI học tốt hơn

---

Done! 🎉
