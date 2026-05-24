package com.veritasbible.app.study.report

import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.study.data.MarkType
import com.veritasbible.app.study.data.ObservationQuestion
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 연구 세션 전체 결과를 Markdown 문자열로 직렬화한다.
 *
 * 외부 라이브러리 없이 순수 문자열 빌더로 작성한다. PDF 출력은
 * 별도 단계로 분리해 후속 Goal에서 다룬다.
 *
 * 라벨은 한·영 두 가지를 지원한다. 영어가 아닌 모든 값은 기본
 * 한국어로 처리해 출시 환경에서 누락된 키가 있어도 작동하도록 한다.
 */
class StudyMarkdownExporter(private val labels: Labels = Labels.KO) {

    /**
     * 사용자 노출 라벨. UI 문자열은 [com.veritasbible.app.util.LanguageManager]에
     * 모여 있지만 리포트는 컨텍스트 의존을 피하기 위해 자체 라벨을 둔다.
     */
    data class Labels(
        val passageRange: String,
        val generatedAt: String,
        val observationSummary: String,
        val markupSummary: String,
        val divisions: String,
        val characterTags: String,
        val interpretations: String,
        val mainTheme: String,
        val themeChecks: String,
        val propositions: String,
        val outline: String,
        val application: String,
        val applicationTruth: String,
        val applicationMirror: String,
        val applicationAdjust: String,
        val applicationAction: String,
        val applicationDue: String,
        val applicationPracticed: String,
        val noEntries: String,
        val divisionSummary: String,
        val markupNotePrefix: String,
        val propositionStatusDraft: String,
        val propositionStatusReviewed: String,
        val propositionStatusNeedsRevision: String
    ) {
        companion object {
            val KO = Labels(
                passageRange = "본문 범위",
                generatedAt = "생성일",
                observationSummary = "관찰 요약",
                markupSummary = "본문 마킹 요약",
                divisions = "단락 구분",
                characterTags = "성격 태그",
                interpretations = "해석 노트",
                mainTheme = "핵심주제",
                themeChecks = "핵심주제 검증",
                propositions = "명제",
                outline = "개요",
                application = "적용",
                applicationTruth = "본문이 보여주는 진리",
                applicationMirror = "거울 — 내 삶을 비추는 부분",
                applicationAdjust = "조정해야 할 부분",
                applicationAction = "실천 계획",
                applicationDue = "점검일",
                applicationPracticed = "실천 여부",
                noEntries = "기록 없음.",
                divisionSummary = "요약",
                markupNotePrefix = "메모",
                propositionStatusDraft = "초안",
                propositionStatusReviewed = "검토 완료",
                propositionStatusNeedsRevision = "수정 필요"
            )

            val EN = Labels(
                passageRange = "Passage",
                generatedAt = "Generated at",
                observationSummary = "Observations",
                markupSummary = "Markups",
                divisions = "Divisions",
                characterTags = "Character tags",
                interpretations = "Interpretation notes",
                mainTheme = "Main theme",
                themeChecks = "Main theme verification",
                propositions = "Propositions",
                outline = "Outline",
                application = "Application",
                applicationTruth = "Truth from the passage",
                applicationMirror = "Mirror — what it exposes in me",
                applicationAdjust = "Adjustment to make",
                applicationAction = "Action plan",
                applicationDue = "Check date",
                applicationPracticed = "Practiced",
                noEntries = "No entries.",
                divisionSummary = "summary",
                markupNotePrefix = "note",
                propositionStatusDraft = "draft",
                propositionStatusReviewed = "reviewed",
                propositionStatusNeedsRevision = "needs revision"
            )

            fun forLanguage(appLanguage: String): Labels =
                if (appLanguage.uppercase() == "EN") EN else KO
        }
    }

