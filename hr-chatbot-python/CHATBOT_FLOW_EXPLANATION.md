# 🤖 GIẢI THÍCH FLOW CHATBOT HR - CHI TIẾT

## 📋 Tổng Quan

Đây là một **HR Chatbot thông minh** với 3 công nghệ AI chính:
1. **AI Training** - Trả lời các câu hỏi chung (chào hỏi, cảm ơn, help...)
2. **AI SQL Generator** - Tự động tạo SQL từ câu hỏi tự nhiên (thống kê, danh sách, top...)
3. **Vector Search** - Tìm kiếm việc làm bằng semantic search

---

## 🏗️ Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INPUT                               │
│                    "Tìm việc developer ở Hà Nội"                │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      HRChatbot (chatbot.py)                      │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BƯỚC 1: Check AI Trained Responses (Greeting/Thanks)    │  │
│  │         → AITrainer.predict()                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             │                                    │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BƯỚC 2: Try AI SQL Generator (Thống kê/Danh sách)       │  │
│  │         → SmartSQLExecutor.query()                        │  │
│  │         → LLM (Groq) + RAG (Vector DB)                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             │                                    │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BƯỚC 3: Extract Context (Position/Salary/Location)       │  │
│  │         → _extract_context()                              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             │                                    │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BƯỚC 4: Job Search với Context/Filters                   │  │
│  │         → search_jobs_with_context()                      │  │
│  │         → ChromaDB Vector Search                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             │                                    │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ BƯỚC 5: Fallback Response                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      RESPONSE TO USER                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 FLOW CHI TIẾT - TỪNG BƯỚC

### **BƯỚC 1: Check AI Trained Responses** 🎯

**Mục đích:** Xử lý các câu hỏi chung (greeting, thanks, goodbye, help)

**Cách hoạt động:**
```python
# File: core/chatbot.py (dòng 99-117)
ai_result = self.ai_trainer.predict(message, threshold=0.7)

non_job_tags = ['greeting', 'goodbye', 'thanks', 'help']
if ai_result and ai_result['tag'] in non_job_tags and ai_result['confidence'] > 0.7:
    return trained_response
```

**Ví dụ:**
```
👤 User: "Xin chào"
🤖 Bot: "Xin chào! 👋 Tôi là trợ lý tuyển dụng AI..."
     [Type: trained, Tag: greeting, Confidence: 98.5%]
```

**Công nghệ:**
- **SentenceTransformer** - Tạo embedding từ câu hỏi
- **Cosine Similarity** - So sánh với patterns đã train
- **Training Data** - Từ file `data/training_data.json`

---

### **BƯỚC 2: AI SQL Generator** 🤖💻

**Mục đích:** Xử lý các câu hỏi phức tạp cần thống kê, danh sách, top...

**Cách hoạt động:**
```python
# File: core/chatbot.py (dòng 120-151)
if self.ai_sql_executor:
    sql_result = self.ai_sql_executor.query(message)
    if sql_result['success']:
        return ai_sql_response
```

**Flow của AI SQL Generator:**

```
User Question: "Top 5 job lương cao nhất"
        │
        ▼
┌───────────────────────────────────────────────┐
│ 1. RAG - Search Similar Examples             │
│    → SQLExamplesTrainer.search_similar()      │
│    → ChromaDB Vector Search                   │
│    → Tìm 5 examples tương tự nhất             │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 2. Build Prompt với Examples                 │
│    → Gửi examples + question cho LLM          │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 3. LLM Generate SQL                           │
│    → Groq API (llama-3.3-70b-versatile)       │
│    → Học từ examples và tạo SQL mới           │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 4. Clean & Validate SQL                       │
│    → Remove comments, markdown                │
│    → Check dangerous keywords                 │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 5. Execute SQL on Database                    │
│    → DatabaseConnector.execute_query()        │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 6. Format Results                             │
│    → SmartSQLExecutor.format_results()        │
└───────────────────────────────────────────────┘
```

**Ví dụ:**
```
👤 User: "Top 5 job lương cao nhất"

🔍 RAG: Found 5 similar examples
   Best match: "Top 10 job lương cao nhất" (95.2%)

🤖 AI Generated SQL:
SELECT TOP 5 jp.job_id, jp.position, jp.title, jp.salary_max
FROM JobsPosting jp
WHERE jp.status = 'ACTIVE'
ORDER BY jp.salary_max DESC

📊 Kết quả (5 records):
1. Senior Backend Developer (ID: 123)
   📋 Java Backend Developer - Spring Boot
   📍 Hà Nội
   💰 30-50 triệu VNĐ

2. Tech Lead (ID: 124)
   ...
```

