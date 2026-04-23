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
  String _statusMessage = '성경 데이터를 준비하고 있습니다...';
  double _progress = 0.0;
  bool _hasError = false;

  @override
  void initState() {
    super.initState();
    _startSetup();
  }

  Future<void> _startSetup() async {
    try {
      setState(() {
        _statusMessage = '최신 성경 데이터를 다운로드 중입니다...';
        _progress = 0.2;
      });

      // 1. 공신력 있는 오픈소스 성경 데이터 (KRV 기준 샘플 URL)
      // 실제 운영 시에는 본인의 서버 또는 안정적인 Raw GitHub URL을 사용해야 함
      final url = Uri.parse('https://raw.githubusercontent.com/jeiel85/veritas-bible/main/assets/bible_krv.json');
      final response = await http.get(url);

      if (response.statusCode == 200) {
        setState(() {
          _statusMessage = '데이터베이스 구축 중... 잠시만 기다려주세요.';
          _progress = 0.6;
        });

        final data = json.decode(response.body);
        final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
        final settingsProvider = Provider.of<SettingsProvider>(context, listen: false);

        // 2. 실제 데이터 임포트 실행
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
                '초기 설정',
                style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              Text(
                _statusMessage,
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.white70, fontSize: 14),
              ),
              const SizedBox(height: 48),
              if (!_hasError)
                LinearProgressIndicator(
                  value: _progress,
                  backgroundColor: Colors.white10,
                  color: Colors.orange,
                )
              else
                ElevatedButton(
                  onPressed: () {
                    setState(() {
                      _hasError = false;
                      _progress = 0.0;
                    });
                    _startSetup();
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
