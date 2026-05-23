package com.veritasbible.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_verses")
data class BibleVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val book: String,       // e.g. "요한복음", "창세기", "시편", "로마서"
    val bookEn: String,     // e.g. "John", "Genesis", "Psalms", "Romans"
    val bookId: Int,        // Index of the book
    val chapter: Int,
    val verse: Int,
    val text: String,       // Korean Translation
    val textEn: String,     // English Translation (KJV/WEB)
    var highlightColor: String? = null // Hex code color for visual highlight, or null
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int,       // Foreign key pointing to BibleVerse id
    val book: String,
    val chapter: Int,
    val verse: Int,
    val content: String,    // Clean AES-encrypted Base64 string for private study memos
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_logs")
data class ReadingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,      // YYYY-MM-DD format
    val countVerses: Int,
    val countChapters: Int,       // Count of unique chapters completed
    val sessionDurationSec: Long  // Reading session duration in seconds
)

@Entity(tableName = "reading_goals")
data class ReadingGoal(
    @PrimaryKey val id: Int = 1,  // Single central reading habit target
    val targetChapters: Int = 100,
    val completedChapters: Int = 0,
    val lastActiveDateString: String = ""
)
