# GitHub Release 본문

릴리즈 본문은 태그를 만들기 전에 손으로 작성합니다.

## 흐름

1. 코드 작업 완료
2. `docs/releases/vX.Y.Z.md` 작성
3. `play_store/release_notes/vX.Y.Z.txt` 작성
4. `scripts/validate_release_version.ps1 vX.Y.Z` 실행
5. `git tag -a vX.Y.Z -m "vX.Y.Z"` 후 태그 푸시

태그가 푸시되면 CI가 이 파일을 GitHub Release 본문으로 사용합니다. 파일이 없으면 GitHub 자동 changelog로 대체되지만, 정상 릴리즈에서는 사용하지 않습니다.

## 파일명

- 태그와 정확히 일치해야 합니다.
- 예: `v1.0.0.md`

## 템플릿

```markdown
## vX.Y.Z — 한 줄 부제

### 핵심 변경

2~3줄 요약

### 변경 사항

- 변경 1
- 변경 2

### 검증

- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat testDebugUnitTest`

### 다운로드

- Play Store 업로드용: `veritas-bible-release.aab`
- Android APK: `veritas-bible-release.apk`
```
