package com.veritasbible.app.study.repository

import com.veritasbible.app.data.AppDatabase
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
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * 연구 모듈 데이터의 JSON 직렬화·역직렬화.
 *
 * 백업 JSON 의 `study` 섹션에 11개 테이블을 모두 담는다. `schemaVersion` 은
 * 백업 파일 단위 마이그레이션 키이며, 현재 Room v[AppDatabase.LATEST_VERSION]
 * 과 동일한 값을 사용한다. 더 낮은 버전 파일을 만나면 import 시 누락된 필드는
 * 기본값으로 채워진다(이전 버전 호환).
 */
object StudyBackup {

    /** 백업 JSON `study.schemaVersion` 의 현재 값. */
    const val SCHEMA_VERSION: Int = AppDatabase.LATEST_VERSION

    /** 마샬링: 모든 study_* 테이블을 한 JSONObject 로 모은다. */
    suspend fun exportTo(db: AppDatabase): JSONObject {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)

        val sessions = db.studySessionDao().observeAll().first()
        root.put("sessions", sessions.toJsonArray { it.toJson() })

        // session-scoped 모든 데이터를 모은다.
        val obsArr = JSONArray()
        val divArr = JSONArray()
        val appArr = JSONArray()
        val mkArr = JSONArray()
        val linkArr = JSONArray()
        val tagArr = JSONArray()
        val interpArr = JSONArray()
        val checkArr = JSONArray()
        val propArr = JSONArray()
        val outlineArr = JSONArray()

        sessions.forEach { s ->
            db.studyObservationDao().observeBySession(s.id).first().forEach { obsArr.put(it.toJson()) }
            db.studyDivisionDao().observeBySession(s.id).first().forEach { divArr.put(it.toJson()) }
            db.studyApplicationDao().findBySession(s.id)?.let { appArr.put(it.toJson()) }
            db.studyMarkupDao().observeBySession(s.id).first().forEach { mkArr.put(it.toJson()) }
            db.studyMarkupLinkDao().observeBySession(s.id).first().forEach { linkArr.put(it.toJson()) }
            db.studyCharacterTagDao().observeBySession(s.id).first().forEach { tagArr.put(it.toJson()) }
            db.studyInterpretationDao().observeBySession(s.id).first().forEach { interpArr.put(it.toJson()) }
            db.studyThemeCheckDao().observeBySession(s.id).first().forEach { checkArr.put(it.toJson()) }
            db.studyPropositionDao().observeBySession(s.id).first().forEach { propArr.put(it.toJson()) }
            db.studyOutlineNodeDao().observeBySession(s.id).first().forEach { outlineArr.put(it.toJson()) }
        }
        root.put("observations", obsArr)
        root.put("divisions", divArr)
        root.put("applications", appArr)
        root.put("markups", mkArr)
        root.put("markupLinks", linkArr)
        root.put("characterTags", tagArr)
        root.put("interpretations", interpArr)
        root.put("themeChecks", checkArr)
        root.put("propositions", propArr)
        root.put("outlineNodes", outlineArr)
        return root
    }

    /**
     * 언마샬링. 기본 정책은 **머지**:
     *  - 동일 PK 가 이미 있으면 upsert(REPLACE)로 덮어쓰지만, 다른 세션 데이터는 건드리지 않는다.
     *  - 누락된 v4 이하 필드는 기본값(빈 문자열 / null / false)으로 채운다.
     *
     * `wipeBeforeImport=true` 일 때는 import 직전에 모든 study_* 테이블을 비우고
     * 백업 내용으로 통째 교체한다(설정 화면에서 사용자 명시 동의 후 호출 가능).
     */
    suspend fun importFrom(db: AppDatabase, root: JSONObject, wipeBeforeImport: Boolean = false) {
        if (wipeBeforeImport) {
            // 세션을 지우면 cascade 로 모든 자식 테이블이 함께 정리된다.
            db.studySessionDao().observeAll().first().forEach { db.studySessionDao().deleteById(it.id) }
        }

        root.optJSONArray("sessions").forEachObject { obj ->
            db.studySessionDao().upsert(StudySession.fromJson(obj))
        }
        root.optJSONArray("observations").forEachObject { obj ->
            db.studyObservationDao().upsert(StudyObservation.fromJson(obj))
        }
        root.optJSONArray("divisions").forEachObject { obj ->
            db.studyDivisionDao().upsert(StudyDivision.fromJson(obj))
        }
        root.optJSONArray("applications").forEachObject { obj ->
            db.studyApplicationDao().upsert(StudyApplication.fromJson(obj))
        }
        root.optJSONArray("markups").forEachObject { obj ->
            db.studyMarkupDao().upsert(StudyMarkup.fromJson(obj))
        }
        root.optJSONArray("markupLinks").forEachObject { obj ->
            db.studyMarkupLinkDao().upsert(StudyMarkupLink.fromJson(obj))
        }
        root.optJSONArray("characterTags").forEachObject { obj ->
            db.studyCharacterTagDao().upsert(StudyCharacterTag.fromJson(obj))
        }
        root.optJSONArray("interpretations").forEachObject { obj ->
            db.studyInterpretationDao().upsert(StudyInterpretation.fromJson(obj))
        }
        root.optJSONArray("themeChecks").forEachObject { obj ->
            db.studyThemeCheckDao().upsert(StudyThemeCheck.fromJson(obj))
        }
        root.optJSONArray("propositions").forEachObject { obj ->
            db.studyPropositionDao().upsert(StudyProposition.fromJson(obj))
        }
        root.optJSONArray("outlineNodes").forEachObject { obj ->
            db.studyOutlineNodeDao().upsert(StudyOutlineNode.fromJson(obj))
        }
    }
}

