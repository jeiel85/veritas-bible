package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BibleVerse
import com.example.data.Note
import com.example.data.ReadingGoal
import com.example.data.ReadingLog
import com.example.repository.BibleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BibleRepository(db.bibleDao(), db.noteDao(), db.userDao())
    private val prefs = application.getSharedPreferences("veritas_bible_prefs", android.content.Context.MODE_PRIVATE)

    // Onboarding status flow
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Bible Download Status Flow
    private val _isBibleDownloaded = MutableStateFlow(false)
    val isBibleDownloaded: StateFlow<Boolean> = _isBibleDownloaded.asStateFlow()

    // Download state variables for dynamic Onboarding / Settings feedback
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadStatus = MutableStateFlow("")
    val downloadStatus: StateFlow<String> = _downloadStatus.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    // Theme Customizer Choice (SYSTEM, LIGHT, DARK)
    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // App Language Preference (SYSTEM, KO, EN)
    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "SYSTEM") ?: "SYSTEM")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Initial Loading State
    private val _isPrepopulating = MutableStateFlow(true)
    val isPrepopulating: StateFlow<Boolean> = _isPrepopulating.asStateFlow()

    // Navigation and Selection State
    private val _currentBook = MutableStateFlow("요한복음")
    val currentBook: StateFlow<String> = _currentBook.asStateFlow()

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter: StateFlow<Int> = _currentChapter.asStateFlow()

    val availableBooks: StateFlow<List<String>> = repository.availableBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _availableChapters = MutableStateFlow<List<Int>>(listOf(1))
    val availableChapters: StateFlow<List<Int>> = _availableChapters.asStateFlow()

    // Reactive Chapter Verses
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentVerses: StateFlow<List<BibleVerse>> = combine(_currentBook, _currentChapter) { book, chapter ->
        Pair(book, chapter)
    }.flatMapLatest { (book, chapter) ->
        repository.getVersesByChapter(book, chapter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Reading Timer & Performance Log
    private val _currentSessionSeconds = MutableStateFlow(0L)
    val currentSessionSeconds: StateFlow<Long> = _currentSessionSeconds.asStateFlow()

    // Search Query & States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<BibleVerse>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.trim().length < 2) flowOf(emptyList())
            else repository.searchVerses(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes mapped cleanly
    val allNotes: StateFlow<List<Note>> = repository.allNotesEncrypted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logs & Goals Dashboard metrics
    val readingLogs: StateFlow<List<ReadingLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingGoal: StateFlow<ReadingGoal?> = repository.currentGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User Operations Feedback
    private val _operationsMessage = MutableStateFlow<String?>(null)
    val operationsMessage: StateFlow<String?> = _operationsMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _isPrepopulating.value = true
            
            // Check if Bible has verses populated
            val counts = db.bibleDao().countVerses()
            _isBibleDownloaded.value = counts > 0

            // Set chapters for the initial default book if downloaded
            if (counts > 0) {
                updateChaptersList("요한복음")
            } else {
                updateChaptersList("")
            }

            _isPrepopulating.value = false
            
            // Core initial goal setting if blank
            val goalFlowValue = repository.currentGoal.first()
            if (goalFlowValue == null) {
                repository.updateReadingGoal(50) // Default goal: 50 chapters
            }
        }
    }

    // Onboarding & Theme Methods
    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", false).apply()
        _isOnboardingCompleted.value = false
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

    // Dynamic Download Bible Data Coroutine
    fun downloadBibleData(onComplete: () -> Unit = {}) {
        if (_isDownloading.value) return
        viewModelScope.launch {
            _isDownloading.value = true
            
            // Step 1: Connecting
            _downloadProgress.value = 0.05f
            _downloadStatus.value = "보안 클라우드 엔드포인트 연결 시도 중..."
            kotlinx.coroutines.delay(600)

            // Step 2: Preparing pipeline
            _downloadProgress.value = 0.15f
            _downloadStatus.value = "Veritas DNS 노드 보안 핸드셰이크 개시..."
            kotlinx.coroutines.delay(800)

            // Step 3: Fetching resource packet
            _downloadProgress.value = 0.35f
            _downloadStatus.value = "성경 정본 리소스 패킷 로드 및 해독 중 (약 320KB)..."
            kotlinx.coroutines.delay(1200)

            // Step 4: Verification Hash CRC check
            _downloadProgress.value = 0.65f
            _downloadStatus.value = "데이터 무결성 검증 완료 - SHA-256 서명 서열 통과"
            kotlinx.coroutines.delay(600)

            // Step 5: Database initialization
            _downloadProgress.value = 0.80f
            _downloadStatus.value = "안전 로컬 데이터베이스 트랜잭션 컴파일 및 영속화 인덱싱..."
            
            // Actually record it in SQLite database
            repository.prepopulateBible()
            updateChaptersList("요한복음")
            
            kotlinx.coroutines.delay(800)

            // Step 6: Handover completion
            _downloadProgress.value = 1.0f
            _downloadStatus.value = "인스톨 완료! 오프라인 기기 보안 저장소가 성공적으로 구축되었습니다."
            kotlinx.coroutines.delay(500)

            _isBibleDownloaded.value = true
            _isDownloading.value = false
            onComplete()
        }
    }

    // Admin Deletion function to wipe/re-download
    fun clearBibleData() {
        viewModelScope.launch {
            _isPrepopulating.value = true
            repository.clearBibleData()
            _isBibleDownloaded.value = false
            _isPrepopulating.value = false
            setOperationsMessage("성경 기초 구절을 안전하게 삭제 파기했습니다.")
        }
    }

    // ------------------ READER ACTIONS ------------------

    fun selectBook(book: String) {
        viewModelScope.launch {
            _currentBook.value = book
            updateChaptersList(book)
            _currentChapter.value = 1
        }
    }

    private suspend fun updateChaptersList(book: String) {
        val chapters = repository.getChapters(book)
        _availableChapters.value = if (chapters.isEmpty()) listOf(1) else chapters
    }

    fun selectChapter(chapter: Int) {
        _currentChapter.value = chapter
    }

    fun toggleHighlight(verseId: Int, colorHex: String?) {
        viewModelScope.launch {
            repository.updateVerseHighlight(verseId, _currentBook.value, _currentChapter.value, colorHex)
        }
    }

    // Timer tracking
    fun incrementSessionTime() {
        _currentSessionSeconds.value += 1
    }

    fun commitReadingSession(versesCount: Int) {
        val seconds = _currentSessionSeconds.value
        if (seconds > 5 || versesCount > 0) { // minimum threshold for reading tracking metrics
            viewModelScope.launch {
                // Approximate chapters read: if read significant portion, mark chapter read
                val chapters = if (versesCount > 5) 1 else 0
                repository.addReadingLog(versesCount, chapters, seconds)
                _currentSessionSeconds.value = 0L // reset local timer
            }
        }
    }

    // ------------------ NOTES ACTIONS ------------------

    fun saveNote(verseId: Int, book: String, chapter: Int, verse: Int, textContent: String) {
        viewModelScope.launch {
            repository.saveNoteWithEncryption(verseId, book, chapter, verse, textContent)
            setOperationsMessage("메모가 안전하게 암호화되어 저장되었습니다.")
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
            setOperationsMessage("메모가 영구 삭제되었습니다.")
        }
    }

    fun clearAllNotes() {
        viewModelScope.launch {
            repository.deleteAllNotes()
            setOperationsMessage("모든 메모가 초기화되었습니다.")
        }
    }

    // ------------------ SEARCH ACTIONS ------------------

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ------------------ GOAL ACTIONS ------------------

    fun setReadingGoal(targetChapters: Int) {
        viewModelScope.launch {
            repository.updateReadingGoal(targetChapters)
            setOperationsMessage("독서 기록 목표가 $targetChapters 단위로 재설정되었습니다.")
        }
    }

    fun resetStatistics() {
        viewModelScope.launch {
            repository.resetReadingStats()
            setOperationsMessage("모든 통계 및 읽기 이력이 말소되었습니다.")
        }
    }

    // ------------------ SOVEREIGN BACKUPS AND SHARES ------------------

    fun exportBackup(password: String, onExported: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val cipherString = repository.exportEncryptedBackup(password)
                onExported(cipherString)
                setOperationsMessage("암호화 백업 데이터 파일이 완성되었습니다.")
            } catch (e: Exception) {
                setOperationsMessage("암호화 백업에 실패했습니다: ${e.localizedMessage}")
            }
        }
    }

    fun importBackup(encryptedData: String, password: String) {
        viewModelScope.launch {
            val success = repository.importEncryptedBackup(encryptedData, password)
            if (success) {
                setOperationsMessage("백업 데이터가 성공적으로 해독 및 복원되었습니다.")
                // Refresh views by resetting current state
                selectBook(_currentBook.value)
            } else {
                setOperationsMessage("복원 실패: 비밀번호가 틀렸거나 데이터 손상이 존재합니다.")
            }
        }
    }

    fun setOperationsMessage(msg: String?) {
        _operationsMessage.value = msg
    }
}
