# CHANGELOG.md

## v0.3.0 - 2026-05-24

### Added
- 기존 앱 루트 `D:\Project\veritas-bible-android\`와 설계 문서 경로 `docs\inductive_bible_study_design_bundle`를 기준으로 한 실제 통합 계획 추가
- 기존 성경 읽기/메모/읽기 계획/읽기 진행률 기능을 보존하면서 귀납적 성경연구 모듈을 추가하는 통합 원칙 추가
- Goal 1, Goal 2, Goal 3로 나눈 에이전트 코딩용 상세 프롬프트 추가
- `docs/VERITAS_INTEGRATION_PLAN.md` 추가
- Goal별 개별 프롬프트 파일 추가: `GOAL_01_BASE_INTEGRATION.md`, `GOAL_02_MARKUP_STRUCTURE.md`, `GOAL_03_INTERPRETATION_EXPORT.md`

### Changed
- `README.md`에 실제 프로젝트 경로와 권장 Goal 실행 순서 추가
- `AGENTS.md` 프로젝트 설정값을 Veritas Bible Android 통합 상황에 맞게 조정
- `docs/GOAL_PROMPTS.md`를 실제 경로 기반 3단계 Goal 프롬프트로 확장

### Documentation
- 기존 메모 기능과 연구 노트 기능을 분리해야 하는 이유를 `DECISION_LOG.md`에 기록

---

## v0.2.0 - 2026-05-24

### Added
- 귀납적 성경연구 특화 앱을 MVP가 아닌 출시 가능한 수준의 제품으로 재정의
- 본문 선택, 관찰, 구조 분석, 성격 규정, 단락 분해, 해석, 핵심주제, 명제화, 개요화, 적용으로 이어지는 전체 연구 워크플로우 명세 추가
- 본문 마킹, 주어-동사 연결, 원인-결과 연결, 대조 연결, 성격 태그, 단락 제목, 핵심주제 검증 기능 설계 추가
- 로컬 우선 저장, 자동 저장, 백업/복원, Markdown/PDF 출력, 검색, 접근성, 다국어, 릴리즈 품질 기준 추가
- AI 코치 기능을 정답 제시가 아닌 연구 과정 점검 역할로 제한하는 정책 추가
- `docs/` 하위 제품 요구사항, 기능 명세, UX 설계, 데이터 모델, 연구 방식 매핑, AI 정책, 로드맵, 품질 체크리스트, Goal 프롬프트, 의사결정 기록 문서 추가

### Changed
- 기존 범용 `AGENTS.md` 규칙을 유지하면서 귀납적 성경연구 앱 프로젝트 고유 규칙을 상단에 통합
- `README.md`를 범용 에이전트 템플릿 소개에서 귀납적 성경연구 앱 제품 명세 중심으로 재작성
- 출시 기준을 MVP에서 production-ready 수준으로 상향

### Security
- 개인 적용 메모와 연구 데이터를 민감 데이터로 취급하도록 명시
- 외부 API, 클라우드, AI, 계정 기능은 사용자 승인 전까지 기본 범위에서 제외

### Documentation
- 기존 설계 묶음 `AGENTS.md`, `CHANGELOG.md`, `HISTORY.md`, `README.md`를 프로젝트 설계 묶음으로 통합

---

## v0.1.2 - 2026-05-17

### Added
- 모바일 배포 프로젝트를 위한 AAB, 스토어 업로드 산출물, mapping/symbols/dSYM 확인 규칙 추가
- GitHub Release 본문과 모바일 스토어용 릴리즈 노트를 분리해 관리하는 규칙 추가
- Android/iOS 버전명과 빌드 번호를 태그 및 문서 버전과 함께 확인하는 규칙 추가
- 외부 SDK, 광고, Play Games Services, 서명 키, 실기 검증 관련 배포 체크리스트 보강

## v0.1.1 - 2026-04-30

### Added
- `AGENTS.md` 프로젝트 설정값에 `Expected Assets` 항목 추가
- 릴리즈 시 APK, EXE 등 실제 구동 파일의 생성 및 유효성 확인 규칙 강화 (Section 23)

## v0.1.0 - 2026-04-29

### Added
- AI 코딩 에이전트를 위한 범용 작업 규칙 `AGENTS.md` 추가
- 프로젝트 개요 및 가이드를 담은 `README.md` 추가
- 이력 관리를 위한 `HISTORY.md` 및 `CHANGELOG.md` 구조화

### Documentation
- `AGENTS.md` 프로젝트 설정값 업데이트 (Repository URL 등)
