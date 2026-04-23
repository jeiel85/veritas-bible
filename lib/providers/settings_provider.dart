import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsProvider with ChangeNotifier {
  bool _isDarkMode = false;
  double _fontSize = 18.0;
  double _ttsSpeed = 0.5; // 기본 속도
  Color _themeColor = const Color(0xFF1A237E); // 기본 Deep Navy

  bool get isDarkMode => _isDarkMode;
  double get fontSize => _fontSize;
  double get ttsSpeed => _ttsSpeed;
  Color get themeColor => _themeColor;

  SettingsProvider() {
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    _isDarkMode = prefs.getBool('isDarkMode') ?? false;
    _fontSize = prefs.getDouble('fontSize') ?? 18.0;
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
