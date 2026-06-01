package com.taskflow.app.ui.common

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import com.taskflow.app.R
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.ui.navigation.Routes
import com.taskflow.app.ui.profile.ProfileUiState
import com.taskflow.app.ui.profile.ProfileViewModel

private val Blue = Color(0xFF2F7DF6)
private val Green = Color(0xFF06C167)
private val Orange = Color(0xFFFF6A00)
private val Red = Color(0xFFFF3B30)
private val Purple = Color(0xFFA64DFF)
private val Yellow = Color(0xFFFFC145)
private val Page = Color(0xFFF6F7F9)
private val OnboardingPage = Color(0xFFEAF4FF)
private val Border = Color(0xFFE1E5EA)
private val Muted = Color(0xFF697386)
private val Soft = Color(0xFFF3F5F7)

@Composable
fun TaskFlowOnboardingScreen(step: Int, onNext: () -> Unit, onBack: () -> Unit) {
    val data = listOf(
        OnboardData(stringResource(R.string.onboarding_title_1), stringResource(R.string.onboarding_desc_1), Icons.Default.Work),
        OnboardData(stringResource(R.string.onboarding_title_2), stringResource(R.string.onboarding_desc_2), Icons.Default.Group),
        OnboardData(stringResource(R.string.onboarding_title_3), stringResource(R.string.onboarding_desc_3), Icons.Default.CheckBox)
    )
    val item = data[step.coerceIn(data.indices)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingPage)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(item.icon, null, tint = if (step == 1) Green else if (step == 2) Purple else Blue, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(36.dp))
                Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(item.subtitle, color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                Dots(step)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    if (step > 0) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Text(stringResource(R.string.btn_back))
                        }
                    }
                    Button(onClick = onNext, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(Blue)) {
                        Text(if (step == 2) stringResource(R.string.onboarding_btn_start) else stringResource(R.string.onboarding_btn_next))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val state by taskFlowState()
    AppScaffold(
        role = "A",
        accent = Blue,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.ADMIN_PROFILE) },
        content = {
            Welcome(stringResource(R.string.dashboard_admin))
            SyncStatus(state)
            val activeProjects = state.projects.count { it.status == ProjectStatus.ACTIVE }
            val completedProjects = state.projects.count { it.status == ProjectStatus.COMPLETED }
            val managers = state.users.count { user -> user.roles.any { it.name == "MANAGER" } }
            val regularUsers = state.users.count { user -> user.roles.any { it.name == "USER" } }
            val completion = state.tasks.completionRate()
            StatNavCard(Icons.Default.Work, stringResource(R.string.projects_title), state.projects.size.toString(), stringResource(R.string.dashboard_projects_detail, activeProjects, completedProjects), Blue) { nav.navigate(Routes.ADMIN_PROJECTS) }
            StatNavCard(Icons.Default.Group, stringResource(R.string.users_title), state.users.size.toString(), stringResource(R.string.dashboard_users_detail, managers, regularUsers), Green) { nav.navigate(Routes.ADMIN_USERS_LIST) }
            StatNavCard(Icons.Default.CalendarToday, stringResource(R.string.stats_title), "$completion%", stringResource(R.string.dashboard_completion_rate), Purple) { nav.navigate(Routes.ADMIN_STATS) }
            SectionCard(stringResource(R.string.dashboard_recent_activity)) {
                state.projects.take(3).forEach { project ->
                    ActivityLine(stringResource(R.string.updated_project_activity, project.name), project.updatedAt.relativeTime())
                }
                if (state.projects.isEmpty()) EmptyData()
            }
        }
    )
}

@Composable
fun AdminProjectsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(
        title = stringResource(R.string.projects_title),
        actionText = stringResource(R.string.new_action),
        onBack = { nav.popBackStack() },
        onAction = { nav.navigate(Routes.ADMIN_PROJECT_CREATE) }
    ) {
        SyncStatus(state)
        SearchField(stringResource(R.string.search_projects_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniSelect(stringResource(R.string.all_statuses), Modifier.weight(1f))
            MiniSelect(stringResource(R.string.all_managers), Modifier.weight(1f))
        }
        state.projects.forEach { project ->
            ProjectListItem(project, state.tasks) {
                viewModel.selectProject(project.id)
                nav.navigate(Routes.ADMIN_PROJECT_DETAILS)
            }
        }
        if (state.projects.isEmpty()) EmptyData()
    }
}

