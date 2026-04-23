import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import 'verse_card_screen.dart';
import '../models/bible.dart';

class MoodBibleScreen extends StatefulWidget {
  const MoodBibleScreen({super.key});

  @override
  State<MoodBibleScreen> createState() => _MoodBibleScreenState();
}

class _MoodBibleScreenState extends State<MoodBibleScreen> {
  final Map<String, List<Map<String, dynamic>>> _moodData = {
    '기쁨': [
      {'book': '데살로니가전서', 'chapter': 5, 'verse': 16, 'text': '항상 기뻐하라'},
      {'book': '빌립보서', 'chapter': 4, 'verse': 4, 'text': '주 안에서 항상 기뻐하라 내가 다시 말하노니 기뻐하라'},
    ],
    '불안': [
      {'book': '빌립보서', 'chapter': 4, 'verse': 6, 'text': '아무 것도 염려하지 말고 오직 모든 일에 기도와 간구로, 너희 구할 것을 감사함으로 하나님께 아뢰라'},
      {'book': '베드로전서', 'chapter': 5, 'verse': 7, 'text': '너희 염려를 다 주께 맡겨 버리라 이는 저가 너희를 권고하심이니라'},
    ],
    '슬픔': [
      {'book': '시편', 'chapter': 34, 'verse': 18, 'text': '여호와는 마음이 상한 자에게 가까이 하시고 중심에 통회하는 자를 구원하시는도다'},
      {'book': '마태복음', 'chapter': 5, 'verse': 4, 'text': '애통하는 자는 복이 있나니 저희가 위로를 받을 것임이요'},
    ],
    '분노': [
      {'book': '에베소서', 'chapter': 4, 'verse': 26, 'text': '분을 내어도 죄를 짓지 말며 해가 지도록 분을 품지 말고'},
      {'book': '잠언', 'chapter': 15, 'verse': 1, 'text': '유순한 대답은 분노를 쉬게 하여도 과격한 말은 노를 격동하느니라'},
    ],
    '평안': [
      {'book': '요한복음', 'chapter': 14, 'verse': 27, 'text': '평안을 너희에게 끼치노니 곧 나의 평안을 너희에게 주노라 내가 너희에게 주는 것은 세상이 주는 것 같지 아니하니라 너희는 마음에 근심도 말고 두려워하지도 말라'},
      {'book': '시편', 'chapter': 23, 'verse': 1, 'text': '여호와는 나의 목자시니 내가 부족함이 없으리로다'},
    ],
  };

  String? _selectedMood;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('마음 챙김 말씀'),
      ),
      body: Column(
        children: [
          const Padding(
            padding: EdgeInsets.all(24.0),
            child: Text(
              '지금 기분이 어떠신가요?\n당신의 마음에 힘이 되는 말씀을 들려드릴게요.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
            ),
          ),
          SizedBox(
            height: 120,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              children: _moodData.keys.map((mood) => _buildMoodItem(mood)).toList(),
            ),
          ),
          const Divider(height: 40),
          Expanded(
            child: _selectedMood == null
                ? const Center(child: Text('위의 감정 아이콘을 선택해 보세요.'))
                : _buildVerseList(_moodData[_selectedMood!]!),
          ),
        ],
      ),
    );
  }

  Widget _buildMoodItem(String mood) {
    bool isSelected = _selectedMood == mood;
    IconData icon;
    Color color;

    switch (mood) {
      case '기쁨': icon = Icons.sentiment_very_satisfied; color = Colors.orange; break;
      case '불안': icon = Icons.sentiment_dissatisfied; color = Colors.blueGrey; break;
      case '슬픔': icon = Icons.sentiment_very_dissatisfied; color = Colors.blue; break;
      case '분노': icon = Icons.sentiment_neutral; color = Colors.red; break;
      case '평안': icon = Icons.sentiment_satisfied; color = Colors.green; break;
      default: icon = Icons.face; color = Colors.grey;
    }

    return GestureDetector(
      onTap: () => setState(() => _selectedMood = mood),
      child: Container(
        width: 80,
        margin: const EdgeInsets.only(right: 12),
        decoration: BoxDecoration(
          color: isSelected ? color.withOpacity(0.2) : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: isSelected ? color : Colors.grey.withOpacity(0.3)),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 40),
            const SizedBox(height: 8),
            Text(mood, style: TextStyle(fontWeight: isSelected ? FontWeight.bold : null)),
          ],
        ),
      ),
    );
  }

  Widget _buildVerseList(List<Map<String, dynamic>> verses) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: verses.length,
      itemBuilder: (context, index) {
        final v = verses[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 16),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  v['text'],
                  style: const TextStyle(fontSize: 18, height: 1.6),
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      '${v['book']} ${v['chapter']}:${v['verse']}',
                      style: const TextStyle(color: Colors.grey, fontStyle: FontStyle.italic),
                    ),
                    IconButton(
                      icon: const Icon(Icons.palette_outlined, size: 20, color: Colors.blue),
                      tooltip: '말씀 카드로 만들기',
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => VerseCardScreen(
                              verse: Verse(chapter: v['chapter'], verse: v['verse'], text: v['text']),
                              bookName: v['book'],
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
