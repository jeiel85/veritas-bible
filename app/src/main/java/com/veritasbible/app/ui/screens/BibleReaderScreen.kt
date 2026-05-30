package com.veritasbible.app.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.ui.BibleViewModel
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import kotlinx.coroutines.delay

enum class ReadTheme(val displayName: String, val bg: Color, val text: Color, val border: Color) {
    LIGHT("라이트", Color(0xFFFFFFFF), Color(0xFF1A1A1A), Color(0xFFE2E8F0)),
    SEPIA("세피아", Color(0xFFFBF0D9), Color(0xFF422006), Color(0xFFEADBBE)),
    COSMIC_NIGHT("우주야간", Color(0xFF0F172A), Color(0xFFF1F5F9), Color(0xFF1E293B))
}

enum class TranslationMode(val displayName: String) {
    KOREAN("한글"),
    ENGLISH("ENG"),
    COMBINED("대역")
}

/**
 * 리더에서 사용자가 절을 탭으로 선택해 만든 연구 시작 범위.
 *
 * 한 절만 골랐으면 start == end. 같은 챕터 안에서 두 절을 찍으면
 * 두 절 사이 전체가 범위로 잡힌다. StudyCreateDialog 가 이 값을
 * 기본 anchor 로 받아 띄운다.
 */
