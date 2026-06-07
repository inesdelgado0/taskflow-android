package com.taskflow.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus

data class ProjectDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("start_date") val startDate: Long? = null,
    @SerializedName("end_date") val endDate: Long? = null,
    @SerializedName("status") val status: ProjectStatus = ProjectStatus.ACTIVE,
    @SerializedName("manager_id") val managerId: Long? = null,
    @SerializedName("created_by") val createdBy: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long
)

data class ProjectRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("start_date") val startDate: Long? = null,
    @SerializedName("end_date") val endDate: Long? = null,
    @SerializedName("status") val status: ProjectStatus = ProjectStatus.ACTIVE,
    @SerializedName("manager_id") val managerId: Long? = null,
    @SerializedName("created_by") val createdBy: Long
)

data class AssignManagerRequest(
    @SerializedName("manager_id") val managerId: Long?
)

data class ProjectStatusRequest(
    @SerializedName("status") val status: ProjectStatus
)

data class AssignUserRequest(
    @SerializedName("user_id") val userId: Long
)

data class TaskDto(
    @SerializedName("id") val id: Long,
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("priority") val priority: TaskPriority = TaskPriority.MEDIUM,
    @SerializedName("deadline") val deadline: Long? = null,
    @SerializedName("status") val status: TaskStatus = TaskStatus.PENDING,
    @SerializedName("created_by") val createdBy: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long
)

data class TaskRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("priority") val priority: TaskPriority = TaskPriority.MEDIUM,
    @SerializedName("deadline") val deadline: Long? = null,
    @SerializedName("status") val status: TaskStatus = TaskStatus.PENDING,
    @SerializedName("created_by") val createdBy: Long
)

data class TaskStatusRequest(
    @SerializedName("status") val status: TaskStatus
)

data class TaskProgressRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("work_date") val workDate: Long?,
    @SerializedName("location") val location: String? = null,
    @SerializedName("completion_percentage") val completionPercentage: Int,
    @SerializedName("time_spent_minutes") val timeSpentMinutes: Int
)
