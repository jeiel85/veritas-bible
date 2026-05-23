package com.veritasbible.app.util

object LanguageManager {
    fun getTranslation(key: String, language: String): String {
        val en = mapOf(
            // Tabs
            "tab_reader" to "Bible",
            "tab_search" to "Search",
            "tab_dashboard" to "Analytics",
            "tab_notes" to "Study Notes",
            "tab_settings" to "Settings",
            
            // Reader screen
            "reader_title" to "Veritas Bible Study",
            "reader_font_size" to "Font Size",
            "reader_screen_theme" to "Reader Accent",
            "reader_timer_study" to "Daily Active Study: ",
            "reader_chapter" to "Ch",
            "reader_verse" to "v",
            "no_scripture_title" to "Scripture data not loaded",
            "no_scripture_desc" to "Please download and install the offline Bible resource packet on your device.",
            "download_bible_btn" to "Download Bible Data",
            "reading_index_loading" to "Loading index database...",
            
            // Search screen
            "search_title" to "Deep Corpus Search",
            "search_placeholder" to "Search keyword (e.g., God, Jesus)...",
            "search_no_results" to "No search results match the query.",
            "search_min_chars" to "Enter at least 2 characters to start search",
            "search_total_results" to "found %d instances in text.",
            
            // Dashboard
            "dashboard_title" to "Analytics Dashboard",
            "db_weekly_summary" to "Weekly Reading Sessions Log",
            "db_cumulative_stats" to "Cumulative Statistics",
            "db_total_chapters" to "Total Read Chapters",
            "db_total_seconds" to "Total Session Time",
            "db_goal_status" to "Daily Target Goal status",
            "db_percentage_complete" to "Percentage Completed",
            "db_goal_target" to "Active Target Goal",
            "db_update_goal" to "Adjust Active Target",
            "db_new_entry" to "Add Manual Reading Entry",
            "db_book_label" to "Book Name",
            "db_chap_label" to "Chapter",
            "db_add_btn" to "Save Study Entry",
            "db_goal_suffix" to " Chapters",
            "db_seconds_suffix" to " min",
            
            // Notes
            "notes_title" to "Encrypted Study Notes",
            "notes_add_placeholder" to "Write your thoughts on %s %d:%d...",
            "notes_save_btn" to "Secure Note",
            "notes_empty_state" to "No study notes matching current filters.",
            "notes_tag_field" to "Topic Tag (Optional)",
            
            // Settings
            "settings_title" to "Settings & Backups",
            "settings_theme_section" to "System Visual Environment Theme",
            "theme_system" to "System Config",
            "theme_light" to "Light Mode",
            "theme_dark" to "Dark Mode",
            "settings_data_section" to "Scripture Data Governance",
            "settings_data_loaded" to "Scripture Base Resource Active",
            "settings_data_not_loaded" to "Scripture Data Deconfigured",
            "settings_data_desc_loaded" to "Safely activated in offline storage",
            "settings_data_desc_not_loaded" to "Reading feature is restricted offline",
            "settings_data_download_btn" to "Load Data",
            "settings_data_wipe_btn" to "Purge Data",
            "settings_backup_section" to "Local Backup Operations",
            "settings_backup_desc" to "Manage encrypted backups",
            "settings_export_btn" to "Export Backup File To Clipboard",
            "settings_import_btn" to "Import System Backup File",
            "settings_onboarding_reset" to "Force Reset App Onboarding",
            "language_section" to "App Interface Language",
            "lang_system" to "System Auto",
            "lang_ko" to "한국어",
            "lang_en" to "English",
            "onboarding_reset_success" to "App Onboarding flow has been reset successfully."
        )
        
        val ko = mapOf(
            // Tabs
            "tab_reader" to "성경읽기",
            "tab_search" to "검색",
            "tab_dashboard" to "통계분석",
            "tab_notes" to "연구노트",
            "tab_settings" to "설정백업",
            
            // Reader screen
            "reader_title" to "Veritas 성경 연구",
            "reader_font_size" to "글꼴 크기",
            "reader_screen_theme" to "뷰어 색상",
            "reader_timer_study" to "오늘의 장치 활성 연구: ",
            "reader_chapter" to "장",
            "reader_verse" to "절",
            "no_scripture_title" to "성경 데이터가 실장되지 않았습니다",
            "no_scripture_desc" to "개인 오프라인 기초 성경 패킷을 기기에 다운로드하여 구축해 주십시오.",
            "download_bible_btn" to "성경 기초 자료 다운로드",
            "reading_index_loading" to "로컬 묵상 데이터 읽는 중...",
            
            // Search screen
            "search_title" to "성경 정본 정밀 수색",
            "search_placeholder" to "수색 키워드 입력 (예: 하나님, 예수)...",
            "search_no_results" to "질의어와 일치하는 성경 구절이 없습니다.",
            "search_min_chars" to "최소 2글자 이상 입력해 주십시오",
            "search_total_results" to "%d개의 절이 발견되었습니다.",
            
            // Dashboard
            "dashboard_title" to "연구 통계 대시보드",
            "db_weekly_summary" to "주간 묵상 연구 이력 추적",
            "db_cumulative_stats" to "누적 연구 통계량",
            "db_total_chapters" to "총 통과 장 수",
            "db_total_seconds" to "누적 집중 시간",
            "db_goal_status" to "일일 목표 달성 진척비",
            "db_percentage_complete" to "달성률",
            "db_goal_target" to "활성 학습 목표",
            "db_update_goal" to "학습 목표량 수정",
            "db_new_entry" to "수동 묵상 이력 기입 및 보고",
            "db_book_label" to "도서명",
            "db_chap_label" to "장 수",
            "db_add_btn" to "연구 이력 기입",
            "db_goal_suffix" to " 장",
            "db_seconds_suffix" to " 분",
            
            // Notes
            "notes_title" to "암호화 연구 노트",
            "notes_add_placeholder" to "%s %d장 %d절 연구 묵상 작성...",
            "notes_save_btn" to "암호 보안 저장",
            "notes_empty_state" to "현재 필터와 일치하는 연구 노트가 없습니다.",
            "notes_tag_field" to "분류 태그 (필수 입력 아님)",
            
            // Settings
            "settings_title" to "종합 환경 설정 및 백업",
            "settings_theme_section" to "화면 테마 환경 변경",
            "theme_system" to "시스템 설정",
            "theme_light" to "라이트 모드",
            "theme_dark" to "다크 모드",
            "settings_data_section" to "성경 데이터 가동 및 관리",
            "settings_data_loaded" to "성경 기초 자료 조율 장착 완료",
            "settings_data_not_loaded" to "성경 데이터가 설치되지 않음",
            "settings_data_desc_loaded" to "오프라인 드라이브에 안전히 활성화됨",
            "settings_data_desc_not_loaded" to "설독 묵상 시 읽기가 제한됩니다",
            "settings_data_download_btn" to "자료 다운로드",
            "settings_data_wipe_btn" to "자료 완전 파기",
            "settings_backup_section" to "로컬 주권 백업 도구",
            "settings_backup_desc" to "보안 데이터 이동 관리",
            "settings_export_btn" to "백업 데이터 텍스트 복사",
            "settings_import_btn" to "백업 데이터 검증 및 복원",
            "settings_onboarding_reset" to "강제 초기 온보딩 되돌리기",
            "language_section" to "앱 표시 언어 설정 (Language)",
            "lang_system" to "시스템 기준 자동",
            "lang_ko" to "한국어",
            "lang_en" to "English",
            "onboarding_reset_success" to "초기 안내 온보딩 과정으로 유턴 완료되었습니다."
        )
        
        val resolvedLang = when (language) {
            "KO" -> "KO"
            "EN" -> "EN"
            else -> {
                val locale = java.util.Locale.getDefault().language
                if (locale.lowercase() == "en" || locale.lowercase().startsWith("en")) "EN" else "KO"
            }
        }
        
        return if (resolvedLang == "EN") {
            en[key] ?: ko[key] ?: key
        } else {
            ko[key] ?: en[key] ?: key
        }
    }
}
