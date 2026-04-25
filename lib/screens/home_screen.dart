import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../models/bible_metadata.dart';
import '../providers/bible_provider.dart';
import 'read_screen.dart';
import 'search_screen.dart';
import 'personal_data_screen.dart';
import 'reading_plan_screen.dart';
import 'settings_screen.dart';
import 'prayer_todo_screen.dart';
import 'mood_bible_screen.dart';
import 'achievement_screen.dart';
import 'spirit_dashboard_screen.dart';
import 'bible_atlas_screen.dart';
import 'community_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Veritas Bible'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '성경'),
              Tab(text: '훈련'),
            ],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.search),
              tooltip: '검색',
              onPressed: () {
                HapticFeedback.lightImpact();
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
                HapticFeedback.lightImpact();
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SettingsScreen()),
                );
              },
            ),
          ],
        ),
        body: bibleProvider.isLoading
            ? const Center(child: CircularProgressIndicator())
            : TabBarView(
                children: [
                  _buildBibleTab(context),
                  _buildTrainingTab(context, bibleProvider),
                ],
              ),
      ),
    );
  }

  // 성경 탭: 창세기~계시록 전체 나열
  Widget _buildBibleTab(BuildContext context) {
    return ListView.builder(
      itemCount: allBibleBooks.length,
      itemBuilder: (context, index) {
        final book = allBibleBooks[index];
        final isFirstNewTestament = !book.isOldTestament &&
            (index == 0 || allBibleBooks[index - 1].isOldTestament);

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (index == 0)
              _buildSectionHeader('구약성경', '39권'),
            if (isFirstNewTestament)
              _buildSectionHeader('신약성경', '27권'),
            ListTile(
              leading: CircleAvatar(
                backgroundColor: book.isOldTestament
                    ? Theme.of(context).colorScheme.primaryContainer
                    : Theme.of(context).colorScheme.secondaryContainer,
                child: Text(
                  '${book.id}',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: book.isOldTestament
                        ? Theme.of(context).colorScheme.onPrimaryContainer
                        : Theme.of(context).colorScheme.onSecondaryContainer,
                  ),
                ),
              ),
              title: Text(book.name, style: const TextStyle(fontWeight: FontWeight.w500)),
              subtitle: Text('전체 ${book.chapterCount}장'),
              trailing: const Icon(Icons.arrow_forward_ios, size: 14),
              onTap: () => _showChapterPicker(context, book),
            ),
          ],
        );
      },
    );
  }

  Widget _buildSectionHeader(String title, String subtitle) {
    return Builder(builder: (context) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
        color: Theme.of(context).colorScheme.surfaceContainerHighest.withOpacity(0.5),
        child: Row(
          children: [
            Text(
              title,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.bold,
                color: Theme.of(context).colorScheme.primary,
                letterSpacing: 0.5,
              ),
            ),
            const SizedBox(width: 8),
            Text(
              subtitle,
              style: TextStyle(
                fontSize: 12,
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      );
    });
  }

  // 훈련 탭: 모든 특별 기능
  Widget _buildTrainingTab(BuildContext context, BibleProvider provider) {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildStreakDashboard(context, provider),
          _buildQTSection(context, provider),
          const SizedBox(height: 8),
          _buildFeatureGrid(context),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildStreakDashboard(BuildContext context, BibleProvider provider) {
    return FutureBuilder<int>(
      future: provider.getStreak(),
      builder: (context, snapshot) {
        final streak = snapshot.data ?? 0;
        final colorScheme = Theme.of(context).colorScheme;

        return Container(
          width: double.infinity,
          margin: const EdgeInsets.fromLTRB(16, 16, 16, 8),
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
            child: Row(
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
                        streak > 0
                            ? '영적 습관을 아주 잘 유지하고 계시네요!'
                            : '오늘의 말씀을 읽고 첫 스트릭을 시작하세요.',
                        style: TextStyle(fontSize: 12, color: colorScheme.onSurfaceVariant),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildQTSection(BuildContext context, BibleProvider provider) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(20, 12, 20, 8),
          child: Text(
            '오늘의 QT',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
        ),
        SizedBox(
          height: 120,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            children: [
              _buildQTCard(context, '🌞 아침 묵상', provider.morningQT, Colors.amber.shade100, Colors.orange.shade900),
              const SizedBox(width: 12),
              _buildQTCard(context, '🌙 저녁 묵상', provider.eveningQT, Colors.indigo.shade100, Colors.indigo.shade900),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildQTCard(BuildContext context, String title, Map<String, dynamic>? qt, Color bgColor, Color textColor) {
    return InkWell(
      onTap: qt == null
          ? null
          : () {
              HapticFeedback.lightImpact();
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ReadScreen(
                    bookName: qt['book_name'],
                    initialChapter: qt['chapter'],
                    maxChapter: 150,
                  ),
                ),
              );
            },
      child: Container(
        width: 200,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(title, style: TextStyle(fontWeight: FontWeight.bold, color: textColor, fontSize: 14)),
            const SizedBox(height: 8),
            Text(
              qt != null ? '${qt['book_name']} ${qt['chapter']}:${qt['verse']}' : '로딩 중...',
              style: TextStyle(color: textColor.withOpacity(0.8), fontSize: 12),
            ),
            const SizedBox(height: 4),
            Text(
              qt != null ? qt['text'] : '',
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(color: textColor.withOpacity(0.7), fontSize: 11, fontStyle: FontStyle.italic),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFeatureGrid(BuildContext context) {
    final features = [
      _FeatureItem(Icons.volunteer_activism, '기도 할 일', Colors.pink, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PrayerTodoScreen()))),
      _FeatureItem(Icons.favorite, '마음 챙김', Colors.red, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MoodBibleScreen()))),
      _FeatureItem(Icons.bar_chart, '성장 통계', Colors.blue, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AchievementScreen()))),
      _FeatureItem(Icons.map_outlined, '성경 지도', Colors.teal, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const BibleAtlasScreen()))),
      _FeatureItem(Icons.people, '신앙 공동체', Colors.deepPurple, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CommunityScreen()))),
      _FeatureItem(Icons.calendar_month, '영성 대시보드', Colors.orange, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SpiritDashboardScreen()))),
      _FeatureItem(Icons.history, '나의 기록', Colors.brown, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PersonalDataScreen()))),
      _FeatureItem(Icons.assignment, '통독 계획', Colors.green, () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ReadingPlanScreen()))),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(20, 12, 20, 12),
          child: Text('영적 훈련', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        ),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 16),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4,
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 0.9,
          ),
          itemCount: features.length,
          itemBuilder: (context, index) {
            final f = features[index];
            return InkWell(
              onTap: () {
                HapticFeedback.lightImpact();
                f.onTap();
              },
              borderRadius: BorderRadius.circular(16),
              child: Container(
                decoration: BoxDecoration(
                  color: f.color.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: f.color.withOpacity(0.2)),
                ),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(f.icon, size: 28, color: f.color),
                    const SizedBox(height: 8),
                    Text(
                      f.label,
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ],
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
                      Text('${book.chapterCount}장', style: TextStyle(fontSize: 14, color: colorScheme.onSurfaceVariant)),
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

class _FeatureItem {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _FeatureItem(this.icon, this.label, this.color, this.onTap);
}
