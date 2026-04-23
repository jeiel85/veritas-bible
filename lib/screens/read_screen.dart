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
  List<Verse> _verses1 = [];
  List<Verse> _verses2 = [];
  bool _isLoading = true;
  bool _isParallelMode = false;
  String _translation1 = 'KRV';
  String _translation2 = 'KJV';

  @override
  void initState() {
    super.initState();
    currentChapter = widget.initialChapter;
    _loadAllVerses();
  }

  Future<void> _loadAllVerses() async {
    setState(() => _isLoading = true);
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    
    final v1 = await bibleProvider.getVerses(widget.bookName, currentChapter, translation: _translation1);
    List<Verse> v2 = [];
    if (_isParallelMode) {
      v2 = await bibleProvider.getVerses(widget.bookName, currentChapter, translation: _translation2);
    }

    setState(() {
      _verses1 = v1;
      _verses2 = v2;
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
            icon: Icon(_isParallelMode ? Icons.view_agenda : Icons.view_column),
            tooltip: _isParallelMode ? '단일 모드' : '병행 모드',
            onPressed: () {
              setState(() => _isParallelMode = !_isParallelMode);
              _loadAllVerses();
            },
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.translate),
            onSelected: (val) {
              setState(() => _translation1 = val);
              _loadAllVerses();
            },
            itemBuilder: (context) => [
              const PopupMenuItem(value: 'KRV', child: Text('개역한글 (KRV)')),
              const PopupMenuItem(value: 'KJV', child: Text('King James (KJV)')),
            ],
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _isParallelMode 
              ? _buildParallelView(settingsProvider)
              : _buildSingleView(_verses1, settingsProvider),
      bottomNavigationBar: _buildBottomNav(),
    );
  }

  Widget _buildSingleView(List<Verse> verses, SettingsProvider settings) {
    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      itemCount: verses.length,
      itemBuilder: (context, index) => _buildVerseItem(verses[index], settings),
    );
  }

  Widget _buildParallelView(SettingsProvider settings) {
    return Row(
      children: [
        Expanded(child: _buildSingleView(_verses1, settings)),
        const VerticalDivider(width: 1),
        Expanded(child: _buildSingleView(_verses2, settings)),
      ],
    );
  }

  Widget _buildVerseItem(Verse verse, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 30,
            child: Text(
              '${verse.verse}',
              style: TextStyle(
                fontSize: settings.fontSize * 0.7,
                fontWeight: FontWeight.bold,
                color: Theme.of(context).colorScheme.primary,
              ),
            ),
          ),
          Expanded(
            child: Text(
              verse.text,
              style: TextStyle(fontSize: settings.fontSize, height: 1.6),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomNav() {
    return BottomAppBar(
      height: 60,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            IconButton(
              icon: const Icon(Icons.arrow_back_ios, size: 20),
              onPressed: currentChapter > 1 ? () {
                setState(() => currentChapter--);
                _loadAllVerses();
              } : null,
            ),
            Text(
              '$currentChapter / ${widget.maxChapter}', 
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)
            ),
            IconButton(
              icon: const Icon(Icons.arrow_forward_ios, size: 20),
              onPressed: currentChapter < widget.maxChapter ? () {
                setState(() => currentChapter++);
                _loadAllVerses();
              } : null,
            ),
          ],
        ),
      ),
    );
  }
}
