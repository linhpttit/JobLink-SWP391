"""
Job Indexer - Index jobs mới vào ChromaDB
"""
import chromadb
from sentence_transformers import SentenceTransformer
from .db_connector import DatabaseConnector


class JobIndexer:
    """Index jobs vào vector database"""
    
    def __init__(self):
        self.db = DatabaseConnector()
        self.embedding_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')
        
        # ChromaDB client
        self.client = chromadb.PersistentClient(path="./data/chromadb")
        self.collection = self.client.get_or_create_collection(
            name="job_postings",
            metadata={"hnsw:space": "cosine"}
        )
    
    def index_job(self, job_id):
        """
        Index 1 job mới vào ChromaDB
        
        Args:
            job_id: ID của job cần index
        """
        try:
            # Load job từ database
            if not self.db.connection:
                self.db.connect()
            
            cursor = self.db.connection.cursor()
            
            query = """
                SELECT 
                    jp.job_id,
                    jp.position,
                    jp.title,
                    jp.job_desc,
                    jp.job_requirements,
                    jp.benefits,
                    jp.salary_min,
                    jp.salary_max,
                    jp.work_type,
                    jp.posted_at,
                    p.province_name as location,
                    c.name as category_name,
                    s.name as skill_name
                FROM JobsPosting jp
                LEFT JOIN Categories c ON jp.category_id = c.category_id
                LEFT JOIN Skills s ON jp.skill_id = s.skill_id
                LEFT JOIN Provinces p ON jp.province_id = p.province_id
                WHERE jp.job_id = ? AND jp.status = 'ACTIVE'
            """
            
            cursor.execute(query, (job_id,))
            row = cursor.fetchone()
            
            # Convert to dict
            if row:
                columns = [column[0] for column in cursor.description]
                job = dict(zip(columns, row))
            else:
                job = None
            
            cursor.close()
            
            if not job:
                print(f"❌ Job {job_id} không tồn tại hoặc không active")
                return False
            
            # Tạo document text
            text = f"""
            Vị trí: {job.get('position', '')}
            Tiêu đề: {job.get('title', '')}
            Mô tả: {job.get('job_desc', '')}
            Yêu cầu: {job.get('job_requirements', '')}
            Quyền lợi: {job.get('benefits', '')}
            Địa điểm: {job.get('location', '')}
            Danh mục: {job.get('category_name', '')}
            Kỹ năng: {job.get('skill_name', '')}
            """
            
            # Create embedding
            embedding = self.embedding_model.encode([text])[0]
            
            # Prepare metadata (không có None values)
            metadata = {
                'job_id': int(job['job_id']),
                'position': str(job.get('position') or ''),
                'title': str(job.get('title') or ''),
                'location': str(job.get('location') or ''),
                'salary_min': float(job.get('salary_min') or 0),
                'salary_max': float(job.get('salary_max') or 0),
                'work_type': str(job.get('work_type') or ''),
                'category': str(job.get('category_name') or ''),
                'skill': str(job.get('skill_name') or ''),
                'posted_at': str(job.get('posted_at') or '')
            }
            
            # Check if job already exists
            doc_id = f"job_{job_id}"
            try:
                self.collection.get(ids=[doc_id])
                # Job exists → Update
                self.collection.update(
                    ids=[doc_id],
                    embeddings=[embedding.tolist()],
                    documents=[text],
                    metadatas=[metadata]
                )
                print(f"✅ Updated job {job_id} in vector database")
            except:
                # Job doesn't exist → Add
                self.collection.add(
                    ids=[doc_id],
                    embeddings=[embedding.tolist()],
                    documents=[text],
                    metadatas=[metadata]
                )
                print(f"✅ Indexed job {job_id} to vector database")
            
            return True
            
        except Exception as e:
            print(f"❌ Lỗi index job {job_id}: {e}")
            return False
    
    def delete_job(self, job_id):
        """
        Xóa job khỏi ChromaDB
        
        Args:
            job_id: ID của job cần xóa
        """
        try:
            doc_id = f"job_{job_id}"
            self.collection.delete(ids=[doc_id])
            print(f"✅ Deleted job {job_id} from vector database")
            return True
        except Exception as e:
            print(f"❌ Lỗi delete job {job_id}: {e}")
            return False
    
    def reindex_all(self):
        """Re-index tất cả jobs (giống train.py)"""
        try:
            print("🔄 Re-indexing all jobs...")
            
            # Load all jobs
            if not self.db.connection:
                self.db.connect()
            
            jobs = self.db.get_all_jobs(status='ACTIVE')
            
            if not jobs:
                print("❌ Không có jobs để index")
                return False
            
            # Clear collection
            self.client.delete_collection("job_postings")
            self.collection = self.client.create_collection(
                name="job_postings",
                metadata={"hnsw:space": "cosine"}
            )
            
            documents = []
            metadatas = []
            ids = []
            
            for job in jobs:
                text = f"""
                Vị trí: {job.get('position', '')}
                Tiêu đề: {job.get('title', '')}
                Mô tả: {job.get('job_desc', '')}
                Yêu cầu: {job.get('job_requirements', '')}
                Quyền lợi: {job.get('benefits', '')}
                Địa điểm: {job.get('location', '')}
                Danh mục: {job.get('category_name', '')}
                Kỹ năng: {job.get('skill_name', '')}
                """
                
                documents.append(text)
                
                metadata = {
                    'job_id': int(job['job_id']),
                    'position': str(job.get('position') or ''),
                    'title': str(job.get('title') or ''),
                    'location': str(job.get('location') or ''),
                    'salary_min': float(job.get('salary_min') or 0),
                    'salary_max': float(job.get('salary_max') or 0),
                    'work_type': str(job.get('work_type') or ''),
                    'category': str(job.get('category_name') or ''),
                    'skill': str(job.get('skill_name') or ''),
                    'posted_at': str(job.get('posted_at') or '')
                }
                metadatas.append(metadata)
                ids.append(f"job_{job['job_id']}")
            
            # Create embeddings
            embeddings = self.embedding_model.encode(documents)
            
            # Add to ChromaDB
            self.collection.add(
                embeddings=embeddings.tolist(),
                documents=documents,
                metadatas=metadatas,
                ids=ids
            )
            
            print(f"✅ Re-indexed {len(jobs)} jobs")
            return True
            
        except Exception as e:
            print(f"❌ Lỗi re-index: {e}")
            return False


if __name__ == "__main__":
    # Test indexer
    indexer = JobIndexer()
    
    # Test index 1 job
    indexer.index_job(80)
