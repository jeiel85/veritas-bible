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
5. 커밋 후 태그 푸시: `git tag -a vX.Y.Z -m "vX.Y.Z"` 후 `git push origin main vX.Y.Z`

태그가 푸시되면 GitHub Actions가 APK/AAB를 빌드하고, `docs/releases/vX.Y.Z.md`를 본문으로 GitHub Release를 생성합니다.
