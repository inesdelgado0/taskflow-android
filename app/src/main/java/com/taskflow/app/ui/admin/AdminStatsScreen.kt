package com.taskflow.app.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.UserTaskEntity
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.StatisticRow
import com.taskflow.app.domain.model.StatisticsExportFormat
import com.taskflow.app.domain.model.StatisticsGrouping
import com.taskflow.app.domain.model.StatisticsSnapshot
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.DropdownSelector
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.Label
import com.taskflow.app.ui.common.components.Metric
import com.taskflow.app.ui.common.components.Ranking
import com.taskflow.app.ui.common.components.SearchField
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.util.exportStatistics

@Composable
fun AdminStatsScreen(nav: NavController) {
    val state by taskFlowState()
    val context = LocalContext.current
    var grouping by rememberSaveable { mutableStateOf(StatisticsGrouping.BY_USER) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedProjectId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedUserId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf<TaskStatus?>(null) }
    var selectedPriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }

    val filteredTasks = state.tasks.filter { task ->
        val project = state.projects.firstOrNull { it.id == task.projectId }
        val linkedUsers = state.users.filter { user ->
            user.id == task.createdBy || state.userTaskAssignments.any { it.taskId == task.id && it.userId == user.id }
        }
        val searchableText = buildString {
            append(task.title)
            append(' ')
            append(task.description.orEmpty())
            append(' ')
            append(project?.name.orEmpty())
            append(' ')
            linkedUsers.forEach { user ->
                append(user.name)
                append(' ')
                append(user.username)
                append(' ')
                append(user.email)
                append(' ')
            }
        }

        query.isBlank() || searchableText.contains(query, ignoreCase = true)
    }.filter { task ->
        selectedProjectId == null || task.projectId == selectedProjectId
    }.filter { task ->
        selectedUserId == null ||
            task.createdBy == selectedUserId ||
            state.userTaskAssignments.any { it.taskId == task.id && it.userId == selectedUserId }
    }.filter { task ->
        selectedStatus == null || task.status == selectedStatus
    }.filter { task ->
        selectedPriority == null || task.priority == selectedPriority
    }

    val filteredAssignments = state.userTaskAssignments.filter { assignment ->
        filteredTasks.any { it.id == assignment.taskId }
    }
    val snapshot = buildAdminStatisticsSnapshot(
        title = stringResource(R.string.stats_title),
        grouping = grouping,
        tasks = filteredTasks,
        projects = state.projects,
        users = state.users,
        assignments = filteredAssignments
    )
    val topUsers = buildTopUsers(state.users, filteredTasks, filteredAssignments)
    val totalTimeMinutes = filteredAssignments.sumOf { it.timeSpentMinutes }
    val averageTimeLabel = formatAverageTime(totalTimeMinutes, filteredTasks.size)
    val groupingLabel = when (grouping) {
        StatisticsGrouping.BY_USER -> stringResource(R.string.stats_by_user)
        StatisticsGrouping.BY_PROJECT -> stringResource(R.string.stats_by_project)
        StatisticsGrouping.BY_TASK -> stringResource(R.string.stats_by_task)
    }

    FormScreen(stringResource(R.string.stats_title), onBack = { nav.popBackStack() }) {
        SyncStatus(state)

        SectionCard(stringResource(R.string.export_data)) {
            DropdownSelector(
                label = stringResource(R.string.report_type),
                selectedText = groupingLabel
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_user)) }, onClick = { grouping = StatisticsGrouping.BY_USER })
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_project)) }, onClick = { grouping = StatisticsGrouping.BY_PROJECT })
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_task)) }, onClick = { grouping = StatisticsGrouping.BY_TASK })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { exportStatistics(context, snapshot, StatisticsExportFormat.PDF) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.export_btn_pdf))
                }
                Button(
                    onClick = { exportStatistics(context, snapshot, StatisticsExportFormat.CSV) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Green),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.export_btn_csv))
                }
            }
        }

        SectionCard(stringResource(R.string.advanced_search)) {
            SearchField(
                placeholder = stringResource(R.string.search_by_task_project_user),
                value = query,
                onValueChange = { query = it }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DropdownSelector(
                    label = stringResource(R.string.project_label),
                    selectedText = state.projects.firstOrNull { it.id == selectedProjectId }?.name ?: stringResource(R.string.all_projects),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.all_projects)) }, onClick = { selectedProjectId = null })
                    state.projects.forEach { project ->
                        DropdownMenuItem(text = { Text(project.name) }, onClick = { selectedProjectId = project.id })
                    }
                }
                DropdownSelector(
                    label = stringResource(R.string.user_label),
                    selectedText = state.users.firstOrNull { it.id == selectedUserId }?.name ?: stringResource(R.string.all_users),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.all_users)) }, onClick = { selectedUserId = null })
                    state.users.forEach { user ->
                        DropdownMenuItem(text = { Text(user.name) }, onClick = { selectedUserId = user.id })
                    }
                }
            }
            Label(stringResource(R.string.filter_by_status))
            FilterChipRow(
                items = TaskStatus.entries,
                selected = selectedStatus,
                label = { it.name },
                onSelected = { selectedStatus = it }
            )
            Label(stringResource(R.string.filter_by_priority))
            FilterChipRow(
                items = TaskPriority.entries,
                selected = selectedPriority,
                label = { it.name },
                onSelected = { selectedPriority = it }
            )
            OutlinedButton(
                onClick = {
                    query = ""
                    selectedProjectId = null
                    selectedUserId = null
                    selectedStatus = null
                    selectedPriority = null
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.filter_btn_clear))
            }
        }

        SectionCard(stringResource(R.string.general_summary)) {
            Metric(stringResource(R.string.dashboard_completion_rate), "${snapshot.completionRate}%")
            Metric(stringResource(R.string.completed_tasks_metric), snapshot.completedTasks.toString())
            Metric(stringResource(R.string.pending_tasks_metric), snapshot.pendingTasks.toString())
            Metric(stringResource(R.string.tasks_found), filteredTasks.size.toString())
            Metric(stringResource(R.string.average_time_per_task), averageTimeLabel)
        }

        SectionCard(stringResource(R.string.report_detail)) {
            snapshot.rows
                .filter { it.totalTasks > 0 || grouping == StatisticsGrouping.BY_TASK }
                .take(8)
                .forEach { row ->
                    Metric(row.label, stringResource(R.string.report_row_completion_details, row.completionRate, row.completedTasks, row.totalTasks))
                }
            if (snapshot.rows.isEmpty()) EmptyData()
        }

        SectionCard(stringResource(R.string.top_users)) {
            topUsers.take(5).forEachIndexed { index, userStats ->
                Ranking(
                    (index + 1).toString(),
                    stringResource(
                        R.string.top_user_ranking_label,
                        userStats.name,
                        userStats.completedTasks,
                        userStats.totalTasks
                    )
                )
            }
            if (topUsers.isEmpty()) EmptyData()
        }
    }
}

