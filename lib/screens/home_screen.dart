import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'read_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);
    final settingsProvider = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Open Bible'),
        actions: [
          IconButton(
            icon: Icon(settingsProvider.isDarkMode ? Icons.light_mode : Icons.dark_mode),
            onPressed: () {
              settingsProvider.toggleTheme();
            },
          )
        ],
      ),
      body: bibleProvider.isLoading
          ? const Center(child: CircularProgressIndicator())
          : bibleProvider.bible == null
              ? const Center(child: Text('성경 데이터를 불러오지 못했습니다.'))
              : ListView.builder(
                  itemCount: bibleProvider.bible!.books.length,
                  itemBuilder: (context, index) {
                    final book = bibleProvider.bible!.books[index];
                    return ListTile(
                      title: Text(book.name, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                      trailing: const Icon(Icons.arrow_forward_ios),
                      onTap: () {
                        // 성경 책을 선택하면 1장부터 읽기 화면으로 넘어감
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => ReadScreen(book: book, initialChapter: 1),
                          ),
                        );
                      },
                    );
                  },
                ),
    );
  }
}
