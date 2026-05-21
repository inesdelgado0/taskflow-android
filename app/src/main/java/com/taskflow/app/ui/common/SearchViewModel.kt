package com.taskflow.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject


data class SearchUiState(
    val query: String                  = "",
    val activeTab: SearchTab           = SearchTab.PROJECTS,

    // Filtros projetos
    val projectStatus: ProjectStatus?  = null,

    // Filtros tarefas
    val taskStatus: TaskStatus?        = null,
    val taskPriority: TaskPriority?    = null,
    val taskProjectId: Long?           = null,

    // Resultados
    val projects: List<Project>        = emptyList(),
    val tasks: List<Task>              = emptyList(),
    val users: List<User>              = emptyList(),

    val isLoading: Boolean             = false
)

enum class SearchTab { PROJECTS, TASKS, USERS }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    // Debounce de 300ms para não pesquisar a cada tecla
    private val queryFlow = _state
        .map { it.query }
        .debounce(300)
        .distinctUntilChanged()

    init {
        observeProjects()
        observeTasks()
        observeUsers()
    }

    // ── Events ────────────────────────────────────────────────

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onTabChange(tab: SearchTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    fun onProjectStatusFilter(status: ProjectStatus?) {
        _state.update { it.copy(projectStatus = status) }
    }

    fun onTaskStatusFilter(status: TaskStatus?) {
        _state.update { it.copy(taskStatus = status) }
    }

    fun onTaskPriorityFilter(priority: TaskPriority?) {
        _state.update { it.copy(taskPriority = priority) }
    }

    fun onTaskProjectFilter(projectId: Long?) {
        _state.update { it.copy(taskProjectId = projectId) }
    }

    fun clearFilters() {
        _state.update { it.copy(
            projectStatus = null,
            taskStatus    = null,
            taskPriority  = null,
            taskProjectId = null
        )}
    }

    // ── Observers ─────────────────────────────────────────────

    private fun observeProjects() {
        combine(
            queryFlow,
            _state.map { it.projectStatus }.distinctUntilChanged()
        ) { query, status -> query to status }
            .flatMapLatest { (query, status) ->
                if (status != null) {
                    projectRepository.getProjectsByStatusFlow(status)
                        .map { list ->
                            if (query.isBlank()) list
                            else list.filter { it.name.contains(query, ignoreCase = true) }
                        }
                } else {
                    projectRepository.searchProjectsFlow(query)
                }
            }
            .onEach { projects -> _state.update { it.copy(projects = projects) } }
            .launchIn(viewModelScope)
    }

    private fun observeTasks() {
        combine(
            queryFlow,
            _state.map { Triple(it.taskStatus, it.taskPriority, it.taskProjectId) }
                .distinctUntilChanged()
        ) { query, filters -> query to filters }
            .flatMapLatest { (query, filters) ->
                val (status, priority, projectId) = filters
                taskRepository.searchTasksFlow(query, projectId, status, priority)
            }
            .onEach { tasks -> _state.update { it.copy(tasks = tasks) } }
            .launchIn(viewModelScope)
    }

    private fun observeUsers() {
        queryFlow
            .flatMapLatest { query -> userRepository.searchUsersFlow(query) }
            .onEach { users -> _state.update { it.copy(users = users) } }
            .launchIn(viewModelScope)
    }
}