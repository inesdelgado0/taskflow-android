package com.taskflow.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.Observation
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.usecase.sync.PopulateLocalDatabaseUseCase
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskFlowDataUiState(
    val projects: List<Project> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val users: List<User> = emptyList(),
    val observations: List<Observation> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val selectedProjectId: Long? = null,
    val selectedTaskId: Long? = null,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null
)

private data class TaskFlowDataCore(
    val projects: List<Project>,
    val tasks: List<Task>,
    val users: List<User>,
    val observations: List<Observation>,
    val evaluations: List<Evaluation>
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskFlowDataViewModel @Inject constructor(
    private val populateLocalDatabase: PopulateLocalDatabaseUseCase,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val observationRepository: ObservationRepository,
    private val evaluationRepository: EvaluationRepository
) : ViewModel() {
    private val selectedProjectId = MutableStateFlow<Long?>(null)
    private val selectedTaskId = MutableStateFlow<Long?>(null)
    private val transient = MutableStateFlow(TaskFlowDataUiState())

    private val projects = projectRepository.getAllProjectsFlow()
    private val users = userRepository.getAllUsersFlow()
    private val tasks = selectedProjectId.flatMapLatest { projectId ->
        if (projectId == null || projectId == 0L) {
            taskRepository.searchTasksFlow("")
        } else {
            taskRepository.getTasksByProjectFlow(projectId)
        }
    }
    private val observations = selectedTaskId.flatMapLatest { taskId ->
        if (taskId == null || taskId == 0L) flowOf(emptyList())
        else observationRepository.getObservationsByTaskFlow(taskId)
    }
    private val evaluations = selectedProjectId.flatMapLatest { projectId ->
        if (projectId == null || projectId == 0L) flowOf(emptyList())
        else evaluationRepository.getEvaluationsByProjectFlow(projectId)
    }

    private val core = combine(
        projects,
        tasks,
        users,
        observations,
        evaluations
    ) { projectsValue, tasksValue, usersValue, observationsValue, evaluationsValue ->
        val projectId = selectedProjectId.value
            .takeIf { id -> id != null && projectsValue.any { it.id == id } }
            ?: projectsValue.firstOrNull()?.id

        if (projectId != selectedProjectId.value) {
            selectedProjectId.value = projectId
        }

        val taskId = selectedTaskId.value
            .takeIf { id -> id != null && tasksValue.any { it.id == id } }
            ?: tasksValue.firstOrNull()?.id

        if (taskId != selectedTaskId.value) {
            selectedTaskId.value = taskId
        }

        TaskFlowDataCore(
            projects = projectsValue,
            tasks = tasksValue,
            users = usersValue,
            observations = observationsValue,
            evaluations = evaluationsValue
        )
    }

    val uiState: StateFlow<TaskFlowDataUiState> = combine(core, transient) { coreValue, transientValue ->
        transientValue.copy(
            projects = coreValue.projects,
            tasks = coreValue.tasks,
            users = coreValue.users,
            observations = coreValue.observations,
            evaluations = coreValue.evaluations,
            selectedProjectId = selectedProjectId.value,
            selectedTaskId = selectedTaskId.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskFlowDataUiState(isRefreshing = true)
    )

    init {
        refreshFromApi()
    }

    fun refreshFromApi() {
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            populateLocalDatabase()
                .onSuccess {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                }
                .onFailure { error ->
                    transient.update {
                        it.copy(
                            isRefreshing = false,
                            refreshError = error.message ?: "Nao foi possivel sincronizar dados."
                        )
                    }
                }
        }
    }

    fun selectProject(projectId: Long) {
        selectedProjectId.value = projectId
    }

    fun selectTask(taskId: Long) {
        selectedTaskId.value = taskId
    }

    fun saveProject(
        existing: Project?,
        name: String,
        description: String?,
        managerId: Long?,
        status: ProjectStatus = ProjectStatus.ACTIVE,
        onDone: () -> Unit = {}
    ) {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val now = System.currentTimeMillis()
            val creatorId = existing?.createdBy ?: uiState.value.users.firstOrNull()?.id ?: 1L
            val project = Project(
                id = existing?.id ?: 0L,
                name = cleanedName,
                description = description?.trim()?.ifBlank { null },
                startDate = existing?.startDate,
                endDate = existing?.endDate,
                status = existing?.status ?: status,
                managerId = managerId ?: existing?.managerId,
                createdBy = creatorId,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )

            when (val result = projectRepository.pushProject(project)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
                }
            }
        }
    }

    fun deleteProject(project: Project?, onDone: () -> Unit = {}) {
        val id = project?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = projectRepository.deleteProjectRemote(id)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun completeProject(project: Project?) {
        val id = project?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = projectRepository.completeProjectRemote(id)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun saveTask(
        existing: Task?,
        project: Project?,
        title: String,
        description: String?,
        priority: TaskPriority = TaskPriority.MEDIUM,
        status: TaskStatus = TaskStatus.PENDING,
        onDone: () -> Unit = {}
    ) {
        val cleanedTitle = title.trim()
        val projectId = existing?.projectId ?: project?.id ?: return
        if (cleanedTitle.isBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val now = System.currentTimeMillis()
            val creatorId = existing?.createdBy ?: uiState.value.users.firstOrNull()?.id ?: 1L
            val task = Task(
                id = existing?.id ?: 0L,
                projectId = projectId,
                title = cleanedTitle,
                description = description?.trim()?.ifBlank { null },
                priority = existing?.priority ?: priority,
                deadline = existing?.deadline,
                status = existing?.status ?: status,
                createdBy = creatorId,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )

            when (val result = taskRepository.pushTask(task)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun updateTaskStatus(task: Task?, status: TaskStatus) {
        val id = task?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = taskRepository.updateTaskStatusRemote(id, status)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun deleteTask(task: Task?, onDone: () -> Unit = {}) {
        val id = task?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = taskRepository.deleteTaskRemote(id)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun createObservation(task: Task?, text: String, onDone: () -> Unit = {}) {
        val targetTask = task ?: return
        val userId = uiState.value.users.firstOrNull()?.id ?: return
        val cleanedText = text.trim()
        if (cleanedText.isBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val observation = Observation(
                taskId = targetTask.id,
                userId = userId,
                text = cleanedText,
                photoPath = null,
                createdAt = System.currentTimeMillis()
            )

            when (val result = observationRepository.pushObservation(observation)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun saveEvaluation(user: User?, project: Project?, rating: Int, comment: String?, onDone: () -> Unit = {}) {
        val evaluated = user ?: return
        val targetProject = project ?: return
        val evaluatorId = uiState.value.users.firstOrNull { candidate -> candidate.id != evaluated.id }?.id
            ?: uiState.value.users.firstOrNull()?.id
            ?: return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val evaluation = Evaluation(
                projectId = targetProject.id,
                evaluatorId = evaluatorId,
                evaluatedUserId = evaluated.id,
                rating = rating.coerceIn(1, 5),
                comment = comment?.trim()?.ifBlank { null },
                createdAt = System.currentTimeMillis()
            )

            when (val result = evaluationRepository.pushEvaluation(evaluation)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }
}
