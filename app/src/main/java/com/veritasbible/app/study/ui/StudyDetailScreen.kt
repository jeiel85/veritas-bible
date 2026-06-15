package com.veritasbible.app.study.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.AnnotatedString as ComposeAnnotatedString
import com.veritasbible.app.data.BibleVerse
import com.veritasbible.app.study.data.CharacterTagTarget
import com.veritasbible.app.study.data.CharacterTagType
import com.veritasbible.app.study.data.LinkType
import com.veritasbible.app.study.data.LinkedIds
import com.veritasbible.app.study.data.MarkType
import com.veritasbible.app.study.data.ObservationQuestion
import com.veritasbible.app.study.data.PropositionStatus
import com.veritasbible.app.study.data.StudyCharacterTag
import com.veritasbible.app.study.data.StudyDivision
import com.veritasbible.app.study.data.StudyInterpretation
import com.veritasbible.app.study.data.StudyMarkup
import com.veritasbible.app.study.data.StudyMarkupLink
import com.veritasbible.app.study.data.StudyObservation
import com.veritasbible.app.study.data.StudyOutlineNode
import com.veritasbible.app.study.data.StudyProposition
import com.veritasbible.app.study.data.StudySession
import com.veritasbible.app.study.data.StudyStage
import com.veritasbible.app.study.data.StudyThemeCheck
import com.veritasbible.app.study.data.ThemeCheckKey
import com.veritasbible.app.ui.BibleViewModel
import com.veritasbible.app.util.LanguageManager
import kotlinx.coroutines.delay

private enum class DetailTab(val stage: String, val labelKey: String) {
    OBSERVATION(StudyStage.OBSERVATION, "stage_observation"),
    MARKUP(StudyStage.STRUCTURE, "stage_structure"),
    DIVISION(StudyStage.DIVISION, "stage_division"),
    INTERPRETATION(StudyStage.INTERPRETATION, "stage_interpretation"),
    THEME(StudyStage.THEME, "stage_theme"),
    PROPOSITION(StudyStage.PROPOSITION, "stage_proposition"),
    OUTLINE(StudyStage.OUTLINE, "stage_outline"),
    APPLICATION(StudyStage.APPLICATION, "stage_application"),
    REPORT(StudyStage.REPORT, "stage_report")
}

/** 본문 직접-탭 상호작용 모드. READ 면 탭은 아무 동작도 하지 않는다(읽기 전용). */
private enum class TapMode { READ, MARK, LINK }

