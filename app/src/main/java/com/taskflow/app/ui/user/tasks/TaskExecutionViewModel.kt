package com.taskflow.app.ui.user.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.usecase.user.tasks.UpdateTaskProgressUseCase
import com.taskflow.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class TaskExecutionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val task: UserTaskItemUi? = null
)

data class TaskProgressFormState(
    val date: String = "",
    val location: String = "",
    val percentage: String = "",
    val timeSpent: String = "",
    val dateError: String? = null,
    val percentageError: String? = null,
    val timeSpentError: String? = null
)

@HiltViewModel
class TaskExecutionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenManager: TokenManager,
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val userTaskDao: UserTaskDao,
    private val updateTaskProgressUseCase: UpdateTaskProgressUseCase
) : ViewModel() {

    private val taskId: Long = checkNotNull(savedStateHandle[Routes.USER_TASK_ID_ARG])

    private val _uiState = MutableStateFlow(TaskExecutionUiState())
    val uiState: StateFlow<TaskExecutionUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TaskProgressFormState())
    val formState: StateFlow<TaskProgressFormState> = _formState.asStateFlow()

    init {
        loadTask()
    }

    fun onDateChanged(value: String) {
        _formState.value = _formState.value.copy(date = value, dateError = null)
    }

    fun onLocationChanged(value: String) {
        _formState.value = _formState.value.copy(location = value)
    }

    fun onPercentageChanged(value: String) {
        _formState.value = _formState.value.copy(percentage = value, percentageError = null)
    }

    fun onTimeSpentChanged(value: String) {
        _formState.value = _formState.value.copy(timeSpent = value, timeSpentError = null)
    }

    fun saveProgress() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(error = "Sessao expirada.")
                return@launch
            }

            val form = _formState.value
            val percentage = form.percentage.toIntOrNull()
            val timeSpent = parseTimeSpent(form.timeSpent)
            val hasErrors = percentage == null || percentage !in 0..100 ||
                timeSpent == null || timeSpent < 0 || !isValidDate(form.date)

            if (hasErrors) {
                _formState.value = form.copy(
                    dateError = if (!isValidDate(form.date)) "Data invalida. Use AAAA-MM-DD." else null,
                    percentageError = if (percentage == null || percentage !in 0..100) "Use um valor entre 0 e 100." else null,
                    timeSpentError = if (timeSpent == null || timeSpent < 0) "Indique tempo valido." else null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            updateTaskProgressUseCase(
                userId = userId,
                taskId = taskId,
                dateText = form.date,
                location = form.location,
                percentage = percentage,
                timeSpentMinutes = timeSpent
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
                loadTask()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error.message ?: "Erro ao guardar progresso."
                )
            }
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = tokenManager.getUserId()
            val task = taskRepository.getTaskById(taskId)

            if (userId == null || task == null) {
                _uiState.value = TaskExecutionUiState(isLoading = false, error = "Tarefa nao encontrada.")
                return@launch
            }

            val assignment = userTaskDao.get(userId, taskId)
            val uiTask = task.toUi(
                progress = assignment?.completionPercentage ?: 0,
                timeSpentMinutes = assignment?.timeSpentMinutes ?: 0
            )

            _uiState.value = TaskExecutionUiState(isLoading = false, task = uiTask)
            _formState.value = TaskProgressFormState(
                date = assignment?.workDate.toInputDate(),
                location = assignment?.location.orEmpty(),
                percentage = uiTask.progress.toString(),
                timeSpent = if (uiTask.timeSpentMinutes == 0) "" else uiTask.timeSpentMinutes.toString()
            )
        }
    }

    private suspend fun Task.toUi(progress: Int, timeSpentMinutes: Int): UserTaskItemUi {
        val projectName = projectRepository.getProjectById(projectId)?.name ?: "Projeto"
        return UserTaskItemUi(
            id = id,
            title = title,
            projectName = projectName,
            description = description.orEmpty(),
            priority = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            deadlineText = deadline.toDisplayDate(),
            dateText = deadline.toDisplayDate(),
            progress = progress.coerceIn(0, 100),
            timeSpentMinutes = timeSpentMinutes,
            status = status
        )
    }

    private fun parseTimeSpent(value: String): Int? {
        val trimmed = value.trim().lowercase()
        if (trimmed.isEmpty()) return null
        trimmed.toIntOrNull()?.let { return it }

        val hourMatch = Regex("""(\d+)\s*h""").find(trimmed)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minuteMatch = Regex("""(\d+)\s*m""").find(trimmed)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val total = hourMatch * 60 + minuteMatch
        return total.takeIf { it > 0 }
    }

    private fun isValidDate(value: String): Boolean =
        runCatching { java.time.LocalDate.parse(value) }.isSuccess

    private fun Long?.toInputDate(): String {
        val millis = this ?: System.currentTimeMillis()
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.toString()
    }

    private fun Long?.toDisplayDate(): String {
        if (this == null) return "-"
        val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
    }
}
