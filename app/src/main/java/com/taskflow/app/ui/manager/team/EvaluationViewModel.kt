package com.taskflow.app.ui.manager.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EvaluationFormState(
    val rating: Int = 0,
    val comment: String = "",
    val selectedUser: User? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EvaluationFormState())
    val state: StateFlow<EvaluationFormState> = _state.asStateFlow()

    fun onRatingChange(rating: Int) {
        _state.update { it.copy(rating = rating, errorMessage = null) }
    }

    fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    fun onUserSelected(user: User?) {
        _state.update { it.copy(selectedUser = user) }
    }

    fun submitEvaluation(projectId: Long, evaluatorId: Long) {
        val currentUser = _state.value.selectedUser ?: return
        viewModelScope.launch {
            val evaluation = com.taskflow.app.domain.model.Evaluation(
                projectId = projectId,
                evaluatorId = evaluatorId,
                evaluatedUserId = currentUser.id,
                rating = _state.value.rating,
                comment = _state.value.comment.ifBlank { null },
                createdAt = System.currentTimeMillis()
            )
            evaluationRepository.upsertEvaluation(evaluation)
        }
    }

    fun resetForm() {
        _state.update {
            it.copy(
                selectedUser = null,
                rating = 0,
                comment = "",
                errorMessage = null
            )
        }
    }
}