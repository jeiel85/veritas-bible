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
        // 1. 샘플 JSON 데이터 로드
        final String response = await rootBundle.loadString('assets/sample_bible.json');
        final data = json.decode(response);
        
        List<Map<String, dynamic>> versesToInsert = [];
        
        // JSON 데이터 처리
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

        // 2. 누락된 성경 권들에 대해 기본 데이터 생성 (앱이 비어 보이지 않도록 함)
        // 실제 운영 시에는 전체 성경 DB 파일을 배포하는 것이 좋음
        for (var bookInfo in allBibleBooks) {
          bool exists = versesToInsert.any((v) => v['book_name'] == bookInfo.name);
          if (!exists) {
            // 각 권의 1장 1절만이라도 기본 텍스트 생성
            for (int ch = 1; ch <= bookInfo.chapterCount; ch++) {
              versesToInsert.add({
                'book_name': bookInfo.name,
                'chapter': ch,
                'verse': 1,
                'text': "${bookInfo.name} $ch장 본문 데이터는 준비 중입니다. (오프라인 모드)"
              });
            }
          }
        }

        await _dbHelper.insertVerses(versesToInsert);
        debugPrint("Successfully migrated and supplemented Bible data");
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
}
