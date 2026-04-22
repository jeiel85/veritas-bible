# Project Open Bible Progress History

## 2026-04-22 (Wednesday)
- 프로젝트 초기 설정 및 GitHub 저장소 연결
- 상위 5개 성경 앱(YouVersion, Blue Letter Bible 등) 분석 완료
  - 핵심 기능 도출: 병행 읽기, 원어 연구, 개인화 기능(노트, 하이라이트), 오프라인 지원
- 성경 데이터 확보 방안 조사
  - CrossWire, Bible-api.com, GitHub 오픈 데이터 활용
- 라이선스 전략 수립
  - 초기: 퍼블릭 도메인(개역한글, KJV, WEB) 기반 개발
  - 확장: 사용자 커스텀 데이터 임포트 기능 제공

## 2026-04-22 (Wednesday) - 추가 작업
- 상위 10개 상용 앱 분석 및 핵심 기능 도출 (`FEATURES.md` 참조)
- 핵심 목표 (MVP): 오프라인 성경 네비게이션, 읽기 화면, 개인화(다크모드/폰트), 전역 상태 관리
- Flutter 프로젝트 초기화 완료 (`open_bible`)
- **MVP 앱 전체 구현 완료:**
  - `sample_bible.json`: 창세기 1~2장, 출애굽기 1장 샘플 데이터 구성
  - `BibleProvider`, `SettingsProvider`: 전역 상태 관리 구성
  - `HomeScreen`: 성경 각 권 선택 메뉴
  - `ReadScreen`: 장/절 읽기 뷰, 장 넘김, 폰트 크기 조절, 다크모드 적용
  - `pubspec.yaml` 에셋 등록 및 최종 앱 구조 완성

  - 기술 스택: Flutter (Dart)
  - 지원 플랫폼: Android, iOS, Web, Windows 등
- Flutter SDK 경로 설정 확인 (`D:\flutter`)
- 프로젝트 기본 스캐폴딩 생성 완료
- 성경 데이터 모델 설계 (`lib/models/bible.dart`)
  - Bible, Book, Verse 클래스 구현
