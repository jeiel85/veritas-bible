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

class HomeScreen extends StatelessWidget {
...
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
        return Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          margin: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.primaryContainer.withOpacity(0.3),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            children: [
              Row(
                children: [
                  const Icon(Icons.local_fire_department, color: Colors.orange, size: 40),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '$streak일 연속 성경 읽기 중!',
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                        Text(
                          streak > 0 ? '영적 습관을 잘 유지하고 계시네요!' : '오늘의 말씀을 읽고 스트릭을 시작하세요.',
                          style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const PrayerTodoScreen()),
                        );
                      },
                      icon: const Icon(Icons.volunteer_activism, size: 16),
                      label: const Text('기도 할 일', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () {
                        // 활동 통계 화면 (추후 구현)
                      },
                      icon: const Icon(Icons.bar_chart, size: 16),
                      label: const Text('활동 통계', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
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
                    crossAxisCount: 6, // 더 많은 열을 배치하여 효율적으로 사용
                    mainAxisSpacing: 4,
                    crossAxisSpacing: 4,
                  ),
                  itemCount: book.chapterCount,
                  itemBuilder: (context, index) {
                    final chapter = index + 1;
                    return ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        padding: EdgeInsets.zero, // 버튼 내부 패딩 제거하여 공간 확보
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
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
                      child: FittedBox( // 텍스트가 버튼을 넘지 않도록 크기 자동 조절
                        fit: BoxFit.scaleDown,
                        child: Text(
                          '$chapter',
                          style: const TextStyle(fontWeight: FontWeight.bold),
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
  }
}
