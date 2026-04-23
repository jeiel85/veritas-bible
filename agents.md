# 🤖 Veritas Bible - 개발 에이전트 지침 (Development Instructions)

본 문서는 **Veritas Bible** 프로젝트에 참여하는 모든 AI 에이전트와 개발자가 준수해야 할 원칙과 규정을 담고 있습니다.

## 🌍 언어 및 커뮤니케이션
- **모든 답변은 한글**로 작성합니다.
- Git 커밋 메시지 및 GitHub Issue 코멘트 또한 **한글**로 작성합니다.
- 기술적인 용어는 원어를 병기할 수 있으나, 설명의 주 언어는 한글입니다.

## 🛠 기술 스택 및 아키텍처
- **Framework**: Flutter 3.5.0+
- **Language**: Dart 3.0.0+
- **Architecture**: MVVM 패턴 지향 (Model → Service/DB → Provider → Screen)
- **State Management**: Provider
- **Local DB**: SQLite (sqflite)
- **Design System**: Material 3, Deep Navy (#1A237E) 브랜딩 컬러 준수

## 🚀 소스 및 이력 관리 (Critical)
- **즉시 커밋**: 소스 코드가 수정되면 즉시 Git에 커밋하여 이력을 세밀하게 남깁니다.
- **GitHub Issue 연동**: 
  - 모든 작업은 작업 시작 전 **GitHub Issue**를 먼저 생성합니다.
  - 작업 완료 후 해당 이슈를 닫고(`close`) 관련 코멘트를 남깁니다.
- **이력 문서화**: 코드 수정, 빌드, 커밋 등의 주요 상황 발생 시 항상 `PROGRESS.md`를 갱신하여 프로젝트 방향성을 유지합니다.

## 📦 배포 및 버전 관리 규칙
- **버전 태그 형식**: GitHub의 버전 태그는 반드시 **`vX.Y.Z`** 형식을 준수해야 합니다. (예: `v1.0.8`, `v1.1.0`)
- **버전 정합성**: 생성된 태그 버전은 반드시 `pubspec.yaml`에 명시된 `version` 정보와 일치해야 합니다.
- **릴리즈 자동화**: `v*` 태그가 푸시되면 GitHub Actions를 통해 자동으로 빌드 및 릴리즈가 생성됩니다.

## 📝 코드 작성 원칙
- **Clean Code**: 함수와 변수명은 의미가 명확하게 작성합니다.
- **Null Safety**: Dart의 Null Safety 기능을 엄격히 준수합니다.
- **UI 최적화**: 다양한 화면 크기에 대응할 수 있도록 유연한 레이아웃(FittedBox, Expanded 등)을 사용합니다.
- **하드코딩 금지**: 버전 정보나 앱 이름 등은 공통 설정 파일(`pubspec.yaml`, metadata 등)에서 관리합니다.

## 📁 문서 파일 역할 분담
- `agents.md`: 개발 지침 및 에이전트 행동 강령 (본 문서)
- `README.md`: 프로젝트 소개 및 설치/실행 가이드
- `FEATURES.md`: 기능 명세 및 상용 앱 분석 결과
- `PROGRESS.md`: 상세 개발 이력 및 타임라인
