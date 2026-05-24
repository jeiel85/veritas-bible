# Goal 2 — 본문 마킹·구조 분석 기능 구현

```text
작업 루트는 D:\Project\veritas-bible-android\ 입니다.

Goal 1에서 추가한 귀납적 성경연구 모듈을 기반으로, 이번에는 본문 관찰과 구조 분석 기능을 출시 가능한 수준으로 강화해 주세요.

반드시 기존 문서와 현재 구현 상태를 먼저 확인해 주세요.

- D:\Project\veritas-bible-android\AGENTS.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\FEATURE_SPEC.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\UX_ARCHITECTURE.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\DATA_MODEL.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\BIBLE_STUDY_METHOD_MAPPING.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\AI_COACH_POLICY.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\IMPLEMENTATION_ROADMAP.md

이번 Goal의 작업 범위:

1. 본문 마킹 데이터 모델 추가
- 연구 세션 안에서 본문 일부를 마킹할 수 있는 데이터 구조를 추가해 주세요.
- 최소한 다음 마킹 타입을 지원해 주세요.
  - 주어
  - 동사
  - 목적어
  - 접속사
  - 반복어
  - 핵심어
  - 원인
  - 결과
  - 대조
  - 근거
  - 명령
- 마킹은 본문 범위, 절 ID, 시작 위치, 끝 위치, 타입, 메모를 저장할 수 있어야 합니다.

2. 본문 마킹 UI 구현
- 연구 상세 화면에서 본문 텍스트를 선택하고 마킹 타입을 지정할 수 있게 해 주세요.
- 이미 지정한 마킹은 시각적으로 구분되어야 합니다.
- 마킹은 수정/삭제 가능해야 합니다.
- 화면이 복잡해지지 않도록 필수 마킹 타입을 우선 노출하고, 고급 타입은 보조 메뉴로 분리해 주세요.

3. 구조 연결 기능 추가
- 주어-동사 연결 데이터를 저장할 수 있게 해 주세요.
- 원인-결과, 대조, 근거-주장 같은 관계 연결도 확장 가능하게 설계해 주세요.
- 이번 Goal에서는 최소한 주어-동사 연결을 실제로 생성/조회/삭제할 수 있어야 합니다.
- 연결된 주어와 동사만 모아서 보는 “구조 요약” 또는 “주어-동사 흐름 보기”를 추가해 주세요.

4. 관찰 단계 강화
- 관찰 노트에 아래 질문 템플릿을 추가해 주세요.
  - 반복되는 단어는 무엇인가?
  - 주된 인물 또는 주체는 누구인가?
  - 주요 동사는 무엇인가?
  - 명령인가, 설명인가, 사건 서술인가?
  - 접속사는 흐름을 어떻게 바꾸는가?
  - 원인과 결과가 있는가?
  - 대조되는 내용이 있는가?
  - 본문이 직접 언급하는 결론이 있는가?
- 사용자가 답변한 내용은 연구 세션에 저장되어야 합니다.

5. 성격 규정 1차 추가
- 본문 또는 단락에 성격 태그를 붙일 수 있게 해 주세요.
- 최소한 다음 태그를 지원해 주세요.
  - 정의
  - 의미
  - 이유
  - 원인
  - 결과
  - 목적
  - 조건
  - 과정
  - 방식
  - 특징
  - 근거
  - 관계
  - 대조
  - 적용
- 성격 태그는 단락 구분과 연결될 수 있어야 합니다.

6. 품질 기준
- 기존 성경 읽기, 메모, 읽기 계획, 읽기 진행률 기능은 회귀되지 않아야 합니다.
- 연구 세션의 기존 관찰/단락/핵심주제/적용 데이터는 유지되어야 합니다.
- 외부 AI API나 외부 서비스는 추가하지 마세요.
- 본문 선택/마킹 UI가 모바일 화면에서 사용 가능해야 합니다.
- 저장 실패, 삭제 확인, 빈 마킹 상태를 처리해 주세요.

7. 문서 갱신
- DATA_MODEL 또는 관련 docs에 추가된 마킹/연결/성격 태그 구조를 기록해 주세요.
- CHANGELOG.md에 변경 사항을 기록해 주세요.
- HISTORY.md에 작업 내용, 변경 파일, 검증 결과, 후속 작업을 기록해 주세요.
- DECISION_LOG가 있다면 본문 마킹 저장 방식과 연결 구조 결정 이유를 기록해 주세요.

8. 검증
- 가능한 범위에서 lint, test, build를 실행해 주세요.
- 연구 세션 생성 후 본문 마킹 추가/수정/삭제가 되는지 확인해 주세요.
- 주어-동사 연결 생성/삭제가 되는지 확인해 주세요.
- 구조 요약 화면 또는 영역이 정상 표시되는지 확인해 주세요.
- 실행하지 못한 검증은 생략 사유를 기록해 주세요.

AGENTS.md 규칙을 따르고, 파괴적 명령, 강제 푸시, 기존 데이터 삭제, 외부 서비스 추가, 시크릿 변경은 하지 마세요. 완료 후 작업 요약, 변경 파일, 검증 결과, 생략한 검증, 후속 작업을 한국어로 보고해 주세요.
```
