# Decision Log

## 2026-05-24

### Decision: MVP가 아닌 출시 가능한 귀납적 성경연구 앱으로 범위 재정의

- Context: 기존 성경앱은 읽기와 메모 기능 중심이며, 교회 성경연구 교재의 귀납적 연구 흐름을 적용하기 어렵다.
- Decision: 앱의 핵심 정체성을 `성경 연구 워크벤치`로 정의한다.
- Consequence: 단순 메모 기능이 아니라 연구 세션, 본문 마킹, 구조 연결, 성격 규정, 핵심주제, 명제화, 개요화, 적용, 출력/백업까지 포함한다.

### Decision: 로컬 우선 저장

- Context: 연구 데이터와 적용 메모는 민감하고 장기 보존이 중요하다.
- Decision: 초기 출시 범위는 로컬 우선으로 설계하고, 클라우드/계정/외부 AI는 별도 승인 후 확장한다.
- Consequence: 백업/복원과 내보내기가 출시 필수 기능이 된다.

### Decision: AI는 정답 제시가 아니라 코치 역할

- Context: 귀납적 연구는 본문 자체에서 관찰과 해석을 도출하는 흐름이 핵심이다.
- Decision: AI 기능은 초기 출시 범위에서 제외하거나, 도입 시에도 과정 점검형으로 제한한다.
- Consequence: AI가 핵심주제나 해석을 먼저 제시하지 않도록 정책화한다.


## 2026-05-24

### Decision: 기존 veritas-bible-android 앱에 단계형 Goal 방식으로 통합

- Context: 기존 앱은 이미 성경 읽기, 메모, 읽기 계획, 읽기 진행률 기능을 가지고 있으며, 추가 설계 문서는 `D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle`에 배치되어 있다.
- Decision: 새 앱을 만들거나 기존 앱을 전면 재작성하지 않고, Goal 1~3 순서로 연구 모듈을 분리 통합한다.
- Consequence: 첫 단계에서는 연구 세션 기반만 안전하게 추가하고, 본문 마킹/구조 분석과 해석/출력 기능은 후속 Goal로 분리한다.

### Decision: 기존 메모와 귀납적 연구 노트 분리

- Context: 기존 메모는 일반 성경앱 수준의 빠른 기록 기능이고, 귀납적 연구 노트는 관찰, 단락, 해석, 핵심주제, 명제, 개요, 적용까지 포함하는 구조화 데이터이다.
- Decision: 기존 Memo를 연구 노트로 강제 변환하지 않고, StudySession 도메인을 별도로 둔다.
- Consequence: 기존 메모 기능 회귀를 줄이고, 연구 노트는 장기적으로 마킹/연결/내보내기/피드백 기능으로 확장 가능하다.

### Decision: 후속 보강 — v1~v4 schema 백포팅, PrintManager 통합, sweeper, 백업 wipeStudy 토글, 칩 색상 (Goal 5 구현 결정)

- Context: Goal 4 까지의 후속 작업 5건을 모두 닫아야 출시 품질 작업이 완료된다.
- Decision:
  - **v1~v4 schema JSON 백포팅**: Room 공식 `MigrationTestHelper` 를 정식으로 사용하기 위해 과거 4개 버전의 schema JSON 을 수기 작성한다(identityHash 는 32자 hex 플레이스홀더 — 마이그레이션 후 framework 가 hash 를 v5 의 것으로 갱신하므로 무관). schemas 폴더를 `main` 소스셋 assets 로 합본해 Robolectric 단위 테스트가 `InstrumentationRegistry.context.assets` 로 읽도록 한다(APK 영향 약 60KB). raw SQL 백업 테스트는 제거.
  - **PrintManager**: Android 표준 `PrintDocumentAdapter` 로 시스템 인쇄 시트 진입. `onLayout` 에서 PDF 를 미리 생성하고 `onWrite` 에서 ParcelFileDescriptor 로 복사. 기존 `StudyPdfExporter` 재사용으로 외부 라이브러리 0.
  - **Sweeper**: 마킹/단락 삭제 시 stale linkedId 가 UI 에 남아 사용자가 혼동하지 않도록 즉시 정리. interpretation.linkedMarkupId / proposition.linkedMarkupIds / proposition.linkedDivisionIds / outline.linkedDivisionId 네 위치를 모두 다룬다. 트랜잭션화는 후속.
  - **백업 wipeStudy 토글**: 기본 동작은 머지(같은 ID 덮어쓰기, 다른 ID 보존)로 안전성 유지. 사용자가 명시적 Switch 로 ‘기존 study 데이터 덮어쓰기’를 선택할 때만 wipe 후 import. 토글 라벨 옆에 동작 차이를 설명.
  - **링크 칩 색상**: 카드 안의 plain 텍스트 요약을 MarkupTheme 색상 배경 칩(`LinkedMarkupChip`)으로 교체. 주어=파랑, 동사=빨강 등 색이 카드 곳곳에서 일관되게 보이도록.