@Composable
fun AdminUsersListScreen(nav: NavController) {
    val state by taskFlowState()
    ListScreen(
        title = stringResource(R.string.users_title),
        actionText = stringResource(R.string.new_action),
        onBack = { nav.popBackStack() },
        onAction = { nav.navigate(Routes.ADMIN_USER_CREATE) }
    ) {
        SyncStatus(state)
        SearchField(stringResource(R.string.search_users_hint))
        state.users.forEach { user ->
            UserCard(user.toDemoUser(), showRole = true, onEdit = { nav.navigate(Routes.ADMIN_USER_EDIT) }, onRemove = {})
        }
        if (state.users.isEmpty()) EmptyData()
    }
}

@Composable
fun AdminStatsScreen(nav: NavController) {
    val state by taskFlowState()
    FormScreen(stringResource(R.string.stats_title), onBack = { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard(stringResource(R.string.export_data)) {
            Label(stringResource(R.string.report_type))
            MiniSelect(stringResource(R.string.stats_by_user))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Red), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PDF")
                }
                Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Green), shape = RoundedCornerShape(8.dp)) {
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

@Composable
fun ProfileScreen(nav: NavController, role: String, accent: Color) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> viewModel.onPhotoSelected(uri?.toString()) }
    )

    ProfileFormScreen(title = stringResource(R.string.profile_title), onBack = { nav.popBackStack() }) {
        ProfileCard {
            if (state.isLoading && state.user == null) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accent)
                }
            } else {
                EditableProfileContent(
                    state = state,
                    role = role,
                    accent = accent,
                    onNameChange = viewModel::onNameChange,
                    onUsernameChange = viewModel::onUsernameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onPhotoClick = { photoPicker.launch("image/*") },
                    onSave = viewModel::saveProfile,
                    onCancel = { nav.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun ProfileFormScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack)
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { content() }
        }
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 430.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun ColumnScope.EditableProfileContent(
    state: ProfileUiState,
    role: String,
    accent: Color,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    ProfileAvatar(
        initial = state.name.initial().ifBlank { role },
        photoUrl = state.photoUrl,
        accent = accent,
        size = 80,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    OutlinedButton(
        onClick = onPhotoClick,
        modifier = Modifier.align(Alignment.CenterHorizontally).height(38.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = Blue)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.change_photo), style = MaterialTheme.typography.bodySmall)
    }
    Spacer(Modifier.height(2.dp))
    ProfileField(
        label = stringResource(R.string.profile_label_name),
        value = state.name,
        onValueChange = onNameChange
    )
    state.nameError?.let { FormError(it) }
    ProfileField(
        label = stringResource(R.string.register_label_username),
        value = state.username,
        onValueChange = onUsernameChange
    )
    state.usernameError?.let { FormError(it) }
    ProfileField(
        label = stringResource(R.string.profile_label_email),
        value = state.email,
        onValueChange = onEmailChange
    )
    state.emailError?.let { FormError(it) }
    ProfileField(
        label = stringResource(R.string.user_label_role),
        value = if (role == "A") stringResource(R.string.dashboard_admin) else if (role == "G") stringResource(R.string.dashboard_manager) else stringResource(R.string.dashboard_user),
        enabled = false
    )
    ProfileField(
        label = stringResource(R.string.new_password),
        value = state.newPassword,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.keep_current_password)
    )
    state.passwordError?.let { FormError(it) }
    state.errorMessage?.let { FormError(it) }
    state.successMessage?.let {
        Text(it, color = Green, style = MaterialTheme.typography.bodySmall)
    }
    Button(
        onClick = onSave,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(Blue),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        } else {
            Text(stringResource(R.string.save_changes))
        }
    }
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Text(stringResource(R.string.btn_cancel))
    }
}

@Composable
fun ProjectFormScreen(nav: NavController, edit: Boolean) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    var name by rememberSaveable(project?.id, edit) { androidx.compose.runtime.mutableStateOf(if (edit) project?.name.orEmpty() else "") }
    var description by rememberSaveable(project?.id, edit) { androidx.compose.runtime.mutableStateOf(if (edit) project?.description.orEmpty() else "") }
    FormScreen(if (edit) stringResource(R.string.edit_project) else stringResource(R.string.create_project), onBack = { nav.popBackStack() }) {
        SyncStatus(state)
        Field(stringResource(R.string.project_label_name), name, onValueChange = { name = it })
        Field(stringResource(R.string.project_label_description), description, onValueChange = { description = it }, minLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Field(stringResource(R.string.start_date), "", Modifier.weight(1f))
            Field(stringResource(R.string.end_date), "", Modifier.weight(1f))
        }
        Field(stringResource(R.string.project_label_manager), if (edit) "" else stringResource(R.string.assign_manager_hint))
        Field(stringResource(R.string.project_label_status), "")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.saveProject(
                        existing = if (edit) project else null,
                        name = name,
                        description = description,
                        managerId = project?.managerId,
                        onDone = { nav.popBackStack() }
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_project))
            }
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    }
}

