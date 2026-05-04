import 'dart:convert';
import 'dart:math';
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

class _SetupScreenState extends State<SetupScreen> with TickerProviderStateMixin {
  String _statusMessage = '시작하려면 성경 번역본을 선택해주세요.';
  String _subMessage = '';
  double _progress = 0.0;
  bool _hasError = false;
  bool _isDownloading = false;
  int _retryCount = 0;
  static const int _maxRetries = 3;
  
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  final List<Map<String, String>> _translations = [
    {'name': '개역한글 (KRV)', 'key': 'krv', 'desc': '한국어'},
    {'name': 'King James (KJV)', 'key': 'kjv', 'desc': 'English'},
  ];

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1500),
    )..repeat(reverse: true);
    
    _pulseAnimation = Tween<double>(
      begin: 1.0,
      end: 1.1,
    ).animate(CurvedAnimation(
      parent: _pulseController,
      curve: Curves.easeInOut,
    ));
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  int _getRetryDelay() {
    // 지수 백오프: 2^retryCount 초 (최대 8초)
    return min(pow(2, _retryCount).toInt(), 8);
  }

  Future<void> _startSetup(String translationKey) async {
    setState(() {
      _isDownloading = true;
      _hasError = false;
      _retryCount = 0;
      _statusMessage = '성경 데이터를 준비하고 있습니다...';
      _subMessage = '서버에 연결 중';
      _progress = 0.1;
    });

    await _downloadWithRetry(translationKey);
  }

  Future<void> _downloadWithRetry(String translationKey) async {
    while (_retryCount < _maxRetries) {
      try {
        await _performDownload(translationKey);
        return; // 성공 시 종료
      } catch (e) {
        _retryCount++;
        
        if (_retryCount >= _maxRetries) {
          setState(() {
            _hasError = true;
            _isDownloading = false;
            _statusMessage = '데이터를 가져오는데 실패했습니다';
            _subMessage = '인터넷 연결을 확인하고 다시 시도해 주세요\n(${_getErrorMessage(e)})';
            _progress = 0.0;
          });
          return;
        }

        final delay = _getRetryDelay();
        setState(() {
          _statusMessage = '연결에 문제가 발생했습니다. 재시도 중...';
          _subMessage = '${_retryCount}번째 재시도 (${delay}초 후)';
          _progress = 0.05 * _retryCount;
        });

        await Future.delayed(Duration(seconds: delay));
      }
    }
  }

  String _getErrorMessage(dynamic error) {
    if (error.toString().contains('SocketException')) {
      return '네트워크 연결 오류';
    } else if (error.toString().contains('TimeoutException')) {
      return '서버 응답 시간 초과';
    } else if (error.toString().contains('404')) {
      return '데이터를 찾을 수 없음';
    }
    return '알 수 없는 오류';
  }

  Future<void> _performDownload(String translationKey) async {
    setState(() {
      _statusMessage = '성경 데이터 다운로드 중...';
      _subMessage = '원격 서버에서 데이터를 가져오는 중';
      _progress = 0.2;
    });

    final url = Uri.parse(
      'https://raw.githubusercontent.com/jeiel85/veritas-bible/main/assets/bible_$translationKey.json',
    );

    // 타임아웃 설정된 HTTP 요청
    final response = await http.get(url).timeout(
      const Duration(seconds: 30),
      onTimeout: () {
        throw Exception('TimeoutException: 서버 응답 시간 초과');
      },
    );

    if (response.statusCode == 200) {
      setState(() {
        _statusMessage = '데이터 처리 중...';
        _subMessage = 'JSON 파싱 및 유효성 검사';
        _progress = 0.5;
      });

      final data = json.decode(response.body);
      
      if (data == null || data.isEmpty) {
        throw Exception('EmptyData: 다운로드된 데이터가 비어있습니다');
      }

      setState(() {
        _statusMessage = '데이터베이스 구축 중...';
        _subMessage = 'SQLite에 성경 본문 저장 중';
        _progress = 0.7;
      });

      final bibleProvider = Provider.of<BibleProvider>(context, listen: false);
      final settingsProvider = Provider.of<SettingsProvider>(context, listen: false);

      await bibleProvider.importExternalData(data);

      setState(() {
        _statusMessage = '설정 저장 중...';
        _subMessage = '초기화 정보 기록';
        _progress = 0.9;
      });

      await settingsProvider.setInitialized(true);

      setState(() {
        _statusMessage = '준비 완료!';
        _subMessage = '앱을 시작합니다';
        _progress = 1.0;
      });

      await Future.delayed(const Duration(milliseconds: 800));

      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (_) => const SplashScreen()),
        );
      }
    } else {
      throw Exception('HTTP ${response.statusCode}: 서버 응답 오류');
    }
  }

  void _resetSetup() {
    setState(() {
      _isDownloading = false;
      _hasError = false;
      _retryCount = 0;
      _statusMessage = '시작하려면 성경 번역본을 선택해주세요.';
      _subMessage = '';
      _progress = 0.0;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1A237E),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                // 아이콘
                ScaleTransition(
                  scale: _isDownloading ? _pulseAnimation : const AlwaysStoppedAnimation(1.0),
                  child: Container(
                    width: 80,
                    height: 80,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.1),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(
                      _hasError ? Icons.error_outline : (_isDownloading ? Icons.cloud_download : Icons.menu_book),
                      color: Colors.white,
                      size: 40,
                    ),
                  ),
                ),
                const SizedBox(height: 32),
                
                // 타이틀
                const Text(
                  'Veritas Bible',
                  style: TextStyle(
                    color: Colors.white, 
                    fontSize: 28, 
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '성경 데이터 설정',
                  style: TextStyle(
                    color: Colors.white.withOpacity(0.7), 
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 32),
                
                // 상태 메시지
                Text(
                  _statusMessage,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.white, 
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                if (_subMessage.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  Text(
                    _subMessage,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Colors.white.withOpacity(0.6), 
                      fontSize: 13,
                    ),
                  ),
                ],
                const SizedBox(height: 32),
                
                // 프로그레스 바 또는 버튼
                if (!_isDownloading && !_hasError)
                  _buildTranslationButtons()
                else if (_isDownloading)
                  _buildProgressIndicator()
                else if (_hasError)
                  _buildErrorButtons(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTranslationButtons() {
    return Column(
      children: _translations.map((t) => Padding(
        padding: const EdgeInsets.only(bottom: 12.0),
        child: Container(
          width: double.infinity,
          height: 56,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            gradient: LinearGradient(
              colors: [
                Colors.white.withOpacity(0.15),
                Colors.white.withOpacity(0.05),
              ],
            ),
            border: Border.all(
              color: Colors.white.withOpacity(0.2),
              width: 1,
            ),
          ),
          child: Material(
            color: Colors.transparent,
            borderRadius: BorderRadius.circular(12),
            child: InkWell(
              borderRadius: BorderRadius.circular(12),
              onTap: () => _startSetup(t['key']!),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            t['name']!,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          Text(
                            t['desc']!,
                            style: TextStyle(
                              color: Colors.white.withOpacity(0.6),
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Icon(
                      Icons.arrow_forward_ios,
                      color: Colors.white.withOpacity(0.5),
                      size: 16,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
 )).toList(),
    );
  }

  Widget _buildProgressIndicator() {
    return Column(
      children: [
        // 프로그레스 바
        Container(
          width: double.infinity,
          height: 6,
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.1),
            borderRadius: BorderRadius.circular(3),
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(3),
            child: LinearProgressIndicator(
              value: _progress > 0 ? _progress : null,
              backgroundColor: Colors.transparent,
              valueColor: AlwaysStoppedAnimation<Color>(
                Colors.orange.shade400,
              ),
            ),
          ),
        ),
        const SizedBox(height: 12),
        // 진행률 텍스트
        Text(
          '${(_progress * 100).toInt()}%',
          style: TextStyle(
            color: Colors.white.withOpacity(0.6),
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
      ],
    );
  }

  Widget _buildErrorButtons() {
    return Column(
      children: [
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.red.withOpacity(0.1),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: Colors.red.withOpacity(0.3),
              width: 1,
            ),
          ),
          child: Row(
            children: [
              Icon(
                Icons.info_outline,
                color: Colors.red.shade300,
                size: 20,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  'Wi-Fi 또는 모바일 데이터 연결을 확인해주세요',
                  style: TextStyle(
                    color: Colors.red.shade200,
                    fontSize: 13,
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        SizedBox(
          width: double.infinity,
          height: 50,
          child: ElevatedButton.icon(
            onPressed: _resetSetup,
            icon: const Icon(Icons.refresh),
            label: const Text('다시 시도'),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.white,
              foregroundColor: const Color(0xFF1A237E),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