- Consequence:
  - 마이그레이션 회귀 위험이 정식 검증 도구로 잡힘. 다음 schema 변경 시에는 KSP 가 자동 export 한 N.json 만 git 에 추가하면 됨.
  - 사용자가 시스템에 설치한 모든 print service(Google Cloud Print 후속·HP·Brother 등)로 리포트를 보낼 수 있음.
  - 마킹/단락 삭제 후에도 카드의 stale 연결이 즉시 사라져 UI 일관성 확보.
  - 백업 import 정책이 명시적이고 사용자 동의 기반(wipe 시).
  - 후속 sweeper 미커버 대상: 마킹/단락에 붙은 character_tag 의 targetId. 이번 범위에서는 별도 도메인이라 제외.

### Decision: 후순위 항목 정리 — PDF 는 AOSP `PdfDocument`, 백업은 기존 워크플로 확장, 마이그레이션 테스트는 raw SQL (Goal 4 구현 결정)

- Context: Goal 3 후속 작업 6건을 출시 품질 수준으로 마무리해야 한다. PDF 라이브러리(iText AGPL, PdfBox, pdf-android 등) 라이선스/APK 크기 부담을 피해야 하고, 기존 사용자(v1~v4) 데이터는 절대 손실되어선 안 된다.
- Decision:
  - **PDF**: `android.graphics.pdf.PdfDocument` (AOSP Apache 2.0, 외부 의존성 0, 한글 시스템 폰트 자동 지원) 만으로 직접 렌더링한다. Markdown 의 `# ## ### > -` 만 해석하는 단순 텍스트 레이아웃 — 출시급 인쇄용 PDF 는 후속에서 `PrintManager` 로 보강 예정.
  - **백업**: 기존 `BibleRepository.exportEncryptedBackup` / `importEncryptedBackup` 워크플로(설정 화면의 "백업 데이터 텍스트 복사" / "복원" 버튼)를 유지하고, JSON 페이로드에 `study` 섹션을 추가하는 머지 정책으로 확장. 신규 사용자가 옛 백업을 import 해도 `study` 누락이 안전하게 무시되도록 `optJSONObject` 사용.
  - **마이그레이션 테스트**: `MigrationTestHelper` 는 과거 버전 schema JSON 이 필수인데 v1~v4 가 export 되지 않았다. 따라서 `SupportSQLiteOpenHelper` 로 v1 schema 를 직접 만들고 우리 `MIGRATION_*` 객체를 순차 호출해 데이터 무결성·컬럼 존재를 검증하는 raw SQL 테스트로 진행. `room-testing` 의존성은 미래(v5+ 기준 정식 자동 마이그레이션 테스트)를 위해 유지하되 현재 테스트에서는 미사용.
  - **명시 링크**: 해석은 단일 마킹, 명제는 마킹/단락 다중 — 각각 nullable TEXT 컬럼(CSV)으로 저장. `LinkedIds.encode/decode` 헬퍼로 정규화. 마킹 삭제 시 stale id 는 UI 가 단순히 무시(후속 sweeper 예정).
  - **DatePicker**: Material 3 `DatePicker` + `DatePickerDialog` 사용, 저장 형식은 ISO `yyyy-MM-dd` (UTC) 로 통일 — 기존 사용자가 입력한 자유 텍스트 점검일은 그대로 표시되며 다시 열면 빈 값이거나 ISO 로 덮어써진다.
- Consequence:
  - PDF 출력이 출시 가능 수준에 도달했고 APK 크기·라이선스 영향 0. 단점은 Markdown 표 렌더링·이미지 미지원 — 추후 인쇄 어댑터로 보강 가능.
  - 백업이 study 데이터까지 포함하므로 기기 교체/재설치 시나리오에서 연구 데이터가 안전하게 이동.
  - v1→v5 마이그레이션이 의존성 없이 자동 검증되어 출시 리스크 감소.
  - 백업 schemaVersion 이 도입되어 후속에 schema 진화 시 자동 마이그레이션 로직을 추가하기 쉬워짐.

### Decision: 출력은 Markdown 우선, PDF는 후순위. 적용 노트는 단일 행으로 구조화 (Goal 3 구현 결정)

- Context: GOAL_03 에서 연구 결과를 “실제로 완성·공유”할 흐름이 필요하다. PDF 라이브러리는 (a) iText AGPL 등 라이선스 부담, (b) APK 크기 증가, (c) 한글 폰트 포함 문제 등을 일으킨다. 또한 사용자 적용 노트는 진리/거울/조정/실천/점검일/실천 여부의 6개 필드를 가지지만, 한 세션당 단일 행으로 다뤄도 충분하다(설계 DATA_MODEL §13 와 일치).
- Decision:
  - Markdown 내보내기를 출시 필수 기능으로 정의하고, PDF 는 별도 Goal/후속 작업으로 분리한다. `study_report_pdf_note` 키로 사용자에게 명시 안내.
  - Markdown 직렬화는 외부 라이브러리 없이 `StudyMarkdownExporter` 한 클래스로 처리한다. 라벨은 한·영 두 종류만 두고 `appLanguage` 로 분기.
  - 적용 노트는 단일 행 upsert 구조를 유지하고, Goal 3 에서 `practiced` 컬럼을 추가한다(`ALTER TABLE ... ADD COLUMN ... DEFAULT 0`). 토글 시 `checkedAt = now` 로 자동 timestamp 저장.
  - 핵심주제 검증은 5개 키 단위 행(`study_theme_checks`)으로 저장해 후속에서 항목 확장/제거가 쉽도록 한다.
  - 개요 노드는 `parentId + level + orderIndex` 트리 구조로 저장하되 모바일 1차 UI는 1차원 리스트(들여쓰기)로 보여 복잡도를 낮춘다.
