"""
FastAPI - API endpoints cho chatbot
"""
import os
import sys

# Disable TensorFlow - Chỉ dùng PyTorch
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from datetime import datetime
from core.chatbot import HRChatbot
from core.ai_trainer import AITrainer
from core.job_indexer import JobIndexer
import numpy as np

# Initialize FastAPI
app = FastAPI(
    title="HR Chatbot API",
    description="AI-powered HR chatbot",
    version="1.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global chatbot instance
chatbot = None


# Helper function to convert numpy types to Python types
def convert_to_json_serializable(obj):
    """Convert numpy types and other non-serializable types to JSON-safe types"""
    if isinstance(obj, dict):
        return {key: convert_to_json_serializable(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [convert_to_json_serializable(item) for item in obj]
    elif isinstance(obj, (np.integer, np.int64, np.int32)):
        return int(obj)
    elif isinstance(obj, (np.floating, np.float64, np.float32)):
        return float(obj)
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    else:
        return obj


# Models
class ChatMessage(BaseModel):
    message: str
    user_id: int = None


class TrainingData(BaseModel):
    tag: str
    patterns: list
    responses: list


class JobIndexRequest(BaseModel):
    job_id: int


# Startup
@app.on_event("startup")
async def startup():
    global chatbot
    print("🚀 Starting HR Chatbot API...")
    chatbot = HRChatbot()
    print("✅ API ready!")


# Endpoints
@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "HR Chatbot API",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "chat": "/api/chat",
            "train": "/api/train",
            "add_training": "/api/training/add"
        }
    }


@app.post("/api/chat")
async def chat(message: ChatMessage):
    """Chat với bot"""
    try:
        if not chatbot:
            raise HTTPException(status_code=503, detail="Chatbot chưa sẵn sàng")
        
        result = chatbot.chat(message.message)
        result['timestamp'] = datetime.now().isoformat()
        
        # Convert numpy types to JSON-serializable types
        result = convert_to_json_serializable(result)
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/train")
async def trigger_training():
    """Trigger training"""
    try:
        import subprocess
        
        # Run training script
        result = subprocess.run(
            [sys.executable, "train.py"],
            capture_output=True,
            text=True
        )
        
        return {
            "success": result.returncode == 0,
            "message": "Training completed" if result.returncode == 0 else "Training failed",
            "output": result.stdout,
            "timestamp": datetime.now().isoformat()
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/training/add")
async def add_training_data(data: TrainingData):
    """Thêm training data mới"""
    try:
        trainer = AITrainer()
        trainer.add_training_data(
            tag=data.tag,
            patterns=data.patterns,
            responses=data.responses
        )
        
        return {
            "success": True,
            "message": f"Đã thêm training data cho tag: {data.tag}",
            "note": "Chạy /api/train để cập nhật model",
            "timestamp": datetime.now().isoformat()
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/jobs/index")
async def index_job(request: JobIndexRequest):
    """Index job mới vào vector database"""
    try:
        indexer = JobIndexer()
        success = indexer.index_job(request.job_id)
        
        if success:
            return {
                "success": True,
                "message": f"Job {request.job_id} đã được index vào vector database",
                "timestamp": datetime.now().isoformat()
            }
        else:
            raise HTTPException(status_code=404, detail=f"Job {request.job_id} không tồn tại hoặc không active")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/jobs/index/{job_id}")
async def delete_job_index(job_id: int):
    """Xóa job khỏi vector database"""
    try:
        indexer = JobIndexer()
        success = indexer.delete_job(job_id)
        
        if success:
            return {
                "success": True,
                "message": f"Job {job_id} đã được xóa khỏi vector database",
                "timestamp": datetime.now().isoformat()
            }
        else:
            raise HTTPException(status_code=404, detail=f"Job {job_id} không tồn tại trong vector database")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/jobs/reindex")
async def reindex_all_jobs():
    """Re-index tất cả jobs (giống train.py)"""
    try:
        indexer = JobIndexer()
        success = indexer.reindex_all()
        
        if success:
            return {
                "success": True,
                "message": "Đã re-index tất cả jobs vào vector database",
                "timestamp": datetime.now().isoformat()
            }
        else:
            raise HTTPException(status_code=500, detail="Re-index thất bại")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/health")
async def health_check():
    """Health check"""
    return {
        "status": "healthy",
        "chatbot": "ready" if chatbot else "not initialized",
        "timestamp": datetime.now().isoformat()
    }


if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv('API_HOST', '0.0.0.0')
    port = int(os.getenv('API_PORT', 8000))
    
    print(f"🚀 Starting server on {host}:{port}")
    print(f"📚 Docs: http://{host}:{port}/docs")
    
    uvicorn.run(
        "api:app",
        host=host,
        port=port,
        reload=True
    )
