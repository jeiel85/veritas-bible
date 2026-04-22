import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import '../models/bible.dart';

class BibleProvider with ChangeNotifier {
  Bible? bible;
  bool isLoading = true;

  BibleProvider() {
    loadBible();
  }

  Future<void> loadBible() async {
    try {
      final String response = await rootBundle.loadString('assets/sample_bible.json');
      final data = await json.decode(response);
      bible = Bible.fromJson(data);
    } catch (e) {
      debugPrint("Error loading Bible: $e");
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }
}
