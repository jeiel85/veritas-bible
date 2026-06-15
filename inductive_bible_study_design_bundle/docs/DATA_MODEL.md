# Data Model

> 실제 프로젝트 기술스택에 맞추어 Room, SQLite, Drift, Isar, Realm, CoreData 등으로 변환합니다. 기본 원칙은 로컬 우선, 마이그레이션 가능, 내보내기 가능 구조입니다.

---

## 1. Entity Overview

```text
BibleTranslation
BibleBook
BibleChapter
BibleVerse
StudySession
StudyStageProgress
TextMarkup
MarkupLink
PassageDivision
ObservationNote
CharacterTag
InterpretationNote
ThemeCandidate
Proposition
OutlineNode
ApplicationNote
CrossReference
FeedbackComment
ExportRecord
ImportRecord
RevisionLog
```

---

## 2. StudySession

```text
id: String
title: String
translationId: String
bookId: String
startChapter: Int
startVerse: Int
endChapter: Int
endVerse: Int
studyType: String
status: String
currentStage: String
createdAt: Instant
updatedAt: Instant
completedAt: Instant?
archivedAt: Instant?
```

---

## 3. StudyStageProgress

```text
id: String
sessionId: String
stage: String
isStarted: Boolean
isCompleted: Boolean
completedAt: Instant?
updatedAt: Instant
```

---

## 4. TextMarkup

```text
id: String
sessionId: String
translationId: String
bookId: String
chapter: Int
verse: Int
startOffset: Int
endOffset: Int
selectedText: String
markType: String
colorKey: String?
memo: String?
createdAt: Instant
updatedAt: Instant
```

### markType

```text
subject
verb
object
connective
repeated_word
keyword
command
request
statement
narrative
cause
result
contrast
purpose
condition
basis
definition
application_clue
```

---

## 5. MarkupLink

```text
id: String
sessionId: String
fromMarkupId: String
toMarkupId: String
linkType: String
memo: String?
createdAt: Instant
updatedAt: Instant
```

### linkType

```text
subject_verb
verb_object
cause_result
contrast
basis_claim
explanation
theme_support
parallel
application_basis
```

---

## 6. PassageDivision

```text
id: String
sessionId: String
orderIndex: Int
startChapter: Int
startVerse: Int
endChapter: Int
endVerse: Int
title: String
summary: String?
characterTag: String?
relationToMainTheme: String?
createdAt: Instant
updatedAt: Instant
```

---

## 7. ObservationNote

```text
id: String
sessionId: String
questionKey: String
questionText: String
answer: String
verseRefs: String?
createdAt: Instant
updatedAt: Instant
```

---

## 8. CharacterTag

```text
id: String
sessionId: String
targetType: String
targetId: String
tag: String
memo: String?
createdAt: Instant
updatedAt: Instant
```

### targetType

```text
markup
verse
division
session
```

---

## 9. InterpretationNote

```text
id: String
sessionId: String
targetType: String
targetId: String
targetText: String
meaning: String
evidence: String
crossReferenceIds: String?
caution: String?
createdAt: Instant
updatedAt: Instant
```

---

## 10. ThemeCandidate

```text
id: String
sessionId: String
themeText: String
evidenceSummary: String
isFinal: Boolean
reviewStatus: String
createdAt: Instant
updatedAt: Instant
```

---

## 11. Proposition

```text
id: String
sessionId: String
sentence: String
supportingVerseRefs: String
reviewStatus: String
createdAt: Instant
updatedAt: Instant
```

---

## 12. OutlineNode

```text
id: String
sessionId: String
parentId: String?
orderIndex: Int
level: Int
title: String
verseRange: String?
summary: String?
createdAt: Instant
updatedAt: Instant
```

---

## 13. ApplicationNote

```text
id: String
sessionId: String
truthStatement: String
mirrorStatement: String
adjustmentStatement: String
actionPlan: String
dueDate: LocalDate?
checkedAt: Instant?
createdAt: Instant
updatedAt: Instant
```

---

## 14. CrossReference

```text
id: String
sessionId: String
sourceVerseRef: String
targetVerseRef: String
relationType: String
memo: String?
createdAt: Instant
updatedAt: Instant
```

