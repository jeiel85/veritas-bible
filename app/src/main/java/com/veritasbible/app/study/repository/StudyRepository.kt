package com.veritasbible.app.study.repository

import com.veritasbible.app.study.data.LinkedIds
import com.veritasbible.app.study.data.PropositionStatus
import com.veritasbible.app.study.data.StudyApplication
import com.veritasbible.app.study.data.StudyApplicationDao
import com.veritasbible.app.study.data.StudyCharacterTag
import com.veritasbible.app.study.data.StudyCharacterTagDao
import com.veritasbible.app.study.data.StudyDivision
import com.veritasbible.app.study.data.StudyDivisionDao
import com.veritasbible.app.study.data.StudyInterpretation
import com.veritasbible.app.study.data.StudyInterpretationDao
import com.veritasbible.app.study.data.StudyMarkup
import com.veritasbible.app.study.data.StudyMarkupDao
import com.veritasbible.app.study.data.StudyMarkupLink
import com.veritasbible.app.study.data.StudyMarkupLinkDao
import com.veritasbible.app.study.data.StudyObservation
import com.veritasbible.app.study.data.StudyObservationDao
import com.veritasbible.app.study.data.StudyOutlineNode
import com.veritasbible.app.study.data.StudyOutlineNodeDao
import com.veritasbible.app.study.data.StudyProposition
import com.veritasbible.app.study.data.StudyPropositionDao
import com.veritasbible.app.study.data.StudySession
import com.veritasbible.app.study.data.StudySessionDao
import com.veritasbible.app.study.data.StudyStage
import com.veritasbible.app.study.data.StudyStatus
import com.veritasbible.app.study.data.StudyThemeCheck
import com.veritasbible.app.study.data.StudyThemeCheckDao
import com.veritasbible.app.study.data.StudyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 연구 세션 도메인의 단일 진입 저장소.
 * Room DAO 호출은 모두 IO 디스패처로 위임한다.
 */
