package com.taskflow.app.ui.manager.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.EvaluationRepository
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// EvaluationViewModel.kt
// app/src/main/java/com/taskflow/app/ui/manager/evaluation/
// ============================================================

data class EvaluationUiState(
    // Lista de utilizadores do projeto a avaliar
    val users: List<User>                      = emptyList(),
    // Avaliações já existentes para este projeto
    val existingEvaluations: List<Evaluation>  = emptyList(),
    // Utilizador seleccionado para avaliar
    val selectedUser: User?                    = null,
    // Campos do formulário
    val rating: Int                            = 0,       // 0 = não selecionado
    val comment: String                        = "",
    // Estado
    val isLoading: Boolean                     = false,
    val isSaved: Boolean                       = false,
    val errorMessage: String?                  = null
)

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EvaluationUiState())
    val state: StateFlow<EvaluationUiState> = _state.asStateFlow()

    // ── Carregar dados do projeto ─────────────────────────────
    fun loadProject(projectId: Long, evaluatorId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Avaliações já existentes
            evaluationRepository.getEvaluationsByProjectFlow(projectId)
                .collect { evaluations ->
                    _state.update { it.copy(existingEvaluations = evaluations) }
                }
        }
    }

    // ── Selecionar utilizador para avaliar ────────────────────
    fun selectUser(user: User, projectId: Long) {
        viewModelScope.launch {
            // Verificar se já existe avaliação para este utilizador
            val existing = evaluationRepository.getEvaluationForUserInProject(projectId, user.id)
            _state.update { it.copy(
                selectedUser = user,
                rating       = existing?.rating ?: 0,
                comment      = existing?.comment ?: "",
                isSaved      = false,
                errorMessage = null
            )}
        }
    }

    // ── Formulário ────────────────────────────────────────────
    fun onRatingChange(rating: Int) {
        _state.update { it.copy(rating = rating, errorMessage = null) }
    }

    fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment) }
    }

    // ── Submeter avaliação ────────────────────────────────────
    fun submitEvaluation(projectId: Long, evaluatorId: Long) {
        val state = _state.value
        val user = state.selectedUser ?: return

        // Validação: rating obrigatório entre 1 e 5
        if (state.rating !in 1..5) {
            _state.update { it.copy(errorMessage = "Seleciona uma classificação entre 1 e 5.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val evaluation = Evaluation(
                projectId       = projectId,
                evaluatorId     = evaluatorId,
                evaluatedUserId = user.id,
                rating          = state.rating,
                comment         = state.comment.trim().ifBlank { null },
                createdAt       = System.currentTimeMillis()
            )

            // Guardar localmente primeiro (offline-first)
            evaluationRepository.upsertEvaluation(evaluation)

            // Tentar sincronizar com API
            when (val result = evaluationRepository.pushEvaluation(evaluation)) {
                is ApiResult.Success -> _state.update { it.copy(
                    isLoading = false,
                    isSaved   = true
                )}
                is ApiResult.Error -> _state.update { it.copy(
                    isLoading    = false,
                    isSaved      = true, // guardado localmente
                    errorMessage = "Guardado localmente. Será sincronizado quando houver ligação."
                )}
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun resetForm() {
        _state.update { it.copy(
            selectedUser = null,
            rating       = 0,
            comment      = "",
            isSaved      = false,
            errorMessage = null
        )}
    }
}