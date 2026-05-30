package com.taskflow.app.domain.usecase.statistics

import com.taskflow.app.domain.model.Project
import com.taskflow.app.domain.model.Task
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.ProjectRepository
import com.taskflow.app.domain.repository.TaskRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.ProjectStatus
import com.taskflow.app.domain.util.TaskPriority
import com.taskflow.app.domain.util.TaskStatus
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUseCasesTest {
    @Test
    fun buildProjectStatistics_countsCompletedPendingAndOverdueTasks() = runBlocking {
        val now = 10_000L
        val project = Project(
            id = 7L,
            name = "App Mobile",
            status = ProjectStatus.ACTIVE,
            createdBy = 1L,
            createdAt = 0L,
            updatedAt = 0L
        )
        val tasks = listOf(
            task(id = 1L, projectId = 7L, status = TaskStatus.COMPLETED, deadline = now - 1_000L),
            task(id = 2L, projectId = 7L, status = TaskStatus.IN_PROGRESS, deadline = now - 500L),
            task(id = 3L, projectId = 7L, status = TaskStatus.PENDING, deadline = now + 5_000L),
            task(id = 4L, projectId = 7L, status = TaskStatus.CANCELLED, deadline = now - 5_000L)
        )
        val useCase = BuildProjectStatisticsUseCase(
            projectRepository = FakeProjectRepository(project),
            taskRepository = FakeTaskRepository(tasks)
        )

        val snapshot = useCase(projectId = 7L, now = now)
        val row = snapshot.rows.single()

        assertEquals("Estatisticas do projeto: App Mobile", snapshot.title)
        assertEquals(4, row.totalTasks)
        assertEquals(1, row.completedTasks)
        assertEquals(2, row.pendingTasks)
        assertEquals(1, row.overdueTasks)
        assertEquals(25, row.completionRate)
    }

    @Test
    fun buildUserStatistics_usesAssignedPendingAndCompletedTasks() = runBlocking {
        val user = User(
            id = 3L,
            name = "Simao",
            username = "simao",
            email = "simao@example.com",
            passwordHash = "hash",
            role = UserRole.USER,
            createdAt = 0L,
            updatedAt = 0L
        )
        val taskRepository = FakeTaskRepository(
            tasks = emptyList(),
            pendingForUser = listOf(task(id = 1L, status = TaskStatus.PENDING)),
            completedForUser = listOf(task(id = 2L, status = TaskStatus.COMPLETED))
        )
        val useCase = BuildUserStatisticsUseCase(
            userRepository = FakeUserRepository(user),
            taskRepository = taskRepository
        )

        val snapshot = useCase(userId = 3L, now = 1_000L)

        assertEquals("Estatisticas do utilizador: Simao", snapshot.title)
        assertEquals(2, snapshot.totalTasks)
        assertEquals(1, snapshot.completedTasks)
        assertEquals(1, snapshot.pendingTasks)
        assertEquals(50, snapshot.completionRate)
    }

    private fun task(
        id: Long,
        projectId: Long = 1L,
        status: TaskStatus,
        deadline: Long? = null
    ) = Task(
        id = id,
        projectId = projectId,
        title = "Task $id",
        priority = TaskPriority.MEDIUM,
        deadline = deadline,
        status = status,
        createdBy = 1L,
        createdAt = 0L,
        updatedAt = 0L
    )
}