**Công nghệ:**
- **RAG (Retrieval-Augmented Generation)** - Học từ examples
- **ChromaDB** - Vector database lưu SQL examples
- **Groq API** - LLM (Llama 3.3 70B) - FREE
- **SQL Server** - Execute SQL queries

---

### **BƯỚC 3: Extract Context** 🔍

**Mục đích:** Trích xuất thông tin từ câu hỏi (vị trí, lương, địa điểm)

**Cách hoạt động:**
```python
# File: core/chatbot.py (dòng 317-405)
def _extract_context(self, message):
    # Extract position (developer, designer, tester...)
    # Extract location (Hà Nội, HCM, Đà Nẵng...)
    # Extract salary (20 triệu, từ 15 triệu...)
    
    self.context = {
        'last_position': 'developer',
        'last_location': 'Hà Nội',
        'last_salary': 20000000  # VNĐ
    }
```

**Ví dụ:**
```
👤 User: "Tìm việc frontend developer lương 20 triệu ở Hà Nội"

Context extracted:
├─ Position: "frontend developer"
├─ Salary: 20,000,000 VNĐ
└─ Location: "Hà Nội"
```

**Regex Patterns:**
- **Position:** `frontend|backend|developer|designer|tester...`
- **Location:** `hà nội|hcm|hồ chí minh|đà nẵng...`
- **Salary:** `lương (\d+) triệu|từ (\d+) triệu|>= (\d+) triệu...`

---

### **BƯỚC 4: Job Search với Context** 🎯

**Mục đích:** Tìm việc làm phù hợp với context

**2 Options:**

#### **Option 1: AI SQL Generator (Ưu tiên)**
```python
# File: core/chatbot.py (dòng 411-458)
if self.ai_sql_executor and has_context:
    # Build natural language query từ context
    nl_query = "Danh sách việc làm frontend developer ở Hà Nội lương từ 20 triệu"
    
    # Generate SQL và execute
    sql_result = self.ai_sql_executor.query(nl_query)
    
    # Convert results to job format
    return jobs
```

#### **Option 2: ChromaDB Vector Search (Fallback)**
```python
# File: core/chatbot.py (dòng 460-524)
# Build search query
search_query = "frontend developer Hà Nội"

# Build metadata filters
where_filters = {
    'location': 'Hà Nội',
    'salary_max': {'$gte': 20000000}
}

# Search với filters
jobs = self.search_jobs_with_filters(search_query, where_filters)

# Post-filter by position (regex)
filtered_jobs = filter_by_position(jobs, 'frontend developer')
```

**Flow của Vector Search:**

```
Query: "frontend developer Hà Nội"
        │
        ▼
┌───────────────────────────────────────────────┐
│ 1. Create Embedding                           │
│    → SentenceTransformer.encode()             │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 2. Search ChromaDB với Filters                │
│    → collection.query()                        │
│    → WHERE location = 'Hà Nội'                │
│    → AND salary_max >= 20000000               │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 3. Calculate Relevance Score                  │
│    → relevance = 100 / (1 + distance * 10)    │
│    → Boost jobs mới đăng (+35%)               │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 4. Post-filter by Position (Regex)            │
│    → Match "frontend" in position/title       │
└───────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────┐
│ 5. Sort by Relevance + Recency                │
│    → 70% relevance + 30% recency              │
└───────────────────────────────────────────────┘
        │
        ▼
Return Top 5 Jobs
```

**Ví dụ:**
```
👤 User: "Tìm việc frontend developer lương 20 triệu ở Hà Nội"

🔍 Using ChromaDB vector search

📊 Kết quả (3 công việc):

**1. Frontend Developer** (ID: 123)
   📋 Senior Frontend Developer - React
   📍 Hà Nội
   💰 20-30 triệu VNĐ
   ⭐ Relevance: 92%

**2. Frontend Developer** (ID: 124)
   📋 Frontend Developer - Vue.js
   📍 Hà Nội
   💰 18-25 triệu VNĐ
   ⭐ Relevance: 88%

**3. Frontend Developer** (ID: 125)
   📋 Junior Frontend Developer
   📍 Hà Nội
   💰 15-22 triệu VNĐ
   ⭐ Relevance: 85%
```

---

### **BƯỚC 5: Fallback Response** 🤷

**Mục đích:** Khi không hiểu câu hỏi