@Composable
private fun <T> FilterChipRow(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelected: (T?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            FilterChip(
                selected = selected == item,
                onClick = { onSelected(if (selected == item) null else item) },
                label = { Text(label(item)) }
            )
        }
    }
}

private data class UserStats(
    val name: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val totalTimeMinutes: Int
)

private fun buildTopUsers(
    users: List<User>,
    tasks: List<Task>,
    assignments: List<UserTaskEntity>
): List<UserStats> =
    users
        .filter { it.isActive }
        .map { user ->
            val userTasks = tasks.filter { task ->
                task.createdBy == user.id || assignments.any { it.userId == user.id && it.taskId == task.id }
            }
            val userAssignments = assignments.filter { it.userId == user.id }
            UserStats(
                name = user.name,
                totalTasks = userTasks.size,
                completedTasks = userTasks.count { it.status == TaskStatus.COMPLETED },
                totalTimeMinutes = userAssignments.sumOf { it.timeSpentMinutes }
            )
        }
        .filter { it.totalTasks > 0 }
        .sortedWith(
            compareByDescending<UserStats> { it.completedTasks }
                .thenByDescending { it.totalTasks }
                .thenByDescending { it.totalTimeMinutes }
        )

private fun buildAdminStatisticsSnapshot(
    title: String,
    grouping: StatisticsGrouping,
    tasks: List<Task>,
    projects: List<Project>,
    users: List<User>,
    assignments: List<UserTaskEntity>
): StatisticsSnapshot {
    val now = System.currentTimeMillis()
    val rows = when (grouping) {
        StatisticsGrouping.BY_USER -> users
            .map { user ->
                val userTasks = tasks.filter { task ->
                    task.createdBy == user.id || assignments.any { it.userId == user.id && it.taskId == task.id }
                }
                userTasks.toStatisticRow(
                    label = user.name,
                    now = now,
                    totalTimeMinutes = assignments
                        .filter { it.userId == user.id && userTasks.any { task -> task.id == it.taskId } }
                        .sumOf { it.timeSpentMinutes }
                )
            }
            .filter { it.totalTasks > 0 }

        StatisticsGrouping.BY_PROJECT -> projects
            .map { project ->
                val projectTasks = tasks.filter { it.projectId == project.id }
                projectTasks.toStatisticRow(
                    label = project.name,
                    now = now,
                    totalTimeMinutes = assignments
                        .filter { assignment -> projectTasks.any { it.id == assignment.taskId } }
                        .sumOf { it.timeSpentMinutes }
                )
            }
            .filter { it.totalTasks > 0 }

        StatisticsGrouping.BY_TASK -> tasks.map { task ->
            listOf(task).toStatisticRow(
                label = task.title,
                now = now,
                totalTimeMinutes = assignments
                    .filter { it.taskId == task.id }
                    .sumOf { it.timeSpentMinutes }
            )
        }
    }

    return StatisticsSnapshot(
        title = title,
        generatedAt = now,
        rows = rows
    )
}

private fun List<Task>.toStatisticRow(
    label: String,
    now: Long,
    totalTimeMinutes: Int
): StatisticRow =
    StatisticRow(
        label = label,
        totalTasks = size,
        completedTasks = count { it.status == TaskStatus.COMPLETED },
        pendingTasks = count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED },
        overdueTasks = count { task ->
            task.deadline != null &&
                task.deadline < now &&
                task.status != TaskStatus.COMPLETED &&
                task.status != TaskStatus.CANCELLED
        },
        totalTimeMinutes = totalTimeMinutes
    )

private fun formatAverageTime(totalMinutes: Int, taskCount: Int): String {
    if (taskCount == 0 || totalMinutes == 0) return "0h"
    val hours = totalMinutes.toDouble() / 60.0 / taskCount
    return "${"%.1f".format(hours)}h"
}
