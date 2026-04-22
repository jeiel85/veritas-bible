class Verse {
  final int chapter;
  final int verse;
  final String text;

  Verse({required this.chapter, required this.verse, required this.text});

  factory Verse.fromJson(Map<String, dynamic> json) {
    return Verse(
      chapter: json['chapter'],
      verse: json['verse'],
      text: json['text'],
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