@Composable
fun AdminProjectDetailsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    val projectTasks = state.tasks.filter { it.projectId == project?.id }
    FormScreen(stringResource(R.string.project_details), onBack = { nav.popBackStack() }) {
        if (project == null) {
            EmptyData()
            return@FormScreen
        }
        SectionCard(project.name) {
            StatusPill(project.status.name, project.status.color())
            Text(project.description.orEmpty(), color = Muted)
            TwoMetrics(stringResource(R.string.start_date), project.startDate.displayDate(), stringResource(R.string.end_date), project.endDate.displayDate())
            TwoMetrics(stringResource(R.string.tasks_title), projectTasks.size.toString(), stringResource(R.string.dashboard_completed), projectTasks.count { it.status == TaskStatus.COMPLETED }.toString())
            val rate = projectTasks.completionRate()
            ProgressLine(stringResource(R.string.progress_label), "$rate%", rate / 100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { nav.navigate(Routes.ADMIN_PROJECT_EDIT) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Blue)) { Text(stringResource(R.string.edit_project)) }
                OutlinedButton(
                    onClick = { viewModel.deleteProject(project) { nav.popBackStack() } },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
                ) { Text(stringResource(R.string.btn_delete)) }
            }
        }
        SectionCard(stringResource(R.string.project_team_count, state.users.take(3).size)) {
            state.users.take(3).forEach { user ->
                val demo = user.toDemoUser()
                CompactUser(demo.name, demo.initial, demo.color)
            }
            if (state.users.isEmpty()) EmptyData()
        }
    }
}

@Composable
fun UserFormScreen(nav: NavController, edit: Boolean) {
    val state by taskFlowState()
    val user = state.users.firstOrNull()
    FormScreen(if (edit) stringResource(R.string.edit_user) else stringResource(R.string.create_user), onBack = { nav.popBackStack() }) {
        Avatar(if (edit) user?.name.initial() else "", if (edit) user?.toDemoUser()?.color ?: Green else Color(0xFFE5E7EB), size = 82, camera = !edit)
        TextButton(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (edit) stringResource(R.string.change_photo) else stringResource(R.string.add_photo), color = Blue)
        }
        Field(stringResource(R.string.full_name), if (edit) user?.name.orEmpty() else "")
        Field(stringResource(R.string.register_label_username), if (edit) user?.username.orEmpty() else "")
        Field(stringResource(R.string.profile_label_email), if (edit) user?.email.orEmpty() else "")
        Field(stringResource(R.string.user_label_role), "")
        if (edit) Field(stringResource(R.string.status_label), "")
        Field(if (edit) stringResource(R.string.new_password) else stringResource(R.string.password_label), if (edit) stringResource(R.string.keep_current_password) else stringResource(R.string.minimum_password))
        if (!edit) Field(stringResource(R.string.confirm_password_label), stringResource(R.string.repeat_password))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(Blue), shape = RoundedCornerShape(8.dp)) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_user))
            }
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    }
}

@Composable
fun ManagerDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val state by taskFlowState()
    AppScaffold(
        role = "G",
        accent = Green,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.MANAGER_PROFILE) }
    ) {
        Welcome(stringResource(R.string.dashboard_manager))
        SyncStatus(state)
        val pending = state.tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
        val completed = state.tasks.count { it.status == TaskStatus.COMPLETED }
        StatNavCard(Icons.Default.CheckBox, stringResource(R.string.tasks_title), state.tasks.size.toString(), stringResource(R.string.dashboard_tasks_detail, pending, completed), Blue) { nav.navigate(Routes.MANAGER_TASKS_LIST) }
        StatNavCard(Icons.Default.Group, stringResource(R.string.team_title), state.users.size.toString(), stringResource(R.string.dashboard_active_members), Green) { nav.navigate(Routes.MANAGER_TEAM) }
        StatNavCard(Icons.Default.Work, stringResource(R.string.dashboard_my_projects), state.projects.size.toString(), stringResource(R.string.dashboard_active_projects, state.projects.count { it.status == ProjectStatus.ACTIVE }), Purple) { nav.navigate(Routes.MANAGER_PROJECTS) }
        SectionCard(stringResource(R.string.dashboard_priority_tasks)) {
            state.tasks.take(3).forEach { task ->
                TaskPriorityLine(task.priority.name, task.title, task.priority.color())
            }
            if (state.tasks.isEmpty()) EmptyData()
        }
    }
}

