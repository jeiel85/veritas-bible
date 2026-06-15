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