private class FakeProjectRepository(
    private val project: Project
) : ProjectRepository {
    override suspend fun createProject(project: Project): Long = project.id
    override suspend fun updateProject(project: Project) = Unit
    override suspend fun deleteProject(id: Long) = Unit
    override suspend fun getProjectById(id: Long): Project? = project.takeIf { it.id == id }
    override fun getAllProjectsFlow(): Flow<List<Project>> = flowOf(listOf(project))
    override fun getProjectsByStatusFlow(status: ProjectStatus): Flow<List<Project>> = flowOf(listOf(project))
    override fun getProjectsByManagerFlow(managerId: Long): Flow<List<Project>> = flowOf(listOf(project))
    override fun searchProjectsFlow(query: String): Flow<List<Project>> = flowOf(listOf(project))
    override suspend fun updateProjectStatus(id: Long, status: ProjectStatus) = Unit
    override suspend fun assignManager(projectId: Long, managerId: Long?) = Unit
    override suspend fun refreshProjects(): ApiResult<List<Project>> = ApiResult.Success(listOf(project))
    override suspend fun pushProject(project: Project): ApiResult<Project> = ApiResult.Success(project)
    override suspend fun assignManagerRemote(projectId: Long, managerId: Long?): ApiResult<Project> = ApiResult.Success(project)
    override suspend fun completeProjectRemote(id: Long): ApiResult<Project> = ApiResult.Success(project)
    override suspend fun updateProjectStatusRemote(id: Long, status: ProjectStatus): ApiResult<Project> = ApiResult.Success(project)
    override suspend fun deleteProjectRemote(id: Long): ApiResult<Unit> = ApiResult.Success(Unit)
}

private class FakeTaskRepository(
    private val tasks: List<Task>,
    private val pendingForUser: List<Task> = emptyList(),
    private val completedForUser: List<Task> = emptyList()
) : TaskRepository {
    override suspend fun createTask(task: Task): Long = task.id
    override suspend fun updateTask(task: Task) = Unit
    override suspend fun deleteTask(id: Long) = Unit
    override suspend fun getTaskById(id: Long): Task? = tasks.firstOrNull { it.id == id }
    override fun getTasksByProjectFlow(projectId: Long): Flow<List<Task>> =
        flowOf(tasks.filter { it.projectId == projectId })
    override fun getTasksByProjectAndStatusFlow(projectId: Long, status: TaskStatus): Flow<List<Task>> =
        flowOf(tasks.filter { it.projectId == projectId && it.status == status })
    override fun getPendingTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(pendingForUser)
    override fun getCompletedTasksForUserFlow(userId: Long): Flow<List<Task>> = flowOf(completedForUser)
    override fun searchTasksFlow(
        query: String,
        projectId: Long?,
        status: TaskStatus?,
        priority: TaskPriority?
    ): Flow<List<Task>> = flowOf(tasks)
    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) = Unit
    override suspend fun refreshTasks(projectId: Long): ApiResult<List<Task>> = ApiResult.Success(tasks)
    override suspend fun pushTask(task: Task): ApiResult<Task> = ApiResult.Success(task)
    override suspend fun updateTaskStatusRemote(id: Long, status: TaskStatus): ApiResult<Task> =
        ApiResult.Success(tasks.first { it.id == id }.copy(status = status))
    override suspend fun deleteTaskRemote(id: Long): ApiResult<Unit> = ApiResult.Success(Unit)
}

private class FakeUserRepository(
    private val user: User
) : UserRepository {
    override suspend fun createUser(user: User): Long = user.id
    override suspend fun updateUser(user: User) = Unit
    override suspend fun deleteUser(id: Long) = Unit
    override suspend fun getUserById(id: Long): User? = user.takeIf { it.id == id }
    override suspend fun getUserByEmail(email: String): User? = user.takeIf { it.email == email }
    override suspend fun getUserByUsername(username: String): User? = user.takeIf { it.username == username }
    override fun getAllUsersFlow(): Flow<List<User>> = flowOf(listOf(user))
    override fun getUsersByRoleFlow(role: UserRole): Flow<List<User>> = flowOf(listOf(user))
    override fun searchUsersFlow(query: String): Flow<List<User>> = flowOf(listOf(user))
    override suspend fun setUserActive(id: Long, isActive: Boolean) = Unit
    override suspend fun refreshUsers(): ApiResult<List<User>> = ApiResult.Success(listOf(user))
}
