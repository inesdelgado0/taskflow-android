package com.taskflow.app.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.common.theme.Black
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.theme.Soft
import com.taskflow.app.ui.common.theme.Yellow
import com.taskflow.app.ui.common.util.DemoUser
import com.taskflow.app.ui.common.util.completionRate
import com.taskflow.app.ui.common.util.color
import com.taskflow.app.ui.common.util.displayDate
import com.taskflow.app.ui.common.util.label

@Composable
internal fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title.isNotBlank()) Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
internal fun StatNavCard(icon: ImageVector, title: String, value: String, detail: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(108.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun SmallStat(icon: ImageVector, title: String, value: String, detail: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(132.dp), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = color)
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun ProjectListItem(
    project: Project,
    allTasks: List<Task>,
    managerName: String,
    onEdit: () -> Unit,
    onDetails: () -> Unit
) {
    val tasks = allTasks.filter { it.projectId == project.id }
    val percent = tasks.completionRate()
    SectionCard("") {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.manager_prefix, managerName), color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            StatusPill(project.status.label(), project.status.color())
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.tasks_count, tasks.size), color = Color.Black, style = MaterialTheme.typography.labelSmall)
            Text("$percent%", color = Color.Black, style = MaterialTheme.typography.labelSmall)
        }
        LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue, trackColor = Border)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_edit), color = Color.Black)
            }
            OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.details), color = Color.Black)
            }
        }
    }
}

@Composable
internal fun ProjectDetailsCard(
    project: Project,
    managerName: String,
    tasks: List<Task>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
    val percent = tasks.completionRate()
    SectionCard("") {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.manager_prefix, managerName), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(project.status.label(), project.status.color())
        }
        Text(project.description.orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Soft)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DetailMetric(stringResource(R.string.start_date), project.startDate.displayDate(), Modifier.weight(1f))
                DetailMetric(stringResource(R.string.end_date), project.endDate.displayDate(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DetailMetric("Total Tarefas", tasks.size.toString(), Modifier.weight(1f))
                DetailMetric(stringResource(R.string.dashboard_completed), completedTasks.toString(), Modifier.weight(1f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.progress_label), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Text("$percent%", color = Color.Black, style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue, trackColor = Border)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onEdit, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(Blue), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.edit_project))
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
            ) {
                Text(stringResource(R.string.btn_delete))
            }
        }
    }
}

@Composable
internal fun DetailMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
        Text(value.ifBlank { "-" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun UserCard(user: DemoUser, showRole: Boolean, onEdit: () -> Unit, onRemove: () -> Unit) {
    SectionCard("") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(user.initial, user.color, 42)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    StatusPill(stringResource(R.string.active_status), Green)
                }
                Text(user.email, color = Muted, style = MaterialTheme.typography.bodySmall)
                if (showRole) Text(user.role, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.btn_edit)) }
            OutlinedButton(onClick = onRemove, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text(stringResource(R.string.btn_delete)) }
        }
    }
}

@Composable
internal fun ActivityLine(text: String, time: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(Blue))
        Spacer(Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(time, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun Metric(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun Ranking(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
            Text(number, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
internal fun TwoMetrics(a: String, av: String, b: String, bv: String) {
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(a, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(av, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(b, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(bv, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun CompactUser(name: String, initial: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(initial, color, 28)
        Spacer(Modifier.width(8.dp))
        Text(name)
    }
}

@Composable
internal fun TaskPriorityLine(level: String, title: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusPill(level, color)
        Spacer(Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
internal fun ManagerTaskCard(task: Task, projects: List<Project>, onAssign: () -> Unit, onEdit: () -> Unit) {
    val progress = if (task.status == TaskStatus.COMPLETED) 1f else if (task.status == TaskStatus.IN_PROGRESS) 0.6f else 0f
    SectionCard("") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(task.priority.label(), task.priority.color())
        }
        ProgressLine(stringResource(R.string.progress_label), "${(progress * 100).toInt()}%", progress)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onAssign, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.associate_action)) }
            Button(onClick = onEdit, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Blue)) { Text(stringResource(R.string.btn_edit)) }
        }
    }
}

@Composable
internal fun TeamMemberCard(
    user: DemoUser,
    rating: String,
    activeTasksText: String,
    completedTasks: String,
    activeTasks: String,
    onViewTasks: () -> Unit = {},
    onEvaluate: () -> Unit
) {
    SectionCard("") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(user.initial, user.color, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                if (rating.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Yellow, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(rating, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(activeTasksText, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        TwoMetrics(
            stringResource(R.string.dashboard_completed),
            completedTasks,
            stringResource(R.string.active_tasks_metric),
            activeTasks
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onViewTasks, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.view_tasks)) }
            Button(onClick = onEvaluate, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Blue)) { Text(stringResource(R.string.evaluate_action)) }
        }
    }
}

@Composable
internal fun ProjectSummary(project: Project, tasks: List<Task>, onDetails: () -> Unit) {
    val done = tasks.count { it.status == TaskStatus.COMPLETED }
    val progress = tasks.completionRate() / 100f
    SectionCard(project.name) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.deadline_prefix, project.endDate.displayDate()), color = Muted, style = MaterialTheme.typography.bodySmall)
            StatusPill(project.status.name, project.status.color())
        }
        TwoMetrics(stringResource(R.string.tasks_title), tasks.size.toString(), stringResource(R.string.dashboard_completed), done.toString())
        ProgressLine(stringResource(R.string.progress_label), "${(progress * 100).toInt()}%", progress)
        Button(onClick = onDetails, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Blue)) {
            Text(stringResource(R.string.view_project_details))
        }
    }
}

@Composable
internal fun EvalLine(user: DemoUser, onEvaluate: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(user.initial, user.color, 32)
        Spacer(Modifier.width(8.dp))
        Text(user.name, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onEvaluate) { Text(stringResource(R.string.evaluate_action)) }
    }
}

@Composable
internal fun Assigned(name: String, initial: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Color(0xFFEAF2FF), RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(initial, color, 28)
        Spacer(Modifier.width(8.dp))
        Text(name, modifier = Modifier.weight(1f))
        Icon(Icons.Default.Delete, null, tint = Red, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun AvailableUser(user: DemoUser) {
    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(user.initial, Color(0xFF9CA3AF), 28)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name)
            Text(user.role, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.Add, null, tint = Blue)
    }
}

@Composable
internal fun UserTaskLine(task: Task, projects: List<Project>, onClick: () -> Unit) {
    val progress = if (task.status == TaskStatus.COMPLETED) 1f else if (task.status == TaskStatus.IN_PROGRESS) 0.6f else 0f
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                task.deadline.displayDate(),
                modifier = Modifier.widthIn(min = 112.dp),
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
        Spacer(Modifier.height(8.dp))
        ProgressLine("", "${(progress * 100).toInt()}%", progress)
    }
}

@Composable
internal fun Observation(name: String, text: String, time: String) {
    Column(Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(time, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(text, color = Muted)
    }
}

@Composable
internal fun HistoryCard(title: String, project: String, time: String, date: String, rating: Int) {
    SectionCard(title) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(project, color = Muted)
            Row { Text("$rating"); Icon(Icons.Default.Star, null, tint = Yellow, modifier = Modifier.size(16.dp)) }
        }
        TwoMetrics("", time, "", date)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFDDF8E7)).padding(10.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.completed_status), color = Green, fontWeight = FontWeight.SemiBold)
        }
    }
}


