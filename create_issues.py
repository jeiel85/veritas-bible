import os
import subprocess
import json

issues = [
    {"title": "[Onboarding] 사용자 성향 맞춤 온보딩 설문 도입", "body": "앱 최초 실행 시 연령, 독서 습관, 선호 번역본 등을 묻는 짧은 설문을 통해 개인화된 경험을 제공합니다.", "labels": "enhancement,UX"},
    {"title": "[Onboarding] 튜토리얼 스킵 및 진행 상황을 보여주는 프로그레스 바 추가", "body": "온보딩 과정에서 사용자가 언제든 스킵할 수 있게 하고, 남은 단계를 프로그레스 바로 명확히 보여주어 이탈률을 줄입니다.", "labels": "enhancement,UX"},
    {"title": "[Onboarding] 게스트 모드에서 회원가입으로의 자연스러운 전환 유도", "body": "로그인 없이 기능을 사용하다가 핵심 기능(백업 등) 사용 시 자연스럽게 혜택을 강조하며 회원가입을 유도하는 가치 제안 화면을 구성합니다.", "labels": "enhancement,UX"},
    {"title": "[Onboarding] 앱 실행 시 첫 화면 로딩 속도 1초 미만으로 최적화", "body": "초기화 로직을 지연 로딩(Lazy Loading)으로 변경하여 스플래시 화면에서 홈 화면으로 진입하는 시간을 1초 이내로 단축합니다.", "labels": "performance,UX"},
    {"title": "[First Experience] '오늘의 말씀'을 메인 홈 화면 위젯으로 전면 배치", "body": "앱 설치 직후 가장 많이 찾는 '오늘의 말씀'을 팝업이 아닌 홈 화면 최상단 위젯으로 배치하여 직관성을 높입니다.", "labels": "enhancement,UI"},
    {"title": "[Onboarding] 주요 기능에 대한 컨텍스트 기반 말풍선 도움말 제공", "body": "병행 읽기, 검색, 하이라이트 등 주요 기능에 처음 접근할 때만 나타나는 인터랙티브 말풍선(Tooltip) 도움말을 추가합니다.", "labels": "enhancement,UX"},
    {"title": "[First Experience] 소셜 로그인(구글, 카카오, 애플) 원클릭 연동 부각", "body": "복잡한 이메일 가입 대신 소셜 로그인을 전면으로 내세워 1초 만에 가입할 수 있도록 UI를 개편합니다.", "labels": "enhancement,UI"},
    {"title": "[First Experience] 기기 시스템 설정에 따른 자동 테마 전환 활성화", "body": "앱을 처음 켤 때부터 기기의 다크/라이트 모드 설정을 따라가도록 하여 눈의 피로를 덜어줍니다.", "labels": "enhancement,UX"},
    {"title": "[Onboarding] 혜택 기반의 알림 권한 요청 프로세스 도입", "body": "시스템 팝업을 띄우기 전에, '아침 묵상 알림을 받아보시겠어요?'와 같이 사용자가 얻을 혜택을 설명하는 커스텀 팝업을 먼저 노출합니다.", "labels": "enhancement,UX"},
    {"title": "[Onboarding] 앱 아이콘 및 스플래시 마이크로 인터랙션 개선", "body": "로고가 나타날 때 부드러운 애니메이션(마이크로 인터랙션)을 주어 앱의 첫인상과 퀄리티를 높입니다.", "labels": "enhancement,UI"},
    
    {"title": "[UI/UX] 하단 네비게이션 바 및 주요 액션의 햅틱 피드백 미세 조정", "body": "버튼을 탭하거나 탭을 전환할 때 기분 좋은 미세 진동(Haptic Feedback)을 제공하여 조작감을 향상시킵니다.", "labels": "enhancement,UX"},
    {"title": "[UI/UX] 글꼴 크기 변경 시 실시간 미리보기 플로팅 슬라이더 적용", "body": "설정 창으로 이동하지 않고도, 읽기 화면 내에서 플로팅 슬라이더를 통해 즉각적으로 글꼴 크기를 조절하고 확인할 수 있게 합니다.", "labels": "enhancement,UI"},
    {"title": "[UI/UX] 스크롤 시 상단 앱바 자동 숨김 처리로 몰입감 개선", "body": "본문을 읽으며 아래로 스크롤할 때 상단 바와 하단 바를 부드럽게 숨겨 화면을 넓게 쓰도록 합니다.", "labels": "enhancement,UI"},
    {"title": "[Design] 핵심 버튼(CTA) 클릭 영역을 최소 48x48dp로 확대", "body": "모바일 접근성 가이드라인에 맞춰 모든 터치 가능한 버튼의 클릭 영역을 넉넉하게 확보합니다.", "labels": "enhancement,UX"},
    {"title": "[UI/UX] 구절 롱프레스 시 나타나는 컨텍스트 메뉴 애니메이션 향상", "body": "구절을 길게 눌렀을 때 뜨는 옵션(복사, 하이라이트) 메뉴가 나타나고 사라지는 애니메이션을 부드럽게 다듬습니다.", "labels": "enhancement,UI"},
    {"title": "[Design] 시각 장애인 및 색약자를 위한 고대비(High Contrast) 테마 추가", "body": "접근성 향상을 위해 일반 테마 외에 명도 대비가 뚜렷한 고대비 모드를 설정에 추가합니다.", "labels": "accessibility,UI"},
    {"title": "[UI/UX] 성경 장/절 이동 시 스와이프 제스처 지원", "body": "이전 장, 다음 장으로 넘어갈 때 화면 좌우 스와이프로 직관적이고 빠르게 이동할 수 있도록 지원합니다.", "labels": "enhancement,UX"},
    {"title": "[Design] 텅 빈 상태(Empty States) 시 일러스트 및 친절한 안내 문구 적용", "body": "검색 결과나 북마크가 없을 때, 텅 빈 화면 대신 아름다운 일러스트와 행동을 유도하는 문구를 배치합니다.", "labels": "enhancement,UI"},
    {"title": "[UI/UX] 한 손 조작성을 위한 검색 바 및 주요 메뉴 하단 배치 검토", "body": "스마트폰 화면이 커짐에 따라, 검색 입력창 등 주요 입력 컨트롤을 화면 하단으로 내려 한 손 조작을 편하게 합니다.", "labels": "enhancement,UX"},
    {"title": "[UI/UX] 텍스트 행간(Line-height) 및 자간(Letter-spacing) 미세 조정 옵션 제공", "body": "사용자가 본인의 시력과 취향에 맞게 줄 간격과 글자 간격을 정밀하게 조절할 수 있는 옵션을 제공합니다.", "labels": "enhancement,UX"},
    
    {"title": "[Retention] 연속 출석(Streak) 달성 시 축하 애니메이션 및 사운드 제공", "body": "7일, 30일 등 연속 출석 목표 달성 시 화면에 폭죽 애니메이션을 띄워 성취감을 줍니다.", "labels": "gamification,UX"},
    {"title": "[Engagement] 주간 영적 활동 리포트 인포그래픽 제공", "body": "이번 주 읽은 장 수, 기도 시간 등을 예쁜 그래프와 인포그래픽으로 요약하여 주말마다 보여줍니다.", "labels": "enhancement,UX"},
    {"title": "[Gamification] 성경 66권 각 권 완료 시 디지털 뱃지 수집 시스템 구현", "body": "창세기, 출애굽기 등 각 권을 끝까지 읽을 때마다 고유한 디자인의 디지털 뱃지나 우표를 책자에 수집하는 재미를 줍니다.", "labels": "gamification,UX"},
    {"title": "[Retention] '놓친 스트릭 복구하기' 아이템 및 포인트 시스템 도입", "body": "하루를 놓쳤을 때 퀴즈를 풀거나 포인트를 사용하여 스트릭을 이어갈 수 있는 구제 수단을 마련합니다.", "labels": "gamification,UX"},
    {"title": "[Habit] 사용자 맞춤형 읽기 시간대 푸시 알림 발송", "body": "사용자가 가장 자주 앱을 여는 시간대를 분석하여, 그 시간에 맞춰 맞춤형 푸시 알림을 보냅니다.", "labels": "enhancement,feature"},
    {"title": "[Engagement] 성경 인물 중심 일일 퀴즈 및 포인트 리워드", "body": "매일 1개의 가벼운 성경 퀴즈를 제공하고, 정답 시 포인트나 뱃지 경험치를 지급합니다.", "labels": "gamification,feature"},
    {"title": "[Engagement] 기도 타이머 및 앰비언트 사운드 제공 고도화", "body": "기도 시간에 집중할 수 있도록 백그라운드로 자연의 소리나 잔잔한 음악을 틀어주는 타이머 기능을 구현합니다.", "labels": "enhancement,feature"},
    {"title": "[Gamification] 누적 읽기 데이터 기반 칭호(Title) 부여 시스템", "body": "레벨에 따라 '말씀의 씨앗', '빛의 자녀' 등 프로필에 달 수 있는 칭호를 부여하여 동기를 부여합니다.", "labels": "gamification,UX"},
    {"title": "[Retention] '1년 전 오늘 읽었던 말씀' 리마인드 기능", "body": "과거 같은 날짜에 북마크하거나 메모했던 구절을 푸시 알림으로 알려주어 감동을 재현합니다.", "labels": "enhancement,feature"},
    {"title": "[Habit] 안드로이드/iOS 인터랙티브 홈 화면 위젯 지원", "body": "앱을 켜지 않고도 홈 화면 위젯에서 바로 다음 구절로 넘기거나 오늘의 말씀을 확인할 수 있게 합니다.", "labels": "enhancement,feature"},

    {"title": "[Viral] 말씀 카드 생성 시 동적 타이포그래피(비디오/GIF) 지원", "body": "정적인 이미지를 넘어, 글씨가 써지거나 배경이 움직이는 동영상 형태의 말씀 카드를 만들어 공유성을 높입니다.", "labels": "enhancement,feature"},
    {"title": "[Community] 익명 기도 요청 게시판 UI 개선 및 공감(아멘) 버튼 활성화", "body": "서로의 기도 제목에 '아멘' 버튼을 눌러 공감을 표시하고 누적 공감 수를 시각적으로 보여줍니다.", "labels": "enhancement,community"},
    {"title": "[Viral] 소셜 미디어 공유 시 해당 구절로 바로 이동하는 딥링크 적용", "body": "친구에게 카톡으로 공유한 구절을 클릭하면, 앱이 열리면서 바로 그 장/절로 이동하도록 딥링크를 구축합니다.", "labels": "enhancement,technical"},
    {"title": "[Community] '오늘의 본문' 공개 묵상 피드 기능", "body": "같은 말씀을 읽은 다른 사용자들의 공개된 은혜로운 묵상 노트들을 랜덤하게 볼 수 있는 피드를 만듭니다.", "labels": "enhancement,community"},
    {"title": "[Viral] 특정 업적 달성 시 긍정적 모먼트 기반 리뷰 유도 팝업 노출", "body": "30일 스트릭 달성 등 유저가 가장 성취감을 느끼는 타이밍에 스토어 리뷰 작성을 유도합니다.", "labels": "enhancement,growth"},
    {"title": "[Community] 초대 코드 기반 소그룹 통독방 및 진도 공유 기능", "body": "가족, 교회 셀 모임 등을 위한 비공개 방을 만들어 서로의 성경 읽기 진도를 공유하고 응원합니다.", "labels": "enhancement,community"},
    {"title": "[Viral] 친구 초대 시 특별 테마 제공 레퍼럴(Referral) 프로그램", "body": "친구를 초대하여 가입하면 양쪽 모두에게 프리미엄 테마나 전용 뱃지를 지급하는 시스템을 구축합니다.", "labels": "enhancement,growth"},
    {"title": "[Community] 유저들이 함께 이어쓰는 성경 필사 릴레이 프로젝트", "body": "커뮤니티 탭에서 유저들이 한 구절씩 디지털로 필사하여 하나의 성경책을 완성하는 이벤트를 운영합니다.", "labels": "gamification,community"},
    {"title": "[Social] 공유 이미지에 앱 로고 및 다운로드 QR 코드 선택 포함 옵션", "body": "말씀 카드 공유 시 우측 하단에 앱 설치로 유도하는 QR 코드나 워터마크를 넣을 수 있는 옵션을 제공합니다.", "labels": "enhancement,growth"},
    {"title": "[Community] 교회/단체별 통독 랭킹 시스템 도입", "body": "소속 교회나 단체를 등록하여 단체 간의 성경 읽기 총량 랭킹을 보여주어 선의의 경쟁을 유도합니다.", "labels": "gamification,community"},

    {"title": "[Performance] SQLite 인덱스 최적화를 통한 검색 속도 2배 향상", "body": "전체 성경 검색 시 병목이 발생하는 쿼리를 분석하고 FTS(Full-Text Search) 및 인덱스를 최적화하여 속도를 높입니다.", "labels": "performance,technical"},
    {"title": "[Security] 개인 데이터 로컬 암호화 및 생체 인증 잠금 기능", "body": "메모나 기도 제목 등 민감한 데이터의 로컬 DB 암호화 및 앱 실행 시 지문/FaceID 잠금 옵션을 제공합니다.", "labels": "security,feature"},
    {"title": "[Technical] Sentry 또는 Firebase Crashlytics 연동 및 에러 모니터링", "body": "앱 크래시 및 오류를 실시간으로 추적하고 리포트받을 수 있도록 분석 툴을 연동합니다.", "labels": "technical,infrastructure"},
    {"title": "[Performance] 불필요한 에셋 압축 및 지연 로딩을 통한 앱 용량 최소화", "body": "고해상도 이미지 및 안 쓰는 폰트 에셋을 제거하고 WebP 압축을 적용하여 다운로드 크기를 줄입니다.", "labels": "performance,technical"},
    {"title": "[Security] 클라우드 백업 기반 OAuth 2.0 보안 토큰 관리 최적화", "body": "구글 드라이브 백업 시 사용하는 인증 토큰의 만료 처리 및 갱신 로직을 보안 표준에 맞게 강화합니다.", "labels": "security,technical"},
    {"title": "[Technical] 성경 본문 무결성 주기적 검증 및 자동 복구 메커니즘", "body": "기기 용량 부족 등으로 DB 파일이 손상되었을 경우 이를 감지하고 원본 데이터를 자동 복원하는 로직을 추가합니다.", "labels": "technical,reliability"},
    {"title": "[Performance] Provider 상태 관리 최적화로 불필요한 리렌더링 방지", "body": "Consumer 위젯의 범위를 최소화하고 Selector를 활용하여 스크롤 시 발생하는 프레임 드랍과 배터리 소모를 줄입니다.", "labels": "performance,technical"},

    {"title": "[Accessibility] 시각 장애인을 위한 스크린 리더(TalkBack, VoiceOver) 최적화", "body": "앱 내 모든 아이콘과 버튼에 Semantics 라벨을 정확하게 부여하여 화면 낭독기가 완벽히 작동하게 합니다.", "labels": "accessibility,UX"},
    {"title": "[Advanced] AI 기반 자연어 질문-성경 구절 추천 기능 연동", "body": "사용자가 '너무 불안할 때 어떻게 해?'라고 치면 AI(LLM)가 적절한 성경 구절과 위로의 말을 찾아주는 기능을 시범 도입합니다.", "labels": "enhancement,AI"},
    {"title": "[Advanced] 구절 터치 시 역사적 배경/지도와 연결되는 하이퍼링크 백과사전", "body": "특정 지명이나 인물이 나왔을 때 터치하면 성경 지도나 인물 사막으로 바로 넘어가는 깊이 있는 연구 도구를 제공합니다.", "labels": "enhancement,feature"}
]

def create_issue(issue):
    cmd = [
        "gh", "issue", "create",
        "--title", issue["title"],
        "--body", issue["body"]
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode == 0:
        print(f"Created: {issue['title']}")
    else:
        print(f"Failed to create: {issue['title']}\nError: {result.stderr}")

for index, issue in enumerate(issues, start=1):
    print(f"Processing {index}/50...")
    create_issue(issue)

print("Finished creating 50 issues.")
