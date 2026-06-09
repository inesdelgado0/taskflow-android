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
import com.taskflow.app.data.local.entity.UserTaskEntity
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
            evaluations = state.allEvaluations,
            assignments = state.userTaskAssignments,
            currentUserId = state.currentUser?.id
        )
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

@Composable
internal fun UserHistoryContent(
    tasks: List<Task>,
    projects: List<Project>,
    evaluations: List<Evaluation>,
    assignments: List<UserTaskEntity>,
    currentUserId: Long?
) {
    var selectedFilter by remember { mutableStateOf("all") }
    val userAssignments = assignments.filter { assignment ->
        currentUserId != null && assignment.userId == currentUserId
    }
    val userTaskIds = userAssignments.map { assignment -> assignment.taskId }.toSet()
    val historyTasks = tasks.filter { task ->
        task.id in userTaskIds &&
            (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.CANCELLED)
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
                time = userAssignments.firstOrNull { it.taskId == task.id }
                    ?.timeSpentMinutes
                    ?.toDurationText()
                    .orEmpty(),
                date = (userAssignments.firstOrNull { it.taskId == task.id }?.workDate ?: task.updatedAt).displayDate(),
                rating = evaluations.firstOrNull {
                    it.projectId == task.projectId && it.evaluatedUserId == currentUserId
                }?.rating ?: 0
            )
        }
    }
    if (filteredTasks.isEmpty()) EmptyData()
    SectionCard(stringResource(R.string.monthly_summary)) {
        Metric(stringResource(R.string.completed_tasks_metric), filteredTasks.count { it.status == TaskStatus.COMPLETED }.toString())
        Metric(
            stringResource(R.string.total_time),
            filteredTasks.sumOf { task ->
                userAssignments.firstOrNull { it.taskId == task.id }?.timeSpentMinutes ?: 0
            }.toDurationText()
        )
        Metric(
            stringResource(R.string.average_rating),
            evaluations.filter { it.evaluatedUserId == currentUserId }.averageRating()
        )
    }
}
