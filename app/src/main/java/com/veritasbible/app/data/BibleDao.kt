package com.veritasbible.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {
    @Query("SELECT * FROM bible_verses ORDER BY bookId ASC, chapter ASC, verse ASC")
    fun getAllVersesFlow(): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE book = :book AND chapter = :chapter ORDER BY verse ASC")
    fun getVersesByBookChapterFlow(book: String, chapter: Int): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE book = :book AND chapter = :chapter ORDER BY verse ASC")
    suspend fun getVersesByBookChapter(book: String, chapter: Int): List<BibleVerse>

    @Update
    suspend fun updateVerse(verse: BibleVerse)

    @Query("SELECT * FROM bible_verses WHERE text LIKE '%' || :query || '%' OR textEn LIKE '%' || :query || '%' ORDER BY bookId ASC, chapter ASC, verse ASC")
    fun searchVersesFlow(query: String): Flow<List<BibleVerse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerse>)

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun countVerses(): Int

    @Query("DELETE FROM bible_verses")
    suspend fun deleteAllVerses()

    @Query("SELECT DISTINCT book FROM bible_verses ORDER BY bookId ASC")
    fun getAvailableBooksFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT chapter FROM bible_verses WHERE book = :book ORDER BY chapter ASC")
    suspend fun getChaptersByBook(book: String): List<Int>

    /**
     * 책 카탈로그 — ko/en 책 이름 + bookId. 언어 전환과 영어 책 이름 표시에 사용.
     * 절 1개만 골라 책 단위 1 row 가 나오도록 GROUP BY 한다.
     */
    @Query("SELECT book AS ko, bookEn AS en, bookId FROM bible_verses GROUP BY bookId ORDER BY bookId ASC")
    suspend fun getBookCatalog(): List<BookCatalogEntry>

    @Query("SELECT MAX(verse) FROM bible_verses WHERE book = :book AND chapter = :chapter")
    suspend fun getVerseCount(book: String, chapter: Int): Int?

    /** v6: 단락 시작 플래그 일괄 적용. (bookId, chapter, verse) 매칭으로 UPDATE. */
    @Query("UPDATE bible_verses SET paragraphStart = 1 WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun markParagraphStart(bookId: Int, chapter: Int, verse: Int)

    @Query("SELECT COUNT(*) FROM bible_verses WHERE paragraphStart = 1")
    suspend fun countParagraphStarts(): Int
}

/** 책 카탈로그 1행. UI 가 한·영 책 이름을 빠르게 룩업하기 위함. */
data class BookCatalogEntry(
    val ko: String,
    val en: String,
    val bookId: Int
)
