package com.taskflow.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.dao.UserProjectDao
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.local.entity.UserProjectEntity
import com.taskflow.app.data.local.entity.UserTaskEntity
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
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
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
    val currentUser: User? = null,
    val observations: List<Observation> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val allEvaluations: List<Evaluation> = emptyList(),
    val userProjectAssignments: List<UserProjectEntity> = emptyList(),
    val userTaskAssignments: List<UserTaskEntity> = emptyList(),
    val selectedProjectUserIds: List<Long> = emptyList(),
    val selectedTaskUserIds: List<Long> = emptyList(),
    val selectedProjectId: Long? = null,
    val selectedTaskId: Long? = null,
    val selectedUserId: Long? = null,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null
)

private data class TaskFlowDataCore(
    val projects: List<Project>,
    val tasks: List<Task>,
    val users: List<User>,
    val observations: List<Observation>,
    val evaluations: List<Evaluation>,
    val allEvaluations: List<Evaluation>,
    val userProjectAssignments: List<UserProjectEntity>,
    val userTaskAssignments: List<UserTaskEntity>,
    val selectedProjectUserIds: List<Long>,
    val selectedTaskUserIds: List<Long>
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskFlowDataViewModel @Inject constructor(
    private val populateLocalDatabase: PopulateLocalDatabaseUseCase,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val observationRepository: ObservationRepository,
    private val evaluationRepository: EvaluationRepository,
    private val userProjectDao: UserProjectDao,
    private val userTaskDao: UserTaskDao,
    private val tokenManager: TokenManager
) : ViewModel() {
    companion object {
        @Volatile
        private var initialRefreshDone = false
    }

    private val selectedProjectId = MutableStateFlow<Long?>(null)
    private val selectedTaskId = MutableStateFlow<Long?>(null)
    private val selectedUserId = MutableStateFlow<Long?>(null)
    private val currentUserId = MutableStateFlow<Long?>(null)
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
    private val allEvaluations = evaluationRepository.getAllEvaluationsFlow()
    private val selectedProjectUsers = selectedProjectId.flatMapLatest { projectId ->
        if (projectId == null || projectId == 0L) flowOf(emptyList())
        else userProjectDao.getUsersByProjectFlow(projectId)
    }
    private val selectedTaskUsers = selectedTaskId.flatMapLatest { taskId ->
        if (taskId == null || taskId == 0L) flowOf(emptyList())
        else userTaskDao.getUsersByTaskFlow(taskId)
    }
    private val userProjectAssignments = userProjectDao.getAllFlow()
    private val userTaskAssignments = userTaskDao.getAllFlow()

    private val core = combine(
        projects,
        tasks,
        users,
        observations,
        evaluations,
        allEvaluations,
        userProjectAssignments,
        userTaskAssignments,
        selectedProjectUsers,
        selectedTaskUsers
    ) { values ->
        val projectsValue = values[0] as List<Project>
        val tasksValue = values[1] as List<Task>
        val usersValue = values[2] as List<User>
        val observationsValue = values[3] as List<Observation>
        val evaluationsValue = values[4] as List<Evaluation>
        val allEvaluationsValue = values[5] as List<Evaluation>
        val userProjectAssignmentsValue = values[6] as List<UserProjectEntity>
        val userTaskAssignmentsValue = values[7] as List<UserTaskEntity>
        val selectedProjectUsersValue = values[8] as List<UserProjectEntity>
        val selectedTaskUsersValue = values[9] as List<UserTaskEntity>
        val projectId = selectedProjectId.value
            .takeIf { id -> id != null && projectsValue.any { it.id == id } }

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
            evaluations = evaluationsValue,
            allEvaluations = allEvaluationsValue,
            userProjectAssignments = userProjectAssignmentsValue,
            userTaskAssignments = userTaskAssignmentsValue,
            selectedProjectUserIds = selectedProjectUsersValue.map { it.userId },
            selectedTaskUserIds = selectedTaskUsersValue.map { it.userId }
        )
    }

    val uiState: StateFlow<TaskFlowDataUiState> = combine(core, transient, currentUserId) { coreValue, transientValue, currentUserIdValue ->
        transientValue.copy(
            projects = coreValue.projects,
            tasks = coreValue.tasks,
            users = coreValue.users,
            currentUser = coreValue.users.firstOrNull { user -> user.id == currentUserIdValue },
            observations = coreValue.observations,
            evaluations = coreValue.evaluations,
            allEvaluations = coreValue.allEvaluations,
            userProjectAssignments = coreValue.userProjectAssignments,
            userTaskAssignments = coreValue.userTaskAssignments,
            selectedProjectUserIds = coreValue.selectedProjectUserIds,
            selectedTaskUserIds = coreValue.selectedTaskUserIds,
            selectedProjectId = selectedProjectId.value,
            selectedTaskId = selectedTaskId.value,
            selectedUserId = selectedUserId.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskFlowDataUiState(isRefreshing = true)
    )

    init {
        viewModelScope.launch {
            currentUserId.value = tokenManager.getUserId()
        }
        ensureInitialRefresh()
    }

    fun refreshFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            populateLocalDatabase()
                .onSuccess {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                }
                .onFailure { error ->
                    transient.update {
                        it.copy(
                            isRefreshing = false,
                            refreshError = null
                        )
                    }
                }
        }
    }

    private fun ensureInitialRefresh() {
        if (initialRefreshDone) return

        synchronized(TaskFlowDataViewModel::class.java) {
            if (initialRefreshDone) return
            initialRefreshDone = true
        }

        refreshFromApi()
    }

    fun selectProject(projectId: Long) {
        selectedProjectId.value = projectId
    }

    fun selectTask(taskId: Long) {
        selectedTaskId.value = taskId
    }

    fun selectUser(userId: Long) {
        selectedUserId.value = userId
    }

    fun saveProject(
        existing: Project?,
        name: String,
        description: String?,
        startDate: Long? = existing?.startDate,
        endDate: Long? = existing?.endDate,
        managerId: Long?,
        status: ProjectStatus = ProjectStatus.ACTIVE,
        onDone: () -> Unit = {}
    ) {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val now = System.currentTimeMillis()
            val creatorId = existing?.createdBy ?: uiState.value.currentUser?.id ?: uiState.value.users.firstOrNull()?.id ?: 1L
            val project = Project(
                id = existing?.id ?: 0L,
                name = cleanedName,
                description = description?.trim()?.ifBlank { null },
                startDate = startDate,
                endDate = endDate,
                status = status,
                managerId = managerId,
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
        if (uiState.value.tasks.any { it.projectId == id }) {
            transient.update {
                it.copy(refreshError = "Nao e possivel remover este projeto porque tem tarefas associadas.")
            }
            return
        }
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = projectRepository.deleteProjectRemote(id)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update {
                    it.copy(
                        isRefreshing = false,
                        refreshError = result.error.message.toProjectDeleteMessage()
                    )
                }
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
        deadline: Long? = existing?.deadline,
        onDone: () -> Unit = {}
    ) {
        val cleanedTitle = title.trim()
        val projectId = existing?.projectId ?: project?.id ?: return
        if (cleanedTitle.isBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val now = System.currentTimeMillis()
            val creatorId = existing?.createdBy ?: uiState.value.currentUser?.id ?: uiState.value.users.firstOrNull()?.id ?: 1L
            val task = Task(
                id = existing?.id ?: 0L,
                projectId = projectId,
                title = cleanedTitle,
                description = description?.trim()?.ifBlank { null },
                priority = priority,
                deadline = deadline,
                status = status,
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

    fun saveUser(
        existing: User?,
        name: String,
        username: String,
        email: String,
        role: UserRole,
        password: String?,
        photoUrl: String?,
        isActive: Boolean = existing?.isActive ?: true,
        onDone: () -> Unit = {}
    ) {
        val cleanedName = name.trim()
        val cleanedUsername = username.trim()
        val cleanedEmail = email.trim()
        if (cleanedName.isBlank() || cleanedUsername.isBlank() || cleanedEmail.isBlank()) return
        if (existing == null && password.isNullOrBlank()) return

        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            val now = System.currentTimeMillis()
            val user = User(
                id = existing?.id ?: 0L,
                name = cleanedName,
                username = cleanedUsername,
                email = cleanedEmail,
                passwordHash = existing?.passwordHash ?: "",
                photoUrl = photoUrl,
                role = role,
                roles = listOf(role),
                isActive = isActive,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )

            when (val result = userRepository.pushUser(user, password)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun deleteUser(user: User?, onDone: () -> Unit = {}) {
        val id = user?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = userRepository.deleteUserRemote(id)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun assignUserToProject(project: Project?, user: User?) {
        val projectId = project?.id?.takeIf { it != 0L } ?: return
        val userId = user?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = projectRepository.assignUserToProjectRemote(projectId, userId)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun removeUserFromProject(project: Project?, user: User?) {
        val projectId = project?.id?.takeIf { it != 0L } ?: return
        val userId = user?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = projectRepository.removeUserFromProjectRemote(projectId, userId)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun assignUserToTask(task: Task?, user: User?) {
        val taskId = task?.id?.takeIf { it != 0L } ?: return
        val userId = user?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = taskRepository.assignUserToTaskRemote(taskId, userId)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
                is ApiResult.Error -> transient.update { it.copy(isRefreshing = false, refreshError = result.error.message) }
            }
        }
    }

    fun removeUserFromTask(task: Task?, user: User?) {
        val taskId = task?.id?.takeIf { it != 0L } ?: return
        val userId = user?.id?.takeIf { it != 0L } ?: return
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, refreshError = null) }
            when (val result = taskRepository.removeUserFromTaskRemote(taskId, userId)) {
                is ApiResult.Success -> transient.update { it.copy(isRefreshing = false, refreshError = null) }
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
        val userId = uiState.value.currentUser?.id ?: uiState.value.users.firstOrNull()?.id ?: return
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
        val evaluatorId = uiState.value.currentUser?.id
            ?: uiState.value.users.firstOrNull { candidate -> candidate.id != evaluated.id }?.id
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

            evaluationRepository.upsertEvaluation(evaluation)

            when (val result = evaluationRepository.pushEvaluation(evaluation)) {
                is ApiResult.Success -> {
                    transient.update { it.copy(isRefreshing = false, refreshError = null) }
                    onDone()
                }
                is ApiResult.Error -> {
                    transient.update {
                        it.copy(
                            isRefreshing = false,
                            refreshError = "Guardado localmente. Sera sincronizado quando houver ligacao."
                        )
                    }
                    onDone()
                }
            }
        }
    }
}

private fun String?.toProjectDeleteMessage(): String =
    when {
        this == null -> "Nao foi possivel remover o projeto."
        contains("FOREIGN KEY", ignoreCase = true) ||
            contains("constraint", ignoreCase = true) ->
            "Nao e possivel remover este projeto porque tem tarefas associadas."
        else -> this
    }