    fun export(snapshot: StudyReportSnapshot): String = buildString {
        val sess = snapshot.session
        append("# ${sess.title}\n\n")
        append("- **${labels.passageRange}**: ${formatRange(sess)}\n")
        append("- **${labels.generatedAt}**: ${formatTimestamp(snapshot.generatedAt)}\n")
        appendLine()

        // Observation summary
        section(labels.observationSummary)
        if (snapshot.observations.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeObservations(snapshot.observations)

        // Markup summary
        section(labels.markupSummary)
        if (snapshot.markups.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeMarkups(snapshot.markups, snapshot.markupLinks)

        // Divisions
        section(labels.divisions)
        if (snapshot.divisions.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeDivisions(snapshot.divisions, snapshot.characterTagsByDivision)

        // Character tags (session-level summary)
        section(labels.characterTags)
        if (snapshot.allCharacterTags.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeCharacterTags(snapshot.allCharacterTags)

        // Interpretation notes
        section(labels.interpretations)
        if (snapshot.interpretations.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeInterpretations(snapshot.interpretations)

        // Main theme + verification
        section(labels.mainTheme)
        if (sess.mainTheme.isBlank()) {
            appendLine("> ${labels.noEntries}")
        } else {
            appendLine(sess.mainTheme)
        }
        if (sess.mainPropositionMemo.isNotBlank()) {
            appendLine()
            appendLine("_${escape(sess.mainPropositionMemo)}_")
        }
        appendLine()

        section(labels.themeChecks)
        if (snapshot.themeChecks.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeThemeChecks(snapshot.themeChecks)

        // Propositions
        section(labels.propositions)
        if (snapshot.propositions.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writePropositions(snapshot.propositions)

        // Outline
        section(labels.outline)
        if (snapshot.outline.isEmpty()) appendLine("> ${labels.noEntries}").appendLine()
        else writeOutline(snapshot.outline)

        // Application
        section(labels.application)
        writeApplication(snapshot.application)
    }

    private fun StringBuilder.section(title: String) {
        appendLine("## $title")
        appendLine()
    }

    private fun StringBuilder.writeObservations(observations: List<StudyObservation>) {
        val templates = observations.filter { it.questionKey != ObservationQuestion.FREE }
        val frees = observations.filter { it.questionKey == ObservationQuestion.FREE }
        templates.forEach {
            val q = it.questionText.ifBlank { it.questionKey }
            appendLine("- **${escape(q)}** — ${escape(it.answer)}")
        }
        if (frees.isNotEmpty()) {
            if (templates.isNotEmpty()) appendLine()
            frees.forEach { appendLine("- ${escape(it.answer)}") }
        }
        appendLine()
    }

    private fun StringBuilder.writeMarkups(
        markups: List<StudyMarkup>,
        links: List<StudyMarkupLink>
    ) {
        markups.groupBy { "${it.chapter}:${it.verse}" }
            .toSortedMap()
            .forEach { (ref, list) ->
                appendLine("- **$ref**")
                list.forEach { m ->
                    val memo = m.memo?.takeIf { it.isNotBlank() }?.let { " — ${labels.markupNotePrefix}: ${escape(it)}" } ?: ""
                    appendLine("  - `${labelForMarkType(m.markType)}` “${escape(m.selectedText)}”$memo")
                }
            }
        if (links.isNotEmpty()) {
            appendLine()
            appendLine("**Links**")
            val byId = markups.associateBy { it.id }
            links.forEach { link ->
                val from = byId[link.fromMarkupId]?.selectedText ?: "?"
                val to = byId[link.toMarkupId]?.selectedText ?: "?"
                appendLine("- `${link.linkType}` ${escape(from)} → ${escape(to)}")
            }
        }
        appendLine()
    }

    private fun StringBuilder.writeDivisions(
        divisions: List<StudyDivision>,
        tagsByDivision: Map<String, List<StudyCharacterTag>>
    ) {
        divisions.forEach { d ->
            val range = "${d.startChapter}:${d.startVerse}~${d.endChapter}:${d.endVerse}"
            appendLine("- **$range — ${escape(d.title)}**")
            if (!d.summary.isNullOrBlank()) {
                appendLine("  - ${labels.divisionSummary}: ${escape(d.summary)}")
            }
            tagsByDivision[d.id].orEmpty().takeIf { it.isNotEmpty() }?.let { tags ->
                val joined = tags.joinToString(", ") { it.tag }
                appendLine("  - tags: $joined")
            }
        }
        appendLine()
    }

    private fun StringBuilder.writeCharacterTags(tags: List<StudyCharacterTag>) {
        tags.groupBy { it.targetType }.forEach { (type, list) ->
            appendLine("- **$type**: ${list.joinToString(", ") { it.tag }}")
        }
        appendLine()
    }

    private fun StringBuilder.writeInterpretations(notes: List<StudyInterpretation>) {
        notes.forEachIndexed { idx, n ->
            appendLine("### ${idx + 1}. ${escape(n.keyword.ifBlank { "(no keyword)" })}")
            if (n.plainMeaning.isNotBlank()) appendLine("- **plain meaning**: ${escape(n.plainMeaning)}")
            if (n.contextualMeaning.isNotBlank()) appendLine("- **contextual meaning**: ${escape(n.contextualMeaning)}")
            if (n.evidence.isNotBlank()) appendLine("- **evidence**: ${escape(n.evidence)}")
            if (n.crossRefs.isNotBlank()) appendLine("- **cross refs**: ${escape(n.crossRefs)}")
            if (n.conclusion.isNotBlank()) appendLine("- **conclusion**: ${escape(n.conclusion)}")
            appendLine()
        }
    }

    private fun StringBuilder.writeThemeChecks(checks: List<StudyThemeCheck>) {
        checks.forEach { c ->
            val mark = if (c.isChecked) "[x]" else "[ ]"
            appendLine("- $mark ${c.checkKey}${if (c.note.isNotBlank()) " — ${escape(c.note)}" else ""}")
        }
        appendLine()
    }

    private fun StringBuilder.writePropositions(propositions: List<StudyProposition>) {
        propositions.forEach { p ->
            val status = when (p.reviewStatus) {
                PropositionStatus.REVIEWED -> labels.propositionStatusReviewed
                PropositionStatus.NEEDS_REVISION -> labels.propositionStatusNeedsRevision
                else -> labels.propositionStatusDraft
            }
            appendLine("- (${status}) ${escape(p.sentence)}")
            if (p.supportingRefs.isNotBlank()) {
                appendLine("  - refs: ${escape(p.supportingRefs)}")
            }
        }
        appendLine()
    }

    private fun StringBuilder.writeOutline(nodes: List<StudyOutlineNode>) {
        val byParent: Map<String?, List<StudyOutlineNode>> =
            nodes.groupBy { it.parentId }.mapValues { (_, list) ->
                list.sortedBy { it.orderIndex }
            }

        fun writeChildren(parentId: String?, indent: Int) {
            byParent[parentId].orEmpty().forEach { node ->
                val pad = "  ".repeat(indent)
                val range = node.verseRange?.let { " ($it)" } ?: ""
                appendLine("$pad- ${escape(node.title)}$range")
                if (!node.summary.isNullOrBlank()) {
                    appendLine("$pad  - ${escape(node.summary)}")
                }
                writeChildren(node.id, indent + 1)
            }
        }
        writeChildren(null, 0)
        appendLine()
    }

    private fun StringBuilder.writeApplication(application: StudyApplication?) {
        if (application == null ||
            listOf(
                application.truthStatement,
                application.mirrorStatement,
                application.adjustmentStatement,
                application.actionPlan
            ).all { it.isBlank() }
        ) {
            appendLine("> ${labels.noEntries}")
            return
        }
        if (application.truthStatement.isNotBlank())
            appendLine("- **${labels.applicationTruth}**: ${escape(application.truthStatement)}")
        if (application.mirrorStatement.isNotBlank())
            appendLine("- **${labels.applicationMirror}**: ${escape(application.mirrorStatement)}")
        if (application.adjustmentStatement.isNotBlank())
            appendLine("- **${labels.applicationAdjust}**: ${escape(application.adjustmentStatement)}")
        if (application.actionPlan.isNotBlank())
            appendLine("- **${labels.applicationAction}**: ${escape(application.actionPlan)}")
        application.dueDate?.takeIf { it.isNotBlank() }?.let {
            appendLine("- **${labels.applicationDue}**: $it")
        }
        appendLine("- **${labels.applicationPracticed}**: ${if (application.practiced) "✓" else "—"}")
    }

    private fun labelForMarkType(type: String): String = when (type) {
        MarkType.SUBJECT -> "subject"
        MarkType.VERB -> "verb"
        MarkType.OBJECT -> "object"
        MarkType.CONNECTIVE -> "connective"
        else -> type
    }

    private fun formatRange(session: StudySession): String {
        val start = "${session.book} ${session.startChapter}:${session.startVerse}"
        val end = when {
            session.startChapter == session.endChapter && session.startVerse == session.endVerse -> ""
            session.startChapter == session.endChapter -> "-${session.endVerse}"
            else -> " ~ ${session.endChapter}:${session.endVerse}"
        }
        return start + end
    }

    private fun formatTimestamp(epochMillis: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return fmt.format(Date(epochMillis))
    }

    /** Markdown 안전성: 사용자 입력의 백틱과 파이프 이스케이프 정도만. */
    private fun escape(text: String): String =
        text.replace("\\", "\\\\").replace("`", "\\`").trim()
}

/**
 * 리포트 생성 시점에 ViewModel이 모아 넘기는 정적 스냅샷.
 * Compose 상태를 직접 참조하지 않게 하기 위해 plain data class로 둔다.
 */
data class StudyReportSnapshot(
    val session: StudySession,
    val verses: List<BibleVerse>,
    val observations: List<StudyObservation>,
    val markups: List<StudyMarkup>,
    val markupLinks: List<StudyMarkupLink>,
    val divisions: List<StudyDivision>,
    val characterTagsByDivision: Map<String, List<StudyCharacterTag>>,
    val allCharacterTags: List<StudyCharacterTag>,
    val interpretations: List<StudyInterpretation>,
    val themeChecks: List<StudyThemeCheck>,
    val propositions: List<StudyProposition>,
    val outline: List<StudyOutlineNode>,
    val application: StudyApplication?,
    val generatedAt: Long = System.currentTimeMillis()
)
