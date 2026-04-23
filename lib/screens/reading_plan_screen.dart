import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import '../models/bible_metadata.dart';
import 'read_screen.dart';

class ReadingPlanScreen extends StatelessWidget {
  const ReadingPlanScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('통독 계획'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_task),
            tooltip: '새 계획 생성',
            onPressed: () => _showCreatePlanDialog(context),
          ),
        ],
      ),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: bibleProvider.getActivePlans(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
          final plans = snapshot.data!;
          if (plans.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.menu_book, size: 64, color: Colors.grey),
                  const SizedBox(height: 16),
                  const Text('진행 중인 통독 계획이 없습니다.'),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed: () => _createSamplePlan(context),
                    child: const Text('90일 통독 시작하기'),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: plans.length,
            itemBuilder: (context, index) {
              final plan = plans[index];
              return Card(
                margin: const EdgeInsets.all(12),
                child: ListTile(
                  title: Text(plan['title'], style: const TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: Text('${plan['total_days']}일 과정 - ${plan['description']}'),
                  trailing: const Icon(Icons.arrow_forward_ios),
                  onTap: () => _showPlanDetails(context, plan),
                ),
              );
            },
          );
        },
      ),
    );
  }

  void _createSamplePlan(BuildContext context) async {
    HapticFeedback.mediumImpact();
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    
    // 샘플 90일 계획 생성 (실제 66권을 순차적으로 배분하는 로직)
    List<Map<String, dynamic>> days = [];
    int dayCount = 1;
    
    // 단순화를 위해 초기 몇 권만 샘플로 구성
    final sampleBooks = allBibleBooks.take(5).toList(); // 창세기~신명기
    for (var book in sampleBooks) {
      for (int ch = 1; ch <= book.chapterCount; ch++) {
        if (dayCount > 90) break;
        days.add({
          'day': dayCount++,
          'book_name': book.name,
          'chapter': ch,
        });
      }
      if (dayCount > 90) break;
    }

    await bibleProvider.createReadingPlan(
      '모세오경 90일 통독 도전', 
      '창세기부터 신명기까지 하나님의 율법과 역사를 90일간 깊이 묵상하세요.', 
      days
    );
  }

  void _showCreatePlanDialog(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('커스텀 계획 생성 기능은 준비 중입니다.'))
    );
  }

  void _showPlanDetails(BuildContext context, Map<String, dynamic> plan) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => _PlanDetailPage(planId: plan['id'], title: plan['title']),
      ),
    );
  }
}

class _PlanDetailPage extends StatelessWidget {
  final int planId;
  final String title;

  const _PlanDetailPage({required this.planId, required this.title});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: bibleProvider.getPlanDays(planId),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
          final days = snapshot.data!;
          
          int completed = days.where((d) => d['is_completed'] == 1).length;
          double progress = days.isEmpty ? 0 : completed / days.length;

          return Column(
            children: [
              LinearProgressIndicator(value: progress, minHeight: 10),
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text('진행률: ${(progress * 100).toStringAsFixed(1)}% ($completed/${days.length})'),
              ),
              Expanded(
                child: ListView.builder(
                  itemCount: days.length,
                  itemBuilder: (context, index) {
                    final d = days[index];
                    final isDone = d['is_completed'] == 1;
                    return ListTile(
                      leading: Checkbox(
                        value: isDone,
                        onChanged: (val) {
                          bibleProvider.updatePlanProgress(d['id'], val ?? false);
                        },
                      ),
                      title: Text('${d['day']}일차: ${d['book_name']} ${d['chapter']}장'),
                      trailing: const Icon(Icons.arrow_forward_ios, size: 14),
                      onTap: () {
                        final bookInfo = allBibleBooks.firstWhere((b) => b.name == d['book_name']);
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => ReadScreen(
                              bookName: d['book_name'],
                              initialChapter: d['chapter'],
                              maxChapter: bookInfo.chapterCount,
                            ),
                          ),
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
