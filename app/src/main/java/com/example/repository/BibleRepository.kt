package com.example.repository

import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BibleRepository(
    private val bibleDao: BibleDao,
    private val noteDao: NoteDao,
    private val userDao: UserDao
) {
    // Bible Flows & Functions
    val allVerses: Flow<List<BibleVerse>> = bibleDao.getAllVersesFlow().flowOn(Dispatchers.IO)
    val availableBooks: Flow<List<String>> = bibleDao.getAvailableBooksFlow().flowOn(Dispatchers.IO)

    fun getVersesByChapter(book: String, chapter: Int): Flow<List<BibleVerse>> {
        return bibleDao.getVersesByBookChapterFlow(book, chapter).flowOn(Dispatchers.IO)
    }

    suspend fun getChapters(book: String): List<Int> = withContext(Dispatchers.IO) {
        bibleDao.getChaptersByBook(book)
    }

    suspend fun updateVerseHighlight(id: Int, book: String, chapter: Int, colorHex: String?) = withContext(Dispatchers.IO) {
        val chapterVerses = bibleDao.getVersesByBookChapter(book, chapter)
        val target = chapterVerses.find { it.id == id }
        if (target != null) {
            target.highlightColor = colorHex
            bibleDao.updateVerse(target)
        }
    }

    fun searchVerses(query: String): Flow<List<BibleVerse>> {
        return bibleDao.searchVersesFlow(query).flowOn(Dispatchers.IO)
    }

    suspend fun ensurePrepopulated() = withContext(Dispatchers.IO) {
        BibleDataPrepopulator.prepopulateIfEmpty(bibleDao)
    }

    suspend fun clearBibleData() = withContext(Dispatchers.IO) {
        bibleDao.deleteAllVerses()
    }

    suspend fun prepopulateBible() = withContext(Dispatchers.IO) {
        BibleDataPrepopulator.prepopulateIfEmpty(bibleDao)
    }

    // Encrypted Notes Flows & Functions
    val allNotesEncrypted: Flow<List<Note>> = noteDao.getAllNotesFlow().flowOn(Dispatchers.IO)

    fun getNotesForVerse(verseId: Int): Flow<List<Note>> {
        return noteDao.getNotesForVerseFlow(verseId).flowOn(Dispatchers.IO)
    }

    suspend fun saveNoteWithEncryption(verseId: Int, book: String, chapter: Int, verse: Int, contentText: String) = withContext(Dispatchers.IO) {
        val encryptedContent = CryptoUtils.encrypt(contentText)
        val note = Note(
            verseId = verseId,
            book = book,
            chapter = chapter,
            verse = verse,
            content = encryptedContent
        )
        noteDao.insertNote(note)
    }

    suspend fun deleteNoteById(noteId: Int) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        noteDao.deleteAllNotes()
    }

    // Reading Habit Statistics Logs and Goals
    val allLogs: Flow<List<ReadingLog>> = userDao.getLogsFlow().flowOn(Dispatchers.IO)
    val currentGoal: Flow<ReadingGoal?> = userDao.getGoalFlow().flowOn(Dispatchers.IO)

    suspend fun addReadingLog(versesCount: Int, chaptersCount: Int, durationSec: Long) = withContext(Dispatchers.IO) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val existingLogs = userDao.getAllLogs()
        val existingToday = existingLogs.find { it.dateString == today }

        if (existingToday != null) {
            val updated = existingToday.copy(
                countVerses = existingToday.countVerses + versesCount,
                countChapters = existingToday.countChapters + chaptersCount,
                sessionDurationSec = existingToday.sessionDurationSec + durationSec
            )
            userDao.insertLog(updated)
        } else {
            val newLog = ReadingLog(
                dateString = today,
                countVerses = versesCount,
                countChapters = chaptersCount,
                sessionDurationSec = durationSec
            )
            userDao.insertLog(newLog)
        }

        // Auto increment goal progress if active
        val goal = userDao.getGoal()
        if (goal != null && goal.lastActiveDateString != today) {
            val updatedGoal = goal.copy(
                completedChapters = goal.completedChapters + chaptersCount,
                lastActiveDateString = today
            )
            userDao.updateGoal(updatedGoal)
        } else if (goal != null) {
            val updatedGoal = goal.copy(
                completedChapters = goal.completedChapters + chaptersCount
            )
            userDao.updateGoal(updatedGoal)
        }
    }

    suspend fun updateReadingGoal(targetChapters: Int) = withContext(Dispatchers.IO) {
        val existing = userDao.getGoal()
        if (existing != null) {
            userDao.updateGoal(existing.copy(targetChapters = targetChapters))
        } else {
            userDao.insertGoal(ReadingGoal(targetChapters = targetChapters, completedChapters = 0))
        }
    }

    suspend fun resetReadingStats() = withContext(Dispatchers.IO) {
        userDao.deleteAllLogs()
        val existing = userDao.getGoal()
        if (existing != null) {
            userDao.updateGoal(existing.copy(completedChapters = 0, lastActiveDateString = ""))
        }
    }

    // ------------------ SOVEREIGN DATA EXPORT / IMPORT BACKUPS ------------------

    /**
     * Packages notes, highlights, goal progress, and statistics logs into a JSON
     * payload and AES-encrypts the entire payload using the user's custom backup password.
     */
    suspend fun exportEncryptedBackup(password: String): String = withContext(Dispatchers.IO) {
        val backupObj = JSONObject()

        // 1. Export Notes
        val notesList = noteDao.getAllNotes()
        val notesArr = JSONArray()
        for (n in notesList) {
            val nObj = JSONObject()
            nObj.put("verseId", n.verseId)
            nObj.put("book", n.book)
            nObj.put("chapter", n.chapter)
            nObj.put("verse", n.verse)
            nObj.put("content", n.content) // Keeps AES encryption or decrypted string securely combined
            nObj.put("createdAt", n.createdAt)
            notesArr.put(nObj)
        }
        backupObj.put("notes", notesArr)

        // 2. Export Highlights
        val allVersesList = bibleDao.getAllVersesFlow().first()
        val highlighted = allVersesList.filter { it.highlightColor != null }
        val highlightsArr = JSONArray()
        for (v in highlighted) {
            val hObj = JSONObject()
            hObj.put("book", v.book)
            hObj.put("chapter", v.chapter)
            hObj.put("verse", v.verse)
            hObj.put("color", v.highlightColor)
            highlightsArr.put(hObj)
        }
        backupObj.put("highlights", highlightsArr)

        // 3. Export Logs
        val logsList = userDao.getAllLogs()
        val logsArr = JSONArray()
        for (l in logsList) {
            val lObj = JSONObject()
            lObj.put("dateString", l.dateString)
            lObj.put("countVerses", l.countVerses)
            lObj.put("countChapters", l.countChapters)
            lObj.put("duration", l.sessionDurationSec)
            logsArr.put(lObj)
        }
        backupObj.put("logs", logsArr)

        // 4. Export Goals
        val goal = userDao.getGoal()
        if (goal != null) {
            val gObj = JSONObject()
            gObj.put("target", goal.targetChapters)
            gObj.put("completed", goal.completedChapters)
            gObj.put("lastActive", goal.lastActiveDateString)
            backupObj.put("goal", gObj)
        }

        // Encrypt the complete package using user-defined backup password
        val jsonPayload = backupObj.toString()
        CryptoUtils.encrypt(jsonPayload, password)
    }

    /**
     * Decrypts high-security payload using user's password and restores it in room database,
     * overriding current states cleanly.
     */
    suspend fun importEncryptedBackup(encryptedPayload: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val decryptedPayload = CryptoUtils.decrypt(encryptedPayload, password)
            if (decryptedPayload.isEmpty() || !decryptedPayload.startsWith("{")) {
                return@withContext false // Fail cryptography signature match
            }

            val backupObj = JSONObject(decryptedPayload)

            // 1. Restore Notes
            if (backupObj.has("notes")) {
                noteDao.deleteAllNotes()
                val notesArr = backupObj.getJSONArray("notes")
                val notesList = mutableListOf<Note>()
                for (i in 0 until notesArr.length()) {
                    val nObj = notesArr.getJSONObject(i)
                    notesList.add(Note(
                        verseId = nObj.getInt("verseId"),
                        book = nObj.getString("book"),
                        chapter = nObj.getInt("chapter"),
                        verse = nObj.getInt("verse"),
                        content = nObj.getString("content"),
                        createdAt = nObj.getLong("createdAt")
                    ))
                }
                noteDao.insertNotes(notesList)
            }

            // 2. Restore Highlights
            // Clear current highlights first
            val currentVerses = bibleDao.getAllVersesFlow().first()
            for (v in currentVerses) {
                if (v.highlightColor != null) {
                    v.highlightColor = null
                    bibleDao.updateVerse(v)
                }
            }
            if (backupObj.has("highlights")) {
                val highlightsArr = backupObj.getJSONArray("highlights")
                for (i in 0 until highlightsArr.length()) {
                    val hObj = highlightsArr.getJSONObject(i)
                    val book = hObj.getString("book")
                    val chapter = hObj.getInt("chapter")
                    val verse = hObj.getInt("verse")
                    val color = hObj.getString("color")

                    val matchedVerses = bibleDao.getVersesByBookChapter(book, chapter)
                    val target = matchedVerses.find { it.verse == verse }
                    if (target != null) {
                        target.highlightColor = color
                        bibleDao.updateVerse(target)
                    }
                }
            }

            // 3. Restore Logs
            if (backupObj.has("logs")) {
                userDao.deleteAllLogs()
                val logsArr = backupObj.getJSONArray("logs")
                val logsList = mutableListOf<ReadingLog>()
                for (i in 0 until logsArr.length()) {
                    val lObj = logsArr.getJSONObject(i)
                    logsList.add(ReadingLog(
                        dateString = lObj.getString("dateString"),
                        countVerses = lObj.getInt("countVerses"),
                        countChapters = lObj.getInt("countChapters"),
                        sessionDurationSec = lObj.getLong("duration")
                    ))
                }
                userDao.insertLogs(logsList)
            }

            // 4. Restore Goals
            if (backupObj.has("goal")) {
                val gObj = backupObj.getJSONObject("goal")
                val goal = ReadingGoal(
                    targetChapters = gObj.getInt("target"),
                    completedChapters = gObj.getInt("completed"),
                    lastActiveDateString = gObj.optString("lastActive", "")
                )
                userDao.insertGoal(goal)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
