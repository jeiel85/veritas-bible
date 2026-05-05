import 'dart:convert';
import 'package:http/http.dart' as http;
import '../services/secure_storage_service.dart';

/// AI 묵상 서비스
/// 외부 AI API 연동 및 로컬 LLM 지원
class AiMeditationService {
  static final AiMeditationService _instance = AiMeditationService._internal();
  factory AiMeditationService() => _instance;
  
  final SecureStorageService _secureStorage = SecureStorageService();
  static const String _apiKeyName = 'ai_api_key';
  static const String _providerName = 'ai_provider'; // 'openai', 'claude', 'local'
  
  String? _apiKey;
  String _provider = 'openai'; // 기본값
  
  AiMeditationService._internal();
  
  /// API 키 저장
  Future<void> saveApiKey(String key, String provider) async {
    _apiKey = key;
    _provider = provider;
    await _secureStorage.write(_apiKeyName, key);
    await _secureStorage.write(_providerName, provider);
  }
  
  /// API 키 및 제공자 로드
  Future<void> loadCredentials() async {
    _apiKey = await _secureStorage.read(_apiKeyName);
    _provider = await _secureStorage.read(_providerName) ?? 'openai';
  }
  
  /// API 키가 설정되어 있는지 확인
  bool get hasCredentials => _apiKey != null && _apiKey!.isNotEmpty;
  
  /// 말씀 기반 묵상 응답 생성
  Future<String> generateMeditation({
    required String verseReference,
    required String verseText,
    required String userQuery,
    List<Map<String, String>> history = const [],
  }) async {
    if (!hasCredentials) {
      return _generateFallbackResponse(userQuery, verseReference);
    }
    
    try {
      switch (_provider) {
        case 'openai':
          return await _callOpenAI(verseReference, verseText, userQuery, history);
        case 'claude':
          return await _callClaude(verseReference, verseText, userQuery, history);
        case 'local':
          return await _callLocalLLM(verseReference, verseText, userQuery, history);
        default:
          return _generateFallbackResponse(userQuery, verseReference);
      }
    } catch (e) {
      debugPrint('AI API Error: $e');
      return _generateFallbackResponse(userQuery, verseReference);
    }
  }
  
  /// OpenAI API 호출
  Future<String> _callOpenAI(
    String verseReference,
    String verseText,
    String userQuery,
    List<Map<String, String>> history,
  ) async {
    final url = Uri.parse('https://api.openai.com/v1/chat/completions');
    
    final messages = [
      {
        'role': 'system',
        'content': '당신은 성경 말씀을 묵상하도록 돕는 그리스도교 영성 지도자입니다. '
            '사용자가 질문한 성경 구절의 의미, 적용, 깨달음을 깊이 있고 따뜻하게 설명해 주세요. '
            '답변은 한국어로 작성하며, 성경 구절을 인용하고 실제적인 적용점을 제시하세요.'
      },
      ...history.map((msg) => {
        'role': msg['role'] == 'user' ? 'user' : 'assistant',
        'content': msg['content'],
      }),
      {
        'role': 'user',
        'content': '성경 구절: $verseReference\n'
            '본문: $verseText\n\n'
            '질문: $userQuery'
      },
    ];
    
    final response = await http.post(
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $_apiKey',
      },
      body: json.encode({
        'model': 'gpt-3.5-turbo',
        'messages': messages,
        'temperature': 0.7,
        'max_tokens': 500,
      }),
    ).timeout(const Duration(seconds: 30));
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return data['choices'][0]['message']['content'] as String;
    } else {
      throw Exception('OpenAI API Error: ${response.statusCode}');
    }
  }
  
  /// Claude API 호출
  Future<String> _callClaude(
    String verseReference,
    String verseText,
    String userQuery,
    List<Map<String, String>> history,
  ) async {
    final url = Uri.parse('https://api.anthropic.com/v1/messages');
    
    final messages = [
      ...history.map((msg) => {
        'role': msg['role'] == 'user' ? 'user' : 'assistant',
        'content': msg['content'],
      }),
      {
        'role': 'user',
        'content': '성경 구절: $verseReference\n'
            '본문: $verseText\n\n'
            '질문: $userQuery'
      },
    ];
    
    final response = await http.post(
      url,
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': _apiKey!,
        'anthropic-version': '2023-06-01',
      },
      body: json.encode({
        'model': 'claude-3-haiku-20240307',
        'system': '당신은 성경 말씀을 묵상하도록 돕는 그리스도교 영성 지도자입니다. '
            '사용자가 질문한 성경 구절의 의미, 적용, 깨달음을 깊이 있고 따뜻하게 설명해 주세요.'
        'messages': messages,
        'max_tokens': 500,
        'temperature': 0.7,
      }),
    ).timeout(const Duration(seconds: 30));
    
    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return data['content'][0]['text'] as String;
    } else {
      throw Exception('Claude API Error: ${response.statusCode}');
    }
  }
  
  /// 로컬 LLM 호출 (나중에 구현)
  Future<String> _callLocalLLM(
    String verseReference,
    String verseText,
    String userQuery,
    List<Map<String, String>> history,
  ) async {
    // TODO: 로컬 LLM 연동 구현 (예: Ollama, GGUF 모델)
    return _generateFallbackResponse(userQuery, verseReference);
  }
  
  /// Fallback 응답 (API 실패 시)
  String _generateFallbackResponse(String query, String reference) {
    if (query.contains('의미') || query.contains('뜻미')) {
      return '$reference의 의미는 하나님의 변치 않는 약속을 상징합니다. '
          '오늘 하루 이 말씀을 묵상하며 하나님의 사랑을 경험해 보세요. '
          '이 구절은 우리에게 인내와 소망을 가르쳐 줍니다.';
    } else if (query.contains('위로') || query.contains('위로')) {
      return '네, 주님은 항상 곁에서 위로하고 계십니다. '
          '"$reference" 말씀을 다시 한 번 천천히 읽어보세요. '
          '하나님께서 이 말씀을 통해 당신에게 하고 싶은 말씀은 무엇일까요?';
    } else if (query.contains('적용') || query.contains('실천')) {
      return '이 말씀을 오늘 하루 당신의 삶에 어떻게 적용할 수 있을까요? '
          '구체적인 행동 하나를 정해보세요. '
          '$reference의 말씀을 마음에 품고 하루를 시작해 보세요.';
    } else {
      return '말씀에 대해 깊이 고민하시는 모습이 아름답습니다. '
          '더 구체적으로 어떤 은혜를 나누고 싶으신가요? '
          '기도, 감사, 혹은 삶의 변화 등 다양한 관점에서 묵상해 보세요.';
    }
  }
}
