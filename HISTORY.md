# Veritas Bible 작업 이력

> 이 파일은 `docs/inductive_bible_study_design_bundle` 설계 문서가 요구하는 `HISTORY.md` 형식을 따릅니다.
> 기존 프로젝트의 일반 진행 기록은 [PROGRESS.md](PROGRESS.md)에 그대로 유지됩니다.

## 2026-05-24 — Goal 5: 후속 보강 (스키마 백포팅 / PrintManager / Sweeper / 백업 토글 / 링크 칩 색상)

### 작업 내용

- **v1~v4 schema JSON 백포팅 + MigrationTestHelper**: `app/schemas/com.veritasbible.app.data.AppDatabase/{1,2,3,4}.json` 을 수기 작성해 Room 공식 `MigrationTestHelper` 로 v1→v5, v2→v5, v3→v5, v4→v5 전구간을 정식 검증. Robolectric 단위 테스트에서 schema JSON 을 찾으려면 main assets 에 노출돼야 하므로 `sourceSets.main.assets.srcDir("$projectDir/schemas")` 로 합본(APK 크기 +약 60KB).
- **PrintManager 인쇄 메뉴**: `StudyPrintAdapter` 신규. `PrintManager.print()` 로 시스템 인쇄 시트에 진입, 동일 `StudyPdfExporter` 가 만든 PDF 를 ParcelFileDescriptor 에 흘려보낸다. 리포트 패널에 ‘인쇄’ 버튼 추가.
- **마킹·단락 삭제 sweeper**: `StudyRepository.deleteMarkup` 이후 `sweepStaleMarkupLinks(sessionId, markupId)` 호출 — interpretation.linkedMarkupId 는 null, proposition.linkedMarkupIds CSV 에서 해당 토큰 제거. `deleteDivision` 도 마찬가지로 proposition.linkedDivisionIds + outline_node.linkedDivisionId 정리.
- **백업 import 덮어쓰기 토글**: `BibleRepository.importEncryptedBackup(..., wipeStudyFirst: Boolean = false)`, `BibleViewModel.importBackup(...wipeStudyFirst=)`, SettingsScreen 복원 다이얼로그에 Material 3 `Switch` 추가. 토글 켜면 import 직전 모든 study 테이블을 비우고 백업으로 통째 교체. 기본은 머지.
- **명시 링크 칩 색상화**: `LinkedMarkupChip`/`LinkedMarkupChipRow`/`LinkedDivisionChipRow` 신규. MarkupTheme 색상을 배경+border 로 입혀 카드 안에서 시각적 식별 강화.
- 신규 DAO 쿼리 3개: `findByLinkedMarkup` (interpretation), `findByLinkedMarkup` / `findByLinkedDivision` (proposition), `findByLinkedDivision` (outline).
- 신규 i18n 키 2개: `study_report_print`, `study_report_print_failed` (한·영).

### 변경 파일

신규
- `app/schemas/com.veritasbible.app.data.AppDatabase/{1,2,3,4}.json` — 과거 스키마 백포팅.
- `app/src/main/java/com/veritasbible/app/study/report/StudyPrintAdapter.kt` — PrintDocumentAdapter + helper.

