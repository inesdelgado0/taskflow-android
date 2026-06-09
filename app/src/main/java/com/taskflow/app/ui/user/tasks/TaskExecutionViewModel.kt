package com.taskflow.app.ui.user.tasks

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
import com.taskflow.app.data.local.dao.UserTaskDao
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.domain.model.Observation
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.ObservationRepository
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.usecase.user.tasks.UpdateTaskProgressUseCase
import com.taskflow.app.ui.navigation.Routes
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class TaskExecutionUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSavingObservation: Boolean = false,
    val saved: Boolean = false,
    val observationSaved: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val observationErrorRes: Int? = null,
    val task: UserTaskItemUi? = null,
    val observations: List<Observation> = emptyList()
)

data class TaskProgressFormState(
    val date: String = "",
    val location: String = "",
    val percentage: String = "",
    val timeSpent: String = "",
    @StringRes val dateError: Int? = null,
    @StringRes val percentageError: Int? = null,
    @StringRes val timeSpentError: Int? = null
)

@HiltViewModel
class TaskExecutionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenManager: TokenManager,
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val observationRepository: ObservationRepository,
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
        observeObservations()
        refreshObservations()
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
                _uiState.value = _uiState.value.copy(errorRes = R.string.error_session_expired)
                return@launch
            }

            val form = _formState.value
            val percentage = form.percentage.toIntOrNull()
            val timeSpent = parseTimeSpent(form.timeSpent)
            val hasErrors = percentage == null || percentage !in 0..100 ||
                timeSpent == null || timeSpent < 0 || !isValidDate(form.date)

            if (hasErrors) {
                _formState.value = form.copy(
                    dateError = if (!isValidDate(form.date)) R.string.error_invalid_date else null,
                    percentageError = if (percentage == null || percentage !in 0..100) R.string.error_invalid_percentage else null,
                    timeSpentError = if (timeSpent == null || timeSpent < 0) R.string.error_invalid_time_spent else null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSaving = true, errorRes = null)
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
                    errorRes = R.string.error_save_progress
                )
            }
        }
    }

    fun saveObservation(text: String, photoBytes: ByteArray?, photoContentType: String?) {
        viewModelScope.launch {
            val cleanedText = text.trim().ifBlank { null }
            if (cleanedText == null && photoBytes == null) {
                _uiState.update {
                    it.copy(observationErrorRes = R.string.error_observation_empty)
                }
                return@launch
            }

            val userId = tokenManager.getUserId()
            if (userId == null) {
                _uiState.update { it.copy(observationErrorRes = R.string.error_session_expired_short) }
                return@launch
            }

            _uiState.update {
                it.copy(isSavingObservation = true, observationErrorRes = null, observationSaved = false)
            }
            val observation = Observation(
                taskId = taskId,
                userId = userId,
                text = cleanedText,
                photoPath = if (cleanedText == null && photoBytes != null) "__pending_upload__" else null,
                createdAt = System.currentTimeMillis()
            )

            when (val result = observationRepository.pushObservation(observation)) {
                is ApiResult.Success -> {
                    if (photoBytes != null) {
                        when (val upload = observationRepository.uploadObservationPhoto(
                            id = result.data.id,
                            bytes = photoBytes,
                            contentType = photoContentType ?: "image/jpeg"
                        )) {
                            is ApiResult.Success -> _uiState.update {
                                it.copy(
                                    isSavingObservation = false,
                                    observationSaved = true,
                                    observationErrorRes = null
                                )
                            }
                            is ApiResult.Error -> _uiState.update {
                                it.copy(
                                    isSavingObservation = false,
                                    observationErrorRes = R.string.error_observation_photo_upload
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSavingObservation = false,
                                observationSaved = true,
                                observationErrorRes = null
                            )
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isSavingObservation = false,
                        observationErrorRes = R.string.error_observation_save
                    )
                }
            }
        }
    }

    fun consumeObservationSaved() {
        _uiState.update { it.copy(observationSaved = false) }
    }

    private fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorRes = null)
            val userId = tokenManager.getUserId()
            val task = taskRepository.getTaskById(taskId)

            if (userId == null || task == null) {
                _uiState.value = TaskExecutionUiState(isLoading = false, errorRes = R.string.error_task_not_found)
                return@launch
            }

            val assignment = userTaskDao.get(userId, taskId)
            val uiTask = task.toUi(
                progress = assignment?.completionPercentage ?: 0,
                timeSpentMinutes = assignment?.timeSpentMinutes ?: 0,
                workDate = assignment?.workDate,
                location = assignment?.location,
                memberCount = userTaskDao.countUsersByTask(taskId)
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

    private fun observeObservations() {
        viewModelScope.launch {
            observationRepository.getObservationsByTaskFlow(taskId).collect { observations ->
                _uiState.update { it.copy(observations = observations) }
            }
        }
    }

    private fun refreshObservations() {
        viewModelScope.launch {
            observationRepository.refreshObservations(taskId)
        }
    }

    private suspend fun Task.toUi(
        progress: Int,
        timeSpentMinutes: Int,
        workDate: Long?,
        location: String?,
        memberCount: Int
    ): UserTaskItemUi {
        val projectName = projectRepository.getProjectById(projectId)?.name ?: "Projeto"
        return UserTaskItemUi(
            id = id,
            title = title,
            projectName = projectName,
            description = description.orEmpty(),
            priority = priority.name.lowercase().replaceFirstChar { it.uppercase() },
            deadlineText = deadline.toDisplayDate(),
            dateText = workDate.toDisplayDate(),
            location = location.orEmpty(),
            progress = progress.coerceIn(0, 100),
            timeSpentMinutes = timeSpentMinutes,
            memberCount = memberCount,
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
