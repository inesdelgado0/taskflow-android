package com.taskflow.app.ui.user.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.usecase.sync.PopulateLocalDatabaseUseCase
import com.taskflow.app.domain.usecase.user.tasks.GetCompletedUserTasksUseCase
import com.taskflow.app.domain.usecase.user.tasks.GetPendingUserTasksUseCase
import com.taskflow.app.domain.util.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class UserTaskItemUi(
    val id: Long,
    val title: String,
    val projectName: String,
    val description: String,
    val priority: String,
    val deadlineText: String,
    val dateText: String,
    val location: String,
    val progress: Int,
    val timeSpentMinutes: Int,
    val memberCount: Int,
    val status: TaskStatus,
    val rating: Double? = null
)

data class UserTasksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String = "Utilizador",
    val pendingTasks: List<UserTaskItemUi> = emptyList(),
    val completedTasks: List<UserTaskItemUi> = emptyList()
) {
    val activeTasks: Int get() = pendingTasks.size
    val totalMinutes: Int get() = completedTasks.sumOf { it.timeSpentMinutes }
    val nearDeadlineCount: Int get() = pendingTasks.count { it.deadlineText.contains("dia") }
}

@HiltViewModel
class UserTasksViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val userTaskDao: UserTaskDao,
    private val populateLocalDatabase: PopulateLocalDatabaseUseCase,
    private val getPendingUserTasksUseCase: GetPendingUserTasksUseCase,
    private val getCompletedUserTasksUseCase: GetCompletedUserTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserTasksUiState())
    val uiState: StateFlow<UserTasksUiState> = _uiState.asStateFlow()

    init {
        loadUserTasks()
    }

    fun loadUserTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = tokenManager.getUserId()
            if (userId == null) {
                _uiState.value = UserTasksUiState(isLoading = false, error = "Sessao expirada.")
                return@launch
            }

            runCatching {
                populateLocalDatabase(userId)
                val userName = userRepository.getUserById(userId)?.name ?: "Utilizador"
                combine(
                    getPendingUserTasksUseCase(userId),
                    getCompletedUserTasksUseCase(userId)
                ) { pending, completed -> pending to completed }
                    .collectLatest { (pending, completed) ->
                        _uiState.value = UserTasksUiState(
                            isLoading = false,
                            userName = userName,
                            pendingTasks = pending.map { it.toUi(userId) },
                            completedTasks = completed.map { it.toUi(userId) }
                        )
                    }
            }.onFailure { error ->
                _uiState.value = UserTasksUiState(
                    isLoading = false,
                    error = error.message ?: "Erro ao carregar tarefas."
                )
            }
        }
    }

    private suspend fun Task.toUi(userId: Long): UserTaskItemUi {
        val assignment = userTaskDao.get(userId, id)
        val projectName = projectRepository.getProjectById(projectId)?.name ?: "Projeto"
        val progress = assignment?.completionPercentage ?: if (status == TaskStatus.COMPLETED) 100 else 0
        val memberCount = userTaskDao.countUsersByTask(id)

        return UserTaskItemUi(
            id = id,
            title = title,
            projectName = projectName,
            description = description.orEmpty(),
            priority = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            deadlineText = deadline.toDeadlineText(),
            dateText = (assignment?.workDate ?: deadline).toDateText(),
            location = assignment?.location.orEmpty(),
            progress = progress.coerceIn(0, 100),
            timeSpentMinutes = assignment?.timeSpentMinutes ?: 0,
            memberCount = memberCount,
            status = status
        )
    }

    private fun Long?.toDateText(): String {
        if (this == null) return "-"
        val date = Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
    }

    private fun Long?.toDeadlineText(): String {
        if (this == null) return "-"
        val deadlineDate = Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val days = ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate)
        return when {
            days < 0 -> "Atrasada"
            days == 0L -> "Hoje"
            days == 1L -> "1 dia"
            days < 7 -> "$days dias"
            days < 14 -> "1 semana"
            else -> "${days / 7} semanas"
        }
    }
}
