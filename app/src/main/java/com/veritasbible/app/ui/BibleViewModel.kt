package com.veritasbible.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.veritasbible.app.data.AppDatabase
import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.data.BookCatalogEntry
import com.veritasbible.app.data.Note
import com.veritasbible.app.data.ReadingGoal
import com.veritasbible.app.data.ReadingLog
import com.veritasbible.app.repository.BibleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = BibleRepository(
        application.applicationContext,
        db.bibleDao(),
        db.noteDao(),
        db.userDao(),
        db
    )
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

    // 한·영 책 이름 카탈로그. 영어 모드에서 책 이름을 영어로 표시할 때 사용.
    private val _bookCatalog = MutableStateFlow<List<BookCatalogEntry>>(emptyList())
    val bookCatalog: StateFlow<List<BookCatalogEntry>> = _bookCatalog.asStateFlow()

    private val _bookKoToEn = MutableStateFlow<Map<String, String>>(emptyMap())
    val bookKoToEn: StateFlow<Map<String, String>> = _bookKoToEn.asStateFlow()

    /** 표시용 책 이름. KO 면 한국어, EN 이면 매핑된 영어(없으면 한국어 fallback). */
    fun displayBook(koBook: String): String {
        val lang = _appLanguage.value
        if (lang != "EN") return koBook
        return _bookKoToEn.value[koBook] ?: koBook
    }

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

            val existing = db.bibleDao().countVerses()
            if (existing == 0) {
                // First launch: stream bundled bible.json into Room with real progress
                _downloadStatus.value = if (_appLanguage.value == "EN") {
                    "Preparing offline scripture library..."
                } else {
                    "오프라인 성경 라이브러리 준비 중..."
                }
                repository.ensurePrepopulated { progress, _ ->
                    _downloadProgress.value = progress
                }
                _downloadProgress.value = 1f
                _downloadStatus.value = ""
            }

            _isBibleDownloaded.value = db.bibleDao().countVerses() > 0
            updateChaptersList("요한복음")
            refreshBookCatalog()
            // v5→v6 마이그레이션 직후 또는 누락된 경우 단락 마커를 채워준다.
            try {
                com.veritasbible.app.data.BibleDataPrepopulator
                    .syncParagraphMarksIfNeeded(application.applicationContext, db.bibleDao())
            } catch (_: Exception) {
                // 단락 표시는 보조 기능이므로 실패해도 앱 동작에 영향 없음.
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

    // Initialize bundled Bible into local Room with real progress
    fun downloadBibleData(onComplete: () -> Unit = {}) {
        if (_isDownloading.value) return
        viewModelScope.launch {
            _isDownloading.value = true
            val isEn = _appLanguage.value == "EN"
            _downloadProgress.value = 0f
            _downloadStatus.value = if (isEn) "Loading bundled scripture..." else "내장 성경 데이터 로딩 중..."

            repository.prepopulateBible { progress, label ->
                _downloadProgress.value = progress
                _downloadStatus.value = if (isEn) {
                    "Importing verses ($label)"
                } else {
                    "구절 가져오는 중 ($label)"
                }
            }

            updateChaptersList("요한복음")
            _downloadProgress.value = 1f
            _downloadStatus.value = if (isEn) {
                "Setup complete. Offline scripture is ready."
            } else {
                "초기화 완료. 오프라인 성경이 준비되었습니다."
            }
            _isBibleDownloaded.value = true
            _isDownloading.value = false
            onComplete()
        }
    }

    // Admin reset to re-import bundled scripture
    fun clearBibleData() {
        viewModelScope.launch {
            _isPrepopulating.value = true
            repository.clearBibleData()
            _isBibleDownloaded.value = false
            _isPrepopulating.value = false
            setOperationsMessage("성경 데이터를 초기화했습니다. 재시작 시 다시 가져옵니다.")
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

    private suspend fun refreshBookCatalog() {
        val catalog = repository.getBookCatalog()
        _bookCatalog.value = catalog
        _bookKoToEn.value = catalog.associate { it.ko to it.en }
    }

    suspend fun getVerseCount(book: String, chapter: Int): Int = repository.getVerseCount(book, chapter)

    /** UI 가 특정 책의 장 목록을 즉시 받아야 할 때 사용 (연구 범위 선택 등). */
    suspend fun repositoryChaptersFor(book: String): List<Int> = repository.getChapters(book)

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

    fun importBackup(encryptedData: String, password: String, wipeStudyFirst: Boolean = false) {
        viewModelScope.launch {
            val success = repository.importEncryptedBackup(encryptedData, password, wipeStudyFirst)
            if (success) {
                setOperationsMessage(
                    if (wipeStudyFirst) "백업으로 연구 데이터를 모두 덮어썼습니다."
                    else "백업 데이터가 성공적으로 해독 및 복원되었습니다."
                )
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
