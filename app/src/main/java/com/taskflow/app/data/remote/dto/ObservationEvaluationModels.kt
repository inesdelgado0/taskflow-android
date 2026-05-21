package com.taskflow.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ObservationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("task_id") val taskId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("text") val text: String? = null,
    @SerializedName("photo_path") val photoPath: String? = null,
    @SerializedName("created_at") val createdAt: Long
)

data class ObservationRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("text") val text: String? = null,
    @SerializedName("photo_path") val photoPath: String? = null
)

data class EvaluationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("evaluator_id") val evaluatorId: Long,
    @SerializedName("evaluated_user_id") val evaluatedUserId: Long,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("created_at") val createdAt: Long
)

data class EvaluationRequest(
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("evaluator_id") val evaluatorId: Long,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String? = null
)