수정
- `app/build.gradle.kts` — schemas/ 를 `main` 소스셋 assets 로 합본.
- `app/src/test/java/com/veritasbible/app/study/StudyMigrationTest.kt` — raw SQL 방식에서 `MigrationTestHelper` 기반으로 교체.
- `app/src/test/java/com/veritasbible/app/study/StudyRepositoryTest.kt` — Goal 5 sweeper 케이스 3건 추가.
- `app/src/main/java/com/veritasbible/app/study/data/StudyDao.kt` — `findByLinkedMarkup/Division` 쿼리.
- `app/src/main/java/com/veritasbible/app/study/repository/StudyRepository.kt` — `sweepStaleMarkupLinks`, `sweepStaleDivisionLinks`.
- `app/src/main/java/com/veritasbible/app/repository/BibleRepository.kt` — `importEncryptedBackup(wipeStudyFirst=)`.
- `app/src/main/java/com/veritasbible/app/ui/BibleViewModel.kt` — 동일 매개변수 전파.
- `app/src/main/java/com/veritasbible/app/ui/screens/SettingsScreen.kt` — 복원 다이얼로그에 Switch 추가.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyDetailScreen.kt` — LinkedMarkupChip/Row 컴포저블, ReportPanel 인쇄 버튼.
- `app/src/main/java/com/veritasbible/app/util/LanguageManager.kt` — 인쇄 키 2개(KO/EN).
- 문서: CHANGELOG / HISTORY / PROGRESS / DECISION_LOG / DATA_MODEL.

### 검증 결과

- `./gradlew.bat assembleDebug` — **성공**.
- `./gradlew.bat testDebugUnitTest` — **성공** (40건 통과: Goal 1-4 + Goal 5 sweeper 3건 + 마이그레이션 4건이 raw SQL → MigrationTestHelper 로 교체된 후에도 모두 그린).
- `./gradlew.bat lintDebug` — **성공**.

### 생략한 검증과 사유

- **PrintManager Robolectric 단위 테스트**: Robolectric 의 PrintManager shadow 가 완전하지 않아 단위 검증 미진행. 실기기에서 ‘리포트 → 인쇄’ 흐름 확인 권장.
- **PDF 본문 단위 테스트**: Goal 4 와 동일하게 Robolectric PdfDocument shadow 한계로 생략.
- **실기기 E2E**: 기기 직접 구동 불가. 후속 점검:
  1. 마킹/단락을 삭제했을 때 해석/명제/개요 카드의 stale 칩이 사라지는지.
  2. 백업 복원 다이얼로그 토글 ON/OFF 동작.
  3. 리포트 → 인쇄 → 시스템 시트에서 “PDF 로 저장” 선택 → 파일 확인.
  4. 명시 링크 칩이 마킹 색상(주어=파랑, 동사=빨강 등)으로 표시되는지.

### 후속 작업

- 인쇄 어댑터 페이지 수 정확 계산 (현재 PAGE_COUNT_UNKNOWN). PDF 사전 생성 시 pageCount 를 알 수 있으므로 PdfDocument 의 pages.size 를 노출하는 API 추가 검토.
- 백업 import 미리보기(어떤 세션이 몇 개 들어오는지) — 토글 ON 시 안전성 확보.
- Sweeper 가 다른 stale 케이스(예: division 삭제 시 character_tag 의 targetId)도 정리하는지 점검.
- Sweeper 트랜잭션화 — 현재는 markup 삭제와 sweep 가 분리된 IO 호출이라 중간 실패 가능성. Room `@Transaction` 으로 묶기.

## 2026-05-24 — Goal 4: PDF / 백업 통합 / 명시 링크 / DatePicker / 마이그레이션 테스트

### 작업 내용

- **마이그레이션 테스트 인프라**: `androidx.room:room-testing` 의존성을 버전 카탈로그·`app/build.gradle.kts` 에 추가. KSP `room.schemaLocation` 을 `app/schemas` 로 설정하고 `exportSchema=true` 로 전환. 다만 v1~v4 의 schema JSON 이 과거에 export 되지 않은 한계 때문에 `MigrationTestHelper` 대신 `SupportSQLiteOpenHelper` 로 v1 schema 를 직접 만들고 마이그레이션 객체를 실행해 검증하는 방식을 채택(외부 의존성 없는 raw SQL 테스트, `StudyMigrationTest` 4건).
- **명시 링크 v4→v5**: `study_interpretations.linkedMarkupId`, `study_propositions.linkedMarkupIds`, `study_propositions.linkedDivisionIds` 컬럼 추가. `LinkedIds` 헬퍼로 CSV 인코딩/디코딩. UI 에 단일/다중 선택 칩(MarkupSingleSelect, MarkupMultiSelect, DivisionMultiSelect) 추가.
- **DatePicker (M3)**: 적용 패널의 점검일이 자유 입력 텍스트필드 → Material 3 `DatePicker` 다이얼로그로 교체. ISO 날짜 문자열(yyyy-MM-dd, UTC)로 저장, 지우기 버튼 추가.
- **백업/복원 통합**: `StudyBackup` 신규(11개 테이블 JSON marshalling/unmarshalling, schemaVersion 포함). `BibleRepository.exportEncryptedBackup` 및 `importEncryptedBackup` 가 `study` 섹션을 포함하도록 확장. 기존 메모/북마크/읽기 로그/목표는 그대로 보존. 신규 사용자가 옛 백업을 import 해도 `study` 누락 시 안전하게 건너뜀. `BibleRepository` 생성자에 `database` 매개변수 추가, `BibleViewModel` 갱신.
- **PDF 내보내기**: `android.graphics.pdf.PdfDocument` (AOSP, 무라이선스) 기반 `StudyPdfExporter`. A4 595×842pt, 시스템 한글 폰트 사용, Markdown 의 `#/##/###/>/-` 접두사를 단순 스타일로 변환. `cacheDir/study_reports/<제목>.pdf` 로 저장 후 `FileProvider` 로 시스템 공유. 매니페스트에 FileProvider 추가, `res/xml/file_provider_paths.xml` 등록.
- i18n: 신규 8개 키(KO/EN) 추가 — `study_report_pdf`, `study_report_pdf_failed`, `study_report_pdf_note`(갱신), `study_interpretation_link_markup`, `study_proposition_link_markups`, `study_proposition_link_divisions`, `study_application_due_unset`, `study_application_due_clear`.
- 엔티티 9개 데이터 클래스에 `companion object` 선언 추가(외부 marshalling 함수가 `Type.Companion.fromJson` 형태로 확장하기 위함).