/** 연결 모드에서 첫 단어를 탭했을 때 보류 중인 시작 토큰. */
private data class PendingToken(
    val verse: BibleVerse,
    val start: Int,
    val end: Int,
    val text: String,
    val existingMarkupId: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDetailScreen(
    bibleViewModel: BibleViewModel,
    studyViewModel: StudyViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val appLanguage by bibleViewModel.appLanguage.collectAsState()
    val session by studyViewModel.selectedSession.collectAsState()
    val verses by studyViewModel.sessionVerses.collectAsState()
    val markups by studyViewModel.markups.collectAsState()
    val message by studyViewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            delay(200)
            studyViewModel.clearMessage()
        }
    }

    val current = session
    var selectedTab by rememberSaveable { mutableStateOf(DetailTab.OBSERVATION) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = current?.title ?: LanguageManager.getTranslation("study_loading", appLanguage),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (current != null) {
                            Text(
                                text = formatRange(current, appLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (current == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val markupLinks by studyViewModel.markupLinks.collectAsState()
            PassagePanel(
                session = current,
                verses = verses,
                markups = markups,
                links = markupLinks,
                appLanguage = appLanguage,
                studyViewModel = studyViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 360.dp)
            )

            HorizontalDivider()

            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 8.dp
            ) {
                DetailTab.values().forEachIndexed { _, tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            if (tab.stage != "others") {
                                studyViewModel.updateSessionMeta(current.id, currentStage = tab.stage)
                            }
                        },
                        text = {
                            Text(
                                text = LanguageManager.getTranslation(tab.labelKey, appLanguage),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("study_tab_${tab.name.lowercase()}")
                    )
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                when (selectedTab) {
                    DetailTab.OBSERVATION -> ObservationPanel(
                        sessionId = current.id,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.MARKUP -> MarkupPanel(
                        sessionId = current.id,
                        verses = verses,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.DIVISION -> DivisionPanel(
                        session = current,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.INTERPRETATION -> InterpretationPanel(
                        sessionId = current.id,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.THEME -> ThemePanel(
                        session = current,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.PROPOSITION -> PropositionPanel(
                        sessionId = current.id,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.OUTLINE -> OutlinePanel(
                        session = current,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.APPLICATION -> ApplicationPanel(
                        sessionId = current.id,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                    DetailTab.REPORT -> ReportPanel(
                        session = current,
                        studyViewModel = studyViewModel,
                        appLanguage = appLanguage
                    )
                }
            }
        }
    }
}

// =================================================================================
// Passage panel with inline markup highlighting
// =================================================================================

@Composable
private fun PassagePanel(
    session: StudySession,
    verses: List<BibleVerse>,
    markups: List<StudyMarkup>,
    links: List<StudyMarkupLink>,
    appLanguage: String,
    studyViewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val markupByVerse = remember(markups) { markups.groupBy { it.verseId } }
    val markupById = remember(markups) { markups.associateBy { it.id } }
    val linksByVerse = remember(links, markupById) {
        links.groupBy { link ->
            markupById[link.fromMarkupId]?.verseId ?: -1
        }.filterKeys { it >= 0 }
    }

    // 직접-탭 상호작용 상태. 타입은 사용자에게 묻지 않는다 — 마킹은 "구조의 일부"라는
    // 표시일 뿐이고, 주어/동사 역할은 연결 시 탭 순서(첫째=주어, 둘째=동사)에 암묵적으로 담긴다.
    var tapMode by remember { mutableStateOf(TapMode.READ) }
    var pendingFrom by remember { mutableStateOf<PendingToken?>(null) }
    // 모드가 바뀌면 보류 선택은 해제
    LaunchedEffect(tapMode) { pendingFrom = null }

    // 직접-탭 모드에서 뒤로가기는 한 뎁스씩 취소한다: 보류 선택 → 모드 해제(READ).
    // READ 상태일 때는 비활성이라 상위(상세 화면 닫기) 핸들러가 처리한다.
    BackHandler(enabled = tapMode != TapMode.READ) {
        if (pendingFrom != null) {
            pendingFrom = null
        } else {
            tapMode = TapMode.READ
        }
    }

    val onWordTap: (BibleVerse, Int, Int, String, String?) -> Unit = { v, s, e, t, existingId ->
        when (tapMode) {
            TapMode.READ -> {}
            // 단순 마킹은 단일 중립 타입(KEYWORD)으로 저장한다.
            TapMode.MARK -> studyViewModel.toggleTokenMarkup(session.id, v, s, e, t, MarkType.KEYWORD, existingId)
            TapMode.LINK -> {
                val pend = pendingFrom
                when {
                    pend == null -> pendingFrom = PendingToken(v, s, e, t, existingId)
                    pend.verse.id == v.id && pend.start == s -> pendingFrom = null // 같은 단어 → 취소
                    else -> {
                        // 탭 순서로 역할 결정: 첫째=주어, 둘째=동사 (LinkType.SUBJECT_VERB 기본).
                        studyViewModel.connectTokens(
                            session.id, LinkType.SUBJECT_VERB,
                            pend.verse, pend.start, pend.end, pend.text, pend.existingMarkupId,
                            v, s, e, t, existingId
                        )
                        pendingFrom = null
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = LanguageManager.getTranslation("study_passage_panel_title", appLanguage),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatRange(session, appLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 모드 바: 읽기 / 마킹 / 연결
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            TapMode.values().forEach { m ->
                val labelKey = when (m) {
                    TapMode.READ -> "study_tap_mode_read"
                    TapMode.MARK -> "study_tap_mode_mark"
                    TapMode.LINK -> "study_tap_mode_link"
                }
                FilterChip(
                    selected = tapMode == m,
                    onClick = { tapMode = m },
                    label = { Text(LanguageManager.getTranslation(labelKey, appLanguage), fontSize = 12.sp) },
                    modifier = Modifier.testTag("study_tap_mode_${m.name.lowercase()}")
                )
            }
        }

        // 모드별 안내 (타입 선택 없음 — 그냥 탭으로 구조를 그린다)
        when (tapMode) {
            TapMode.MARK -> TapHint(LanguageManager.getTranslation("study_tap_hint_mark", appLanguage))
            TapMode.LINK -> {
                val pend = pendingFrom
                TapHint(
                    if (pend != null) {
                        String.format(LanguageManager.getTranslation("study_tap_pending_from", appLanguage), pend.text)
                    } else {
                        LanguageManager.getTranslation("study_tap_hint_link", appLanguage)
                    }
                )
            }
            TapMode.READ -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (verses.isEmpty()) {
            Text(
                text = LanguageManager.getTranslation("study_passage_empty", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f, fill = false)
            ) {
                verses.forEach { v ->
                    val verseMarkups = markupByVerse[v.id].orEmpty()
                    val verseLinks = linksByVerse[v.id].orEmpty()
                    val pend = pendingFrom
                    val pendingRange = if (pend != null && pend.verse.id == v.id) pend.start to pend.end else null
                    VerseRow(
                        verse = v,
                        markups = verseMarkups,
                        links = verseLinks,
                        markupById = markupById,
                        tapEnabled = tapMode != TapMode.READ,
                        pendingRange = pendingRange,
                        onWordTap = onWordTap
                    )
                }
            }
        }
    }
}

@Composable
private fun TapHint(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 한 절(verse)을 그리는 행. ‘● 1:1 본문 텍스트’ 레이아웃이며,
 * 같은 절 안의 명시 link 들은 본문 텍스트 영역 위에 Canvas overlay 로
 * 곡선으로 표시한다. 다른 절을 가로지르는 link 는 PassagePanel 의
 * 별도 ‘구조 요약’ 영역에서 텍스트로 제공한다.
 */
@Composable
private fun VerseRow(
    verse: BibleVerse,
    markups: List<StudyMarkup>,
    links: List<StudyMarkupLink>,
    markupById: Map<String, StudyMarkup>,
    tapEnabled: Boolean = false,
    pendingRange: Pair<Int, Int>? = null,
    onWordTap: (verse: BibleVerse, start: Int, end: Int, text: String, existingMarkupId: String?) -> Unit = { _, _, _, _, _ -> }
) {
    var layout by remember(verse.id, markups) { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    val layoutState = rememberUpdatedState(layout)

    // 본문 + (보류 중 시작 단어가 이 절이면) 강조 span
    val pendingColor = MaterialTheme.colorScheme.primary
    val displayText = remember(verse.text, markups, pendingRange) {
        val base = buildMarkedVerseText(verse.text, markups)
        if (pendingRange == null) base
        else buildAnnotatedString {
            append(base)
            val s = pendingRange.first.coerceIn(0, verse.text.length)
            val e = pendingRange.second.coerceIn(s, verse.text.length)
            if (e > s) addStyle(SpanStyle(background = pendingColor.copy(alpha = 0.25f)), s, e)
        }
    }

    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        if (verse.paragraphStart) {
            Text(
                text = "●",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 3.dp, top = 6.dp)
            )
        }
        Text(
            text = "${verse.chapter}:${verse.verse}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 6.dp, top = 2.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            val tapModifier = if (tapEnabled) {
                Modifier.pointerInput(verse.id) {
                    detectTapGestures { pos ->
                        val lay = layoutState.value ?: return@detectTapGestures
                        val offset = lay.getOffsetForPosition(pos).coerceIn(0, verse.text.length)
                        val tok = tokenAt(verse.text, offset) ?: return@detectTapGestures
                        val tokenText = verse.text.substring(tok.start, tok.end)
                        // 토큰과 겹치는 기존 마킹이 있으면 그 ID를 함께 넘긴다.
                        val existing = markups.firstOrNull { it.startOffset < tok.end && it.endOffset > tok.start }
                        onWordTap(verse, tok.start, tok.end, tokenText, existing?.id)
                    }
                }
            } else Modifier

            Text(
                text = displayText,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                onTextLayout = { layout = it },
                modifier = tapModifier
            )
            val current = layout
            if (current != null && links.isNotEmpty()) {
                MarkupLinksOverlay(
                    layout = current,
                    text = verse.text,
                    links = links,
                    markupById = markupById,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

/** 문자 offset 이 속한 토큰을 찾는다. 토큰 사이/끝을 탭하면 가장 가까운 앞 토큰. */
private fun tokenAt(text: String, offset: Int): Token? {
    val toks = tokenize(text)
    if (toks.isEmpty()) return null
    return toks.firstOrNull { offset in it.start until it.end }
        ?: toks.lastOrNull { it.start <= offset }
        ?: toks.first()
}

/**
 * 본문 위에 link 곡선을 그리는 Canvas overlay.
 *
 * 같은 줄 안에서는 짧은 곡선(아래로 내려갔다 올라오는 brace),
 * 다른 줄로 넘어가면 두 끝점을 잇는 경로를 brace 두 개로 분리한다.
 */
@Composable
private fun MarkupLinksOverlay(
    layout: androidx.compose.ui.text.TextLayoutResult,
    text: String,
    links: List<StudyMarkupLink>,
    markupById: Map<String, StudyMarkup>,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 2.5f
        links.forEach { link ->
            val from = markupById[link.fromMarkupId] ?: return@forEach
            val to = markupById[link.toMarkupId] ?: return@forEach
            val color = MarkupTheme.colorFor(from.markType)

            // 절을 넘는 연결: 이 overlay 는 from 절의 좌표만 알 수 있으므로
            // 정확한 선 대신 "다음 구절로 이어진다"는 아래방향 화살촉 힌트만 그린다.
            if (from.verseId != to.verseId) {
                val fromMid = ((from.startOffset + from.endOffset) / 2).coerceIn(0, text.length)
                val fromBox = layout.getBoundingBox(fromMid)
                val cx = fromBox.center.x
                val topY = fromBox.bottom + 2f
                val tipY = topY + 7f
                drawLine(color, Offset(cx, topY), Offset(cx, tipY), strokeWidth)
                drawLine(color, Offset(cx - 4f, tipY - 4f), Offset(cx, tipY), strokeWidth)
                drawLine(color, Offset(cx + 4f, tipY - 4f), Offset(cx, tipY), strokeWidth)
                return@forEach
            }

            // 손글씨 귀납법처럼: 두 밑줄의 끝점(양 끝) 중 서로 가장 가까운 한 쌍을
            // 골라, 줄 사이 공간으로 살짝 내려왔다 올라가는 얕은 곡선으로 잇는다.
            val fromEnds = underlineEnds(layout, from, text) ?: return@forEach
            val toEnds = underlineEnds(layout, to, text) ?: return@forEach
            val (p1, p2) = nearestEnds(fromEnds, toEnds)
            if (p1 == p2) return@forEach

            val sameLine = p1.y == p2.y
            val depth = if (sameLine) 9f else 6f
            val baseY = maxOf(p1.y, p2.y) + depth
            val c1x = p1.x + (p2.x - p1.x) * 0.33f
            val c2x = p1.x + (p2.x - p1.x) * 0.67f
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                cubicTo(c1x, baseY, c2x, baseY, p2.x, p2.y)
            }
            drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
        }
    }
}

/**
 * 한 마킹(밑줄)의 왼끝/오른끝 화면 좌표. 밑줄은 글자 박스의 bottom 선에
 * 그려지므로 끝점 y 도 bottom 을 쓴다. 연결선을 단어 한가운데가 아니라
 * "밑줄의 양 끝"에서 잇기 위한 좌표다.
 */
private fun underlineEnds(
    layout: androidx.compose.ui.text.TextLayoutResult,
    markup: StudyMarkup,
    text: String
): Pair<Offset, Offset>? {
    if (text.isEmpty()) return null
    val start = markup.startOffset.coerceIn(0, text.length - 1)
    val lastChar = (markup.endOffset - 1).coerceIn(start, text.length - 1)
    val startBox = layout.getBoundingBox(start)
    val endBox = layout.getBoundingBox(lastChar)
    val left = Offset(startBox.left, startBox.bottom)
    val right = Offset(endBox.right, endBox.bottom)
    return left to right
}

/** 두 밑줄의 끝점 4쌍 중 거리(제곱)가 가장 짧은 끝점 쌍을 고른다. */
private fun nearestEnds(
    a: Pair<Offset, Offset>,
    b: Pair<Offset, Offset>
): Pair<Offset, Offset> {
    val aEnds = listOf(a.first, a.second)
    val bEnds = listOf(b.first, b.second)
    var best = aEnds[0] to bEnds[0]
    var bestDist = Float.MAX_VALUE
    for (pa in aEnds) for (pb in bEnds) {
        val dx = pa.x - pb.x
        val dy = pa.y - pb.y
        val d = dx * dx + dy * dy
        if (d < bestDist) {
            bestDist = d
            best = pa to pb
        }
    }
    return best
}

/**
 * 본문에 마킹을 시각화한다.
 *
 * 색상 배경 박스 대신 **타입별 색상 + 두꺼운 밑줄**을 사용해 문장 흐름이
 * 끊기지 않도록 한다. 같은 카테고리(주어/동사 등)의 마킹은 동일한 밑줄
 * 색상을 공유하므로, 사용자는 한눈에 주어와 동사의 위치를 짚을 수 있다.
 *
 * 연결선(주어→동사 등)은 [MarkupLinksOverlay] 가 별도 Canvas 로 그린다.
 */
internal fun buildMarkedVerseText(text: String, markups: List<StudyMarkup>): AnnotatedString {
    if (markups.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        markups.forEach { m ->
            val start = m.startOffset.coerceIn(0, text.length)
            val end = m.endOffset.coerceIn(start, text.length)
            if (end > start) {
                val color = MarkupTheme.colorFor(m.markType)
                addStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    start, end
                )
            }
        }
    }
}

// =================================================================================
// Observation panel (templates + free notes)
// =================================================================================

@Composable
private fun ObservationPanel(
    sessionId: String,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val observations by studyViewModel.observations.collectAsState()
    val byKey = remember(observations) { observations.associateBy { it.questionKey } }
    var freeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getTranslation("study_observation_templates_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LanguageManager.getTranslation("study_observation_templates_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(ObservationQuestion.TEMPLATE_KEYS, key = { it }) { key ->
            ObservationTemplateCard(
                sessionId = sessionId,
                questionKey = key,
                appLanguage = appLanguage,
                existing = byKey[key],
                studyViewModel = studyViewModel
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = LanguageManager.getTranslation("study_observation_free_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = freeInput,
                onValueChange = { freeInput = it },
                label = { Text(LanguageManager.getTranslation("study_observation_input_label", appLanguage)) },
                placeholder = { Text(LanguageManager.getTranslation("study_observation_input_hint", appLanguage)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .testTag("study_observation_input"),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    studyViewModel.addObservation(sessionId, freeInput)
                    freeInput = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_observation_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getTranslation("study_observation_add", appLanguage))
            }
        }

        val frees = observations.filter { it.questionKey == ObservationQuestion.FREE }
        if (frees.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_observation_empty", appLanguage)) }
        } else {
            items(frees, key = { it.id }) { obs ->
                ObservationFreeItem(obs, studyViewModel, appLanguage)
            }
        }
    }
}

@Composable
private fun ObservationTemplateCard(
    sessionId: String,
    questionKey: String,
    appLanguage: String,
    existing: StudyObservation?,
    studyViewModel: StudyViewModel
) {
    val questionText = LanguageManager.getTranslation("observation_q_$questionKey", appLanguage)
    var expanded by remember(questionKey, existing?.id) { mutableStateOf(false) }
    var draft by remember(questionKey, existing?.id) { mutableStateOf(existing?.answer ?: "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_observation_template_$questionKey")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (existing != null && existing.answer.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = LanguageManager.getTranslation("study_observation_answered", appLanguage),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (!expanded && existing != null && existing.answer.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = existing.answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) LanguageManager.getTranslation("common_cancel", appLanguage)
                        else LanguageManager.getTranslation(
                            if (existing == null || existing.answer.isBlank()) "common_answer" else "common_edit",
                            appLanguage
                        ),
                        fontSize = 12.sp
                    )
                }
            }
            if (expanded) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_observation_template_input_$questionKey"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        studyViewModel.saveObservationTemplate(
                            sessionId = sessionId,
                            questionKey = questionKey,
                            questionText = questionText,
                            answer = draft
                        )
                        expanded = false
                    }) {
                        Text(LanguageManager.getTranslation("common_save", appLanguage))
                    }
                    if (existing != null) {
                        OutlinedButton(onClick = {
                            studyViewModel.deleteObservation(existing)
                            expanded = false
                            draft = ""
                        }) {
                            Text(LanguageManager.getTranslation("common_delete", appLanguage))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationFreeItem(
    observation: StudyObservation,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(observation.id) { mutableStateOf(observation.answer) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (editing) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_observation_edit_${observation.id}"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        studyViewModel.updateObservation(observation.copy(answer = draft.trim()))
                        editing = false
                    }) {
                        Text(LanguageManager.getTranslation("common_save", appLanguage))
                    }
                    OutlinedButton(onClick = {
                        editing = false
                        draft = observation.answer
                    }) {
                        Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                    }
                }
            } else {
                Text(
                    text = observation.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { editing = true }) {
                        Text(LanguageManager.getTranslation("common_edit", appLanguage), fontSize = 12.sp)
                    }
                    IconButton(onClick = { studyViewModel.deleteObservation(observation) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// =================================================================================
// Markup / Structure panel
// =================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkupPanel(
    sessionId: String,
    verses: List<BibleVerse>,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val markups by studyViewModel.markups.collectAsState()
    val links by studyViewModel.markupLinks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<StudyMarkup?>(null) }
    var editingMemoFor by remember { mutableStateOf<StudyMarkup?>(null) }

    var pickedSubjectId by remember { mutableStateOf<String?>(null) }
    var pickedVerbId by remember { mutableStateOf<String?>(null) }
    var pickedLinkType by remember { mutableStateOf(LinkType.SUBJECT_VERB) }

    val subjectCandidates = remember(markups) { markups.filter { it.markType == MarkType.SUBJECT } }
    val verbCandidates = remember(markups) { markups.filter { it.markType == MarkType.VERB } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageManager.getTranslation("study_markup_section_title", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = LanguageManager.getTranslation("study_markup_section_desc", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.testTag("study_markup_add_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageManager.getTranslation("study_markup_add", appLanguage), fontSize = 12.sp)
                }
            }
        }

        if (markups.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_markup_empty", appLanguage)) }
        } else {
            items(markups, key = { it.id }) { m ->
                MarkupChipRow(
                    markup = m,
                    appLanguage = appLanguage,
                    onEditMemo = { editingMemoFor = m },
                    onChangeType = { studyViewModel.updateMarkupType(m.id, it) },
                    onDeleteRequest = { pendingDelete = m }
                )
            }
        }

        // ---------------- Structure linking ----------------
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = LanguageManager.getTranslation("study_structure_section_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LanguageManager.getTranslation("study_structure_section_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = LanguageManager.getTranslation("study_structure_link_type", appLanguage),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinkType.ALL.forEach { lt ->
                    FilterChip(
                        selected = pickedLinkType == lt,
                        onClick = { pickedLinkType = lt },
                        label = {
                            Text(
                                LanguageManager.getTranslation("link_type_$lt", appLanguage),
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = LanguageManager.getTranslation("study_structure_pick_from", appLanguage),
                style = MaterialTheme.typography.labelMedium
            )
            MarkupPickerRow(
                candidates = if (pickedLinkType == LinkType.SUBJECT_VERB) subjectCandidates else markups,
                pickedId = pickedSubjectId,
                appLanguage = appLanguage,
                onPick = { pickedSubjectId = if (pickedSubjectId == it) null else it },
                emptyKey = if (pickedLinkType == LinkType.SUBJECT_VERB) "study_structure_no_subjects" else "study_markup_empty",
                testTagPrefix = "study_structure_from"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LanguageManager.getTranslation("study_structure_pick_to", appLanguage),
                style = MaterialTheme.typography.labelMedium
            )
            MarkupPickerRow(
                candidates = if (pickedLinkType == LinkType.SUBJECT_VERB) verbCandidates else markups,
                pickedId = pickedVerbId,
                appLanguage = appLanguage,
                onPick = { pickedVerbId = if (pickedVerbId == it) null else it },
                emptyKey = if (pickedLinkType == LinkType.SUBJECT_VERB) "study_structure_no_verbs" else "study_markup_empty",
                testTagPrefix = "study_structure_to"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val from = pickedSubjectId
                    val to = pickedVerbId
                    if (from != null && to != null) {
                        studyViewModel.addMarkupLink(sessionId, from, to, pickedLinkType)
                        pickedSubjectId = null
                        pickedVerbId = null
                    }
                },
                enabled = pickedSubjectId != null && pickedVerbId != null && pickedSubjectId != pickedVerbId,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_structure_link_save")
            ) {
                Text(LanguageManager.getTranslation("study_structure_link_save", appLanguage))
            }
        }

        // ---------------- Structure summary ----------------
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = LanguageManager.getTranslation("study_structure_summary_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val subjectVerbLinks = links.filter { it.linkType == LinkType.SUBJECT_VERB }
        if (subjectVerbLinks.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_structure_summary_empty", appLanguage)) }
        } else {
            items(subjectVerbLinks, key = { it.id }) { link ->
                StructureLinkRow(link, markups, appLanguage, onDelete = { studyViewModel.deleteMarkupLink(link) })
            }
        }

        val otherLinks = links.filter { it.linkType != LinkType.SUBJECT_VERB }
        if (otherLinks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = LanguageManager.getTranslation("study_structure_other_links", appLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(otherLinks, key = { it.id }) { link ->
                StructureLinkRow(link, markups, appLanguage, onDelete = { studyViewModel.deleteMarkupLink(link) })
            }
        }
    }

    if (showDialog) {
        StudyMarkupDialog(
            appLanguage = appLanguage,
            verses = verses,
            initialVerse = verses.firstOrNull(),
            onDismiss = { showDialog = false },
            onSave = { verse, startOffset, endOffset, selectedText, markType, memo ->
                studyViewModel.addMarkup(
                    sessionId = sessionId,
                    verse = verse,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    selectedText = selectedText,
                    markType = markType,
                    memo = memo
                )
                showDialog = false
            }
        )
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(LanguageManager.getTranslation("study_markup_delete_title", appLanguage)) },
            text = {
                Text(
                    LanguageManager.getTranslation("study_markup_delete_desc", appLanguage)
                        .replace("%s", "\"${target.selectedText}\"")
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.deleteMarkup(target)
                    pendingDelete = null
                }) { Text(LanguageManager.getTranslation("common_delete", appLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }

    editingMemoFor?.let { target ->
        var memoDraft by remember(target.id) { mutableStateOf(target.memo.orEmpty()) }
        AlertDialog(
            onDismissRequest = { editingMemoFor = null },
            title = { Text(LanguageManager.getTranslation("study_markup_memo_title", appLanguage)) },
            text = {
                OutlinedTextField(
                    value = memoDraft,
                    onValueChange = { memoDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .testTag("study_markup_memo_edit_${target.id}"),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.updateMarkupMemo(target.id, memoDraft)
                    editingMemoFor = null
                }) { Text(LanguageManager.getTranslation("common_save", appLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { editingMemoFor = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkupChipRow(
    markup: StudyMarkup,
    appLanguage: String,
    onEditMemo: () -> Unit,
    onChangeType: (String) -> Unit,
    onDeleteRequest: () -> Unit
) {
    val color = MarkupTheme.colorFor(markup.markType)
    var showTypeMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_markup_item_${markup.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = MarkupTheme.highlightAlpha()))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = LanguageManager.getTranslation("mark_type_${markup.markType}", appLanguage),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${markup.chapter}:${markup.verse}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("study_markup_delete_${markup.id}")
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "“${markup.selectedText}”",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!markup.memo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✎ ${markup.memo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditMemo) {
                    Text(LanguageManager.getTranslation("study_markup_memo_edit", appLanguage), fontSize = 12.sp)
                }
                Box {
                    TextButton(onClick = { showTypeMenu = true }) {
                        Text(LanguageManager.getTranslation("study_markup_change_type", appLanguage), fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        MarkType.ALL.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = LanguageManager.getTranslation("mark_type_$type", appLanguage),
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onChangeType(type)
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkupPickerRow(
    candidates: List<StudyMarkup>,
    pickedId: String?,
    appLanguage: String,
    onPick: (String) -> Unit,
    emptyKey: String,
    testTagPrefix: String
) {
    if (candidates.isEmpty()) {
        EmptyHint(LanguageManager.getTranslation(emptyKey, appLanguage))
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        candidates.forEach { c ->
            val sel = pickedId == c.id
            val color = MarkupTheme.colorFor(c.markType)
            FilterChip(
                selected = sel,
                onClick = { onPick(c.id) },
                label = {
                    Text(
                        text = "${c.chapter}:${c.verse} ${c.selectedText.take(12)}",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("${testTagPrefix}_${c.id}")
            )
        }
    }
}

@Composable
private fun StructureLinkRow(
    link: StudyMarkupLink,
    markups: List<StudyMarkup>,
    appLanguage: String,
    onDelete: () -> Unit
) {
    val from = markups.firstOrNull { it.id == link.fromMarkupId }
    val to = markups.firstOrNull { it.id == link.toMarkupId }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_structure_link_${link.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LanguageManager.getTranslation("link_type_${link.linkType}", appLanguage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(80.dp)
            )
            Text(
                text = "${from?.selectedText ?: "?"}  →  ${to?.selectedText ?: "?"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// =================================================================================
// Division panel + character tags
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DivisionPanel(
    session: StudySession,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val divisions by studyViewModel.divisions.collectAsState()
    val tags by studyViewModel.characterTags.collectAsState()
    val tagsByTarget = remember(tags) {
        tags.filter { it.targetType == CharacterTagTarget.DIVISION }.groupBy { it.targetId }
    }

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var startCh by remember(session.id) { mutableStateOf(session.startChapter.toString()) }
    var startV by remember(session.id) { mutableStateOf(session.startVerse.toString()) }
    var endCh by remember(session.id) { mutableStateOf(session.endChapter.toString()) }
    var endV by remember(session.id) { mutableStateOf(session.endVerse.toString()) }

    var tagPickerFor by remember { mutableStateOf<StudyDivision?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getTranslation("study_division_form_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(LanguageManager.getTranslation("study_division_title_label", appLanguage)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_division_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberField(LanguageManager.getTranslation("study_division_start_chapter", appLanguage), startCh, { startCh = it }, modifier = Modifier.weight(1f))
                NumberField(LanguageManager.getTranslation("study_division_start_verse", appLanguage), startV, { startV = it }, modifier = Modifier.weight(1f))
                NumberField(LanguageManager.getTranslation("study_division_end_chapter", appLanguage), endCh, { endCh = it }, modifier = Modifier.weight(1f))
                NumberField(LanguageManager.getTranslation("study_division_end_verse", appLanguage), endV, { endV = it }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text(LanguageManager.getTranslation("study_division_summary_label", appLanguage)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val sc = startCh.toIntOrNull() ?: session.startChapter
                    val sv = startV.toIntOrNull() ?: session.startVerse
                    val ec = endCh.toIntOrNull() ?: session.endChapter
                    val ev = endV.toIntOrNull() ?: session.endVerse
                    studyViewModel.addDivision(
                        sessionId = session.id,
                        title = title,
                        startChapter = sc,
                        startVerse = sv,
                        endChapter = ec,
                        endVerse = ev,
                        summary = summary
                    )
                    title = ""
                    summary = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_division_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getTranslation("study_division_add", appLanguage))
            }
        }

        if (divisions.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_division_empty", appLanguage)) }
        } else {
            items(divisions, key = { it.id }) { div ->
                DivisionItem(
                    division = div,
                    tags = tagsByTarget[div.id].orEmpty(),
                    studyViewModel = studyViewModel,
                    appLanguage = appLanguage,
                    onAddTag = { tagPickerFor = div }
                )
            }
        }
    }

    tagPickerFor?.let { div ->
        CharacterTagPickerDialog(
            appLanguage = appLanguage,
            onPick = { tag ->
                studyViewModel.addCharacterTag(
                    sessionId = session.id,
                    targetType = CharacterTagTarget.DIVISION,
                    targetId = div.id,
                    tag = tag
                )
                tagPickerFor = null
            },
            onDismiss = { tagPickerFor = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DivisionItem(
    division: StudyDivision,
    tags: List<StudyCharacterTag>,
    studyViewModel: StudyViewModel,
    appLanguage: String,
    onAddTag: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = division.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${division.startChapter}:${division.startVerse} ~ ${division.endChapter}:${division.endVerse}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { studyViewModel.deleteDivision(division) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (!division.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = division.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LanguageManager.getTranslation("study_character_tags_label", appLanguage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { studyViewModel.deleteCharacterTag(tag) },
                        label = {
                            Text(
                                LanguageManager.getTranslation("character_tag_${tag.tag}", appLanguage),
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("study_character_tag_${tag.id}")
                    )
                }
                AssistChip(
                    onClick = onAddTag,
                    label = {
                        Text(
                            LanguageManager.getTranslation("study_character_tag_add", appLanguage),
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.testTag("study_character_tag_add_${division.id}")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacterTagPickerDialog(
    appLanguage: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LanguageManager.getTranslation("study_character_tag_dialog_title", appLanguage)) },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CharacterTagType.ALL.forEach { tag ->
                    AssistChip(
                        onClick = { onPick(tag) },
                        label = {
                            Text(
                                LanguageManager.getTranslation("character_tag_$tag", appLanguage),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("study_character_tag_picker_$tag")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageManager.getTranslation("common_close", appLanguage))
            }
        }
    )
}

// =================================================================================
// Theme + Application + Others (unchanged from Goal 1 baseline)
// =================================================================================

@Composable
private fun ThemePanel(
    session: StudySession,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val checks by studyViewModel.themeChecks.collectAsState()
    val checksByKey = remember(checks) { checks.associateBy { it.checkKey } }
    var theme by remember(session.id) { mutableStateOf(session.mainTheme) }
    var proposition by remember(session.id) { mutableStateOf(session.mainPropositionMemo) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = LanguageManager.getTranslation("study_theme_explainer", appLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = theme,
            onValueChange = { theme = it },
            label = { Text(LanguageManager.getTranslation("study_theme_label", appLanguage)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .testTag("study_theme_input"),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = proposition,
            onValueChange = { proposition = it },
            label = { Text(LanguageManager.getTranslation("study_theme_proposition_label", appLanguage)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .testTag("study_theme_proposition_input"),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                studyViewModel.updateSessionMeta(
                    sessionId = session.id,
                    mainTheme = theme,
                    mainPropositionMemo = proposition
                )
                studyViewModel.showMessage(LanguageManager.getTranslation("study_theme_saved", appLanguage))
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_theme_save")
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(LanguageManager.getTranslation("common_save", appLanguage))
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = LanguageManager.getTranslation("study_theme_check_title", appLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = LanguageManager.getTranslation("study_theme_check_desc", appLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThemeCheckKey.ALL.forEach { key ->
            ThemeCheckRow(
                sessionId = session.id,
                checkKey = key,
                existing = checksByKey[key],
                appLanguage = appLanguage,
                studyViewModel = studyViewModel
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ThemeCheckRow(
    sessionId: String,
    checkKey: String,
    existing: StudyThemeCheck?,
    appLanguage: String,
    studyViewModel: StudyViewModel
) {
    val questionText = LanguageManager.getTranslation("theme_check_$checkKey", appLanguage)
    var isChecked by remember(checkKey, existing?.id, existing?.isChecked) {
        mutableStateOf(existing?.isChecked ?: false)
    }
    var noteDraft by remember(checkKey, existing?.id) { mutableStateOf(existing?.note ?: "") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_theme_check_$checkKey")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        isChecked = it
                        studyViewModel.saveThemeCheck(sessionId, checkKey, it, noteDraft)
                    },
                    modifier = Modifier.testTag("study_theme_check_box_$checkKey")
                )
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = LanguageManager.getTranslation(
                            if (expanded) "common_close" else "study_theme_check_note", appLanguage
                        ),
                        fontSize = 12.sp
                    )
                }
            }
            if (expanded) {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text(LanguageManager.getTranslation("study_theme_check_note_hint", appLanguage)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_theme_check_note_$checkKey"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        studyViewModel.saveThemeCheck(sessionId, checkKey, isChecked, noteDraft)
                        expanded = false
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(LanguageManager.getTranslation("common_save", appLanguage))
                }
            }
        }
    }
}

@Composable
private fun ApplicationPanel(
    sessionId: String,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val applicationNote by studyViewModel.applicationNote.collectAsState()

    var truth by remember(applicationNote?.id) { mutableStateOf(applicationNote?.truthStatement ?: "") }
    var mirror by remember(applicationNote?.id) { mutableStateOf(applicationNote?.mirrorStatement ?: "") }
    var adjust by remember(applicationNote?.id) { mutableStateOf(applicationNote?.adjustmentStatement ?: "") }
    var action by remember(applicationNote?.id) { mutableStateOf(applicationNote?.actionPlan ?: "") }
    var dueDate by remember(applicationNote?.id) { mutableStateOf(applicationNote?.dueDate ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ApplicationField(
            label = LanguageManager.getTranslation("study_application_truth_label", appLanguage),
            hint = LanguageManager.getTranslation("study_application_truth_hint", appLanguage),
            value = truth,
            onChange = { truth = it },
            tag = "study_application_truth"
        )
        Spacer(modifier = Modifier.height(10.dp))
        ApplicationField(
            label = LanguageManager.getTranslation("study_application_mirror_label", appLanguage),
            hint = LanguageManager.getTranslation("study_application_mirror_hint", appLanguage),
            value = mirror,
            onChange = { mirror = it },
            tag = "study_application_mirror"
        )
        Spacer(modifier = Modifier.height(10.dp))
        ApplicationField(
            label = LanguageManager.getTranslation("study_application_adjust_label", appLanguage),
            hint = LanguageManager.getTranslation("study_application_adjust_hint", appLanguage),
            value = adjust,
            onChange = { adjust = it },
            tag = "study_application_adjust"
        )
        Spacer(modifier = Modifier.height(10.dp))
        ApplicationField(
            label = LanguageManager.getTranslation("study_application_action_label", appLanguage),
            hint = LanguageManager.getTranslation("study_application_action_hint", appLanguage),
            value = action,
            onChange = { action = it },
            tag = "study_application_action"
        )
        Spacer(modifier = Modifier.height(10.dp))
        DueDatePicker(
            dueDate = dueDate,
            appLanguage = appLanguage,
            onChange = { dueDate = it }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = applicationNote?.practiced == true,
                onCheckedChange = { studyViewModel.toggleApplicationPracticed(sessionId) },
                modifier = Modifier.testTag("study_application_practiced_box")
            )
            Text(
                text = LanguageManager.getTranslation("study_application_practiced_label", appLanguage),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                studyViewModel.saveApplication(
                    sessionId = sessionId,
                    truth = truth,
                    mirror = mirror,
                    adjustment = adjust,
                    actionPlan = action,
                    dueDate = dueDate
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_application_save")
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(LanguageManager.getTranslation("common_save", appLanguage))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePicker(
    dueDate: String,
    appLanguage: String,
    onChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayText = dueDate.takeIf { it.isNotBlank() }
        ?: LanguageManager.getTranslation("study_application_due_unset", appLanguage)

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_application_due_button")
    ) {
        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = LanguageManager.getTranslation("study_application_due_label", appLanguage) + ": " + displayText,
            fontSize = 13.sp
        )
    }
    if (dueDate.isNotBlank()) {
        TextButton(
            onClick = { onChange("") },
            modifier = Modifier.testTag("study_application_due_clear")
        ) {
            Text(LanguageManager.getTranslation("study_application_due_clear", appLanguage), fontSize = 11.sp)
        }
    }

    if (showPicker) {
        val initialMillis = parseIsoDateToMillis(dueDate) ?: System.currentTimeMillis()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = state.selectedDateMillis
                    if (picked != null) {
                        onChange(formatMillisToIsoDate(picked))
                    }
                    showPicker = false
                }) {
                    Text(LanguageManager.getTranslation("common_save", appLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun parseIsoDateToMillis(iso: String): Long? {
    if (iso.isBlank()) return null
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(iso)?.time
    } catch (_: Exception) {
        null
    }
}

private fun formatMillisToIsoDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(java.util.Date(millis))
}

@Composable
private fun ApplicationField(
    label: String,
    hint: String,
    value: String,
    onChange: (String) -> Unit,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(hint, fontSize = 12.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag(tag),
        minLines = 2
    )
}

// =================================================================================
// Goal 3 panels: Interpretation / Proposition / Outline / Report
// =================================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterpretationPanel(
    sessionId: String,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val notes by studyViewModel.interpretations.collectAsState()
    val markups by studyViewModel.markups.collectAsState()
    var pendingDelete by remember { mutableStateOf<StudyInterpretation?>(null) }

    var keyword by remember { mutableStateOf("") }
    var plain by remember { mutableStateOf("") }
    var ctx by remember { mutableStateOf("") }
    var evidence by remember { mutableStateOf("") }
    var refs by remember { mutableStateOf("") }
    var conclusion by remember { mutableStateOf("") }
    var linkedMarkupId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getTranslation("study_interpretation_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LanguageManager.getTranslation("study_interpretation_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = keyword, onValueChange = { keyword = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_keyword", appLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("study_interp_keyword")
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = plain, onValueChange = { plain = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_plain", appLanguage)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("study_interp_plain"),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = ctx, onValueChange = { ctx = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_context", appLanguage)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("study_interp_context"),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = evidence, onValueChange = { evidence = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_evidence", appLanguage)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("study_interp_evidence"),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = refs, onValueChange = { refs = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_refs", appLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("study_interp_refs")
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = conclusion, onValueChange = { conclusion = it },
                label = { Text(LanguageManager.getTranslation("study_interpretation_conclusion", appLanguage)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("study_interp_conclusion"),
                minLines = 2
            )
            if (markups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageManager.getTranslation("study_interpretation_link_markup", appLanguage),
                    style = MaterialTheme.typography.labelMedium
                )
                MarkupSingleSelect(
                    markups = markups,
                    pickedId = linkedMarkupId,
                    appLanguage = appLanguage,
                    onPick = { linkedMarkupId = if (linkedMarkupId == it) null else it },
                    testTagPrefix = "study_interp_link"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    studyViewModel.addInterpretation(
                        sessionId = sessionId,
                        keyword = keyword,
                        plainMeaning = plain,
                        contextualMeaning = ctx,
                        evidence = evidence,
                        crossRefs = refs,
                        conclusion = conclusion,
                        linkedMarkupId = linkedMarkupId
                    )
                    keyword = ""; plain = ""; ctx = ""; evidence = ""; refs = ""; conclusion = ""
                    linkedMarkupId = null
                },
                modifier = Modifier.fillMaxWidth().testTag("study_interp_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getTranslation("study_interpretation_add", appLanguage))
            }
        }

        if (notes.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_interpretation_empty", appLanguage)) }
        } else {
            items(notes, key = { it.id }) { note ->
                InterpretationCard(
                    note = note,
                    appLanguage = appLanguage,
                    markups = markups,
                    onUpdate = { studyViewModel.updateInterpretation(it) },
                    onDeleteRequest = { pendingDelete = note }
                )
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(LanguageManager.getTranslation("study_interpretation_delete_title", appLanguage)) },
            text = { Text(LanguageManager.getTranslation("study_interpretation_delete_desc", appLanguage)) },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.deleteInterpretation(target)
                    pendingDelete = null
                }) { Text(LanguageManager.getTranslation("common_delete", appLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterpretationCard(
    note: StudyInterpretation,
    appLanguage: String,
    markups: List<StudyMarkup>,
    onUpdate: (StudyInterpretation) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var keyword by remember(note.id) { mutableStateOf(note.keyword) }
    var plain by remember(note.id) { mutableStateOf(note.plainMeaning) }
    var ctx by remember(note.id) { mutableStateOf(note.contextualMeaning) }
    var evidence by remember(note.id) { mutableStateOf(note.evidence) }
    var refs by remember(note.id) { mutableStateOf(note.crossRefs) }
    var conclusion by remember(note.id) { mutableStateOf(note.conclusion) }
    var linkedMarkupId by remember(note.id) { mutableStateOf(note.linkedMarkupId) }
    val linkedMarkup = note.linkedMarkupId?.let { id -> markups.firstOrNull { it.id == id } }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("study_interp_card_${note.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (editing) {
                LabeledField(LanguageManager.getTranslation("study_interpretation_keyword", appLanguage), keyword) { keyword = it }
                LabeledField(LanguageManager.getTranslation("study_interpretation_plain", appLanguage), plain) { plain = it }
                LabeledField(LanguageManager.getTranslation("study_interpretation_context", appLanguage), ctx) { ctx = it }
                LabeledField(LanguageManager.getTranslation("study_interpretation_evidence", appLanguage), evidence) { evidence = it }
                LabeledField(LanguageManager.getTranslation("study_interpretation_refs", appLanguage), refs) { refs = it }
                LabeledField(LanguageManager.getTranslation("study_interpretation_conclusion", appLanguage), conclusion) { conclusion = it }
                if (markups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageManager.getTranslation("study_interpretation_link_markup", appLanguage),
                        style = MaterialTheme.typography.labelMedium
                    )
                    MarkupSingleSelect(
                        markups = markups,
                        pickedId = linkedMarkupId,
                        appLanguage = appLanguage,
                        onPick = { linkedMarkupId = if (linkedMarkupId == it) null else it },
                        testTagPrefix = "study_interp_link_edit"
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onUpdate(
                            note.copy(
                                keyword = keyword.trim(),
                                plainMeaning = plain.trim(),
                                contextualMeaning = ctx.trim(),
                                evidence = evidence.trim(),
                                crossRefs = refs.trim(),
                                conclusion = conclusion.trim(),
                                linkedMarkupId = linkedMarkupId
                            )
                        )
                        editing = false
                    }) { Text(LanguageManager.getTranslation("common_save", appLanguage)) }
                    OutlinedButton(onClick = { editing = false }) {
                        Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                    }
                }
            } else {
                Text(
                    text = note.keyword.ifBlank { "(no keyword)" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (linkedMarkup != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinkedMarkupChip(linkedMarkup, appLanguage)
                }
                if (note.plainMeaning.isNotBlank()) ReadOnlyLine("• ${note.plainMeaning}")
                if (note.contextualMeaning.isNotBlank()) ReadOnlyLine("• ${note.contextualMeaning}")
                if (note.evidence.isNotBlank()) ReadOnlyLine("⚓ ${note.evidence}")
                if (note.crossRefs.isNotBlank()) ReadOnlyLine("↪ ${note.crossRefs}")
                if (note.conclusion.isNotBlank()) ReadOnlyLine("✦ ${note.conclusion}", primary = true)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { editing = true }) {
                        Text(LanguageManager.getTranslation("common_edit", appLanguage), fontSize = 12.sp)
                    }
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 명시 링크 요약을 표시할 때 사용하는 색상 칩. MarkupTheme 색상을 배경에 입혀
 * 어떤 종류(주어/동사/접속사 등)의 마킹과 연결됐는지 시각적으로 즉시 식별되게 한다.
 */
@Composable
private fun LinkedMarkupChip(markup: StudyMarkup, appLanguage: String) {
    val color = MarkupTheme.colorFor(markup.markType)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = MarkupTheme.highlightAlpha()))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("linked_markup_chip_${markup.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = LanguageManager.getTranslation("mark_type_${markup.markType}", appLanguage),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${markup.chapter}:${markup.verse} ${markup.selectedText}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkedMarkupChipRow(markups: List<StudyMarkup>, appLanguage: String) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        markups.forEach { LinkedMarkupChip(it, appLanguage) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkedDivisionChipRow(divisions: List<StudyDivision>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        divisions.forEach { d ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("linked_division_chip_${d.id}")
            ) {
                Text(
                    text = "${d.startChapter}:${d.startVerse} ${d.title}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkupSingleSelect(
    markups: List<StudyMarkup>,
    pickedId: String?,
    appLanguage: String,
    onPick: (String) -> Unit,
    testTagPrefix: String
) {
    if (markups.isEmpty()) {
        EmptyHint(LanguageManager.getTranslation("study_markup_empty", appLanguage))
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        markups.forEach { m ->
            FilterChip(
                selected = pickedId == m.id,
                onClick = { onPick(m.id) },
                label = {
                    Text(
                        text = "${m.chapter}:${m.verse} ${m.selectedText.take(12)}",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MarkupTheme.colorFor(m.markType),
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("${testTagPrefix}_${m.id}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarkupMultiSelect(
    markups: List<StudyMarkup>,
    pickedIds: Set<String>,
    appLanguage: String,
    onToggle: (String) -> Unit,
    testTagPrefix: String
) {
    if (markups.isEmpty()) {
        EmptyHint(LanguageManager.getTranslation("study_markup_empty", appLanguage))
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        markups.forEach { m ->
            FilterChip(
                selected = pickedIds.contains(m.id),
                onClick = { onToggle(m.id) },
                label = {
                    Text(
                        text = "${m.chapter}:${m.verse} ${m.selectedText.take(12)}",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MarkupTheme.colorFor(m.markType),
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("${testTagPrefix}_${m.id}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DivisionMultiSelect(
    divisions: List<StudyDivision>,
    pickedIds: Set<String>,
    onToggle: (String) -> Unit,
    testTagPrefix: String
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        divisions.forEach { d ->
            FilterChip(
                selected = pickedIds.contains(d.id),
                onClick = { onToggle(d.id) },
                label = {
                    Text(
                        text = "${d.startChapter}:${d.startVerse} ${d.title.take(14)}",
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("${testTagPrefix}_${d.id}")
            )
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = 4
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ReadOnlyLine(text: String, primary: Boolean = false) {
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropositionPanel(
    sessionId: String,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val propositions by studyViewModel.propositions.collectAsState()
    val markups by studyViewModel.markups.collectAsState()
    val divisions by studyViewModel.divisions.collectAsState()
    var sentence by remember { mutableStateOf("") }
    var refs by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(PropositionStatus.DRAFT) }
    var pickedMarkupIds by remember { mutableStateOf(setOf<String>()) }
    var pickedDivisionIds by remember { mutableStateOf(setOf<String>()) }
    var pendingDelete by remember { mutableStateOf<StudyProposition?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getTranslation("study_proposition_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LanguageManager.getTranslation("study_proposition_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = sentence,
                onValueChange = { sentence = it },
                label = { Text(LanguageManager.getTranslation("study_proposition_sentence", appLanguage)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .testTag("study_proposition_sentence"),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = refs,
                onValueChange = { refs = it },
                label = { Text(LanguageManager.getTranslation("study_proposition_refs", appLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("study_proposition_refs")
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = LanguageManager.getTranslation("study_proposition_status", appLanguage),
                style = MaterialTheme.typography.labelMedium
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PropositionStatus.ALL.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = {
                            Text(
                                LanguageManager.getTranslation("proposition_status_$s", appLanguage),
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }
            if (markups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageManager.getTranslation("study_proposition_link_markups", appLanguage),
                    style = MaterialTheme.typography.labelMedium
                )
                MarkupMultiSelect(
                    markups = markups,
                    pickedIds = pickedMarkupIds,
                    appLanguage = appLanguage,
                    onToggle = { id ->
                        pickedMarkupIds = if (pickedMarkupIds.contains(id))
                            pickedMarkupIds - id else pickedMarkupIds + id
                    },
                    testTagPrefix = "study_prop_link_mk"
                )
            }
            if (divisions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageManager.getTranslation("study_proposition_link_divisions", appLanguage),
                    style = MaterialTheme.typography.labelMedium
                )
                DivisionMultiSelect(
                    divisions = divisions,
                    pickedIds = pickedDivisionIds,
                    onToggle = { id ->
                        pickedDivisionIds = if (pickedDivisionIds.contains(id))
                            pickedDivisionIds - id else pickedDivisionIds + id
                    },
                    testTagPrefix = "study_prop_link_div"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    studyViewModel.addProposition(
                        sessionId = sessionId,
                        sentence = sentence,
                        supportingRefs = refs,
                        reviewStatus = status,
                        linkedMarkupIds = LinkedIds.encode(pickedMarkupIds.toList()),
                        linkedDivisionIds = LinkedIds.encode(pickedDivisionIds.toList())
                    )
                    sentence = ""; refs = ""; status = PropositionStatus.DRAFT
                    pickedMarkupIds = emptySet(); pickedDivisionIds = emptySet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_proposition_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getTranslation("study_proposition_add", appLanguage))
            }
        }

        if (propositions.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_proposition_empty", appLanguage)) }
        } else {
            items(propositions, key = { it.id }) { p ->
                PropositionCard(
                    proposition = p,
                    appLanguage = appLanguage,
                    markups = markups,
                    divisions = divisions,
                    onUpdate = { studyViewModel.updateProposition(it) },
                    onDeleteRequest = { pendingDelete = p }
                )
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(LanguageManager.getTranslation("study_proposition_delete_title", appLanguage)) },
            text = {
                Text(
                    LanguageManager.getTranslation("study_proposition_delete_desc", appLanguage)
                        .replace("%s", target.sentence.take(40))
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.deleteProposition(target)
                    pendingDelete = null
                }) { Text(LanguageManager.getTranslation("common_delete", appLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropositionCard(
    proposition: StudyProposition,
    appLanguage: String,
    markups: List<StudyMarkup>,
    divisions: List<StudyDivision>,
    onUpdate: (StudyProposition) -> Unit,
    onDeleteRequest: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var sentence by remember(proposition.id) { mutableStateOf(proposition.sentence) }
    var refs by remember(proposition.id) { mutableStateOf(proposition.supportingRefs) }
    var status by remember(proposition.id) { mutableStateOf(proposition.reviewStatus) }
    var linkedMkIds by remember(proposition.id) {
        mutableStateOf(LinkedIds.decode(proposition.linkedMarkupIds).toSet())
    }
    var linkedDivIds by remember(proposition.id) {
        mutableStateOf(LinkedIds.decode(proposition.linkedDivisionIds).toSet())
    }
    val linkedMarkups = LinkedIds.decode(proposition.linkedMarkupIds)
        .mapNotNull { id -> markups.firstOrNull { it.id == id } }
    val linkedDivisions = LinkedIds.decode(proposition.linkedDivisionIds)
        .mapNotNull { id -> divisions.firstOrNull { it.id == id } }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("study_proposition_card_${proposition.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (editing) {
                OutlinedTextField(
                    value = sentence,
                    onValueChange = { sentence = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = refs,
                    onValueChange = { refs = it },
                    label = { Text(LanguageManager.getTranslation("study_proposition_refs", appLanguage)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PropositionStatus.ALL.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = {
                                Text(
                                    LanguageManager.getTranslation("proposition_status_$s", appLanguage),
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
                if (markups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageManager.getTranslation("study_proposition_link_markups", appLanguage),
                        style = MaterialTheme.typography.labelMedium
                    )
                    MarkupMultiSelect(
                        markups = markups,
                        pickedIds = linkedMkIds,
                        appLanguage = appLanguage,
                        onToggle = { id ->
                            linkedMkIds = if (linkedMkIds.contains(id)) linkedMkIds - id else linkedMkIds + id
                        },
                        testTagPrefix = "study_prop_link_mk_edit"
                    )
                }
                if (divisions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageManager.getTranslation("study_proposition_link_divisions", appLanguage),
                        style = MaterialTheme.typography.labelMedium
                    )
                    DivisionMultiSelect(
                        divisions = divisions,
                        pickedIds = linkedDivIds,
                        onToggle = { id ->
                            linkedDivIds = if (linkedDivIds.contains(id)) linkedDivIds - id else linkedDivIds + id
                        },
                        testTagPrefix = "study_prop_link_div_edit"
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onUpdate(
                            proposition.copy(
                                sentence = sentence.trim(),
                                supportingRefs = refs.trim(),
                                reviewStatus = status,
                                linkedMarkupIds = LinkedIds.encode(linkedMkIds.toList()),
                                linkedDivisionIds = LinkedIds.encode(linkedDivIds.toList())
                            )
                        )
                        editing = false
                    }) { Text(LanguageManager.getTranslation("common_save", appLanguage)) }
                    OutlinedButton(onClick = { editing = false }) {
                        Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = LanguageManager.getTranslation("proposition_status_${proposition.reviewStatus}", appLanguage),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { editing = true }) {
                        Text(LanguageManager.getTranslation("common_edit", appLanguage), fontSize = 12.sp)
                    }
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = proposition.sentence,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (proposition.supportingRefs.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "↪ ${proposition.supportingRefs}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (linkedMarkups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinkedMarkupChipRow(linkedMarkups, appLanguage)
                }
                if (linkedDivisions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinkedDivisionChipRow(linkedDivisions)
                }
            }
        }
    }
}

@Composable
private fun OutlinePanel(
    session: StudySession,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val nodes by studyViewModel.outlineNodes.collectAsState()
    val divisions by studyViewModel.divisions.collectAsState()

    var title by remember { mutableStateOf("") }
    var level by remember { mutableStateOf(0) }
    var verseRange by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<StudyOutlineNode?>(null) }
    var pickedDivisionId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = LanguageManager.getTranslation("study_outline_title", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LanguageManager.getTranslation("study_outline_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(LanguageManager.getTranslation("study_outline_node_title", appLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("study_outline_title_input")
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberField(
                    label = LanguageManager.getTranslation("study_outline_level", appLanguage),
                    value = level.toString(),
                    onChange = { level = it.toIntOrNull()?.coerceIn(0, 3) ?: 0 },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = verseRange,
                    onValueChange = { verseRange = it },
                    label = { Text(LanguageManager.getTranslation("study_outline_range", appLanguage)) },
                    singleLine = true,
                    modifier = Modifier.weight(2f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text(LanguageManager.getTranslation("study_outline_summary", appLanguage)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                minLines = 2
            )
            if (divisions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageManager.getTranslation("study_outline_link_division", appLanguage),
                    style = MaterialTheme.typography.labelMedium
                )
                DivisionLinkPicker(
                    divisions = divisions,
                    pickedId = pickedDivisionId,
                    onPick = { pickedDivisionId = if (pickedDivisionId == it) null else it }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    studyViewModel.addOutlineNode(
                        sessionId = session.id,
                        title = title,
                        level = level,
                        verseRange = verseRange,
                        summary = summary,
                        linkedDivisionId = pickedDivisionId
                    )
                    title = ""; level = 0; verseRange = ""; summary = ""; pickedDivisionId = null
                },
                modifier = Modifier.fillMaxWidth().testTag("study_outline_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(LanguageManager.getTranslation("study_outline_add", appLanguage))
            }
        }

        if (nodes.isEmpty()) {
            item { EmptyHint(LanguageManager.getTranslation("study_outline_empty", appLanguage)) }
        } else {
            items(nodes, key = { it.id }) { node ->
                OutlineNodeRow(
                    node = node,
                    divisions = divisions,
                    appLanguage = appLanguage,
                    onDeleteRequest = { pendingDelete = node }
                )
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(LanguageManager.getTranslation("study_outline_delete_title", appLanguage)) },
            text = {
                Text(
                    LanguageManager.getTranslation("study_outline_delete_desc", appLanguage)
                        .replace("%s", target.title)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.deleteOutlineNode(target)
                    pendingDelete = null
                }) { Text(LanguageManager.getTranslation("common_delete", appLanguage)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DivisionLinkPicker(
    divisions: List<StudyDivision>,
    pickedId: String?,
    onPick: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        divisions.forEach { d ->
            FilterChip(
                selected = pickedId == d.id,
                onClick = { onPick(d.id) },
                label = {
                    Text(
                        text = "${d.startChapter}:${d.startVerse} ${d.title.take(16)}",
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.testTag("study_outline_div_${d.id}")
            )
        }
    }
}

@Composable
private fun OutlineNodeRow(
    node: StudyOutlineNode,
    divisions: List<StudyDivision>,
    appLanguage: String,
    onDeleteRequest: () -> Unit
) {
    val indentDp = (node.level.coerceIn(0, 3) * 16).dp
    val linkedTitle = node.linkedDivisionId?.let { id -> divisions.firstOrNull { it.id == id }?.title }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentDp)
            .testTag("study_outline_node_${node.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "L${node.level} • ${node.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteRequest) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (!node.verseRange.isNullOrBlank()) {
                Text(
                    text = node.verseRange,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!node.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = node.summary, style = MaterialTheme.typography.bodySmall)
            }
            if (linkedTitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⤷ ${LanguageManager.getTranslation("study_outline_linked_to", appLanguage)} $linkedTitle",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReportPanel(
    session: StudySession,
    studyViewModel: StudyViewModel,
    appLanguage: String
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var preview by remember { mutableStateOf<String?>(null) }
    val pdfExporter = remember { com.veritasbible.app.study.report.StudyPdfExporter() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = LanguageManager.getTranslation("study_report_title", appLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = LanguageManager.getTranslation("study_report_desc", appLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                studyViewModel.buildMarkdownReport(appLanguage) { _, markdown ->
                    preview = markdown
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_report_generate")
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(LanguageManager.getTranslation("study_report_generate", appLanguage))
        }

        preview?.let { md ->
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(ComposeAnnotatedString(md))
                        studyViewModel.showMessage(LanguageManager.getTranslation("study_report_copied", appLanguage))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_report_copy")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageManager.getTranslation("study_report_copy", appLanguage), fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, session.title)
                            putExtra(Intent.EXTRA_TEXT, md)
                            type = "text/markdown"
                        }
                        val chooser = Intent.createChooser(
                            sendIntent,
                            LanguageManager.getTranslation("study_report_share_title", appLanguage)
                        )
                        context.startActivity(chooser)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_report_share")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageManager.getTranslation("study_report_share", appLanguage), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        try {
                            val file = pdfExporter.writeToCache(context, md, session.title)
                            val uri = pdfExporter.shareUri(context, file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, session.title)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    LanguageManager.getTranslation("study_report_share_title", appLanguage)
                                )
                            )
                        } catch (e: Exception) {
                            studyViewModel.showMessage(
                                LanguageManager.getTranslation("study_report_pdf_failed", appLanguage) +
                                    ": " + e.localizedMessage
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_report_pdf")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageManager.getTranslation("study_report_pdf", appLanguage), fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = {
                        try {
                            com.veritasbible.app.study.report.startStudyPrintJob(
                                context, md, session.title
                            )
                        } catch (e: Exception) {
                            studyViewModel.showMessage(
                                LanguageManager.getTranslation("study_report_print_failed", appLanguage) +
                                    ": " + e.localizedMessage
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("study_report_print")
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageManager.getTranslation("study_report_print", appLanguage), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = LanguageManager.getTranslation("study_report_preview_title", appLanguage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_report_preview_card")
            ) {
                Text(
                    text = md,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() }) },
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
