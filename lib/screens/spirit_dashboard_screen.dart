import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:table_calendar/table_calendar.dart';
import '../providers/bible_provider.dart';
import '../models/bible_metadata.dart';

class SpiritDashboardScreen extends StatefulWidget {
  const SpiritDashboardScreen({super.key});

  @override
  State<SpiritDashboardScreen> createState() => _SpiritDashboardScreenState();
}

class _SpiritDashboardScreenState extends State<SpiritDashboardScreen> {
  CalendarFormat _calendarFormat = CalendarFormat.month;
  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;
  Map<DateTime, List<String>> _events = {};
  Map<String, int> _readHistory = {}; // bookName -> readChapters count

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
    _loadData();
  }

  Future<void> _loadData() async {
    final provider = Provider.of<BibleProvider>(context, listen: false);
    
    // 1. 달력 이벤트 로드
    final logs = await provider.getRecentActivity();
    Map<DateTime, List<String>> eventMap = {};
    for (var log in logs) {
      DateTime date = DateTime.parse(log['activity_date']);
      DateTime cleanDate = DateTime(date.year, date.month, date.day);
      int count = log['read_count'] as int;
      if (count > 0) {
        eventMap[cleanDate] = ['성경 읽기 $count회'];
      }
    }

    // 2. 장 읽기 기록 로드 (66권 지도용)
    final history = await provider.getReadHistory();
    Map<String, Set<int>> bookReadMap = {};
    for (var entry in history) {
      String name = entry['book_name'];
      int chapter = entry['chapter'];
      bookReadMap.putIfAbsent(name, () => {}).add(chapter);
    }

    Map<String, int> readCounts = {};
    bookReadMap.forEach((key, value) {
      readCounts[key] = value.length;
    });

    setState(() {
      _events = eventMap;
      _readHistory = readCounts;
    });
  }

  List<String> _getEventsForDay(DateTime day) {
    DateTime cleanDay = DateTime(day.year, day.month, day.day);
    return _events[cleanDay] ?? [];
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        app_bar: AppBar(
          title: const Text('영성 대시보드'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '활동 캘린더'),
              Tab(text: '성경 통독 지도'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _buildCalendarTab(),
            _buildProgressMapTab(),
          ],
        ),
      ),
    );
  }

  Widget _buildCalendarTab() {
    return Column(
      children: [
        TableCalendar(
          firstDay: DateTime.utc(2020, 1, 1),
          lastDay: DateTime.utc(2030, 12, 31),
          focusedDay: _focusedDay,
          calendarFormat: _calendarFormat,
          eventLoader: _getEventsForDay,
          selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
          onDaySelected: (selectedDay, focusedDay) {
            setState(() {
              _selectedDay = selectedDay;
              _focusedDay = focusedDay;
            });
          },
          onFormatChanged: (format) {
            setState(() => _calendarFormat = format);
          },
          calendarStyle: CalendarStyle(
            todayDecoration: BoxDecoration(color: Theme.of(context).colorScheme.secondary, shape: BoxShape.circle),
            selectedDecoration: BoxDecoration(color: Theme.of(context).colorScheme.primary, shape: BoxShape.circle),
            markerDecoration: const BoxDecoration(color: Colors.orange, shape: BoxShape.circle),
          ),
        ),
        const SizedBox(height: 20),
        Expanded(
          child: _buildDayDetails(),
        ),
      ],
    );
  }

  Widget _buildProgressMapTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '66권 성경 통독 현황',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          const Text(
            '각 권의 모든 장을 읽으면 색상이 진해집니다.',
            style: TextStyle(fontSize: 12, color: Colors.grey),
          ),
          const SizedBox(height: 20),
          _buildMapGrid(allBibleBooks.where((b) => b.isOldTestament).toList(), '구약성경'),
          const SizedBox(height: 30),
          _buildMapGrid(allBibleBooks.where((b) => !b.isOldTestament).toList(), '신약성경'),
          const SizedBox(height: 40),
        ],
      ),
    );
  }

  Widget _buildMapGrid(List<BibleBookInfo> books, String title) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 8.0),
          child: Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        ),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 6,
            mainAxisSpacing: 8,
            crossAxisSpacing: 8,
          ),
          itemCount: books.length,
          itemBuilder: (context, index) {
            final book = books[index];
            final readCount = _readHistory[book.name] ?? 0;
            final progress = readCount / book.chapterCount;
            final color = progress >= 1.0 
                ? Colors.green 
                : progress > 0 
                    ? Colors.green.withOpacity(0.3 + (progress * 0.5))
                    : Colors.grey.shade200;

            return Container(
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.black12),
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    book.name.substring(0, 1), // 첫 글자만
                    style: TextStyle(
                      fontSize: 12, 
                      fontWeight: FontWeight.bold,
                      color: progress > 0.5 ? Colors.white : Colors.black87
                    ),
                  ),
                  Text(
                    '${(progress * 100).toInt()}%',
                    style: TextStyle(
                      fontSize: 8,
                      color: progress > 0.5 ? Colors.white70 : Colors.black54
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildDayDetails() {
    final events = _getEventsForDay(_selectedDay!);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.3),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(32)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${_selectedDay!.year}년 ${_selectedDay!.month}월 ${_selectedDay!.day}일 활동',
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          if (events.isEmpty)
            const Center(child: Text('이 날은 기록된 활동이 없습니다.'))
          else
            ...events.map((e) => ListTile(
              leading: const Icon(Icons.check_circle, color: Colors.green),
              title: Text(e),
            )).toList(),
        ],
      ),
    );
  }
}
