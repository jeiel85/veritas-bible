import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/bible_metadata.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'read_screen.dart';
import 'search_screen.dart';
import 'personal_data_screen.dart';
import 'reading_plan_screen.dart';
import 'settings_screen.dart';
import 'prayer_todo_screen.dart';
import 'mood_bible_screen.dart';
import 'achievement_screen.dart';

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
          title: const Text('Veritas Bible'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '구약성경'),
              Tab(text: '신약성경'),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.history),
              tooltip: '나의 기록',
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const PersonalDataScreen()),
                );
              },
            ),
            IconButton(
              icon: const Icon(Icons.assignment),
              tooltip: '통독 계획',
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ReadingPlanScreen()),
                );
              },
            ),
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
              icon: const Icon(Icons.settings),
              tooltip: '설정',
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SettingsScreen()),
                );
              },
            ),
          ],
        ),
        body: Column(
          children: [
            _buildStreakDashboard(bibleProvider),
            Expanded(
              child: bibleProvider.isLoading
                  ? const Center(child: CircularProgressIndicator())
                  : TabBarView(
                      children: [
                        _buildBookList(context, oldTestament),
                        _buildBookList(context, newTestament),
                      ],
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStreakDashboard(BibleProvider provider) {
    return FutureBuilder<int>(
      future: provider.getStreak(),
      builder: (context, snapshot) {
        final streak = snapshot.data ?? 0;
        final colorScheme = Theme.of(context).colorScheme;

        return Container(
          width: double.infinity,
          margin: const EdgeInsets.fromLTRB(16, 12, 16, 12),
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [colorScheme.primaryContainer.withOpacity(0.4), colorScheme.surface],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: colorScheme.outlineVariant.withOpacity(0.5)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.05),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.orange.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(Icons.local_fire_department, color: Colors.orange, size: 32),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '$streak일 연속 스트릭',
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                          ),
                          Text(
                            streak > 0 ? '영적 습관을 아주 잘 유지하고 계시네요!' : '오늘의 말씀을 읽고 첫 스트릭을 시작하세요.',
                            style: TextStyle(fontSize: 12, color: colorScheme.onSurfaceVariant),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                Row(
                  children: [
                    _buildDashButton(
                      context, 
                      Icons.volunteer_activism, 
                      '기도 할 일', 
                      () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PrayerTodoScreen()))
                    ),
                    const SizedBox(width: 10),
                    _buildDashButton(
                      context, 
                      Icons.favorite, 
                      '마음 챙김', 
                      () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MoodBibleScreen())),
                      iconColor: Colors.pink
                    ),
                    const SizedBox(width: 10),
                    _buildDashButton(
                      context, 
                      Icons.bar_chart, 
                      '성장 통계', 
                      () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AchievementScreen()))
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildDashButton(BuildContext context, IconData icon, String label, VoidCallback onTap, {Color? iconColor}) {
    return Expanded(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.4),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            children: [
              Icon(icon, size: 20, color: iconColor ?? Theme.of(context).colorScheme.primary),
              const SizedBox(height: 6),
              Text(label, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold)),
            ],
          ),
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
      isScrollControlled: true,
      builder: (context) {
        final colorScheme = Theme.of(context).colorScheme;
        final isDarkMode = Theme.of(context).brightness == Brightness.dark;

        return DraggableScrollableSheet(
          initialChildSize: 0.6,
          minChildSize: 0.3,
          maxChildSize: 0.9,
          expand: false,
          builder: (context, scrollController) {
            return Container(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  // 드래그 핸들
                  Container(
                    width: 40,
                    height: 4,
                    decoration: BoxDecoration(
                      color: colorScheme.outlineVariant,
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Icon(Icons.menu_book, color: colorScheme.primary, size: 24),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          '${book.name} - 장 선택',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: colorScheme.onSurface),
                        ),
                      ),
                      Text(
                        '${book.chapterCount}장',
                        style: TextStyle(fontSize: 14, color: colorScheme.onSurfaceVariant),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Expanded(
                    child: GridView.builder(
                      controller: scrollController,
                      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: _calculateCrossAxisCount(book.chapterCount),
                        mainAxisSpacing: 8,
                        crossAxisSpacing: 8,
                        childAspectRatio: 1.2,
                      ),
                      itemCount: book.chapterCount,
                      itemBuilder: (context, index) {
                        final chapter = index + 1;
                        // 밝은 배경과 어두운 텍스트로 다크모드 가시성 확보
                        final buttonBgColor = isDarkMode
                            ? colorScheme.surfaceContainerHighest
                            : colorScheme.primaryContainer;
                        final textColor = isDarkMode
                            ? colorScheme.onSurface
                            : colorScheme.onPrimaryContainer;

                        return Material(
                          color: buttonBgColor,
                          borderRadius: BorderRadius.circular(12),
                          child: InkWell(
                            onTap: () {
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
                            borderRadius: BorderRadius.circular(12),
                            child: Center(
                              child: Text(
                                '$chapter',
                                style: TextStyle(
                                  fontSize: chapter >= 100 ? 14 : 16,
                                  fontWeight: FontWeight.bold,
                                  color: textColor,
                                ),
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  int _calculateCrossAxisCount(int chapterCount) {
    if (chapterCount <= 10) return 5;
    if (chapterCount <= 50) return 6;
    if (chapterCount <= 100) return 8;
    return 10;
  }
}