```python
return {
    'response': "Xin lỗi, tôi chưa hiểu câu hỏi này. Bạn có thể hỏi tôi về:\n"
                "• Tìm việc làm\n"
                "• Yêu cầu công việc\n"
                "• Mức lương\n"
                "• Cách ứng tuyển\n"
                "• Thống kê jobs",
    'type': 'fallback'
}
```

---

## 🎓 TRAINING FLOW

### **1. Train AI Messages**

```bash
python train.py
```

**Flow:**
```
1. Load training_data.json
   ↓
2. AITrainer.train()
   ├─ Create embeddings cho patterns
   ├─ Save embeddings + tags + responses
   └─ Save to trained_model.pkl
   ↓
3. ✅ AI Training Done!
```

**File:** `data/training_data.json`
```json
{
  "conversations": [
    {
      "tag": "greeting",
      "patterns": ["xin chào", "hello", "hi"],
      "responses": ["Xin chào! 👋 Tôi là trợ lý AI..."]
    }
  ]
}
```

### **2. Train SQL Examples (RAG)**

```bash
python train_sql_examples.py
```

**Flow:**
```
1. Load SQL examples từ code
   ↓
2. SQLExamplesTrainer.add_examples()
   ├─ Create embeddings cho questions
   ├─ Save to ChromaDB (data/sql_examples_db/)
   └─ Store: question, SQL, description, category
   ↓
3. ✅ RAG Database Ready!
```

**Examples:**
```python
{
    'question': 'Top 5 job lương cao nhất',
    'sql': 'SELECT TOP 5 ... ORDER BY salary_max DESC',
    'description': 'Lấy 5 jobs có lương cao nhất',
    'category': 'ranking'
}
```

### **3. Index Jobs to Vector DB**

```bash
python train.py  # Hoặc API: POST /api/jobs/reindex
```

**Flow:**
```
1. Load jobs từ database (status = ACTIVE)
   ↓
2. JobIndexer.reindex_all()
   ├─ Create text từ job (position, title, desc...)
   ├─ Create embeddings
   └─ Save to ChromaDB (data/vector_db/)
   ↓
3. ✅ Job Vector DB Ready!
```

---

## 🚀 API FLOW

### **Start API Server**

```bash
python run.py api
# hoặc
python api.py
```

**Endpoints:**

#### **1. Chat Endpoint**
```bash
POST /api/chat
{
  "message": "Tìm việc developer"
}
```

**Flow:**
```
Request → FastAPI → HRChatbot.chat() → Response
```

#### **2. Train Endpoint**
```bash
POST /api/train
```

**Flow:**
```
Request → Run train.py subprocess → Response
```

#### **3. Index Job Endpoint**
```bash
POST /api/jobs/index
{
  "job_id": 123
}
```

**Flow:**
```
Request → JobIndexer.index_job(123) → ChromaDB → Response
```

---

## 📊 DECISION TREE - KHI NÀO DÙNG GÌ?

```
User Question
    │
    ├─ "Xin chào", "Cảm ơn", "Tạm biệt"
    │   → AI Trained Response (BƯỚC 1)
    │
    ├─ "Danh sách job ở Hà Nội", "Top 5 job lương cao"
    │   → AI SQL Generator (BƯỚC 2)
    │
    ├─ "Tìm việc developer lương 20 triệu"
    │   → Extract Context (BƯỚC 3)
    │   → Job Search với Context (BƯỚC 4)
    │       ├─ Try AI SQL first
    │       └─ Fallback to Vector Search
    │
    ├─ "Tìm việc frontend"
    │   → Vector Search (BƯỚC 4)
    │
    └─ "Random question"
        → Fallback Response (BƯỚC 5)
```

---

## 🔧 CÁC COMPONENT CHÍNH

### **1. HRChatbot** (`core/chatbot.py`)
- Main orchestrator
- Điều phối giữa AI Trainer, AI SQL, Vector Search
- Extract context và manage conversation flow

### **2. AITrainer** (`core/ai_trainer.py`)
- Train từ `training_data.json`
- Predict response cho greeting/thanks/help...
- Sử dụng SentenceTransformer + Cosine Similarity

### **3. SmartSQLExecutor** (`core/ai_sql_generator.py`)
- Generate SQL từ natural language
- Sử dụng Groq LLM + RAG
- Execute SQL và format results

### **4. SQLExamplesTrainer** (`core/sql_examples_trainer.py`)
- Manage SQL examples trong vector DB
- Search similar examples cho RAG
- Sử dụng ChromaDB

