import 'package:flutter/material.dart';

class AiMeditationScreen extends StatefulWidget {
  final String reference;
  final String verseText;

  const AiMeditationScreen({super.key, required this.reference, required this.verseText});

  @override
  State<AiMeditationScreen> createState() => _AiMeditationScreenState();
}

class _AiMeditationScreenState extends State<AiMeditationScreen> {
  final TextEditingController _controller = TextEditingController();
  final List<Map<String, String>> _messages = [];
  bool _isTyping = false;

  @override
  void initState() {
    super.initState();
    _messages.add({
      'role': 'ai',
      'content': '안녕하세요! ${widget.reference} 말씀을 함께 묵상해봐요. 이 말씀에서 어떤 점이 궁금하신가요?'
    });
  }

  void _sendMessage() async {
    if (_controller.text.isEmpty) return;

    setState(() {
      _messages.add({'role': 'user', 'content': _controller.text});
      _isTyping = true;
    });

    String userQuery = _controller.text;
    _controller.clear();

    // AI 응답 시뮬레이션
    await Future.delayed(const Duration(seconds: 2));

    String aiResponse = "";
    if (userQuery.contains('의미')) {
      aiResponse = "${widget.reference}의 의미는 하나님의 변치 않는 약속을 상징합니다. 오늘 하루 이 말씀을 붙들고 승리하세요!";
    } else if (userQuery.contains('위로')) {
      aiResponse = "네, 주님은 항상 곁에서 위로하고 계십니다. '${widget.verseText}' 말씀을 다시 한 번 천천히 읽어보세요.";
    } else {
      aiResponse = "말씀에 대해 깊이 고민하시는 모습이 아름답습니다. 더 구체적으로 어떤 은혜를 나누고 싶으신가요?";
    }

    if (mounted) {
      setState(() {
        _messages.add({'role': 'ai', 'content': aiResponse});
        _isTyping = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('AI 묵상 어시스턴트'),
        backgroundColor: Colors.indigo.shade50,
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                final msg = _messages[index];
                bool isAi = msg['role'] == 'ai';
                return Align(
                  alignment: isAi ? Alignment.centerLeft : Alignment.centerRight,
                  child: Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: isAi ? Colors.grey.shade200 : Colors.indigo.shade100,
                      borderRadius: BorderRadius.circular(12).copyWith(
                        bottomLeft: isAi ? Radius.zero : const Radius.circular(12),
                        bottomRight: isAi ? const Radius.circular(12) : Radius.zero,
                      ),
                    ),
                    constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.7),
                    child: Text(msg['content']!, style: const TextStyle(fontSize: 14)),
                  ),
                );
              },
            ),
          ),
          if (_isTyping)
            const Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('AI가 묵상 중입니다...', style: TextStyle(fontSize: 12, color: Colors.grey)),
            ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    decoration: const InputDecoration(
                      hintText: '궁금한 점을 물어보세요...',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  onPressed: _sendMessage,
                  icon: const Icon(Icons.send, color: Colors.indigo),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
