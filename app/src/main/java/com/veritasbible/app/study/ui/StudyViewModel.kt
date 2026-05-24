package com.veritasbible.app.study.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.veritasbible.app.data.AppDatabase
import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.study.data.CharacterTagTarget
import com.veritasbible.app.study.data.PropositionStatus
import com.veritasbible.app.study.data.StudyApplication
import com.veritasbible.app.study.data.StudyCharacterTag
import com.veritasbible.app.study.data.StudyDivision
import com.veritasbible.app.study.data.StudyInterpretation
import com.veritasbible.app.study.data.StudyMarkup
import com.veritasbible.app.study.data.StudyMarkupLink
import com.veritasbible.app.study.data.StudyObservation
import com.veritasbible.app.study.data.StudyOutlineNode
import com.veritasbible.app.study.data.StudyProposition
import com.veritasbible.app.study.data.StudySession
import com.veritasbible.app.study.data.StudyThemeCheck
import com.veritasbible.app.study.repository.StudyRepository
import com.veritasbible.app.study.report.StudyMarkdownExporter
import com.veritasbible.app.study.report.StudyReportSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 연구 모듈 ViewModel.
 *
 * 기존 [com.veritasbible.app.ui.BibleViewModel]을 건드리지 않고
 * 별도 ViewModel로 격리한다. 본문 조회는 같은 Room DB를 공유한다.
 */
