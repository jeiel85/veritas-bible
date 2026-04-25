import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:flutter_tts/flutter_tts.dart';
import 'package:google_fonts/google_fonts.dart';
import '../models/bible.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'verse_card_screen.dart';

class ReadScreen extends StatefulWidget {
  final String bookName;
  final int initialChapter;
  final int maxChapter;
  final int? initialVerse;

  const ReadScreen({
    super.key,
    required this.bookName,
    required this.initialChapter,
    required this.maxChapter,
    this.initialVerse,
  });

  @override
  State<ReadScreen> createState() => _ReadScreenState();
}

class _ReadScreenState extends State<ReadScreen> {
  final ScrollController _scrollController = ScrollController();
  late int currentChapter;
  List<Verse> _verses1 = [];
  List<Verse> _verses2 = [];
  Map<int, String> _highlights = {};
  Set<int> _versesWithNotes = {}; // 메모가 있는 절 번호 집합
  bool _isLoading = true;
  bool _isParallelMode = false;
  bool _isFocusMode = false; // 포커스 모드 상태
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
    
    // 현재 장의 메모 목록 조회
    final notes = await bibleProvider.getAllNotes();
    final currentNotes = notes.where((n) => n['book_name'] == widget.bookName && n['chapter'] == currentChapter).map((n) => n['verse'] as int).toSet();

    List<Verse> v2 = [];
    if (_isParallelMode) {
      v2 = await bibleProvider.getVerses(widget.bookName, currentChapter, translation: _translation2);
    }

    setState(() {
      _verses1 = v1;
      _verses2 = v2;
      _highlights = h;
      _versesWithNotes = currentNotes;
      _isLoading = false;
    });

    // 특정 구절로 자동 스크롤
    if (widget.initialVerse != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _scrollToVerse(widget.initialVerse!);
      });
    }

    // 읽기 활동 기록
    bibleProvider.logReadActivity();
  }

  void _scrollToVerse(int verseNumber) {
    int index = _verses1.indexWhere((v) => v.verse == verseNumber);
    if (index != -1 && _scrollController.hasClients) {
      // 대략적인 구절 높이(80)를 기준으로 스크롤
      double offset = index * 85.0;
      _scrollController.animateTo(
        offset,
        duration: const Duration(milliseconds: 600),
        curve: Curves.easeOutQuart,
      );
    }
  }

  Future<void> _speak() async {
    final settings = Provider.of<SettingsProvider>(context, listen: false);
    if (_isPlaying) {
      await _flutterTts.stop();
      setState(() => _isPlaying = false);
    } else {
      await _flutterTts.setSpeechRate(settings.ttsSpeed);
      String fullText = _verses1.map((v) => "${v.verse}절. ${v.text}").join(" ");
      setState(() => _isPlaying = true);
      await _flutterTts.speak(fullText);
    }
  }

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: _isFocusMode ? null : AppBar(
        title: Text('${widget.bookName} $currentChapter장'),
        actions: [
          IconButton(
            icon: const Icon(Icons.fullscreen),
            tooltip: '포커스 모드',
            onPressed: () => setState(() => _isFocusMode = true),
          ),
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
      floatingActionButton: _isFocusMode
          ? FloatingActionButton(
              backgroundColor: Colors.black.withOpacity(0.5),
              elevation: 0,
              mini: true,
              child: const Icon(Icons.fullscreen_exit, color: Colors.white),
              onPressed: () => setState(() => _isFocusMode = false),
            )
          : null,
      floatingActionButtonLocation: FloatingActionButtonLocation.endFloat,
      bottomNavigationBar: _isFocusMode ? null : _buildBottomNav(),
    );
  }

  Widget _buildSingleView(List<Verse> verses, SettingsProvider settings) {
    return ListView.builder(
      controller: _scrollController,
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
    final hasNote = _versesWithNotes.contains(verse.verse);
    Color? bgColor;
    if (highlightColor != null) {
      bgColor = Color(int.parse(highlightColor, radix: 16)).withOpacity(0.3);
    }

    // 폰트 스타일 결정
    TextStyle textStyle = settings.fontFamily == 'Serif'
        ? GoogleFonts.nanumMyeongjo(fontSize: settings.fontSize, height: settings.lineHeight)
        : GoogleFonts.nanumGothic(fontSize: settings.fontSize, height: settings.lineHeight);

    return InkWell(
      onTap: () {
        HapticFeedback.lightImpact();
        _showVerseOptions(verse);
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 8.0),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 32,
              child: Text(
                '${verse.verse}',
                style: TextStyle(
                  fontSize: settings.fontSize * 0.7,
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.primary.withOpacity(0.7),
                ),
              ),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(verse.text, style: textStyle),
                  if (hasNote)
                    const Padding(
                      padding: EdgeInsets.only(top: 6.0),
                      child: Icon(Icons.note_alt, size: 14, color: Colors.blueGrey),
                    ),
                ],
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
      isScrollControlled: true, // 키보드 대응을 위해 추가
      builder: (context) {
        return FutureBuilder<bool>(
          future: bibleProvider.isBookmarked(widget.bookName, currentChapter, verse.verse),
          builder: (context, snapshot) {
            final isBookmarked = snapshot.data ?? false;
            return Padding(
              padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
              child: Container(
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
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.note_alt_outlined),
                      title: const Text('메모 남기기'),
                      onTap: () {
                        Navigator.pop(context);
                        _showNoteDialog(verse);
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.image_outlined),
                      title: const Text('말씀 카드 만들기'),
                      onTap: () {
                        Navigator.pop(context);
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => VerseCardScreen(verse: verse, bookName: widget.bookName),
                          ),
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
              ),
            );
          }
        );
      },
    );
  }

  void _showNoteDialog(Verse verse) async {
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    final existingNote = await bibleProvider.getNote(widget.bookName, currentChapter, verse.verse);
    final controller = TextEditingController(text: existingNote);

    if (!mounted) return;
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('${widget.bookName} ${currentChapter}:${verse.verse} 메모'),
        content: TextField(
          controller: controller,
          maxLines: 5,
          decoration: const InputDecoration(
            hintText: '이 구절에 대한 묵상을 기록하세요...',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
          ElevatedButton(
            onPressed: () async {
              await bibleProvider.saveNote(widget.bookName, currentChapter, verse.verse, controller.text);
              if (mounted) Navigator.pop(context);
              _loadAllVerses();
            },
            child: const Text('저장'),
          ),
        ],
      ),
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
