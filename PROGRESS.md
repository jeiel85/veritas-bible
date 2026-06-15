# Veritas Bible 진행 기록

## 2026-06-15 - v1.8.3 컴파일 검증 및 새 버전 릴리즈 준비

- GitHub Issue #58 `v1.8.3 컴파일 검증 및 새 버전 릴리즈 준비` 생성.
- 연구 상세 본문 구조 연결선 수정분(`StudyDetailScreen.kt`)에 대해 `.\gradlew.bat assembleDebug --no-daemon --stacktrace` 실행, 빌드 성공 확인.
- `app/build.gradle.kts` 버전 정보를 `versionName 1.8.3`, `versionCode 8`로 갱신.
- GitHub Release 본문 `docs/releases/v1.8.3.md`와 Play Console 릴리즈 노트 `play_store/release_notes/v1.8.3.txt` 작성.
- `.\scripts\validate_release_version.ps1 v1.8.3` 실행, Play Console 노트 길이(`ko-KR 168/500`, `en-US 329/500`) 및 릴리즈 문서 정합성 검증 성공.
- `.\scripts\export_play_console_assets.ps1 v1.8.3` 실행, release AAB 빌드 및 바탕화면 `Build` 폴더 내보내기 성공.
- 바탕화면 생성 파일: `veritas-bible-v1.8.3-vc8.aab`, `veritas-bible-v1.8.3-vc8-release-notes.txt`.
- release bundle manifest에서 `package=com.veritasbible.app`, `versionCode=8`, `versionName=1.8.3` 확인.

## 2026-05-25 — v1.6.0 새 버전 릴리즈 검증 및 배포 자산 내보내기

- GitHub Issue #58 `v1.6.0 새 버전 릴리즈 검증 및 배포 자산 내보내기` 생성 (로컬 기록).
- `.keystore/release.env` 내 하드코딩된 키스토어 경로(`veritas-bible` -> `veritas-bible-android`) 수정.
- `.\scripts\validate_release_version.ps1 v1.6.0` 실행하여 `app/build.gradle.kts`와 `docs/releases/v1.6.0.md`, `play_store/release_notes/v1.6.0.txt` 정합성 검증 완료.
- `.\scripts\export_play_console_assets.ps1 v1.6.0` 실행하여 정식 릴리즈 AAB(`veritas-bible-v1.6.0.aab`) 빌드 및 바탕화면 루트 내보내기 성공.
- 바탕화면 생성 파일: `veritas-bible-v1.6.0.aab`, `veritas-bible-v1.6.0-release-notes.txt`.
- Git 릴리즈 태그 `v1.6.0` 로컬 생성 및 푸시.

## 2026-05-24 — GitHub Pages 랜딩 페이지 및 저장소 메타 정비


- GitHub Issue #57 `GitHub Pages 랜딩 페이지 및 스토어 등록 자료 정비` 생성.
- `docs/index.html` 신규 작성: Veritas Bible의 오프라인 한·영 성경, 로컬 개인정보 정책, 귀납적 성경연구 워크벤치 정체성을 보여주는 GitHub Pages 랜딩 페이지.
- `docs/assets/` 신규 구성: 랜딩 페이지용 앱 아이콘, 피처 그래픽, 실제 기기 스크린샷 4장 복사.
- 기존 `docs/privacy.html` 및 `play_store/PRIVACY_POLICY.md`가 이미 존재하여 개인정보 처리 방침 신규 작성은 생략하고 랜딩 페이지/README에서 연결.
- Play Store 등록용 기존 애셋 확인: `play_store_icon_512.png`(512×512), `feature_graphic_1024x500.png`(1024×500).
- `README.md` 전면 개편: 앱 소개, 핵심 기능, 기술 스택, GitHub Pages/Privacy 링크, Play Store 등록 자료, 릴리즈 절차를 정리.
- GitHub Pages 설정 확인: `main` 브랜치 `/docs`, URL `https://jeiel85.github.io/veritas-bible-android/`.
- GitHub 저장소 설명, 홈페이지 URL, 토픽(android/kotlin/jetpack-compose/material3/bible/bible-study/offline-first/room/sqlite/privacy-first) 갱신.
- `docs/index.html`, `docs/privacy.html` 로컬 참조 파일 정적 검증 통과.

