package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(project: Project): Long
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(id: Long)
    suspend fun getProjectById(id: Long): Project?
    fun getAllProjectsFlow(): Flow<List<Project>>
    fun getProjectsByStatusFlow(status: ProjectStatus): Flow<List<Project>>
    fun getProjectsByManagerFlow(managerId: Long): Flow<List<Project>>
    fun searchProjectsFlow(query: String): Flow<List<Project>>
    suspend fun updateProjectStatus(id: Long, status: ProjectStatus)
    suspend fun assignManager(projectId: Long, managerId: Long?)
    suspend fun refreshProjects(): ApiResult<List<Project>>
    suspend fun pushProject(project: Project): ApiResult<Project>
    suspend fun assignManagerRemote(projectId: Long, managerId: Long?): ApiResult<Project>
    suspend fun refreshProjectUsers(projectId: Long): ApiResult<List<Long>>
    suspend fun assignUserToProjectRemote(projectId: Long, userId: Long): ApiResult<Unit>
    suspend fun removeUserFromProjectRemote(projectId: Long, userId: Long): ApiResult<Unit>
    suspend fun completeProjectRemote(id: Long): ApiResult<Project>
    suspend fun updateProjectStatusRemote(id: Long, status: ProjectStatus): ApiResult<Project>
    suspend fun deleteProjectRemote(id: Long): ApiResult<Unit>
}