@Composable
fun ManagerTasksListScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(stringResource(R.string.task_management), stringResource(R.string.new_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_TASK_CREATE) }) {
        SyncStatus(state)
        SearchField(stringResource(R.string.search_tasks_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = {}, label = { Text(stringResource(R.string.pending_tasks_metric)) })
            FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.completed_filter)) })
        }
        state.tasks.forEach { task ->
            ManagerTaskCard(task, state.projects, { 
                viewModel.selectTask(task.id)
                nav.navigate(Routes.MANAGER_ASSIGN_USERS)
            }, {
                viewModel.selectTask(task.id)
                nav.navigate(Routes.MANAGER_TASK_EDIT)
            })
        }
        if (state.tasks.isEmpty()) EmptyData()
    }
}

@Composable
fun ManagerTeamScreen(nav: NavController) {
    val state by taskFlowState()
    ListScreen(stringResource(R.string.team_title), stringResource(R.string.add_action), { nav.popBackStack() }, { nav.navigate(Routes.MANAGER_ADD_TEAM) }) {
        SyncStatus(state)
        state.users.forEach { user ->
            TeamMemberCard(user.toDemoUser()) { nav.navigate(Routes.MANAGER_EVALUATE_USER) }
        }
        if (state.users.isEmpty()) EmptyData()
    }
}

@Composable
fun ManagerProjectsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    ListScreen(stringResource(R.string.dashboard_my_projects), null, { nav.popBackStack() }, {}) {
        SyncStatus(state)
        state.projects.forEach { project ->
            ProjectSummary(project, state.tasks.filter { it.projectId == project.id }) {
                viewModel.selectProject(project.id)
                nav.navigate(Routes.MANAGER_PROJECT_DETAILS)
            }
        }
        if (state.projects.isEmpty()) EmptyData()
    }
}

@Composable
fun ManagerProjectDetailsScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    val projectTasks = state.tasks.filter { it.projectId == project?.id }
    FormScreen(stringResource(R.string.project_details), { nav.popBackStack() }) {
        if (project == null) {
            EmptyData()
            return@FormScreen
        }
        SectionCard(project.name) {
            StatusPill(project.status.name, project.status.color())
            Text(stringResource(R.string.deadline_prefix, project.endDate.displayDate()), color = Muted)
            Text(project.description.orEmpty(), color = Muted)
            TwoMetrics(stringResource(R.string.tasks_title), projectTasks.size.toString(), stringResource(R.string.dashboard_completed), projectTasks.count { it.status == TaskStatus.COMPLETED }.toString())
            val rate = projectTasks.completionRate()
            ProgressLine(stringResource(R.string.progress_label), "$rate%", rate / 100f)
            Button(onClick = { viewModel.completeProject(project) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Green), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.complete_project))
            }
        }
        SectionCard(stringResource(R.string.tasks_title)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text(stringResource(R.string.pending_tasks_metric)) })
                FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.completed_filter)) })
            }
            OutlinedButton(onClick = { nav.navigate(Routes.MANAGER_TASKS_LIST) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.view_all_tasks))
            }
        }
        SectionCard(stringResource(R.string.project_team_count, state.users.take(3).size)) {
            state.users.take(3).forEach { EvalLine(it.toDemoUser()) { nav.navigate(Routes.MANAGER_EVALUATE_USER) } }
            if (state.users.isEmpty()) EmptyData()
            OutlinedButton(onClick = { nav.navigate(Routes.MANAGER_TEAM) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.view_full_team))
            }
        }
    }
}

@Composable
fun ManagerTaskDetailsScreen(nav: NavController) {
    UserTaskDetailsScreen(nav = nav, managerMode = true)
}

