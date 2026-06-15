# Feature Specification

## 1. 기능 목록

| ID | 기능 | 출시 필수 | 설명 |
|---|---|---:|---|
| BIBLE-001 | 성경 탐색 | Yes | 책/장/절 기준 본문 탐색 |
| BIBLE-002 | 본문 검색 | Yes | 단어/구절 검색 |
| BIBLE-003 | 연구 세션 전환 | Yes | 본문 선택 후 연구 시작 |
| STUDY-001 | 연구 세션 생성 | Yes | 본문 범위와 연구 유형 지정 |
| STUDY-002 | 연구 단계 관리 | Yes | 준비~적용 단계 진행 |
| STUDY-003 | 자동 저장 | Yes | 입력 즉시 또는 주기적 저장 |
| MARK-001 | 텍스트 마킹 | Yes | 주어, 동사, 접속사 등 태그 |
| MARK-002 | 연결선 | Yes | 주어-동사, 원인-결과 등 연결 |
| MARK-003 | 마킹 필터 | Yes | 태그별 표시/숨김 |
| OBS-001 | 관찰 질문 | Yes | 단계별 질문 템플릿 |
| STRUCT-001 | 단락 나누기 | Yes | 본문 범위 안에서 단락 생성 |
| STRUCT-002 | 단락 제목 | Yes | 단락별 제목과 요약 |
| CHAR-001 | 성격 규정 | Yes | 정의/이유/결과 등 태그 |
| INTERP-001 | 해석 노트 | Yes | 단어/문장/단락 해석 |
| THEME-001 | 핵심주제 후보 | Yes | 여러 후보 작성 |
| THEME-002 | 핵심주제 검증 | Yes | 본문/단락 근거 점검 |
| PROP-001 | 명제 작성 | Yes | 핵심주제 명제화 |
| OUTLINE-001 | 개요 작성 | Yes | 대지/소지 구조화 |
| APP-001 | 적용 작성 | Yes | 거울화/조정화/실천 |
| EXPORT-001 | Markdown 출력 | Yes | 연구 리포트 출력 |
| EXPORT-002 | PDF 출력 | Yes | 인쇄/공유용 출력 |
| BACKUP-001 | JSON/ZIP 백업 | Yes | 데이터 백업 |
| BACKUP-002 | 가져오기 | Yes | 백업 복원 |
| GROUP-001 | 피드백 코멘트 | Phase 2 | 리더/그룹 피드백 |
| AI-001 | AI 코치 | Phase 3 | 정답 제시가 아닌 점검 질문 |

---

## 2. 연구 세션 상태

```text
Draft
InProgress
Review
Completed
Archived
```

### 상태 의미

- `Draft`: 생성했지만 연구 입력 전
- `InProgress`: 연구 진행 중
- `Review`: 리더/자기 검토 단계
- `Completed`: 연구 완료
- `Archived`: 보관

---

## 3. 연구 단계

```text
Preparation
Observation
Structure
Characterization
Division
Interpretation
Theme
Proposition
Outline
Application
Report
```

각 단계는 별도 데이터 저장 단위를 가진다.

---

## 4. 마킹 타입

```text
subject
verb
object
connective
repeated_word
keyword
command
request
statement
narrative
cause
result
contrast
purpose
condition
basis
definition
application_clue
```

---

## 5. 연결 타입

```text
subject_verb
verb_object
cause_result
contrast
basis_claim
explanation
theme_support
parallel
application_basis
```

---

## 6. 성격 태그

```text
definition
meaning
reason
cause
result
phenomenon
stage
domain
aspect
basis
feature
attribute
proposal
purpose
goal
identity
root
method
condition
process
relation
contrast
action
perception
application
```

---

## 7. 연구 리포트 구성

연구 세션 리포트는 다음 순서를 따른다.

1. 제목
2. 본문 범위
3. 연구 날짜
4. 연구 유형
5. 준비/배경
6. 관찰 요약
7. 문법/구조 요약
8. 단락 구분
9. 성격 규정
10. 해석 요약
11. 핵심주제
12. 명제
13. 개요
14. 적용
15. 관주/참조
16. 피드백
17. 수정 이력

---

## 8. 오류/빈 상태

모든 주요 화면은 다음 상태를 가져야 한다.

- loading
- empty
- content
- error
- offline
- permission required
- migration required
- import conflict

