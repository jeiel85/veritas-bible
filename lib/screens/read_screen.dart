import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/bible.dart';
import '../providers/settings_provider.dart';

class ReadScreen extends StatefulWidget {
  final Book book;
  final int initialChapter;

  const ReadScreen({super.key, required this.book, required this.initialChapter});

  @override
  State<ReadScreen> createState() => _ReadScreenState();
}

class _ReadScreenState extends State<ReadScreen> {
  late int currentChapter;

  @override
  void initState() {
    super.initState();
    currentChapter = widget.initialChapter;
  }

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);
    
    // 현재 장(Chapter)에 해당하는 절(Verse) 필터링
    final chapterVerses = widget.book.verses.where((v) => v.chapter == currentChapter).toList();
    // 해당 권(Book)의 최대 장 수 계산
    final maxChapter = widget.book.verses.fold<int>(1, (max, v) => v.chapter > max ? v.chapter : max);

    return Scaffold(
      appBar: AppBar(
        title: Text('${widget.book.name} $currentChapter장'),
        actions: [
          IconButton(
            icon: const Icon(Icons.text_decrease),
            onPressed: () => settingsProvider.updateFontSize(settingsProvider.fontSize - 2),
            tooltip: '글꼴 작게',
          ),
          IconButton(
            icon: const Icon(Icons.text_increase),
            onPressed: () => settingsProvider.updateFontSize(settingsProvider.fontSize + 2),
            tooltip: '글꼴 크게',
          ),
        ],
      ),
      body: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
        itemCount: chapterVerses.length,
        itemBuilder: (context, index) {
          final verse = chapterVerses[index];
          return Padding(
            padding: const EdgeInsets.only(bottom: 12.0),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.only(right: 8.0, top: 4.0),
                  child: Text(
                    '${verse.verse}',
                    style: TextStyle(
                      fontSize: settingsProvider.fontSize * 0.7, // 장 번호는 살짝 작게
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    verse.text,
                    style: TextStyle(
                      fontSize: settingsProvider.fontSize,
                      height: 1.6, // 줄 간격 설정
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
      bottomNavigationBar: BottomAppBar(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              TextButton.icon(
                icon: const Icon(Icons.arrow_back_ios, size: 16),
                label: const Text('이전 장'),
                onPressed: currentChapter > 1
                    ? () => setState(() => currentChapter--)
                    : null,
              ),
              Text('$currentChapter / $maxChapter', style: const TextStyle(fontWeight: FontWeight.bold)),
              TextButton.icon(
                icon: const Icon(Icons.arrow_forward_ios, size: 16),
                label: const Text('다음 장'),
                onPressed: currentChapter < maxChapter
                    ? () => setState(() => currentChapter++)
                    : null,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
