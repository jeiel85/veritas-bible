import 'package:flutter/material.dart';

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

class _PrayerCommunityView extends StatelessWidget {
  const _PrayerCommunityView();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          color: Colors.blue.withOpacity(0.05),
          child: const Text(
            '서로를 위해 기도하는 따뜻한 공간입니다.',
            style: TextStyle(fontSize: 12, color: Colors.blueGrey),
          ),
        ),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _prayerCard('익명', '가족의 건강과 평안을 위해 기도 부탁드립니다.', 12),
              _prayerCard('김성도', '새로운 직장 적응을 위해 지혜를 구합니다.', 5),
              _prayerCard('이집사', '수험생 자녀를 위한 기도를 요청합니다.', 28),
            ],
          ),
        ),
      ],
    );
  }

  Widget _prayerCard(String author, String content, int prayCount) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(child: Text(author[0])),
                const SizedBox(width: 12),
                Text(author, style: const TextStyle(fontWeight: FontWeight.bold)),
              ],
            ),
            const SizedBox(height: 12),
            Text(content),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('중보 $prayCount명', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                ElevatedButton.icon(
                  onPressed: () {},
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
