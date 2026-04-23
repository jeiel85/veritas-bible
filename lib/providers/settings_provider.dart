import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsProvider with ChangeNotifier {
  bool _isDarkMode = false;
  double _fontSize = 18.0;
  double _lineHeight = 1.6; // 기본 행간
  String _fontFamily = 'Sans-serif'; // Sans-serif(고딕), Serif(명조)
  double _ttsSpeed = 0.5;
  Color _themeColor = const Color(0xFF1A237E);

  bool get isDarkMode => _isDarkMode;
  double get fontSize => _fontSize;
  double get lineHeight => _lineHeight;
  String get fontFamily => _fontFamily;
  double get ttsSpeed => _ttsSpeed;
  Color get themeColor => _themeColor;

  SettingsProvider() {
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    _isDarkMode = prefs.getBool('isDarkMode') ?? false;
    _fontSize = prefs.getDouble('fontSize') ?? 18.0;
    _lineHeight = prefs.getDouble('lineHeight') ?? 1.6;
    _fontFamily = prefs.getString('fontFamily') ?? 'Sans-serif';
    _ttsSpeed = prefs.getDouble('ttsSpeed') ?? 0.5;
    int? colorValue = prefs.getInt('themeColor');
    if (colorValue != null) {
      _themeColor = Color(colorValue);
    }
    notifyListeners();
  }

  Future<void> toggleTheme() async {
    _isDarkMode = !_isDarkMode;
    SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool('isDarkMode', _isDarkMode);
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
