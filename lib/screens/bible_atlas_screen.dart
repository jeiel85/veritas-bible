import 'package:flutter/material.dart';

class BibleAtlasScreen extends StatelessWidget {
  const BibleAtlasScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('성경 지도 및 연대표'),
          bottom: const TabBar(
            tabs: [
              Tab(icon: Icon(Icons.map), text: '지도'),
              Tab(icon: Icon(Icons.timeline), text: '연대표'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _buildMapsView(),
            _buildTimelineView(),
          ],
        ),
      ),
    );
  }

  Widget _buildMapsView() {
    return GridView.count(
      crossAxisCount: 2,
      padding: const EdgeInsets.all(16),
      mainAxisSpacing: 16,
      crossAxisSpacing: 16,
      children: [
        _mapCard('출애굽 여정', '시내 광야와 가나안'),
        _mapCard('예수님의 공생애', '갈릴리와 예루살렘'),
        _mapCard('바울의 전도 여행', '소아시아와 로마'),
        _mapCard('다윗 왕국', '이스라엘 통일 왕국'),
      ],
    );
  }

  Widget _mapCard(String title, String subtitle) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Stack(
        fit: StackFit.expand,
        children: [
          Container(
            color: Colors.blueGrey.shade100,
            child: const Icon(Icons.map_outlined, size: 48, color: Colors.blueGrey),
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: Container(
              color: Colors.black54,
              padding: const EdgeInsets.all(8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                  Text(subtitle, style: const TextStyle(color: Colors.white70, fontSize: 10)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTimelineView() {
    final List<Map<String, String>> timelineData = [
      {'year': 'BC 4000', 'event': '창조 시대', 'desc': '천지 창조와 에덴 동산'},
      {'year': 'BC 2100', 'event': '족장 시대', 'desc': '아브라함, 이삭, 야곱'},
      {'year': 'BC 1446', 'event': '출애굽 시대', 'desc': '모세와 광야 여정'},
      {'year': 'BC 1010', 'event': '통일 왕국', 'desc': '다윗 왕과 솔로몬'},
      {'year': 'AD 1', 'event': '예수 탄생', 'desc': '그리스도의 강림'},
      {'year': 'AD 33', 'event': '초대 교회', 'desc': '오순절 성령 강림과 선교'},
    ];

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: timelineData.length,
      itemBuilder: (context, index) {
        final item = timelineData[index];
        return Row(
          children: [
            SizedBox(
              width: 80,
              child: Column(
                children: [
                  Text(item['year']!, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blue)),
                  if (index < timelineData.length - 1)
                    Container(width: 2, height: 40, color: Colors.blue.withOpacity(0.3)),
                ],
              ),
            ),
            Expanded(
              child: Card(
                margin: const EdgeInsets.only(bottom: 16),
                child: ListTile(
                  title: Text(item['event']!, style: const TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: Text(item['desc']!),
                ),
              ),
            ),
          ],
        );
      },
    );
  }
}
