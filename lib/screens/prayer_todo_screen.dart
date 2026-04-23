import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';

class PrayerTodoScreen extends StatefulWidget {
  const PrayerTodoScreen({super.key});

  @override
  State<PrayerTodoScreen> createState() => _PrayerTodoScreenState();
}

class _PrayerTodoScreenState extends State<PrayerTodoScreen> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('기도 할 일 (To-do)'),
      ),
      body: FutureBuilder<List<Map<String, dynamic>>>(
        future: bibleProvider.getPrayerTodos(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const Center(child: CircularProgressIndicator());
          final prayers = snapshot.data!;
          if (prayers.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.volunteer_activism, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('등록된 기도 제목이 없습니다.'),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: prayers.length,
            itemBuilder: (context, index) {
              final p = prayers[index];
              final isDone = p['is_completed'] == 1;
              return ListTile(
                leading: Checkbox(
                  value: isDone,
                  onChanged: (val) {
                    bibleProvider.updatePrayerStatus(p['id'], val ?? false);
                  },
                ),
                title: Text(
                  p['title'],
                  style: TextStyle(
                    decoration: isDone ? TextDecoration.lineThrough : null,
                    color: isDone ? Colors.grey : null,
                  ),
                ),
                subtitle: Text('등록일: ${p['created_at'].split(' ')[0]}', style: const TextStyle(fontSize: 10)),
                trailing: IconButton(
                  icon: const Icon(Icons.delete_outline, size: 20),
                  onPressed: () => bibleProvider.deletePrayer(p['id']),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddPrayerDialog(context),
        child: const Icon(Icons.add),
      ),
    );
  }

  void _showAddPrayerDialog(BuildContext context) {
    final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('새 기도 제목'),
        content: TextField(
          controller: _controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: '기도하고 싶은 내용을 적어주세요...',
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
          ElevatedButton(
            onPressed: () {
              if (_controller.text.isNotEmpty) {
                bibleProvider.addPrayer(_controller.text);
                _controller.clear();
                Navigator.pop(context);
              }
            },
            child: const Text('저장'),
          ),
        ],
      ),
    );
  }
}