data class ReaderStudyRange(
    val book: String,
    val bookEn: String,
    val bookId: Int,
    val startChapter: Int,
    val startVerse: Int,
    val endChapter: Int,
    val endVerse: Int
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onNavigateToNotes: () -> Unit = {},
    onStartStudy: (ReaderStudyRange?) -> Unit = {}
) {
    val currentBook by viewModel.currentBook.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val availableBooks by viewModel.availableBooks.collectAsState()
    val availableChapters by viewModel.availableChapters.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val koToEn by viewModel.bookKoToEn.collectAsState()
    val currentVerses by viewModel.currentVerses.collectAsState()
    val currentSessionSec by viewModel.currentSessionSeconds.collectAsState()
    val isBibleDownloaded by viewModel.isBibleDownloaded.collectAsState()

    // Reader UI styling settings states
    var fontSize by remember { mutableStateOf(18) }
    var readTheme by remember { mutableStateOf(ReadTheme.COSMIC_NIGHT) }
    var translationMode by remember { mutableStateOf(TranslationMode.COMBINED) }

    // Dropdown visibility states
    var showBookDropdown by remember { mutableStateOf(false) }
    var showChapterDropdown by remember { mutableStateOf(false) }

    // Selected verse popup actions states.
    // 한 절 anchor + (optional) target. anchor == target 이면 단일 선택, 다르면 범위.
    var selectionAnchor by remember { mutableStateOf<BibleVerse?>(null) }
    var selectionTarget by remember { mutableStateOf<BibleVerse?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("") }

    // 챕터/책이 바뀌면 선택은 해제 (다른 절 ID들이라 의미 없음).
    LaunchedEffect(currentBook, currentChapter) {
        selectionAnchor = null
        selectionTarget = null
    }

    // 현재 선택된 절 목록 (정렬된 범위). 표시·일괄 작업·범위 전달에 모두 사용.
    val selectedVerses: List<BibleVerse> = remember(selectionAnchor, selectionTarget, currentVerses) {
        val a = selectionAnchor ?: return@remember emptyList()
        val t = selectionTarget ?: a
        val (low, high) = if (a.verse <= t.verse) a.verse to t.verse else t.verse to a.verse
        currentVerses.filter { it.verse in low..high }
    }
    // 절을 선택한 상태에서 뒤로가기는 선택만 해제한다(앱 종료 대신 한 뎁스 위로).
    BackHandler(enabled = selectionAnchor != null) {
        selectionAnchor = null
        selectionTarget = null
    }

    val selectedVerseIds: Set<Int> = remember(selectedVerses) { selectedVerses.map { it.id }.toSet() }
    val isRangeSelection = selectedVerses.size > 1
    val headlineVerse = selectedVerses.firstOrNull()
    val tailVerse = selectedVerses.lastOrNull()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Secondary Timer Coroutine
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.incrementSessionTime()
        }
    }

    // Capture reading statistics when user exits/changes this section
    DisposableEffect(Unit) {
        onDispose {
            viewModel.commitReadingSession(currentVerses.size)
        }
    }

    // Auto trigger commit on chapter change
    LaunchedEffect(currentBook, currentChapter) {
        viewModel.commitReadingSession(currentVerses.size)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(readTheme.bg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Formatting Controls Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "Bible Reader",
                            tint = readTheme.text.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Veritas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = readTheme.text,
                            letterSpacing = 1.sp
                        )
                    }

                    // Format adjustments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Font decrease
                        IconButton(
                            onClick = { if (fontSize > 12) fontSize -= 2 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A-", color = readTheme.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Font increase
                        IconButton(
                            onClick = { if (fontSize < 32) fontSize += 2 },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("A+", color = readTheme.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Theme Toggle cycle
                        IconButton(
                            onClick = {
                                readTheme = when (readTheme) {
                                    ReadTheme.LIGHT -> ReadTheme.SEPIA
                                    ReadTheme.SEPIA -> ReadTheme.COSMIC_NIGHT
                                    ReadTheme.COSMIC_NIGHT -> ReadTheme.LIGHT
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = "Change Theme",
                                tint = readTheme.text
                            )
                        }

                        // Translation Toggle
                        IconButton(
                            onClick = {
                                translationMode = when (translationMode) {
                                    TranslationMode.KOREAN -> TranslationMode.ENGLISH
                                    TranslationMode.ENGLISH -> TranslationMode.COMBINED
                                    TranslationMode.COMBINED -> TranslationMode.KOREAN
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Translation Mode",
                                tint = readTheme.text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Selectors Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Book selector
                    Box(modifier = Modifier.weight(1.5f)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = readTheme.bg),
                            border = BorderStroke(1.dp, readTheme.border),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBookDropdown = true }
                                .testTag("book_selector_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getBookTranslation(currentBook, appLanguage, koToEn),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = readTheme.text
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select Book",
                                    tint = readTheme.text.copy(alpha = 0.7f)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showBookDropdown,
                            onDismissRequest = { showBookDropdown = false },
                            modifier = Modifier
                                .background(readTheme.bg)
                                .border(1.dp, readTheme.border)
                                .width(180.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            availableBooks.forEach { book ->
                                DropdownMenuItem(
                                    text = { Text(getBookTranslation(book, appLanguage, koToEn), color = readTheme.text, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        viewModel.selectBook(book)
                                        showBookDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Chapter selector
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = readTheme.bg),
                            border = BorderStroke(1.dp, readTheme.border),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChapterDropdown = true }
                                .testTag("chapter_selector_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (appLanguage == "EN") "Ch $currentChapter" else "${currentChapter}장",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = readTheme.text
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select Chapter",
                                    tint = readTheme.text.copy(alpha = 0.7f)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showChapterDropdown,
                            onDismissRequest = { showChapterDropdown = false },
                            modifier = Modifier
                                .background(readTheme.bg)
                                .border(1.dp, readTheme.border)
                                .width(110.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            availableChapters.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(if (appLanguage == "EN") "Ch $chap" else "${chap}장", color = readTheme.text, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        viewModel.selectChapter(chap)
                                        showChapterDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Simple Live Session Study Timer display
                    val mins = currentSessionSec / 60
                    val secs = currentSessionSec % 60
                    Surface(
                        color = readTheme.text.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "Timer",
                                tint = readTheme.text.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                                fontSize = 12.sp,
                                color = readTheme.text,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        containerColor = readTheme.bg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentVerses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = "No Scripture Loaded",
                            tint = readTheme.text.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isBibleDownloaded) "로컬 묵상 데이터 읽는 중..." else "성경 데이터가 실장되지 않았습니다",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = readTheme.text,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBibleDownloaded) "데이터베이스 인덱스를 로드해 들이고 있습니다." else "개인 오프라인 기초 성경 패킷을 기기로 다운로드하여 구축해 주십시오.",
                            fontSize = 12.sp,
                            color = readTheme.text.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        if (!isBibleDownloaded) {
                            Spacer(modifier = Modifier.height(24.dp))
                            val isDownloading by viewModel.isDownloading.collectAsState()
                            if (isDownloading) {
                                val downloadProgress by viewModel.downloadProgress.collectAsState()
                                val downloadStatus by viewModel.downloadStatus.collectAsState()
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = downloadStatus,
                                        fontSize = 11.sp,
                                        color = readTheme.text.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(200.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.downloadBibleData() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("reader_inline_download_button")
                                ) {
                                    Text("성경 기초 자료 다운로드")
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                // Scriptures List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Chapter Heading Header Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = getBookTranslation(currentBook, appLanguage, koToEn),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = readTheme.text,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "CHAPTER $currentChapter",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = readTheme.text.copy(alpha = 0.5f),
                                    letterSpacing = 3.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 2.dp)
                                        .background(readTheme.text.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }

                    // Bible verses items list
                    items(currentVerses, key = { it.id }) { verse ->
                        val isHighlighted = verse.highlightColor != null
                        val parsedColor = if (isHighlighted) {
                            Color(android.graphics.Color.parseColor(verse.highlightColor))
                        } else {
                            Color.Transparent
                        }
                        val isSelected = verse.id in selectedVerseIds

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = when {
                                        isSelected -> readTheme.text.copy(alpha = 0.07f)
                                        isHighlighted -> parsedColor.copy(alpha = readTheme.toHighlightAlpha())
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) readTheme.text.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    val a = selectionAnchor
                                    when {
                                        a == null -> {
                                            // 첫 탭: anchor 와 target 동일 (단일 선택)
                                            selectionAnchor = verse
                                            selectionTarget = verse
                                        }
                                        a.id == verse.id && selectionTarget?.id == verse.id -> {
                                            // 같은 절을 다시 탭 → 선택 해제
                                            selectionAnchor = null
                                            selectionTarget = null
                                        }
                                        else -> {
                                            // 다른 절 탭 → 범위의 다른 끝 갱신
                                            selectionTarget = verse
                                        }
                                    }
                                }
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            // 절 단위 highlight 는 본문 흐름이 끊기지 않도록 왼쪽 색상 막대로 표시
                            if (isHighlighted) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(parsedColor, RoundedCornerShape(2.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.Top) {
                                // 단락 시작 표시 (●) — 한글 개역 단락 감각을 따라간다.
                                if (verse.paragraphStart) {
                                    Text(
                                        text = "●",
                                        fontSize = (fontSize - 6).sp,
                                        color = readTheme.text.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                                    )
                                }
                                // Verse index indicator
                                Text(
                                    text = "${verse.verse} ",
                                    fontSize = (fontSize - 2).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighlighted) readTheme.text else readTheme.text.copy(alpha = 0.45f),
                                    modifier = Modifier.padding(end = 6.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    // Korean verse translation
                                    if (translationMode == TranslationMode.KOREAN || translationMode == TranslationMode.COMBINED) {
                                        Text(
                                            text = verse.text,
                                            fontSize = fontSize.sp,
                                            lineHeight = (fontSize * 1.6).sp,
                                            color = readTheme.text,
                                            fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }

                                    // English translation
                                    if (translationMode == TranslationMode.ENGLISH || translationMode == TranslationMode.COMBINED) {
                                        if (translationMode == TranslationMode.COMBINED) Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = verse.textEn,
                                            fontSize = (fontSize - 1).sp,
                                            lineHeight = ((fontSize - 1) * 1.55).sp,
                                            color = if (translationMode == TranslationMode.COMBINED) readTheme.text.copy(alpha = 0.75f) else readTheme.text,
                                            fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }

                    // Buffer bottom spacing to prevent cut offs
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            // Highlighting and Memo Option Palette popup
            AnimatedVisibility(
                visible = headlineVerse != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                if (headlineVerse != null && tailVerse != null) {
                    val verse = headlineVerse
                    // 범위 안에서 모든 절이 같은 색이면 그 색을 '현재 색'으로 보고, 섞여 있으면 null.
                    val commonHighlightColor: String? = remember(selectedVerses) {
                        val first = selectedVerses.firstOrNull()?.highlightColor
                        if (selectedVerses.all { it.highlightColor == first }) first else null
                    }
                    val rangeLabel = if (isRangeSelection) {
                        val bookText = if (appLanguage == "EN") verse.bookEn else verse.book
                        "$bookText ${verse.chapter}:${verse.verse}-${tailVerse.verse} (${selectedVerses.size}${if (appLanguage == "EN") " verses" else "개 절"})"
                    } else {
                        "${if (appLanguage == "EN") verse.bookEn else verse.book} ${verse.chapter}:${verse.verse}"
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("action_palette_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Title header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rangeLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = {
                                        selectionAnchor = null
                                        selectionTarget = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close Palette",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 다중 선택 안내 — 한 번 더 절을 탭하면 범위가 잡힌다는 힌트
                            if (!isRangeSelection) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (appLanguage == "EN") "Tap another verse to make a range." else "다른 절을 탭하면 범위가 잡힙니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Color Highlight Dot picker Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val colorsList = listOf(
                                    "#E74C3C" to "Red",       // Warm Red
                                    "#F1C40F" to "Yellow",    // Bright Yellow
                                    "#2ECC71" to "Green",     // Pure Green
                                    "#3498DB" to "Blue",      // sky Blue
                                    "#9B59B6" to "Purple"     // Purple
                                )

                                colorsList.forEach { (colorHex, _) ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                                            .border(
                                                width = if (commonHighlightColor == colorHex) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedVerses.forEach { v ->
                                                    viewModel.toggleHighlight(v.id, colorHex)
                                                }
                                            }
                                    )
                                }

                                // Clear Highlight color selection
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                                        .clickable {
                                            selectedVerses.forEach { v ->
                                                viewModel.toggleHighlight(v.id, null)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FormatColorReset,
                                        contentDescription = "Clear Highlight",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 본문 기반 연구 세션 생성 진입 — 선택 범위를 그대로 다이얼로그에 전달
                            Button(
                                onClick = {
                                    val range = ReaderStudyRange(
                                        book = verse.book,
                                        bookEn = verse.bookEn,
                                        bookId = verse.bookId,
                                        startChapter = verse.chapter,
                                        startVerse = verse.verse,
                                        endChapter = tailVerse.chapter,
                                        endVerse = tailVerse.verse
                                    )
                                    selectionAnchor = null
                                    selectionTarget = null
                                    onStartStudy(range)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("action_start_study_button"),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Outlined.AutoStories, contentDescription = "Start Inductive Study")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (appLanguage == "EN") "Start inductive study from this passage" else "이 본문으로 귀납적 연구 시작",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Functional action buttons: Memo, Share, Copy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add Memo / Study notes — 범위 선택일 땐 anchor 절에 기록
                                Button(
                                    onClick = {
                                        noteInputText = ""
                                        showNoteDialog = true
                                    },
                                    enabled = !isRangeSelection,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_memo_button"),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Outlined.EditNote, contentDescription = "Memo")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (appLanguage == "EN") "Note" else "메모 작성",
                                        fontSize = 13.sp
                                    )
                                }

                                // Native share verse(s)
                                OutlinedButton(
                                    onClick = {
                                        val header = if (isRangeSelection) {
                                            "[${verse.book} ${verse.chapter}:${verse.verse}-${tailVerse.verse}]"
                                        } else {
                                            "[${verse.book} ${verse.chapter}:${verse.verse}]"
                                        }
                                        val koBody = selectedVerses.joinToString("\n") { "${it.verse} ${it.text}" }
                                        val enBody = selectedVerses.joinToString(" ") { it.textEn }
                                        val shareText = "$header\n$koBody\n($enBody)\n\nFrom Veritas Bible Study App"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "성경 구절 공유")
                                        context.startActivity(shareIntent)
                                        selectionAnchor = null
                                        selectionTarget = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("구절 공유", fontSize = 13.sp)
                                }

                                // Fast Clip board copy
                                OutlinedButton(
                                    onClick = {
                                        val header = if (isRangeSelection) {
                                            "[${verse.book} ${verse.chapter}:${verse.verse}-${tailVerse.verse}]"
                                        } else {
                                            "[${verse.book} ${verse.chapter}:${verse.verse}]"
                                        }
                                        val body = selectedVerses.joinToString(" ") { it.text }
                                        clipboardManager.setText(AnnotatedString("$header $body"))
                                        viewModel.setOperationsMessage(
                                            if (isRangeSelection) "${selectedVerses.size}개 절을 클립보드에 복사했습니다."
                                            else "클립보드에 성경 구절이 복사되었습니다."
                                        )
                                        selectionAnchor = null
                                        selectionTarget = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("복사", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Memo popup Input Dialog — 단일 절 선택일 때만 활성화됨
    if (showNoteDialog) {
        val memoVerse = headlineVerse
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = "${if (appLanguage == "EN") memoVerse?.bookEn else memoVerse?.book} ${memoVerse?.chapter}:${memoVerse?.verse} " + (if (appLanguage == "EN") "note" else "메모"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = noteInputText,
                    onValueChange = { noteInputText = it },
                    placeholder = { Text(if (appLanguage == "EN") "Write your notes, meditation, or commentary on this verse..." else "이 절에 대한 신앙 고백, 묵상 내용 혹은 해설을 입력해 보세요...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("memo_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memoVerse != null && noteInputText.trim().isNotEmpty()) {
                            viewModel.saveNote(
                                verseId = memoVerse.id,
                                book = memoVerse.book,
                                chapter = memoVerse.chapter,
                                verse = memoVerse.verse,
                                textContent = noteInputText.trim()
                            )
                        }
                        showNoteDialog = false
                        selectionAnchor = null
                        selectionTarget = null
                    },
                    modifier = Modifier.testTag("confirm_save_memo")
                ) {
                    Text(if (appLanguage == "EN") "Save & Encrypt" else "저장 및 암호화")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(if (appLanguage == "EN") "Cancel" else "취소")
                }
            }
        )
    }
}

// 본문 흐름이 끊기지 않도록 배경 alpha 는 살짝만 (왼쪽 색상 막대가 주된 표식).
fun ReadTheme.toHighlightAlpha(): Float = when (this) {
    ReadTheme.LIGHT -> 0.12f
    ReadTheme.SEPIA -> 0.14f
    ReadTheme.COSMIC_NIGHT -> 0.10f
}

/**
 * 책 이름을 사용자 언어에 맞춰 변환. 매핑 정보는 [BibleViewModel.bookKoToEn] 가
 * DB 카탈로그에서 채워준다. 매핑이 없으면 한국어 그대로 fallback.
 */
fun getBookTranslation(book: String, appLanguage: String, koToEn: Map<String, String> = emptyMap()): String {
    if (book.isEmpty()) return ""
    if (appLanguage != "EN") return book
    return koToEn[book] ?: book
}
