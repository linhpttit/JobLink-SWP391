"""
Run Script - Chạy chatbot interactive hoặc API
"""
import os
import sys

# Disable TensorFlow - Chỉ dùng PyTorch
os.environ['TRANSFORMERS_NO_TF'] = '1'
os.environ['USE_TORCH'] = '1'

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.chatbot import HRChatbot


def run_interactive():
    """Chạy chatbot interactive trong console"""
    print("="*60)
    print("🤖 HR CHATBOT - INTERACTIVE MODE")
    print("="*60)
    print("Gõ 'exit' hoặc 'quit' để thoát\n")
    
    chatbot = HRChatbot()
    
    while True:
        try:
            # Get user input
            user_input = input("\n👤 You: ").strip()
            
            if not user_input:
                continue
            
            # Check exit
            if user_input.lower() in ['exit', 'quit', 'thoát', 'bye']:
                print("\n👋 Goodbye!")
                break
            
            # Get response
            result = chatbot.chat(user_input)
            
            # Print response
            print(f"\n🤖 Bot: {result['response']}")
            
            # Show metadata
            if result.get('type') == 'trained':
                print(f"   [Trained response - {result['tag']} - {result['confidence']:.1%}]")
            elif result.get('type') == 'job_search':
                print(f"   [Job search - {len(result.get('jobs', []))} results]")
            
        except KeyboardInterrupt:
            print("\n\n👋 Goodbye!")
            break
        except Exception as e:
            print(f"\n❌ Error: {e}")


def run_api():
    """Chạy API server"""
    import uvicorn
    
    host = os.getenv('API_HOST', '0.0.0.0')
    port = int(os.getenv('API_PORT', 8000))
    
    print("="*60)
    print("🚀 HR CHATBOT - API MODE")
    print("="*60)
    print(f"Server: http://{host}:{port}")
    print(f"Docs: http://{host}:{port}/docs")
    print("="*60)
    
    uvicorn.run(
        "api:app",
        host=host,
        port=port,
        reload=True
    )


def main():
    """Main function"""
    if len(sys.argv) > 1 and sys.argv[1] == 'api':
        run_api()
    else:
        run_interactive()


if __name__ == "__main__":
    main()
