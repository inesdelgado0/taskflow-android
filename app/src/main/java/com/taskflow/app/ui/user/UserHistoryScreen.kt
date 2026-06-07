package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.HistoryCard
import com.taskflow.app.ui.common.components.Metric
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.util.averageRating
import com.taskflow.app.ui.common.util.displayDate

@Composable
fun UserHistoryScreen(nav: NavController) {
    val state by taskFlowState()
    var selectedFilter by remember { mutableStateOf("all") }
    val historyTasks = state.tasks.filter {
        it.status == TaskStatus.COMPLETED || it.status == TaskStatus.CANCELLED
    }
    val filteredTasks = when (selectedFilter) {
        "all" -> historyTasks
        "completed" -> historyTasks.filter { it.status == TaskStatus.COMPLETED }
        "cancelled" -> historyTasks.filter { it.status == TaskStatus.CANCELLED }
        else -> historyTasks
    }

    FormScreen(stringResource(R.string.history_tasks_title), { nav.popBackStack() }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                label = { Text(stringResource(R.string.all_filter)) }
            )
            FilterChip(
                selected = selectedFilter == "completed",
                onClick = { selectedFilter = "completed" },
                label = { Text(stringResource(R.string.completed_filter)) }
            )
            FilterChip(
                selected = selectedFilter == "cancelled",
                onClick = { selectedFilter = "cancelled" },
                label = { Text(stringResource(R.string.cancelled_filter)) }
            )
        }
        filteredTasks.forEach { task ->
            HistoryCard(
                title = task.title,
                project = state.projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(),
                time = "",
                date = task.updatedAt.displayDate(),
                rating = state.evaluations.firstOrNull()?.rating ?: 0
            )
        }
        if (filteredTasks.isEmpty()) EmptyData()
        SectionCard(stringResource(R.string.monthly_summary)) {
            Metric(stringResource(R.string.completed_tasks_metric), filteredTasks.count { it.status == TaskStatus.COMPLETED }.toString())
            Metric(stringResource(R.string.total_time), "")
            Metric(stringResource(R.string.average_rating), state.evaluations.averageRating())
        }
    }
}
