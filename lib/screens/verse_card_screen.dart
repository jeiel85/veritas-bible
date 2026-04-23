import 'dart:io';
import 'package:flutter/material.dart';
import 'package:screenshot/screenshot.dart';
import 'package:share_plus/share_plus.dart';
import 'package:path_provider/path_provider.dart';
import '../models/bible.dart';

class VerseCardScreen extends StatefulWidget {
  final Verse verse;
  final String bookName;

  const VerseCardScreen({super.key, required this.verse, required this.bookName});

  @override
  State<VerseCardScreen> createState() => _VerseCardScreenState();
}

class _VerseCardScreenState extends State<VerseCardScreen> {
  final ScreenshotController _screenshotController = ScreenshotController();
  Color _backgroundColor = const Color(0xFF1A237E); // 기본 Deep Navy
  Color _textColor = Colors.white;

  final List<Color> _colors = [
    const Color(0xFF1A237E),
    const Color(0xFFAD1457),
    const Color(0xFF2E7D32),
    const Color(0xFFEF6C00),
    Colors.black,
    Colors.grey[800]!,
  ];

  Future<void> _shareCard() async {
    final image = await _screenshotController.capture();
    if (image == null) return;

    final directory = await getTemporaryDirectory();
    final imagePath = await File('${directory.path}/verse_card.png').create();
    await imagePath.writeAsBytes(image);

    await Share.shareXFiles([XFile(imagePath.path)], text: 'Veritas Bible에서 공유한 말씀 카드입니다.');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('말씀 카드 만들기'),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: _shareCard,
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: Screenshot(
                controller: _screenshotController,
                child: Container(
                  width: 300,
                  height: 400,
                  padding: const EdgeInsets.all(32),
                  decoration: BoxDecoration(
                    color: _backgroundColor,
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.2),
                        blurRadius: 10,
                        offset: const Offset(0, 5),
                      ),
                    ],
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.format_quote, color: Colors.white54, size: 40),
                      const SizedBox(height: 16),
                      Text(
                        widget.verse.text,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: _textColor,
                          fontSize: 20,
                          fontWeight: FontWeight.w500,
                          height: 1.6,
                        ),
                      ),
                      const SizedBox(height: 24),
                      Text(
                        '- ${widget.bookName} ${widget.verse.chapter}:${widget.verse.verse} -',
                        style: TextStyle(
                          color: _textColor.withOpacity(0.8),
                          fontSize: 14,
                          fontStyle: FontStyle.italic,
                        ),
                      ),
                      const SizedBox(height: 16),
                      const Text(
                        'Veritas Bible',
                        style: TextStyle(color: Colors.white24, fontSize: 10, letterSpacing: 2),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: Theme.of(context).cardColor,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('배경색 선택', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 16),
                SizedBox(
                  height: 50,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: _colors.length,
                    itemBuilder: (context, index) {
                      return GestureDetector(
                        onTap: () => setState(() => _backgroundColor = _colors[index]),
                        child: Container(
                          width: 50,
                          margin: const EdgeInsets.only(right: 12),
                          decoration: BoxDecoration(
                            color: _colors[index],
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: _backgroundColor == _colors[index] ? Colors.blue : Colors.transparent,
                              width: 3,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton.icon(
                    onPressed: _shareCard,
                    icon: const Icon(Icons.share),
                    label: const Text('이미지로 공유하기'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
