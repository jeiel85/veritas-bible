package com.veritasbible.app.study.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritasbible.app.study.data.StudySession
import com.veritasbible.app.ui.BibleViewModel
import com.veritasbible.app.util.LanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyListScreen(
    bibleViewModel: BibleViewModel,
    studyViewModel: StudyViewModel,
    modifier: Modifier = Modifier,
    onOpenSession: (String) -> Unit,
    onStartNewFromReader: () -> Unit
) {
    val sessions by studyViewModel.sessions.collectAsState()
    val appLanguage by bibleViewModel.appLanguage.collectAsState()
    var pendingDelete by remember { mutableStateOf<StudySession?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = LanguageManager.getTranslation("study_title", appLanguage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageManager.getTranslation("study_subtitle", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartNewFromReader,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(LanguageManager.getTranslation("study_create_from_reader", appLanguage)) },
                modifier = Modifier.testTag("study_create_fab")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = LanguageManager.getTranslation("study_empty_title", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = LanguageManager.getTranslation("study_empty_desc", appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onStartNewFromReader) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageManager.getTranslation("study_go_to_reader", appLanguage))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        appLanguage = appLanguage,
                        onClick = { onOpenSession(session.id) },
                        onDeleteRequest = { pendingDelete = session }
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(LanguageManager.getTranslation("study_delete_confirm_title", appLanguage)) },
            text = {
                Text(
                    LanguageManager.getTranslation("study_delete_confirm_desc", appLanguage)
                        .replace("%s", target.title)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    studyViewModel.deleteSession(target.id)
                    pendingDelete = null
                }) {
                    Text(LanguageManager.getTranslation("common_delete", appLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(LanguageManager.getTranslation("common_cancel", appLanguage))
                }
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: StudySession,
    appLanguage: String,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val df = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val rangeText = formatRange(session)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .testTag("study_session_card_${session.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rangeText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("study_session_delete_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = LanguageManager.getTranslation("common_delete", appLanguage),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (session.mainTheme.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = LanguageManager.getTranslation("study_main_theme_label", appLanguage) + ": " + session.mainTheme,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(
                            text = stageLabel(session.currentStage, appLanguage),
                            fontSize = 11.sp
                        )
                    }
                )
                Text(
                    text = df.format(Date(session.updatedAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

internal fun formatRange(session: StudySession): String {
    val start = "${session.book} ${session.startChapter}:${session.startVerse}"
    val end = if (session.startChapter == session.endChapter && session.startVerse == session.endVerse) {
        ""
    } else if (session.startChapter == session.endChapter) {
        "-${session.endVerse}"
    } else {
        " ~ ${session.endChapter}:${session.endVerse}"
    }
    return start + end
}

internal fun stageLabel(stage: String, appLanguage: String): String {
    val key = "stage_$stage"
    return LanguageManager.getTranslation(key, appLanguage)
}
