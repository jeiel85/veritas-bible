package com.veritasbible.app.study.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 귀납적 성경연구 워크벤치의 1차 데이터 모델.
 *
 * 기존 [com.veritasbible.app.data.Note]는 절 단위 빠른 메모 용도로 그대로 유지하고,
 * 연구 세션은 별도의 테이블 군으로 분리한다.
 * 자세한 분리 사유는 docs/inductive_bible_study_design_bundle/docs/DECISION_LOG.md 참고.
 */

object StudyStage {
    const val PREPARATION = "preparation"
    const val OBSERVATION = "observation"
    const val STRUCTURE = "structure"
    const val CHARACTERIZATION = "characterization"
    const val DIVISION = "division"
    const val INTERPRETATION = "interpretation"
    const val THEME = "theme"
    const val PROPOSITION = "proposition"
    const val OUTLINE = "outline"
    const val APPLICATION = "application"
    const val REPORT = "report"
}

object StudyStatus {
    const val DRAFT = "draft"
    const val IN_PROGRESS = "in_progress"
    const val REVIEW = "review"
    const val COMPLETED = "completed"
    const val ARCHIVED = "archived"
}

object StudyType {
    const val PASSAGE = "passage"
    const val CHAPTER = "chapter"
    const val BOOK = "book"
    const val TOPIC = "topic"
    const val PERSON = "person"
    const val PROPHECY = "prophecy"
}

/** 본문 마킹 타입. FEATURE_SPEC §4 와 DATA_MODEL §4 markType 을 따른다. */
object MarkType {
    const val SUBJECT = "subject"
    const val VERB = "verb"
    const val OBJECT = "object"
    const val CONNECTIVE = "connective"
    const val REPEATED_WORD = "repeated_word"
    const val KEYWORD = "keyword"
    const val COMMAND = "command"
    const val REQUEST = "request"
    const val STATEMENT = "statement"
    const val NARRATIVE = "narrative"
    const val CAUSE = "cause"
    const val RESULT = "result"
    const val CONTRAST = "contrast"
    const val PURPOSE = "purpose"
    const val CONDITION = "condition"
    const val BASIS = "basis"
    const val DEFINITION = "definition"
    const val APPLICATION_CLUE = "application_clue"

    /** 모바일 메인 UI에 우선 노출할 핵심 5종. */
    val ESSENTIAL: List<String> = listOf(SUBJECT, VERB, OBJECT, CONNECTIVE, KEYWORD)

    /** 보조 메뉴(더보기)에서 펼치는 확장 타입. */
    val ADVANCED: List<String> = listOf(
        REPEATED_WORD, COMMAND, REQUEST, STATEMENT, NARRATIVE,
        CAUSE, RESULT, CONTRAST, PURPOSE, CONDITION, BASIS,
        DEFINITION, APPLICATION_CLUE
    )

    val ALL: List<String> = ESSENTIAL + ADVANCED
}

/** 마킹 간 연결 타입. DATA_MODEL §5 linkType. */
object LinkType {
    const val SUBJECT_VERB = "subject_verb"
    const val VERB_OBJECT = "verb_object"
    const val CAUSE_RESULT = "cause_result"
    const val CONTRAST = "contrast"
    const val BASIS_CLAIM = "basis_claim"
    const val EXPLANATION = "explanation"
    const val THEME_SUPPORT = "theme_support"
    const val PARALLEL = "parallel"
    const val APPLICATION_BASIS = "application_basis"

    val ALL: List<String> = listOf(
        SUBJECT_VERB, VERB_OBJECT, CAUSE_RESULT, CONTRAST,
        BASIS_CLAIM, EXPLANATION, THEME_SUPPORT, PARALLEL, APPLICATION_BASIS
    )
}

/** 성격 태그. BIBLE_STUDY_METHOD_MAPPING §4. */
object CharacterTagType {
    const val DEFINITION = "definition"
    const val MEANING = "meaning"
    const val REASON = "reason"
    const val CAUSE = "cause"
    const val RESULT = "result"
    const val PURPOSE = "purpose"
    const val CONDITION = "condition"
    const val PROCESS = "process"
    const val METHOD = "method"
    const val FEATURE = "feature"
    const val BASIS = "basis"
    const val RELATION = "relation"
    const val CONTRAST = "contrast"
    const val APPLICATION = "application"

    val ALL: List<String> = listOf(
        DEFINITION, MEANING, REASON, CAUSE, RESULT, PURPOSE,
        CONDITION, PROCESS, METHOD, FEATURE, BASIS, RELATION,
        CONTRAST, APPLICATION
    )
}

/** 성격 태그가 어떤 객체에 붙는지 식별. DATA_MODEL §8 targetType. */
object CharacterTagTarget {
    const val DIVISION = "division"
    const val MARKUP = "markup"
    const val VERSE = "verse"
    const val SESSION = "session"
}

object ObservationQuestion {
    /** GOAL_02 4번에서 명시한 8개 질문 템플릿의 stable key. */
    const val REPEATED_WORD = "repeated_word"
    const val MAIN_SUBJECT = "main_subject"
    const val MAIN_VERB = "main_verb"
    const val TONE = "tone"
    const val CONNECTIVES = "connectives"
    const val CAUSE_RESULT = "cause_result"
    const val CONTRAST = "contrast"
    const val EXPLICIT_CONCLUSION = "explicit_conclusion"
    const val FREE = "free"