@Composable
fun TaskFormScreen(nav: NavController, edit: Boolean) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    val project = state.projects.firstOrNull { it.id == task?.projectId } ?: state.projects.firstOrNull()
    var title by rememberSaveable(task?.id, edit) { androidx.compose.runtime.mutableStateOf(if (edit) task?.title.orEmpty() else "") }
    var description by rememberSaveable(task?.id, edit) { androidx.compose.runtime.mutableStateOf(if (edit) task?.description.orEmpty() else "") }
    FormScreen(if (edit) stringResource(R.string.edit_task_title) else stringResource(R.string.create_task_title), { nav.popBackStack() }) {
        SyncStatus(state)
        Field(stringResource(R.string.task_title_label), title, onValueChange = { title = it })
        Field(stringResource(R.string.description_label), description, onValueChange = { description = it }, minLines = 3)
        Field(stringResource(R.string.project_label_name), project?.name.orEmpty(), enabled = false)
        Field(stringResource(R.string.priority_label), "")
        Field(stringResource(R.string.deadline_label), "")
        Field(stringResource(R.string.status_label), "")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.saveTask(
                        existing = if (edit) task else null,
                        project = project,
                        title = title,
                        description = description,
                        onDone = { nav.popBackStack() }
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_task_title))
            }
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    }
}

@Composable
fun AssignUsersScreen(nav: NavController) {
    val state by taskFlowState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    FormScreen(stringResource(R.string.assign_users), { nav.popBackStack() }) {
        SectionCard(stringResource(R.string.task_title)) {
            Text(task?.title.orEmpty(), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.project_prefix, state.projects.firstOrNull { it.id == task?.projectId }?.name.orEmpty()), color = Muted)
        }
        SectionCard(stringResource(R.string.assigned_users)) {
            state.users.take(2).forEach { Assigned(it.name, it.name.initial(), Blue) }
            if (state.users.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_to_assign)) {
            state.users.drop(2).forEach { AvailableUser(it.toDemoUser()) }
            if (state.users.drop(2).isEmpty()) EmptyData()
        }
    }
}

@Composable
fun AddTeamScreen(nav: NavController) {
    val state by taskFlowState()
    FormScreen(stringResource(R.string.add_to_team), { nav.popBackStack() }) {
        SectionCard(stringResource(R.string.select_project)) {
            MiniSelect(state.projects.firstOrNull()?.name ?: stringResource(R.string.no_projects_synced))
            Text(stringResource(R.string.assign_project_users_hint), color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        SectionCard(stringResource(R.string.current_project_team, state.users.take(3).size)) {
            state.users.take(3).forEach { user ->
                val demo = user.toDemoUser()
                Assigned(demo.name, demo.initial, demo.color)
            }
            if (state.users.isEmpty()) EmptyData()
        }
        SectionCard(stringResource(R.string.available_users)) {
            SearchField(stringResource(R.string.search_users_hint))
            state.users.drop(2).forEach { AvailableUser(it.toDemoUser()) }
            if (state.users.drop(2).isEmpty()) EmptyData()
        }
    }
}

@Composable
fun EvaluateUserScreen(nav: NavController) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val user = state.users.firstOrNull()
    val project = state.projects.firstOrNull { it.id == state.selectedProjectId } ?: state.projects.firstOrNull()
    var rating by rememberSaveable { androidx.compose.runtime.mutableStateOf(5) }
    var comment by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    FormScreen(stringResource(R.string.evaluate_user), { nav.popBackStack() }) {
        SyncStatus(state)
        SectionCard("") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user?.name.initial(), user?.toDemoUser()?.color ?: Orange, 56)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(user?.name.orEmpty(), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.project_prefix, project?.name.orEmpty()), color = Muted)
                }
            }
        }
        SectionCard(stringResource(R.string.performance_evaluation)) {
            TwoMetrics(
                stringResource(R.string.completed_tasks_metric),
                state.tasks.count { it.status == TaskStatus.COMPLETED }.toString(),
                stringResource(R.string.active_tasks_metric),
                state.tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }.toString()
            )
            Text(stringResource(R.string.numeric_rating), fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = if (index < rating) Yellow else Muted,
                        modifier = Modifier.size(30.dp).clickable { rating = index + 1 }
                    )
                }
            }
            Field(stringResource(R.string.comment_optional), comment, onValueChange = { comment = it }, minLines = 4)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.saveEvaluation(user, project, rating, comment) { nav.popBackStack() } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(Blue)
                ) { Text(stringResource(R.string.save_evaluation)) }
                OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.btn_cancel)) }
            }
        }
    }
}

