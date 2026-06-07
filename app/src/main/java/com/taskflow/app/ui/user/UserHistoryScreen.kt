package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
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
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task

@Composable
fun UserHistoryScreen(nav: NavController) {
    val state by taskFlowState()
    FormScreen(stringResource(R.string.history_tasks_title), { nav.popBackStack() }) {
        UserHistoryContent(
            tasks = state.tasks,
            projects = state.projects,
            evaluations = state.evaluations
        )
    }
}

@Composable
internal fun UserHistoryContent(
    tasks: List<Task>,
    projects: List<Project>,
    evaluations: List<Evaluation>
) {
    var selectedFilter by remember { mutableStateOf("all") }
    val historyTasks = tasks.filter {
        it.status == TaskStatus.COMPLETED || it.status == TaskStatus.CANCELLED
    }
    val filteredTasks = when (selectedFilter) {
        "all" -> historyTasks
        "completed" -> historyTasks.filter { it.status == TaskStatus.COMPLETED }
        "cancelled" -> historyTasks.filter { it.status == TaskStatus.CANCELLED }
        else -> historyTasks
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedFilter == "all",
            onClick = { selectedFilter = "all" },
            modifier = Modifier.testTag("history_filter_all"),
            label = { Text(stringResource(R.string.all_filter)) }
        )
        FilterChip(
            selected = selectedFilter == "completed",
            onClick = { selectedFilter = "completed" },
            modifier = Modifier.testTag("history_filter_completed"),
            label = { Text(stringResource(R.string.completed_filter)) }
        )
        FilterChip(
            selected = selectedFilter == "cancelled",
            onClick = { selectedFilter = "cancelled" },
            modifier = Modifier.testTag("history_filter_cancelled"),
            label = { Text(stringResource(R.string.cancelled_filter)) }
        )
    }
    filteredTasks.forEach { task ->
        Box(Modifier.testTag("history_task_${task.status.name.lowercase()}_${task.id}")) {
            HistoryCard(
                title = task.title,
                project = projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(),
                time = "",
                date = task.updatedAt.displayDate(),
                rating = evaluations.firstOrNull()?.rating ?: 0
            )
        }
    }
    if (filteredTasks.isEmpty()) EmptyData()
    SectionCard(stringResource(R.string.monthly_summary)) {
        Metric(stringResource(R.string.completed_tasks_metric), filteredTasks.count { it.status == TaskStatus.COMPLETED }.toString())
        Metric(stringResource(R.string.total_time), "")
        Metric(stringResource(R.string.average_rating), evaluations.averageRating())
    }
}
