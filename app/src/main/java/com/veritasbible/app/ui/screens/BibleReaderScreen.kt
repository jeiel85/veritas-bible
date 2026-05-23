package com.veritasbible.app.ui.screens

import android.content.Intent
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    onNavigateToNotes: () -> Unit = {}
) {
    val currentBook by viewModel.currentBook.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val availableBooks by viewModel.availableBooks.collectAsState()
    val availableChapters by viewModel.availableChapters.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
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

    // Selected verse popup actions states
    var selectedVerseForAction by remember { mutableStateOf<BibleVerse?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf("") }

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
                                    text = getBookTranslation(currentBook, appLanguage),
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
                                    text = { Text(getBookTranslation(book, appLanguage), color = readTheme.text, fontWeight = FontWeight.SemiBold) },
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
                                    text = "$currentBook",
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isHighlighted) parsedColor.copy(alpha = readTheme.toHighlightAlpha()) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = if (selectedVerseForAction?.id == verse.id) 1.5.dp else 0.dp,
                                    color = if (selectedVerseForAction?.id == verse.id) readTheme.text.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedVerseForAction = if (selectedVerseForAction?.id == verse.id) null else verse
                                }
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
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

                    // Buffer bottom spacing to prevent cut offs
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            // Highlighting and Memo Option Palette popup
            AnimatedVisibility(
                visible = selectedVerseForAction != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                selectedVerseForAction?.let { verse ->
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
                                    text = "${verse.book} ${verse.chapter}:${verse.verse}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { selectedVerseForAction = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close Palette",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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

                                colorsList.forEach { (colorHex, contentDesc) ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                                            .border(
                                                width = if (verse.highlightColor == colorHex) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.toggleHighlight(verse.id, colorHex)
                                                selectedVerseForAction = verse.copy(highlightColor = colorHex)
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
                                            viewModel.toggleHighlight(verse.id, null)
                                            selectedVerseForAction = verse.copy(highlightColor = null)
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

                            // Functional action buttons: Memo, Share, Copy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add Memo / Study notes
                                Button(
                                    onClick = {
                                        noteInputText = ""
                                        showNoteDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("action_memo_button"),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(Icons.Outlined.EditNote, contentDescription = "Memo")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("메모 작성", fontSize = 13.sp)
                                }

                                // Native share verse
                                OutlinedButton(
                                    onClick = {
                                        val shareText = "[${verse.book} ${verse.chapter}:${verse.verse}]\n" +
                                                "${verse.text}\n" +
                                                "(${verse.bookEn} ${verse.chapter}:${verse.verse} - ${verse.textEn})\n\n" +
                                                "From Veritas Bible Study App"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "성경 구절 공유")
                                        context.startActivity(shareIntent)
                                        selectedVerseForAction = null
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
                                        val copyText = "[${verse.book} ${verse.chapter}:${verse.verse}] ${verse.text}"
                                        clipboardManager.setText(AnnotatedString(copyText))
                                        viewModel.setOperationsMessage("클립보드에 성경 구절이 복사되었습니다.")
                                        selectedVerseForAction = null
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

    // Memo popup Input Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = "${selectedVerseForAction?.book} ${selectedVerseForAction?.chapter}:${selectedVerseForAction?.verse} 메모",
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
                        val verseCopy = selectedVerseForAction
                        if (verseCopy != null && noteInputText.trim().isNotEmpty()) {
                            viewModel.saveNote(
                                verseId = verseCopy.id,
                                book = verseCopy.book,
                                chapter = verseCopy.chapter,
                                verse = verseCopy.verse,
                                textContent = noteInputText.trim()
                            )
                        }
                        showNoteDialog = false
                        selectedVerseForAction = null
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

// Alpha values depending on contrasting themes to optimize accessibility standard
fun ReadTheme.toHighlightAlpha(): Float = when (this) {
    ReadTheme.LIGHT -> 0.35f
    ReadTheme.SEPIA -> 0.35f
    ReadTheme.COSMIC_NIGHT -> 0.22f
}

fun getBookTranslation(book: String, appLanguage: String): String {
    if (book.isEmpty()) return ""
    if (appLanguage != "EN") return book
    return when (book) {
        "요한복음" -> "John"
        "창세기" -> "Genesis"
        "마태복음" -> "Matthew"
        "마가복음" -> "Mark"
        "누가복음" -> "Luke"
        "로마서" -> "Romans"
        "시편" -> "Psalms"
        "잠언" -> "Proverbs"
        else -> book
    }
}