class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val bibleDao = db.bibleDao()
    private val repository: StudyRepository = StudyRepository(
        sessionDao = db.studySessionDao(),
        observationDao = db.studyObservationDao(),
        divisionDao = db.studyDivisionDao(),
        applicationDao = db.studyApplicationDao(),
        markupDao = db.studyMarkupDao(),
        markupLinkDao = db.studyMarkupLinkDao(),
        characterTagDao = db.studyCharacterTagDao(),
        interpretationDao = db.studyInterpretationDao(),
        themeCheckDao = db.studyThemeCheckDao(),
        propositionDao = db.studyPropositionDao(),
        outlineNodeDao = db.studyOutlineNodeDao()
    )

    val sessions: StateFlow<List<StudySession>> = repository.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSession: StateFlow<StudySession?> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val observations: StateFlow<List<StudyObservation>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeObservations(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val divisions: StateFlow<List<StudyDivision>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeDivisions(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val applicationNote: StateFlow<StudyApplication?> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeApplication(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val markups: StateFlow<List<StudyMarkup>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeMarkups(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val markupLinks: StateFlow<List<StudyMarkupLink>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeMarkupLinks(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val characterTags: StateFlow<List<StudyCharacterTag>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeCharacterTags(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val interpretations: StateFlow<List<StudyInterpretation>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeInterpretations(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val themeChecks: StateFlow<List<StudyThemeCheck>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeThemeChecks(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val propositions: StateFlow<List<StudyProposition>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observePropositions(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val outlineNodes: StateFlow<List<StudyOutlineNode>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeOutlineNodes(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionVerses: StateFlow<List<BibleVerse>> = selectedSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else flowOf(loadVersesForSession(session))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun selectSession(id: String?) {
        _selectedSessionId.value = id
    }

    fun createSession(
        title: String,
        book: String,
        bookEn: String,
        bookId: Int,
        startChapter: Int,
        startVerse: Int,
        endChapter: Int,
        endVerse: Int,
        onCreated: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val session = repository.createSession(
                    title = title,
                    book = book,
                    bookEn = bookEn,
                    bookId = bookId,
                    startChapter = startChapter,
                    startVerse = startVerse,
                    endChapter = endChapter,
                    endVerse = endVerse
                )
                _message.value = "연구 세션을 만들었습니다."
                onCreated(session.id)
            } catch (e: Exception) {
                _message.value = "연구 세션 생성에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateSessionMeta(
        sessionId: String,
        title: String? = null,
        mainTheme: String? = null,
        mainPropositionMemo: String? = null,
        currentStage: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateSessionMeta(
                    sessionId = sessionId,
                    title = title,
                    mainTheme = mainTheme,
                    mainPropositionMemo = mainPropositionMemo,
                    currentStage = currentStage
                )
            } catch (e: Exception) {
                _message.value = "저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteSession(sessionId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteSession(sessionId)
                if (_selectedSessionId.value == sessionId) _selectedSessionId.value = null
                _message.value = "연구 세션을 삭제했습니다."
                onDeleted()
            } catch (e: Exception) {
                _message.value = "삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun addObservation(sessionId: String, answer: String, question: String = "") {
        if (answer.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addObservation(sessionId, answer.trim(), question)
            } catch (e: Exception) {
                _message.value = "관찰 노트 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun saveObservationTemplate(
        sessionId: String,
        questionKey: String,
        questionText: String,
        answer: String
    ) {
        viewModelScope.launch {
            try {
                repository.upsertObservationTemplate(sessionId, questionKey, questionText, answer.trim())
            } catch (e: Exception) {
                _message.value = "관찰 답안 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateObservation(observation: StudyObservation) {
        viewModelScope.launch {
            try {
                repository.updateObservation(observation)
            } catch (e: Exception) {
                _message.value = "관찰 노트 수정에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteObservation(observation: StudyObservation) {
        viewModelScope.launch {
            try {
                repository.deleteObservation(observation)
            } catch (e: Exception) {
                _message.value = "관찰 노트 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun addDivision(
        sessionId: String,
        title: String,
        startChapter: Int,
        startVerse: Int,
        endChapter: Int,
        endVerse: Int,
        summary: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addDivision(
                    sessionId = sessionId,
                    title = title.trim(),
                    startChapter = startChapter,
                    startVerse = startVerse,
                    endChapter = endChapter,
                    endVerse = endVerse,
                    summary = summary?.takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                _message.value = "단락 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateDivision(division: StudyDivision) {
        viewModelScope.launch {
            try {
                repository.updateDivision(division)
            } catch (e: Exception) {
                _message.value = "단락 수정에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteDivision(division: StudyDivision) {
        viewModelScope.launch {
            try {
                repository.deleteDivision(division.id, division.sessionId)
            } catch (e: Exception) {
                _message.value = "단락 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun saveApplication(
        sessionId: String,
        truth: String,
        mirror: String,
        adjustment: String,
        actionPlan: String,
        dueDate: String? = null,
        practiced: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                repository.saveApplication(
                    sessionId = sessionId,
                    truth = truth.trim(),
                    mirror = mirror.trim(),
                    adjustment = adjustment.trim(),
                    actionPlan = actionPlan.trim(),
                    dueDate = dueDate?.takeIf { it.isNotBlank() },
                    practiced = practiced
                )
                _message.value = "적용 노트를 저장했습니다."
            } catch (e: Exception) {
                _message.value = "적용 노트 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun toggleApplicationPracticed(sessionId: String) {
        viewModelScope.launch {
            try {
                repository.toggleApplicationPracticed(sessionId)
            } catch (e: Exception) {
                _message.value = "실천 상태 변경에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Markup ----------------

    fun addMarkup(
        sessionId: String,
        verse: BibleVerse,
        startOffset: Int,
        endOffset: Int,
        selectedText: String,
        markType: String,
        memo: String? = null
    ) {
        if (selectedText.isBlank() || startOffset >= endOffset) {
            _message.value = "마킹할 텍스트가 비어 있습니다."
            return
        }
        viewModelScope.launch {
            try {
                repository.addMarkup(
                    sessionId = sessionId,
                    verseId = verse.id,
                    book = verse.book,
                    chapter = verse.chapter,
                    verse = verse.verse,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    selectedText = selectedText,
                    markType = markType,
                    memo = memo
                )
            } catch (e: Exception) {
                _message.value = "마킹 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateMarkupMemo(markupId: String, memo: String?) {
        viewModelScope.launch {
            try {
                repository.updateMarkupMemo(markupId, memo)
            } catch (e: Exception) {
                _message.value = "마킹 메모 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateMarkupType(markupId: String, newType: String) {
        viewModelScope.launch {
            try {
                repository.updateMarkupType(markupId, newType)
            } catch (e: Exception) {
                _message.value = "마킹 타입 변경에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteMarkup(markup: StudyMarkup) {
        viewModelScope.launch {
            try {
                repository.deleteMarkup(markup.id, markup.sessionId)
            } catch (e: Exception) {
                _message.value = "마킹 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Markup links ----------------

    fun addMarkupLink(
        sessionId: String,
        fromMarkupId: String,
        toMarkupId: String,
        linkType: String,
        memo: String? = null
    ) {
        viewModelScope.launch {
            try {
                val ok = repository.addMarkupLink(sessionId, fromMarkupId, toMarkupId, linkType, memo)
                if (!ok) {
                    _message.value = "이미 같은 연결이 존재합니다."
                }
            } catch (e: Exception) {
                _message.value = "연결 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteMarkupLink(link: StudyMarkupLink) {
        viewModelScope.launch {
            try {
                repository.deleteMarkupLink(link.id, link.sessionId)
            } catch (e: Exception) {
                _message.value = "연결 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Character tags ----------------

    fun addCharacterTag(
        sessionId: String,
        targetType: String,
        targetId: String,
        tag: String,
        memo: String? = null
    ) {
        viewModelScope.launch {
            try {
                val ok = repository.addCharacterTag(sessionId, targetType, targetId, tag, memo)
                if (!ok) {
                    _message.value = "이미 같은 성격 태그가 있습니다."
                }
            } catch (e: Exception) {
                _message.value = "성격 태그 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCharacterTag(tag: StudyCharacterTag) {
        viewModelScope.launch {
            try {
                repository.deleteCharacterTag(tag.id, tag.sessionId)
            } catch (e: Exception) {
                _message.value = "성격 태그 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Interpretation ----------------

    fun addInterpretation(
        sessionId: String,
        keyword: String,
        plainMeaning: String,
        contextualMeaning: String,
        evidence: String,
        crossRefs: String,
        conclusion: String,
        linkedMarkupId: String? = null
    ) {
        val anyText = listOf(keyword, plainMeaning, contextualMeaning, evidence, crossRefs, conclusion)
            .any { it.isNotBlank() }
        if (!anyText) {
            _message.value = "해석 노트에 최소 한 항목은 입력해 주세요."
            return
        }
        viewModelScope.launch {
            try {
                repository.addInterpretation(
                    sessionId = sessionId,
                    keyword = keyword.trim(),
                    plainMeaning = plainMeaning.trim(),
                    contextualMeaning = contextualMeaning.trim(),
                    evidence = evidence.trim(),
                    crossRefs = crossRefs.trim(),
                    conclusion = conclusion.trim(),
                    linkedMarkupId = linkedMarkupId
                )
            } catch (e: Exception) {
                _message.value = "해석 노트 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateInterpretation(note: StudyInterpretation) {
        viewModelScope.launch {
            try {
                repository.updateInterpretation(note)
            } catch (e: Exception) {
                _message.value = "해석 노트 수정에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteInterpretation(note: StudyInterpretation) {
        viewModelScope.launch {
            try {
                repository.deleteInterpretation(note.id, note.sessionId)
            } catch (e: Exception) {
                _message.value = "해석 노트 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Theme verification ----------------

    fun saveThemeCheck(sessionId: String, checkKey: String, isChecked: Boolean, note: String) {
        viewModelScope.launch {
            try {
                repository.upsertThemeCheck(sessionId, checkKey, isChecked, note.trim())
            } catch (e: Exception) {
                _message.value = "검증 결과 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Proposition ----------------

    fun addProposition(
        sessionId: String,
        sentence: String,
        supportingRefs: String,
        reviewStatus: String = PropositionStatus.DRAFT,
        linkedMarkupIds: String? = null,
        linkedDivisionIds: String? = null
    ) {
        if (sentence.isBlank()) {
            _message.value = "명제 문장을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            try {
                repository.addProposition(
                    sessionId = sessionId,
                    sentence = sentence.trim(),
                    supportingRefs = supportingRefs.trim(),
                    reviewStatus = reviewStatus,
                    linkedMarkupIds = linkedMarkupIds,
                    linkedDivisionIds = linkedDivisionIds
                )
            } catch (e: Exception) {
                _message.value = "명제 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateProposition(proposition: StudyProposition) {
        viewModelScope.launch {
            try {
                repository.updateProposition(proposition)
            } catch (e: Exception) {
                _message.value = "명제 수정에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteProposition(proposition: StudyProposition) {
        viewModelScope.launch {
            try {
                repository.deleteProposition(proposition.id, proposition.sessionId)
            } catch (e: Exception) {
                _message.value = "명제 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    // ---------------- Outline ----------------

    fun addOutlineNode(
        sessionId: String,
        title: String,
        level: Int,
        parentId: String? = null,
        verseRange: String? = null,
        summary: String? = null,
        linkedDivisionId: String? = null
    ) {
        if (title.isBlank()) {
            _message.value = "개요 제목을 입력해 주세요."
            return
        }
        viewModelScope.launch {
            try {
                repository.addOutlineNode(
                    sessionId = sessionId,
                    title = title.trim(),
                    level = level,
                    parentId = parentId,
                    verseRange = verseRange?.takeIf { it.isNotBlank() },
                    summary = summary?.takeIf { it.isNotBlank() },
                    linkedDivisionId = linkedDivisionId
                )
            } catch (e: Exception) {
                _message.value = "개요 노드 저장에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun updateOutlineNode(node: StudyOutlineNode) {
        viewModelScope.launch {
            try {
                repository.updateOutlineNode(node)
            } catch (e: Exception) {
                _message.value = "개요 노드 수정에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun deleteOutlineNode(node: StudyOutlineNode) {
        viewModelScope.launch {
            try {
                repository.deleteOutlineNode(node)
            } catch (e: Exception) {
                _message.value = "개요 노드 삭제에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    fun showMessage(text: String) {
        _message.value = text
    }

    // ---------------- Report export ----------------

    /**
     * 현재 선택된 세션의 모든 데이터를 [StudyReportSnapshot] 으로 모으고
     * Markdown 으로 직렬화한다. 외부 IO(파일/공유)는 호출자(UI)가 담당한다.
     *
     * 라벨은 사용자 언어(`appLanguage`)에 맞춰 한·영을 선택한다.
     */
    fun buildMarkdownReport(appLanguage: String, onReady: (StudySession, String) -> Unit) {
        val sessionId = _selectedSessionId.value ?: run {
            _message.value = "내보낼 연구 세션이 선택되지 않았습니다."
            return
        }
        viewModelScope.launch {
            try {
                val session = repository.observeSession(sessionId).first()
                    ?: throw IllegalStateException("session not found")
                val observations = repository.observeObservations(sessionId).first()
                val markups = repository.observeMarkups(sessionId).first()
                val links = repository.observeMarkupLinks(sessionId).first()
                val divisions = repository.observeDivisions(sessionId).first()
                val tags = repository.observeCharacterTags(sessionId).first()
                val interpretations = repository.observeInterpretations(sessionId).first()
                val themeChecks = repository.observeThemeChecks(sessionId).first()
                val propositions = repository.observePropositions(sessionId).first()
                val outline = repository.observeOutlineNodes(sessionId).first()
                val applicationNote = repository.observeApplication(sessionId).first()
                val verses = loadVersesForSession(session)

                val tagsByDivision = tags
                    .filter { it.targetType == CharacterTagTarget.DIVISION }
                    .groupBy { it.targetId }

                val snapshot = StudyReportSnapshot(
                    session = session,
                    verses = verses,
                    observations = observations,
                    markups = markups,
                    markupLinks = links,
                    divisions = divisions,
                    characterTagsByDivision = tagsByDivision,
                    allCharacterTags = tags,
                    interpretations = interpretations,
                    themeChecks = themeChecks,
                    propositions = propositions,
                    outline = outline,
                    application = applicationNote
                )

                val exporter = StudyMarkdownExporter(StudyMarkdownExporter.Labels.forLanguage(appLanguage))
                val markdown = exporter.export(snapshot)
                onReady(session, markdown)
            } catch (e: Exception) {
                _message.value = "리포트 생성에 실패했습니다: ${e.localizedMessage}"
            }
        }
    }

    private suspend fun loadVersesForSession(session: StudySession): List<BibleVerse> {
        val collected = mutableListOf<BibleVerse>()
        for (chapter in session.startChapter..session.endChapter) {
            val chapterVerses = bibleDao.getVersesByBookChapter(session.book, chapter)
            val filtered = chapterVerses.filter { verse ->
                val afterStart = if (chapter == session.startChapter) verse.verse >= session.startVerse else true
                val beforeEnd = if (chapter == session.endChapter) verse.verse <= session.endVerse else true
                afterStart && beforeEnd
            }
            collected.addAll(filtered)
        }
        return collected
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application required")
                return StudyViewModel(app) as T
            }
        }
    }
}
