"""
AI SQL Generator - Sử dụng LLM để tự động generate SQL từ natural language
Text-to-SQL với AI (OpenAI GPT / Groq / Local LLM)
"""
import os
import re
from typing import Dict
from dotenv import load_dotenv

load_dotenv()


class AISQLGenerator:
    """
    AI-powered SQL Generator sử dụng LLM với RAG
    Hỗ trợ: OpenAI GPT, Groq, hoặc Local LLM
    """
    
    def __init__(self, provider='groq', use_rag=True):
        """
        Initialize AI SQL Generator
        
        Args:
            provider: 'openai', 'groq', hoặc 'local'
            use_rag: Sử dụng RAG (Retrieval-Augmented Generation) với vector DB (REQUIRED)
        """
        self.provider = provider
        self.api_key = None
        self.client = None
        self.use_rag = use_rag
        self.sql_trainer = None
        
        # Initialize RAG (REQUIRED - No fallback to static prompt)
        if not self.use_rag:
            raise ValueError("RAG is required! Set use_rag=True and train SQL examples first.")
        
        try:
            from core.sql_examples_trainer import SQLExamplesTrainer
            self.sql_trainer = SQLExamplesTrainer()
            print("✅ RAG enabled - Using SQL examples vector database")
        except Exception as e:
            raise ValueError(f"RAG initialization failed: {e}\nPlease run: python train_sql_examples.py")
        
        # Initialize LLM client
        self._init_llm_client()
    
    def _init_llm_client(self):
        """Initialize LLM client based on provider"""
        if self.provider == 'openai':
            try:
                import openai
                self.api_key = os.getenv('OPENAI_API_KEY')
                if self.api_key:
                    self.client = openai.OpenAI(api_key=self.api_key)
                    print("✅ OpenAI client initialized")
                else:
                    print("⚠️ OPENAI_API_KEY not found in .env")
            except ImportError:
                print("⚠️ openai package not installed. Run: pip install openai")
        
        elif self.provider == 'groq':
            try:
                from groq import Groq
                self.api_key = os.getenv('GROQ_API_KEY')
                if self.api_key:
                    self.client = Groq(api_key=self.api_key)
                    print("✅ Groq client initialized")
                else:
                    print("⚠️ GROQ_API_KEY not found in .env")
            except ImportError:
                print("⚠️ groq package not installed. Run: pip install groq")
        
        elif self.provider == 'local':
            # TODO: Implement local LLM (Ollama, LLaMA, etc.)
            print("⚠️ Local LLM not implemented yet")
    
    def generate_sql(self, question: str) -> Dict:
        """
        Generate SQL query từ natural language sử dụng AI
        
        Args:
            question: Câu hỏi từ user
            
        Returns:
            Dict chứa SQL query và metadata
        """
        if not self.client:
            return {
                'success': False,
                'message': f'LLM client ({self.provider}) chưa được khởi tạo. Kiểm tra API key.',
                'sql': None
            }
        
        # Build prompt
        prompt = self._build_prompt(question)
        
        # Call LLM
        try:
            sql_query = self._call_llm(prompt)
            
            # Validate and clean SQL
            sql_query = self._clean_sql(sql_query)
            
            if not sql_query:
                return {
                    'success': False,
                    'message': 'AI không thể generate SQL query hợp lệ',
                    'sql': None
                }
            
            return {
                'success': True,
                'sql': sql_query,
                'provider': self.provider,
                'question': question
            }
            
        except Exception as e:
            return {
                'success': False,
                'message': f'Lỗi khi gọi LLM: {str(e)}',
                'sql': None
            }
    
    def _build_prompt(self, question: str) -> str:
        """Build prompt cho LLM với RAG - Learn from examples only"""
        
        # RAG ONLY - No static schema or rules
        if not self.use_rag or not self.sql_trainer:
            raise ValueError("RAG is required! Please train SQL examples first: python train_sql_examples.py")
        
        try:
            # Search for similar examples from vector database
            similar_examples = self.sql_trainer.search_similar_examples(question, top_k=5)
            
            if not similar_examples:
                raise ValueError(f"No similar examples found for: {question}. Please add more examples to vector database.")
            
            # Simple prompt - Learn from examples only
            prompt = """You are an expert SQL developer. Learn from these examples and generate ONLY the SQL query for the new question.

IMPORTANT: Return ONLY the SQL query, no explanations, no markdown, no comments.

EXAMPLES:

"""
            
            for i, ex in enumerate(similar_examples, 1):
                similarity_pct = ex['similarity'] * 100
                prompt += f"Example {i}:\n"
                prompt += f"Question: \"{ex['question']}\"\n"
                prompt += f"SQL: {ex['sql']}\n\n"
            
            print(f"🔍 RAG: Found {len(similar_examples)} similar examples")
            print(f"   Best match: \"{similar_examples[0]['question']}\" ({similar_examples[0]['similarity']:.1%})")
            
        except Exception as e:
            raise ValueError(f"RAG search failed: {e}. Make sure to train SQL examples first!")
        
        # Add current question
        prompt += f"""Now generate ONLY the SQL query (no explanation):
Question: "{question}"
SQL:"""
        
        return prompt
    
    def _call_llm(self, prompt: str) -> str:
        """Call LLM API to generate SQL"""
        if self.provider == 'openai':
            response = self.client.chat.completions.create(
                model="gpt-3.5-turbo",  # hoặc "gpt-4" nếu có
                messages=[
                    {"role": "system", "content": "You are an expert SQL developer specializing in SQL Server."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1,  # Low temperature for consistent SQL
                max_tokens=500
            )
            return response.choices[0].message.content.strip()
        
        elif self.provider == 'groq':
            response = self.client.chat.completions.create(
                model="llama-3.3-70b-versatile",  # Updated model (Nov 2024)
                messages=[
                    {"role": "system", "content": "You are an expert SQL developer specializing in SQL Server."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1,
                max_tokens=500
            )
            return response.choices[0].message.content.strip()
        
        return ""
    
    def _clean_sql(self, sql: str) -> str:
        """Clean and validate SQL query"""
        if not sql:
            return ""
        
        # Extract SQL from explanation if present
        # Look for SELECT statement
        select_match = re.search(r'(SELECT\s+.*?(?:;|$))', sql, re.IGNORECASE | re.DOTALL)
        if select_match:
            sql = select_match.group(1)
        
        # Remove markdown code blocks
        sql = re.sub(r'```sql\s*', '', sql)
        sql = re.sub(r'```\s*', '', sql)
        
        # Remove comments
        sql = re.sub(r'--.*$', '', sql, flags=re.MULTILINE)
        
        # Remove extra whitespace
        sql = ' '.join(sql.split())
        
        # Remove trailing semicolon if present
        sql = sql.rstrip(';').strip()
        
        # Basic SQL injection prevention
        dangerous_keywords = ['DROP', 'DELETE', 'TRUNCATE', 'ALTER', 'CREATE', 'INSERT', 'UPDATE', 'EXEC', 'EXECUTE']
        sql_upper = sql.upper()
        for keyword in dangerous_keywords:
            if keyword in sql_upper:
                print(f"⚠️ Dangerous keyword detected: {keyword}")
                return ""
        
        return sql.strip()
    

class SmartSQLExecutor:
    """
    Smart SQL Executor - Kết hợp AI SQL Generator với Database Connector
    """
    
    def __init__(self, db_connector, provider='groq', use_rag=True):
        """
        Initialize Smart SQL Executor
        
        Args:
            db_connector: DatabaseConnector instance
            provider: LLM provider ('openai', 'groq', 'local')
            use_rag: Sử dụng RAG với vector database (REQUIRED)
        """
        self.db = db_connector
        self.ai_sql = AISQLGenerator(provider=provider, use_rag=use_rag)
    
    def query(self, question: str) -> Dict:
        """
        Execute natural language query
        
        Args:
            question: Câu hỏi từ user
            
        Returns:
            Dict chứa results và metadata
        """
        # Generate SQL
        sql_result = self.ai_sql.generate_sql(question)
        
        if not sql_result['success']:
            return {
                'success': False,
                'message': sql_result['message'],
                'results': None
            }
        
        sql = sql_result['sql']
        
        print(f"\n🤖 AI Generated SQL:")
        print(f"{sql}\n")
        
        # Execute SQL
        try:
            # Detect query type
            sql_upper = sql.upper()
            
            if sql_upper.startswith('SELECT COUNT(') or 'COUNT(*)' in sql_upper:
                # Scalar query (COUNT, SUM, AVG)
                result = self.db.execute_scalar(sql)
                return {
                    'success': True,
                    'query_type': 'scalar',
                    'result': result,
                    'sql': sql,
                    'question': question
                }
            else:
                # List query
                results = self.db.execute_query(sql)
                return {
                    'success': True,
                    'query_type': 'list',
                    'results': results,
                    'count': len(results) if results else 0,
                    'sql': sql,
                    'question': question
                }
        
        except Exception as e:
            return {
                'success': False,
                'message': f'Lỗi execute SQL: {str(e)}',
                'sql': sql,
                'results': None
            }
    
    def format_results(self, query_result: Dict) -> str:
        """Format query results thành text response"""
        if not query_result['success']:
            return f"❌ {query_result['message']}"
        
        response = ""
        
        if query_result['query_type'] == 'scalar':
            # Format scalar result
            result = query_result['result']
            response += f"📊 **Kết quả:** {result}\n"
        
        elif query_result['query_type'] == 'list':
            # Format list results
            results = query_result['results']
            
            if not results:
                response += "😔 Không tìm thấy kết quả nào.\n"
            else:
                response += f"📊 **Kết quả ({len(results)} records):**\n\n"
                
                # Auto-detect format based on columns
                for i, row in enumerate(results[:10], 1):
                    if 'job_id' in row:
                        # Job format
                        response += f"**{i}. {row.get('position', 'N/A')}**\n"
                        if row.get('title'):
                            response += f"   📋 {row['title']}\n"
                        if row.get('location'):
                            response += f"   📍 {row['location']}\n"
                        if row.get('salary_min') and row.get('salary_max'):
                            sal_min = int(row['salary_min'] / 1000000)
                            sal_max = int(row['salary_max'] / 1000000)
                            response += f"   💰 {sal_min}-{sal_max} triệu VNĐ\n"
                    elif 'job_count' in row:
                        # Grouped format
                        keys = [k for k in row.keys() if k != 'job_count']
                        if keys:
                            label = row[keys[0]]
                            count = row['job_count']
                            response += f"{i}. **{label}**: {count} jobs\n"
                    else:
                        # Generic format
                        response += f"{i}. "
                        for key, value in row.items():
                            if value is not None:
                                response += f"{key}: {value}, "
                        response = response.rstrip(', ') + "\n"
                    
                    response += "\n"
                
                if len(results) > 10:
                    response += f"_...và {len(results) - 10} kết quả khác_\n"
                
        return response


# Add execute methods to DatabaseConnector if not exists
def add_execute_methods_to_db_connector():
    """Helper function to add execute methods to DatabaseConnector"""
    from core.db_connector import DatabaseConnector
    
    if not hasattr(DatabaseConnector, 'execute_query'):
        def execute_query(self, sql, params=None):
            """Execute dynamic SQL query"""
            if not self.connection:
                self.connect()
            
            try:
                cursor = self.connection.cursor()
                if params:
                    cursor.execute(sql, params)
                else:
                    cursor.execute(sql)
                
                columns = [column[0] for column in cursor.description]
                results = []
                for row in cursor.fetchall():
                    results.append(dict(zip(columns, row)))
                return results
            except Exception as e:
                print(f"❌ Lỗi execute query: {e}")
                return None
        
        DatabaseConnector.execute_query = execute_query
    
    if not hasattr(DatabaseConnector, 'execute_scalar'):
        def execute_scalar(self, sql, params=None):
            """Execute query và trả về single value"""
            if not self.connection:
                self.connect()
            
            try:
                cursor = self.connection.cursor()
                if params:
                    cursor.execute(sql, params)
                else:
                    cursor.execute(sql)
                
                result = cursor.fetchone()
                return result[0] if result else None
            except Exception as e:
                print(f"❌ Lỗi execute scalar: {e}")
                return None
        
        DatabaseConnector.execute_scalar = execute_scalar


if __name__ == "__main__":
    # Test AI SQL Generator
    print("="*80)
    print("🤖 TEST AI SQL GENERATOR")
    print("="*80)
    
    # Add execute methods
    add_execute_methods_to_db_connector()
    
    from core.db_connector import DatabaseConnector
    
    # Initialize
    db = DatabaseConnector()
    executor = SmartSQLExecutor(db, provider='groq')  # hoặc 'openai'
    
    # Test questions
    test_questions = [
        "Có bao nhiêu job đang tuyển?",
        "Danh sách việc làm ở Hà Nội",
        "Top 5 job lương cao nhất",
        "Thống kê job theo địa điểm",
        "Việc làm developer lương trên 20 triệu"
    ]
    
    for question in test_questions:
        print(f"\n{'='*80}")
        print(f"❓ {question}")
        print(f"{'='*80}")
        
        result = executor.query(question)
        response = executor.format_results(result)
        print(response)
