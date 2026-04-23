# Project Open Bible Progress History

## 2026-04-23 (Thursday)
- **앱 아이콘 공식 적용:** Deep Navy 배경의 성경책 디자인 아이콘 생성 및 적용 완료
  - `scripts/generate_icon.py`를 통한 아이콘 이미지 자동 생성
  - `flutter_launcher_icons`를 활용한 Android/iOS 플랫폼별 아이콘 배포
- **버전 업데이트:** 앱 버전을 `1.0.8+1`로 상향 조정
- **소스 관리:** 아이콘 리소스 추가 및 설정 파일 업데이트

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

## 2026-04-22 (Wednesday) - 최종 고도화 완료
- **SQLite 전환:** 대용량 데이터를 효율적으로 처리하기 위해 JSON에서 SQLite로 데이터베이스 엔진 교체
- **66권 전체 지원:** 구약/신약 66권 전체 메타데이터(권, 장 수) 구축
- **검색 기능 구현:** `SearchScreen` 및 `DatabaseHelper`를 통한 전체 성경 키워드 검색 지원
- **UI 개선:** 탭 방식의 구약/신약 목록, 장 선택 그리드(Bottom Sheet) 구현
- **프로젝트 아키텍처 완성:** 모델, 서비스(DB), 상태 관리(Provider), 화면(UI)의 깔끔한 분리

## 2026-04-22 (Wednesday) - 릴리즈 자동화 및 첫 배포
- **GitHub Actions 워크플로 구축:** 태그(`v*`) 푸시 시 자동으로 Android APK를 빌드하고 GitHub Release에 업로드하는 파이프라인 완성 (`.github/workflows/release.yml`)
- **최초 릴리즈:** `v1.0.0` 태그와 함께 소스 코드 전체 GitHub 푸시 및 첫 배포 진행

## 2026-04-22 (Wednesday) - 브랜딩 및 디자인 고도화
- **앱 브랜딩 변경:** 서비스명을 **Veritas Bible (베리타스 성경)**으로 확정하고 프로젝트 전체 적용
  - 안드로이드 라벨, iOS 표시 이름, 웹 타이틀 일괄 수정
  - GitHub 리포지토리 이름을 `open-bible`에서 `veritas-bible`로 변경 완료
- **테마 컬러 적용:** Deep Navy(#1A237E)를 메인 컬러로 설정하여 신뢰감 있고 전문적인 UI 구축
- **아이콘 설정 자동화:** `flutter_launcher_icons` 패키지 구성 완료 (assets/icon/icon.png 기반 자동 생성 준비)

  - 기술 스택: Flutter (Dart)
  - 지원 플랫폼: Android, iOS, Web, Windows 등
- Flutter SDK 경로 설정 확인 (`D:\flutter`)
- 프로젝트 기본 스캐폴딩 생성 완료
- 성경 데이터 모델 설계 (`lib/models/bible.dart`)
  - Bible, Book, Verse 클래스 구현
