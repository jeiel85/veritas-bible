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
import com.veritasbible.app.study.repository.StudyBackup
import com.veritasbible.app.study.repository.StudyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 백업/복원 roundtrip 무결성 검증.
 *
 * 한 인스턴스에 데이터를 가득 채우고 [StudyBackup.exportTo] 로 JSON 을 만든 뒤,
 * 비어 있는 새 DB 인스턴스에 [StudyBackup.importFrom] 으로 동일하게 복원되는지 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyBackupTest {

    private lateinit var src: AppDatabase
    private lateinit var dst: AppDatabase

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        src = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dst = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        src.close()
        dst.close()
    }

    @Test
    fun `roundtrip preserves every study domain table`() = runTest {
        val srcRepo = StudyRepository(
            src.studySessionDao(), src.studyObservationDao(), src.studyDivisionDao(),
            src.studyApplicationDao(), src.studyMarkupDao(), src.studyMarkupLinkDao(),
            src.studyCharacterTagDao(), src.studyInterpretationDao(), src.studyThemeCheckDao(),
            src.studyPropositionDao(), src.studyOutlineNodeDao()
        )
        val dstRepo = StudyRepository(
            dst.studySessionDao(), dst.studyObservationDao(), dst.studyDivisionDao(),
            dst.studyApplicationDao(), dst.studyMarkupDao(), dst.studyMarkupLinkDao(),
            dst.studyCharacterTagDao(), dst.studyInterpretationDao(), dst.studyThemeCheckDao(),
            dst.studyPropositionDao(), dst.studyOutlineNodeDao()
        )

        // -- Populate src
        val session = srcRepo.createSession(
            title = "요한복음 1 연구",
            book = "요한복음", bookEn = "John", bookId = 43,
            startChapter = 1, startVerse = 1, endChapter = 1, endVerse = 18
        )
        srcRepo.upsertObservationTemplate(
            session.id, ObservationQuestion.REPEATED_WORD, "반복?", "말씀"
        )
        srcRepo.addObservation(session.id, "1절과 14절은 수미상관")
        srcRepo.addDivision(session.id, "말씀의 본질", 1, 1, 1, 5, summary = "그가 누구이신가")
        val subj = srcRepo.addMarkup(session.id, 1, "요한복음", 1, 1, 0, 3, "태초에", MarkType.SUBJECT)
        val verb = srcRepo.addMarkup(session.id, 1, "요한복음", 1, 1, 4, 8, "계시니라", MarkType.VERB)
        srcRepo.addMarkupLink(session.id, subj.id, verb.id, LinkType.SUBJECT_VERB)
        val division = srcRepo.observeDivisions(session.id).first().first()
        srcRepo.addCharacterTag(session.id, CharacterTagTarget.DIVISION, division.id, CharacterTagType.DEFINITION)
        srcRepo.addInterpretation(
            session.id, "말씀", "로고스", "성육신 이전", "1:1", "창 1:1", "그리스도",
            linkedMarkupId = subj.id
        )
        srcRepo.upsertThemeCheck(session.id, ThemeCheckKey.COVERS_WHOLE_PASSAGE, true, "포괄")
        srcRepo.addProposition(
            session.id, "예수님은 영원한 말씀이시다.", "1:1, 1:14", PropositionStatus.REVIEWED,
            linkedMarkupIds = "${subj.id},${verb.id}",
            linkedDivisionIds = division.id
        )
        val root = srcRepo.addOutlineNode(session.id, "I. 영원한 말씀", 0)
        srcRepo.addOutlineNode(session.id, "1. 태초에 계심", 1, parentId = root.id)
        srcRepo.saveApplication(session.id, "복", "거울", "조정", "행동", dueDate = "2026-06-01", practiced = true)

        // -- Export from src, import into dst
        val json = StudyBackup.exportTo(src)
        StudyBackup.importFrom(dst, json, wipeBeforeImport = true)

        // -- Verify dst mirrors src
        val sessions = dstRepo.observeSessions().first()
        assertEquals(1, sessions.size)
        assertEquals(session.id, sessions.first().id)
        assertEquals("요한복음 1 연구", sessions.first().title)

        val obs = dstRepo.observeObservations(session.id).first()
        assertEquals(2, obs.size)

        val divisions = dstRepo.observeDivisions(session.id).first()
        assertEquals(1, divisions.size)

        val markups = dstRepo.observeMarkups(session.id).first()
        assertEquals(2, markups.size)

        val links = dstRepo.observeMarkupLinks(session.id).first()
        assertEquals(1, links.size)

        val tags = dstRepo.observeCharacterTags(session.id).first()
        assertEquals(1, tags.size)

        val interp = dstRepo.observeInterpretations(session.id).first()
        assertEquals(1, interp.size)
        assertEquals(subj.id, interp.first().linkedMarkupId)

        val checks = dstRepo.observeThemeChecks(session.id).first()
        assertEquals(1, checks.size)
        assertTrue(checks.first().isChecked)

        val props = dstRepo.observePropositions(session.id).first()
        assertEquals(1, props.size)
        assertEquals(PropositionStatus.REVIEWED, props.first().reviewStatus)
        assertNotNull(props.first().linkedMarkupIds)
        assertEquals(division.id, props.first().linkedDivisionIds)

        val outline = dstRepo.observeOutlineNodes(session.id).first()
        assertEquals(2, outline.size)

        val app = dstRepo.observeApplication(session.id).first()
        assertNotNull(app)
        assertTrue(app!!.practiced)
        assertEquals("2026-06-01", app.dueDate)
    }

    @Test
    fun `schemaVersion field is included`() = runTest {
        val json = StudyBackup.exportTo(src)
        assertEquals(StudyBackup.SCHEMA_VERSION, json.optInt("schemaVersion"))
    }
}
