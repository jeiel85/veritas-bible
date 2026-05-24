package com.veritasbible.app.study

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.veritasbible.app.data.AppDatabase
import com.veritasbible.app.study.data.CharacterTagTarget
import com.veritasbible.app.study.data.CharacterTagType
import com.veritasbible.app.study.data.LinkType
import com.veritasbible.app.study.data.MarkType
import com.veritasbible.app.study.data.ObservationQuestion
import com.veritasbible.app.study.data.PropositionStatus
import com.veritasbible.app.study.data.ThemeCheckKey
import com.veritasbible.app.study.repository.StudyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: StudyRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StudyRepository(
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
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun newSession() = repository.createSession(
        title = "요한복음 1:1 연구",
        book = "요한복음",
        bookEn = "John",
        bookId = 43,
        startChapter = 1,
        startVerse = 1,
        endChapter = 1,
        endVerse = 18
    )

    // ---------------- Goal 1 baseline ----------------

    @Test
    fun `creates session and persists observation`() = runTest {
        val session = newSession()
        repository.addObservation(session.id, "태초에 말씀이 계시니라")

        val observations = repository.observeObservations(session.id).first()
        assertEquals(1, observations.size)
        assertEquals("태초에 말씀이 계시니라", observations.first().answer)
    }

    @Test
    fun `adds and orders divisions`() = runTest {
        val session = repository.createSession(
            title = "롬1 연구",
            book = "로마서",
            bookEn = "Romans",
            bookId = 45,
            startChapter = 1,
            startVerse = 1,
            endChapter = 1,
            endVerse = 17
        )
        repository.addDivision(session.id, "복음의 인사", 1, 1, 1, 7)
        repository.addDivision(session.id, "방문 의지", 1, 8, 1, 15)

        val divisions = repository.observeDivisions(session.id).first()
        assertEquals(2, divisions.size)
        assertEquals(0, divisions[0].orderIndex)
        assertEquals(1, divisions[1].orderIndex)
    }

    @Test
    fun `saves and overwrites application note`() = runTest {
        val session = repository.createSession(
            title = "시1 연구",
            book = "시편",
            bookEn = "Psalms",
            bookId = 19,
            startChapter = 1,
            startVerse = 1,
            endChapter = 1,
            endVerse = 6
        )
        repository.saveApplication(session.id, "복 있는 사람", "악인의 꾀를 따른 적 있다", "그 자리에서 떠난다", "오늘 저녁 30분 묵상")
        var current = repository.observeApplication(session.id).first()
        assertNotNull(current)
        assertEquals("복 있는 사람", current!!.truthStatement)

        repository.saveApplication(session.id, "복 있는 사람의 길", current.mirrorStatement, current.adjustmentStatement, current.actionPlan)
        current = repository.observeApplication(session.id).first()
        assertEquals("복 있는 사람의 길", current!!.truthStatement)
    }

    @Test
    fun `deletes session and cascades`() = runTest {
        val session = repository.createSession(
            title = "마5 연구",
            book = "마태복음",
            bookEn = "Matthew",
            bookId = 40,
            startChapter = 5,
            startVerse = 1,
            endChapter = 5,
            endVerse = 12
        )
        repository.addObservation(session.id, "심령이 가난한 자 복")
        repository.deleteSession(session.id)

        val sessions = repository.observeSessions().first()
        assertEquals(0, sessions.size)
        val observations = repository.observeObservations(session.id).first()
        assertEquals(0, observations.size)
    }

    @Test
    fun `updates main theme via meta update`() = runTest {
        val session = repository.createSession(
            title = "에1 연구",
            book = "에베소서",
            bookEn = "Ephesians",
            bookId = 49,
            startChapter = 1,
            startVerse = 3,
            endChapter = 1,
            endVerse = 14
        )
        repository.updateSessionMeta(session.id, mainTheme = "그리스도 안의 신령한 복")
        val refreshed = repository.observeSession(session.id).first()
        assertNotNull(refreshed)
        assertEquals("그리스도 안의 신령한 복", refreshed!!.mainTheme)

        repository.deleteSession(session.id)
        val gone = repository.observeSession(session.id).first()
        assertNull(gone)
    }

    // ---------------- Goal 2 — Markup ----------------

    @Test
    fun `adds and deletes markups`() = runTest {
        val session = newSession()
        val markup = repository.addMarkup(
            sessionId = session.id,
            verseId = 100,
            book = "요한복음",
            chapter = 1,
            verse = 1,
            startOffset = 0,
            endOffset = 4,
            selectedText = "태초에",
            markType = MarkType.SUBJECT
        )
        val list = repository.observeMarkups(session.id).first()
        assertEquals(1, list.size)
        assertEquals(MarkType.SUBJECT, list.first().markType)

        repository.deleteMarkup(markup.id, session.id)
        assertTrue(repository.observeMarkups(session.id).first().isEmpty())
    }

    @Test
    fun `updates markup memo and type`() = runTest {
        val session = newSession()
        val markup = repository.addMarkup(
            sessionId = session.id,
            verseId = 101,
            book = "요한복음",
            chapter = 1,
            verse = 1,
            startOffset = 5,
            endOffset = 9,
            selectedText = "말씀이",
            markType = MarkType.SUBJECT,
            memo = "본문 주어 후보"
        )
        repository.updateMarkupMemo(markup.id, "주체 = 로고스")
        repository.updateMarkupType(markup.id, MarkType.KEYWORD)
        val updated = repository.observeMarkups(session.id).first().first()
        assertEquals("주체 = 로고스", updated.memo)
        assertEquals(MarkType.KEYWORD, updated.markType)
    }

    // ---------------- Goal 2 — Structure links ----------------

    @Test
    fun `subject verb link create dedupe and delete`() = runTest {
        val session = newSession()
        val subj = repository.addMarkup(
            sessionId = session.id,
            verseId = 102, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 3, selectedText = "태초",
            markType = MarkType.SUBJECT
        )
        val verb = repository.addMarkup(
            sessionId = session.id,
            verseId = 102, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 4, endOffset = 8, selectedText = "계시니라",
            markType = MarkType.VERB
        )
        assertTrue(repository.addMarkupLink(session.id, subj.id, verb.id, LinkType.SUBJECT_VERB))
        // Duplicate must be rejected
        assertFalse(repository.addMarkupLink(session.id, subj.id, verb.id, LinkType.SUBJECT_VERB))

        val links = repository.observeMarkupLinks(session.id).first()
        assertEquals(1, links.size)
        assertEquals(LinkType.SUBJECT_VERB, links.first().linkType)

        repository.deleteMarkupLink(links.first().id, session.id)
        assertTrue(repository.observeMarkupLinks(session.id).first().isEmpty())
    }

    @Test
    fun `self link is rejected`() = runTest {
        val session = newSession()
        val m = repository.addMarkup(
            sessionId = session.id,
            verseId = 103, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 2, selectedText = "AB",
            markType = MarkType.SUBJECT
        )
        assertFalse(repository.addMarkupLink(session.id, m.id, m.id, LinkType.SUBJECT_VERB))
    }

    @Test
    fun `markup deletion cascades to its links`() = runTest {
        val session = newSession()
        val subj = repository.addMarkup(
            sessionId = session.id, verseId = 104, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 3, selectedText = "그가",
            markType = MarkType.SUBJECT
        )
        val verb = repository.addMarkup(
            sessionId = session.id, verseId = 104, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 4, endOffset = 7, selectedText = "오다",
            markType = MarkType.VERB
        )
        repository.addMarkupLink(session.id, subj.id, verb.id, LinkType.SUBJECT_VERB)
        repository.deleteMarkup(verb.id, session.id)
        assertTrue(repository.observeMarkupLinks(session.id).first().isEmpty())
    }

    // ---------------- Goal 2 — Observation templates ----------------

    @Test
    fun `upsert observation template stores one row per question`() = runTest {
        val session = newSession()
        repository.upsertObservationTemplate(
            session.id, ObservationQuestion.REPEATED_WORD, "반복되는 단어는?", "말씀"
        )
        repository.upsertObservationTemplate(
            session.id, ObservationQuestion.REPEATED_WORD, "반복되는 단어는?", "말씀 / 빛"
        )
        repository.upsertObservationTemplate(
            session.id, ObservationQuestion.MAIN_SUBJECT, "주된 주체는?", "말씀(로고스)"
        )

        val rows = repository.observeObservations(session.id).first()
        val byKey = rows.groupBy { it.questionKey }
        assertEquals(1, byKey[ObservationQuestion.REPEATED_WORD]!!.size)
        assertEquals("말씀 / 빛", byKey[ObservationQuestion.REPEATED_WORD]!!.first().answer)
        assertEquals("말씀(로고스)", byKey[ObservationQuestion.MAIN_SUBJECT]!!.first().answer)
    }

    // ---------------- Goal 2 — Character tags ----------------

    // ---------------- Goal 3 — Interpretation / Theme check / Proposition / Outline / Application practiced ----------------

    @Test
    fun `interpretation add update delete`() = runTest {
        val session = newSession()
        val note = repository.addInterpretation(
            sessionId = session.id,
            keyword = "말씀",
            plainMeaning = "logos",
            contextualMeaning = "성육신 이전의 영원한 말씀",
            evidence = "1:1, 1:14",
            crossRefs = "창세기 1:1",
            conclusion = "그리스도 = 말씀"
        )
        var list = repository.observeInterpretations(session.id).first()
        assertEquals(1, list.size)
        assertEquals("말씀", list.first().keyword)

        repository.updateInterpretation(note.copy(conclusion = "그리스도 = 말씀(로고스)"))
        list = repository.observeInterpretations(session.id).first()
        assertEquals("그리스도 = 말씀(로고스)", list.first().conclusion)

        repository.deleteInterpretation(note.id, session.id)
        assertTrue(repository.observeInterpretations(session.id).first().isEmpty())
    }

    @Test
    fun `theme check upsert keeps single row per key`() = runTest {
        val session = newSession()
        repository.upsertThemeCheck(session.id, ThemeCheckKey.COVERS_WHOLE_PASSAGE, true, "잘 포괄됨")
        repository.upsertThemeCheck(session.id, ThemeCheckKey.COVERS_WHOLE_PASSAGE, false, "12절 약함")
        repository.upsertThemeCheck(session.id, ThemeCheckKey.FREE_FROM_BIAS, true, "")

        val checks = repository.observeThemeChecks(session.id).first()
        assertEquals(2, checks.size)
        val coverRow = checks.first { it.checkKey == ThemeCheckKey.COVERS_WHOLE_PASSAGE }
        assertFalse(coverRow.isChecked)
        assertEquals("12절 약함", coverRow.note)
    }

    @Test
    fun `proposition add update delete with status`() = runTest {
        val session = newSession()
        val prop = repository.addProposition(
            sessionId = session.id,
            sentence = "예수님은 영원한 말씀이시다.",
            supportingRefs = "요 1:1, 1:14"
        )
        var list = repository.observePropositions(session.id).first()
        assertEquals(1, list.size)
        assertEquals(PropositionStatus.DRAFT, list.first().reviewStatus)

        repository.updateProposition(prop.copy(reviewStatus = PropositionStatus.REVIEWED))
        list = repository.observePropositions(session.id).first()
        assertEquals(PropositionStatus.REVIEWED, list.first().reviewStatus)

        repository.deleteProposition(prop.id, session.id)
        assertTrue(repository.observePropositions(session.id).first().isEmpty())
    }

    @Test
    fun `outline node ordering and cascade delete of children`() = runTest {
        val session = newSession()
        val root = repository.addOutlineNode(session.id, "대지 1", level = 0)
        repository.addOutlineNode(session.id, "소지 1-1", level = 1, parentId = root.id)
        repository.addOutlineNode(session.id, "소지 1-2", level = 1, parentId = root.id)
        repository.addOutlineNode(session.id, "대지 2", level = 0)

        val nodes = repository.observeOutlineNodes(session.id).first()
        assertEquals(4, nodes.size)

        repository.deleteOutlineNode(root)
        val remaining = repository.observeOutlineNodes(session.id).first()
        assertEquals(1, remaining.size)
        assertEquals("대지 2", remaining.first().title)
    }

    @Test
    fun `application practiced toggle updates flag`() = runTest {
        val session = newSession()
        repository.saveApplication(session.id, "진리", "거울", "조정", "행동")
        repository.toggleApplicationPracticed(session.id)
        var note = repository.observeApplication(session.id).first()
        assertNotNull(note)
        assertTrue(note!!.practiced)
        assertNotNull(note.checkedAt)

        repository.toggleApplicationPracticed(session.id)
        note = repository.observeApplication(session.id).first()
        assertFalse(note!!.practiced)
    }

    @Test
    fun `application saveApplication preserves practiced when not specified`() = runTest {
        val session = newSession()
        repository.saveApplication(session.id, "진리", "거울", "조정", "행동")
        repository.toggleApplicationPracticed(session.id)
        // Subsequent save without practiced argument must not toggle the flag back.
        repository.saveApplication(session.id, "진리 갱신", "거울", "조정", "행동")
        val note = repository.observeApplication(session.id).first()
        assertTrue(note!!.practiced)
        assertEquals("진리 갱신", note.truthStatement)
    }

    // ---------------- Goal 5 — sweeper ----------------

    @Test
    fun `deleteMarkup sweeps stale interpretation linkedMarkupId`() = runTest {
        val session = newSession()
        val markup = repository.addMarkup(
            sessionId = session.id, verseId = 1, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 3, selectedText = "태초에", markType = MarkType.SUBJECT
        )
        repository.addInterpretation(
            session.id, "태초", "처음", "영원성", "1:1", "", "그리스도",
            linkedMarkupId = markup.id
        )
        repository.deleteMarkup(markup.id, session.id)

        val note = repository.observeInterpretations(session.id).first().first()
        assertNull("linkedMarkupId must be nulled after markup deletion", note.linkedMarkupId)
    }

    @Test
    fun `deleteMarkup sweeps stale proposition linkedMarkupIds entry`() = runTest {
        val session = newSession()
        val subj = repository.addMarkup(
            sessionId = session.id, verseId = 1, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 3, selectedText = "태초", markType = MarkType.SUBJECT
        )
        val verb = repository.addMarkup(
            sessionId = session.id, verseId = 1, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 4, endOffset = 8, selectedText = "계시니라", markType = MarkType.VERB
        )
        repository.addProposition(
            session.id, "예수님은 영원한 말씀이시다.", "1:1",
            linkedMarkupIds = "${subj.id},${verb.id}"
        )

        repository.deleteMarkup(verb.id, session.id)

        val prop = repository.observePropositions(session.id).first().first()
        // verb.id 만 빠지고 subj.id 는 살아남는다.
        assertEquals(subj.id, prop.linkedMarkupIds)
    }

    @Test
    fun `deleteDivision sweeps stale proposition linkedDivisionIds and outline linkedDivisionId`() = runTest {
        val session = newSession()
        repository.addDivision(session.id, "복음 인사", 1, 1, 1, 5)
        repository.addDivision(session.id, "주제 선언", 1, 6, 1, 18)
        val divisions = repository.observeDivisions(session.id).first()
        val a = divisions[0].id
        val b = divisions[1].id
        repository.addProposition(
            session.id, "복음은 하나님의 능력이다.", "1:16",
            linkedDivisionIds = "$a,$b"
        )
        repository.addOutlineNode(
            session.id, "I. 인사", level = 0, linkedDivisionId = a
        )

        repository.deleteDivision(a, session.id)

        val prop = repository.observePropositions(session.id).first().first()
        assertEquals(b, prop.linkedDivisionIds)
        val node = repository.observeOutlineNodes(session.id).first().first()
        assertNull("outline linkedDivisionId must be nulled after division deletion", node.linkedDivisionId)
    }

    // ---------------- Goal 2 baseline (kept) ----------------

    @Test
    fun `character tag add deduplicate and delete`() = runTest {
        val session = newSession()
        repository.addDivision(session.id, "서언", 1, 1, 1, 5)
        val division = repository.observeDivisions(session.id).first().first()

        assertTrue(
            repository.addCharacterTag(
                session.id, CharacterTagTarget.DIVISION, division.id, CharacterTagType.DEFINITION
            )
        )
        assertFalse(
            repository.addCharacterTag(
                session.id, CharacterTagTarget.DIVISION, division.id, CharacterTagType.DEFINITION
            )
        )
        assertTrue(
            repository.addCharacterTag(
                session.id, CharacterTagTarget.DIVISION, division.id, CharacterTagType.PURPOSE
            )
        )

        val tags = repository.observeCharacterTags(session.id).first()
        assertEquals(2, tags.size)

        repository.deleteCharacterTag(tags.first().id, session.id)
        assertEquals(1, repository.observeCharacterTags(session.id).first().size)
    }
}
