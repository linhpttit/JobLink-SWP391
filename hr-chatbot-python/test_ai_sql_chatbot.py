"""
Test AI SQL Chatbot - Demo tính năng Text-to-SQL với LLM
"""
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.chatbot import HRChatbot


def test_chatbot():
    """Test chatbot với AI SQL Generator"""
    
    print("="*80)
    print("🤖 TEST AI SQL CHATBOT")
    print("="*80)
    print()
    
    # Initialize chatbot
    print("Đang khởi tạo chatbot...")
    chatbot = HRChatbot()
    
    print("\n" + "="*80)
    print("✅ CHATBOT READY!")
    print("="*80)
    print()
    
    # Test questions
    test_questions = [
        # AI SQL queries (sẽ dùng LLM)
        "Danh sách việc làm ở Hà Nội",
        "Top 5 job lương cao nhất",
        "Thống kê job theo địa điểm",
        "Có bao nhiêu job đang tuyển?",
        
        # Normal queries (dùng vector search)
        "Tìm việc frontend developer",
        "Việc làm developer lương 20 triệu",
        
        # Trained responses
        "Xin chào",
        "Cảm ơn"
    ]
    
    for i, question in enumerate(test_questions, 1):
        print(f"\n{'='*80}")
        print(f"❓ Test {i}/{len(test_questions)}: {question}")
        print(f"{'='*80}")
        
        try:
            result = chatbot.chat(question)
            
            print(f"\n📊 Type: {result['type']}")
            
            if result['type'] == 'ai_sql_query':
                print(f"🤖 AI SQL Query!")
                print(f"💻 SQL: {result.get('sql', 'N/A')[:100]}...")
            
            print(f"\n🤖 Response:")
            print(result['response'])
            
        except Exception as e:
            print(f"❌ Error: {e}")
            import traceback
            traceback.print_exc()
    
    print("\n" + "="*80)
    print("✅ TEST COMPLETED!")
    print("="*80)


def interactive_mode():
    """Interactive chat mode"""
    print("="*80)
    print("🤖 AI SQL CHATBOT - INTERACTIVE MODE")
    print("="*80)
    print()
    print("💡 Tips:")
    print("  • Hỏi 'danh sách job ở Hà Nội' để test AI SQL")
    print("  • Hỏi 'top 5 job lương cao' để test AI SQL")
    print("  • Hỏi 'tìm việc developer' để test vector search")
    print("  • Gõ 'exit' để thoát")
    print()
    
    chatbot = HRChatbot()
    
    print("\n" + "="*80)
    print("✅ CHATBOT READY! Start chatting...")
    print("="*80 + "\n")
    
    while True:
        try:
            # Get user input
            user_input = input("👤 You: ").strip()
            
            if not user_input:
                continue
            
            if user_input.lower() in ['exit', 'quit', 'bye', 'thoát']:
                print("\n👋 Goodbye!")
                break
            
            # Chat
            result = chatbot.chat(user_input)
            
            # Show type
            type_emoji = {
                'ai_sql_query': '🤖',
                'job_search': '🔍',
                'trained': '💬',
                'statistics': '📊',
                'fallback': '❓'
            }
            emoji = type_emoji.get(result['type'], '🤖')
            
            print(f"\n{emoji} Bot ({result['type']}):")
            
            # Show SQL if AI SQL query
            if result['type'] == 'ai_sql_query' and result.get('sql'):
                print(f"💻 SQL: {result['sql'][:80]}...")
                print()
            
            # Show response
            print(result['response'])
            print()
            
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as e:
            print(f"\n❌ Error: {e}")
            import traceback
            traceback.print_exc()
            print()


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Test AI SQL Chatbot')
    parser.add_argument('--mode', choices=['test', 'interactive'], default='interactive',
                       help='Test mode: test (auto) or interactive (manual)')
    
    args = parser.parse_args()
    
    if args.mode == 'test':
        test_chatbot()
    else:
        interactive_mode()
