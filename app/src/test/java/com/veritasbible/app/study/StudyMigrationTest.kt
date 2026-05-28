package com.veritasbible.app.study

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.veritasbible.app.data.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Room [MigrationTestHelper] 기반 v1 → v5 전구간 마이그레이션 검증.
 *
 * `app/schemas/com.veritasbible.app.data.AppDatabase/{1..5}.json` 이 test
 * sourceSet 의 assets 로 노출되어 있어, 각 시작 버전의 schema 를
 * `MigrationTestHelper.createDatabase` 로 직접 생성한 뒤 우리 [MIGRATION_*]
 * 객체들로 v5 까지 끌어올린다.
 *
 * `runMigrationsAndValidate(name, version, validateDroppedTables=true, ...)`
 * 는 최종 schema 가 v5.json (이 DB 클래스에 대해 KSP 가 export 한 최신
 * schema) 와 일치하는지 자동 검증한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private val dbName = "veritas-migration-test.db"

    @Test
    fun `v1 to v5 preserves notes highlights logs and goals`() {
        helper.createDatabase(dbName, 1).use { db ->
            db.execSQL(
                "INSERT INTO notes (id, verseId, book, chapter, verse, content, createdAt) " +
                    "VALUES (1, 99, '요한복음', 1, 1, 'encrypted-blob', 1700000000)"
            )
            db.execSQL(
                "INSERT INTO reading_logs (id, dateString, countVerses, countChapters, sessionDurationSec) " +
                    "VALUES (1, '2026-05-01', 10, 1, 300)"
            )
            db.execSQL(
                "INSERT INTO reading_goals (id, targetChapters, completedChapters, lastActiveDateString) " +
                    "VALUES (1, 100, 5, '2026-05-01')"
            )
        }

        runAllMigrationsAndValidate().use { db ->
            db.query("SELECT content FROM notes WHERE id = 1").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("encrypted-blob", c.getString(0))
            }
            db.query("SELECT countVerses FROM reading_logs WHERE id = 1").use { c ->
                c.moveToFirst(); assertEquals(10, c.getInt(0))
            }
            db.query("SELECT targetChapters, completedChapters FROM reading_goals WHERE id = 1").use { c ->
                c.moveToFirst()
                assertEquals(100, c.getInt(0))
                assertEquals(5, c.getInt(1))
            }
            // v5 신규 테이블 존재 확인
            assertTable(db, "study_interpretations")
            assertTable(db, "study_theme_checks")
            assertTable(db, "study_propositions")
            assertTable(db, "study_outline_nodes")
            // v5 신규 컬럼 존재 확인
            assertColumn(db, "study_applications", "practiced")
            assertColumn(db, "study_interpretations", "linkedMarkupId")
            assertColumn(db, "study_propositions", "linkedMarkupIds")
            assertColumn(db, "study_propositions", "linkedDivisionIds")
        }
    }

    @Test
    fun `v2 to v5 preserves study session row`() {
        helper.createDatabase(dbName, 2).use { db ->
            db.execSQL(
                "INSERT INTO study_sessions (id, title, translationId, book, bookEn, bookId, " +
                    "startChapter, startVerse, endChapter, endVerse, studyType, status, currentStage, " +
                    "mainTheme, mainPropositionMemo, createdAt, updatedAt) " +
                    "VALUES ('sess-A', '롬1 연구', 'ko-rev-1910+web', '로마서', 'Romans', 45, " +
                    "1, 1, 1, 17, 'passage', 'in_progress', 'observation', '', '', 1, 1)"
            )
        }
        runAllMigrationsAndValidate().use { db ->
            db.query("SELECT title FROM study_sessions WHERE id = 'sess-A'").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("롬1 연구", c.getString(0))
            }
        }
    }

    @Test
    fun `v3 to v5 preserves markup data and adds practiced column with default 0`() {
        helper.createDatabase(dbName, 3).use { db ->
            db.execSQL(
                "INSERT INTO study_sessions (id, title, translationId, book, bookEn, bookId, " +
                    "startChapter, startVerse, endChapter, endVerse, studyType, status, currentStage, " +
                    "mainTheme, mainPropositionMemo, createdAt, updatedAt) " +
                    "VALUES ('sess-B', '요1', 'ko', '요한복음', 'John', 43, 1, 1, 1, 18, 'passage', " +
                    "'in_progress', 'observation', '', '', 1, 1)"
            )
            db.execSQL(
                "INSERT INTO study_markups (id, sessionId, verseId, book, chapter, verse, startOffset, " +
                    "endOffset, selectedText, markType, memo, colorKey, createdAt, updatedAt) " +
                    "VALUES ('mk-B', 'sess-B', 1, '요한복음', 1, 1, 0, 3, '태초에', 'subject', NULL, NULL, 1, 1)"
            )
            db.execSQL(
                "INSERT INTO study_applications (id, sessionId, truthStatement, mirrorStatement, " +
                    "adjustmentStatement, actionPlan, dueDate, checkedAt, createdAt, updatedAt) " +
                    "VALUES ('app-B', 'sess-B', '진리', '거울', '조정', '실천', NULL, NULL, 1, 1)"
            )
        }
        runAllMigrationsAndValidate().use { db ->
            db.query("SELECT selectedText, markType FROM study_markups WHERE id = 'mk-B'").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("태초에", c.getString(0))
                assertEquals("subject", c.getString(1))
            }
            db.query("SELECT truthStatement, practiced FROM study_applications WHERE id = 'app-B'").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("진리", c.getString(0))
                assertEquals(0, c.getInt(1))
            }
        }
    }

    @Test
    fun `v4 to v5 adds link columns with null default and preserves existing rows`() {
        helper.createDatabase(dbName, 4).use { db ->
            db.execSQL(
                "INSERT INTO study_sessions (id, title, translationId, book, bookEn, bookId, " +
                    "startChapter, startVerse, endChapter, endVerse, studyType, status, currentStage, " +
                    "mainTheme, mainPropositionMemo, createdAt, updatedAt) " +
                    "VALUES ('sess-C', '엡1', 'ko', '에베소서', 'Ephesians', 49, 1, 3, 1, 14, 'passage', " +
                    "'in_progress', 'observation', '', '', 1, 1)"
            )
            db.execSQL(
                "INSERT INTO study_interpretations (id, sessionId, keyword, plainMeaning, contextualMeaning, " +
                    "evidence, crossRefs, conclusion, createdAt, updatedAt) " +
                    "VALUES ('int-C', 'sess-C', '신령한 복', '영적 축복', '그리스도 안에서', '엡 1:3', '', '주제 = 그리스도', 1, 1)"
            )
            db.execSQL(
                "INSERT INTO study_propositions (id, sessionId, sentence, supportingRefs, reviewStatus, createdAt, updatedAt) " +
                    "VALUES ('prop-C', 'sess-C', '하나님은 모든 신령한 복을 주신다.', '엡 1:3-14', 'draft', 1, 1)"
            )
        }
        runAllMigrationsAndValidate().use { db ->
            db.query("SELECT keyword, linkedMarkupId FROM study_interpretations WHERE id = 'int-C'").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("신령한 복", c.getString(0))
                assertTrue(c.isNull(1))
            }
            db.query(
                "SELECT sentence, linkedMarkupIds, linkedDivisionIds FROM study_propositions WHERE id = 'prop-C'"
            ).use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("하나님은 모든 신령한 복을 주신다.", c.getString(0))
                assertTrue(c.isNull(1))
                assertTrue(c.isNull(2))
            }
        }
    }

    // ------------------------------------------------------------------------

    private fun runAllMigrationsAndValidate(): SupportSQLiteDatabase =
        helper.runMigrationsAndValidate(
            dbName,
            AppDatabase.LATEST_VERSION,
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6
        )

    @Test
    fun `v5 to v6 adds paragraphStart column with default 0 and preserves verse rows`() {
        helper.createDatabase(dbName, 5).use { db ->
            db.execSQL(
                "INSERT INTO bible_verses (id, book, bookEn, bookId, chapter, verse, text, textEn, highlightColor) " +
                    "VALUES (1, '요한복음', 'John', 43, 1, 1, '태초에 말씀', 'In the beginning', NULL)"
            )
        }
        runAllMigrationsAndValidate().use { db ->
            assertColumn(db, "bible_verses", "paragraphStart")
            db.query("SELECT text, paragraphStart FROM bible_verses WHERE id = 1").use { c ->
                assertEquals(1, c.count); c.moveToFirst()
                assertEquals("태초에 말씀", c.getString(0))
                assertEquals(0, c.getInt(1))
            }
        }
    }

    private fun assertTable(db: SupportSQLiteDatabase, table: String) {
        db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf<Any>(table)
        ).use { c ->
            assertEquals("table $table is missing", 1, c.count)
        }
    }

    private fun assertColumn(db: SupportSQLiteDatabase, table: String, column: String) {
        db.query("PRAGMA table_info(`$table`)").use { c ->
            var found = false
            while (c.moveToNext()) {
                if (c.getString(c.getColumnIndexOrThrow("name")) == column) { found = true; break }
            }
            assertTrue("$table is missing column $column", found)
        }
    }
}
