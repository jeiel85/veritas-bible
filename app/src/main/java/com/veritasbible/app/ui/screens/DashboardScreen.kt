package com.veritasbible.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veritasbible.app.data.ReadingLog
import com.veritasbible.app.ui.BibleViewModel
import kotlin.math.max

@Composable
fun DashboardScreen(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.readingLogs.collectAsState()
    val goal by viewModel.readingGoal.collectAsState()
    val isPrepopulating by viewModel.isPrepopulating.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var goalInputText by remember { mutableStateOf("") }

    // Aggregate statistics
    val totalVersesRead = logs.sumOf { it.countVerses }
    val totalChaptersRead = logs.sumOf { it.countChapters }
    val totalSecondsSpent = logs.sumOf { it.sessionDurationSec }
    val totalMinutesSpent = totalSecondsSpent / 60

    // Compute streak
    val streakDays = computeStreak(logs)

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
                    text = "성경 독서 분석 대시보드",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isPrepopulating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Card
                item {
                    StreakCard(streak = streakDays)
                }

                // Core Analytics grids
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricBox(
                            title = "읽은 총 구절",
                            value = "$totalVersesRead",
                            unit = "절",
                            icon = Icons.Filled.MenuBook,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "묵상 누적 장수",
                            value = "$totalChaptersRead",
                            unit = "장",
                            icon = Icons.Filled.CollectionsBookmark,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "합산 공부 시간",
                            value = "$totalMinutesSpent",
                            unit = "분",
                            icon = Icons.Filled.HourglassEmpty,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Goals card progress
                item {
                    goal?.let { activeGoal ->
                        GoalProgressCard(
                            goal = activeGoal,
                            onModifyClick = {
                                goalInputText = activeGoal.targetChapters.toString()
                                showGoalDialog = true
                            }
                        )
                    }
                }

                // Graph metrics showing active weekly history
                item {
                    WeeklyReadingGraphCard(logs = logs)
                }

                // History logs header helper
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Recent History Logs",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "최근 독서 이력 일계표",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (logs.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "사용자 독서 데이터가 기록되지 않았습니다. 성경 리더에서 구절을 읽으면 매일 통계가 확장됩니다.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(logs.take(7)) { log ->
                        HistoryLogItem(log = log)
                    }
                }

                // bottom buffer
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("독서 장수 목표 기입", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "목표 연간/분기 묵상 장수를 재합산 기입해 주세요. 현재 완료된 누적 장수와 결합되어 목표 달성률을 환산해 줍니다.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = goalInputText,
                        onValueChange = { goalInputText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("목표 묵상 장 수") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_chapters_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intVal = goalInputText.toIntOrNull()
                        if (intVal != null && intVal > 0) {
                            viewModel.setReadingGoal(intVal)
                        }
                        showGoalDialog = false
                    },
                    modifier = Modifier.testTag("confirm_goal_button")
                ) {
                    Text("목표 설정")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_tracker_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "성경 공부 스트릭",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (streak > 0) "${streak}일 연속 성경 연구 중! 🔥" else "오늘의 말씀 공부를 시작해 보세요!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$streak",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "DAY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
                Text(
                    text = " $unit",
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GoalProgressCard(
    goal: com.veritasbible.app.data.ReadingGoal,
    onModifyClick: () -> Unit
) {
    val progress = if (goal.targetChapters > 0) {
        goal.completedChapters.toFloat() / goal.targetChapters.toFloat()
    } else 0f
    val clampedProgress = progress.coerceIn(0f, 1f)
    val pctPercent = (clampedProgress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = "Goal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "목표 달성 진척도",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onModifyClick) {
                    Text("목표 변경", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage complete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "목표 ${goal.targetChapters}장 중 ${goal.completedChapters}장 완독",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$pctPercent%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { clampedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun WeeklyReadingGraphCard(logs: List<ReadingLog>) {
    val barColor = MaterialTheme.colorScheme.primary
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f).toArgb()

    // Filter last 5 logs for graph display
    val graphLogs = remember(logs) {
        logs.reversed().takeLast(5).ifEmpty {
            listOf(
                ReadingLog(dateString = "준비", countVerses = 0, countChapters = 0, sessionDurationSec = 0)
            )
        }
    }

    val maxVerses = remember(graphLogs) {
        val calculatedMax = graphLogs.maxOf { it.countVerses }
        if (calculatedMax <= 0) 10 else calculatedMax
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "최근 독서 추이 (읽은 구절 수/일)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas rendering of custom vertical bar chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val graphHeight = canvasHeight - 30.dp.toPx()
                val graphWidth = canvasWidth - 10.dp.toPx()

                // Draw horizontal target helper grids
                val gridLevels = 3
                for (i in 0..gridLevels) {
                    val y = (graphHeight / gridLevels) * i
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars for each available log
                val elementCount = graphLogs.size
                val spaceBetween = graphWidth / elementCount
                val barMaxWidth = 24.dp.toPx()

                graphLogs.forEachIndexed { i, item ->
                    val xCenter = (spaceBetween * i) + (spaceBetween / 2)
                    val valueRatio = item.countVerses.toFloat() / maxVerses.toFloat()
                    val barHeightFraction = graphHeight * valueRatio
                    val barHeight = max(barHeightFraction, 4.dp.toPx()) // thin base is drawn even if 0

                    // Draw rectangular bar
                    drawRect(
                        color = barColor,
                        topLeft = Offset(xCenter - (barMaxWidth / 2), graphHeight - barHeight),
                        size = Size(barMaxWidth, barHeight)
                    )

                    // Draw verses numeric text above the bar
                    drawContext.canvas.nativeCanvas.drawText(
                        "${item.countVerses}",
                        xCenter,
                        graphHeight - barHeight - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textLabelColor
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )

                    // Draw date string on the bottom axis
                    val rawDateParts = item.dateString.split("-")
                    val mmddLabel = if (rawDateParts.size >= 3) "${rawDateParts[1]}/${rawDateParts[2]}" else item.dateString
                    drawContext.canvas.nativeCanvas.drawText(
                        mmddLabel,
                        xCenter,
                        canvasHeight - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textLabelColor
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryLogItem(log: ReadingLog) {
    val durationMins = log.sessionDurationSec / 60
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = log.dateString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "총 ${log.countChapters}개 장 접속 기입 완료",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${log.countVerses}절",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "읽음",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${durationMins}분",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "묵상",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Compute active consecutive days spent studying
fun computeStreak(logs: List<ReadingLog>): Int {
    if (logs.isEmpty()) return 0
    val datesSet = logs.map { it.dateString }.toSet()
    var streak = 0
    val calendar = java.util.Calendar.getInstance()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

    // Start checking from today backwards
    while (true) {
        val checkDate = sdf.format(calendar.time)
        if (datesSet.contains(checkDate)) {
            streak++
            calendar.add(java.util.Calendar.DATE, -1)
        } else {
            // If today is empty, check yesterday as well. If yesterday is also empty, streak breaks.
            if (streak == 0) {
                calendar.add(java.util.Calendar.DATE, -1)
                val checkYest = sdf.format(calendar.time)
                if (datesSet.contains(checkYest)) {
                    streak++
                    calendar.add(java.util.Calendar.DATE, -1)
                    continue
                }
            }
            break
        }
    }
    return streak
}

fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (this.alpha * 255).toInt(),
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}
