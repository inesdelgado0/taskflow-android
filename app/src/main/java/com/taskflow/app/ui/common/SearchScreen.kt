package com.taskflow.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Barra de pesquisa ─────────────────────────────────
        SearchBar(
            query    = state.query,
            onQuery  = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ── Tabs ──────────────────────────────────────────────
        SearchTabs(
            active   = state.activeTab,
            onChange = viewModel::onTabChange
        )

        // ── Filtros por tab ───────────────────────────────────
        when (state.activeTab) {
            SearchTab.PROJECTS -> ProjectFilters(
                status   = state.projectStatus,
                onStatus = viewModel::onProjectStatusFilter
            )
            SearchTab.TASKS -> TaskFilters(
                status    = state.taskStatus,
                priority  = state.taskPriority,
                onStatus  = viewModel::onTaskStatusFilter,
                onPriority = viewModel::onTaskPriorityFilter
            )
            SearchTab.USERS -> Unit
        }

        // ── Resultados ────────────────────────────────────────
        when (state.activeTab) {
            SearchTab.PROJECTS -> ProjectResults(state.projects)
            SearchTab.TASKS    -> TaskResults(state.tasks)
            SearchTab.USERS    -> UserResults(state.users)
        }
    }
}

// ── Search Bar ────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQuery,
        modifier      = modifier,
        placeholder   = { Text("Pesquisar...") },
        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon  = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
            }
        },
        singleLine = true
    )
}

// ── Tabs ──────────────────────────────────────────────────────

@Composable
private fun SearchTabs(
    active: SearchTab,
    onChange: (SearchTab) -> Unit
) {
    TabRow(selectedTabIndex = active.ordinal) {
        SearchTab.entries.forEach { tab ->
            Tab(
                selected = active == tab,
                onClick  = { onChange(tab) },
                text     = {
                    Text(when (tab) {
                        SearchTab.PROJECTS -> "Projetos"
                        SearchTab.TASKS    -> "Tarefas"
                        SearchTab.USERS    -> "Utilizadores"
                    })
                }
            )
        }
    }
}

// ── Filtros Projetos ──────────────────────────────────────────

@Composable
private fun ProjectFilters(
    status: ProjectStatus?,
    onStatus: (ProjectStatus?) -> Unit
) {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProjectStatus.entries.forEach { s ->
            FilterChip(
                selected = status == s,
                onClick  = { onStatus(if (status == s) null else s) },
                label    = { Text(s.name) }
            )
        }
    }
}

// ── Filtros Tarefas ───────────────────────────────────────────

@Composable
private fun TaskFilters(
    status: TaskStatus?,
    priority: TaskPriority?,
    onStatus: (TaskStatus?) -> Unit,
    onPriority: (TaskPriority?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {

        // Status
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskStatus.entries.forEach { s ->
                FilterChip(
                    selected = status == s,
                    onClick  = { onStatus(if (status == s) null else s) },
                    label    = { Text(s.name) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Prioridade
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskPriority.entries.forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick  = { onPriority(if (priority == p) null else p) },
                    label    = { Text(p.name) }
                )
            }
        }
    }
}

// ── Resultados ────────────────────────────────────────────────

@Composable
private fun ProjectResults(projects: List<Project>) {
    if (projects.isEmpty()) {
        EmptyState("Nenhum projeto encontrado")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(projects, key = { it.id }) { project ->
            ProjectResultCard(project)
        }
    }
}

@Composable
private fun TaskResults(tasks: List<Task>) {
    if (tasks.isEmpty()) {
        EmptyState("Nenhuma tarefa encontrada")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tasks, key = { it.id }) { task ->
            TaskResultCard(task)
        }
    }
}

@Composable
private fun UserResults(users: List<User>) {
    if (users.isEmpty()) {
        EmptyState("Nenhum utilizador encontrado")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users, key = { it.id }) { user ->
            UserResultCard(user)
        }
    }
}

// ── Cards ─────────────────────────────────────────────────────

@Composable
private fun ProjectResultCard(project: Project) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(project.name, style = MaterialTheme.typography.titleMedium)
            project.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label   = { Text(project.status.name) }
            )
        }
    }
}

@Composable
private fun TaskResultCard(task: Task) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            task.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(task.status.name) })
                AssistChip(onClick = {}, label = { Text(task.priority.name) })
            }
        }
    }
}

@Composable
private fun UserResultCard(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier         = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleMedium)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = {}, label = { Text(user.role.name) })
        }
    }
}

// ── Empty State ───────────────────────────────────────────────

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}