- Consequence:
  - 출시 일정이 PDF 라이브러리 검증에 묶이지 않는다. Markdown 만으로도 외부 도구(예: Pandoc, 노션, 옵시디언)로 PDF 변환이 가능.
  - 적용 노트는 v3 사용자 데이터가 그대로 보존되며 단지 `practiced` 가 false로 채워진다.
  - 검증 체크리스트 항목을 늘려도 새 `ThemeCheckKey` 상수와 i18n 키만 추가하면 마이그레이션 없이 확장.

### Decision: 본문 마킹은 절 단위 char offset, 구조 연결은 마킹 ID 쌍, 성격 태그는 일반화 target로 저장 (Goal 2 구현 결정)

- Context: Compose `SelectionContainer` 기반의 임의 위치 텍스트 선택은 모바일에서 일관성이 떨어지고, 본문 데이터(`bible_verses`)는 절 단위로 분리되어 있다. 마킹은 후속 출시에서도 안전하게 직렬화·복원돼야 하며, AI 코치 정책상 외부 서비스에 의존해서는 안 된다.
- Decision:
  - 마킹 단위는 절 1개로 한정한다. `study_markups`는 `verseId / book / chapter / verse / startOffset / endOffset / selectedText / markType` 을 모두 저장해, 본문 번역본 교체 시에도 `selectedText`를 fallback으로 보존한다.
  - 모바일 UI는 절 단위 ‘단어 칩 탭’으로 시작/끝 단어를 고른 뒤 마킹 타입을 선택하는 다이얼로그 흐름을 채택한다. 임의 위치 텍스트 선택보다 모바일 정확도가 높다.
  - 구조 연결은 `study_markup_links (fromMarkupId, toMarkupId, linkType)` 의 마킹 ID 쌍으로 저장한다. 마킹이 삭제되면 cascade로 연결도 제거된다. 같은 from/to/linkType 중복 저장은 차단한다.
  - 성격 태그는 단락 외에도 마킹/절/세션에 붙일 수 있도록 `(targetType, targetId)` 일반화 구조로 둔다. 이번 Goal에서는 단락에만 UI를 노출하지만 데이터 모델은 모두 수용한다.
  - 관찰 질문 템플릿은 기존 `study_observations` 테이블을 재사용하되 `(sessionId, questionKey)` 1행 보장 upsert를 추가해 자유 관찰 노트(`questionKey=free`)와 공존시킨다.
- Consequence:
  - 마킹 인덱스 무결성이 단순해진다. 절 ID와 char offset만 알면 복원 가능.
  - 연결 타입은 9종 모두 동일 테이블에 들어가 후속 Goal에서 새 타입 추가 시 UI만 확장하면 된다.
  - 성격 태그 UI를 마킹/절로 확장할 때 추가 마이그레이션이 필요 없다.
  - 한 번에 1절 범위만 마킹 가능 — 절을 가로지르는 마킹은 후속 Goal에서 별도 모델(혹은 분할 저장)로 다룬다.

### Decision: 연구 모듈을 별도 Kotlin 패키지/ViewModel/Room 테이블 군으로 격리 (Goal 1 구현 결정)

- Context: 코드 구현 시점에 기존 앱은 단일 `BibleViewModel`과 `notes`/`bible_verses`/`reading_logs`/`reading_goals` 4개 테이블을 사용했고, DB는 `fallbackToDestructiveMigration()`으로 설정되어 있었다.
- Decision:
  - 연구 도메인은 패키지 `com.veritasbible.app.study` 로 분리하고, `StudyViewModel` 을 새로 둔다.
  - Room 테이블 `study_sessions`, `study_observations`, `study_divisions`, `study_applications` 를 신규 생성한다.
  - DB 버전을 2로 올리되, destructive fallback을 제거하고 `MIGRATION_1_2` 를 추가해 기존 사용자 데이터(메모/북마크/읽기 로그/목표)는 무손실 유지한다.
  - 기존 `Note` 엔티티와 `NotesScreen`은 변경하지 않는다.
- Consequence:
  - 기존 메모 기능을 학습 곡선 없이 그대로 사용 가능. 연구 모듈은 후속 Goal에서 `text_markups`, `markup_links`, `interpretation_notes`, `outline_nodes` 등으로 자유롭게 확장 가능.
  - 마이그레이션 실패 시 destructive fallback이 없으므로 앱이 크래시할 수 있으나, 이는 데이터 보존 우선 정책에 부합한다. 후속 Goal에서 마이그레이션 실패 화면을 별도로 노출해야 한다.
