"""
Main Chatbot - Kết hợp AI training và job search
"""
import os
import sys

# Disable TensorFlow - Chỉ dùng PyTorch
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from core.ai_trainer import AITrainer
from core.db_connector import DatabaseConnector
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings

# AI SQL Generator
try:
    from core.ai_sql_generator import SmartSQLExecutor, add_execute_methods_to_db_connector
    AI_SQL_AVAILABLE = True
    # Add execute methods to DatabaseConnector
    add_execute_methods_to_db_connector()
except ImportError as e:
    print(f"⚠️ AI SQL Generator not available: {e}")
    AI_SQL_AVAILABLE = False


class HRChatbot:
    """HR Chatbot với AI training và job search"""
    
    def __init__(self):
        print("🚀 Khởi tạo HR Chatbot...")
        
        # Load AI trainer
        self.ai_trainer = AITrainer()
        self.ai_trainer.load_trained_model()
        
        # Load embedding model cho job search
        self.embedding_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')
        
        # Connect to vector database
        try:
            self.chroma_client = chromadb.PersistentClient(
                path='./data/vector_db',
                settings=Settings(anonymized_telemetry=False)
            )
            self.collection = self.chroma_client.get_collection("job_postings")
            print("✅ Đã kết nối vector database")
        except:
            print("⚠️ Chưa có vector database. Chạy training jobs trước!")
            self.collection = None
        
        # Database connector
        self.db = DatabaseConnector()
        
        # AI SQL Generator with RAG (required)
        self.ai_sql_executor = None
        if AI_SQL_AVAILABLE:
            try:
                # Initialize với RAG enabled (use_rag=True by default)
                self.ai_sql_executor = SmartSQLExecutor(self.db, provider='groq', use_rag=True)
                print("✅ AI SQL Generator initialized (Groq + RAG)")
            except ValueError as e:
                print(f"❌ AI SQL Generator initialization failed: {e}")
                print("💡 Please run: python train_sql_examples.py")
                self.ai_sql_executor = None
            except Exception as e:
                print(f"⚠️ AI SQL Generator failed to initialize: {e}")
                print("💡 Tips:")
                print("   1. Add GROQ_API_KEY to .env file")
                print("   2. Run: python train_sql_examples.py")
                self.ai_sql_executor = None
        
        # Context memory để nhớ conversation
        self.context = {
            'last_salary': None,
            'last_position': None,
            'last_location': None
        }
        
        print("✅ Chatbot sẵn sàng!")
    
    def chat(self, message):
        """
        Xử lý tin nhắn từ user
        
        Args:
            message: Tin nhắn từ user
            
        Returns:
            Dict chứa response và metadata
        """
        message = message.strip()
        
        # BƯỚC 1: Check AI trained responses cho greeting/thanks/goodbye/help FIRST
        # Ưu tiên cao nhất để chatbot tự nhiên hơn
        ai_result = self.ai_trainer.predict(message, threshold=0.7)
        
        non_job_tags = ['greeting', 'goodbye', 'thanks', 'help']
        if ai_result and ai_result['tag'] in non_job_tags and ai_result['confidence'] > 0.7:
            # Reset context cho các tag không liên quan job
            self.context = {
                'last_position': None,
                'last_salary': None,
                'last_location': None
            }
            
            return {
                'success': True,
                'message': message,
                'response': ai_result['response'],
                'type': 'trained',
                'tag': ai_result['tag'],
                'confidence': ai_result['confidence']
            }
        
        # BƯỚC 2: Try AI SQL Generator (cho các câu hỏi phức tạp)
        if self.ai_sql_executor:
            try:
                print(f"\n{'='*80}")
                print(f"🤖 AI SQL Generator")
                print(f"❓ Question: {message}")
                print(f"{'='*80}")
                
                sql_result = self.ai_sql_executor.query(message)
                
                if sql_result['success']:
                    print(f"\n✅ SQL Generated Successfully!")
                    print(f"💻 SQL: {sql_result.get('sql', 'N/A')}")
                    print(f"📊 Query Type: {sql_result.get('query_type', 'N/A')}")
                    print(f"{'='*80}\n")
                    
                    response = self.ai_sql_executor.format_results(sql_result)
                    return {
                        'success': True,
                        'message': message,
                        'response': response,
                        'type': 'ai_sql_query',
                        'sql': sql_result.get('sql'),
                        'query_type': sql_result.get('query_type')
                    }
                else:
                    print(f"\n⚠️ AI SQL failed: {sql_result.get('message', 'Unknown error')}")
                    print(f"{'='*80}\n")
                    
            except Exception as e:
                print(f"\n❌ AI SQL error: {e}")
                print(f"{'='*80}\n")
                # Fallback to normal flow
        
        # Extract context từ message
        self._extract_context(message)
        
        # PRIORITY CHECK: Nếu có position + salary/location → Job search ngay
        # Bỏ qua AI trained responses để tránh nhầm với salary_question
        has_position = self.context['last_position'] is not None
        has_salary = self.context['last_salary'] is not None
        has_location = self.context['last_location'] is not None
        
        if has_position and (has_salary or has_location):
            # Có position + (salary hoặc location) → Job search ngay
            jobs = self.search_jobs_with_context(message)
            response = self._format_job_results(jobs, message)
            return {
                'success': True,
                'message': message,
                'response': response,
                'type': 'job_search',
                'jobs': jobs,
                'context': self.context.copy()
            }
        
        # Bước 2: Xử lý AI trained responses (ai_result đã được predict ở trên)
        if ai_result and ai_result['tag'] != 'unknown' and ai_result['confidence'] > 0.7:
            # Có trained response với confidence cao
            
            # Nếu tag là job_statistics → Show statistics
            if ai_result['tag'] == 'job_statistics':
                stats = self.get_job_statistics()
                response = self._format_statistics(stats)
                return {
                    'success': True,
                    'message': message,
                    'response': response,
                    'type': 'statistics',
                    'statistics': stats
                }
            
            # Nếu tag là today_jobs → Show only today's jobs count
            if ai_result['tag'] == 'today_jobs':
                stats = self.get_job_statistics()
                response = self._format_today_jobs(stats)
                return {
                    'success': True,
                    'message': message,
                    'response': response,
                    'type': 'today_jobs',
                    'today_count': stats['today_jobs'] if stats else 0
                }
            
            # Nếu tag là position_inquiry → Trigger job search
            if ai_result['tag'] == 'position_inquiry':
                jobs = self.search_jobs(message)
                response = self._format_job_results(jobs, message)
                return {
                    'success': True,
                    'message': message,
                    'response': response,
                    'type': 'job_search',
                    'jobs': jobs
                }
            
            # Nếu tag là location_search → Trigger job search by location
            if ai_result['tag'] == 'location_search':
                jobs = self.search_jobs_with_context(message)
                response = self._format_job_results(jobs, message)
                return {
                    'success': True,
                    'message': message,
                    'response': response,
                    'type': 'job_search',
                    'jobs': jobs,
                    'context': self.context.copy()
                }
            
            return {
                'success': True,
                'message': message,
                'response': ai_result['response'],
                'type': 'trained',
                'tag': ai_result['tag'],
                'confidence': ai_result['confidence']
            }
        
        # Bước 3: Check nếu chỉ có position (không có salary/location)
        has_position = self.context['last_position'] is not None
        has_salary = self.context['last_salary'] is not None
        has_location = self.context['last_location'] is not None
        
        # Trigger job search nếu:
        # 1. Chỉ có position
        # 2. Có salary/location + job keywords (nhưng không có position)
        if has_position:
            jobs = self.search_jobs_with_context(message)
            response = self._format_job_results(jobs, message)
            return {
                'success': True,
                'message': message,
                'response': response,
                'type': 'job_search',
                'jobs': jobs,
                'context': self.context.copy()
            }
        
        if (has_salary and self._has_job_keywords(message)) or (has_location and self._has_job_keywords(message)):
            jobs = self.search_jobs_with_context(message)
            response = self._format_job_results(jobs, message)
            return {
                'success': True,
                'message': message,
                'response': response,
                'type': 'job_search',
                'jobs': jobs,
                'context': self.context.copy()
            }
        
        # Bước 4: Check nếu là job search (keywords)
        if self._is_job_search(message):
            jobs = self.search_jobs_with_context(message)
            response = self._format_job_results(jobs, message)
            return {
                'success': True,
                'message': message,
                'response': response,
                'type': 'job_search',
                'jobs': jobs,
                'context': self.context.copy()
            }
        
        # Bước 5: Fallback response
        return {
            'success': True,
            'message': message,
            'response': "Xin lỗi, tôi chưa hiểu câu hỏi này. Bạn có thể hỏi tôi về:\n• Tìm việc làm\n• Yêu cầu công việc\n• Mức lương\n• Cách ứng tuyển\n• Thống kê jobs (VD: 'Danh sách job ở Hà Nội', 'Top 5 job lương cao')",
            'type': 'fallback'
        }
    
    def _extract_context(self, message):
        """Extract context từ message (salary, position, location)"""
        import re
        message_lower = message.lower()
        
        # Detect nếu có position/location/salary mới
        new_position = None
        new_location = None
        new_salary = None
        
        # Extract position - Ưu tiên specific positions (dài hơn) trước
        positions = [
            # Specific positions (2+ words) - ưu tiên cao nhất
            'business analyst', 'data analyst', 'product manager',
            'frontend developer', 'backend developer', 'fullstack developer',
            'mobile developer', 'ios developer', 'android developer',
            'ui/ux designer', 'graphic designer',
            # Single word positions - specific
            'frontend', 'backend', 'fullstack', 'mobile',
            'designer', 'tester', 'qa', 'devops',
            'marketing', 'sale', 'hr',
            # Generic (cuối cùng)
            'developer'
        ]
        for pos in positions:
            if pos in message_lower:
                new_position = pos
                break
        
        # Extract location
        locations = {
            'hà nội': 'Hà Nội',
            'hanoi': 'Hà Nội',
            'hcm': 'Hồ Chí Minh',
            'hồ chí minh': 'Hồ Chí Minh',
            'sài gòn': 'Hồ Chí Minh',
            'đà nẵng': 'Đà Nẵng'
        }
        for key, value in locations.items():
            if key in message_lower:
                new_location = value
                break
        
        # Extract salary - CHỈ extract khi có từ khóa về lương
        salary_patterns = [
            # Có từ "lương" trước
            r'lương\s+(\d+)\s*(?:triệu|tr|million|m)?',
            r'mức\s+lương\s+(\d+)\s*(?:triệu|tr|million|m)?',
            r'thu\s+nhập\s+(\d+)\s*(?:triệu|tr|million|m)?',
            r'salary\s+(\d+)\s*(?:triệu|tr|million|m)?',
            # Có từ "lương" sau
            r'(\d+)\s*(?:triệu|tr)\s+lương',
            # Có "từ", "trở lên", ">", ">="
            r'từ\s+(\d+)\s*(?:triệu|tr)',
            r'(\d+)\s*(?:triệu|tr)\s+trở\s+lên',
            r'>=?\s*(\d+)\s*(?:triệu|tr)',
            r'>\s*(\d+)\s*(?:triệu|tr)'
        ]
        
        for pattern in salary_patterns:
            match = re.search(pattern, message_lower)
            if match:
                amount = int(match.group(1))
                new_salary = amount * 1000000  # Convert to VND
                break
        
        # Logic reset context:
        # 1. Nếu message CHỈ có location (không có position/salary) 
        #    → Reset position cũ
        # 2. Nếu message CHỈ có position (không có location/salary)
        #    → Reset location cũ
        if new_location and not new_position and not new_salary:
            # Message chỉ có location → Clear position cũ
            self.context['last_position'] = None
            self.context['last_salary'] = None
            self.context['last_location'] = new_location
        elif new_position and not new_location and not new_salary:
            # Message chỉ có position → Clear location cũ
            self.context['last_position'] = new_position
            self.context['last_location'] = None
            self.context['last_salary'] = None
        else:
            # Update context bình thường
            if new_position:
                self.context['last_position'] = new_position
            if new_location:
                self.context['last_location'] = new_location
            if new_salary:
                self.context['last_salary'] = new_salary
    
    def search_jobs_with_context(self, query):
        """Search jobs với context filters - Ưu tiên dùng AI SQL Generator"""
        
        # OPTION 1: Try AI SQL Generator first (nếu có context)
        if self.ai_sql_executor and (self.context['last_position'] or self.context['last_location'] or self.context['last_salary']):
            try:
                # Build natural language query từ context
                nl_query_parts = []
                
                if self.context['last_position']:
                    nl_query_parts.append(f"vị trí {self.context['last_position']}")
                
                if self.context['last_location']:
                    nl_query_parts.append(f"ở {self.context['last_location']}")
                
                if self.context['last_salary']:
                    salary_m = int(self.context['last_salary'] / 1000000)
                    nl_query_parts.append(f"lương từ {salary_m} triệu trở lên")
                
                nl_query = "Danh sách việc làm " + " ".join(nl_query_parts)
                
                print(f"\n🤖 Using AI SQL for job search: {nl_query}")
                
                # Generate SQL
                sql_result = self.ai_sql_executor.query(nl_query)
                
                if sql_result['success'] and sql_result.get('results'):
                    print(f"✅ AI SQL returned {len(sql_result['results'])} jobs")
                    
                    # Convert SQL results to job format
                    jobs = []
                    for row in sql_result['results'][:10]:
                        job = {
                            'job_id': row.get('job_id', 0),
                            'position': row.get('position', ''),
                            'title': row.get('title', ''),
                            'location': row.get('location', ''),
                            'salary_min': row.get('salary_min', 0),
                            'salary_max': row.get('salary_max', 0),
                            'work_type': row.get('work_type', ''),
                            'posted_at': row.get('posted_at', ''),
                            'relevance': 0.9  # High relevance vì exact match
                        }
                        jobs.append(job)
                    
                    return jobs[:5]
                else:
                    print(f"⚠️ AI SQL failed or no results, fallback to vector search")
                    
            except Exception as e:
                print(f"⚠️ AI SQL error in job search: {e}")
                # Fallback to vector search
        
        # OPTION 2: Fallback to ChromaDB vector search
        print(f"\n🔍 Using ChromaDB vector search")
        
        # Build enhanced query với context
        search_query = query
        if self.context['last_location'] and not self.context['last_position']:
            search_query = f"việc làm {self.context['last_location']}"
        elif self.context['last_position'] and self.context['last_location']:
            search_query = f"{self.context['last_position']} {self.context['last_location']}"
        
        # Build ChromaDB metadata filters (WHERE clause)
        where_filters = {}
        
        # Filter by location (exact match)
        if self.context['last_location']:
            where_filters['location'] = self.context['last_location']
        
        # Filter by salary (>=)
        if self.context['last_salary']:
            where_filters['salary_max'] = {'$gte': float(self.context['last_salary'])}
        
        # Search với metadata filters
        jobs = self.search_jobs_with_filters(search_query, where_filters, top_k=20)
        
        if not jobs:
            return []
        
        # Post-filter by position (vì ChromaDB không support LIKE/CONTAINS)
        if self.context['last_position']:
            import re
            pos_filter = self.context['last_position'].lower()
            filtered_jobs = []
            
            # Tạo pattern với word boundary
            pattern = r'\b' + re.escape(pos_filter) + r'\b'
            
            print(f"\n🔍 Filtering jobs with position: '{pos_filter}'")
            print(f"Pattern: {pattern}")
            
            for job in jobs:
                job_pos = job.get('position', '').lower()
                job_title = job.get('title', '').lower()
                
                # Check match
                pos_match = re.search(pattern, job_pos)
                title_match = re.search(pattern, job_title)
                
                print(f"\nJob: {job.get('position', 'N/A')} - {job.get('title', 'N/A')}")
                print(f"  Position match: {bool(pos_match)}")
                print(f"  Title match: {bool(title_match)}")
                
                if pos_match or title_match:
                    filtered_jobs.append(job)
                    print(f"  ✅ ADDED")
                else:
                    print(f"  ❌ FILTERED OUT")
                
                if len(filtered_jobs) >= 5:
                    break
            
            print(f"\n✅ Filtered: {len(filtered_jobs)} jobs")
            return filtered_jobs
        
        # Nếu không có position filter, return top 5
        return jobs[:5]
    
    def _has_job_keywords(self, message):
        """Check nếu message có keywords liên quan đến job"""
        message_lower = message.lower()
        job_keywords = [
            'việc', 'job', 'công việc', 'tuyển', 'ứng tuyển',
            'lương', 'salary', 'mức lương', 'thu nhập'
        ]
        return any(keyword in message_lower for keyword in job_keywords)
    
    def _is_job_search(self, message):
        """Check nếu message là job search"""
        message_lower = message.lower()
        
        # Keywords rõ ràng về job search
        job_search_keywords = [
            'tìm việc', 'việc làm', 'công việc', 'job', 'tuyển dụng',
            'tìm', 'có việc', 'cần tuyển'
        ]
        
        # Job positions
        job_positions = [
            'developer', 'dev', 'lập trình', 'programmer',
            'frontend', 'front-end', 'backend', 'back-end', 'fullstack', 'full-stack',
            'mobile', 'android', 'ios', 'react', 'angular', 'vue', 'nodejs',
            'java', 'python', 'php', '.net', 'c#', 'javascript',
            'designer', 'thiết kế', 'ui/ux', 'graphic',
            'tester', 'qa', 'kiểm thử',
            'devops', 'sysadmin', 'network',
            'data analyst', 'data scientist', 'business analyst', 'ba',
            'product manager', 'pm', 'project manager',
            'marketing', 'sale', 'hr', 'nhân sự', 'accountant', 'kế toán'
        ]
        
        # Locations
        locations = [
            'hà nội', 'hanoi', 'hn',
            'hồ chí minh', 'hcm', 'sài gòn', 'saigon',
            'đà nẵng', 'danang', 'hải phòng', 'cần thơ'
        ]
        
        # Check job search keywords
        if any(keyword in message_lower for keyword in job_search_keywords):
            return True
        
        # Check nếu chỉ gõ position name (VD: "frontend", "developer")
        # Và message ngắn (< 20 ký tự) → Có thể là job search
        if len(message) < 20:
            if any(pos in message_lower for pos in job_positions):
                return True
            if any(loc in message_lower for loc in locations):
                return True
        
        return False
    
    def search_jobs(self, query, top_k=5):
        """Tìm kiếm jobs - Ưu tiên dùng AI SQL Generator"""
        
        # OPTION 1: Try AI SQL Generator first
        if self.ai_sql_executor:
            try:
                print(f"\n🤖 Using AI SQL for basic job search: {query}")
                
                # Generate SQL
                sql_result = self.ai_sql_executor.query(f"Danh sách việc làm {query}")
                
                if sql_result['success'] and sql_result.get('results'):
                    print(f"✅ AI SQL returned {len(sql_result['results'])} jobs")
                    
                    # Convert SQL results to job format
                    jobs = []
                    for row in sql_result['results'][:top_k]:
                        job = {
                            'job_id': row.get('job_id', 0),
                            'position': row.get('position', ''),
                            'title': row.get('title', ''),
                            'location': row.get('location', ''),
                            'salary_min': row.get('salary_min', 0),
                            'salary_max': row.get('salary_max', 0),
                            'work_type': row.get('work_type', ''),
                            'posted_at': row.get('posted_at', ''),
                            'relevance': 0.85  # High relevance
                        }
                        jobs.append(job)
                    
                    return jobs
                else:
                    print(f"⚠️ AI SQL failed or no results, fallback to vector search")
                    
            except Exception as e:
                print(f"⚠️ AI SQL error in basic search: {e}")
                # Fallback to vector search
        
        # OPTION 2: Fallback to ChromaDB vector search
        print(f"\n🔍 Using ChromaDB vector search for: {query}")
        return self.search_jobs_with_filters(query, where_filters=None, top_k=top_k)
    
    def search_jobs_with_filters(self, query, where_filters=None, top_k=5):
        """Tìm kiếm jobs với ChromaDB metadata filters (WHERE clause)"""
        if not self.collection:
            return []
        
        try:
            # Create embedding
            query_embedding = self.embedding_model.encode([query])[0]
            
            # Build query parameters
            query_params = {
                'query_embeddings': [query_embedding.tolist()],
                'n_results': top_k
            }
            
            # Add WHERE filters nếu có
            if where_filters:
                query_params['where'] = where_filters
            
            # Search với filters
            results = self.collection.query(**query_params)
            
            jobs = []
            if results['ids'] and results['ids'][0]:
                for i in range(len(results['ids'][0])):
                    metadata = results['metadatas'][0][i]
                    distance = results['distances'][0][i]
                    
                    # Fix relevance calculation
                    # ChromaDB distance càng nhỏ càng giống
                    # Convert to similarity score (0-100%)
                    if distance < 0:
                        distance = abs(distance)
                    
                    # Công thức cải thiện: 
                    # relevance = 100 / (1 + distance * 10)
                    # Điều này cho score cao hơn và dễ hiểu hơn
                    relevance = min(100, 100 / (1 + distance * 10))
                    
                    # Boost relevance cho jobs mới đăng
                    # Nếu job có posted_at trong metadata
                    if 'posted_at' in metadata:
                        from datetime import datetime, timedelta
                        try:
                            posted_date = datetime.fromisoformat(str(metadata['posted_at']))
                            days_ago = (datetime.now() - posted_date).days
                            
                            # Boost jobs trong 7 ngày gần nhất
                            if days_ago <= 7:
                                boost_factor = 1.0 + (7 - days_ago) * 0.05  # Max boost 35%
                                relevance = min(100, relevance * boost_factor)
                        except:
                            pass
                    
                    job = {
                        'job_id': metadata['job_id'],
                        'position': metadata.get('position', ''),
                        'title': metadata.get('title', ''),
                        'location': metadata.get('location', ''),
                        'salary_min': metadata.get('salary_min', 0),
                        'salary_max': metadata.get('salary_max', 0),
                        'work_type': metadata.get('work_type', ''),
                        'posted_at': metadata.get('posted_at', ''),
                        'relevance': relevance / 100  # Normalize về 0-1
                    }
                    jobs.append(job)
            
            # Sort by relevance (sau khi boost) + posted_at
            # Ưu tiên: relevance cao + ngày đăng mới
            def sort_key(job):
                from datetime import datetime
                relevance_score = job['relevance']
                
                # Tính recency score (0-1)
                recency_score = 0
                if job.get('posted_at'):
                    try:
                        posted_date = datetime.fromisoformat(str(job['posted_at']))
                        days_ago = (datetime.now() - posted_date).days
                        # Jobs mới hơn có score cao hơn
                        recency_score = max(0, 1 - (days_ago / 30))  # Decay trong 30 ngày
                    except:
                        pass
                
                # Kết hợp: 70% relevance + 30% recency
                combined_score = (relevance_score * 0.7) + (recency_score * 0.3)
                return combined_score
            
            jobs.sort(key=sort_key, reverse=True)
            
            # Return top_k jobs
            return jobs[:top_k]
        except Exception as e:
            print(f"❌ Lỗi search: {e}")
            return []
    
    def get_job_statistics(self):
        """Lấy thống kê jobs từ database - Ưu tiên dùng AI SQL"""
        
        # OPTION 1: Try AI SQL Generator for statistics
        if self.ai_sql_executor:
            try:
                print(f"\n🤖 Using AI SQL for statistics")
                
                # Get multiple statistics
                stats_result = {}
                
                # 1. Total active jobs
                total_query = self.ai_sql_executor.query("Có bao nhiêu job đang tuyển?")
                if total_query['success'] and total_query.get('result') is not None:
                    stats_result['total_active'] = total_query['result']
                
                # 2. Today's jobs
                today_query = self.ai_sql_executor.query("Có bao nhiêu job đăng hôm nay?")
                if today_query['success'] and today_query.get('result') is not None:
                    stats_result['today_jobs'] = today_query['result']
                else:
                    stats_result['today_jobs'] = 0
                
                # 3. Top positions
                positions_query = self.ai_sql_executor.query("Top 5 vị trí có nhiều job nhất")
                if positions_query['success'] and positions_query.get('results'):
                    stats_result['top_positions'] = []
                    for row in positions_query['results'][:5]:
                        stats_result['top_positions'].append({
                            'position': row.get('position', 'N/A'),
                            'count': row.get('job_count', 0)
                        })
                
                # 4. Locations
                locations_query = self.ai_sql_executor.query("Thống kê job theo địa điểm")
                if locations_query['success'] and locations_query.get('results'):
                    stats_result['locations'] = []
                    for row in locations_query['results'][:5]:
                        stats_result['locations'].append({
                            'location': row.get('location', 'N/A'),
                            'count': row.get('job_count', 0)
                        })
                
                # 5. Categories
                categories_query = self.ai_sql_executor.query("Thống kê job theo danh mục")
                if categories_query['success'] and categories_query.get('results'):
                    stats_result['categories'] = []
                    for row in categories_query['results'][:5]:
                        stats_result['categories'].append({
                            'category': row.get('name', 'N/A'),
                            'count': row.get('job_count', 0)
                        })
                
                # If we got at least total_active, return AI SQL results
                if 'total_active' in stats_result:
                    print(f"✅ AI SQL statistics successful")
                    return stats_result
                else:
                    print(f"⚠️ AI SQL statistics incomplete, fallback to DB")
                    
            except Exception as e:
                print(f"⚠️ AI SQL error in statistics: {e}")
                # Fallback to DB
        
        # OPTION 2: Fallback to database query
        print(f"\n🔍 Using database for statistics")
        if not self.db.connection:
            self.db.connect()
        
        return self.db.get_job_statistics()
    
    def _format_today_jobs(self, stats):
        """Format today's jobs count - CHỈ đếm số lượng"""
        if not stats:
            return "😔 Xin lỗi, không thể lấy thông tin lúc này."
        
        today_count = stats['today_jobs']
        total_active = stats['total_active']
        
        response = f"📅 **VIỆC LÀM HÔM NAY**\n\n"
        
        if today_count == 0:
            response += "😔 Hôm nay chưa có việc làm mới được đăng.\n\n"
        else:
            response += f"🎉 Hôm nay có **{today_count}** việc làm mới được đăng!\n\n"
        
        response += f"💼 Tổng số việc làm đang tuyển: **{total_active}** jobs\n\n"
        response += "💡 Hỏi tôi về vị trí bạn quan tâm để tìm việc phù hợp!"
        
        return response
    
    def _format_statistics(self, stats):
        """Format statistics thành response text"""
        if not stats:
            return "😔 Xin lỗi, không thể lấy thống kê lúc này."
        
        response = "📊 **THỐNG KÊ VIỆC LÀM**\n\n"
        
        # Tổng quan
        response += f"📈 **Tổng quan:**\n"
        response += f"• Tổng số việc làm đang tuyển: **{stats['total_active']}** jobs\n"
        response += f"• Việc làm mới hôm nay: **{stats['today_jobs']}** jobs\n\n"
        
        # Top positions
        if stats['top_positions']:
            response += f"🔥 **Top 5 vị trí hot:**\n"
            for i, pos in enumerate(stats['top_positions'], 1):
                response += f"{i}. {pos['position']}: **{pos['count']}** jobs\n"
            response += "\n"
        
        # Locations
        if stats['locations']:
            response += f"📍 **Theo địa điểm:**\n"
            for loc in stats['locations'][:5]:
                response += f"• {loc['location']}: **{loc['count']}** jobs\n"
            response += "\n"
        
        # Categories
        if stats['categories']:
            response += f"📂 **Theo danh mục:**\n"
            for cat in stats['categories'][:5]:
                response += f"• {cat['category']}: **{cat['count']}** jobs\n"
        
        response += "\n💡 Hỏi tôi về vị trí bạn quan tâm để tìm việc phù hợp!"
        
        return response
    
    def _format_job_results(self, jobs, query):
        """Format job results thành response text"""
        if not jobs:
            # Build context message
            context_parts = []
            if self.context['last_position']:
                context_parts.append(f"vị trí **{self.context['last_position']}**")
            if self.context['last_salary']:
                salary_m = self.context['last_salary'] / 1000000
                context_parts.append(f"lương từ **{salary_m:.0f} triệu**")
            if self.context['last_location']:
                context_parts.append(f"tại **{self.context['last_location']}**")
            
            context_text = " ".join(context_parts) if context_parts else f"'{query}'"
            
            return f"😔 Xin lỗi, tôi không tìm thấy công việc nào {context_text}.\n\nBạn có thể thử:\n• Giảm yêu cầu về lương\n• Thay đổi vị trí\n• Tìm ở địa điểm khác"
        
        # Build header với context
        header_parts = []
        if self.context['last_position']:
            header_parts.append(f"**{self.context['last_position']}**")
        if self.context['last_salary']:
            salary_m = self.context['last_salary'] / 1000000
            header_parts.append(f"lương từ **{salary_m:.0f} triệu**")
        if self.context['last_location']:
            header_parts.append(f"tại **{self.context['last_location']}**")
        
        header = " ".join(header_parts) if header_parts else "phù hợp"
        
        response = f"🎯 Tôi tìm thấy **{len(jobs)}** công việc {header}:\n\n"
        
        for i, job in enumerate(jobs, 1):
            salary_min = int(job['salary_min'] / 1000000)
            salary_max = int(job['salary_max'] / 1000000)
            
            # Check if job is new (posted within 7 days)
            is_new = False
            if job.get('posted_at'):
                from datetime import datetime
                try:
                    posted_date = datetime.fromisoformat(str(job['posted_at']))
                    days_ago = (datetime.now() - posted_date).days
                    is_new = days_ago <= 7
                except:
                    pass
            
            new_badge = " 🆕" if is_new else ""
            
            response += f"**{i}. {job['position']}**{new_badge} (ID: {job['job_id']})\n"
            response += f"   📋 {job['title']}\n"
            response += f"   📍 {job['location']}\n"
            response += f"   💰 {salary_min}-{salary_max} triệu VNĐ\n"
            response += f"   💼 {job['work_type']}\n"
            response += f"   ✨ Độ phù hợp: {job['relevance']:.1%}\n\n"
        
        return response


if __name__ == "__main__":
    # Test chatbot
    chatbot = HRChatbot()
    
    print("\n" + "="*60)
    print("HR CHATBOT TEST")
    print("="*60)
    
    test_messages = [
        "xin chào",
        "tìm việc làm developer",
        "cảm ơn",
        "yêu cầu gì",
        "tạm biệt"
    ]
    
    for msg in test_messages:
        print(f"\n👤 User: {msg}")
        result = chatbot.chat(msg)
        print(f"🤖 Bot ({result['type']}): {result['response']}")
