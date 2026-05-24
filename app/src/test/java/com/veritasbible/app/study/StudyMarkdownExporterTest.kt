package com.veritasbible.app.study

import com.veritasbible.app.study.data.MarkType
import com.veritasbible.app.study.data.ObservationQuestion
import com.veritasbible.app.study.data.PropositionStatus
import com.veritasbible.app.study.data.StudyApplication
import com.veritasbible.app.study.data.StudyCharacterTag
import com.veritasbible.app.study.data.CharacterTagTarget
import com.veritasbible.app.study.data.StudyDivision
import com.veritasbible.app.study.data.StudyInterpretation
import com.veritasbible.app.study.data.StudyMarkup
import com.veritasbible.app.study.data.StudyMarkupLink
import com.veritasbible.app.study.data.LinkType
import com.veritasbible.app.study.data.StudyObservation
import com.veritasbible.app.study.data.StudyOutlineNode
import com.veritasbible.app.study.data.StudyProposition
import com.veritasbible.app.study.data.StudySession
import com.veritasbible.app.study.data.StudyThemeCheck
import com.veritasbible.app.study.data.ThemeCheckKey
import com.veritasbible.app.study.report.StudyMarkdownExporter
import com.veritasbible.app.study.report.StudyReportSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyMarkdownExporterTest {

    private val session = StudySession(
        id = "sess-1",
        title = "요한복음 1:1-18 연구",
        book = "요한복음",
        bookEn = "John",
        bookId = 43,
        startChapter = 1,
        startVerse = 1,
        endChapter = 1,
        endVerse = 18,
        mainTheme = "그리스도는 영원한 말씀이시다.",
        mainPropositionMemo = "말씀이 사람이 되심"
    )

    @Test
    fun `empty session renders all section headers with placeholders`() {
        val snapshot = StudyReportSnapshot(
            session = session,
            verses = emptyList(),
            observations = emptyList(),
            markups = emptyList(),
            markupLinks = emptyList(),
            divisions = emptyList(),
            characterTagsByDivision = emptyMap(),
            allCharacterTags = emptyList(),
            interpretations = emptyList(),
            themeChecks = emptyList(),
            propositions = emptyList(),
            outline = emptyList(),
            application = null,
            generatedAt = 1_700_000_000_000
        )
        val md = StudyMarkdownExporter(StudyMarkdownExporter.Labels.KO).export(snapshot)

        assertTrue(md.startsWith("# 요한복음 1:1-18 연구"))
        listOf(
            "관찰 요약",
            "본문 마킹 요약",
            "단락 구분",
            "성격 태그",
            "해석 노트",
            "핵심주제",
            "핵심주제 검증",
            "명제",
            "개요",
            "적용"
        ).forEach { header ->
            assertTrue("missing header `$header`", md.contains("## $header"))
        }
        // Main theme block still renders because session has theme text.
        assertTrue(md.contains("그리스도는 영원한 말씀이시다."))
        // Other sections fall back to placeholder.
        assertTrue(md.contains("> 기록 없음."))
    }

    @Test
    fun `populated session includes markups divisions theme checks proposition outline application`() {
        val divisionId = "div-1"
        val obs1 = StudyObservation(
            sessionId = session.id,
            questionKey = ObservationQuestion.REPEATED_WORD,
            questionText = "반복되는 단어는?",
            answer = "말씀"
        )
        val obsFree = StudyObservation(
            sessionId = session.id,
            questionKey = ObservationQuestion.FREE,
            answer = "1절과 14절이 수미상관 구조"
        )
        val markupA = StudyMarkup(
            id = "mk-a",
            sessionId = session.id,
            verseId = 1, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 0, endOffset = 3,
            selectedText = "태초에",
            markType = MarkType.SUBJECT,
            memo = "시간 표시"
        )
        val markupB = StudyMarkup(
            id = "mk-b",
            sessionId = session.id,
            verseId = 1, book = "요한복음", chapter = 1, verse = 1,
            startOffset = 4, endOffset = 8,
            selectedText = "계시니라",
            markType = MarkType.VERB
        )
        val link = StudyMarkupLink(
            sessionId = session.id,
            fromMarkupId = markupA.id,
            toMarkupId = markupB.id,
            linkType = LinkType.SUBJECT_VERB
        )
        val division = StudyDivision(
            id = divisionId,
            sessionId = session.id,
            orderIndex = 0,
            startChapter = 1, startVerse = 1, endChapter = 1, endVerse = 5,
            title = "말씀의 본질",
            summary = "그가 누구이신가"
        )
        val charTag = StudyCharacterTag(
            sessionId = session.id,
            targetType = CharacterTagTarget.DIVISION,
            targetId = divisionId,
            tag = "definition"
        )
        val interp = StudyInterpretation(
            sessionId = session.id,
            keyword = "말씀",
            plainMeaning = "로고스",
            conclusion = "성자 그리스도"
        )
        val check = StudyThemeCheck(
            sessionId = session.id,
            checkKey = ThemeCheckKey.COVERS_WHOLE_PASSAGE,
            isChecked = true,
            note = "전체 단락이 말씀 한 인물로 수렴"
        )
        val prop = StudyProposition(
            sessionId = session.id,
            sentence = "예수님은 영원한 말씀이시다.",
            supportingRefs = "1:1, 1:14",
            reviewStatus = PropositionStatus.REVIEWED
        )
        val outlineRoot = StudyOutlineNode(
            id = "ol-1",
            sessionId = session.id,
            title = "1. 영원하신 말씀",
            level = 0,
            orderIndex = 0
        )
        val outlineChild = StudyOutlineNode(
            id = "ol-2",
            sessionId = session.id,
            title = "1.1 태초에 계심",
            level = 1,
            parentId = "ol-1",
            orderIndex = 0
        )
        val app = StudyApplication(
            sessionId = session.id,
            truthStatement = "말씀이 곧 그리스도",
            mirrorStatement = "내 우선순위는?",
            adjustmentStatement = "성경 묵상 시간 확보",
            actionPlan = "매일 30분 요한복음 묵상",
            dueDate = "2026-06-01",
            practiced = true
        )
        val snapshot = StudyReportSnapshot(
            session = session,
            verses = emptyList(),
            observations = listOf(obs1, obsFree),
            markups = listOf(markupA, markupB),
            markupLinks = listOf(link),
            divisions = listOf(division),
            characterTagsByDivision = mapOf(divisionId to listOf(charTag)),
            allCharacterTags = listOf(charTag),
            interpretations = listOf(interp),
            themeChecks = listOf(check),
            propositions = listOf(prop),
            outline = listOf(outlineRoot, outlineChild),
            application = app,
            generatedAt = 1_700_000_000_000
        )
        val md = StudyMarkdownExporter(StudyMarkdownExporter.Labels.KO).export(snapshot)

        // observation template + free
        assertTrue(md.contains("반복되는 단어는?"))
        assertTrue(md.contains("1절과 14절이 수미상관 구조"))
        // markups grouped and link rendered
        assertTrue(md.contains("`subject` “태초에”"))
        assertTrue(md.contains("`verb` “계시니라”"))
        assertTrue(md.contains("subject_verb"))
        assertTrue(md.contains("태초에 → 계시니라"))
        // division + tag
        assertTrue(md.contains("말씀의 본질"))
        assertTrue(md.contains("tags: definition"))
        // interpretation + theme check + proposition + outline + application
        assertTrue(md.contains("해석 노트"))
        assertTrue(md.contains("성자 그리스도"))
        assertTrue(md.contains("[x] covers_whole_passage"))
        assertTrue(md.contains("(검토 완료)"))
        assertTrue(md.contains("1. 영원하신 말씀"))
        assertTrue(md.contains("  - 1.1 태초에 계심"))
        assertTrue(md.contains("실천 여부**: ✓"))
        assertTrue(md.contains("점검일**: 2026-06-01"))
    }

    @Test
    fun `english labels are used when EN is selected`() {
        val snapshot = StudyReportSnapshot(
            session = session,
            verses = emptyList(),
            observations = emptyList(),
            markups = emptyList(),
            markupLinks = emptyList(),
            divisions = emptyList(),
            characterTagsByDivision = emptyMap(),
            allCharacterTags = emptyList(),
            interpretations = emptyList(),
            themeChecks = emptyList(),
            propositions = emptyList(),
            outline = emptyList(),
            application = null
        )
        val md = StudyMarkdownExporter(StudyMarkdownExporter.Labels.forLanguage("EN")).export(snapshot)
        assertTrue(md.contains("## Observations"))
        assertTrue(md.contains("## Main theme"))
        assertTrue(md.contains("> No entries."))
    }
}
