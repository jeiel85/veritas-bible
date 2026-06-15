# Goal Prompts

이 문서는 `D:\Project\veritas-bible-android\` 기존 앱에 귀납적 성경연구 기능을 통합하기 위한 에이전트 코딩용 `/goal` 프롬프트입니다.

요청 순서는 반드시 다음을 권장합니다.

1. Goal 1 — 귀납적 연구 모듈의 출시급 기반 통합
2. Goal 2 — 본문 마킹·구조 분석 기능 구현
3. Goal 3 — 해석·명제·개요·내보내기 완성

각 Goal은 복사해서 그대로 사용할 수 있게 작성했습니다.

---

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

---

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

---

# Goal 3 — 해석·명제·개요·내보내기 완성

```text
작업 루트는 D:\Project\veritas-bible-android\ 입니다.

Goal 1과 Goal 2에서 추가한 귀납적 성경연구 모듈을 기반으로, 이번에는 연구 결과를 실제로 완성하고 공유/보관할 수 있는 출시급 흐름을 구현해 주세요.

반드시 현재 코드와 아래 문서를 먼저 확인해 주세요.

- D:\Project\veritas-bible-android\AGENTS.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\PRODUCT_REQUIREMENTS.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\FEATURE_SPEC.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\UX_ARCHITECTURE.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\DATA_MODEL.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\BIBLE_STUDY_METHOD_MAPPING.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\QUALITY_RELEASE_CHECKLIST.md
- D:\Project\veritas-bible-android\docs\inductive_bible_study_design_bundle\DECISION_LOG.md

이번 Goal의 작업 범위:

1. 해석 노트 구현
- 연구 상세 화면에 해석 단계를 실제 사용 가능하게 구현해 주세요.
- 핵심 단어, 본문 의미, 문맥 의미, 본문 근거, 관련 구절, 해석 결론을 기록할 수 있게 해 주세요.
- 해석 노트는 연구 세션과 연결되어 저장/수정/삭제 가능해야 합니다.

2. 핵심주제 검증 기능 구현
- 핵심주제를 작성한 뒤 아래 기준으로 점검할 수 있는 체크리스트를 추가해 주세요.
  - 본문 전체를 포괄하는가?
  - 각 단락이 이 주제를 뒷받침하는가?
  - 본문 단어와 문장에 근거하는가?
  - 적용이 해석보다 먼저 나오지 않았는가?
  - 내 선입견을 본문에 넣은 것은 아닌가?
- 체크 결과와 수정 메모를 저장할 수 있게 해 주세요.

3. 명제화 기능 구현
- 핵심주제를 명제 문장으로 정리하는 화면을 추가해 주세요.
- 명제 문장, 본문 근거, 검토 상태를 저장할 수 있게 해 주세요.
- 명제는 연구 리포트에 포함되어야 합니다.

4. 개요화 기능 구현
- 연구 결과를 개요 형태로 정리할 수 있게 해 주세요.
- 개요 노드는 제목, 본문 범위, 요약, 하위 노드를 가질 수 있어야 합니다.
- 단락 구분 데이터와 개요 노드를 연결할 수 있으면 연결해 주세요.
- 모바일에서 너무 복잡하지 않도록 기본 편집 흐름을 우선 구현해 주세요.

5. 적용 단계 고도화
- 적용 노트를 다음 구조로 정리해 주세요.
  - 본문을 통해 확인한 진리
  - 나를 비추는 거울화
  - 조정해야 할 부분
  - 구체적 실천 계획
  - 점검일
  - 실천 여부
- 기존 적용 노트 데이터가 있다면 데이터 손실 없이 마이그레이션해 주세요.

6. 연구 리포트 내보내기
- 연구 세션 결과를 Markdown으로 내보낼 수 있게 해 주세요.
- 리포트에는 최소한 다음 항목이 포함되어야 합니다.
  - 제목
  - 본문 범위
  - 관찰 요약
  - 본문 마킹 요약
  - 단락 구분
  - 성격 태그
  - 해석 노트
  - 핵심주제
  - 명제
  - 개요
  - 적용
  - 생성일
- 가능하다면 PDF 내보내기 구조도 준비하되, 무리한 라이브러리 추가가 필요하면 Markdown 내보내기를 우선 완료해 주세요.

7. 출시 품질 점검
- QUALITY_RELEASE_CHECKLIST 기준으로 현재 구현 상태를 점검해 주세요.
- 접근성, 빈 상태, 오류 상태, 저장 실패, 삭제 확인, 데이터 마이그레이션, 기존 기능 회귀 가능성을 확인해 주세요.
- 기존 성경 읽기, 메모, 읽기 계획, 읽기 진행률 기능이 정상 동작해야 합니다.

8. 문서 갱신
- README 또는 관련 docs에 연구 완료 흐름과 내보내기 기능을 기록해 주세요.
- CHANGELOG.md에 사용자에게 보이는 변경 사항을 기록해 주세요.
- HISTORY.md에 작업 내용, 변경 파일, 검증 결과, 후속 작업을 기록해 주세요.
- DECISION_LOG가 있다면 Markdown 우선 내보내기, PDF 후순위 처리 여부, 적용 노트 구조화 결정을 기록해 주세요.

9. 검증
- 가능한 범위에서 lint, test, build를 실행해 주세요.
- 연구 세션 생성부터 관찰, 마킹, 단락, 해석, 핵심주제, 명제, 개요, 적용, Markdown 내보내기까지 핵심 흐름을 확인해 주세요.
- 실행하지 못한 검증은 성공으로 기록하지 말고 생략 사유를 남겨 주세요.

AGENTS.md 규칙을 따르고, 파괴적 명령, 강제 푸시, 기존 데이터 삭제, 외부 서비스 추가, 시크릿 변경은 하지 마세요. 완료 후 작업 요약, 변경 파일, 검증 결과, 생략한 검증, 후속 작업을 한국어로 보고해 주세요.
```
