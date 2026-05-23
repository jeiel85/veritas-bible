package com.veritasbible.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val isPrepopulating by viewModel.isPrepopulating.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(), // Ensures gesture navigation layouts do not obscure items
        bottomBar = {
            if (!isPrepopulating) {
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
        } else {
            // High speed fluid swipe fade transitions
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
                        onNavigateToNotes = { currentTab = MainTab.NOTES }
                    )
                    MainTab.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        modifier = padModifier,
                        onNavigateToReader = { currentTab = MainTab.READER }
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