class StudyRepository(
    private val sessionDao: StudySessionDao,
    private val observationDao: StudyObservationDao,
    private val divisionDao: StudyDivisionDao,
    private val applicationDao: StudyApplicationDao,
    private val markupDao: StudyMarkupDao,
    private val markupLinkDao: StudyMarkupLinkDao,
    private val characterTagDao: StudyCharacterTagDao,
    private val interpretationDao: StudyInterpretationDao,
    private val themeCheckDao: StudyThemeCheckDao,
    private val propositionDao: StudyPropositionDao,
    private val outlineNodeDao: StudyOutlineNodeDao
) {

    fun observeSessions(): Flow<List<StudySession>> =
        sessionDao.observeAll().flowOn(Dispatchers.IO)

    fun observeSession(id: String): Flow<StudySession?> =
        sessionDao.observeById(id).flowOn(Dispatchers.IO)

    fun observeObservations(sessionId: String): Flow<List<StudyObservation>> =
        observationDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeDivisions(sessionId: String): Flow<List<StudyDivision>> =
        divisionDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeApplication(sessionId: String): Flow<StudyApplication?> =
        applicationDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeMarkups(sessionId: String): Flow<List<StudyMarkup>> =
        markupDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeMarkupLinks(sessionId: String): Flow<List<StudyMarkupLink>> =
        markupLinkDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeCharacterTags(sessionId: String): Flow<List<StudyCharacterTag>> =
        characterTagDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeInterpretations(sessionId: String): Flow<List<StudyInterpretation>> =
        interpretationDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeThemeChecks(sessionId: String): Flow<List<StudyThemeCheck>> =
        themeCheckDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observePropositions(sessionId: String): Flow<List<StudyProposition>> =
        propositionDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    fun observeOutlineNodes(sessionId: String): Flow<List<StudyOutlineNode>> =
        outlineNodeDao.observeBySession(sessionId).flowOn(Dispatchers.IO)

    suspend fun createSession(
        title: String,
        book: String,
        bookEn: String,
        bookId: Int,
        startChapter: Int,
        startVerse: Int,
        endChapter: Int,
        endVerse: Int,
        studyType: String = StudyType.PASSAGE
    ): StudySession = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val session = StudySession(
            title = title.ifBlank { "$book ${startChapter}:${startVerse} 연구" },
            book = book,
            bookEn = bookEn,
            bookId = bookId,
            startChapter = startChapter,
            startVerse = startVerse,
            endChapter = endChapter,
            endVerse = endVerse,
            studyType = studyType,
            status = StudyStatus.IN_PROGRESS,
            currentStage = StudyStage.OBSERVATION,
            createdAt = now,
            updatedAt = now
        )
        sessionDao.upsert(session)
        session
    }

    suspend fun updateSessionMeta(
        sessionId: String,
        title: String? = null,
        mainTheme: String? = null,
        mainPropositionMemo: String? = null,
        currentStage: String? = null,
        status: String? = null
    ) = withContext(Dispatchers.IO) {
        val current = sessionDao.findById(sessionId) ?: return@withContext
        val updated = current.copy(
            title = title ?: current.title,
            mainTheme = mainTheme ?: current.mainTheme,
            mainPropositionMemo = mainPropositionMemo ?: current.mainPropositionMemo,
            currentStage = currentStage ?: current.currentStage,
            status = status ?: current.status,
            updatedAt = System.currentTimeMillis()
        )
        sessionDao.update(updated)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.deleteById(sessionId)
    }

    // ---------------- Observation ----------------

    suspend fun addObservation(sessionId: String, answer: String, questionText: String = "") =
        withContext(Dispatchers.IO) {
            val obs = StudyObservation(
                sessionId = sessionId,
                questionText = questionText,
                answer = answer
            )
            observationDao.upsert(obs)
            bumpSession(sessionId)
        }

    /**
     * 질문 템플릿(주제별로 단일 답안)을 upsert 한다.
     * 이미 답한 질문이 있으면 기존 행을 갱신해 답안이 단일 행에 모이도록 한다.
     */
    suspend fun upsertObservationTemplate(
        sessionId: String,
        questionKey: String,
        questionText: String,
        answer: String
    ) = withContext(Dispatchers.IO) {
        val existing = observationDao.findByQuestion(sessionId, questionKey)
        val now = System.currentTimeMillis()
        val record = (existing ?: StudyObservation(
            sessionId = sessionId,
            questionKey = questionKey,
            questionText = questionText,
            answer = answer
        )).copy(
            questionText = questionText,
            answer = answer,
            updatedAt = now
        )
        observationDao.upsert(record)
        bumpSession(sessionId)
    }

    suspend fun updateObservation(observation: StudyObservation) = withContext(Dispatchers.IO) {
        observationDao.update(observation.copy(updatedAt = System.currentTimeMillis()))
        bumpSession(observation.sessionId)
    }

    suspend fun deleteObservation(observation: StudyObservation) = withContext(Dispatchers.IO) {
        observationDao.delete(observation)
        bumpSession(observation.sessionId)
    }

    // ---------------- Division ----------------

    suspend fun addDivision(
        sessionId: String,
        title: String,
        startChapter: Int,
        startVerse: Int,
        endChapter: Int,
        endVerse: Int,
        summary: String? = null
    ) = withContext(Dispatchers.IO) {
        val nextOrder = (divisionDao.maxOrderIndex(sessionId) ?: -1) + 1
        val division = StudyDivision(
            sessionId = sessionId,
            orderIndex = nextOrder,
            startChapter = startChapter,
            startVerse = startVerse,
            endChapter = endChapter,
            endVerse = endVerse,
            title = title,
            summary = summary
        )
        divisionDao.upsert(division)
        bumpSession(sessionId)
    }

    suspend fun updateDivision(division: StudyDivision) = withContext(Dispatchers.IO) {
        divisionDao.update(division.copy(updatedAt = System.currentTimeMillis()))
        bumpSession(division.sessionId)
    }

    suspend fun deleteDivision(divisionId: String, sessionId: String) = withContext(Dispatchers.IO) {
        divisionDao.deleteById(divisionId)
        sweepStaleDivisionLinks(sessionId, divisionId)
        bumpSession(sessionId)
    }

    /**
     * 단락 삭제 후 stale ID 를 가리키는 명제/개요 행을 갱신한다.
     * - StudyProposition.linkedDivisionIds CSV → divisionId 토큰 제거
     * - StudyOutlineNode.linkedDivisionId == divisionId → null
     */
    private suspend fun sweepStaleDivisionLinks(sessionId: String, divisionId: String) {
        propositionDao.findByLinkedDivision(sessionId, divisionId).forEach { prop ->
            val cleaned = LinkedIds.encode(
                LinkedIds.decode(prop.linkedDivisionIds).filter { it != divisionId }
            )
            propositionDao.update(
                prop.copy(linkedDivisionIds = cleaned, updatedAt = System.currentTimeMillis())
            )
        }
        outlineNodeDao.findByLinkedDivision(sessionId, divisionId).forEach { node ->
            outlineNodeDao.update(
                node.copy(linkedDivisionId = null, updatedAt = System.currentTimeMillis())
            )
        }
    }

    // ---------------- Application ----------------

    suspend fun saveApplication(
        sessionId: String,
        truth: String,
        mirror: String,
        adjustment: String,
        actionPlan: String,
        dueDate: String? = null,
        practiced: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        val existing = applicationDao.findBySession(sessionId)
        val now = System.currentTimeMillis()
        val nextPracticed = practiced ?: existing?.practiced ?: false
        val record = (existing ?: StudyApplication(sessionId = sessionId)).copy(
            truthStatement = truth,
            mirrorStatement = mirror,
            adjustmentStatement = adjustment,
            actionPlan = actionPlan,
            dueDate = dueDate,
            practiced = nextPracticed,
            checkedAt = if (nextPracticed) now else null,
            updatedAt = now,
            createdAt = existing?.createdAt ?: now
        )
        applicationDao.upsert(record)
        bumpSession(sessionId)
    }

    suspend fun toggleApplicationPracticed(sessionId: String) = withContext(Dispatchers.IO) {
        val existing = applicationDao.findBySession(sessionId) ?: StudyApplication(sessionId = sessionId)
        val now = System.currentTimeMillis()
        val next = !existing.practiced
        applicationDao.upsert(
            existing.copy(
                practiced = next,
                checkedAt = if (next) now else null,
                updatedAt = now
            )
        )
        bumpSession(sessionId)
    }

    // ---------------- Markup ----------------

    suspend fun addMarkup(
        sessionId: String,
        verseId: Int,
        book: String,
        chapter: Int,
        verse: Int,
        startOffset: Int,
        endOffset: Int,
        selectedText: String,
        markType: String,
        memo: String? = null
    ): StudyMarkup = withContext(Dispatchers.IO) {
        val markup = StudyMarkup(
            sessionId = sessionId,
            verseId = verseId,
            book = book,
            chapter = chapter,
            verse = verse,
            startOffset = startOffset,
            endOffset = endOffset,
            selectedText = selectedText,
            markType = markType,
            memo = memo?.takeIf { it.isNotBlank() }
        )
        markupDao.upsert(markup)
        bumpSession(sessionId)
        markup
    }

    suspend fun updateMarkupMemo(markupId: String, memo: String?) = withContext(Dispatchers.IO) {
        val current = markupDao.findById(markupId) ?: return@withContext
        markupDao.update(
            current.copy(
                memo = memo?.takeIf { it.isNotBlank() },
                updatedAt = System.currentTimeMillis()
            )
        )
        bumpSession(current.sessionId)
    }

    suspend fun updateMarkupType(markupId: String, newType: String) = withContext(Dispatchers.IO) {
        val current = markupDao.findById(markupId) ?: return@withContext
        markupDao.update(current.copy(markType = newType, updatedAt = System.currentTimeMillis()))
        bumpSession(current.sessionId)
    }

    suspend fun deleteMarkup(markupId: String, sessionId: String) = withContext(Dispatchers.IO) {
        // 1) 마킹 자체 삭제. (markup_links 는 FK CASCADE 로 자동 정리됨.)
        markupDao.deleteById(markupId)
        // 2) Stale link id 정리: interpretation.linkedMarkupId, proposition.linkedMarkupIds.
        sweepStaleMarkupLinks(sessionId, markupId)
        bumpSession(sessionId)
    }

    /**
     * 마킹 삭제 후 stale ID 를 가리키는 해석/명제 행을 갱신한다.
     * - StudyInterpretation.linkedMarkupId == markupId → null 로 클리어
     * - StudyProposition.linkedMarkupIds CSV → markupId 토큰 제거
     */
    private suspend fun sweepStaleMarkupLinks(sessionId: String, markupId: String) {
        interpretationDao.findByLinkedMarkup(sessionId, markupId).forEach { note ->
            interpretationDao.update(
                note.copy(linkedMarkupId = null, updatedAt = System.currentTimeMillis())
            )
        }
        propositionDao.findByLinkedMarkup(sessionId, markupId).forEach { prop ->
            val cleaned = LinkedIds.encode(
                LinkedIds.decode(prop.linkedMarkupIds).filter { it != markupId }
            )
            propositionDao.update(
                prop.copy(linkedMarkupIds = cleaned, updatedAt = System.currentTimeMillis())
            )
        }
    }

    // ---------------- Markup link ----------------

    /** 동일 (from,to,linkType) 중복을 방지하며 새 연결을 만든다. */
    suspend fun addMarkupLink(
        sessionId: String,
        fromMarkupId: String,
        toMarkupId: String,
        linkType: String,
        memo: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (fromMarkupId == toMarkupId) return@withContext false
        val exists = markupLinkDao.countDuplicate(sessionId, fromMarkupId, toMarkupId, linkType) > 0
        if (exists) return@withContext false
        val link = StudyMarkupLink(
            sessionId = sessionId,
            fromMarkupId = fromMarkupId,
            toMarkupId = toMarkupId,
            linkType = linkType,
            memo = memo?.takeIf { it.isNotBlank() }
        )
        markupLinkDao.upsert(link)
        bumpSession(sessionId)
        true
    }

    suspend fun deleteMarkupLink(linkId: String, sessionId: String) = withContext(Dispatchers.IO) {
        markupLinkDao.deleteById(linkId)
        bumpSession(sessionId)
    }

    // ---------------- Character tag ----------------

    suspend fun addCharacterTag(
        sessionId: String,
        targetType: String,
        targetId: String,
        tag: String,
        memo: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val exists = characterTagDao.countDuplicate(sessionId, targetType, targetId, tag) > 0
        if (exists) return@withContext false
        val record = StudyCharacterTag(
            sessionId = sessionId,
            targetType = targetType,
            targetId = targetId,
            tag = tag,
            memo = memo?.takeIf { it.isNotBlank() }
        )
        characterTagDao.upsert(record)
        bumpSession(sessionId)
        true
    }

    suspend fun deleteCharacterTag(tagId: String, sessionId: String) = withContext(Dispatchers.IO) {
        characterTagDao.deleteById(tagId)
        bumpSession(sessionId)
    }

    // ---------------- Interpretation ----------------

    suspend fun addInterpretation(
        sessionId: String,
        keyword: String,
        plainMeaning: String,
        contextualMeaning: String,
        evidence: String,
        crossRefs: String,
        conclusion: String,
        linkedMarkupId: String? = null
    ): StudyInterpretation = withContext(Dispatchers.IO) {
        val note = StudyInterpretation(
            sessionId = sessionId,
            keyword = keyword,
            plainMeaning = plainMeaning,
            contextualMeaning = contextualMeaning,
            evidence = evidence,
            crossRefs = crossRefs,
            conclusion = conclusion,
            linkedMarkupId = linkedMarkupId
        )
        interpretationDao.upsert(note)
        bumpSession(sessionId)
        note
    }

    suspend fun updateInterpretation(note: StudyInterpretation) = withContext(Dispatchers.IO) {
        interpretationDao.update(note.copy(updatedAt = System.currentTimeMillis()))
        bumpSession(note.sessionId)
    }

    suspend fun deleteInterpretation(noteId: String, sessionId: String) = withContext(Dispatchers.IO) {
        interpretationDao.deleteById(noteId)
        bumpSession(sessionId)
    }

    // ---------------- Theme verification check ----------------

    suspend fun upsertThemeCheck(
        sessionId: String,
        checkKey: String,
        isChecked: Boolean,
        note: String
    ) = withContext(Dispatchers.IO) {
        val existing = themeCheckDao.findByKey(sessionId, checkKey)
        val now = System.currentTimeMillis()
        val record = (existing ?: StudyThemeCheck(
            sessionId = sessionId,
            checkKey = checkKey,
            isChecked = isChecked,
            note = note
        )).copy(
            isChecked = isChecked,
            note = note,
            updatedAt = now,
            createdAt = existing?.createdAt ?: now
        )
        themeCheckDao.upsert(record)
        bumpSession(sessionId)
    }

    // ---------------- Proposition ----------------

    suspend fun addProposition(
        sessionId: String,
        sentence: String,
        supportingRefs: String,
        reviewStatus: String = PropositionStatus.DRAFT,
        linkedMarkupIds: String? = null,
        linkedDivisionIds: String? = null
    ): StudyProposition = withContext(Dispatchers.IO) {
        val proposition = StudyProposition(
            sessionId = sessionId,
            sentence = sentence,
            supportingRefs = supportingRefs,
            reviewStatus = reviewStatus,
            linkedMarkupIds = linkedMarkupIds,
            linkedDivisionIds = linkedDivisionIds
        )
        propositionDao.upsert(proposition)
        bumpSession(sessionId)
        proposition
    }

    suspend fun updateProposition(proposition: StudyProposition) = withContext(Dispatchers.IO) {
        propositionDao.update(proposition.copy(updatedAt = System.currentTimeMillis()))
        bumpSession(proposition.sessionId)
    }

    suspend fun deleteProposition(id: String, sessionId: String) = withContext(Dispatchers.IO) {
        propositionDao.deleteById(id)
        bumpSession(sessionId)
    }

    // ---------------- Outline node ----------------

    suspend fun addOutlineNode(
        sessionId: String,
        title: String,
        level: Int = 0,
        parentId: String? = null,
        verseRange: String? = null,
        summary: String? = null,
        linkedDivisionId: String? = null
    ): StudyOutlineNode = withContext(Dispatchers.IO) {
        val nextOrder = (outlineNodeDao.maxOrderIndex(sessionId, parentId) ?: -1) + 1
        val node = StudyOutlineNode(
            sessionId = sessionId,
            title = title,
            level = level,
            parentId = parentId,
            verseRange = verseRange,
            summary = summary,
            linkedDivisionId = linkedDivisionId,
            orderIndex = nextOrder
        )
        outlineNodeDao.upsert(node)
        bumpSession(sessionId)
        node
    }

    suspend fun updateOutlineNode(node: StudyOutlineNode) = withContext(Dispatchers.IO) {
        outlineNodeDao.update(node.copy(updatedAt = System.currentTimeMillis()))
        bumpSession(node.sessionId)
    }

    suspend fun deleteOutlineNode(node: StudyOutlineNode) = withContext(Dispatchers.IO) {
        outlineNodeDao.deleteCascade(node.id)
        bumpSession(node.sessionId)
    }

    private suspend fun bumpSession(sessionId: String) {
        val current = sessionDao.findById(sessionId) ?: return
        sessionDao.update(current.copy(updatedAt = System.currentTimeMillis()))
    }
}
