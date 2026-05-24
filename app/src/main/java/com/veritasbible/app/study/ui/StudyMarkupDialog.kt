package com.veritasbible.app.study.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.study.data.MarkType
import com.veritasbible.app.util.LanguageManager

/**
 * 모바일 친화적 마킹 다이얼로그.
 *
 * 흐름:
 *   1) 절 선택 → 2) 단어 칩 탭(시작 + 끝) → 3) 마킹 타입 선택(핵심 5종 / 더보기 13종)
 *   → 4) 선택적으로 메모 입력 → 5) 저장
 *
 * 단어 단위 칩은 모바일에서 일관성 있게 작동하며, 시작 오프셋과 끝
 * 오프셋은 원본 절 텍스트 내 character index로 계산되어
 * [com.veritasbible.app.study.data.StudyMarkup] 에 저장된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMarkupDialog(
    appLanguage: String,
    verses: List<BibleVerse>,
    initialVerse: BibleVerse?,
    onSave: (verse: BibleVerse, startOffset: Int, endOffset: Int, selectedText: String, markType: String, memo: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedVerse by remember { mutableStateOf(initialVerse ?: verses.firstOrNull()) }
    val verse = selectedVerse
    val tokens = remember(verse?.id) { tokenize(verse?.text.orEmpty()) }
    var anchorIndex by remember(verse?.id) { mutableStateOf<Int?>(null) }
    var extendIndex by remember(verse?.id) { mutableStateOf<Int?>(null) }
    var pickedType by remember(verse?.id) { mutableStateOf<String?>(null) }
    var showAdvanced by remember { mutableStateOf(false) }
    var memo by remember(verse?.id) { mutableStateOf("") }

    val (selStart, selEnd) = computeRange(tokens, anchorIndex, extendIndex)
    val selectedText = if (verse != null && selStart in 0..selEnd && selEnd <= verse.text.length) {
        verse.text.substring(selStart, selEnd)
    } else ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(min = 320.dp, max = 640.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = LanguageManager.getTranslation("study_markup_dialog_title", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 1) 절 선택
                Text(
                    text = LanguageManager.getTranslation("study_markup_pick_verse", appLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(verses, key = { it.id }) { v ->
                        val isSel = v.id == verse?.id
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedVerse = v
                                anchorIndex = null
                                extendIndex = null
                            },
                            label = { Text("${v.chapter}:${v.verse}", fontSize = 12.sp) },
                            modifier = Modifier.testTag("markup_verse_chip_${v.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (verse == null) {
                    Text(
                        text = LanguageManager.getTranslation("study_passage_empty", appLanguage),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // 2) 단어 칩 선택
                    Text(
                        text = LanguageManager.getTranslation("study_markup_pick_words", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    WordChipFlow(
                        tokens = tokens,
                        anchorIndex = anchorIndex,
                        extendIndex = extendIndex,
                        onTokenClick = { index ->
                            when {
                                anchorIndex == null -> {
                                    anchorIndex = index
                                    extendIndex = index
                                }
                                anchorIndex == index && extendIndex == index -> {
                                    anchorIndex = null
                                    extendIndex = null
                                }
                                else -> extendIndex = index
                            }
                        }
                    )
                    if (selectedText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "›  \"$selectedText\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3) 마킹 타입
                    Text(
                        text = LanguageManager.getTranslation("study_markup_pick_type", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    MarkTypeRow(
                        types = MarkType.ESSENTIAL,
                        pickedType = pickedType,
                        appLanguage = appLanguage,
                        onClick = { pickedType = it }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { showAdvanced = !showAdvanced }) {
                        Icon(
                            imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageManager.getTranslation("study_markup_advanced_toggle", appLanguage))
                    }
                    if (showAdvanced) {
                        MarkTypeRow(
                            types = MarkType.ADVANCED,
                            pickedType = pickedType,
                            appLanguage = appLanguage,
                            onClick = { pickedType = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4) 메모
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        label = { Text(LanguageManager.getTranslation("study_markup_memo_label", appLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_markup_memo"),
                        minLines = 1,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val canSave = verse != null && pickedType != null && selectedText.isNotEmpty()
                    Button(
                        onClick = {
                            val v = verse ?: return@Button
                            val t = pickedType ?: return@Button
                            onSave(v, selStart, selEnd, selectedText, t, memo.trim().ifBlank { null })
                        },
                        enabled = canSave,
                        modifier = Modifier.testTag("study_markup_save")
                    ) {
                        Text(LanguageManager.getTranslation("common_save", appLanguage))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkTypeRow(
    types: List<String>,
    pickedType: String?,
    appLanguage: String,
    onClick: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        types.forEach { type ->
            val color = MarkupTheme.colorFor(type)
            FilterChip(
                selected = pickedType == type,
                onClick = { onClick(type) },
                label = {
                    Text(
                        text = LanguageManager.getTranslation("mark_type_$type", appLanguage),
                        fontSize = 12.sp,
                        color = if (pickedType == type) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("study_markup_type_$type")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordChipFlow(
    tokens: List<Token>,
    anchorIndex: Int?,
    extendIndex: Int?,
    onTokenClick: (Int) -> Unit
) {
    val (selStart, selEnd) = computeRangeIndices(anchorIndex, extendIndex)
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tokens.forEachIndexed { index, token ->
            val isSelected = index in selStart..selEnd && selStart >= 0
            val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            val border = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(6.dp))
                    .clickable { onTokenClick(index) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("markup_word_chip_$index")
            ) {
                Text(
                    text = token.text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

internal data class Token(val text: String, val start: Int, val end: Int)

internal fun tokenize(text: String): List<Token> {
    if (text.isEmpty()) return emptyList()
    val list = mutableListOf<Token>()
    var i = 0
    while (i < text.length) {
        // skip whitespace
        while (i < text.length && text[i].isWhitespace()) i++
        if (i >= text.length) break
        val start = i
        while (i < text.length && !text[i].isWhitespace()) i++
        list.add(Token(text.substring(start, i), start, i))
    }
    return list
}

internal fun computeRange(tokens: List<Token>, anchorIndex: Int?, extendIndex: Int?): Pair<Int, Int> {
    if (anchorIndex == null || extendIndex == null || tokens.isEmpty()) return 0 to 0
    val lo = minOf(anchorIndex, extendIndex).coerceIn(0, tokens.lastIndex)
    val hi = maxOf(anchorIndex, extendIndex).coerceIn(0, tokens.lastIndex)
    return tokens[lo].start to tokens[hi].end
}

private fun computeRangeIndices(anchorIndex: Int?, extendIndex: Int?): Pair<Int, Int> {
    if (anchorIndex == null || extendIndex == null) return -1 to -1
    return minOf(anchorIndex, extendIndex) to maxOf(anchorIndex, extendIndex)
}
