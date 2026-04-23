import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/bible.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;

  factory DatabaseHelper() => _instance;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'bible.db');
    return await openDatabase(
      path,
      version: 4, // 메모(notes) 기능 추가를 위해 버전 업그레이드
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE verses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            translation TEXT DEFAULT 'KRV',
            book_id INTEGER,
            book_name TEXT,
            chapter INTEGER,
            verse INTEGER,
            text TEXT
          )
        ''');
        await db.execute('''
          CREATE TABLE highlights (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            book_name TEXT,
            chapter INTEGER,
            verse INTEGER,
            color TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
          )
        ''');
        await db.execute('''
          CREATE TABLE bookmarks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            book_name TEXT,
            chapter INTEGER,
            verse INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
          )
        ''');
        await db.execute('''
          CREATE TABLE notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            book_name TEXT,
            chapter INTEGER,
            verse INTEGER,
            content TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
          )
        ''');
        await db.execute('CREATE INDEX idx_text ON verses(text)');
        await db.execute('CREATE INDEX idx_translation ON verses(translation)');
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        if (oldVersion < 2) {
          await db.execute('ALTER TABLE verses ADD COLUMN translation TEXT DEFAULT "KRV"');
          await db.execute('CREATE INDEX idx_translation ON verses(translation)');
        }
        if (oldVersion < 3) {
          await db.execute('''CREATE TABLE highlights (id INTEGER PRIMARY KEY AUTOINCREMENT, book_name TEXT, chapter INTEGER, verse INTEGER, color TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)''');
          await db.execute('''CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT, book_name TEXT, chapter INTEGER, verse INTEGER, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)''');
        }
        if (oldVersion < 4) {
          await db.execute('''
            CREATE TABLE notes (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              book_name TEXT,
              chapter INTEGER,
              verse INTEGER,
              content TEXT,
              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
          ''');
        }
      },
    );
  }

  // 하이라이트 추가/업데이트
  Future<void> saveHighlight(String bookName, int chapter, int verse, String color) async {
    final db = await database;
    await db.insert(
      'highlights',
      {
        'book_name': bookName,
        'chapter': chapter,
        'verse': verse,
        'color': color,
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // 하이라이트 가져오기 (특정 장 전체)
  Future<Map<int, String>> getHighlights(String bookName, int chapter) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'highlights',
      where: 'book_name = ? AND chapter = ?',
      whereArgs: [bookName, chapter],
    );

    return {for (var m in maps) m['verse']: m['color']};
  }

  // 메모 저장/업데이트
  Future<void> saveNote(String bookName, int chapter, int verse, String content) async {
    final db = await database;
    await db.insert(
      'notes',
      {
        'book_name': bookName,
        'chapter': chapter,
        'verse': verse,
        'content': content,
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // 특정 구절의 메모 가져오기
  Future<String?> getNote(String bookName, int chapter, int verse) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'notes',
      where: 'book_name = ? AND chapter = ? AND verse = ?',
      whereArgs: [bookName, chapter, verse],
    );
    return maps.isNotEmpty ? maps.first['content'] : null;
  }

  // 모든 북마크 목록 가져오기 (전체 성경 중)
  Future<List<Map<String, dynamic>>> getAllBookmarks() async {
    final db = await database;
    return await db.query('bookmarks', orderBy: 'created_at DESC');
  }

  // 모든 하이라이트 목록 가져오기
  Future<List<Map<String, dynamic>>> getAllHighlights() async {
    final db = await database;
    return await db.query('highlights', orderBy: 'created_at DESC');
  }

  // 모든 메모 목록 가져오기
  Future<List<Map<String, dynamic>>> getAllNotes() async {
    final db = await database;
    return await db.query('notes', orderBy: 'created_at DESC');
  }

  // 특정 장의 절 가져오기 (번역본별)
  Future<List<Verse>> getVerses(String bookName, int chapter, {String translation = 'KRV'}) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'verses',
      where: 'book_name = ? AND chapter = ? AND translation = ?',
      whereArgs: [bookName, chapter, translation],
      orderBy: 'verse ASC',
    );

    return List.generate(maps.length, (i) {
      return Verse(
        chapter: maps[i]['chapter'],
        verse: maps[i]['verse'],
        text: maps[i]['text'],
        translation: maps[i]['translation'],
      );
    });
  }

  // 초기 데이터 삽입
  Future<void> insertVerses(List<Map<String, dynamic>> verses) async {
    final db = await database;
    Batch batch = db.batch();
    for (var v in verses) {
      batch.insert('verses', v);
    }
    await batch.commit(noResult: true);
  }

  // 전체 검색
  Future<List<Map<String, dynamic>>> searchVerses(String query) async {
    final db = await database;
    return await db.query(
      'verses',
      where: 'text LIKE ?',
      whereArgs: ['%$query%'],
      limit: 100, // 최대 100개까지만
    );
  }
  
  // 데이터 존재 여부 확인
  Future<bool> hasData() async {
    final db = await database;
    var count = Sqflite.firstIntValue(await db.rawQuery('SELECT COUNT(*) FROM verses'));
    return count != null && count > 0;
  }
}
