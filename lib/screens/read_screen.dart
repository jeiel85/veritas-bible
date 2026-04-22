import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/bible.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';

class ReadScreen extends StatefulWidget {
  final String bookName;
  final int initialChapter;
  final int maxChapter;

  const ReadScreen({
    super.key,
    required this.bookName,
    required this.initialChapter,
    required this.maxChapter,
  });

  @override
  State<ReadScreen> createState() => _ReadScreenState();
}

class _ReadScreenState extends State<ReadScreen> {
  late int currentChapter;
  List<Verse> _verses = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    currentChapter = widget.initialChapter;
    _loadVerses();
  }

  Future<void> _loadVerses() async {
    setState(() => _isLoading = true);
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    final verses = await bibleProvider.getVerses(widget.bookName, currentChapter);
    setState(() {
      _verses = verses;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: Text('${widget.bookName} $currentChapter장'),
        actions: [
          IconButton(
            icon: const Icon(Icons.text_decrease),
            onPressed: () => settingsProvider.updateFontSize(settingsProvider.fontSize - 2),
          ),
          IconButton(
            icon: const Icon(Icons.text_increase),
            onPressed: () => settingsProvider.updateFontSize(settingsProvider.fontSize + 2),
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _verses.isEmpty
              ? const Center(child: Text('해당 장의 구절 데이터가 없습니다.'))
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
                  itemCount: _verses.length,
                  itemBuilder: (context, index) {
                    final verse = _verses[index];
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
                                fontSize: settingsProvider.fontSize * 0.7,
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
                                height: 1.6,
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
                    ? () {
                        setState(() => currentChapter--);
                        _loadVerses();
                      }
                    : null,
              ),
              Text('$currentChapter / ${widget.maxChapter}', style: const TextStyle(fontWeight: FontWeight.bold)),
              TextButton.icon(
                icon: const Icon(Icons.arrow_forward_ios, size: 16),
                label: const Text('다음 장'),
                onPressed: currentChapter < widget.maxChapter
                    ? () {
                        setState(() => currentChapter++);
                        _loadVerses();
                      }
                    : null,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