// ---------------- JSON helpers ----------------

private inline fun <T> List<T>.toJsonArray(serialize: (T) -> JSONObject): JSONArray {
    val arr = JSONArray()
    forEach { arr.put(serialize(it)) }
    return arr
}

private inline fun JSONArray?.forEachObject(block: (JSONObject) -> Unit) {
    if (this == null) return
    for (i in 0 until length()) {
        block(getJSONObject(i))
    }
}

// ---- Entity ↔ JSON ----

private fun StudySession.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("title", title); put("translationId", translationId)
    put("book", book); put("bookEn", bookEn); put("bookId", bookId)
    put("startChapter", startChapter); put("startVerse", startVerse)
    put("endChapter", endChapter); put("endVerse", endVerse)
    put("studyType", studyType); put("status", status); put("currentStage", currentStage)
    put("mainTheme", mainTheme); put("mainPropositionMemo", mainPropositionMemo)
    put("createdAt", createdAt); put("updatedAt", updatedAt)
    completedAt?.let { put("completedAt", it) }
    archivedAt?.let { put("archivedAt", it) }
}

private fun StudySession.Companion.fromJson(o: JSONObject): StudySession = StudySession(
    id = o.getString("id"), title = o.getString("title"),
    translationId = o.optString("translationId", "ko-rev-1910+web"),
    book = o.getString("book"), bookEn = o.optString("bookEn", ""),
    bookId = o.optInt("bookId", 0),
    startChapter = o.getInt("startChapter"), startVerse = o.getInt("startVerse"),
    endChapter = o.getInt("endChapter"), endVerse = o.getInt("endVerse"),
    studyType = o.optString("studyType", "passage"),
    status = o.optString("status", "in_progress"),
    currentStage = o.optString("currentStage", "observation"),
    mainTheme = o.optString("mainTheme", ""),
    mainPropositionMemo = o.optString("mainPropositionMemo", ""),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
    completedAt = if (o.has("completedAt") && !o.isNull("completedAt")) o.getLong("completedAt") else null,
    archivedAt = if (o.has("archivedAt") && !o.isNull("archivedAt")) o.getLong("archivedAt") else null
)

