import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import '../models/bible.dart';
import '../services/database_helper.dart';

class BibleProvider with ChangeNotifier {
  final DatabaseHelper _dbHelper = DatabaseHelper();
  bool isLoading = true;

  BibleProvider() {
    _initBibleData();
  }

  Future<void> _initBibleData() async {
    isLoading = true;
    notifyListeners();

    try {
      bool hasData = await _dbHelper.hasData();
      if (!hasData) {
        // 데이터가 없으면 JSON을 읽어 SQLite로 마이그레이션
        final String response = await rootBundle.loadString('assets/sample_bible.json');
        final data = json.decode(response);
        
        List<Map<String, dynamic>> versesToInsert = [];
        for (var book in data['books']) {
          for (var verse in book['verses']) {
            versesToInsert.add({
              'book_name': book['name'],
              'chapter': verse['chapter'],
              'verse': verse['verse'],
              'text': verse['text']
            });
          }
        }
        await _dbHelper.insertVerses(versesToInsert);
        debugPrint("Successfully migrated JSON to SQLite");
      }
    } catch (e) {
      debugPrint("Error initializing Bible data: $e");
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  // 특정 권/장의 구절 가져오기
  Future<List<Verse>> getVerses(String bookName, int chapter) async {
    return await _dbHelper.getVerses(bookName, chapter);
  }

  // 검색
  Future<List<Map<String, dynamic>>> search(String query) async {
    if (query.length < 2) return [];
    return await _dbHelper.searchVerses(query);
  }
}
