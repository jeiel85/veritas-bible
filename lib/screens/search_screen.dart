import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/bible_metadata.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'read_screen.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  List<Map<String, dynamic>> _searchResults = [];
  bool _isSearching = false;

  void _handleSearch(String query) async {
    if (query.length < 2) return;
    
    setState(() => _isSearching = true);
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    final results = await bibleProvider.search(query);
    setState(() {
      _searchResults = results;
      _isSearching = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: TextField(
          controller: _searchController,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: '검색어 입력 (최소 2자)',
            border: InputBorder.none,
          ),
          onSubmitted: _handleSearch,
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => _handleSearch(_searchController.text),
          ),
        ],
      ),
      body: _isSearching
          ? const Center(child: CircularProgressIndicator())
          : _searchResults.isEmpty
              ? const Center(child: Text('검색 결과가 없습니다.'))
              : ListView.builder(
                  itemCount: _searchResults.length,
                  itemBuilder: (context, index) {
                    final result = _searchResults[index];
                    return ListTile(
                      title: Text(
                        '${result['book_name']} ${result['chapter']}:${result['verse']}',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                      subtitle: Text(
                        result['text'],
                        style: TextStyle(fontSize: settingsProvider.fontSize * 0.9),
                      ),
                      onTap: () {
                        final bookInfo = allBibleBooks.firstWhere(
                          (b) => b.name == result['book_name'],
                          orElse: () => allBibleBooks[0],
                        );

                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => ReadScreen(
                              bookName: result['book_name'],
                              initialChapter: result['chapter'],
                              maxChapter: bookInfo.chapterCount,
                            ),
                          ),
                        );
                      },
                    );
                  },
                ),
    );
  }
}