private fun StudyObservation.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    put("questionKey", questionKey); put("questionText", questionText); put("answer", answer)
    verseRefs?.let { put("verseRefs", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyObservation.Companion.fromJson(o: JSONObject): StudyObservation = StudyObservation(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    questionKey = o.optString("questionKey", "free"),
    questionText = o.optString("questionText", ""),
    answer = o.getString("answer"),
    verseRefs = o.optStringOrNull("verseRefs"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyDivision.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId); put("orderIndex", orderIndex)
    put("startChapter", startChapter); put("startVerse", startVerse)
    put("endChapter", endChapter); put("endVerse", endVerse); put("title", title)
    summary?.let { put("summary", it) }
    characterTag?.let { put("characterTag", it) }
    relationToMainTheme?.let { put("relationToMainTheme", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyDivision.Companion.fromJson(o: JSONObject): StudyDivision = StudyDivision(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    orderIndex = o.optInt("orderIndex", 0),
    startChapter = o.getInt("startChapter"), startVerse = o.getInt("startVerse"),
    endChapter = o.getInt("endChapter"), endVerse = o.getInt("endVerse"),
    title = o.getString("title"),
    summary = o.optStringOrNull("summary"),
    characterTag = o.optStringOrNull("characterTag"),
    relationToMainTheme = o.optStringOrNull("relationToMainTheme"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyApplication.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    put("truthStatement", truthStatement); put("mirrorStatement", mirrorStatement)
    put("adjustmentStatement", adjustmentStatement); put("actionPlan", actionPlan)
    dueDate?.let { put("dueDate", it) }
    checkedAt?.let { put("checkedAt", it) }
    put("practiced", practiced)
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyApplication.Companion.fromJson(o: JSONObject): StudyApplication = StudyApplication(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    truthStatement = o.optString("truthStatement", ""),
    mirrorStatement = o.optString("mirrorStatement", ""),
    adjustmentStatement = o.optString("adjustmentStatement", ""),
    actionPlan = o.optString("actionPlan", ""),
    dueDate = o.optStringOrNull("dueDate"),
    checkedAt = if (o.has("checkedAt") && !o.isNull("checkedAt")) o.getLong("checkedAt") else null,
    practiced = o.optBoolean("practiced", false),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyMarkup.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId); put("verseId", verseId)
    put("book", book); put("chapter", chapter); put("verse", verse)
    put("startOffset", startOffset); put("endOffset", endOffset)
    put("selectedText", selectedText); put("markType", markType)
    memo?.let { put("memo", it) }
    colorKey?.let { put("colorKey", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyMarkup.Companion.fromJson(o: JSONObject): StudyMarkup = StudyMarkup(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    verseId = o.optInt("verseId", 0),
    book = o.getString("book"), chapter = o.getInt("chapter"), verse = o.getInt("verse"),
    startOffset = o.optInt("startOffset", 0), endOffset = o.optInt("endOffset", 0),
    selectedText = o.optString("selectedText", ""),
    markType = o.getString("markType"),
    memo = o.optStringOrNull("memo"),
    colorKey = o.optStringOrNull("colorKey"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyMarkupLink.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    put("fromMarkupId", fromMarkupId); put("toMarkupId", toMarkupId)
    put("linkType", linkType)
    memo?.let { put("memo", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyMarkupLink.Companion.fromJson(o: JSONObject): StudyMarkupLink = StudyMarkupLink(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    fromMarkupId = o.getString("fromMarkupId"), toMarkupId = o.getString("toMarkupId"),
    linkType = o.getString("linkType"),
    memo = o.optStringOrNull("memo"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyCharacterTag.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    put("targetType", targetType); put("targetId", targetId); put("tag", tag)
    memo?.let { put("memo", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyCharacterTag.Companion.fromJson(o: JSONObject): StudyCharacterTag = StudyCharacterTag(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    targetType = o.getString("targetType"), targetId = o.getString("targetId"),
    tag = o.getString("tag"),
    memo = o.optStringOrNull("memo"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyInterpretation.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId); put("keyword", keyword)
    put("plainMeaning", plainMeaning); put("contextualMeaning", contextualMeaning)
    put("evidence", evidence); put("crossRefs", crossRefs); put("conclusion", conclusion)
    linkedMarkupId?.let { put("linkedMarkupId", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyInterpretation.Companion.fromJson(o: JSONObject): StudyInterpretation = StudyInterpretation(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    keyword = o.optString("keyword", ""),
    plainMeaning = o.optString("plainMeaning", ""),
    contextualMeaning = o.optString("contextualMeaning", ""),
    evidence = o.optString("evidence", ""),
    crossRefs = o.optString("crossRefs", ""),
    conclusion = o.optString("conclusion", ""),
    linkedMarkupId = o.optStringOrNull("linkedMarkupId"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyThemeCheck.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId); put("checkKey", checkKey)
    put("isChecked", isChecked); put("note", note)
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyThemeCheck.Companion.fromJson(o: JSONObject): StudyThemeCheck = StudyThemeCheck(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    checkKey = o.getString("checkKey"),
    isChecked = o.optBoolean("isChecked", false),
    note = o.optString("note", ""),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyProposition.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    put("sentence", sentence); put("supportingRefs", supportingRefs); put("reviewStatus", reviewStatus)
    linkedMarkupIds?.let { put("linkedMarkupIds", it) }
    linkedDivisionIds?.let { put("linkedDivisionIds", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyProposition.Companion.fromJson(o: JSONObject): StudyProposition = StudyProposition(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    sentence = o.optString("sentence", ""),
    supportingRefs = o.optString("supportingRefs", ""),
    reviewStatus = o.optString("reviewStatus", "draft"),
    linkedMarkupIds = o.optStringOrNull("linkedMarkupIds"),
    linkedDivisionIds = o.optStringOrNull("linkedDivisionIds"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun StudyOutlineNode.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("sessionId", sessionId)
    parentId?.let { put("parentId", it) }
    put("orderIndex", orderIndex); put("level", level); put("title", title)
    verseRange?.let { put("verseRange", it) }
    summary?.let { put("summary", it) }
    linkedDivisionId?.let { put("linkedDivisionId", it) }
    put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun StudyOutlineNode.Companion.fromJson(o: JSONObject): StudyOutlineNode = StudyOutlineNode(
    id = o.getString("id"), sessionId = o.getString("sessionId"),
    parentId = o.optStringOrNull("parentId"),
    orderIndex = o.optInt("orderIndex", 0),
    level = o.optInt("level", 0),
    title = o.getString("title"),
    verseRange = o.optStringOrNull("verseRange"),
    summary = o.optStringOrNull("summary"),
    linkedDivisionId = o.optStringOrNull("linkedDivisionId"),
    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key, "").takeIf { it.isNotEmpty() } else null
