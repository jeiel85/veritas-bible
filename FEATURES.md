# Open Bible - Feature Specification

## 상용 성경 앱 10개 분석 결과
1. **YouVersion:** 소셜, 공유, 오디오, 묵상 계획
2. **Blue Letter Bible:** 원어(히브리어/헬라어) 분해, 주석, 검색
3. **Logos:** 방대한 신학 도서관 연동, 학술적 툴
4. **Olive Tree:** 분할 화면, 오프라인 주석, 강력한 동기화
5. **Bible Gateway:** 다양한 번역본 병행 읽기, 키워드 검색
6. **Tecarta:** 빠른 네비게이션, 깔끔한 노트 UI
7. **Accordance:** Mac/PC 환경의 강력한 원어 검색
8. **E-Sword:** 무료 PC 기반의 오프라인 모듈(사전, 주석)
9. **MySword:** 안드로이드 환경의 오프라인 모듈 확장성
10. **NeuBible:** 극강의 미니멀리즘, 타이포그래피, 제스처 기반 이동

## 핵심 구현 기능 리스트 (MVP)
### 1. 코어 엔진 (Data & State)
- 로컬 JSON/SQLite 기반 오프라인 데이터 로딩
- `Provider`를 활용한 전역 상태 관리 (현재 읽고 있는 위치 저장)

### 2. 사용자 인터페이스 (UI)
- **홈 화면 (네비게이션):** 구약/신약 목록 및 장(Chapter) 선택 그리드
- **읽기 화면 (Reading View):** 부드러운 스크롤, 구절 터치 반응(하이라이트/복사)
- **설정 메뉴:** 다크 모드 토글, 폰트 크기 조절 (`shared_preferences`로 로컬 저장)

### 3. 향후 확장 기능
- 다국어 번역본 병행 읽기 (Parallel View)
- 원어 사전 (Strong's Code) 연동
- 북마크, 노트 동기화
