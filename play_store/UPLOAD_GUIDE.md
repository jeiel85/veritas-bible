# Veritas Bible — Play Console 신규 앱 등록 가이드

준비된 자산과 입력값을 한 번에 모아둔 체크리스트입니다.
계정: `pedaiah85@gmail.com`
Play Console: <https://play.google.com/console/>

---

## 0. 사전 자료 파일 위치

| 항목 | 경로 |
|---|---|
| 서명된 AAB | `~/Desktop/veritas-bible-v1.0.0.aab` |
| 릴리즈 노트 (한·영) | `~/Desktop/veritas-bible-v1.0.0-release-notes.txt` |
| 앱 아이콘 512×512 | `play_store/assets/play_store_icon_512.png` |
| 피처 그래픽 1024×500 | `play_store/assets/feature_graphic_1024x500.png` |
| 휴대전화 스크린샷 8장 | `play_store/screenshots/01..08_*.png` (1080×2340) |
| 개인정보 처리방침 URL | <https://jeiel85.github.io/veritas-bible/privacy.html> |
| 한국어 등록정보 | `play_store/listing/ko-KR/` |
| 영어 등록정보 | `play_store/listing/en-US/` |

---

## 1. 앱 만들기

`Play Console → "앱 만들기"` 클릭 후 다음 값을 입력합니다.

| 필드 | 값 |
|---|---|
| 앱 이름 | `Veritas Bible — 오프라인 한영 성경` |
| 기본 언어 | `한국어 - 한국 (ko-KR)` |
| 앱 또는 게임 | `앱` |
| 무료 또는 유료 | `무료` |
| 동의 사항 | 모두 동의 |

---

## 2. 출시 트랙 (내부 테스트 → 프로덕션)

처음에는 **내부 테스트**로 올려 검증한 뒤 프로덕션으로 승격하는 흐름을 권장합니다.

`출시 → 테스트 → 내부 테스트 → 새 버전 만들기`

1. 앱 번들 업로드: `veritas-bible-v1.0.0.aab`
2. 출시명: `1.0.0 (1)`
3. 출시 노트: 바탕화면 `veritas-bible-v1.0.0-release-notes.txt` 파일 내용 그대로 복사·붙여넣기

---

## 3. 앱 콘텐츠 설문 (모두 필수)

좌측 메뉴 `앱 콘텐츠`에서 다음 항목을 모두 작성해야 출시가 가능합니다.

### 3-1. 개인정보처리방침
- URL: `https://jeiel85.github.io/veritas-bible/privacy.html`

### 3-2. 광고
- `이 앱에 광고가 포함되어 있나요?` → **아니요**

### 3-3. 앱 액세스 권한
- `모든 기능이 제한 없이 사용 가능` 선택

### 3-4. 콘텐츠 등급
- 설문 시작 → 카테고리 `참고/뉴스/교육`
- 모든 질문 `아니요` 응답 (앱에 폭력·성·도박·약물 콘텐츠 없음)
- 결과: **전체 이용가** 등급 발급

### 3-5. 타겟 사용자층 및 콘텐츠
- 대상 연령: `13세 이상` 권장 (성경 본문 일부에 종교적·역사적 폭력 묘사 포함)
- 어린이 대상 앱 여부: **아니요**

### 3-6. 뉴스 앱
- **아니요**

### 3-7. COVID-19 추적 앱
- **아니요**

### 3-8. 데이터 보안
- **데이터 수집·공유 없음** 으로 설정
- 데이터를 전송하지 않음 ✔
- 다음 모든 카테고리에 대해 "수집 또는 공유 안 함":
  - 개인정보, 금융 정보, 위치, 메시지, 사진/동영상, 오디오, 파일, 캘린더, 연락처, 앱 활동, 웹 검색 기록, 앱 정보 및 성능, 기기 또는 기타 ID
- 데이터 보안 방식:
  - `전송 중 데이터가 암호화됩니다` → 해당 없음 (외부 통신 없음)
  - `사용자가 데이터 삭제를 요청할 수 있습니다` → 해당 없음 (앱 삭제 = 데이터 삭제)
- 독립적 보안 검토 받음: **아니요**

### 3-9. 정부 앱
- **아니요**

### 3-10. 금융 기능
- **이 앱은 금융 기능을 제공하지 않습니다**

### 3-11. 상거래법 (인앱 결제)
- **이 앱에는 인앱 상품이 없습니다**

---

## 4. 스토어 등록정보 (한국어)

`스토어 등록정보 → 기본 스토어 등록정보 (한국어)`

| 필드 | 값 | 출처 |
|---|---|---|
| 앱 이름 | `Veritas Bible — 오프라인 한영 성경` | `play_store/listing/ko-KR/title.txt` |
| 간단한 설명 | `오프라인 한·영 성경. 묵상 노트와 하이라이트는 기기에만 저장.` | `short_description.txt` |
| 자세한 설명 | (전체 본문) | `full_description.txt` |
| 앱 아이콘 (512×512) | `play_store/assets/play_store_icon_512.png` | |
| 그래픽 이미지 (1024×500) | `play_store/assets/feature_graphic_1024x500.png` | |
| 휴대전화 스크린샷 | `play_store/screenshots/01_onboarding_welcome.png` ~ `08_settings.png` (8장) | |
| 앱 카테고리 | `도서/참고자료` | |
| 태그 | `성경`, `오프라인`, `묵상`, `한영` | |
| 이메일 | `pedaiah85@gmail.com` | |
| 웹사이트 (선택) | `https://github.com/jeiel85/veritas-bible` | |

### 4-1. 영어 등록정보 추가
`현지화 추가 → English (United States)` 후 `play_store/listing/en-US/` 파일 내용으로 채움.

---

## 5. 가격 및 배포

- 가격: `무료`
- 국가/지역: `모든 국가` 또는 `한국 + 미국`만 선택
- 콘텐츠 가이드라인 동의

---

## 6. 출시 검토 제출

내부 테스트 트랙에서 `검토를 위해 출시 시작` 클릭. 검토는 보통 1~3일 소요.

승인되면 `프로덕션으로 출시` 버튼으로 승격 가능.

---

## 7. 핵심 출시 후 작업

- 키스토어 백업: `.keystore/veritas-bible-upload.jks`, `release.env`, `release-keystore-info.txt`
  세 파일을 안전한 곳(USB, 1Password, 가족용 클라우드 등)에 복사. **이 파일들을 잃어버리면 영원히 같은 ID로 업데이트할 수 없습니다.**
- Play App Signing이 활성화되어 있는지 확인 — `설정 → 앱 무결성` (대부분 자동 활성)
