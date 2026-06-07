package com.taskflow.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.taskflow.app.domain.model.StatisticsExportFormat
import com.taskflow.app.domain.model.StatisticsGrouping
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.components.DropdownSelector
import com.taskflow.app.ui.common.components.EmptyData
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.Label
import com.taskflow.app.ui.common.components.Metric
import com.taskflow.app.ui.common.components.Ranking
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.components.SyncStatus
import com.taskflow.app.ui.common.components.taskFlowState
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.common.util.exportStatistics
import com.taskflow.app.ui.common.util.toStatisticsSnapshot

@Composable
fun AdminStatsScreen(nav: NavController) {
    val state by taskFlowState()
    val context = LocalContext.current
    var grouping by rememberSaveable { mutableStateOf(StatisticsGrouping.BY_USER) }
    val groupingLabel = when (grouping) {
        StatisticsGrouping.BY_USER -> stringResource(R.string.stats_by_user)
        StatisticsGrouping.BY_PROJECT -> stringResource(R.string.stats_by_project)
        StatisticsGrouping.BY_TASK -> stringResource(R.string.stats_by_task)
    }
    val snapshot = state.toStatisticsSnapshot(stringResource(R.string.stats_title), grouping)
    FormScreen(stringResource(R.string.stats_title), onBack = { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard(stringResource(R.string.export_data)) {
            Label(stringResource(R.string.report_type))
            DropdownSelector(
                label = stringResource(R.string.report_type),
                selectedText = groupingLabel
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_user)) }, onClick = { grouping = StatisticsGrouping.BY_USER })
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_project)) }, onClick = { grouping = StatisticsGrouping.BY_PROJECT })
                DropdownMenuItem(text = { Text(stringResource(R.string.stats_by_task)) }, onClick = { grouping = StatisticsGrouping.BY_TASK })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { exportStatistics(context, snapshot, StatisticsExportFormat.PDF) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Red), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PDF")
                }
                Button(onClick = { exportStatistics(context, snapshot, StatisticsExportFormat.CSV) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Green), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("CSV")
                }
            }
        }
        SectionCard(stringResource(R.string.general_summary)) {
            Metric(stringResource(R.string.dashboard_completion_rate), "${state.tasks.completionRate()}%")
            Metric(stringResource(R.string.completed_tasks_metric), state.tasks.count { it.status == TaskStatus.COMPLETED }.toString())
            Metric(stringResource(R.string.pending_tasks_metric), state.tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }.toString())
            Metric(stringResource(R.string.total_projects_metric), state.projects.size.toString())
        }
        SectionCard(stringResource(R.string.top_users)) {
            state.users.take(3).forEachIndexed { index, user ->
                Ranking((index + 1).toString(), user.name)
            }
            if (state.users.isEmpty()) EmptyData()
        }
    }
}
