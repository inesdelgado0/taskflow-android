package com.taskflow.app.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val completedTasks = state.tasks.filter { it.status == TaskStatus.COMPLETED }
    FormScreen(stringResource(R.string.history_tasks_title), { nav.popBackStack() }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(false, {}, label = { Text(stringResource(R.string.all_filter)) })
            FilterChip(true, {}, label = { Text(stringResource(R.string.completed_filter)) })
            FilterChip(false, {}, label = { Text(stringResource(R.string.cancelled_filter)) })
        }
        completedTasks.forEach { task ->
            HistoryCard(
                title = task.title,
                project = state.projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(),
                time = "",
                date = task.updatedAt.displayDate(),
                rating = state.evaluations.firstOrNull()?.rating ?: 0
            )
        }
        if (completedTasks.isEmpty()) EmptyData()
        SectionCard(stringResource(R.string.monthly_summary)) {
            Metric(stringResource(R.string.completed_tasks_metric), completedTasks.size.toString())
            Metric(stringResource(R.string.total_time), "")
            Metric(stringResource(R.string.average_rating), state.evaluations.averageRating())
        }
    }
}
