package com.taskflow.app.ui.manager.tasks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.ProjectEntity
import com.taskflow.app.data.local.entity.TaskEntity
import com.taskflow.app.domain.usecase.manager.tasks.CreateTaskUseCase
import com.taskflow.app.domain.usecase.manager.tasks.DeleteTaskUseCase
import com.taskflow.app.domain.usecase.manager.tasks.GetManagerProjectsUseCase
import com.taskflow.app.domain.usecase.manager.tasks.GetProjectTasksUseCase
import com.taskflow.app.domain.usecase.manager.tasks.UpdateTaskUseCase
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManagerTaskFormState(
    val id: Long = 0,
    val projectId: Long = 0,
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val deadlineText: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val createdBy: Long = 0,
    val createdAt: Long = 0,
    @StringRes val titleError: Int? = null,
    @StringRes val projectError: Int? = null,
    @StringRes val deadlineError: Int? = null
)

data class ManagerTasksUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProjectId: Long = 0,
    val tasks: List<TaskEntity> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val editingTaskId: Long? = null,
    @StringRes val errorMessageRes: Int? = null,
    @StringRes val successMessageRes: Int? = null
)

@HiltViewModel
class ManagerTasksViewModel @Inject constructor(
    private val getManagerProjectsUseCase: GetManagerProjectsUseCase,
    private val getProjectTasksUseCase: GetProjectTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerTasksUiState())
    val uiState: StateFlow<ManagerTasksUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ManagerTaskFormState())
    val formState: StateFlow<ManagerTaskFormState> = _formState.asStateFlow()

    private var tasksJob: Job? = null

    fun loadProjects(managerId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getManagerProjectsUseCase(managerId).collect { projects ->
                val selectedProjectId = _uiState.value.selectedProjectId
                    .takeIf { id -> projects.any { it.id == id } }
                    ?: projects.firstOrNull()?.id
                    ?: 0L

                _uiState.update {
                    it.copy(
                        projects = projects,
                        selectedProjectId = selectedProjectId,
                        isLoading = false
                    )
                }
                _formState.update { it.copy(projectId = selectedProjectId) }
                observeTasks(selectedProjectId)
            }
        }
    }

    fun onProjectSelected(projectId: Long) {
        _uiState.update { it.copy(selectedProjectId = projectId) }
        _formState.update { it.copy(projectId = projectId, projectError = null) }
        observeTasks(projectId)
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun onTitleChange(value: String) {
        _formState.update { it.copy(title = value, titleError = null) }
    }

    fun onDescriptionChange(value: String) {
        _formState.update { it.copy(description = value) }
    }

    fun onPriorityChange(value: TaskPriority) {
        _formState.update { it.copy(priority = value) }
    }

    fun onDeadlineChange(value: String) {
        _formState.update { it.copy(deadlineText = value, deadlineError = null) }
    }

    fun onStatusChange(value: TaskStatus) {
        _formState.update { it.copy(status = value) }
    }

    fun startEdit(task: TaskEntity) {
        _formState.value = ManagerTaskFormState(
            id = task.id,
            projectId = task.projectId,
            title = task.title,
            description = task.description.orEmpty(),
            priority = task.priority,
            deadlineText = task.deadline?.let(::formatDeadline).orEmpty(),
            status = task.status,
            createdBy = task.createdBy,
            createdAt = task.createdAt
        )
        _uiState.update {
            it.copy(editingTaskId = task.id, selectedProjectId = task.projectId)
        }
    }

    fun clearForm() {
        _formState.value = ManagerTaskFormState(projectId = _uiState.value.selectedProjectId)
        _uiState.update { it.copy(editingTaskId = null, errorMessageRes = null, successMessageRes = null) }
    }

    fun saveTask(managerId: Long) {
        val validation = validateForm(_formState.value)
        _formState.value = validation
        if (validation.hasErrors()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val deadline = parseDeadline(validation.deadlineText)
            val task = TaskEntity(
                id = validation.id,
                projectId = validation.projectId,
                title = validation.title.trim(),
                description = validation.description.trim().ifBlank { null },
                priority = validation.priority,
                deadline = deadline,
                status = validation.status,
                createdBy = if (validation.createdBy == 0L) managerId else validation.createdBy,
                createdAt = if (validation.createdAt == 0L) now else validation.createdAt,
                updatedAt = now
            )

            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            val result = if (task.id == 0L) {
                createTaskUseCase(task).map { Unit }
            } else {
                updateTaskUseCase(task)
            }

            result
                .onSuccess {
                    clearForm()
                    _uiState.update {
                        it.copy(isLoading = false, successMessageRes = R.string.task_saved_success)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.error_task_save
                        )
                    }
                }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            deleteTaskUseCase(task)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessageRes = R.string.task_deleted_success)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.error_task_delete
                        )
                    }
                }
        }
    }

    private fun observeTasks(projectId: Long) {
        tasksJob?.cancel()
        if (projectId == 0L) {
            _uiState.update { it.copy(tasks = emptyList()) }
            return
        }

        tasksJob = viewModelScope.launch {
            getProjectTasksUseCase(projectId).collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }

    private fun validateForm(form: ManagerTaskFormState): ManagerTaskFormState {
        val deadlineError = if (form.deadlineText.isBlank()) {
            R.string.error_deadline_required
        } else {
            try {
                parseDeadline(form.deadlineText)
                null
            } catch (_: DateTimeParseException) {
                R.string.error_invalid_date
            }
        }

        return form.copy(
            titleError = if (form.title.isBlank()) R.string.error_field_required else null,
            projectError = if (form.projectId == 0L) R.string.error_select_project else null,
            deadlineError = deadlineError
        )
    }

    private fun parseDeadline(value: String): Long? =
        value.trim()
            .ifBlank { null }
            ?.let {
                LocalDate.parse(it)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }

    private fun formatDeadline(value: Long): String =
        java.time.Instant.ofEpochMilli(value)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

    private fun ManagerTaskFormState.hasErrors(): Boolean =
        listOf(titleError, projectError, deadlineError).any { it != null }
}