    val TEMPLATE_KEYS: List<String> = listOf(
        REPEATED_WORD, MAIN_SUBJECT, MAIN_VERB, TONE,
        CONNECTIVES, CAUSE_RESULT, CONTRAST, EXPLICIT_CONCLUSION
    )
}

@Entity(
    tableName = "study_markups",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyMarkup(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val verseId: Int,
    val book: String,
    val chapter: Int,
    val verse: Int,
    val startOffset: Int,
    val endOffset: Int,
    val selectedText: String,
    val markType: String,
    val memo: String? = null,
    val colorKey: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_markup_links",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyMarkup::class,
            parentColumns = ["id"],
            childColumns = ["fromMarkupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyMarkup::class,
            parentColumns = ["id"],
            childColumns = ["toMarkupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("fromMarkupId"), Index("toMarkupId")]
)
data class StudyMarkupLink(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val fromMarkupId: String,
    val toMarkupId: String,
    val linkType: String,
    val memo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

/** 핵심주제 검증 체크리스트 키. Goal 3. */
object ThemeCheckKey {
    const val COVERS_WHOLE_PASSAGE = "covers_whole_passage"
    const val SUPPORTED_BY_DIVISIONS = "supported_by_divisions"
    const val GROUNDED_IN_TEXT = "grounded_in_text"
    const val INTERPRETATION_BEFORE_APPLICATION = "interpretation_before_application"
    const val FREE_FROM_BIAS = "free_from_bias"

    val ALL: List<String> = listOf(
        COVERS_WHOLE_PASSAGE,
        SUPPORTED_BY_DIVISIONS,
        GROUNDED_IN_TEXT,
        INTERPRETATION_BEFORE_APPLICATION,
        FREE_FROM_BIAS
    )
}

/** 콤마 구분 ID 리스트 ↔ List<String> 변환 헬퍼. nullable 보존. */
object LinkedIds {
    fun decode(csv: String?): List<String> =
        csv?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    fun encode(ids: List<String>): String? =
        ids.filter { it.isNotBlank() }.distinct().takeIf { it.isNotEmpty() }?.joinToString(",")
}

/** 명제 검토 상태. DATA_MODEL §11 reviewStatus. */
object PropositionStatus {
    const val DRAFT = "draft"
    const val REVIEWED = "reviewed"
    const val NEEDS_REVISION = "needs_revision"

    val ALL: List<String> = listOf(DRAFT, REVIEWED, NEEDS_REVISION)
}

@Entity(
    tableName = "study_interpretations",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyInterpretation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val keyword: String = "",
    val plainMeaning: String = "",
    val contextualMeaning: String = "",
    val evidence: String = "",
    val crossRefs: String = "",
    val conclusion: String = "",
    /** Goal 4: 이 해석이 어떤 마킹에서 출발했는지 명시적으로 가리킨다. nullable. */
    val linkedMarkupId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_theme_checks",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyThemeCheck(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val checkKey: String,
    val isChecked: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_propositions",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyProposition(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val sentence: String = "",
    val supportingRefs: String = "",
    val reviewStatus: String = PropositionStatus.DRAFT,
    /** Goal 4: 명제의 본문 근거를 마킹·단락 ID 리스트(콤마 구분)로 명시한다. */
    val linkedMarkupIds: String? = null,
    val linkedDivisionIds: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_outline_nodes",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("parentId")]
)
data class StudyOutlineNode(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val parentId: String? = null,
    @ColumnInfo(name = "orderIndex") val orderIndex: Int = 0,
    val level: Int = 0,
    val title: String,
    val verseRange: String? = null,
    val summary: String? = null,
    /** 단락 구분(`study_divisions.id`)과의 선택적 연결. */
    val linkedDivisionId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_character_tags",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("targetType"), Index("targetId")]
)
data class StudyCharacterTag(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val targetType: String,
    val targetId: String,
    val tag: String,
    val memo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val translationId: String = "ko-rev-1910+web",
    val book: String,
    val bookEn: String,
    val bookId: Int,
    val startChapter: Int,
    val startVerse: Int,
    val endChapter: Int,
    val endVerse: Int,
    val studyType: String = StudyType.PASSAGE,
    val status: String = StudyStatus.DRAFT,
    val currentStage: String = StudyStage.OBSERVATION,
    // 핵심주제는 빠른 조회를 위해 세션에 함께 보관한다.
    val mainTheme: String = "",
    val mainPropositionMemo: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val archivedAt: Long? = null
) {
    companion object
}

@Entity(
    tableName = "study_observations",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyObservation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val questionKey: String = "free",
    val questionText: String = "",
    val answer: String,
    val verseRefs: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_divisions",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyDivision(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    @ColumnInfo(name = "orderIndex") val orderIndex: Int,
    val startChapter: Int,
    val startVerse: Int,
    val endChapter: Int,
    val endVerse: Int,
    val title: String,
    val summary: String? = null,
    val characterTag: String? = null,
    val relationToMainTheme: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}

@Entity(
    tableName = "study_applications",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class StudyApplication(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val truthStatement: String = "",
    val mirrorStatement: String = "",
    val adjustmentStatement: String = "",
    val actionPlan: String = "",
    val dueDate: String? = null,
    val checkedAt: Long? = null,
    /** Goal 3: 실천 여부 토글. true 이면 사용자가 실천했다고 표시한 상태. */
    val practiced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object
}
