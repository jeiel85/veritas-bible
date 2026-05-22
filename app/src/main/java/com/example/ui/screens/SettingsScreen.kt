package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BibleViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val operationsMessage by viewModel.operationsMessage.collectAsState()
    val isBibleDownloaded by viewModel.isBibleDownloaded.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()

    // Backup dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    var passwordInput by remember { mutableStateOf("") }
    var generatedBackupString by remember { mutableStateOf("") }
    var importPayloadInput by remember { mutableStateOf("") }

    // Floating Snackbar trigger on message
    LaunchedEffect(operationsMessage) {
        operationsMessage?.let {
            delay(3000)
            viewModel.setOperationsMessage(null)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = com.example.util.LanguageManager.getTranslation("settings_title", appLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Safety Title card segment
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "Security Guard",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (appLanguage == "EN") "Fully Encrypted Offline Store" else "다중 암호화 오프라인 굳건이",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (appLanguage == "EN") "Your faith diaries, highlights, real-time study duration, and notes are completely stored offline with local cryptography." else "당신의 신앙 일기, 밑줄 정보, 실시간 묵상 시간 등 모든 활동 내역은 네트워크 전송 없이 완벽히 기기 내부에 암호화되어 보존되고 있습니다.",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Backup section header
                item {
                    Text(
                        text = com.example.util.LanguageManager.getTranslation("settings_backup_section", appLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                // Option: Export card
                item {
                    OptionButtonRow(
                        title = com.example.util.LanguageManager.getTranslation("settings_export_btn", appLanguage),
                        desc = if (appLanguage == "EN") "Extract and copy an AESGCM-encrypted backup payload secured by a custom passcode." else "사용자가 정한 비밀번호로 AES-GCM 하위 레벨 암호화를 시행하여 텍스트 파일을 추출해 냅니다.",
                        icon = Icons.Filled.CloudUpload,
                        onClick = {
                            passwordInput = ""
                            generatedBackupString = ""
                            showExportDialog = true
                        },
                        tag = "export_backup_trigger"
                    )
                }

                // Option: Import card
                item {
                    OptionButtonRow(
                        title = com.example.util.LanguageManager.getTranslation("settings_import_btn", appLanguage),
                        desc = if (appLanguage == "EN") "Restore your historical diaries, analytics and saved study configurations with your backup passcode." else "생성했었던 백업 스트링 파일과 설정했던 비밀번호를 복원 입력하여 이전 데이터를 복주합니다.",
                        icon = Icons.Filled.CloudDownload,
                        onClick = {
                            passwordInput = ""
                            importPayloadInput = ""
                            showImportDialog = true
                        },
                        tag = "import_backup_trigger"
                    )
                }

                // Theme Customization Segment
                item {
                    Text(
                        text = com.example.util.LanguageManager.getTranslation("settings_theme_section", appLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("theme_system", appLanguage),
                                    selected = themeMode == "SYSTEM",
                                    onClick = { viewModel.setThemeMode("SYSTEM") },
                                    modifier = Modifier.weight(1f).testTag("theme_system_chip")
                                )
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("theme_light", appLanguage),
                                    selected = themeMode == "LIGHT",
                                    onClick = { viewModel.setThemeMode("LIGHT") },
                                    modifier = Modifier.weight(1f).testTag("theme_light_chip")
                                )
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("theme_dark", appLanguage),
                                    selected = themeMode == "DARK",
                                    onClick = { viewModel.setThemeMode("DARK") },
                                    modifier = Modifier.weight(1f).testTag("theme_dark_chip")
                                )
                            }
                        }
                    }
                }

                // Language Selection Segment
                item {
                    Text(
                        text = com.example.util.LanguageManager.getTranslation("language_section", appLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("lang_system", appLanguage),
                                    selected = appLanguage == "SYSTEM",
                                    onClick = { viewModel.setAppLanguage("SYSTEM") },
                                    modifier = Modifier.weight(1.2f).testTag("lang_system_chip")
                                )
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("lang_ko", appLanguage),
                                    selected = appLanguage == "KO",
                                    onClick = { viewModel.setAppLanguage("KO") },
                                    modifier = Modifier.weight(0.9f).testTag("lang_ko_chip")
                                )
                                ThemeChip(
                                    label = com.example.util.LanguageManager.getTranslation("lang_en", appLanguage),
                                    selected = appLanguage == "EN",
                                    onClick = { viewModel.setAppLanguage("EN") },
                                    modifier = Modifier.weight(0.9f).testTag("lang_en_chip")
                                )
                            }
                        }
                    }
                }

                // Bible Data Download section header
                item {
                    Text(
                        text = com.example.util.LanguageManager.getTranslation("settings_data_section", appLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isBibleDownloaded) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                        contentDescription = "Bible data status",
                                        tint = if (isBibleDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = com.example.util.LanguageManager.getTranslation(if (isBibleDownloaded) "settings_data_loaded" else "settings_data_not_loaded", appLanguage),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = com.example.util.LanguageManager.getTranslation(if (isBibleDownloaded) "settings_data_desc_loaded" else "settings_data_desc_not_loaded", appLanguage),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (!isBibleDownloaded && !isDownloading) {
                                    Button(
                                        onClick = { viewModel.downloadBibleData() },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("download_bible_settings_button")
                                    ) {
                                        Text(
                                            text = com.example.util.LanguageManager.getTranslation("settings_data_download_btn", appLanguage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (isBibleDownloaded && !isDownloading) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearBibleData() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.testTag("wipe_bible_settings_button"),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = com.example.util.LanguageManager.getTranslation("settings_data_wipe_btn", appLanguage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (isDownloading) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = downloadStatus,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = String.format("%.0f%%", downloadProgress * 100),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .background(Color.Transparent, shape = RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Management section header
                item {
                    Text(
                        text = if (appLanguage == "EN") "Irreversible System Wipe Zone" else "영구 말소 관리구역",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                    )
                }

                // Option: Wipe local data
                item {
                    OptionButtonRow(
                        title = if (appLanguage == "EN") "Permanently Purge All Data & Notes" else "모든 통계 및 사용자 메모 영구 삭제",
                        desc = if (appLanguage == "EN") "Irreversibly delete all verse notes, study highlights, daily session logs and personal goals. This cannot be undone." else "작성된 구절 메모, 하이라이트 밑줄, 일별 묵상 이력, 설정한 목표 모두를 영구 지업합니다. 복구 불가능합니다.",
                        icon = Icons.Filled.DeleteForever,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            showClearDialog = true
                        },
                        tag = "wipe_data_trigger"
                    )
                }

                // App version specs
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Veritas Bible Study v1.0.0",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (appLanguage == "EN") "Fully Offline • Non-commercial Use" else "완벽 오프라인 • 상업적 배포 차단",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // Centralized Operations Toast Notification
            operationsMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Alert Alert",
                            tint = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // ------------------- POPUP PORTALS -------------------

    // 1. Export back dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("암호화 백업 생성", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "백업을 암호화하기 위해 사용할 비밀번호를 기입하세요. 나중에 복원 시 똑같이 입력해야 합니다.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("임의 비밀번호 설정") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_password_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (generatedBackupString.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "키 데이터가 생성되었습니다! 하단의 공유나 클립보드 복사 버튼을 눌러 소유하세요.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = generatedBackupString,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("암호 기압문 (AES Cipher String)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("cipher_text_box"),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (generatedBackupString.isEmpty()) {
                        Button(
                            onClick = {
                                if (passwordInput.trim().isNotEmpty()) {
                                    viewModel.exportBackup(passwordInput.trim()) { cipherText ->
                                        generatedBackupString = cipherText
                                    }
                                } else {
                                    viewModel.setOperationsMessage("비밀번호를 기입해 주세요.")
                                }
                            },
                            modifier = Modifier.testTag("generate_backup_button")
                        ) {
                            Text("백업 파일 생성")
                        }
                    } else {
                        // Action: clipboard copy
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedBackupString))
                                viewModel.setOperationsMessage("백업 텍스트가 클립보드에 복사되었습니다.")
                                showExportDialog = false
                            },
                            modifier = Modifier.testTag("copy_backup_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("복사")
                        }

                        // Action: Android intent share
                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, generatedBackupString)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Veritas Backup Share"))
                                showExportDialog = false
                            },
                            modifier = Modifier.testTag("share_backup_button")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("외부 공유")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

    // 2. Import back dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("로컬 백업 데이터 복원", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "클립보드나 파일 등에서 복사해둔 백업 문자열(AES 암호문)을 하단에 붙여넣고, 생성 시 기입한 비밀번호를 일치시켜 조율 복구합니다.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = importPayloadInput,
                        onValueChange = { importPayloadInput = it },
                        label = { Text("백업 암호문 문자열 붙여넣기") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("import_payload_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("매칭 비밀번호 기입") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_password_field"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importPayloadInput.trim().isNotEmpty() && passwordInput.trim().isNotEmpty()) {
                            viewModel.importBackup(importPayloadInput.trim(), passwordInput.trim())
                            showImportDialog = false
                        } else {
                            viewModel.setOperationsMessage("백업 텍스트와 비밀번호를 빠짐없이 기입하세요.")
                        }
                    },
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("복원 개시")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

    // 3. Wipe dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("데이터 완전 파기 확인", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    text = "이 작업을 완료하면 모든 개인 묵상 메모, 공부 이력, 스트릭 콤보가 단 한 개도 남지 않고 스마트폰 내부에서 영구 제거됩니다. 계속하시겠습니까?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllNotes()
                        viewModel.resetStatistics()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_wipe_button")
                ) {
                    Text("모두 말소 파기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun OptionButtonRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
