package com.taskflow.app.domain.usecase.user.tasks

import com.taskflow.app.util.ApiResult
import javax.inject.Inject

class UpdateTaskProgressUseCase @Inject constructor() {
    suspend operator fun invoke(
        userId: Long,
        taskId: Long,
        dateText: String,
        location: String,
        percentage: Int,
        timeSpentMinutes: Int
    ): ApiResult<Unit> {
        return ApiResult.Success(Unit)
    }
}