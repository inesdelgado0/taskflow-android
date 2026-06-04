package com.taskflow.app.ui.user.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.user.tasks.UserTaskItemUi
import com.taskflow.app.ui.user.tasks.UserTasksViewModel

private val PageBackground = Color(0xFFF6F7F9)
private val CardBorder = Color(0xFFE2E6EA)
private val PrimaryBlue = Color(0xFF2F7DF6)
private val TextSecondary = Color(0xFF667085)
private val SuccessGreen = Color(0xFF20A464)
private val SuccessGreenBg = Color(0xFFD8F8E4)
private val StarYellow = Color(0xFFFFB000)

private enum class HistoryFilter {
    ALL,
    COMPLETED,
    CANCELLED
}

@Composable
fun UserTaskHistoryScreen(
    nav: NavController,
    viewModel: UserTasksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        HistoryTopBar(onBack = { nav.popBackStack() })

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            else -> {
                val tasks = when (selectedFilter) {
                    HistoryFilter.ALL -> state.completedTasks
                    HistoryFilter.COMPLETED -> state.completedTasks
                    HistoryFilter.CANCELLED -> state.completedTasks.filter { it.status == TaskStatus.CANCELLED }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HistoryFilters(
                        selected = selectedFilter,
                        onSelected = { selectedFilter = it }
                    )

                    if (tasks.isEmpty()) {
                        EmptyHistoryCard()
                    } else {
                        tasks.forEach { task ->
                            HistoryTaskCard(task = task)
                        }
                    }

                    MonthlySummaryCard(tasks = state.completedTasks)
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.btn_back))
        }
        Text(
            text = stringResource(R.string.history_tasks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun HistoryFilters(
    selected: HistoryFilter,
    onSelected: (HistoryFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryFilterChip(
            text = stringResource(R.string.all_filter),
            selected = selected == HistoryFilter.ALL,
            onClick = { onSelected(HistoryFilter.ALL) }
        )
        HistoryFilterChip(
            text = stringResource(R.string.completed_filter),
            selected = selected == HistoryFilter.COMPLETED,
            onClick = { onSelected(HistoryFilter.COMPLETED) }
        )
        HistoryFilterChip(
            text = stringResource(R.string.cancelled_filter),
            selected = selected == HistoryFilter.CANCELLED,
            onClick = { onSelected(HistoryFilter.CANCELLED) }
        )
    }
}

@Composable
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryBlue,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = Color.Black
        )
    )
}

@Composable
private fun HistoryTaskCard(task: UserTaskItemUi) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = task.projectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Star, contentDescription = null, tint = StarYellow)
                    Text(
                        text = (task.rating ?: 5.0).toCompactRating(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(Icons.Outlined.AccessTime, task.timeSpentMinutes.toDurationText())
                Text(text = task.dateText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuccessGreenBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CheckBox, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                    Text(
                        text = stringResource(R.string.completed_status),
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            modifier = Modifier.padding(16.dp),
            color = TextSecondary
        )
    }
}

@Composable
private fun MonthlySummaryCard(tasks: List<UserTaskItemUi>) {
    val totalMinutes = tasks.sumOf { it.timeSpentMinutes }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            SummaryRow(stringResource(R.string.completed_tasks_metric), tasks.size.toString())
            SummaryRow(stringResource(R.string.total_time), totalMinutes.toDurationText())
            SummaryRow(stringResource(R.string.average_rating), "5")
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IconText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

private fun Int.toDurationText(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        this == 0 -> "0h"
        minutes == 0 -> "${hours}h"
        hours == 0 -> "${minutes}min"
        else -> "${hours}h ${minutes}min"
    }
}

private fun Double.toCompactRating(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
