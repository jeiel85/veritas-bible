import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:table_calendar/table_calendar.dart';
import '../providers/bible_provider.dart';

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

  @override
  void initState() {
    super.initState();
    _selectedDay = _focusedDay;
    _loadEvents();
  }

  Future<void> _loadEvents() async {
    final provider = Provider.of<BibleProvider>(context, listen: false);
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

    setState(() => _events = eventMap);
  }

  List<String> _getEventsForDay(DateTime day) {
    DateTime cleanDay = DateTime(day.year, day.month, day.day);
    return _events[cleanDay] ?? [];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('영성 대시보드'),
      ),
      body: Column(
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
      ),
    );
  }

  Widget _buildDayDetails() {
    final events = _getEventsForDay(_selectedDay!);
    return Container(
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
