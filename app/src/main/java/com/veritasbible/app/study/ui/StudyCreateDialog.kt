package com.veritasbible.app.study.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritasbible.app.data.BookCatalogEntry
import com.veritasbible.app.ui.BibleViewModel
import com.veritasbible.app.ui.screens.getBookTranslation
import com.veritasbible.app.util.LanguageManager
import kotlinx.coroutines.launch

/** 시작/끝 위치 한 쌍. */
private data class VerseRef(val book: String, val bookEn: String, val bookId: Int, val chapter: Int, val verse: Int)

private enum class AnchorMode { START, END, NONE }

/**
 * 본문 범위를 직접 골라 새 연구 세션을 만드는 다이얼로그.
 *
 * 사용자는 ‘여기부터’ / ‘여기까지’ 칩으로 두 anchor 를 활성화하고
 * 책 → 장 → 절 순서로 선택한다. 빈 anchor 는 현재 리더 위치를 기본값으로
 * 채우고, 두 anchor 가 모두 채워지면 ‘만들기’가 활성화된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyCreateDialog(
    bibleViewModel: BibleViewModel,
    studyViewModel: StudyViewModel,
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val appLanguage by bibleViewModel.appLanguage.collectAsState()
    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val currentVerses by bibleViewModel.currentVerses.collectAsState()
    val koToEn by bibleViewModel.bookKoToEn.collectAsState()
    val catalog by bibleViewModel.bookCatalog.collectAsState()

    val defaultBookEn = currentVerses.firstOrNull()?.bookEn ?: koToEn[currentBook] ?: currentBook
    val defaultBookId = currentVerses.firstOrNull()?.bookId
        ?: catalog.firstOrNull { it.ko == currentBook }?.bookId
        ?: 0
    val defaultFirstVerse = currentVerses.firstOrNull()?.verse ?: 1
    val defaultLastVerse = currentVerses.lastOrNull()?.verse ?: defaultFirstVerse

    // 두 anchor 의 현재 값. 다이얼로그 진입 시 리더 컨텍스트로 초기화.
    var startRef by remember(currentBook, currentChapter, defaultFirstVerse) {
        mutableStateOf(VerseRef(currentBook, defaultBookEn, defaultBookId, currentChapter, defaultFirstVerse))
    }
    var endRef by remember(currentBook, currentChapter, defaultLastVerse) {
        mutableStateOf(VerseRef(currentBook, defaultBookEn, defaultBookId, currentChapter, defaultLastVerse))
    }
    var title by remember(currentBook, currentChapter) {
        mutableStateOf(buildDefaultTitle(currentBook, currentChapter, appLanguage))
    }

    // 어느 anchor 를 편집 중인지. NONE 이면 anchor 칩 두 개만 보이는 메인 폼.
    var editing by remember { mutableStateOf(AnchorMode.NONE) }

    val isValid = remember(startRef, endRef) {
        val startKey = Triple(startRef.bookId, startRef.chapter, startRef.verse)
        val endKey = Triple(endRef.bookId, endRef.chapter, endRef.verse)
        compareKeys(startKey, endKey) <= 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LanguageManager.getTranslation("study_create_dialog_title", appLanguage)) },
        text = {
            Column {
                Text(
                    text = LanguageManager.getTranslation("study_create_dialog_hint", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(LanguageManager.getTranslation("study_create_title_label", appLanguage)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_create_title")
                )
                Spacer(modifier = Modifier.height(12.dp))

                AnchorRow(
                    label = LanguageManager.getTranslation("study_create_anchor_start", appLanguage),
                    ref = startRef,
                    appLanguage = appLanguage,
                    onClick = { editing = AnchorMode.START },
                    tag = "study_create_anchor_start"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AnchorRow(
                    label = LanguageManager.getTranslation("study_create_anchor_end", appLanguage),
                    ref = endRef,
                    appLanguage = appLanguage,
                    onClick = { editing = AnchorMode.END },
                    tag = "study_create_anchor_end"
                )

                if (!isValid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LanguageManager.getTranslation("study_create_invalid_range", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    studyViewModel.createSession(
                        title = title.ifBlank {
                            buildDefaultTitle(startRef.book, startRef.chapter, appLanguage)
                        },
                        book = startRef.book,
                        bookEn = startRef.bookEn,
                        bookId = startRef.bookId,
                        startChapter = startRef.chapter,
                        startVerse = startRef.verse,
                        endChapter = endRef.chapter,
                        endVerse = endRef.verse,
                        onCreated = onCreated
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("study_create_confirm")
            ) {
                Text(LanguageManager.getTranslation("study_create_confirm", appLanguage))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageManager.getTranslation("common_cancel", appLanguage))
            }
        }
    )

    if (editing != AnchorMode.NONE) {
        VersePickerSheet(
            bibleViewModel = bibleViewModel,
            initial = if (editing == AnchorMode.START) startRef else endRef,
            appLanguage = appLanguage,
            label = if (editing == AnchorMode.START)
                LanguageManager.getTranslation("study_create_anchor_start", appLanguage)
            else
                LanguageManager.getTranslation("study_create_anchor_end", appLanguage),
            onPick = { picked ->
                if (editing == AnchorMode.START) {
                    startRef = picked
                    // 만약 startRef > endRef 가 되면 endRef 를 startRef 로 맞춰준다 (혼란 방지)
                    val sKey = Triple(picked.bookId, picked.chapter, picked.verse)
                    val eKey = Triple(endRef.bookId, endRef.chapter, endRef.verse)
                    if (compareKeys(sKey, eKey) > 0) endRef = picked
                    // 제목이 비어 있거나 기본 제목 패턴이면 자동 업데이트
                    title = if (title.isBlank()) buildDefaultTitle(picked.book, picked.chapter, appLanguage) else title
                } else {
                    endRef = picked
                    val sKey = Triple(startRef.bookId, startRef.chapter, startRef.verse)
                    val eKey = Triple(picked.bookId, picked.chapter, picked.verse)
                    if (compareKeys(sKey, eKey) > 0) startRef = picked
                }
                editing = AnchorMode.NONE
                // refresh chapters list for new book if needed
                scope.launch { /* no-op: BibleViewModel manages chapters per current reader book */ }
            },
            onCancel = { editing = AnchorMode.NONE }
        )
    }
}

