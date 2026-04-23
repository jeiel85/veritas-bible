class Verse {
  final int chapter;
  final int verse;
  final String text;
  final String? translation; // 번역본 정보 (예: KRV, KJV)

  Verse({
    required this.chapter,
    required this.verse,
    required this.text,
    this.translation,
  });

  factory Verse.fromJson(Map<String, dynamic> json, {String? translation}) {
    return Verse(
      chapter: json['chapter'],
      verse: json['verse'],
      text: json['text'],
      translation: translation ?? json['translation'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'chapter': chapter,
      'verse': verse,
      'text': text,
      'translation': translation,
    };
  }
}

class Note {
  final int? id;
  final String bookName;
  final int chapter;
  final int verse;
  final String content;
  final DateTime createdAt;

  Note({
    this.id,
    required this.bookName,
    required this.chapter,
    required this.verse,
    required this.content,
    required this.createdAt,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'book_name': bookName,
      'chapter': chapter,
      'verse': verse,
      'content': content,
      'created_at': createdAt.toIso8601String(),
    };
  }

  factory Note.fromMap(Map<String, dynamic> map) {
    return Note(
      id: map['id'],
      bookName: map['book_name'],
      chapter: map['chapter'],
      verse: map['verse'],
      content: map['content'],
      createdAt: DateTime.parse(map['created_at']),
    );
  }
}

class ReadingPlan {
  final int? id;
  final String title;
  final String description;
  final int totalDays;
  final DateTime startDate;

  ReadingPlan({
    this.id,
    required this.title,
    required this.description,
    required this.totalDays,
    required this.startDate,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'total_days': totalDays,
      'start_date': startDate.toIso8601String(),
    };
  }

  factory ReadingPlan.fromMap(Map<String, dynamic> map) {
    return ReadingPlan(
      id: map['id'],
      title: map['title'],
      description: map['description'],
      totalDays: map['total_days'],
      startDate: DateTime.parse(map['start_date']),
    );
  }
}

class PlanDay {
  final int id;
  final int planId;
  final int day;
  final String bookName;
  final int chapter;
  final bool isCompleted;

  PlanDay({
    required this.id,
    required this.planId,
    required this.day,
    required this.bookName,
    required this.chapter,
    this.isCompleted = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'plan_id': planId,
      'day': day,
      'book_name': bookName,
      'chapter': chapter,
      'is_completed': isCompleted ? 1 : 0,
    };
  }

  factory PlanDay.fromMap(Map<String, dynamic> map) {
    return PlanDay(
      id: map['id'],
      planId: map['plan_id'],
      day: map['day'],
      bookName: map['book_name'],
      chapter: map['chapter'],
      isCompleted: map['is_completed'] == 1,
    );
  }
}

class Book {
  final String name;
  final List<Verse> verses;

  Book({required this.name, required this.verses});

  factory Book.fromJson(Map<String, dynamic> json) {
    var list = json['verses'] as List;
    List<Verse> verseList = list.map((i) => Verse.fromJson(i)).toList();

    return Book(
      name: json['name'],
      verses: verseList,
    );
  }
}

class Bible {
  final String translation;
  final List<Book> books;

  Bible({required this.translation, required this.books});

  factory Bible.fromJson(Map<String, dynamic> json) {
    var list = json['books'] as List;
    List<Book> bookList = list.map((i) => Book.fromJson(i)).toList();

    return Bible(
      translation: json['translation'],
      books: bookList,
    );
  }
}