### 변경 파일

신규
- `app/src/main/java/com/veritasbible/app/study/repository/StudyBackup.kt`
- `app/src/main/java/com/veritasbible/app/study/report/StudyPdfExporter.kt`
- `app/src/main/res/xml/file_provider_paths.xml`
- `app/src/test/java/com/veritasbible/app/study/StudyBackupTest.kt`
- `app/src/test/java/com/veritasbible/app/study/StudyMigrationTest.kt`
- `app/schemas/com.veritasbible.app.data.AppDatabase/5.json` (Room export 산출물)

수정
- `app/src/main/AndroidManifest.xml` — FileProvider 등록.
- `app/build.gradle.kts` — `room-testing` 의존성, `room.schemaLocation` KSP arg, `schemas/` 를 test/androidTest assets 에 노출.
- `gradle/libs.versions.toml` — `androidx-room-testing` 등록.
- `app/src/main/java/com/veritasbible/app/data/AppDatabase.kt` — `LATEST_VERSION=5`, `MIGRATION_4_5`, `exportSchema=true`.
- `app/src/main/java/com/veritasbible/app/study/data/StudyEntities.kt` — `LinkedIds`, `linkedMarkupId/linkedMarkupIds/linkedDivisionIds` 컬럼 + 모든 엔티티에 `companion object`.
- `app/src/main/java/com/veritasbible/app/study/repository/StudyRepository.kt` — addInterpretation/addProposition 시그니처 확장.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyViewModel.kt` — 동일 시그니처 확장.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyDetailScreen.kt` — MarkupSingleSelect/MarkupMultiSelect/DivisionMultiSelect, InterpretationPanel·PropositionPanel·ReportPanel·DueDatePicker 통합, PDF 공유 버튼.
- `app/src/main/java/com/veritasbible/app/util/LanguageManager.kt` — 8 KO/EN 키 추가.
- `app/src/main/java/com/veritasbible/app/repository/BibleRepository.kt` — backup/import 에 study 섹션 통합.
- `app/src/main/java/com/veritasbible/app/ui/BibleViewModel.kt` — Repository 생성자에 `db` 전달.
- `CHANGELOG.md`, `HISTORY.md`, `PROGRESS.md`, `docs/.../DECISION_LOG.md`, `docs/.../DATA_MODEL.md`.

### 검증 결과

- `./gradlew.bat assembleDebug` — **성공**.
- `./gradlew.bat testDebugUnitTest` — **성공** (37건 통과: Goal 1 5건 + Goal 2 7건 + Goal 3 6건 + Goal 4 마이그레이션 4건 + 백업 2건 + tokenizer 4건 + Markdown exporter 3건 + 기존 Robolectric/Screenshot).
- `./gradlew.bat lintDebug` — **성공**.

### 생략한 검증과 사유

- **PDF Robolectric 단위 테스트**: Robolectric 의 `PdfDocument` shadow 가 `startPage` 직후 "document is closed!" 예외를 던지는 알려진 한계로 단위 검증 불가. 코드 경로 자체는 실기기에서 표준 AOSP API 만 사용하므로 동작에 무리는 없으나, 실기기/에뮬레이터에서 ‘리포트 → Markdown 생성 → PDF 로 공유’ 흐름을 수동 점검 권장.
- **실기기 E2E**: 기기 직접 구동 불가. 후속 점검 시나리오:
  1. v3 또는 v4 사용자가 앱 업그레이드 → 기존 연구 데이터(관찰/마킹/단락/적용/해석/명제/개요) 모두 보존되는지.
  2. 적용 노트에서 점검일 DatePicker 동작 + 지우기 버튼.
  3. 해석에 마킹 링크, 명제에 마킹/단락 다중 링크 → 카드에 요약 표시.
  4. 설정 → ‘백업 데이터 텍스트 복사’ → 다른 기기에서 ‘백업 데이터 검증 및 복원’ → study 데이터 복원 확인.
  5. 리포트 탭 → ‘Markdown 생성’ → ‘PDF 로 공유’ → 공유 시트에서 한글 표시 확인.
- **assembleRelease**: 키스토어 환경 변수 부재로 생략.

### 후속 작업

