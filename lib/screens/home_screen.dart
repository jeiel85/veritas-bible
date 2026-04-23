import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/bible_metadata.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'read_screen.dart';
import 'search_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);
    final settingsProvider = Provider.of<SettingsProvider>(context);

    // 구약과 신약 리스트 분리
    final oldTestament = allBibleBooks.where((b) => b.isOldTestament).toList();
    final newTestament = allBibleBooks.where((b) => !b.isOldTestament).toList();

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Lumina Bible'),          bottom: const TabBar(
            tabs: [
              Tab(text: '구약성경'),
              Tab(text: '신약성경'),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.search),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SearchScreen()),
                );
              },
            ),
            IconButton(
              icon: Icon(settingsProvider.isDarkMode ? Icons.light_mode : Icons.dark_mode),
              onPressed: () => settingsProvider.toggleTheme(),
            ),
          ],
        ),
        body: bibleProvider.isLoading
            ? const Center(child: CircularProgressIndicator())
            : TabBarView(
                children: [
                  _buildBookList(context, oldTestament),
                  _buildBookList(context, newTestament),
                ],
              ),
      ),
    );
  }

  Widget _buildBookList(BuildContext context, List<BibleBookInfo> books) {
    return ListView.builder(
      itemCount: books.length,
      itemBuilder: (context, index) {
        final book = books[index];
        return ListTile(
          title: Text(book.name),
          subtitle: Text('전체 ${book.chapterCount}장'),
          trailing: const Icon(Icons.arrow_forward_ios, size: 14),
          onTap: () {
            // 장 선택 다이얼로그
            _showChapterPicker(context, book);
          },
        );
      },
    );
  }

  void _showChapterPicker(BuildContext context, BibleBookInfo book) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('${book.name} - 장 선택', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              Expanded(
                child: GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 5,
                    mainAxisSpacing: 8,
                    crossAxisSpacing: 8,
                  ),
                  itemCount: book.chapterCount,
                  itemBuilder: (context, index) {
                    final chapter = index + 1;
                    return ElevatedButton(
                      onPressed: () {
                        Navigator.pop(context);
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => ReadScreen(
                              bookName: book.name,
                              initialChapter: chapter,
                              maxChapter: book.chapterCount,
                            ),
                          ),
                        );
                      },
                      child: Text('$chapter'),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
