# Veritas Bible 진행 기록

## 2026-05-22

- GitHub Issue #52 `리뉴얼 프로젝트 기능 및 UI 이관` 생성.
- `D:\Project\veritas-bible-renew` 프로젝트를 기준으로 기존 Flutter 앱 구조를 Android/Kotlin Compose 프로젝트 구조로 전환.
- 기존 `lib`, `assets`, `ios`, `web`, `linux`, `macos`, `windows`, Flutter 관련 설정 파일을 제거하고 리뉴얼 프로젝트의 `app`, `gradle`, Gradle 설정, Android 리소스 및 Kotlin 소스 전체를 이관.
- 빌드 산출물인 `.build-outputs/app-debug.apk`는 저장소에 포함하지 않음.

## 현재 구조

- 플랫폼: Android 네이티브
- UI: Jetpack Compose, Material 3
- 데이터: Room 기반 로컬 데이터베이스
- 주요 화면: 온보딩, 대시보드, 성경 리더, 검색, 노트, 설정