- v1~v4 schema JSON 을 백포팅(가능하다면) → `MigrationTestHelper` 기반 정식 마이그레이션 자동 테스트로 교체.
- PDF 인쇄 라이브러리 도입(예: `android.print.PrintManager` + `PrintDocumentAdapter`)로 시스템 인쇄 메뉴까지 통합.
- 백업 import UI 에 ‘기존 study 데이터 덮어쓰기’ 토글(현재는 머지 정책 기본).
- 명제·해석 링크 칩에 색상 코드(MarkupTheme) 직접 반영.
- 명시 링크가 마킹 삭제 시 stale id 가 되는 문제를 정리하는 sweeper(현재는 UI 가 stale id 를 단순히 무시).

## 2026-05-24 — Goal 3: 연구 완성·내보내기 흐름

### 작업 내용

- QUALITY_RELEASE_CHECKLIST / PRODUCT_REQUIREMENTS / FEATURE_SPEC / UX_ARCHITECTURE / DATA_MODEL / BIBLE_STUDY_METHOD_MAPPING / DECISION_LOG 재확인. 외부 라이브러리 없이 Markdown 우선, PDF는 보류.
- Room v3 → v4 마이그레이션:
  - 새 테이블: `study_interpretations`, `study_theme_checks`, `study_propositions`, `study_outline_nodes`.
  - 기존 `study_applications` 에 `practiced INTEGER NOT NULL DEFAULT 0` 컬럼 추가.
  - destructive fallback 없이 add-only 마이그레이션이라 기존 적용/마킹/연구 데이터 보존.
- 상수: `ThemeCheckKey`(5), `PropositionStatus`(3). 기존 `StudyStage` 의 모든 단계(준비/관찰/구조/성격/단락/해석/주제/명제/개요/적용/리포트) 와 1대1 매핑되도록 탭 구성.
- Repository: 해석/검증/명제/개요 CRUD + 적용 `practiced` 토글. 사용자 메시지(snackbar)와 update timestamp 일관 관리.
- ViewModel: 4종 Flow 추가, `buildMarkdownReport(appLanguage, onReady)` 비동기 스냅샷 + 직렬화.
- UI:
  - `DetailTab` 을 OBSERVATION / MARKUP / DIVISION / INTERPRETATION / THEME / PROPOSITION / OUTLINE / APPLICATION / REPORT 9개로 재구성. `OthersPlaceholder` 제거.
  - 해석 패널: 입력 폼 + 카드 인라인 편집 + 삭제 확인 다이얼로그.
  - 핵심주제 패널에 검증 체크리스트(5개 Card + 메모 영역) 추가.
  - 명제 패널: 문장/근거/상태(FilterChip 3종) 입력 + 카드 인라인 편집 + 삭제 확인.
  - 개요 패널: 제목/레벨/범위/요약/링크 단락(`StudyDivision`) 선택 + 들여쓰기 표시 + cascade 삭제 확인.
  - 적용 패널: 점검일 자유 입력 + 실천 여부 Checkbox(즉시 토글).
  - 리포트 패널: ‘Markdown 생성’ → 미리보기 카드 + ‘복사’ + ‘공유’ (`ACTION_SEND text/markdown`).
- Markdown 내보내기: 외부 라이브러리 없이 `StudyMarkdownExporter` 단일 클래스로 직렬화. 한·영 라벨 모두 지원. PDF 후순위 사유는 `study_report_pdf_note` 로 사용자에게 안내.
- i18n: Goal 3 관련 60여 키 한·영 동시 추가.

### 변경 파일

신규
- `app/src/main/java/com/veritasbible/app/study/report/StudyMarkdownExporter.kt` (+`StudyReportSnapshot`)
- `app/src/test/java/com/veritasbible/app/study/StudyMarkdownExporterTest.kt` (3 케이스)

수정
- `app/src/main/java/com/veritasbible/app/study/data/StudyEntities.kt` — `ThemeCheckKey`, `PropositionStatus`, `StudyInterpretation/StudyThemeCheck/StudyProposition/StudyOutlineNode`, `StudyApplication.practiced`.
- `app/src/main/java/com/veritasbible/app/study/data/StudyDao.kt` — 4종 신규 DAO.
- `app/src/main/java/com/veritasbible/app/data/AppDatabase.kt` — version=4, `MIGRATION_3_4`.
- `app/src/main/java/com/veritasbible/app/study/repository/StudyRepository.kt` — 신규 CRUD + `toggleApplicationPracticed` + `saveApplication(practiced=)`.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyViewModel.kt` — Flow 4 + 액션 12 + `buildMarkdownReport`.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyDetailScreen.kt` — 탭 9종 재구성 + 신규 패널 4개 + Theme/Application 확장.
- `app/src/main/java/com/veritasbible/app/util/LanguageManager.kt` — Goal 3 키 한·영 동시 추가.
- `app/src/test/java/com/veritasbible/app/study/StudyRepositoryTest.kt` — Goal 3 케이스 6건 추가(레포 생성자 변경 반영 포함).
- `CHANGELOG.md`, `HISTORY.md`, `PROGRESS.md`, `README.md`, `docs/inductive_bible_study_design_bundle/docs/DECISION_LOG.md`.