## 2026-05-22

- GitHub Issue #52 `리뉴얼 프로젝트 기능 및 UI 이관` 생성.
- `D:\Project\veritas-bible-renew` 프로젝트를 기준으로 기존 Flutter 앱 구조를 Android/Kotlin Compose 프로젝트 구조로 전환.
- 기존 `lib`, `assets`, `ios`, `web`, `linux`, `macos`, `windows`, Flutter 관련 설정 파일을 제거하고 리뉴얼 프로젝트의 `app`, `gradle`, Gradle 설정, Android 리소스 및 Kotlin 소스 전체를 이관.
- 빌드 산출물인 `.build-outputs/app-debug.apk`는 저장소에 포함하지 않음.
- 명령줄 빌드 재현을 위해 Gradle Wrapper를 추가하고 Android Gradle Plugin 9.1.1 요구사항에 맞춰 Gradle 9.3.1을 사용하도록 설정.
- `debug.keystore`가 없는 환경에서도 debug 빌드가 가능하도록, 파일이 있을 때만 커스텀 debug signing 설정을 적용하도록 조정.
- GitHub Issue #53 `새 버전 생성 흐름 정비` 생성.
- Nightseed Survivor의 릴리즈 방식과 동일하게 `docs/releases/vX.Y.Z.md`, `play_store/release_notes/vX.Y.Z.txt`, 태그 기반 GitHub Release 자동화, 버전 검증 스크립트 흐름을 Android 네이티브 프로젝트에 맞춰 도입.
- 검증 성공: `.\scripts\validate_release_version.ps1 v1.0.0`, `.\gradlew.bat assembleDebug`, `.\gradlew.bat testDebugUnitTest`.
- GitHub Issue #54 `릴리즈 산출물 바탕화면 내보내기 추가` 생성.
- Play Console 업로드용 AAB와 릴리즈 노트 TXT를 바탕화면 `veritas-bible-vX.Y.Z` 폴더로 복사하는 `scripts/export_play_console_assets.ps1` 추가.
- 로컬 업로드 키가 없는 환경에서도 릴리즈 노트 TXT는 먼저 바탕화면에 복사되도록 처리.
- GitHub Issue #55 `릴리즈 산출물 바탕화면 루트 내보내기로 변경` 생성.
- 내보내기 위치를 버전별 폴더가 아닌 바탕화면 루트의 `veritas-bible-vX.Y.Z.aab`, `veritas-bible-vX.Y.Z-release-notes.txt`로 변경.
- GitHub Issue #56 `로컬 릴리즈 키 생성 및 AAB 내보내기` 생성.
- `.keystore/`를 Git 제외 대상에 추가하고, `scripts/export_play_console_assets.ps1`가 `.keystore/release.env`를 자동 로드하도록 개선.
- `.keystore/veritas-bible-upload.jks`, `.keystore/release.env`, `.keystore/release-keystore-info.txt` 생성. PKCS12 특성상 store password와 key password를 동일하게 기록.
- `.\scripts\export_play_console_assets.ps1 v1.0.0`로 release AAB 빌드 및 바탕화면 루트 내보내기 성공.
- 바탕화면 생성 파일: `veritas-bible-v1.0.0.aab`, `veritas-bible-v1.0.0-release-notes.txt`.
- `jarsigner -verify`로 AAB 서명 확인 완료.

## 2026-05-23

