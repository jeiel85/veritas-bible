import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import '../models/bible.dart';
import '../models/bible_metadata.dart';
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
      // 1. 등록된 번역본 목록 로드 시도
      final List<String> translationFiles = ['assets/bible_krv.json', 'assets/bible_kjv.json'];
      
      for (String filePath in translationFiles) {
        String response;
        try {
          response = await rootBundle.loadString(filePath);
        } catch (e) {
          debugPrint("Skipping optional bible file: $filePath");
          continue;
        }

        final data = json.decode(response);
        final translation = data['translation'] ?? 'UNKNOWN';

        // 이미 해당 번역본 데이터가 있는지 확인
        final db = await _dbHelper.database;
        var count = Sqflite.firstIntValue(await db.rawQuery(
          'SELECT COUNT(*) FROM verses WHERE translation = ?', [translation]
        ));

        if (count == null || count == 0) {
          List<Map<String, dynamic>> versesToInsert = [];
          for (var book in data['books']) {
            for (var verse in book['verses']) {
              versesToInsert.add({
                'translation': translation,
                'book_name': book['name'],
                'chapter': verse['chapter'],
                'verse': verse['verse'],
                'text': verse['text']
              });
            }
          }
          await _dbHelper.insertVerses(versesToInsert);
          debugPrint("Successfully imported $translation from $filePath");
        }
      }
    } catch (e) {
      debugPrint("Error initializing Bible data: $e");
    } finally {
      isLoading = false;
      notifyListeners();
    }
  }

  // 특정 권/장의 구절 가져오기
  Future<List<Verse>> getVerses(String bookName, int chapter, {String translation = 'KRV'}) async {
    final verses = await _dbHelper.getVerses(bookName, chapter, translation: translation);
    if (verses.isEmpty) {
      // 실시간으로 본문이 없는 경우에도 기본 텍스트 반환
      return [
        Verse(
          chapter: chapter, 
          verse: 1, 
          text: "$bookName $chapter장 ($translation) 본문 데이터가 현재 데이터베이스에 없습니다.",
          translation: translation,
        )
      ];
    }
    return verses;
  }

  // 검색
  Future<List<Map<String, dynamic>>> search(String query) async {
    if (query.length < 2) return [];
    return await _dbHelper.searchVerses(query);
  }

  // 하이라이트 저장
  Future<void> saveHighlight(String bookName, int chapter, int verse, String color) async {
    await _dbHelper.saveHighlight(bookName, chapter, verse, color);
    notifyListeners();
  }

  // 하이라이트 가져오기
  Future<Map<int, String>> getHighlights(String bookName, int chapter) async {
    return await _dbHelper.getHighlights(bookName, chapter);
  }

  // 북마크 저장
  Future<void> toggleBookmark(String bookName, int chapter, int verse) async {
    final db = await _dbHelper.database;
    final List<Map<String, dynamic>> existing = await db.query(
      'bookmarks',
      where: 'book_name = ? AND chapter = ? AND verse = ?',
      whereArgs: [bookName, chapter, verse],
    );

    if (existing.isEmpty) {
      await db.insert('bookmarks', {
        'book_name': bookName,
        'chapter': chapter,
        'verse': verse,
      });
    } else {
      await db.delete(
        'bookmarks',
        where: 'book_name = ? AND chapter = ? AND verse = ?',
        whereArgs: [bookName, chapter, verse],
      );
    }
    notifyListeners();
  }

  // 북마크 여부 확인
  Future<bool> isBookmarked(String bookName, int chapter, int verse) async {
    final db = await _dbHelper.database;
    final List<Map<String, dynamic>> existing = await db.query(
      'bookmarks',
      where: 'book_name = ? AND chapter = ? AND verse = ?',
      whereArgs: [bookName, chapter, verse],
    );
    return existing.isNotEmpty;
  }

  // 메모 저장
  Future<void> saveNote(String bookName, int chapter, int verse, String content) async {
    await _dbHelper.saveNote(bookName, chapter, verse, content);
    notifyListeners();
  }

  // 특정 구절 메모 가져오기
  Future<String?> getNote(String bookName, int chapter, int verse) async {
    return await _dbHelper.getNote(bookName, chapter, verse);
  }

  // 전체 개인 데이터 조회
  Future<List<Map<String, dynamic>>> getAllBookmarks() => _dbHelper.getAllBookmarks();
  Future<List<Map<String, dynamic>>> getAllHighlights() => _dbHelper.getAllHighlights();
  Future<List<Map<String, dynamic>>> getAllNotes() => _dbHelper.getAllNotes();

  // 통독 계획 관련
  Future<void> createReadingPlan(String title, String description, List<Map<String, dynamic>> days) async {
    int planId = await _dbHelper.createReadingPlan(title, description, days.length);
    List<Map<String, dynamic>> planDays = days.map((d) => {
      'plan_id': planId,
      'day': d['day'],
      'book_name': d['book_name'],
      'chapter': d['chapter'],
      'is_completed': 0,
    }).toList();
    await _dbHelper.insertPlanDays(planDays);
    notifyListeners();
  }

  Future<List<Map<String, dynamic>>> getActivePlans() => _dbHelper.getActivePlans();
  Future<List<Map<String, dynamic>>> getPlanDays(int planId) => _dbHelper.getPlanDays(planId);
  
  Future<void> updatePlanProgress(int progressId, bool completed) async {
    await _dbHelper.updateProgress(progressId, completed);
    notifyListeners();
  }

  // 스트릭 및 활동 로그 관련
  Future<void> logReadActivity() async {
    await _dbHelper.logActivity();
    notifyListeners();
  }

  Future<int> getStreak() async {
    final logs = await _dbHelper.getActivityLogs();
    if (logs.isEmpty) return 0;

    int streak = 0;
    DateTime today = DateTime.now();
    DateTime checkDate = DateTime(today.year, today.month, today.day);

    // 로그는 날짜 역순(최신순)으로 정렬되어 있음
    for (var log in logs) {
      DateTime logDate = DateTime.parse(log['activity_date']);
      DateTime compareDate = DateTime(logDate.year, logDate.month, logDate.day);

      if (compareDate == checkDate) {
        streak++;
        checkDate = checkDate.subtract(const Duration(days: 1));
      } else if (compareDate.isBefore(checkDate)) {
        // 중간에 끊김
        break;
      }
    }
    return streak;
  }

  Future<List<Map<String, dynamic>>> getRecentActivity() => _dbHelper.getActivityLogs();
}