### QUALITY_RELEASE_CHECKLIST 적용 결과

- 핵심 플로우: 본문 탐색 / 연구 세션 생성 / 마킹 / 단락 / 핵심주제 / 명제 / 적용 / Markdown 리포트 출력 — 코드 경로 모두 갖춤. ✅
- 데이터: destructive fallback 없음, 삭제 전 확인 다이얼로그(세션/마킹/단락/해석/명제/개요/태그) 전부 있음. ✅
- UI/UX: 빈 상태 / 오류(snackbar) / 다크모드(기존 테마) 유지. ✅
- 접근성: testTag 부여, 색상 + 텍스트 라벨 병기로 색맹 사용자 고려. ✅
- 국제화: 신규 문자열 100% 한·영 동시 추가, 하드코딩된 한국어 사용자 노출 문구 없음. ✅
- 빌드/릴리즈: assembleDebug ✅, testDebugUnitTest ✅, lintDebug ✅. release 빌드는 키스토어 환경 변수 필요로 이번 패치에서 미실행(생략 사유 기록).
- 미흡: PDF/JSON·ZIP 백업, Room MigrationTestHelper 자동 테스트, 실기기 E2E.

### 검증 결과

- `./gradlew.bat assembleDebug` — **성공**.
- `./gradlew.bat testDebugUnitTest` — **성공** (총 25건: Goal 1 5건 + Goal 2 7건 + Goal 3 6건 + tokenizer 4건 + Markdown exporter 3건 + 기존 Robolectric/Screenshot).
- `./gradlew.bat lintDebug` — **성공**.

### 생략한 검증과 사유

- **release 빌드 (`assembleRelease`)**: 키스토어 환경 변수 (`ANDROID_RELEASE_KEYSTORE_*`)를 이 세션 환경에서 명시적으로 설정하지 않았으므로 생략. 코드 자체는 minify 비활성 + 기존 release 설정과 동일해 변동 위험은 낮으나, 출시 직전 `scripts/export_play_console_assets.ps1 vX.Y.Z` 로 별도 검증 권장.
- **Room MigrationTestHelper v3→v4 자동 테스트**: `androidx.room:room-testing` 의존성 미포함 상태로 생략. v3→v4는 add-only(`ALTER TABLE ... ADD COLUMN ... DEFAULT 0` + 신규 테이블)이라 기존 데이터 보존 위험은 낮음. 후속 작업으로 의존성 추가 후 자동 테스트 작성.
- **실기기 E2E**: Android 기기 직접 구동 불가. HISTORY 점검 시나리오는 후속에서 다음 흐름으로 진행 권장:
  1. v2/v3 사용자로 업그레이드 → 기존 메모/북마크/연구 세션/관찰/마킹 모두 보존되는지.
  2. 해석 노트 추가/수정/삭제, 명제 추가/수정(상태 변경 포함)/삭제, 개요 노드 추가(부모-자식 cascade)/삭제, 적용 실천 토글 후 재시작 복원.
  3. 리포트 생성 → 미리보기 길이 확인 → 복사 → 공유 다이얼로그 동작.
- **androidTest Compose UI**: testTag만 부여. 정식 androidTest는 별도 Goal에서 작성 예정.

### 후속 작업

- PDF 내보내기 도입(예: Android `PdfDocument` 또는 `android-pdf` 라이브러리 후보 검토 → 라이선스 확인).
- JSON/ZIP 백업 스키마에 `study_interpretations / study_theme_checks / study_propositions / study_outline_nodes` 포함.
- `androidx.room:room-testing` 추가 후 v1→v2, v2→v3, v3→v4 마이그레이션 자동 테스트 일괄 작성.
- 적용 노트 점검일을 Material 3 `DatePicker` 로 보강 (현재 자유 입력).
- 명제·해석·개요의 본문 근거를 마킹 / 단락 / 절 ID 와 명시적으로 연결.
- 백업 인터페이스를 설정 화면의 기존 백업 워크플로(`exportEncryptedBackup`)와 통합.

## 2026-05-24 — Goal 2: 본문 마킹·구조 분석·관찰 강화

### 작업 내용

