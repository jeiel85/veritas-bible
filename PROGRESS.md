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

## 현재 구조

- 플랫폼: Android 네이티브
- UI: Jetpack Compose, Material 3
- 데이터: Room 기반 로컬 데이터베이스
- 주요 화면: 온보딩, 대시보드, 성경 리더, 검색, 노트, 설정
