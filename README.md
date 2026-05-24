# Veritas Bible

세련된 디자인과 가독성 중심의 오프라인 퍼스트 성경 연구 Android 앱입니다.

## 개발 실행

필수 도구:

- Android Studio
- JDK 17 이상
- Android SDK 36 이상

실행:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

Gemini API 기능을 사용할 경우 프로젝트 루트에 `.env`를 만들고 `.env.example` 형식에 맞춰 `GEMINI_API_KEY`를 설정합니다.

## 새 버전 만들기

릴리즈는 Nightseed Survivor와 동일하게 태그 기반으로 처리합니다.

1. 앱 버전 확인: [app/build.gradle.kts](app/build.gradle.kts)의 `versionName`과 태그 `vX.Y.Z`가 일치해야 합니다.
2. GitHub Release 본문 작성: `docs/releases/vX.Y.Z.md`
3. Play Console 노트 작성: `play_store/release_notes/vX.Y.Z.txt`
4. 검증: `.\scripts\validate_release_version.ps1 vX.Y.Z`
5. Play Console 업로드 파일을 바탕화면으로 내보내기: `.\scripts\export_play_console_assets.ps1 vX.Y.Z`
6. 커밋 후 태그 푸시: `git tag -a vX.Y.Z -m "vX.Y.Z"` 후 `git push origin main vX.Y.Z`

태그가 푸시되면 GitHub Actions가 APK/AAB를 빌드하고, `docs/releases/vX.Y.Z.md`를 본문으로 GitHub Release를 생성합니다.

바탕화면 내보내기는 바탕화면 루트에 다음 파일을 생성합니다.

- `veritas-bible-vX.Y.Z.aab`
- `veritas-bible-vX.Y.Z-release-notes.txt`

AAB 생성에는 릴리즈 업로드 키가 필요합니다.

```powershell
$env:ANDROID_RELEASE_KEYSTORE_PATH="D:\path\to\my-upload-key.jks"
$env:ANDROID_RELEASE_KEYSTORE_PASSWORD="..."
$env:ANDROID_RELEASE_KEY_ALIAS="upload"
$env:ANDROID_RELEASE_KEY_PASSWORD="..."
.\scripts\export_play_console_assets.ps1 v1.0.0
```

로컬 `.keystore\release.env` 파일이 있으면 스크립트가 해당 값을 자동으로 읽습니다. `.keystore\`는 백업용 로컬 비밀 폴더이며 Git에 커밋하지 않습니다.

## 귀납적 성경연구 모듈

기존 성경 읽기/메모/읽기 계획/읽기 진행률 기능은 그대로 유지하고, `docs/inductive_bible_study_design_bundle` 설계 문서에 따라 별도의 “성경연구” 모듈이 추가되어 있습니다.

- 진입점: 하단 네비게이션의 `성경연구` 탭, 또는 성경 리더에서 절을 선택한 후 ‘이 본문으로 귀납적 연구 시작’ 버튼.
- 화면: 연구 세션 목록(`StudyListScreen`) → 연구 상세(`StudyDetailScreen`, 본문 패널 + 관찰/단락/핵심주제/적용 입력 패널).
- 데이터: Room `study_sessions / study_observations / study_divisions / study_applications` (Goal 1, DB v1→v2)와 `study_markups / study_markup_links / study_character_tags` (Goal 2, DB v2→v3). 모든 마이그레이션은 기존 사용자 데이터를 보존합니다.
- 코드 위치: `app/src/main/java/com/veritasbible/app/study/`
  - `data/` Room 엔티티·DAO
  - `repository/StudyRepository.kt`
  - `ui/` Compose 화면 및 ViewModel
- 다국어: `LanguageManager` 한·영 키 (`study_*`, `stage_*`, `common_*`).
- Goal 2에서 본문 마킹(18종), 주어-동사 등 구조 연결(9종), 관찰 질문 템플릿(8종), 단락 성격 태그(14종)가 추가되었습니다.
- Goal 3에서 해석 노트, 핵심주제 검증 체크리스트(5종), 명제(검토 상태 3종), 개요(부모-자식 트리 + 단락 링크), 적용 노트의 점검일·실천 여부, **Markdown 리포트 생성·복사·공유**가 추가되었습니다.
- Goal 4 후속 통합: **PDF 리포트 공유**(Android `PdfDocument`), **암호화 백업/복원에 연구 데이터 전체 포함**, **해석·명제의 명시적 마킹/단락 링크**, **점검일 Material 3 DatePicker**, **v1→v5 마이그레이션 자동 테스트**.
- 리포트 내보내기: 연구 상세 → ‘리포트’ 탭 → ‘Markdown 생성’ → 미리보기 확인 → ‘복사’ / ‘공유’ / ‘PDF 로 공유’. 한국어/영어 라벨이 사용자 언어 설정을 따릅니다.
- 백업: 설정 화면의 ‘백업 데이터 텍스트 복사’ / ‘복원’ 흐름이 연구 세션·관찰·마킹·연결·단락·해석·검증·명제·개요·적용·성격 태그를 모두 포함합니다.
