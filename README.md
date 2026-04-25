# 📖 Veritas Bible (베리타스 성경)

<p align="center">
  <img src="https://img.shields.io/badge/Flutter-3.5.0+-02569B?style=for-the-badge&logo=flutter&logoColor=white" alt="Flutter">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Web%20%7C%20Windows-02569B?style=for-the-badge" alt="Platforms">
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/badge/Version-1.1.1-blue?style=for-the-badge" alt="Version">
</p>

### 🌐 [Live Demo (Web)](https://jeiel85.github.io/veritas-bible/)

> **Veritas**는 라틴어로 "진리"를 의미합니다. 모든이가 성경의 진리에 쉽게 접근할 수 있도록 만든 오픈소스 성경 앱입니다.

Cross-platform으로 개발된 성경 읽기 앱으로, 오프라인 지원, 검색 기능, 개인화 설정을 제공합니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 📚 **66권 전체 지원** | 구약 39권 + 신약 27권, 전 성경 |
| 🔍 **전체 검색** | 키워드로 성경 전체 텍스트 검색 |
| 🌙 **다크 모드** | 야간 읽기를 위한 다크 테마 지원 |
| 🔤 **폰트 크기 조절** | 개인에게 맞는 글꼴 크기 설정 |
| 📱 **Cross-Platform** | Android, iOS, Web, Windows 지원 |
| 💾 **오프라인 지원** | SQLite 로컬 데이터베이스 |
| 🎨 **테마 커스터마이징** | Deep Navy (#1A237E) 메인 컬러 |

---

## 🛠 기술 스택

<p align="center">
  <img src="https://skillicons.dev/icons?i=flutter&theme=light" height="50" alt="Flutter">
  <img src="https://skillicons.dev/icons?i=dart&theme=light" height="50" alt="Dart">
  <img src="https://skillicons.dev/icons?i=sqlite&theme=light" height="50" alt="SQLite">
</p>

- **Framework**: [Flutter](https://flutter.dev/) 3.5.0+
- **Language**: [Dart](https://dart.dev/)
- **State Management**: [Provider](https://pub.dev/packages/provider)
- **Local Database**: [sqflite](https://pub.dev/packages/sqflite)
- **Storage**: [shared_preferences](https://pub.dev/packages/shared_preferences)
- **Architecture**: Clean Architecture (Model → Service → Provider → Screen)

---

## 📱 지원 플랫폼

<p align="center">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/android/android-original.svg" height="40" alt="Android">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apple/apple-original.svg" height="40" alt="iOS">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/googlecloud/googlecloud-original.svg" height="40" alt="Web">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/windows8/windows8-original.svg" height="40" alt="Windows">
</p>

- ✅ Android
- ✅ iOS
- ✅ Web
- ✅ Windows

---

## 🚀 시작하기

### 필수 조건

- Flutter SDK 3.5.0 이상
- Dart SDK 3.0.0 이상

### 설치

```bash
# 레포지토리 클론
git clone https://github.com/your-username/veritas-bible.git
cd veritas-bible

# 의존성 설치
flutter pub get

# 실행
flutter run
```

### 빌드

```bash
# Android APK
flutter build apk --release

# iOS
flutter build ios --release

# Web
flutter build web
```

---

## 📂 프로젝트 구조

```
lib/
├── main.dart                 # 앱 진입점
├── models/                   # 데이터 모델
│   ├── bible.dart
│   └── bible_metadata.dart
├── providers/                # 상태 관리
│   ├── bible_provider.dart
│   └── settings_provider.dart
├── services/                 # 데이터베이스 서비스
│   └── database_helper.dart
└── screens/                  # UI 스크린
    ├── home_screen.dart
    ├── read_screen.dart
    └── search_screen.dart
```

---

## 🗺️ 로드맵

- [x] 성경 데이터 확장 (전체 66권 텍스트 기반 마련)
- [x] 병행 읽기 (Parallel Reading) 모드
- [x] 하이라이트 및 북마크 시스템
- [x] 개인 메모(Note) 기능
- [x] 오디오(TTS) 낭독 기능
- [x] 고도화된 성경 검색 및 네비게이션
- [ ] 성경 통독 계획 (Reading Plan)
- [ ] 구절 카드 이미지 제작 및 공유

---

## 🛠 개발 지침

본 프로젝트의 모든 개발 및 에이전트 행동 규칙은 **[agents.md](agents.md)** 문서에 정의되어 있습니다. 에이전트는 작업 시작 전 반드시 해당 문서를 숙지해야 합니다.

---

## 🤝 기여하기

이 프로젝트에 기여하고 싶으시다면:

1. 이 레포지토리를 Fork 합니다
2. Feature 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경 사항을 Commit 합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 Push 합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성합니다

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🙏 감사합니다

- [CrossWire Bible Society](https://www.crosswire.org/) - 성경 데이터 소스
- [Flutter Community](https://flutter.dev/) - 훌륭한 프레임워크
- 모든 기여자와 사용자분들

---

<p align="center">
  <sub>Built with ❤️ using Flutter</sub>
</p>