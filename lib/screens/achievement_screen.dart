import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import 'spirit_dashboard_screen.dart';

class AchievementScreen extends StatelessWidget {
  const AchievementScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('영적 성장 및 업적'),
      ),
      body: FutureBuilder<Map<String, dynamic>>(
        future: _loadData(bibleProvider),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
          
          final int level = snapshot.data!['level'];
          final List<String> earnedBadges = snapshot.data!['badges'];
          
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _buildLevelCard(context, level),
              const SizedBox(height: 16),
              ElevatedButton.icon(
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const SpiritDashboardScreen()),
                  );
                },
                icon: const Icon(Icons.calendar_month),
                label: const Text('상세 활동 캘린더 보기'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
              const SizedBox(height: 32),
              const Text('성취 배지', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              _buildBadgeGrid(earnedBadges),
            ],
          );
        },
      ),
    );
  }

  Future<Map<String, dynamic>> _loadData(BibleProvider provider) async {
    return {
      'level': await provider.getLevel(),
      'badges': await provider.getEarnedBadges(),
    };
  }

  Widget _buildLevelCard(BuildContext context, int level) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Theme.of(context).colorScheme.primary, Theme.of(context).colorScheme.secondary],
        ),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        children: [
          const Text('현재 영적 성장 레벨', style: TextStyle(color: Colors.white70, fontSize: 14)),
          const SizedBox(height: 8),
          Text('Lv. $level', style: const TextStyle(color: Colors.white, fontSize: 48, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          const LinearProgressIndicator(value: 0.7, color: Colors.white, backgroundColor: Colors.white24),
          const SizedBox(height: 8),
          const Text('다음 레벨까지 3회 더 읽기', style: TextStyle(color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }

  Widget _buildBadgeGrid(List<String> earned) {
    final List<Map<String, dynamic>> allBadges = [
      {'id': 'streak_7', 'title': '7일의 약속', 'desc': '7일 연속 성경 읽기 달성', 'icon': Icons.timer},
      {'id': 'streak_30', 'title': '한 달의 기적', 'desc': '30일 연속 성경 읽기 달성', 'icon': Icons.calendar_month},
      {'id': 'read_100', 'title': '백절불굴', 'desc': '총 100구절 이상 통독', 'icon': Icons.auto_stories},
      {'id': 'prayer_10', 'title': '기도의 용사', 'desc': '기도 제목 10개 응답 완료', 'icon': Icons.favorite},
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 0.8,
      ),
      itemCount: allBadges.length,
      itemBuilder: (context, index) {
        final badge = allBadges[index];
        bool isEarned = earned.contains(badge['id']);
        return Card(
          elevation: isEarned ? 4 : 0,
          color: isEarned ? null : Colors.grey[200],
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                badge['icon'], 
                size: 48, 
                color: isEarned ? Theme.of(context).colorScheme.primary : Colors.grey[400]
              ),
              const SizedBox(height: 12),
              Text(
                badge['title'],
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: isEarned ? null : Colors.grey[600]
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0),
                child: Text(
                  badge['desc'],
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 10, color: Colors.grey[600]),
                ),
              ),
              if (isEarned)
                const Padding(
                  padding: EdgeInsets.only(top: 8.0),
                  child: Icon(Icons.check_circle, color: Colors.green, size: 16),
                ),
            ],
          ),
        );
      },
    );
  }
}
