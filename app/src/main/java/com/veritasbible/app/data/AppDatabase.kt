package com.veritasbible.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.veritasbible.app.study.data.StudyThemeCheck
import com.veritasbible.app.study.data.StudyThemeCheckDao

@Database(
    entities = [
        BibleVerse::class,
        Note::class,
        ReadingLog::class,
        ReadingGoal::class,
        StudySession::class,
        StudyObservation::class,
        StudyDivision::class,
        StudyApplication::class,
        StudyMarkup::class,
        StudyMarkupLink::class,
        StudyCharacterTag::class,
        StudyInterpretation::class,
        StudyThemeCheck::class,
        StudyProposition::class,
        StudyOutlineNode::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun studyObservationDao(): StudyObservationDao
    abstract fun studyDivisionDao(): StudyDivisionDao
    abstract fun studyApplicationDao(): StudyApplicationDao
    abstract fun studyMarkupDao(): StudyMarkupDao
    abstract fun studyMarkupLinkDao(): StudyMarkupLinkDao
    abstract fun studyCharacterTagDao(): StudyCharacterTagDao
    abstract fun studyInterpretationDao(): StudyInterpretationDao
    abstract fun studyThemeCheckDao(): StudyThemeCheckDao
    abstract fun studyPropositionDao(): StudyPropositionDao
    abstract fun studyOutlineNodeDao(): StudyOutlineNodeDao

    companion object {
        /** Room DB 최신 버전. 테스트와 도구 코드가 참조한다. */
        const val LATEST_VERSION: Int = 5

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Goal 1: 연구 모듈 테이블 4종을 신규 생성. 기존 사용자 데이터는 보존.
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_sessions` (" +
                        "`id` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`translationId` TEXT NOT NULL, " +
                        "`book` TEXT NOT NULL, " +
                        "`bookEn` TEXT NOT NULL, " +
                        "`bookId` INTEGER NOT NULL, " +
                        "`startChapter` INTEGER NOT NULL, " +
                        "`startVerse` INTEGER NOT NULL, " +
                        "`endChapter` INTEGER NOT NULL, " +
                        "`endVerse` INTEGER NOT NULL, " +
                        "`studyType` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`currentStage` TEXT NOT NULL, " +
                        "`mainTheme` TEXT NOT NULL, " +
                        "`mainPropositionMemo` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`completedAt` INTEGER, " +
                        "`archivedAt` INTEGER, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_observations` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`questionKey` TEXT NOT NULL, " +
                        "`questionText` TEXT NOT NULL, " +
                        "`answer` TEXT NOT NULL, " +
                        "`verseRefs` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_observations_sessionId` " +
                        "ON `study_observations` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_divisions` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`orderIndex` INTEGER NOT NULL, " +
                        "`startChapter` INTEGER NOT NULL, " +
                        "`startVerse` INTEGER NOT NULL, " +
                        "`endChapter` INTEGER NOT NULL, " +
                        "`endVerse` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`summary` TEXT, " +
                        "`characterTag` TEXT, " +
                        "`relationToMainTheme` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_divisions_sessionId` " +
                        "ON `study_divisions` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_applications` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`truthStatement` TEXT NOT NULL, " +
                        "`mirrorStatement` TEXT NOT NULL, " +
                        "`adjustmentStatement` TEXT NOT NULL, " +
                        "`actionPlan` TEXT NOT NULL, " +
                        "`dueDate` TEXT, " +
                        "`checkedAt` INTEGER, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_applications_sessionId` " +
                        "ON `study_applications` (`sessionId`)"
                )
            }
        }

        // Goal 2: 마킹/연결/성격 태그 테이블 추가. 기존 연구 데이터는 보존.
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_markups` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`verseId` INTEGER NOT NULL, " +
                        "`book` TEXT NOT NULL, " +
                        "`chapter` INTEGER NOT NULL, " +
                        "`verse` INTEGER NOT NULL, " +
                        "`startOffset` INTEGER NOT NULL, " +
                        "`endOffset` INTEGER NOT NULL, " +
                        "`selectedText` TEXT NOT NULL, " +
                        "`markType` TEXT NOT NULL, " +
                        "`memo` TEXT, " +
                        "`colorKey` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_markups_sessionId` " +
                        "ON `study_markups` (`sessionId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_markup_links` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`fromMarkupId` TEXT NOT NULL, " +
                        "`toMarkupId` TEXT NOT NULL, " +
                        "`linkType` TEXT NOT NULL, " +
                        "`memo` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`fromMarkupId`) REFERENCES `study_markups`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`toMarkupId`) REFERENCES `study_markups`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_markup_links_sessionId` " +
                        "ON `study_markup_links` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_markup_links_fromMarkupId` " +
                        "ON `study_markup_links` (`fromMarkupId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_markup_links_toMarkupId` " +
                        "ON `study_markup_links` (`toMarkupId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_character_tags` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`targetType` TEXT NOT NULL, " +
                        "`targetId` TEXT NOT NULL, " +
                        "`tag` TEXT NOT NULL, " +
                        "`memo` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_character_tags_sessionId` " +
                        "ON `study_character_tags` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_character_tags_targetType` " +
                        "ON `study_character_tags` (`targetType`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_character_tags_targetId` " +
                        "ON `study_character_tags` (`targetId`)"
                )
            }
        }

        // Goal 3: 해석·핵심주제 검증·명제·개요 테이블 신규, study_applications에 practiced 컬럼 추가.
        // 기존 적용 노트 데이터는 보존되며 practiced 기본값은 0(false).
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `study_applications` " +
                        "ADD COLUMN `practiced` INTEGER NOT NULL DEFAULT 0"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_interpretations` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`keyword` TEXT NOT NULL, " +
                        "`plainMeaning` TEXT NOT NULL, " +
                        "`contextualMeaning` TEXT NOT NULL, " +
                        "`evidence` TEXT NOT NULL, " +
                        "`crossRefs` TEXT NOT NULL, " +
                        "`conclusion` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_interpretations_sessionId` " +
                        "ON `study_interpretations` (`sessionId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_theme_checks` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`checkKey` TEXT NOT NULL, " +
                        "`isChecked` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_theme_checks_sessionId` " +
                        "ON `study_theme_checks` (`sessionId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_propositions` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`sentence` TEXT NOT NULL, " +
                        "`supportingRefs` TEXT NOT NULL, " +
                        "`reviewStatus` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_propositions_sessionId` " +
                        "ON `study_propositions` (`sessionId`)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_outline_nodes` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`parentId` TEXT, " +
                        "`orderIndex` INTEGER NOT NULL, " +
                        "`level` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`verseRange` TEXT, " +
                        "`summary` TEXT, " +
                        "`linkedDivisionId` TEXT, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`sessionId`) REFERENCES `study_sessions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_outline_nodes_sessionId` " +
                        "ON `study_outline_nodes` (`sessionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_study_outline_nodes_parentId` " +
                        "ON `study_outline_nodes` (`parentId`)"
                )
            }
        }

        // Goal 4: 명시적 본문 근거 링크. interpretation/proposition 에 nullable 컬럼 추가.
        // ALTER TABLE add column 만으로 충분하므로 기존 데이터 무손실.
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `study_interpretations` ADD COLUMN `linkedMarkupId` TEXT"
                )
                db.execSQL(
                    "ALTER TABLE `study_propositions` ADD COLUMN `linkedMarkupIds` TEXT"
                )
                db.execSQL(
                    "ALTER TABLE `study_propositions` ADD COLUMN `linkedDivisionIds` TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "veritas_bible.db"
                )
                    // destructive fallback은 사용하지 않는다. 데이터 보존이 우선.
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
