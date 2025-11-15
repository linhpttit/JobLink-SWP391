"""
Train SQL Examples - Chạy script này để train vector database với SQL examples
"""
import os
import sys

# Disable TensorFlow BEFORE importing any libraries
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

from core.sql_examples_trainer import SQLExamplesTrainer, get_default_sql_examples


def main():
    print("="*80)
    print("🚀 TRAINING SQL EXAMPLES VECTOR DATABASE")
    print("="*80)
    
    # Initialize trainer
    trainer = SQLExamplesTrainer()
    
    # Clear old data
    print("\n🗑️  Clearing old examples...")
    trainer.clear_examples()
    
    # Get default examples
    examples = get_default_sql_examples()
    
    print(f"\n📚 Loading {len(examples)} SQL examples...")
    print(f"   - COUNT queries: {len([e for e in examples if e['category'] == 'count'])}")
    print(f"   - LIST queries: {len([e for e in examples if e['category'] == 'list'])}")
    print(f"   - TOP queries: {len([e for e in examples if e['category'] == 'top'])}")
    print(f"   - STATISTICS queries: {len([e for e in examples if e['category'] == 'statistics'])}")
    print(f"   - FILTER queries: {len([e for e in examples if e['category'] == 'filter'])}")
    print(f"   - COMBINED queries: {len([e for e in examples if e['category'] == 'combined'])}")
    
    # Add examples to vector database
    trainer.add_examples(examples)
    
    print(f"\n✅ Training completed!")
    print(f"📊 Total examples in database: {len(trainer.get_all_examples())}")
    
    # Test similarity search
    print("\n" + "="*80)
    print("🔍 TESTING SIMILARITY SEARCH")
    print("="*80)
    
    test_questions = [
        "Có bao nhiêu job ở HCM?",
        "Danh sách job backend developer",
        "Top 3 job lương cao",
        "Thống kê theo khu vực",
        "Việc làm frontend ở Hà Nội lương 25 triệu"
    ]
    
    for question in test_questions:
        print(f"\n❓ Question: {question}")
        similar = trainer.search_similar_examples(question, top_k=2)
        
        for i, ex in enumerate(similar, 1):
            print(f"\n  {i}. Similar (similarity: {ex['similarity']:.1%})")
            print(f"     Q: {ex['question']}")
            print(f"     SQL: {ex['sql'][:80]}...")
    
    print("\n" + "="*80)
    print("✅ DONE! SQL examples vector database is ready!")
    print("💡 Now AI SQL Generator will use RAG for better accuracy")
    print("="*80)


if __name__ == "__main__":
    main()