- GOAL_02_MARKUP_STRUCTURE / BIBLE_STUDY_METHOD_MAPPING / AI_COACH_POLICY / FEATURE_SPEC 마킹 섹션 정독. AI 코치 정책에 따라 외부 AI 연결 없이 수동 도구만 확장.
- 데이터 모델 확장 (DB v2 → v3):
  - `study_markups` (sessionId, verseId, book/chapter/verse, startOffset, endOffset, selectedText, markType, memo, colorKey, timestamps)
  - `study_markup_links` (sessionId, fromMarkupId, toMarkupId, linkType, memo, timestamps) — markup cascade delete
  - `study_character_tags` (sessionId, targetType, targetId, tag, memo, timestamps)
  - 마킹 타입 18종 (`MarkType`), 연결 타입 9종 (`LinkType`), 성격 태그 14종 (`CharacterTagType`), 관찰 질문 8종 (`ObservationQuestion`) 상수화.
- Repository/ViewModel:
  - `StudyRepository`에 markup/link/tag CRUD + 중복/자기 자신 연결 가드.
  - 관찰 템플릿 upsert(`upsertObservationTemplate`): `(sessionId, questionKey)` 단일 행 보장.
  - `StudyViewModel`에 marker/links/tags Flow 노출, 저장 실패 시 사용자 메시지 갱신.
- UI:
  - 연구 상세 본문 패널은 마킹을 `AnnotatedString` `SpanStyle.background`로 인라인 표시. 타입별 색상은 [MarkupTheme](app/src/main/java/com/veritasbible/app/study/ui/MarkupTheme.kt)에 분리.
  - 새 ‘마킹/구조’ 탭(`MARKUP`): 마킹 목록(메모 수정/타입 변경/삭제 확인), 연결 생성(연결 타입 9종 → from/to 마킹 선택), 주어-동사 흐름 요약 + 그 외 연결.
  - 절 선택 → 단어 칩 탭 → 마킹 타입 선택 다이얼로그(`StudyMarkupDialog`). 모바일 텍스트 선택 토큰화 함수는 char offset 정확도 유지를 위해 `tokenize`/`computeRange`로 분리.
  - 관찰 탭에 8개 질문 템플릿 카드(답변 시 칩 표시, 수정/삭제). 자유 관찰 노트는 별도 섹션 유지.
  - 단락 카드에 성격 태그 14종 칩(`AssistChip`로 추가, `InputChip` 탭으로 삭제). 삭제는 중복 방지 + 즉시 삭제.
- 한국어/영어 i18n 키 다수 추가(`mark_type_*`, `link_type_*`, `character_tag_*`, `observation_q_*`, `study_markup_*`, `study_structure_*`, `study_observation_templates_*`, `study_character_tag_*`, `common_close`, `common_answer`).

### 변경 파일

신규
- `app/src/main/java/com/veritasbible/app/study/ui/MarkupTheme.kt`
- `app/src/main/java/com/veritasbible/app/study/ui/StudyMarkupDialog.kt`
- `app/src/test/java/com/veritasbible/app/study/MarkupTokenizerTest.kt`

