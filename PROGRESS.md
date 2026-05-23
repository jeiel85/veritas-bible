# Veritas Bible 진행 기록

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

## 현재 구조

- 플랫폼: Android 네이티브
- UI: Jetpack Compose, Material 3, Deep Navy(#1A237E) 브랜드 테마
- 데이터: Room + assets/bible.json 번들 (31,105절, 한국어 1910 + 영어 WEB, PD)
- 주요 화면: 온보딩, 대시보드, 성경 리더, 검색, 노트, 설정
- 패키지: `com.veritasbible.app`
- 키스토어: `.keystore/veritas-bible-upload.jks` (유효 2053-10-07)
