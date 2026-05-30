package com.taskflow.app.data.repository

import com.taskflow.app.data.local.dao.ProjectDao
import com.taskflow.app.data.local.entity.ProjectEntity
import com.taskflow.app.data.remote.api.ProjectApi
import com.taskflow.app.data.remote.dto.ProjectDto
import com.taskflow.app.data.remote.dto.ProjectRequest
import com.taskflow.app.data.remote.dto.AssignManagerRequest
import com.taskflow.app.data.remote.dto.ProjectStatusRequest
import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.util.ApiResult
import com.taskflow.app.util.map
import com.taskflow.app.util.onSuccess
import com.taskflow.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val projectApi: ProjectApi
) : ProjectRepository {

    override suspend fun createProject(project: Project): Long =
        projectDao.insert(project.toEntity())

    override suspend fun updateProject(project: Project) =
        projectDao.update(project.toEntity())

    override suspend fun deleteProject(id: Long) {
        val entity = projectDao.getById(id) ?: return
        projectDao.delete(entity)
    }

    override suspend fun getProjectById(id: Long): Project? =
        projectDao.getById(id)?.toDomain()

    override fun getAllProjectsFlow(): Flow<List<Project>> =
        projectDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override fun getProjectsByStatusFlow(status: ProjectStatus): Flow<List<Project>> =
        projectDao.getByStatusFlow(status).map { list -> list.map { it.toDomain() } }

    override fun getProjectsByManagerFlow(managerId: Long): Flow<List<Project>> =
        projectDao.getByManagerFlow(managerId).map { list -> list.map { it.toDomain() } }

    override fun searchProjectsFlow(query: String): Flow<List<Project>> =
        projectDao.searchFlow(query).map { list -> list.map { it.toDomain() } }

    override suspend fun updateProjectStatus(id: Long, status: ProjectStatus) {
        projectDao.updateStatus(id, status, System.currentTimeMillis())
    }

    override suspend fun assignManager(projectId: Long, managerId: Long?) {
        projectDao.assignManager(projectId, managerId, System.currentTimeMillis())
    }

    override suspend fun refreshProjects(): ApiResult<List<Project>> =
        safeApiCall { projectApi.getProjects() }
            .map { projects -> projects.map { it.toDomain() } }
            .onSuccess { projects -> projectDao.upsertAll(projects.map { it.toEntity() }) }

    override suspend fun pushProject(project: Project): ApiResult<Project> {
        val result = if (project.id == 0L) {
            safeApiCall { projectApi.createProject(project.toRequest()) }
        } else {
            safeApiCall { projectApi.updateProject(project.id, project.toRequest()) }
        }

        return result
            .map { it.toDomain() }
            .onSuccess { synced -> projectDao.upsert(synced.toEntity()) }
    }

    override suspend fun assignManagerRemote(projectId: Long, managerId: Long?): ApiResult<Project> =
        safeApiCall { projectApi.assignManager(projectId, AssignManagerRequest(managerId)) }
            .map { it.toDomain() }
            .onSuccess { synced -> projectDao.upsert(synced.toEntity()) }

    override suspend fun completeProjectRemote(id: Long): ApiResult<Project> =
        safeApiCall { projectApi.completeProject(id) }
            .map { it.toDomain() }
            .onSuccess { synced -> projectDao.upsert(synced.toEntity()) }

    override suspend fun updateProjectStatusRemote(id: Long, status: ProjectStatus): ApiResult<Project> =
        safeApiCall { projectApi.updateStatus(id, ProjectStatusRequest(status)) }
            .map { it.toDomain() }
            .onSuccess { synced -> projectDao.upsert(synced.toEntity()) }

    override suspend fun deleteProjectRemote(id: Long): ApiResult<Unit> =
        safeApiCall { projectApi.deleteProject(id) }
            .onSuccess { deleteProject(id) }

    private fun Project.toEntity() = ProjectEntity(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        status = status,
        managerId = managerId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        status = status,
        managerId = managerId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ProjectDto.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        status = status,
        managerId = managerId,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Project.toRequest() = ProjectRequest(
        name = name,
        description = description,
        startDate = startDate,
        endDate = endDate,
        status = status,
        managerId = managerId,
        createdBy = createdBy
    )
}