### relationType

```text
same_event
same_theme
same_word
contrast
prophecy_fulfillment
principle_support
explanation_support
application_support
```

---

## 15. FeedbackComment

```text
id: String
sessionId: String
targetType: String
targetId: String
authorName: String?
comment: String
status: String
createdAt: Instant
updatedAt: Instant
```

---

## 16. RevisionLog

```text
id: String
sessionId: String
entityType: String
entityId: String
action: String
beforeJson: String?
afterJson: String?
createdAt: Instant
```

---

## 17. Export Format

백업과 내보내기는 버전 필드를 포함해야 합니다.

```json
{
  "schemaVersion": 1,
  "appVersion": "0.1.0",
  "exportedAt": "2026-05-24T00:00:00Z",
  "sessions": [],
  "markups": [],
  "links": [],
  "notes": []
}
```

---

## 18. Veritas Android 실제 매핑 (Goal 4 시점)

설계 문서의 추상 모델은 다음과 같이 Veritas 앱 Room v5 스키마에 매핑되어 있습니다.

| 설계 엔티티 | 실제 Room 테이블 | 비고 |
|---|---|---|
| StudySession | `study_sessions` | Goal 1 |
| ObservationNote | `study_observations` (questionKey + answer + free) | Goal 1 / Goal 2 템플릿 8종 |
| PassageDivision | `study_divisions` | Goal 1 |
| ApplicationNote | `study_applications` | Goal 1 / Goal 3 `practiced` 추가 |
| TextMarkup | `study_markups` | Goal 2. 절 단위 char offset. selectedText fallback 보존. |
| MarkupLink | `study_markup_links` | Goal 2. 마킹 cascade. (from,to,linkType) 중복 차단. |
| CharacterTag | `study_character_tags` | Goal 2. `targetType ∈ {division, markup, verse, session}` 일반화. |
| InterpretationNote | `study_interpretations` | Goal 3. keyword/plain/context/evidence/refs/conclusion 6필드. Goal 4 에서 `linkedMarkupId` 추가. |
| ThemeCandidate 검증 (확장) | `study_theme_checks` | Goal 3. `(sessionId, checkKey)` 단일 행, 5개 키. |
| Proposition | `study_propositions` | Goal 3. sentence/refs/reviewStatus(`draft/reviewed/needs_revision`). Goal 4 에서 `linkedMarkupIds`, `linkedDivisionIds` (CSV) 추가. |
| OutlineNode | `study_outline_nodes` | Goal 3. parentId 트리 + level/orderIndex/linkedDivisionId. |
| CrossReference / FeedbackComment / RevisionLog / ExportRecord / ImportRecord | 미구현 | 후속 Goal. |

다음 상수가 도메인 키를 안전하게 잡아 둡니다.

- `MarkType`(18), `LinkType`(9), `CharacterTagType`(14) — Goal 2
- `ObservationQuestion`(8), `ThemeCheckKey`(5), `PropositionStatus`(3) — Goal 3

연구 리포트 출력은 `com.veritasbible.app.study.report.StudyMarkdownExporter`
(외부 라이브러리 없음, 한·영 라벨 지원)이 담당하며,
Goal 4 에서 `com.veritasbible.app.study.report.StudyPdfExporter`
(Android 표준 `PdfDocument` 기반, 외부 의존성 0)가 추가되어 PDF 도 함께 공유할 수 있습니다.

백업/복원은 `com.veritasbible.app.repository.BibleRepository` 의
기존 암호화 워크플로(AES + JSON) 를 그대로 사용하며, JSON 페이로드의
`study` 섹션에 11개 연구 테이블 전체를 직렬화합니다. 마이그레이션 무결성은
`StudyMigrationTest` 가 raw SQL 로 v1→v5 전구간을 검증합니다.

## 19. Migration Rules

- 모든 스키마 변경은 버전 증가가 필요하다.
- 삭제 마이그레이션은 기본 금지한다.
- 필드명 변경 시 이전 필드를 읽어 새 필드로 복사한다.
- 실패 시 원본 DB를 보존한다.
- 마이그레이션 테스트 데이터를 유지한다.

