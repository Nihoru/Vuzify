package com.nihoru.vuzify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihoru.vuzify.data.repository.ScheduleRepository
import com.nihoru.vuzify.ui.components.DateBadge
import com.nihoru.vuzify.ui.components.GroupBadge
import com.nihoru.vuzify.ui.components.OfflineNotice
import com.nihoru.vuzify.ui.components.ScheduleCard
import com.nihoru.vuzify.ui.theme.BackgroundLavender
import com.nihoru.vuzify.ui.theme.TextPrimary
import com.nihoru.vuzify.ui.theme.TextSecondary
import com.nihoru.vuzify.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DAYS_RANGE = 365
private const val INITIAL_PAGE = DAYS_RANGE / 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    repository: ScheduleRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentGroup by remember { mutableStateOf("БЭИ2302") }
    var isGroupDialogVisible by remember { mutableStateOf(false) }
    var isOfflineMode by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val baseCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE,
        pageCount = { DAYS_RANGE }
    )

    fun getDateForPage(page: Int): Date {
        val cal = baseCalendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, page - INITIAL_PAGE)
        return cal.time
    }

    val currentDate = remember(pagerState.currentPage) {
        getDateForPage(pagerState.currentPage)
    }

    val dateApiFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Initial sync & check cache
    LaunchedEffect(currentGroup) {
        lastUpdatedText = repository.getLastSyncInfo(currentGroup)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        isRefreshing = true
        val result = repository.refreshSchedule(currentGroup, currentMonth)
        isOfflineMode = result.isFailure
        lastUpdatedText = repository.getLastSyncInfo(currentGroup)
        isRefreshing = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLavender)
            .statusBarsPadding()
    ) {
        // Top Row: Date Badge ("25 МАР ср") + Group Badge ("БЭИ2302")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateBadge(
                date = currentDate,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(INITIAL_PAGE)
                    }
                }
            )

            GroupBadge(
                groupName = currentGroup,
                onGroupClick = { isGroupDialogVisible = true },
                onCalendarClick = { /* DatePicker action */ },
                onSettingsClick = {
                    coroutineScope.launch {
                        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                        isRefreshing = true
                        val result = repository.refreshSchedule(currentGroup, currentMonth)
                        isOfflineMode = result.isFailure
                        lastUpdatedText = repository.getLastSyncInfo(currentGroup)
                        isRefreshing = false
                    }
                }
            )
        }

        // Offline / Cache banner
        OfflineNotice(
            isOffline = isOfflineMode,
            lastUpdated = lastUpdatedText
        )

        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        }

        // Horizontal Pager for Days (Buttery 120 FPS swipe)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageDate = remember(page) { getDateForPage(page) }
            val dateStr = remember(pageDate) { dateApiFormat.format(pageDate) }

            val lessonsFlow = remember(currentGroup, dateStr) {
                repository.getLessonsForDay(currentGroup, dateStr)
            }
            val lessons by lessonsFlow.collectAsState(initial = emptyList())

            if (lessons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎉",
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Занятий нет",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Отличный день для отдыха!",
                            fontSize = 14.sp,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    items(lessons, key = { it.id }) { lesson ->
                        ScheduleCard(lesson = lesson)
                    }
                }
            }
        }
    }

    // Group Selection Dialog
    if (isGroupDialogVisible) {
        var inputGroup by remember { mutableStateOf(currentGroup) }

        AlertDialog(
            onDismissRequest = { isGroupDialogVisible = false },
            title = {
                Text(
                    text = "Выбор группы",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Введите название группы (например, БЭИ2302):",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputGroup,
                        onValueChange = { inputGroup = it.uppercase() },
                        singleLine = true,
                        placeholder = { Text("БЭИ2302") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputGroup.isNotBlank()) {
                            currentGroup = inputGroup.trim()
                            isGroupDialogVisible = false
                        }
                    }
                ) {
                    Text("Выбрать", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isGroupDialogVisible = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
