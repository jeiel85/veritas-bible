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
      bool hasData = await _dbHelper.hasData();
      if (!hasData) {
        // 1. 실제 성경 JSON 데이터 로드 (개역한글 66권 전체)
        // 만약 bible_krv.json이 없으면 sample_bible.json으로 대체 시도
        String response;
        try {
          response = await rootBundle.loadString('assets/bible_krv.json');
        } catch (e) {
          response = await rootBundle.loadString('assets/sample_bible.json');
        }
        
        final data = json.decode(response);
        List<Map<String, dynamic>> versesToInsert = [];
        
        final translation = data['translation'] ?? 'KRV';
        
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

        // 2. 대용량 벌크 인서트 수행 (Batch 사용)
        await _dbHelper.insertVerses(versesToInsert);
        debugPrint("Successfully imported full Bible data: $translation");
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
}
