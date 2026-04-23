import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import '../models/bible_metadata.dart';
import 'read_screen.dart';

class PersonalDataScreen extends StatelessWidget {
  const PersonalDataScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('나의 기록'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '북마크'),
              Tab(text: '하이라이트'),
              Tab(text: '메모'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _BookmarkList(),
            _HighlightList(),
            _NoteList(),
          ],
        ),
      ),
    );
  }
}

class _BookmarkList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);
    return FutureBuilder<List<Map<String, dynamic>>>(
      future: bibleProvider.getAllBookmarks(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
        final bookmarks = snapshot.data!;
        if (bookmarks.isEmpty) return const Center(child: Text('저장된 북마크가 없습니다.'));

        return ListView.builder(
          itemCount: bookmarks.length,
          itemBuilder: (context, index) {
            final b = bookmarks[index];
            return ListTile(
              title: Text('${b['book_name']} ${b['chapter']}:${b['verse']}'),
              subtitle: const Text('북마크된 구절로 이동'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 14),
              onTap: () => _navigateToRead(context, b),
            );
          },
        );
      },
    );
  }
}

class _HighlightList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);
    return FutureBuilder<List<Map<String, dynamic>>>(
      future: bibleProvider.getAllHighlights(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
        final highlights = snapshot.data!;
        if (highlights.isEmpty) return const Center(child: Text('저장된 하이라이트가 없습니다.'));

        return ListView.builder(
          itemCount: highlights.length,
          itemBuilder: (context, index) {
            final h = highlights[index];
            final color = Color(int.parse(h['color'], radix: 16)).withOpacity(0.5);
            return ListTile(
              leading: Container(width: 12, height: 12, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
              title: Text('${h['book_name']} ${h['chapter']}:${h['verse']}'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 14),
              onTap: () => _navigateToRead(context, h),
            );
          },
        );
      },
    );
  }
}

class _NoteList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);
    return FutureBuilder<List<Map<String, dynamic>>>(
      future: bibleProvider.getAllNotes(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
        final notes = snapshot.data!;
        if (notes.isEmpty) return const Center(child: Text('작성된 메모가 없습니다.'));

        return ListView.builder(
          itemCount: notes.length,
          itemBuilder: (context, index) {
            final n = notes[index];
            return ListTile(
              title: Text('${n['book_name']} ${n['chapter']}:${n['verse']}'),
              subtitle: Text(n['content'], maxLines: 1, overflow: TextOverflow.ellipsis),
              trailing: const Icon(Icons.arrow_forward_ios, size: 14),
              onTap: () => _navigateToRead(context, n),
            );
          },
        );
      },
    );
  }
}

void _navigateToRead(BuildContext context, Map<String, dynamic> data) {
  final bookInfo = allBibleBooks.firstWhere((b) => b.name == data['book_name']);
  Navigator.push(
    context,
    MaterialPageRoute(
      builder: (context) => ReadScreen(
        bookName: data['book_name'],
        initialChapter: data['chapter'],
        maxChapter: bookInfo.chapterCount,
      ),
    ),
  );
}
