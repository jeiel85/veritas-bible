# Veritas Bible

![Veritas Bible feature graphic](docs/assets/feature-graphic.png)

Veritas Bible은 오프라인 한·영 성경 읽기와 귀납적 성경연구를 위한 Android 네이티브 앱입니다. `Veritas`는 라틴어로 “진리”를 뜻하며, 성경 본문과 개인 연구 기록을 조용하고 신뢰할 수 있는 방식으로 다루는 것을 목표로 합니다.

- 랜딩 페이지: <https://jeiel85.github.io/veritas-bible-android/>
- 개인정보 처리 방침: <https://jeiel85.github.io/veritas-bible-android/privacy.html>
- GitHub 저장소: <https://github.com/jeiel85/veritas-bible-android>

## 핵심 기능

- 오프라인 성경 본문: 한국어 1910 개역과 영어 WEB 31,105절을 앱 패키지에 번들.
- 한·영 병행 읽기: 인터넷 연결 없이 성경 리더와 검색 사용.
- 개인 노트: 구절 하이라이트와 묵상 노트를 로컬 DB에 저장.
- 암호화 백업/복원: 사용자가 만든 백업 페이로드를 비밀번호 기반으로 암호화.
- 귀납적 성경연구 워크벤치: 관찰, 본문 마킹, 구조 연결, 단락, 해석, 핵심주제 검증, 명제, 개요, 적용.
- 연구 리포트: Markdown 복사/공유, Android PDF 공유, 시스템 인쇄 지원.

## 기술 스택

- Android Native
- Kotlin
- Jetpack Compose
- Material 3
- Room / SQLite
- 브랜드 컬러: Deep Navy `#1A237E`

## 프로젝트 구조

```text
app/src/main/java/com/veritasbible/app/
  data/          Room DB, DAO, 성경 본문 prepopulate, 암호화 유틸
  repository/    성경 읽기/노트/백업 Repository
  study/         귀납적 성경연구 데이터, UI, 리포트, 백업 통합
  ui/            Compose 화면, 테마, ViewModel

docs/
  index.html     GitHub Pages 랜딩 페이지
  privacy.html   Play Store 등록용 개인정보 처리 방침
  releases/      GitHub Release 본문

play_store/
  assets/         Play Console 아이콘 및 피처 그래픽
  listing/        스토어 등록 문구
  release_notes/  Play Console 릴리즈 노트
  screenshots/    휴대전화 스크린샷
```

## 개발 실행

필수 도구:

- Android Studio
- JDK 17 이상
- Android SDK 36 이상

주요 명령:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

## Play Store 등록 자료

현재 저장소에는 Play Console 등록에 필요한 기본 자료가 포함되어 있습니다.

- 앱 아이콘: `play_store/assets/play_store_icon_512.png`
- 피처 그래픽: `play_store/assets/feature_graphic_1024x500.png`
- 개인정보 처리 방침: `docs/privacy.html`, `play_store/PRIVACY_POLICY.md`
- 한국어/영어 등록정보: `play_store/listing/ko-KR/`, `play_store/listing/en-US/`
- 휴대전화 스크린샷: `play_store/screenshots/`

## 새 버전 만들기

릴리즈는 태그 기반으로 처리합니다.

1. `app/build.gradle.kts`의 `versionName`과 태그 `vX.Y.Z`를 일치시킵니다.
2. GitHub Release 본문 `docs/releases/vX.Y.Z.md`를 작성합니다.
3. Play Console 노트 `play_store/release_notes/vX.Y.Z.txt`를 작성합니다.
4. 버전 검증을 실행합니다.

```powershell
.\scripts\validate_release_version.ps1 vX.Y.Z
```

5. Play Console 업로드 파일을 바탕화면 루트로 내보냅니다.

```powershell
.\scripts\export_play_console_assets.ps1 vX.Y.Z
```

6. 커밋 후 태그를 푸시합니다.

```powershell
git tag -a vX.Y.Z -m "vX.Y.Z"
git push origin main vX.Y.Z
```

태그가 푸시되면 GitHub Actions가 APK/AAB를 빌드하고, `docs/releases/vX.Y.Z.md`를 본문으로 GitHub Release를 생성합니다.

## 릴리즈 서명

AAB 생성에는 릴리즈 업로드 키가 필요합니다.

```powershell
$env:ANDROID_RELEASE_KEYSTORE_PATH="D:\path\to\my-upload-key.jks"
$env:ANDROID_RELEASE_KEYSTORE_PASSWORD="..."
$env:ANDROID_RELEASE_KEY_ALIAS="upload"
$env:ANDROID_RELEASE_KEY_PASSWORD="..."
.\scripts\export_play_console_assets.ps1 v1.6.0
```

로컬 `.keystore\release.env` 파일이 있으면 스크립트가 해당 값을 자동으로 읽습니다. `.keystore\`는 로컬 비밀 폴더이며 Git에 커밋하지 않습니다.