- 신규 작업 트리거: Play Console 신규 앱 등록 준비.
- 성경 본문 데이터 전면 교체: 하드코딩 70여 절 → 한국어 1910(개역) + 영어 WEB 31,105절 (ebible.org Public Domain).
- `scripts/build_bible_json.py` 추가: USFM → 통합 `assets/bible.json` (9.6MB).
- `BibleDataPrepopulator`를 Android `JsonReader` 스트리밍 + 1000건 단위 batch insert로 재작성, 실제 진행률 노출.
- `BibleViewModel.downloadBibleData()`의 가짜 "보안 핸드셰이크/SHA-256/CDN" 문구 제거, 실제 prepopulate 진행률만 표시. `init {}`에서 첫 실행 시 자동 로드.
- 온보딩·설정 화면의 과장된 보안/주권 문구를 정직한 표현으로 다듬음.
- `applicationId` / `namespace` 를 `com.aistudio.veritasbible.pxlqwt` / `com.example` → `com.veritasbible.app` 으로 확정.
- Kotlin 패키지 경로 일괄 이동 + `com.example` → `com.veritasbible.app` 치환.
- Deep Navy(#1A237E) 브랜드 컬러 기반 `Color.kt`/`Theme.kt` 재정의, 동적 컬러 비활성화.
- `scripts/generate_app_assets.py` 추가: 펼친 성경책 실루엣 + Deep Navy 배경 아이콘 (`mipmap-*` webp 5종), Adaptive 전·배경 PNG, Play Store 512×512 아이콘, 1024×500 피처 그래픽 생성.
- Play Store 등록정보 작성: `play_store/listing/ko-KR/`, `en-US/` (제목·짧은 설명·전체 설명).
- 개인정보 처리방침: `play_store/PRIVACY_POLICY.md` + `docs/privacy.html` (한·영). GitHub Pages 활성화: <https://jeiel85.github.io/veritas-bible/privacy.html>.
- 실 기기(R3CWC0KB53Z, 1080×2340)에서 8장 스크린샷 캡처: 온보딩 3장 + 리더(한·영 병행) + 검색 결과 577건 + 통계 대시보드 + 노트 + 설정.
- `scripts/export_play_console_assets.ps1 v1.0.0` 실행 → 서명된 AAB 빌드 + 바탕화면 내보내기 성공.
- Play Console 업로드 체크리스트: `play_store/UPLOAD_GUIDE.md`.
- `.bible_source/` 를 `.gitignore`에 추가.

## 2026-05-23 (오후) — Play Console 신규 앱 등록 진행

- Chrome MCP + Play Console (`pedaiah85@gmail.com`, Sitdory 개발자 계정 `6375329746023339599`)으로 신규 앱 등록 진행.
- 앱 만들기 완료: 앱 ID `4975661064860878106`, 패키지 `com.veritasbible.app`.
- 자산 라이브러리에 모든 그래픽 fetch 업로드(GitHub raw URL 경유): 아이콘 512×512, 피처 1024×500, 휴대전화 스크린샷 8장.
- 키스토어 백업 zip 생성: `~/Desktop/veritas-bible-keystore-backup.zip` (jks + release.env + info.txt).
- 앱 콘텐츠 설문 11개 중 자동화 완료 6개(개인정보 처리방침 URL, 광고, 앱 액세스, 광고 ID, 정부 앱, 일부) + 사용자 수동 4개(콘텐츠 등급, 타겟층, 데이터 보안, 금융, 건강).
- 스토어 설정: 앱 카테고리 `도서/참고자료`, 연락처 이메일 `pedaiah85@gmail.com`, 웹사이트 `https://github.com/jeiel85/veritas-bible` 등록.
- 내부 테스트 트랙 활성화: 테스터 그룹 `Veritas Bible internal testers` (pedaiah85@gmail.com) 등록.
- 내부 테스트 새 버전 `1 (1.0.0)`: AAB(15MB) 업로드 + 출시 노트 등록.
- 남은 작업: 기본 스토어 등록정보 자산 슬롯 적용/저장 후 게시 개요에서 검토 제출.

## 2026-05-24 — Goal 5 후속 보강 (스키마 백포팅 / 인쇄 / Sweeper / 백업 토글 / 칩 색상)

- v1~v4 schema JSON 백포팅(`app/schemas/.../{1..4}.json`) + Robolectric 가 읽도록 schemas/ 를 `main` 소스셋 assets 에 합본. raw SQL 대신 `MigrationTestHelper` 로 4건 전환.
- 시스템 인쇄: `StudyPrintAdapter` 신규, 리포트 패널 ‘인쇄’ 버튼으로 진입.
- 마킹/단락 삭제 sweeper: stale linkedMarkupId(s), linkedDivisionIds, linkedDivisionId 자동 정리. 테스트 3건 추가.
- 설정 → 백업 복원에 ‘기존 study 데이터 덮어쓰기’ Material 3 Switch 추가, repository/ViewModel 시그니처 확장.
- 명시 링크 칩 색상화(`LinkedMarkupChip` / `LinkedMarkupChipRow` / `LinkedDivisionChipRow`).
- assembleDebug / testDebugUnitTest / lintDebug 모두 통과.

## 2026-05-24 — Goal 4 후속 통합 (PDF/백업/링크/DatePicker/마이그레이션 테스트)

- DB v4→v5: `linkedMarkupId`, `linkedMarkupIds`, `linkedDivisionIds` 컬럼 추가 + `exportSchema=true` 전환(v5 JSON 산출).
- Markdown→PDF 변환기 `StudyPdfExporter`(AOSP `PdfDocument`, 외부 의존성 0) + FileProvider 등록.
- `StudyBackup` 으로 11개 study 테이블을 기존 암호화 백업/복원 워크플로에 통합. `BibleRepository` 생성자에 DB 핸들 추가.
- 적용 점검일 입력을 Material 3 `DatePicker` 로 교체(ISO yyyy-MM-dd 저장, 지우기 버튼).
- 해석/명제에 마킹·단락 명시 링크 UI(SingleSelect/MultiSelect chip).
- `StudyMigrationTest` (raw SQL, v1→v5) 4건 + `StudyBackupTest` 2건 신규.
- assembleDebug / testDebugUnitTest / lintDebug 모두 통과.

## 현재 구조

- 데이터: Room **v5** + assets/bible.json 번들 (31,105절, 한국어 1910 + 영어 WEB, PD)

## 2026-05-24 — Goal 3 연구 완성·내보내기

- 데이터 모델 v3→v4: `study_interpretations / study_theme_checks / study_propositions / study_outline_nodes` + `study_applications.practiced`.
- 신규 상수: `ThemeCheckKey`(5), `PropositionStatus`(3).
- Repository: 4개 도메인 CRUD + `toggleApplicationPracticed` + `saveApplication(practiced=)`.
- ViewModel: 4종 Flow + 12개 액션 + `buildMarkdownReport` 비동기 직렬화.
- UI: 9개 탭(OBSERVATION/MARKUP/DIVISION/INTERPRETATION/THEME(+검증)/PROPOSITION/OUTLINE/APPLICATION(+점검일·실천 토글)/REPORT(+공유)).
- Markdown 내보내기: `StudyMarkdownExporter`(외부 라이브러리 없음, KO/EN 라벨), 클립보드 복사 + ACTION_SEND 공유. PDF 보류 안내.
- 테스트: `StudyRepositoryTest` Goal 3 6건 + `StudyMarkdownExporterTest` 3건. assembleDebug / testDebugUnitTest / lintDebug 모두 통과.
- 문서: CHANGELOG / HISTORY / DATA_MODEL / DECISION_LOG 갱신.

## 현재 구조

- 데이터: Room v4 + assets/bible.json 번들 (31,105절, 한국어 1910 + 영어 WEB, PD)

## 2026-05-24 — Goal 2 본문 마킹·구조 분석·관찰 강화

- 마킹/연결/성격 태그 데이터 모델(Room v2→v3 마이그레이션): `study_markups / study_markup_links / study_character_tags`.
- `MarkType`(18), `LinkType`(9), `CharacterTagType`(14), `ObservationQuestion`(8) 상수화.
- `StudyRepository` markup/link/tag CRUD + 중복 차단 + 관찰 템플릿 upsert.
- `StudyDetailScreen` 마킹/구조 탭(다이얼로그 → 절·단어 칩·타입 선택), 본문 패널 인라인 하이라이트, 관찰 질문 8종 카드, 단락 카드 성격 태그.
- `MarkupTheme.kt`(타입별 색상) + `StudyMarkupDialog.kt`(모바일 단어 칩 선택) 신규.
- 테스트: `StudyRepositoryTest` Goal 2 케이스 7건 추가, `MarkupTokenizerTest` 4건 신규.
- 검증: assembleDebug / testDebugUnitTest / lintDebug 모두 통과.
- 문서: CHANGELOG / HISTORY / DATA_MODEL / DECISION_LOG 갱신.

## 2026-05-24 — Goal 1 귀납적 성경연구 워크벤치 기반 통합

- `docs/inductive_bible_study_design_bundle` 설계 문서(README/PRODUCT_REQUIREMENTS/FEATURE_SPEC/UX_ARCHITECTURE/DATA_MODEL/IMPLEMENTATION_ROADMAP/DECISION_LOG/GOAL_01) 정독.
- 신규 패키지 `com.veritasbible.app.study` 도입: `data/StudyEntities.kt`, `data/StudyDao.kt`, `repository/StudyRepository.kt`, `ui/StudyViewModel.kt`, `ui/StudyListScreen.kt`, `ui/StudyDetailScreen.kt`, `ui/StudyCreateDialog.kt`.
- Room DB v1→v2 마이그레이션(`AppDatabase.MIGRATION_1_2`) 추가. `study_sessions / study_observations / study_divisions / study_applications` 4개 테이블 생성. `fallbackToDestructiveMigration` 제거로 기존 사용자 데이터 보존 우선.
- `MainTab` 에 `STUDY` 탭 추가, 세션 상세 진입 시 바텀 네비 숨김. 성경 리더 액션 팔레트에 ‘이 본문으로 귀납적 연구 시작’ 버튼 추가.
- 연구 상세 화면: 본문 패널 + `관찰 / 단락 / 핵심주제 / 적용 / 그 외 단계` 탭. 실제 저장 가능한 단계는 관찰·단락·핵심주제·적용 4종. 나머지 단계는 후속 Goal 자리.
- `LanguageManager` 에 `tab_study`, `study_*`, `stage_*`, `common_*` 한·영 키 추가.
- 테스트: `app/src/test/java/com/veritasbible/app/study/StudyRepositoryTest.kt` (관찰/단락/적용 저장, 세션 삭제 cascade, 메타 업데이트 5건).
- 검증: `assembleDebug`, `testDebugUnitTest`, `lintDebug` 모두 통과. 실기기 수동 점검은 다음 작업에서 진행 권장.
- 문서: `README.md` 모듈 섹션, `CHANGELOG.md` Unreleased 항목, `HISTORY.md` 신규, `DECISION_LOG.md` 분리 사유 보강.

## 현재 구조

- 플랫폼: Android 네이티브
- UI: Jetpack Compose, Material 3, Deep Navy(#1A237E) 브랜드 테마
- 데이터: Room v3 + assets/bible.json 번들 (31,105절, 한국어 1910 + 영어 WEB, PD)
- 주요 화면: 온보딩, 대시보드, 성경 리더, 검색, 노트, 설정, **성경연구 (목록·상세)**
- 패키지: `com.veritasbible.app` (+ `.study` 서브패키지)
- 키스토어: `.keystore/veritas-bible-upload.jks` (유효 2053-10-07)
