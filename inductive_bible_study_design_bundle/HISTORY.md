# HISTORY.md

## 2026-05-24
- 작업: 기존 `D:\Project\veritas-bible-android\` 앱에 귀납적 성경연구 기능을 통합하기 위한 실제 경로 기반 Goal 프롬프트와 통합 계획 추가
- 변경 파일:
  - AGENTS.md: 기존 앱 루트와 설계 문서 경로, 기존 앱 통합 전제 추가
  - README.md: Veritas Bible Android 통합 목적과 Goal 사용 순서 추가
  - CHANGELOG.md: v0.3.0 변경 사항 추가
  - HISTORY.md: 작업 이력 기록
  - docs/VERITAS_INTEGRATION_PLAN.md: 기존 앱 보존 원칙, 새 연구 도메인, Goal 분할 이유, 금지 작업 작성
  - docs/GOAL_PROMPTS.md: 실제 경로 기반 3개 Goal 프롬프트로 확장
  - docs/GOAL_01_BASE_INTEGRATION.md: 연구 모듈 기반 통합 프롬프트 추가
  - docs/GOAL_02_MARKUP_STRUCTURE.md: 본문 마킹/구조 분석 프롬프트 추가
  - docs/GOAL_03_INTERPRETATION_EXPORT.md: 해석/명제/개요/내보내기 프롬프트 추가
  - docs/IMPLEMENTATION_ROADMAP.md: 실제 적용 경로와 3단계 진행 방식 추가
  - docs/DECISION_LOG.md: 기존 앱 통합 방식과 메모/연구 노트 분리 결정 기록
- 검증:
  - 문서 파일 생성 확인
  - ZIP 묶음 생성 확인
- 결과: Veritas Bible Android 기존 앱 통합용 설계 묶음 v0.3.0 생성 완료
- 후속 작업:
  - 실제 레포에서 Goal 1부터 순차 실행
  - Goal 1 완료 후 기존 기능 회귀 여부 확인

---

## 2026-05-24
- 작업: 귀납적 성경연구 특화 성경앱 설계를 MVP가 아닌 출시 가능한 앱 수준으로 재정의하고 기존 설계 묶음과 통합
- 변경 파일:
  - AGENTS.md: 기존 범용 에이전트 규칙을 유지하면서 프로젝트 고유 설정, 연구 도메인 규칙, AI 코치 제한, 데이터 보존, 출시 품질 기준 추가
  - README.md: 제품 정체성, 문제 정의, 핵심 연구 흐름, 주요 기능, 문서 구조 작성
  - CHANGELOG.md: v0.2.0 설계 변경 사항 추가
  - HISTORY.md: 통합 작업 이력 기록
  - docs/PRODUCT_REQUIREMENTS.md: 출시 수준 제품 요구사항 작성
  - docs/FEATURE_SPEC.md: 기능 명세 작성
  - docs/UX_ARCHITECTURE.md: 연구 워크벤치 UX 구조 작성
  - docs/DATA_MODEL.md: 연구 세션, 마킹, 연결, 단락, 해석, 핵심주제, 명제, 적용 데이터 모델 작성
  - docs/BIBLE_STUDY_METHOD_MAPPING.md: 교재의 귀납적 연구 방식과 앱 기능 매핑
  - docs/AI_COACH_POLICY.md: AI 기능의 역할과 제한 정책 작성
  - docs/IMPLEMENTATION_ROADMAP.md: 단계별 구현 로드맵 작성
  - docs/QUALITY_RELEASE_CHECKLIST.md: 출시 전 품질 체크리스트 작성
  - docs/GOAL_PROMPTS.md: 에이전트 코딩용 Goal 프롬프트 작성
  - docs/DECISION_LOG.md: 핵심 의사결정 기록
- 검증:
  - 문서 파일 생성 확인
  - ZIP 묶음 생성 확인
- 결과: 설계 문서 묶음 생성 완료
- 후속 작업:
  - 실제 기존 성경앱 레포에서 기술스택, 패키지명, DB 구조, 성경 본문 라이선스를 확인한 뒤 `TBD` 항목 치환
  - 첫 구현 목표는 연구 세션 기반 구축부터 진행

---

## 2026-05-17
- 작업: `nightseed-survivor` 프로젝트의 모바일 배포 및 릴리즈 운영 규칙을 범용 규칙으로 반영
- 변경 파일:
  - AGENTS.md: AAB, 스토어 릴리즈 노트, 모바일 버전 동기화, 서명/외부 SDK/실기 검증 체크리스트 추가
  - CHANGELOG.md: v0.1.2 변경 사항 추가
  - HISTORY.md: 작업 이력 기록
- 검증: `git diff`로 변경 내용 확인
- 결과: 성공
- 후속 작업: 다른 모바일 프로젝트에 적용 시 프로젝트별 `Expected Assets`, 버전 파일, 스토어 노트 경로를 설정값에 맞게 조정

## 2026-04-30
- 작업: 릴리즈 산출물 검증 규칙 강화 및 템플릿 업데이트
- 변경 파일:
  - AGENTS.md: 'Expected Assets' 설정 항목 추가 및 릴리즈 검증 체크리스트 구체화
- 검증: 파일 수정 확인
- 결과: 성공
- 후속 작업: 신규 규칙 적용 확인

## 2026-04-29
- 작업: 프로젝트 초기화 및 기본 문서 작성
- 변경 파일:
  - AGENTS.md: 범용 에이전트 규칙 작성 및 프로젝트 설정 업데이트
  - README.md: 프로젝트 소개 및 사용법 작성
  - HISTORY.md: 이력 관리 문서 생성
  - CHANGELOG.md: 변경 로그 문서 생성
- 검증: 로컬 파일 생성 확인
- 결과: 성공
- 후속 작업: GitHub 저장소 푸시 및 에이전트 연동 테스트
