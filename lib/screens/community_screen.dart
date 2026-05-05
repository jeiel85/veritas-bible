import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:flutter/services.dart';

class CommunityScreen extends StatefulWidget {
  const CommunityScreen({super.key});

  @override
  State<CommunityScreen> createState() => _CommunityScreenState();
}

class _CommunityScreenState extends State<CommunityScreen> {
  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('신앙 공동체'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '기도 중보'),
              Tab(text: '통독 챌린지'),
              Tab(text: '묵상 나눔'),
            ],
          ),
        ),
        body: const TabBarView(
          children: [
            _PrayerCommunityView(),
            _ReadingChallengeView(),
            _JournalSharingView(),
          ],
        ),
      ),
    );
  }
}

/// 입력값 새니타이즈 유틸리티
class CommunitySecurity {
  /// XSS 방지를 위한 HTML 태그/스크립트 제거
  static String sanitizeInput(String input) {
    if (input.isEmpty) return input;
    
    String sanitized = input;
    // HTML 태그 제거
    sanitized = sanitized.replaceAll(RegExp(r'<[^>]*>'), '');
    // 스크립트 태그 제거
    sanitized = sanitized.replaceAll(RegExp(r'<script[^>]*>.*?</script>', caseSensitive: false), '');
    // 특수문자 이스케이프 (기본)
    sanitized = sanitized.replaceAll('&', '&amp;');
    sanitized = sanitized.replaceAll('<', '&lt;');
    sanitized = sanitized.replaceAll('>', '&gt;');
    sanitized = sanitized.replaceAll('"', '&quot;');
    sanitized = sanitized.replaceAll("'", '&#39;');
    
    return sanitized.trim();
  }

  /// 금지어 필터링 (기본 구현)
  static String filterProfanity(String input) {
    final profanities = ['badword1', 'badword2']; // 실제 금지어 목록으로 교체 필요
    String filtered = input;
    for (final word in profanities) {
      filtered = filtered.replaceAll(RegExp(word, caseSensitive: false), '***');
    }
    return filtered;
  }

  /// 입력 검증 (길이, 빈칸, 특수문자)
  static String? validatePrayerRequest(String input) {
    if (input.isEmpty) return '기도 제목을 입력해 주세요.';
    if (input.length < 10) return '너무 짧습니다. 최소 10자 이상 입력해 주세요.';
    if (input.length > 500) return '너무 깁니다. 500자 이내로 입력해 주세요.';
    
    // 악성 패턴 검사 (기본)
    if (input.contains(RegExp(r'(javascript|vbscript|onload)', caseSensitive: false))) {
      return '허용되지 않은 내용이 포함되어 있습니다.';
    }
    
    return null; // 통과
  }

  /// 사용자 ID 생성 (익명용)
  static String generateAnonymousId() {
    final random = DateTime.now().millisecondsSinceEpoch % 10000;
    return 'user_$random';
  }
}

class _PrayerCommunityView extends StatefulWidget {
  const _PrayerCommunityView();

  @override
  State<_PrayerCommunityView> createState() => _PrayerCommunityViewState();
}

class _PrayerCommunityViewState extends State<_PrayerCommunityView> {
  final List<Map<String, dynamic>> _prayers = [
    {'author': '익명', 'content': '가족의 건강과 평안을 위해 기도 부탁드립니다.', 'prayCount': 12, 'isUser': false},
    {'author': '김성도', 'content': '새로운 직장 적응을 위해 지혜를 구합니다.', 'prayCount': 5, 'isUser': false},
    {'author': '이집사', 'content': '수험생 자녀를 위한 기도를 요청합니다.', 'prayCount': 28, 'isUser': false},
  ];

