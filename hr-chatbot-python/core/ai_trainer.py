"""
AI Trainer - Train chatbot với training data
"""
import json
import os
import pickle
import numpy as np

# Disable TensorFlow - Chỉ dùng PyTorch
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'

from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


class AITrainer:
    """Train và lưu AI model từ training data"""
    
    def __init__(self, training_file='./data/training_data.json', model_file='./data/trained_model.pkl'):
        self.training_file = training_file
        self.model_file = model_file
        self.embedding_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')
        self.trained_data = None
        
    def train(self):
        """Train model từ training data"""
        print("🔄 Đang train AI model...")
        
        # Load training data
        with open(self.training_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        conversations = data['conversations']
        
        # Prepare training data
        training_patterns = []
        training_tags = []
        training_responses = []
        
        for conv in conversations:
            tag = conv['tag']
            patterns = conv['patterns']
            responses = conv['responses']
            
            for pattern in patterns:
                training_patterns.append(pattern)
                training_tags.append(tag)
                training_responses.append(responses)
        
        # Create embeddings
        print(f"📊 Creating embeddings cho {len(training_patterns)} patterns...")
        embeddings = self.embedding_model.encode(training_patterns)
        
        # Save trained data
        self.trained_data = {
            'patterns': training_patterns,
            'tags': training_tags,
            'responses': training_responses,
            'embeddings': embeddings,
            'conversations': conversations
        }
        
        # Save to file
        os.makedirs(os.path.dirname(self.model_file), exist_ok=True)
        with open(self.model_file, 'wb') as f:
            pickle.dump(self.trained_data, f)
        
        print(f"✅ Training hoàn tất! Đã lưu vào {self.model_file}")
        print(f"📈 Tổng số patterns: {len(training_patterns)}")
        print(f"📈 Tổng số tags: {len(set(training_tags))}")
        
        return True
    
    def load_trained_model(self):
        """Load trained model"""
        if not os.path.exists(self.model_file):
            print("⚠️ Chưa có trained model. Chạy training trước!")
            return False
        
        with open(self.model_file, 'rb') as f:
            self.trained_data = pickle.load(f)
        
        print(f"✅ Đã load trained model với {len(self.trained_data['patterns'])} patterns")
        return True
    
    def predict(self, message, threshold=0.5):
        """
        Predict response cho message
        
        Args:
            message: Tin nhắn từ user
            threshold: Ngưỡng similarity (0-1)
            
        Returns:
            Dict chứa tag, response, confidence
        """
        if not self.trained_data:
            if not self.load_trained_model():
                return None
        
        # Create embedding cho message
        message_embedding = self.embedding_model.encode([message])[0]
        
        # Tính similarity với tất cả patterns
        similarities = cosine_similarity(
            [message_embedding],
            self.trained_data['embeddings']
        )[0]
        
        # Lấy best match
        best_idx = np.argmax(similarities)
        best_score = similarities[best_idx]
        
        if best_score < threshold:
            return {
                'tag': 'unknown',
                'response': None,
                'confidence': best_score,
                'matched_pattern': None
            }
        
        # Get response
        tag = self.trained_data['tags'][best_idx]
        responses = self.trained_data['responses'][best_idx]
        matched_pattern = self.trained_data['patterns'][best_idx]
        
        # Random response
        import random
        response = random.choice(responses)
        
        return {
            'tag': tag,
            'response': response,
            'confidence': best_score,
            'matched_pattern': matched_pattern
        }
    
    def add_training_data(self, tag, patterns, responses):
        """
        Thêm training data mới
        
        Args:
            tag: Tag của conversation
            patterns: List các patterns
            responses: List các responses
        """
        # Load current data
        with open(self.training_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # Add new conversation
        data['conversations'].append({
            'tag': tag,
            'patterns': patterns,
            'responses': responses
        })
        
        # Save
        with open(self.training_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Đã thêm training data cho tag: {tag}")
        print("⚠️ Chạy lại training để cập nhật model!")


if __name__ == "__main__":
    # Test training
    trainer = AITrainer()
    
    print("="*60)
    print("AI TRAINER TEST")
    print("="*60)
    
    # Train
    trainer.train()
    
    # Test predict
    print("\n" + "="*60)
    print("TESTING PREDICTIONS")
    print("="*60)
    
    test_messages = [
        "xin chào",
        "hello",
        "tìm việc làm",
        "cảm ơn",
        "tạm biệt",
        "yêu cầu gì",
        "random message không có trong training"
    ]
    
    for msg in test_messages:
        print(f"\n📨 Message: {msg}")
        result = trainer.predict(msg)
        if result:
            print(f"🎯 Tag: {result['tag']}")
            print(f"📊 Confidence: {result['confidence']:.2%}")
            print(f"🔗 Matched: {result['matched_pattern']}")
            if result['response']:
                print(f"💬 Response: {result['response']}")