### **5. JobIndexer** (`core/job_indexer.py`)
- Index jobs vào ChromaDB
- Update/Delete jobs trong vector DB
- Reindex tất cả jobs

### **6. DatabaseConnector** (`core/db_connector.py`)
- Kết nối SQL Server
- Execute queries
- Load jobs từ database

---

## 💡 CÔNG NGHỆ SỬ DỤNG

| Công nghệ | Mục đích | File |
|-----------|----------|------|
| **SentenceTransformer** | Tạo embeddings | `ai_trainer.py`, `chatbot.py`, `job_indexer.py` |
| **ChromaDB** | Vector database | `chatbot.py`, `job_indexer.py`, `sql_examples_trainer.py` |
| **Groq API** | LLM (Llama 3.3 70B) | `ai_sql_generator.py` |
| **FastAPI** | REST API | `api.py` |
| **SQL Server** | Database | `db_connector.py` |
| **PyTorch** | Deep learning backend | All |

---

## 📝 VÍ DỤ THỰC TẾ

### **Ví dụ 1: Greeting**
```
👤 User: "Xin chào"

Flow:
1. ✅ BƯỚC 1: AI Trained Response
   - Tag: greeting
   - Confidence: 98.5%
   - Response: "Xin chào! 👋 Tôi là trợ lý AI..."

🤖 Bot: "Xin chào! 👋 Tôi là trợ lý tuyển dụng AI..."
```

### **Ví dụ 2: Thống kê phức tạp**
```
👤 User: "Top 5 job lương cao nhất"

Flow:
1. ❌ BƯỚC 1: Không match greeting/thanks
2. ✅ BƯỚC 2: AI SQL Generator
   - RAG: Tìm examples tương tự
   - LLM: Generate SQL
   - Execute: SELECT TOP 5 ... ORDER BY salary_max DESC
   - Format: 5 jobs

🤖 Bot: 
📊 Kết quả (5 records):
1. Senior Backend Developer - 30-50 triệu
2. Tech Lead - 28-45 triệu
...
```

### **Ví dụ 3: Tìm việc với context**
```
👤 User: "Tìm việc frontend developer lương 20 triệu ở Hà Nội"

Flow:
1. ❌ BƯỚC 1: Không match greeting
2. ❌ BƯỚC 2: Không phải thống kê
3. ✅ BƯỚC 3: Extract Context
   - Position: "frontend developer"
   - Salary: 20,000,000
   - Location: "Hà Nội"
4. ✅ BƯỚC 4: Job Search
   - Try AI SQL first
   - Fallback to Vector Search
   - Filter by context
   - Return top 5

🤖 Bot:
🎯 Tôi tìm thấy 3 công việc phù hợp:
1. Senior Frontend Developer - React
   📍 Hà Nội
   💰 20-30 triệu VNĐ
...
```

### **Ví dụ 4: Câu hỏi không hiểu**
```
👤 User: "Thời tiết hôm nay thế nào?"

Flow:
1. ❌ BƯỚC 1: Không match
2. ❌ BƯỚC 2: Không phải SQL query
3. ❌ BƯỚC 3: Không có context
4. ❌ BƯỚC 4: Không phải job search
5. ✅ BƯỚC 5: Fallback

🤖 Bot: "Xin lỗi, tôi chưa hiểu câu hỏi này..."
```

---

## 🎯 ĐIỂM MẠNH CỦA CHATBOT

1. **Thông minh** - Kết hợp 3 AI technologies
2. **Linh hoạt** - Tự động chọn phương pháp phù hợp
3. **Chính xác** - RAG giúp SQL generation chính xác
4. **Nhanh** - Vector search < 100ms
5. **Mở rộng** - Dễ thêm training data và SQL examples
6. **FREE** - Groq API miễn phí unlimited

---

## 🔄 CÁCH MỞ RỘNG

### **Thêm câu hỏi mới**
1. Edit `data/training_data.json`
2. Run `python train.py`

### **Thêm SQL examples**
1. Edit `train_sql_examples.py`
2. Run `python train_sql_examples.py`

### **Index job mới**
```bash
# Option 1: API
POST /api/jobs/index {"job_id": 123}

# Option 2: Code
from core.job_indexer import JobIndexer
indexer = JobIndexer()
indexer.index_job(123)
```

---

**Chatbot này là sự kết hợp hoàn hảo giữa AI Training, LLM, và Vector Search! 🚀**
