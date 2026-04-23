import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';
import '../providers/bible_provider.dart';
import '../providers/settings_provider.dart';
import 'splash_screen.dart';

class SetupScreen extends StatefulWidget {
  const SetupScreen({super.key});

  @override
  State<SetupScreen> createState() => _SetupScreenState();
}

class _SetupScreenState extends State<SetupScreen> {
  String _statusMessage = '다운로드할 성경 번역본을 선택해주세요.';
  double _progress = 0.0;
  bool _hasError = false;
  bool _isDownloading = false;

  final List<Map<String, String>> _translations = [
    {'name': '개역한글 (KRV)', 'key': 'krv'},
    {'name': 'King James (KJV)', 'key': 'kjv'},
  ];

  Future<void> _startSetup(String translationKey) async {
    setState(() {
      _isDownloading = true;
      _hasError = false;
      _statusMessage = '최신 성경 데이터를 다운로드 중입니다...';
      _progress = 0.2;
    });

    try {
      final url = Uri.parse('https://raw.githubusercontent.com/jeiel85/veritas-bible/main/assets/bible_$translationKey.json');
      final response = await http.get(url);

      if (response.statusCode == 200) {
        setState(() {
          _statusMessage = '데이터베이스 구축 중... 잠시만 기다려주세요.';
          _progress = 0.6;
        });

        final data = json.decode(response.body);
        final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
        final settingsProvider = Provider.of<SettingsProvider>(context, listen: false);

        await bibleProvider.importExternalData(data);

        setState(() {
          _statusMessage = '준비 완료! 시작합니다.';
          _progress = 1.0;
        });

        await Future.delayed(const Duration(milliseconds: 1000));
        await settingsProvider.setInitialized(true);

        if (mounted) {
          Navigator.pushReplacement(
            context, 
            MaterialPageRoute(builder: (_) => const SplashScreen())
          );
        }
      } else {
        throw Exception('데이터를 불러오지 못했습니다. (Error: ${response.statusCode})');
      }
    } catch (e) {
      debugPrint("Setup Error: $e");
      setState(() {
        _statusMessage = '데이터를 가져오는데 실패했습니다.\n인터넷 연결을 확인하고 다시 시도해 주세요.';
        _hasError = true;
        _isDownloading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1A237E),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.cloud_download, color: Colors.white, size: 64),
              const SizedBox(height: 32),
              const Text(
                '성경 데이터 설정',
                style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              Text(
                _statusMessage,
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.white70, fontSize: 14),
              ),
              const SizedBox(height: 48),
              if (!_isDownloading)
                ..._translations.map((t) => Padding(
                  padding: const EdgeInsets.only(bottom: 12.0),
                  child: SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      onPressed: () => _startSetup(t['key']!),
                      child: Text(t['name']!),
                    ),
                  ),
                )).toList()
              else if (!_hasError)
                LinearProgressIndicator(
                  value: _progress,
                  backgroundColor: Colors.white10,
                  color: Colors.orange,
                )
              else
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      _isDownloading = false;
                      _hasError = false;
                      _statusMessage = '다운로드할 성경 번역본을 선택해주세요.';
                    });
                  },
                  child: const Text('다시 시도'),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