@Composable
fun UserDashboardScreen(nav: NavController, onLogout: () -> Unit) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    AppScaffold(
        role = "U",
        accent = Orange,
        onLogout = onLogout,
        onProfile = { nav.navigate(Routes.USER_PROFILE) }
    ) {
        Welcome(state.currentUser?.name ?: stringResource(R.string.dashboard_user))
        SyncStatus(state)
        val activeTasks = state.tasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SmallStat(Icons.Default.CheckBox, stringResource(R.string.dashboard_active_tasks), activeTasks.size.toString(), stringResource(R.string.dashboard_near_deadline, activeTasks.count { it.deadline.isNearDeadline() }), Orange, Modifier.weight(1f))
            SmallStat(Icons.Default.CalendarToday, stringResource(R.string.dashboard_completed), state.tasks.count { it.status == TaskStatus.COMPLETED }.toString(), stringResource(R.string.dashboard_total_synced), Blue, Modifier.weight(1f))
        }
        SectionCard(stringResource(R.string.pending_tasks_title)) {
            activeTasks.forEach { task ->
                UserTaskLine(task, state.projects) {
                    viewModel.selectTask(task.id)
                    nav.navigate(Routes.USER_TASK_DETAILS)
                }
            }
            if (activeTasks.isEmpty()) EmptyData()
        }
        OutlinedButton(onClick = { nav.navigate(Routes.USER_HISTORY) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp)) {
            Text(stringResource(R.string.view_completed_history))
        }
    }
}

@Composable
fun UserTaskDetailsScreen(nav: NavController, managerMode: Boolean = false) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val task = state.tasks.firstOrNull { it.id == state.selectedTaskId } ?: state.tasks.firstOrNull()
    val project = state.projects.firstOrNull { it.id == task?.projectId }
    var observationText by rememberSaveable(task?.id) { androidx.compose.runtime.mutableStateOf("") }
    FormScreen(stringResource(R.string.task_details), { nav.popBackStack() }) {
        SyncStatus(state)
        if (task == null) {
            EmptyData()
            return@FormScreen
        }
        SectionCard(task.title) {
            StatusPill(task.priority.name, task.priority.color())
            Text(stringResource(R.string.project_prefix, project?.name.orEmpty()), color = Muted)
            Text(task.description.orEmpty(), color = Muted)
            TwoMetrics(stringResource(R.string.deadline_label), task.deadline.displayDate(), stringResource(R.string.status_label), task.status.name)
            val progress = if (task.status == TaskStatus.COMPLETED) 100 else if (task.status == TaskStatus.IN_PROGRESS) 60 else 0
            TwoMetrics(stringResource(R.string.assigned_count), state.users.size.toString(), stringResource(R.string.progress_label), "$progress%")
            ProgressLine(stringResource(R.string.progress_label), "$progress%", progress / 100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (managerMode) nav.navigate(Routes.MANAGER_TASK_EDIT)
                        else viewModel.updateTaskStatus(task, TaskStatus.IN_PROGRESS)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(Blue)
                ) {
                    Text(if (managerMode) stringResource(R.string.edit_task_title) else stringResource(R.string.save_progress))
                }
                OutlinedButton(
                    onClick = {
                        if (managerMode) nav.navigate(Routes.MANAGER_ASSIGN_USERS)
                        else viewModel.createObservation(task, observationText) { observationText = "" }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (managerMode) stringResource(R.string.associate_action) else stringResource(R.string.new_observation_short))
                }
            }
        }
        if (!managerMode) {
            SectionCard(stringResource(R.string.task_progress_register)) {
                Field(stringResource(R.string.data_label), "")
                Field(stringResource(R.string.location_label), "")
                Field(stringResource(R.string.completion_percentage), "")
                Field(stringResource(R.string.time_spent), "")
                Field(stringResource(R.string.comment_optional), observationText, onValueChange = { observationText = it }, minLines = 3)
                Button(onClick = { viewModel.updateTaskStatus(task, TaskStatus.COMPLETED) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Blue)) {
                    Text(stringResource(R.string.save_progress))
                }
            }
        }
        SectionCard(stringResource(R.string.observations_count, state.observations.size)) {
            state.observations.forEach { observation ->
                val user = state.users.firstOrNull { it.id == observation.userId }
                Observation(user?.name.orEmpty(), observation.text.orEmpty(), observation.createdAt.relativeTime())
            }
            if (state.observations.isEmpty()) EmptyData()
        }
    }
}

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

