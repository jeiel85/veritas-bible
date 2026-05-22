# Veritas Bible - 개발 에이전트 지침

## 언어 및 커뮤니케이션

- 모든 답변은 한글로 작성합니다.
- Git 커밋 메시지 및 GitHub Issue 코멘트 또한 한글로 작성합니다.
- 기술적인 용어는 원어를 병기할 수 있으나, 설명의 주 언어는 한글입니다.

## 기술 스택 및 아키텍처

- Framework: Android 네이티브, Jetpack Compose
- Language: Kotlin
- Local DB: Room, SQLite
- Design System: Material 3, Deep Navy (#1A237E) 브랜딩 컬러 준수

## 소스 및 이력 관리

- 소스 코드가 수정되면 즉시 Git에 커밋하여 이력을 세밀하게 남깁니다.
- 모든 작업은 작업 시작 전 GitHub Issue를 먼저 생성합니다.
- 작업 완료 후 해당 이슈를 닫고 관련 코멘트를 남깁니다.
- 코드 수정, 빌드, 커밋 등의 주요 상황 발생 시 항상 `PROGRESS.md`를 갱신합니다.

## 배포 및 버전 관리 규칙

- GitHub 버전 태그는 반드시 `vX.Y.Z` 형식을 준수합니다.
- 태그 버전은 앱의 명시 버전 정보와 일치해야 합니다.
- 릴리즈 전 `docs/releases/vX.Y.Z.md`와 `play_store/release_notes/vX.Y.Z.txt`를 반드시 작성합니다.
- `scripts/validate_release_version.ps1 vX.Y.Z`로 태그, 앱 버전, 릴리즈 문서 존재 여부를 검증합니다.
- Play Console 업로드를 위해 `scripts/export_play_console_assets.ps1 vX.Y.Z`로 AAB와 릴리즈 노트 TXT를 바탕화면 루트에 직접 내보냅니다.
- `v*` 태그가 푸시되면 GitHub Actions가 APK/AAB를 빌드하고 GitHub Release를 생성합니다.

## 코드 작성 원칙

- 함수와 변수명은 의미가 명확하게 작성합니다.
- Kotlin Null Safety를 엄격히 준수합니다.
- 다양한 화면 크기에 대응할 수 있도록 유연한 Compose 레이아웃을 사용합니다.
- 버전 정보나 앱 이름 등은 공통 설정 파일에서 관리합니다.

## 문서 파일 역할

- `agents.md`: 개발 지침 및 에이전트 행동 강령
- `README.md`: 프로젝트 소개 및 설치/실행 가이드
- `docs/releases/`: GitHub Release 본문
- `play_store/release_notes/`: Play Console 릴리즈 노트
- `PROGRESS.md`: 상세 개발 이력 및 타임라인
