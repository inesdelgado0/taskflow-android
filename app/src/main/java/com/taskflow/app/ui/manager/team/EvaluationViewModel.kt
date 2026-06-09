package com.taskflow.app.ui.manager.team

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EvaluationUiState(
    val users: List<User> = emptyList(),
    val existingEvaluations: List<Evaluation> = emptyList(),
    val selectedUser: User? = null,
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val errorMessageRes: Int? = null
)

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EvaluationUiState())
    val state: StateFlow<EvaluationUiState> = _state.asStateFlow()

    fun loadProject(projectId: Long, evaluatorId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            evaluationRepository.getEvaluationsByProjectFlow(projectId)
                .collect { evaluations ->
                    _state.update {
                        it.copy(
                            existingEvaluations = evaluations,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectUser(user: User, projectId: Long) {
        viewModelScope.launch {
            val existing = evaluationRepository.getEvaluationForUserInProject(projectId, user.id)
            _state.update {
                it.copy(
                    selectedUser = user,
                    rating = existing?.rating ?: 0,
                    comment = existing?.comment.orEmpty(),
                    isSaved = false,
                    errorMessageRes = null
                )
            }
        }
    }

    fun onRatingChange(rating: Int) {
        _state.update { it.copy(rating = rating, errorMessageRes = null) }
    }

    fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    fun submitEvaluation(projectId: Long, evaluatorId: Long) {
        val state = _state.value
        val user = state.selectedUser ?: return

        if (state.rating !in 1..5) {
            _state.update { it.copy(errorMessageRes = R.string.error_rating_required) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessageRes = null) }

            val evaluation = Evaluation(
                projectId = projectId,
                evaluatorId = evaluatorId,
                evaluatedUserId = user.id,
                rating = state.rating,
                comment = state.comment.trim().ifBlank { null },
                createdAt = System.currentTimeMillis()
            )

            evaluationRepository.upsertEvaluation(evaluation)

            when (evaluationRepository.pushEvaluation(evaluation)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isLoading = false, isSaved = true)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true,
                        errorMessageRes = R.string.sync_saved_local
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessageRes = null) }
    }

    fun resetForm() {
        _state.update {
            it.copy(
                selectedUser = null,
                rating = 0,
                comment = "",
                isSaved = false,
                errorMessageRes = null
            )
        }
    }
}