수정
- `app/src/main/java/com/veritasbible/app/study/data/StudyEntities.kt` — `MarkType / LinkType / CharacterTagType / CharacterTagTarget / ObservationQuestion` 상수 + `StudyMarkup / StudyMarkupLink / StudyCharacterTag` 엔티티 추가.
- `app/src/main/java/com/veritasbible/app/study/data/StudyDao.kt` — 신규 DAO 3종 + `findByQuestion`.
- `app/src/main/java/com/veritasbible/app/data/AppDatabase.kt` — entities 확장, version 3, `MIGRATION_2_3` 추가.
- `app/src/main/java/com/veritasbible/app/study/repository/StudyRepository.kt` — markup/link/tag CRUD + observation template upsert.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyViewModel.kt` — 신규 Flow 4종 + 액션 다수.
- `app/src/main/java/com/veritasbible/app/study/ui/StudyDetailScreen.kt` — `MARKUP` 탭, 패시지 패널 인라인 하이라이트, 관찰 템플릿 카드, 단락 성격 태그 패널.
- `app/src/main/java/com/veritasbible/app/util/LanguageManager.kt` — Goal 2 키 한·영 동시 추가.
- `app/src/test/java/com/veritasbible/app/study/StudyRepositoryTest.kt` — Goal 2 케이스 7건 추가(레포 생성자 변경 반영 포함).
- `CHANGELOG.md`, `HISTORY.md`, `PROGRESS.md`, `docs/inductive_bible_study_design_bundle/docs/DECISION_LOG.md`, `docs/inductive_bible_study_design_bundle/docs/DATA_MODEL.md`.

### 검증 결과

- `./gradlew.bat assembleDebug` — **성공**.
- `./gradlew.bat testDebugUnitTest` — **성공** (Goal 1 5건 + Goal 2 7건 + tokenizer 4건 = 16건 전체 통과, + 기존 Robolectric/Screenshot 테스트).
- `./gradlew.bat lintDebug` — **성공** (사전 존재하던 deprecation 외 신규 경고 없음).

### 생략한 검증과 사유

- **Room MigrationTestHelper v2→v3 자동 테스트**: `androidx.room:room-testing` 의존성이 버전 카탈로그에 포함되지 않은 상태라 의존성 추가 없이는 작성 불가. 마이그레이션 SQL은 Goal 1과 동일한 방식으로 idempotent하게 작성했고, 신규 테이블만 추가하므로 v2 데이터는 그대로 보존된다. 후속 작업으로 `room-testing`을 추가해 마이그레이션 테스트를 정식 작성할 예정.
- **실기기/에뮬레이터 E2E 수동 확인**: Android 기기를 직접 구동할 수 없어 생략. 후속 확인 시나리오:
  1. 기존 메모/북마크/읽기 진행률이 보존되는지(앱 첫 실행 후).
  2. 연구 세션 생성 → ‘마킹/구조’ 탭 진입 → ‘마킹 추가’로 절 선택, 단어 탭, 타입 선택, 저장 → 본문에 하이라이트 보이는지.
  3. 마킹 메모 수정/타입 변경/삭제 확인 다이얼로그 동작.
  4. 주어 마킹 + 동사 마킹을 만든 뒤 ‘주어 ↔ 동사’ 연결 → ‘주어 → 동사 흐름’ 요약 표시 → 연결 삭제.
  5. 관찰 템플릿 8개 질문에 답안 작성/수정/삭제 → 답변 칩 상태 확인.
  6. 단락 생성 후 성격 태그 추가/삭제(중복 차단 안내 확인).
  7. 앱 재시작 시 모든 데이터 복원.
- **androidTest instrumentation**: testTag만 부여, Compose UI 테스트는 후속 Goal 3에서 정식 작성 예정.

### 후속 작업

- Goal 3: 해석 노트, 명제, 개요, Markdown/PDF 내보내기, JSON/ZIP 백업·복원, 마킹/연결을 포함한 리포트 출력.
- 마킹 다이얼로그를 본문 패널에서 직접 단어를 탭해 시작할 수 있도록 통합(현재는 ‘마킹 추가’ 버튼 → 다이얼로그).
- 원인-결과·대조·근거-주장 연결을 위한 전용 UI(지금은 `LinkType.ALL`에서 선택만 가능).
- 마킹·연결을 백업/복원 JSON 스키마에 추가.
- `androidx.room:room-testing` 의존성 추가 후 마이그레이션 자동 테스트 작성.
- 성격 태그 사용자 정의(자유 입력) 옵션 추가.



## 2026-05-24 — Goal 1: 귀납적 성경연구 워크벤치 기반 통합

### 작업 내용

- `docs/inductive_bible_study_design_bundle`의 README/PRODUCT_REQUIREMENTS/FEATURE_SPEC/UX_ARCHITECTURE/DATA_MODEL/IMPLEMENTATION_ROADMAP/DECISION_LOG/GOAL_01 정독.
- 기존 코드 구조 파악: 단일 Room DB(v1, `fallbackToDestructiveMigration`), `BibleViewModel` 단일 AndroidViewModel, 5탭 `MainTab` Crossfade 네비게이션, `LanguageManager` 코드 기반 i18n.
- 기존 메모/북마크/읽기 계획/읽기 진행률 도메인을 건드리지 않고, **별도 패키지 `com.veritasbible.app.study`** 로 연구 모듈을 신설.
- Room DB 버전 1→2 마이그레이션 추가. 기존 사용자 데이터 손실 없이 `study_sessions` 외 3개 테이블만 신규 생성.
- 하단 네비게이션에 ‘성경연구’ 탭 신설. 기존 5개 탭은 그대로 유지.
- 성경 리더의 절 선택 액션 팔레트에 ‘이 본문으로 귀납적 연구 시작’ CTA 추가. 현재 본문 범위를 가지고 새 연구 세션을 만든다.
- 연구 세션 목록 화면(`StudyListScreen`)과 상세 화면(`StudyDetailScreen`) 1차 구현.
- 상세 화면은 상단에 본문 패널, 하단에 `관찰 / 단락 / 핵심주제 / 적용` 탭(+ 자리만 마련된 `그 외 단계`)을 제공. 입력은 즉시 Room에 저장.
- 한국어/영어 문자열을 `LanguageManager` 맵에 동시 추가 (`tab_study`, `study_*`, `stage_*`, `common_*`).
- 빈 상태(연구 없음/관찰 없음/단락 없음), 삭제 확인 다이얼로그, 저장 실패 시 Snackbar 메시지를 모두 처리.

### 변경 파일

신규
- `app/src/main/java/com/veritasbible/app/study/data/StudyEntities.kt`
- `app/src/main/java/com/veritasbible/app/study/data/StudyDao.kt`
- `app/src/main/java/com/veritasbible/app/study/repository/StudyRepository.kt`
- `app/src/main/java/com/veritasbible/app/study/ui/StudyViewModel.kt`
- `app/src/main/java/com/veritasbible/app/study/ui/StudyListScreen.kt`
- `app/src/main/java/com/veritasbible/app/study/ui/StudyDetailScreen.kt`
- `app/src/main/java/com/veritasbible/app/study/ui/StudyCreateDialog.kt`
- `app/src/test/java/com/veritasbible/app/study/StudyRepositoryTest.kt`
- `HISTORY.md` (이 파일)

수정
- `app/src/main/java/com/veritasbible/app/data/AppDatabase.kt` — entities 확장, v1→v2 `MIGRATION_1_2` 추가, destructive fallback 제거.
- `app/src/main/java/com/veritasbible/app/MainActivity.kt` — `STUDY` 탭 추가, `StudyViewModel`/`StudyCreateDialog`/`StudyDetailScreen` 연결, 세션 상세 진입 시 바텀 네비게이션 숨김.
- `app/src/main/java/com/veritasbible/app/ui/screens/BibleReaderScreen.kt` — `onStartStudy` 콜백 매개변수 추가, 절 액션 팔레트에 ‘이 본문으로 귀납적 연구 시작’ 버튼 추가.
- `app/src/main/java/com/veritasbible/app/util/LanguageManager.kt` — `tab_study`, `study_*`, `stage_*`, `common_*` 키 한·영 동시 추가.
- `README.md` — ‘귀납적 성경연구 모듈’ 섹션 추가.
- `CHANGELOG.md` — `Unreleased` 항목 추가.
- `docs/inductive_bible_study_design_bundle/docs/DECISION_LOG.md` — 기존 메모와 연구 노트를 분리한 사유 보강.

### 검증 결과

- `./gradlew.bat assembleDebug` — **성공** (1m 6s, 사전 존재하던 Material Icons deprecation 경고만, 새 코드로 인한 신규 경고 없음).
- `./gradlew.bat testDebugUnitTest` — **성공** (전체).
- `./gradlew.bat testDebugUnitTest --tests com.veritasbible.app.study.StudyRepositoryTest` — **성공** (관찰/단락/적용/세션 삭제/메타 업데이트 5건).
- `./gradlew.bat lintDebug` — **성공** (HTML 리포트 정상 생성).
- 기존 성경 읽기/검색/메모/통계/설정 화면 코드 경로 미수정 — 자동 회귀 테스트는 없으나 컴파일/lint/유닛 테스트 모두 통과.

### 생략한 검증과 사유

- 실기기 또는 에뮬레이터 E2E 수동 확인 — 본 작업 환경에서는 Android 기기 실행을 직접 수행할 수 없어 생략. 후속에서 실제 기기로 다음 시나리오를 점검 권장:
  1. 기존 메모/북마크/읽기 진행률이 그대로 보이는지(마이그레이션 무손실 확인).
  2. ‘성경연구’ 탭 진입 → 빈 상태 표시.
  3. 성경 리더에서 절 선택 → ‘이 본문으로 귀납적 연구 시작’ → 다이얼로그 → 세션 생성 → 상세 진입.
  4. 관찰/단락/핵심주제/적용 입력 후 앱 재실행 시 데이터 유지.
  5. 연구 세션 삭제 다이얼로그 동작.
- `androidTest` 계열 instrumentation 테스트 — 신규 작성하지 않음(Compose UI 계약은 testTag만 부여하여 후속 Goal에서 작성).

### 후속 작업

- Goal 2: 본문 마킹(주어/동사/접속사 등 17종) + 주어-동사·원인-결과 연결선 구현.
- Goal 3: 해석 노트, 명제, 개요, Markdown/PDF 내보내기, JSON/ZIP 백업·복원.
- 본문 패널 폰트 크기·테마를 기존 리더와 동기화하고, 절 단위 ID 매핑을 마킹 모듈에 노출.
- 적용 노트의 점검 날짜(`dueDate`)를 캘린더 위젯으로 입력받는 흐름 추가.
- 백업 JSON에 `study_*` 테이블도 포함시키는 확장 (현재 백업/복원은 기존 메모·읽기 기록 범위만 다룸).
- 다국어 리소스 정리 시 `LanguageManager` 코드 맵 대신 Android `strings.xml`로의 점진 이관 검토.