private data class OnboardData(val title: String, val subtitle: String, val icon: ImageVector)
private data class DemoUser(val name: String, val email: String, val role: String, val initial: String, val color: Color)

@Composable
private fun AppScaffold(
    role: String,
    accent: Color,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(Page)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TaskFlow", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(22.dp))
                Box(modifier = Modifier.clickable(onClick = onProfile)) {
                    Avatar(role, accent, 34)
                }
                IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, null) }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@Composable
private fun ListScreen(title: String, actionText: String?, onBack: () -> Unit, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack, actionText, onAction)
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@Composable
private fun FormScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack)
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit, actionText: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (actionText != null) {
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(Blue), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(actionText)
            }
        }
    }
}

@Composable
private fun Welcome(name: String) {
    Column {
        Text(stringResource(R.string.dashboard_welcome), color = Muted)
        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun StatNavCard(icon: ImageVector, title: String, value: String, detail: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(108.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SmallStat(icon: ImageVector, title: String, value: String, detail: String, color: Color, modifier: Modifier) {
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
private fun SearchField(placeholder: String) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Border, unfocusedBorderColor = Border)
    )
}

@Composable
private fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier) {
        Label(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Border, unfocusedBorderColor = Border)
        )
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    placeholder: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier) {
        Label(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            enabled = enabled,
            placeholder = {
                Text(placeholder, color = Muted, style = MaterialTheme.typography.bodySmall)
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Border,
                unfocusedBorderColor = Border,
                disabledBorderColor = Border,
                disabledContainerColor = Soft,
                disabledTextColor = Color.Black
            )
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun MiniSelect(text: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Box(modifier.height(38.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
        Text(text, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Avatar(text: String, color: Color, size: Int, camera: Boolean = false) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        if (camera) Icon(Icons.Default.CameraAlt, null, tint = Muted) else Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileAvatar(
    initial: String,
    photoUrl: String?,
    accent: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(accent),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageURI(Uri.parse(photoUrl))
                    }
                },
                update = { image ->
                    image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setImageURI(Uri.parse(photoUrl))
                }
            )
        } else {
            Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FormError(text: String) {
    Text(text, color = Red, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun Dots(index: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (it == index) Blue else Color(0xFFD2DAE5)))
        }
    }
}

@Composable
private fun ProjectListItem(project: Project, allTasks: List<Task>, onDetails: () -> Unit) {
    val tasks = allTasks.filter { it.projectId == project.id }
    val percent = tasks.completionRate()
    SectionCard("") {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(project.name, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.manager_prefix, project.managerId?.toString().orEmpty()), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(project.status.name, project.status.color())
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.tasks_count, tasks.size), color = Muted, style = MaterialTheme.typography.bodySmall)
            Text("$percent%", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue, trackColor = Border)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.btn_edit)) }
            OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.details)) }
        }
    }
}

@Composable
private fun UserCard(user: DemoUser, showRole: Boolean, onEdit: () -> Unit, onRemove: () -> Unit) {
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
private fun StatusPill(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ActivityLine(text: String, time: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(Blue))
        Spacer(Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(time, color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Ranking(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
            Text(number, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun TwoMetrics(a: String, av: String, b: String, bv: String) {
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
private fun ProgressLine(label: String, value: String, progress: Float) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Blue, trackColor = Border)
    }
}

@Composable
private fun CompactUser(name: String, initial: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(initial, color, 28)
        Spacer(Modifier.width(8.dp))
        Text(name)
    }
}

@Composable
private fun TaskPriorityLine(level: String, title: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusPill(level, color)
        Spacer(Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
private fun ManagerTaskCard(task: Task, projects: List<Project>, onAssign: () -> Unit, onEdit: () -> Unit) {
    val progress = if (task.status == TaskStatus.COMPLETED) 1f else if (task.status == TaskStatus.IN_PROGRESS) 0.6f else 0f
    SectionCard("") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(task.priority.name, task.priority.color())
        }
        Text(task.status.name, color = Muted, style = MaterialTheme.typography.bodySmall)
        ProgressLine(stringResource(R.string.progress_label), "${(progress * 100).toInt()}%", progress)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onAssign, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.associate_action)) }
            Button(onClick = onEdit, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Blue)) { Text(stringResource(R.string.btn_edit)) }
        }
    }
}

