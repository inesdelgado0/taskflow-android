package com.taskflow.app.ui.manager.team

import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvaluationViewModelTest {
    @Test
    fun onRatingChange_updatesRatingAndClearsError() {
        val viewModel = EvaluationViewModel(FakeEvaluationRepository(), FakeUserRepository())

        viewModel.onRatingChange(4)

        assertEquals(4, viewModel.state.value.rating)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun onCommentChange_updatesComment() {
        val viewModel = EvaluationViewModel(FakeEvaluationRepository(), FakeUserRepository())

        viewModel.onCommentChange("Bom desempenho")

        assertEquals("Bom desempenho", viewModel.state.value.comment)
    }

    @Test
    fun submitEvaluation_withoutSelectedUserDoesNothing() {
        val viewModel = EvaluationViewModel(FakeEvaluationRepository(), FakeUserRepository())

        viewModel.onRatingChange(5)
        viewModel.submitEvaluation(projectId = 3L, evaluatorId = 2L)

        assertEquals(5, viewModel.state.value.rating)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun resetForm_clearsSelectedUserAndFormState() {
        val viewModel = EvaluationViewModel(FakeEvaluationRepository(), FakeUserRepository())

        viewModel.onRatingChange(3)
        viewModel.onCommentChange("Precisa melhorar")
        viewModel.resetForm()

        assertNull(viewModel.state.value.selectedUser)
        assertEquals(0, viewModel.state.value.rating)
        assertEquals("", viewModel.state.value.comment)
        assertNull(viewModel.state.value.errorMessage)
    }
}

private class FakeEvaluationRepository : EvaluationRepository {
    override suspend fun upsertEvaluation(evaluation: Evaluation): Long = evaluation.id
    override suspend fun getEvaluationById(id: Long): Evaluation? = null
    override suspend fun getEvaluationForUserInProject(projectId: Long, userId: Long): Evaluation? = null
    override fun getEvaluationsByProjectFlow(projectId: Long): Flow<List<Evaluation>> = flowOf(emptyList())
    override fun getEvaluationsByUserFlow(userId: Long): Flow<List<Evaluation>> = flowOf(emptyList())
    override suspend fun getAverageRating(userId: Long): Float? = null
    override suspend fun refreshEvaluations(projectId: Long): ApiResult<List<Evaluation>> =
        ApiResult.Success(emptyList())
    override suspend fun pushEvaluation(evaluation: Evaluation): ApiResult<Evaluation> =
        ApiResult.Success(evaluation)
}

private class FakeUserRepository : UserRepository {
    override suspend fun createUser(user: User): Long = user.id
    override suspend fun updateUser(user: User) = Unit
    override suspend fun deleteUser(id: Long) = Unit
    override suspend fun getUserById(id: Long): User? = null
    override suspend fun getUserByEmail(email: String): User? = null
    override suspend fun getUserByUsername(username: String): User? = null
    override fun getAllUsersFlow(): Flow<List<User>> = flowOf(emptyList())
    override fun getUsersByRoleFlow(role: UserRole): Flow<List<User>> = flowOf(emptyList())
    override fun searchUsersFlow(query: String): Flow<List<User>> = flowOf(emptyList())
    override suspend fun setUserActive(id: Long, isActive: Boolean) = Unit
    override suspend fun refreshUsers(): ApiResult<List<User>> = ApiResult.Success(emptyList())
    override suspend fun updateProfileRemote(user: User, newPassword: String?): ApiResult<User> =
        ApiResult.Success(user)
}
