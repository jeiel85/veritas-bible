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
}
