package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingUserTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: Long): Flow<List<Task>> =
        taskRepository.getPendingTasksForUserFlow(userId)
}
