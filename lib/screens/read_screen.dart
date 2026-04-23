import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:flutter_tts/flutter_tts.dart';
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
  Map<int, String> _highlights = {};
  bool _isLoading = true;
  bool _isParallelMode = false;
  String _translation1 = 'KRV';
  String _translation2 = 'KJV';

  // TTS 관련
  final FlutterTts _flutterTts = FlutterTts();
  bool _isPlaying = false;

  @override
  void initState() {
    super.initState();
    currentChapter = widget.initialChapter;
    _initTts();
    _loadAllVerses();
  }

  void _initTts() async {
    await _flutterTts.setLanguage("ko-KR");
    await _flutterTts.setSpeechRate(0.5);
    await _flutterTts.setVolume(1.0);
    await _flutterTts.setPitch(1.0);

    _flutterTts.setCompletionHandler(() {
      setState(() => _isPlaying = false);
    });
  }

  @override
  void dispose() {
    _flutterTts.stop();
    super.dispose();
  }

  Future<void> _loadAllVerses() async {
    setState(() => _isLoading = true);
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    
    final v1 = await bibleProvider.getVerses(widget.bookName, currentChapter, translation: _translation1);
    final h = await bibleProvider.getHighlights(widget.bookName, currentChapter);
    
    List<Verse> v2 = [];
    if (_isParallelMode) {
      v2 = await bibleProvider.getVerses(widget.bookName, currentChapter, translation: _translation2);
    }

    setState(() {
      _verses1 = v1;
      _verses2 = v2;
      _highlights = h;
      _isLoading = false;
    });
  }

  Future<void> _speak() async {
    if (_isPlaying) {
      await _flutterTts.stop();
      setState(() => _isPlaying = false);
    } else {
      String fullText = _verses1.map((v) => "${v.verse}절. ${v.text}").join(" ");
      setState(() => _isPlaying = true);
      await _flutterTts.speak(fullText);
    }
  }

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: Text('${widget.bookName} $currentChapter장'),
        actions: [
          IconButton(
            icon: Icon(_isPlaying ? Icons.stop_circle : Icons.play_circle_fill),
            color: _isPlaying ? Colors.red : null,
            tooltip: '본문 낭독',
            onPressed: _speak,
          ),
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
    final highlightColor = _highlights[verse.verse];
    Color? bgColor;
    if (highlightColor != null) {
      bgColor = Color(int.parse(highlightColor, radix: 16)).withOpacity(0.3);
    }

    return InkWell(
      onTap: () => _showVerseOptions(verse),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 6.0, horizontal: 4.0),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(4),
        ),
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
      ),
    );
  }

  void _showVerseOptions(Verse verse) {
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return FutureBuilder<bool>(
          future: bibleProvider.isBookmarked(widget.bookName, currentChapter, verse.verse),
          builder: (context, snapshot) {
            final isBookmarked = snapshot.data ?? false;
            return Container(
              padding: const EdgeInsets.all(16),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('${widget.bookName} ${currentChapter}:${verse.verse}', 
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const Divider(),
                  ListTile(
                    leading: const Icon(Icons.copy),
                    title: const Text('복사하기'),
                    onTap: () {
                      Clipboard.setData(ClipboardData(text: '${widget.bookName} ${currentChapter}:${verse.verse} ${verse.text}'));
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('클립보드에 복사되었습니다.')));
                    },
                  ),
                  ListTile(
                    leading: Icon(isBookmarked ? Icons.bookmark : Icons.bookmark_border),
                    title: Text(isBookmarked ? '북마크 해제' : '북마크 추가'),
                    onTap: () async {
                      await bibleProvider.toggleBookmark(widget.bookName, currentChapter, verse.verse);
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text(isBookmarked ? '북마크가 해제되었습니다.' : '북마크에 추가되었습니다.'))
                      );
                    },
                  ),
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 8.0),
                    child: Text('하이라이트 색상 선택', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      _colorOption(verse, 'FFFFEB3B'), // 노랑
                      _colorOption(verse, 'FF81C784'), // 초록
                      _colorOption(verse, 'FF64B5F6'), // 파랑
                      _colorOption(verse, 'FFF06292'), // 핑크
                      _colorOption(verse, '00000000', icon: Icons.format_color_reset), // 초기화
                    ],
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            );
          }
        );
      },
    );
  }

  Widget _colorOption(Verse verse, String hexColor, {IconData? icon}) {
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    return InkWell(
      onTap: () {
        bibleProvider.saveHighlight(widget.bookName, currentChapter, verse.verse, hexColor);
        Navigator.pop(context);
        _loadAllVerses();
      },
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: hexColor == '00000000' ? Colors.grey[200] : Color(int.parse(hexColor, radix: 16)),
          shape: BoxShape.circle,
          border: Border.all(color: Colors.grey.withOpacity(0.5)),
        ),
        child: icon != null ? Icon(icon, size: 20) : null,
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
