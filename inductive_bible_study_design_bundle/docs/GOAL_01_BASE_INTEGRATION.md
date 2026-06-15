# Goal 1 — 귀납적 연구 모듈의 출시급 기반 통합

```text
작업 루트는 D:\Project\veritas-bible-android\ 입니다.

기존 앱은 이미 성경 읽기, 메모, 읽기 계획, 읽기 진행률 기능이 구현된 Android 성경앱입니다. 이번 작업은 기존 기능을 갈아엎는 것이 아니라, docs\inductive_bible_study_design_bundle 설계 문서를 기준으로 “귀납적 성경연구 워크벤치” 기능을 출시 가능한 품질로 통합하기 위한 1차 기반을 만드는 것입니다.

먼저 아래 문서를 반드시 읽고 현재 코드 구조와 비교해 주세요.

- D:\Project\veritas-bible-android\AGENTS.md
- D:\Project\veritas-bible-android\README.md
- D:\Project\veritas-bible-android\CHANGELOG.md
- D:\Project\veritas-bible-android\HISTORY.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\README.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\PRODUCT_REQUIREMENTS.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\FEATURE_SPEC.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\UX_ARCHITECTURE.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\DATA_MODEL.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\BIBLE_STUDY_METHOD_MAPPING.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\IMPLEMENTATION_ROADMAP.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\DECISION_LOG.md

이번 Goal의 작업 범위:

1. 기존 앱 구조 분석
- 현재 성경 본문 데이터 구조, 메모 구조, 읽기 계획 구조, 읽기 진행률 구조, 화면 네비게이션 구조를 파악해 주세요.
- 기존 성경 읽기/메모/읽기 계획/읽기 진행률 기능은 회귀되지 않게 보존해 주세요.
- 기존 메모 기능을 귀납적 연구 노트로 억지로 바꾸지 말고, 연구 기능은 별도 모듈로 추가해 주세요.

2. 연구 모듈 진입점 추가
- 앱 안에 “성경연구” 또는 “연구” 진입점을 추가해 주세요.
- 기존 성경 읽기 화면에서 현재 본문 또는 선택한 본문 범위로 연구 세션을 만들 수 있는 흐름을 추가해 주세요.
- 연구 세션 목록 화면을 추가해 주세요.
- 연구 세션 상세 화면의 기본 레이아웃을 추가해 주세요.

3. 1차 데이터 모델과 로컬 저장소 추가
- StudySession 중심의 로컬 데이터 모델을 추가해 주세요.
- 최소한 다음 항목은 저장/조회/수정 가능해야 합니다.
  - 연구 세션 ID
  - 연구 제목
  - 본문 범위
  - 현재 연구 단계
  - 관찰 노트
  - 단락 구분
  - 핵심주제
  - 적용 노트
  - 생성일/수정일
- DB 마이그레이션이 필요하면 기존 사용자 데이터 손실 없이 처리해 주세요.

4. 연구 상세 화면 1차 구현
- 선택된 본문 범위를 표시해 주세요.
- 본문 영역과 연구 입력 영역을 분리해 주세요.
- 이번 Goal에서는 아래 단계가 실제 저장/수정 가능해야 합니다.
  - 관찰
  - 단락 구분
  - 핵심주제
  - 적용
- 준비, 구조, 성격, 해석, 명제, 개요 단계는 확장 가능한 화면 자리만 만들어도 됩니다.

5. 품질 기준
- 외부 API, 로그인, 클라우드 동기화, AI API, 분석 SDK, 광고 SDK는 추가하지 마세요.
- 사용자 연구 데이터는 로컬 우선으로 저장해 주세요.
- 기존 앱의 디자인 시스템, 네비게이션 방식, 상태 관리 방식을 최대한 따르세요.
- 한국어 UI를 기본으로 유지하고, 프로젝트가 다국어 리소스를 사용한다면 영어 리소스도 함께 추가해 주세요.
- 빈 상태, 저장 실패, 삭제 확인, 마이그레이션 실패 가능성을 고려해 주세요.

6. 문서 갱신
- README 또는 관련 docs에 이번에 추가한 연구 모듈 구조를 기록해 주세요.
- CHANGELOG.md에 사용자에게 보이는 변경 사항을 기록해 주세요.
- HISTORY.md에 작업 내용, 변경 파일, 검증 결과, 후속 작업을 기록해 주세요.
- DECISION_LOG가 있다면 기존 메모와 연구 노트를 분리한 이유를 기록해 주세요.

7. 검증
- 가능한 범위에서 lint, test, build를 실행해 주세요.
- 실행하지 못한 검증은 성공했다고 기록하지 말고 생략 사유를 남겨 주세요.
- 기존 성경 읽기, 메모, 읽기 계획, 읽기 진행률이 정상 동작하는지 확인해 주세요.
- 새 연구 세션 생성, 목록 표시, 상세 진입, 관찰/단락/핵심주제/적용 저장이 되는지 확인해 주세요.

AGENTS.md 규칙을 따르고, 파괴적 명령, 강제 푸시, 기존 데이터 삭제, 외부 서비스 추가, 시크릿 변경은 하지 마세요. 완료 후 작업 요약, 변경 파일, 검증 결과, 생략한 검증, 후속 작업을 한국어로 보고해 주세요.
```