@Composable
private fun TeamMemberCard(user: DemoUser, onEvaluate: () -> Unit) {
    SectionCard("") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(user.initial, user.color, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.role, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.view_tasks)) }
            Button(onClick = onEvaluate, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Blue)) { Text(stringResource(R.string.evaluate_action)) }
        }
    }
}

@Composable
private fun ProjectSummary(project: Project, tasks: List<Task>, onDetails: () -> Unit) {
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
private fun EvalLine(user: DemoUser, onEvaluate: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(user.initial, user.color, 32)
        Spacer(Modifier.width(8.dp))
        Text(user.name, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onEvaluate) { Text(stringResource(R.string.evaluate_action)) }
    }
}

@Composable
private fun Assigned(name: String, initial: String, color: Color) {
    Row(Modifier.fillMaxWidth().background(Color(0xFFEAF2FF), RoundedCornerShape(8.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(initial, color, 28)
        Spacer(Modifier.width(8.dp))
        Text(name, modifier = Modifier.weight(1f))
        Icon(Icons.Default.Delete, null, tint = Red, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AvailableUser(user: DemoUser) {
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
private fun UserTaskLine(task: Task, projects: List<Project>, onClick: () -> Unit) {
    val progress = if (task.status == TaskStatus.COMPLETED) 1f else if (task.status == TaskStatus.IN_PROGRESS) 0.6f else 0f
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(projects.firstOrNull { it.id == task.projectId }?.name.orEmpty(), color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Text(task.deadline.displayDate(), color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        ProgressLine("", "${(progress * 100).toInt()}%", progress)
    }
}

@Composable
private fun Observation(name: String, text: String, time: String) {
    Column(Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(time, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(text, color = Muted)
    }
}

@Composable
private fun HistoryCard(title: String, project: String, time: String, date: String, rating: Int) {
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

@Composable
private fun taskFlowState(): androidx.compose.runtime.State<TaskFlowDataUiState> {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    return viewModel.uiState.collectAsState()
}

@Composable
private fun SyncStatus(state: TaskFlowDataUiState) {
    when {
        state.isRefreshing -> Text(stringResource(R.string.syncing_data), color = Muted, style = MaterialTheme.typography.bodySmall)
        state.refreshError != null -> Text(state.refreshError, color = Red, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyData() {
    Text(stringResource(R.string.empty_synced_data), color = Muted, style = MaterialTheme.typography.bodyMedium)
}

private fun User.toDemoUser(): DemoUser {
    val primaryRole = roles.firstOrNull() ?: role
    val color = when (primaryRole.name) {
        "ADMIN" -> Blue
        "MANAGER" -> Green
        else -> Orange
    }

    return DemoUser(
        name = name,
        email = email,
        role = primaryRole.name,
        initial = name.initial(),
        color = color
    )
}

private fun String?.initial(): String =
    this
        ?.trim()
        ?.firstOrNull()
        ?.uppercase()
        ?: ""

private fun List<Task>.completionRate(): Int {
    if (isEmpty()) return 0
    return count { it.status == TaskStatus.COMPLETED } * 100 / size
}

private fun List<com.taskflow.app.domain.model.Evaluation>.averageRating(): String {
    if (isEmpty()) return ""
    return "%.1f".format(map { it.rating }.average())
}

private fun Long?.displayDate(): String {
    if (this == null || this == 0L) return ""
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(this))
}

private fun Long?.isNearDeadline(): Boolean {
    if (this == null) return false
    val now = System.currentTimeMillis()
    val sevenDays = 7L * 24L * 60L * 60L * 1000L
    return this in now..(now + sevenDays)
}

private fun Long.relativeTime(): String {
    val diff = (System.currentTimeMillis() - this).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> if (java.util.Locale.getDefault().language == "pt") "agora" else "now"
    }
}

private fun ProjectStatus.color(): Color =
    when (this) {
        ProjectStatus.ACTIVE -> Green
        ProjectStatus.COMPLETED -> Color(0xFF9CA3AF)
        ProjectStatus.CANCELLED -> Red
    }

private fun TaskPriority.color(): Color =
    when (this) {
        TaskPriority.LOW -> Green
        TaskPriority.MEDIUM -> Orange
        TaskPriority.HIGH -> Red
        TaskPriority.CRITICAL -> Purple
    }
