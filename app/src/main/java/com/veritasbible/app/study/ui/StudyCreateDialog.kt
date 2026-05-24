package com.veritasbible.app.study.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.veritasbible.app.ui.BibleViewModel
import com.veritasbible.app.ui.screens.getBookTranslation
import com.veritasbible.app.util.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyCreateDialog(
    bibleViewModel: BibleViewModel,
    studyViewModel: StudyViewModel,
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit
) {
    val appLanguage by bibleViewModel.appLanguage.collectAsState()
    val currentBook by bibleViewModel.currentBook.collectAsState()
    val currentChapter by bibleViewModel.currentChapter.collectAsState()
    val currentVerses by bibleViewModel.currentVerses.collectAsState()

    val firstVerse = currentVerses.firstOrNull()?.verse ?: 1
    val lastVerse = currentVerses.lastOrNull()?.verse ?: 1
    val bookEn = currentVerses.firstOrNull()?.bookEn ?: currentBook
    val bookId = currentVerses.firstOrNull()?.bookId ?: 0

    var title by remember(currentBook, currentChapter) {
        mutableStateOf("$currentBook ${currentChapter}장 연구")
    }
    var startV by remember(currentBook, currentChapter) { mutableStateOf(firstVerse.toString()) }
    var endV by remember(currentBook, currentChapter) { mutableStateOf(lastVerse.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LanguageManager.getTranslation("study_create_dialog_title", appLanguage)) },
        text = {
            Column {
                Text(
                    text = "${getBookTranslation(currentBook, appLanguage)} ${currentChapter}장",
                    style = MaterialTheme.typography.titleSmall
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
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startV,
                        onValueChange = { startV = it.filter { c -> c.isDigit() } },
                        label = { Text(LanguageManager.getTranslation("study_create_start_verse", appLanguage)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endV,
                        onValueChange = { endV = it.filter { c -> c.isDigit() } },
                        label = { Text(LanguageManager.getTranslation("study_create_end_verse", appLanguage)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sv = startV.toIntOrNull() ?: firstVerse
                    val ev = endV.toIntOrNull() ?: lastVerse
                    studyViewModel.createSession(
                        title = title,
                        book = currentBook,
                        bookEn = bookEn,
                        bookId = bookId,
                        startChapter = currentChapter,
                        startVerse = minOf(sv, ev),
                        endChapter = currentChapter,
                        endVerse = maxOf(sv, ev),
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
}
