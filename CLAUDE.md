# Veritas Bible - Claude Guidelines

## 버전 관리 규칙

### Android versionCode (pubspec.yaml `+숫자`) 는 항상 증가해야 한다

`pubspec.yaml`의 버전 형식: `major.minor.patch+buildNumber`

- `+buildNumber`(versionCode)는 **릴리즈마다 반드시 이전보다 높아야** 한다
- Android는 versionCode가 낮거나 같은 APK 설치를 거부한다 ("이전버전을 설치할 수 없습니다" 오류)
- major/minor 버전을 올리더라도 buildNumber는 전체 릴리즈 이력에서 단조 증가해야 한다

**올바른 예시:**
```
v1.2.0+4  →  v1.3.0+5  →  v1.4.0+6
```

**잘못된 예시 (설치 오류 발생):**
```
v1.2.0+4  →  v1.3.0+1  ← +1 < +4 이므로 설치 불가
```

버전을 올릴 때는 `git log`나 이전 태그로 마지막 buildNumber를 확인하고 +1 이상 높게 설정한다.