@Composable
private fun AnchorRow(
    label: String,
    ref: VerseRef,
    appLanguage: String,
    onClick: () -> Unit,
    tag: String
) {
    val bookName = if (appLanguage == "EN") ref.bookEn else ref.book
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(72.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$bookName ${ref.chapter}:${ref.verse}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "▾",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 책 → 장 → 절을 단계적으로 고르는 풀스크린 시트형 다이얼로그.
 *
 * 모바일에서 한 화면에 책 66권 + 장 + 절을 같이 보여주면 답답하므로,
 * 단계별 패널을 보여주고 ‘선택’을 누르면 다음 단계로 넘어간다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VersePickerSheet(
    bibleViewModel: BibleViewModel,
    initial: VerseRef,
    appLanguage: String,
    label: String,
    onPick: (VerseRef) -> Unit,
    onCancel: () -> Unit
) {
    val catalog by bibleViewModel.bookCatalog.collectAsState()

    var step by remember { mutableStateOf(0) } // 0: book, 1: chapter, 2: verse
    var bookEntry by remember(initial) {
        mutableStateOf(catalog.firstOrNull { it.ko == initial.book }
            ?: BookCatalogEntry(initial.book, initial.bookEn, initial.bookId))
    }
    var chapter by remember(initial) { mutableStateOf(initial.chapter) }
    var verse by remember(initial) { mutableStateOf(initial.verse) }
    var chapters by remember { mutableStateOf<List<Int>>(emptyList()) }
    var verseCount by remember { mutableStateOf(0) }

    // 책이 바뀌면 장 목록 갱신, 장이 바뀌면 절 수 갱신
    LaunchedEffect(bookEntry, step) {
        if (step >= 1) {
            chapters = bibleViewModel.repositoryChaptersFor(bookEntry.ko)
            if (chapter !in chapters && chapters.isNotEmpty()) chapter = chapters.first()
        }
    }
    LaunchedEffect(bookEntry, chapter, step) {
        if (step >= 2) {
            verseCount = bibleViewModel.getVerseCount(bookEntry.ko, chapter)
            if (verse > verseCount) verse = verseCount.coerceAtLeast(1)
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val bookName = if (appLanguage == "EN") bookEntry.en else bookEntry.ko
                    Text(
                        text = "$bookName ${chapter}:${verse}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()

                // 단계 indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        LanguageManager.getTranslation("study_create_step_book", appLanguage),
                        LanguageManager.getTranslation("study_create_step_chapter", appLanguage),
                        LanguageManager.getTranslation("study_create_step_verse", appLanguage)
                    ).forEachIndexed { i, l ->
                        AssistChip(
                            onClick = { if (i <= step) step = i },
                            label = { Text(l, fontSize = 11.sp) },
                            modifier = Modifier.testTag("study_create_step_$i"),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (step == i)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                HorizontalDivider()

                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                ) {
                    when (step) {
                        0 -> BookPicker(
                            catalog = catalog,
                            picked = bookEntry,
                            appLanguage = appLanguage,
                            onPick = {
                                bookEntry = it
                                step = 1
                            }
                        )
                        1 -> ChapterPicker(
                            chapters = chapters,
                            picked = chapter,
                            onPick = {
                                chapter = it
                                step = 2
                            }
                        )
                        else -> VerseNumberPicker(
                            count = verseCount,
                            picked = verse,
                            onPick = { verse = it }
                        )
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = step == 2,
                        onClick = {
                            onPick(
                                VerseRef(
                                    book = bookEntry.ko,
                                    bookEn = bookEntry.en,
                                    bookId = bookEntry.bookId,
                                    chapter = chapter,
                                    verse = verse
                                )
                            )
                        },
                        modifier = Modifier.testTag("study_create_picker_confirm")
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageManager.getTranslation("study_create_picker_confirm", appLanguage))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookPicker(
    catalog: List<BookCatalogEntry>,
    picked: BookCatalogEntry,
    appLanguage: String,
    onPick: (BookCatalogEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(catalog, key = { it.bookId }) { entry ->
            val selected = entry.bookId == picked.bookId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onPick(entry) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("study_create_book_${entry.bookId}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == "EN") entry.en else entry.ko,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(Icons.Filled.Done, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ChapterPicker(
    chapters: List<Int>,
    picked: Int,
    onPick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(64.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chapters) { ch ->
            val selected = ch == picked
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onPick(ch) }
                    .testTag("study_create_chapter_$ch"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ch.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun VerseNumberPicker(
    count: Int,
    picked: Int,
    onPick: (Int) -> Unit
) {
    if (count <= 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(56.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items((1..count).toList()) { v ->
            val selected = v == picked
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onPick(v) }
                    .testTag("study_create_verse_$v"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = v.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun buildDefaultTitle(book: String, chapter: Int, appLanguage: String): String =
    if (appLanguage == "EN") "$book $chapter study" else "$book ${chapter}장 연구"

private fun compareKeys(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Int {
    val firstCmp = a.first.compareTo(b.first)
    if (firstCmp != 0) return firstCmp
    val secondCmp = a.second.compareTo(b.second)
    if (secondCmp != 0) return secondCmp
    return a.third.compareTo(b.third)
}
