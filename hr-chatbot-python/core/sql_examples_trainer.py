"""
SQL Examples Trainer - Train vector database với SQL examples
Dùng RAG (Retrieval-Augmented Generation) để improve SQL generation
"""
import os

# Disable TensorFlow BEFORE importing sentence_transformers
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
import json


class SQLExamplesTrainer:
    """Train và manage SQL examples trong vector database"""
    
    def __init__(self, db_path='./data/sql_examples_db'):
        """Initialize trainer"""
        self.db_path = db_path
        self.embedding_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')
        
        # Initialize ChromaDB
        self.client = chromadb.PersistentClient(
            path=db_path,
            settings=Settings(anonymized_telemetry=False)
        )
        
        # Get or create collection
        try:
            self.collection = self.client.get_collection("sql_examples")
            count = self.collection.count()
            print(f"✅ Loaded existing SQL examples collection ({count} examples)")
            
            if count == 0:
                print(f"⚠️ Collection is empty! Please run: python train_sql_examples.py")
        except:
            self.collection = self.client.create_collection(
                name="sql_examples",
                metadata={"description": "SQL query examples for RAG"}
            )
            print(f"✅ Created new SQL examples collection (empty)")
            print(f"⚠️ Please run: python train_sql_examples.py")
    
    def add_examples(self, examples):
        """
        Add SQL examples to vector database
        
        Args:
            examples: List of dicts with 'question', 'sql', 'description'
        """
        if not examples:
            return
        
        ids = []
        questions = []
        embeddings = []
        metadatas = []
        
        for i, example in enumerate(examples):
            question = example['question']
            sql = example['sql']
            description = example.get('description', '')
            category = example.get('category', 'general')
            
            # Create embedding
            embedding = self.embedding_model.encode(question)
            
            ids.append(f"sql_example_{i}")
            questions.append(question)
            embeddings.append(embedding.tolist())
            metadatas.append({
                'question': question,
                'sql': sql,
                'description': description,
                'category': category
            })
        
        # Add to collection
        self.collection.add(
            ids=ids,
            embeddings=embeddings,
            documents=questions,
            metadatas=metadatas
        )
        
        print(f"✅ Added {len(examples)} SQL examples to vector database")
    
    def search_similar_examples(self, question, top_k=3):
        """
        Search for similar SQL examples
        
        Args:
            question: User's question
            top_k: Number of examples to return
            
        Returns:
            List of similar examples
        """
        # Create embedding
        query_embedding = self.embedding_model.encode(question)
        
        # Search
        results = self.collection.query(
            query_embeddings=[query_embedding.tolist()],
            n_results=top_k
        )
        
        examples = []
        if results['ids'] and results['ids'][0]:
            for i in range(len(results['ids'][0])):
                metadata = results['metadatas'][0][i]
                distance = results['distances'][0][i]
                
                # Convert distance to similarity score (0-100%)
                # ChromaDB uses squared L2 distance, so we need to normalize
                # Lower distance = higher similarity
                # Use: similarity = 1 / (1 + distance)
                # This maps: distance=0 → 100%, distance=1 → 50%, distance=10 → 9%
                similarity = 1.0 / (1.0 + distance)
                
                examples.append({
                    'question': metadata['question'],
                    'sql': metadata['sql'],
                    'description': metadata.get('description', ''),
                    'category': metadata.get('category', 'general'),
                    'similarity': similarity
                })
        
        return examples
    
    def get_all_examples(self):
        """Get all examples from database"""
        results = self.collection.get()
        
        examples = []
        if results['ids']:
            for i in range(len(results['ids'])):
                metadata = results['metadatas'][i]
                examples.append({
                    'question': metadata['question'],
                    'sql': metadata['sql'],
                    'description': metadata.get('description', ''),
                    'category': metadata.get('category', 'general')
                })
        
        return examples
    
    def clear_examples(self):
        """Clear all examples"""
        self.client.delete_collection("sql_examples")
        self.collection = self.client.create_collection(
            name="sql_examples",
            metadata={"description": "SQL query examples for RAG"}
        )
        print("✅ Cleared all SQL examples")


def get_default_sql_examples():
    """
    Load SQL examples from JSON file
    Returns list of example dictionaries
    """
    import json
    import os
    
    # Get path to training_sql.json
    current_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = os.path.join(current_dir, '..', 'data', 'training_sql.json')
    
    # Load from JSON file
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data['sql_examples']
    except FileNotFoundError:
        print(f"⚠️ File not found: {json_path}")
        print("❌ Please create data/training_sql.json file with SQL examples")
        return []



if __name__ == "__main__":
    # Test trainer
    print("🚀 Training SQL Examples Vector Database...")
    
    trainer = SQLExamplesTrainer()
    
    # Clear old data
    trainer.clear_examples()
    
    # Add default examples
    examples = get_default_sql_examples()
    trainer.add_examples(examples)
    
    print(f"\n✅ Training completed! Total examples: {len(examples)}")
    
    # Test search
    print("\n" + "="*80)
    print("🔍 Testing similarity search...")
    print("="*80)
    
    test_questions = [
        "Có bao nhiêu job ở HCM?",
        "Danh sách job backend developer",
        "Top 3 job lương cao",
        "Thống kê theo khu vực"
    ]
    
    for question in test_questions:
        print(f"\n❓ Question: {question}")
        similar = trainer.search_similar_examples(question, top_k=2)
        
        for i, ex in enumerate(similar, 1):
            print(f"\n  {i}. Similar question (similarity: {ex['similarity']:.2%})")
            print(f"     Q: {ex['question']}")
            print(f"     SQL: {ex['sql'][:100]}...")
