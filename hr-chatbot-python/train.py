"""
Training Script - Train cả AI messages và job postings
"""
import os
import sys

# Disable TensorFlow - Chỉ dùng PyTorch
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.ai_trainer import AITrainer
from core.db_connector import DatabaseConnector
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings


def train_ai_messages():
    """Train AI với training messages"""
    print("\n" + "="*60)
    print("🤖 TRAINING AI MESSAGES")
    print("="*60)
    
    trainer = AITrainer()
    success = trainer.train()
    
    if success:
        print("✅ AI training hoàn tất!")
    else:
        print("❌ AI training thất bại!")
    
    return success


def train_job_postings():
    """Train job postings vào vector database"""
    print("\n" + "="*60)
    print("💼 TRAINING JOB POSTINGS")
    print("="*60)
    
    # Connect database
    db = DatabaseConnector()
    if not db.connect():
        print("❌ Không thể kết nối database!")
        return False
    
    # Get jobs
    print("📊 Đang lấy jobs từ database...")
    jobs = db.get_all_jobs(status='ACTIVE')
    
    if not jobs:
        print("⚠️ Không có jobs để train!")
        return False
    
    print(f"✅ Lấy được {len(jobs)} jobs")
    
    # Load embedding model
    print("🔄 Đang load embedding model...")
    embedding_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')
    
    # Connect ChromaDB
    print("🔄 Đang kết nối vector database...")
    chroma_client = chromadb.PersistentClient(
        path='data/vector_db',
        settings=Settings(anonymized_telemetry=False)
    )
    
    # Delete old collection if exists
    try:
        chroma_client.delete_collection("job_postings")
        print("🗑️ Đã xóa collection cũ")
    except:
        pass
    
    # Create new collection
    collection = chroma_client.create_collection(
        name="job_postings",
        metadata={"description": "Job posting embeddings"}
    )
    
    # Prepare data
    print("📝 Đang tạo embeddings...")
    documents = []
    metadatas = []
    ids = []
    
    for job in jobs:
        # Create text for embedding
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
        
        # ChromaDB không chấp nhận None values → Convert tất cả None thành default
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
    embeddings = embedding_model.encode(documents)
    
    # Add to ChromaDB
    print("💾 Đang lưu vào vector database...")
    collection.add(
        embeddings=embeddings.tolist(),
        documents=documents,
        metadatas=metadatas,
        ids=ids
    )
    
    print(f"✅ Đã train {len(jobs)} job postings!")
    
    db.disconnect()
    return True


def main():
    """Main training function"""
    print("="*60)
    print("🚀 HR CHATBOT TRAINING")
    print("="*60)
    
    # Train AI messages
    ai_success = train_ai_messages()
    
    # Train job postings
    job_success = train_job_postings()
    
    print("\n" + "="*60)
    if ai_success and job_success:
        print("✅ TRAINING HOÀN TẤT!")
    else:
        print("⚠️ TRAINING HOÀN TẤT VỚI MỘT SỐ LỖI")
    print("="*60)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⏹️ Đã dừng training!")
    except Exception as e:
        print(f"\n❌ Lỗi: {e}")
        import traceback
        traceback.print_exc()
