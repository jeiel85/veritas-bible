package com.veritasbible.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.veritasbible.app.study.ui.StudyCreateDialog
import com.veritasbible.app.study.ui.StudyDetailScreen
import com.veritasbible.app.study.ui.StudyListScreen
import com.veritasbible.app.study.ui.StudyViewModel
import com.veritasbible.app.ui.BibleViewModel
import com.veritasbible.app.ui.screens.*
import com.veritasbible.app.ui.theme.MyApplicationTheme

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    READER("성경읽기", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_reader"),
    SEARCH("검색", Icons.Filled.Search, Icons.Outlined.Search, "tab_search"),
    STUDY("성경연구", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, "tab_study"),
    DASHBOARD("통계분석", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_dashboard"),
    NOTES("연구노트", Icons.Filled.EditNote, Icons.Outlined.EditNote, "tab_notes"),
    SETTINGS("설정백업", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable complete visual notch edge-to-edge drawing compliance
        enableEdgeToEdge()

        setContent {
            val viewModel: BibleViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                if (!isOnboardingCompleted) {
                    OnboardingScreen(viewModel = viewModel)
                } else {
                    WordStudyAppMain(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WordStudyAppMain(viewModel: BibleViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.READER) }
    val studyViewModel: StudyViewModel = viewModel(factory = StudyViewModel.Factory)
    val isPrepopulating by viewModel.isPrepopulating.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    // 연구 모듈 네비게이션 상태: 세션 ID가 설정되면 상세 화면을 띄운다.
    var openSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    // 리더에서 선택한 연구 시작 범위. null 이면 다이얼로그가 챕터 전체로 fallback.
    var pendingStudyRange by remember { mutableStateOf<com.veritasbible.app.ui.screens.ReaderStudyRange?>(null) }

    // 뒤로가기: 각 화면 뎁스를 한 단계씩 거슬러 올라간다. 리더 탭에서만 기본 동작(앱 종료)이 일어난다.
    // 다이얼로그·드롭다운은 Compose가 자체적으로 back 을 dismiss 로 처리하므로 여기서 다루지 않는다.
    // 1) 연구 상세 화면 → 연구 목록 (바텀 네비 복귀)
    BackHandler(enabled = openSessionId != null) {
        openSessionId = null
        studyViewModel.selectSession(null)
    }
    // 2) 리더가 아닌 탭 → 리더 탭
    BackHandler(enabled = openSessionId == null && currentTab != MainTab.READER) {
        currentTab = MainTab.READER
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        bottomBar = {
            if (!isPrepopulating && openSessionId == null) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_navigation_bar"),
                    tonalElevation = 8.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val localizedTitle = com.veritasbible.app.util.LanguageManager.getTranslation(tab.tag, appLanguage)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = localizedTitle,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = localizedTitle,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isPrepopulating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (appLanguage == "EN") "Initializing Veritas Bible..." else "Veritas Bible 초기화 중...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (openSessionId != null) {
            // 연구 세션 상세 화면 (전체 화면, 바텀 네비게이션 숨김)
            LaunchedEffect(openSessionId) {
                studyViewModel.selectSession(openSessionId)
            }
            StudyDetailScreen(
                bibleViewModel = viewModel,
                studyViewModel = studyViewModel,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                onBack = {
                    openSessionId = null
                    studyViewModel.selectSession(null)
                }
            )
        } else {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(220),
                label = "ScreenSwitchFade"
            ) { tab ->
                val padModifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                when (tab) {
                    MainTab.READER -> BibleReaderScreen(
                        viewModel = viewModel,
                        modifier = padModifier,
                        onNavigateToNotes = { currentTab = MainTab.NOTES },
                        onStartStudy = { range ->
                            pendingStudyRange = range
                            showCreateDialog = true
                        }
                    )
                    MainTab.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        modifier = padModifier,
                        onNavigateToReader = { currentTab = MainTab.READER }
                    )
                    MainTab.STUDY -> StudyListScreen(
                        bibleViewModel = viewModel,
                        studyViewModel = studyViewModel,
                        modifier = padModifier,
                        onOpenSession = { id -> openSessionId = id },
                        onStartNewFromReader = {
                            currentTab = MainTab.READER
                            pendingStudyRange = null
                            showCreateDialog = true
                        }
                    )
                    MainTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        modifier = padModifier
                    )
                    MainTab.NOTES -> NotesScreen(
                        viewModel = viewModel,
                        modifier = padModifier,
                        onNavigateToReader = { currentTab = MainTab.READER }
                    )
                    MainTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        modifier = padModifier
                    )
                }
            }
        }
    }

    if (showCreateDialog && openSessionId == null) {
        StudyCreateDialog(
            bibleViewModel = viewModel,
            studyViewModel = studyViewModel,
            initialRange = pendingStudyRange,
            onDismiss = {
                showCreateDialog = false
                pendingStudyRange = null
            },
            onCreated = { newId ->
                showCreateDialog = false
                pendingStudyRange = null
                openSessionId = newId
            }
        )
    }
}
