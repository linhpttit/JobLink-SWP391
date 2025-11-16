"""
Test RAG-enabled Chatbot - Verify SQL examples vector database is working
"""
import os
import sys

# Disable TensorFlow
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.chatbot import HRChatbot


def test_rag_chatbot():
    print("="*80)
    print("🧪 TESTING RAG-ENABLED CHATBOT")
    print("="*80)
    
    # Initialize chatbot
    print("\n📦 Initializing chatbot...")
    try:
        chatbot = HRChatbot()
        print("\n✅ Chatbot initialized successfully!")
    except Exception as e:
        print(f"\n❌ Failed to initialize chatbot: {e}")
        return
    
    # Check if AI SQL Generator is available
    if not chatbot.ai_sql_executor:
        print("\n❌ AI SQL Generator not available!")
        print("💡 Please run: python train_sql_examples.py")
        return
    
    print("\n✅ AI SQL Generator is ready with RAG!")
    
    # Test queries
    print("\n" + "="*80)
    print("🔍 TESTING QUERIES")
    print("="*80)
    
    test_queries = [
        "Có bao nhiêu job đang tuyển?",
        "Danh sách job ở Hà Nội",
        "Top 5 job lương cao nhất",
        "Thống kê job theo địa điểm",
        "Việc làm developer lương trên 20 triệu"
    ]
    
    for i, query in enumerate(test_queries, 1):
        print(f"\n{'='*80}")
        print(f"Test {i}/{len(test_queries)}: {query}")
        print(f"{'='*80}")
        
        try:
            result = chatbot.chat(query)
            
            print(f"\n✅ Response Type: {result['type']}")
            
            if result['type'] == 'ai_sql_query':
                print(f"💻 SQL Generated: {result.get('sql', 'N/A')[:100]}...")
                print(f"📊 Query Type: {result.get('query_type', 'N/A')}")
            
            print(f"\n📝 Response:")
            print(result['response'][:300] + "..." if len(result['response']) > 300 else result['response'])
            
        except Exception as e:
            print(f"\n❌ Error: {e}")
    
    print("\n" + "="*80)
    print("✅ TESTING COMPLETED!")
    print("="*80)


def check_sql_examples_db():
    """Check if SQL examples database exists"""
    print("\n🔍 Checking SQL examples database...")
    
    try:
        from core.sql_examples_trainer import SQLExamplesTrainer
        
        trainer = SQLExamplesTrainer()
        examples = trainer.get_all_examples()
        
        print(f"✅ SQL examples database found!")
        print(f"📊 Total examples: {len(examples)}")
        
        # Count by category
        categories = {}
        for ex in examples:
            cat = ex.get('category', 'unknown')
            categories[cat] = categories.get(cat, 0) + 1
        
        print(f"\n📚 Examples by category:")
        for cat, count in categories.items():
            print(f"   - {cat}: {count}")
        
        return True
        
    except Exception as e:
        print(f"❌ SQL examples database not found: {e}")
        print(f"💡 Please run: python train_sql_examples.py")
        return False


if __name__ == "__main__":
    print("\n" + "="*80)
    print("🚀 RAG CHATBOT TEST SUITE")
    print("="*80)
    
    # Step 1: Check SQL examples database
    if not check_sql_examples_db():
        print("\n❌ Cannot proceed without SQL examples database")
        print("💡 Run: python train_sql_examples.py")
        sys.exit(1)
    
    # Step 2: Test chatbot
    test_rag_chatbot()
    
    print("\n" + "="*80)
    print("🎉 ALL TESTS COMPLETED!")
    print("="*80)
