import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/notification_service.dart';

class SettingsProvider with ChangeNotifier {
  final NotificationService _notificationService = NotificationService();
  bool _isDarkMode = false;
  bool _isInitialized = false; // 데이터 초기화 완료 여부
  double _fontSize = 18.0;
  double _lineHeight = 1.6; // 기본 행간
  String _fontFamily = 'Sans-serif'; // Sans-serif(고딕), Serif(명조)
  double _ttsSpeed = 0.5;
  Color _themeColor = const Color(0xFF1A237E);
  
  // 알림 관련 (Issue #30)
  bool _isNotificationEnabled = false;
  String _notificationTime = '08:00';

  bool get isDarkMode => _isDarkMode;
  bool get isInitialized => _isInitialized;
  double get fontSize => _fontSize;
  double get lineHeight => _lineHeight;
  String get fontFamily => _fontFamily;
  double get ttsSpeed => _ttsSpeed;
  Color get themeColor => _themeColor;
  bool get isNotificationEnabled => _isNotificationEnabled;
  String get notificationTime => _notificationTime;

  SettingsProvider() {
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    _isDarkMode = prefs.getBool('isDarkMode') ?? false;
    _isInitialized = prefs.getBool('isInitialized') ?? false;
    _fontSize = prefs.getDouble('fontSize') ?? 18.0;
    _lineHeight = prefs.getDouble('lineHeight') ?? 1.6;
    _fontFamily = prefs.getString('fontFamily') ?? 'Sans-serif';
    _ttsSpeed = prefs.getDouble('ttsSpeed') ?? 0.5;
    _isNotificationEnabled = prefs.getBool('isNotificationEnabled') ?? false;
    _notificationTime = prefs.getString('notificationTime') ?? '08:00';
    int? colorValue = prefs.getInt('themeColor');
    if (colorValue != null) {
      _themeColor = Color(colorValue);
    }
    notifyListeners();
  }

  Future<void> toggleNotification(bool value) async {
    _isNotificationEnabled = value;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool('isNotificationEnabled', value);
    
    if (value) {
      // 알림 활성화 시 권한 요청 및 스케줄링
      final hasPermission = await _notificationService.requestPermissions();
      if (hasPermission) {
        await _scheduleDailyNotification();
      } else {
        _isNotificationEnabled = false;
        prefs.setBool('isNotificationEnabled', false);
      }
    } else {
      // 알림 비활성화 시 취소
      await _notificationService.cancelDailyNotification();
    }
    
    notifyListeners();
  }
  
  /// 알림 스케줄링
  Future<void> _scheduleDailyNotification() async {
    final timeParts = _notificationTime.split(':');
    final hour = int.parse(timeParts[0]);
    final minute = int.parse(timeParts[1]);
    
    await _notificationService.scheduleDailyVerseNotification(
      title: '오늘의 말씀',
      body: '새로운 말씀으로 하루를 시작하세요 📖',
      time: TimeOfDay(hour: hour, minute: minute),
    );
  }

  Future<void> updateNotificationTime(String time) async {
    _notificationTime = time;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString('notificationTime', time);
    
    // 알림이 활성화되어 있으면 새 시간으로 재스케줄링
    if (_isNotificationEnabled) {
      await _scheduleDailyNotification();
    }
    
    notifyListeners();
  }

  Future<void> toggleTheme() async {
    _isDarkMode = !_isDarkMode;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool('isDarkMode', _isDarkMode);
    notifyListeners();
  }

  Future<void> setInitialized(bool value) async {
    _isInitialized = value;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool('isInitialized', value);
    notifyListeners();
  }

  Future<void> updateFontSize(double newSize) async {
    if (newSize >= 10.0 && newSize <= 40.0) {
      _fontSize = newSize;
      SharedPreferences prefs = await SharedPreferences.getInstance();
      prefs.setDouble('fontSize', _fontSize);
      notifyListeners();
    }
  }

  Future<void> updateLineHeight(double newHeight) async {
    if (newHeight >= 1.0 && newHeight <= 3.0) {
      _lineHeight = newHeight;
      SharedPreferences prefs = await SharedPreferences.getInstance();
      prefs.setDouble('lineHeight', _lineHeight);
      notifyListeners();
    }
  }

  Future<void> updateFontFamily(String family) async {
    _fontFamily = family;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setString('fontFamily', family);
    notifyListeners();
  }

  Future<void> updateTtsSpeed(double newSpeed) async {
    if (newSpeed >= 0.1 && newSpeed <= 2.0) {
      _ttsSpeed = newSpeed;
      SharedPreferences prefs = await SharedPreferences.getInstance();
      prefs.setDouble('ttsSpeed', _ttsSpeed);
      notifyListeners();
    }
  }

  Future<void> updateThemeColor(Color newColor) async {
    _themeColor = newColor;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setInt('themeColor', newColor.value);
    notifyListeners();
  }
}