  void _addPrayerRequest() {
    final controller = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('기도 제목 올리기'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: controller,
              decoration: const InputDecoration(
                labelText: '기도 제목',
                hintText: '기도 부탁드릴 내용을 적어주세요...',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
              maxLength: 500,
            ),
            const SizedBox(height: 8),
            const Text(
              '익명으로 올라갑니다. (최소 10자 이상)',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () {
              final content = controller.text.trim();
              final error = CommunitySecurity.validatePrayerRequest(content);

              if (error != null) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text(error)),
                );
                return;
              }

              final sanitized = CommunitySecurity.sanitizeInput(content);
              final filtered = CommunitySecurity.filterProfanity(sanitized);

              setState(() {
                _prayers.insert(0, {
                  'author': '익명${CommunitySecurity.generateAnonymousId()}',
                  'content': filtered,
                  'prayCount': 0,
                  'isUser': true,
                });
              });

              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('기도 제목이 올라갔습니다. 🙏')),
              );
            },
            child: const Text('올리기'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          color: Colors.blue.withOpacity(0.05),
          child: Row(
            children: [
              Expanded(
                child: const Text(
                  '서로를 위해 기도하는 따뜻한 공간입니다.',
                  style: TextStyle(fontSize: 12, color: Colors.blueGrey),
                ),
              ),
              TextButton.icon(
                onPressed: _addPrayerRequest,
                icon: const Icon(Icons.add, size: 16),
                label: const Text('기도 제목 올리기'),
              ),
            ],
          ),
        ),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: _prayers.map((prayer) {
              return _prayerCard(
                prayer['author'],
                prayer['content'],
                prayer['prayCount'],
                isUserGenerated: prayer['isUser'] ?? false,
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _prayerCard(String author, String content, int prayCount, {bool isUserGenerated = false}) {
    final sanitizedContent = CommunitySecurity.sanitizeInput(content);
    
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                  child: Text(author[0], style: TextStyle(color: Theme.of(context).colorScheme.onPrimaryContainer)),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(author, style: const TextStyle(fontWeight: FontWeight.bold)),
                ),
                if (isUserGenerated)
                  IconButton(
                    icon: const Icon(Icons.delete_outline, size: 18),
                    tooltip: '삭제',
                    onPressed: () {
                      // TODO: 삭제 로직 구현
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('기도 제목이 삭제되었습니다.')),
                      );
                    },
                  ),
                if (!isUserGenerated)
                  PopupMenuButton<String>(
                    icon: const Icon(Icons.more_vert, size: 18),
                    onSelected: (value) {
                      if (value == 'report') {
                        _showReportDialog(context, author, sanitizedContent);
                      } else if (value == 'block') {
                        _showBlockDialog(context, author);
                      }
                    },
                    itemBuilder: (context) => [
                      const PopupMenuItem(value: 'report', child: Row(children: [Icon(Icons.flag, size: 16), SizedBox(width: 8), Text('신고하기')])),
                      const PopupMenuItem(value: 'block', child: Row(children: [Icon(Icons.block, size: 16), SizedBox(width: 8), Text('차단하기')])),
                    ],
                  ),
              ],
            ),
            const SizedBox(height: 12),
            Text(sanitizedContent, style: const TextStyle(height: 1.5)),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('중보 $prayCount명', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                ElevatedButton.icon(
                  onPressed: () {
                    // TODO: 서버 연동 시 중보 수 증가
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('기도에 함께 하였습니다. 🙏')),
                    );
                  },
                  icon: const Icon(Icons.favorite, size: 16),
                  label: const Text('함께 기도함'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.pink.shade50,
                    foregroundColor: Colors.pink,
                    elevation: 0,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showReportDialog(BuildContext context, String author, String content) {
    final reasonController = TextEditingController();
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('기도 제목 신고'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('작성자: $author'),
            const SizedBox(height: 8),
            Text('내용: $content', maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 12, color: Colors.grey)),
            const SizedBox(height: 16),
            TextField(
              controller: reasonController,
              decoration: const InputDecoration(
                labelText: '신고 사유',
                hintText: '신고 사유를 입력해 주세요...',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
              maxLength: 200,
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
          ElevatedButton(
            onPressed: () {
              if (reasonController.text.isEmpty) return;
              // TODO: 서버 연동 시 신고 접수
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('신고가 접수되었습니다. 검토 후 조치하겠습니다.')),
              );
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('신고하기'),
          ),
        ],
      ),
    );
  }

  void _showBlockDialog(BuildContext context, String author) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('사용자 차단'),
        content: Text('$author님을 차단하시겠습니까?\n\n차단 후에는 이 사용자의 게시물을 볼 수 없습니다.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
          ElevatedButton(
            onPressed: () {
              // TODO: 서버 연동 시 차단 처리
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text('$author님을 차단했습니다.')),
              );
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
            child: const Text('차단하기'),
          ),
        ],
      ),
    );
  }
}

class _ReadingChallengeView extends StatelessWidget {
  const _ReadingChallengeView();

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _challengeCard('신약 100일 통독', '45명 참여 중', 0.65),
        _challengeCard('시편 30일 묵상', '128명 참여 중', 0.2),
        _challengeCard('바울 서신 정복', '22명 참여 중', 0.9),
      ],
    );
  }

  Widget _challengeCard(String title, String participants, double progress) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 4),
            Text(participants, style: const TextStyle(fontSize: 12, color: Colors.grey)),
            const SizedBox(height: 16),
            LinearProgressIndicator(value: progress),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('나의 진도율 ${(progress * 100).toInt()}%', style: const TextStyle(fontSize: 11)),
                TextButton(onPressed: () {}, child: const Text('입장하기')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _JournalSharingView extends StatelessWidget {
  const _JournalSharingView();

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _journalCard('박청년', '오늘 시편 23편을 읽으며 주님이 나의 목자 되심에 큰 위로를 받았습니다.', '시편 23:1'),
        _journalCard('최권사', '말씀이 내 발의 등이 됨을 실감하는 하루였습니다.', '시편 119:105'),
      ],
    );
  }

  Widget _journalCard(String author, String content, String ref) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ListTile(
            leading: CircleAvatar(backgroundColor: Colors.teal.shade100, child: Text(author[0])),
            title: Text(author, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text(ref, style: const TextStyle(color: Colors.blue, fontSize: 12)),
          ),
          Padding(
            padding: const EdgeInsets.only(left: 16.0, right: 16.0, bottom: 16.0),
            child: Text(content, style: const TextStyle(height: 1.4)),
          ),
        ],
      ),
    );
  }
}
