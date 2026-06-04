package com.taskflow.app.ui.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.remote.TokenManager
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.repository.UserRepository
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val photoUrl: String? = null,
    val newPassword: String = "",
    val nameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val userId = tokenManager.getUserId()
            val storedRole = tokenManager.getUserRole()?.toUserRoleOrNull() ?: UserRole.USER
            val user = userId?.let { userRepository.getUserById(it) }

            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = storedRole,
                        errorMessage = "Nao foi possivel carregar os dados do perfil."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    name = user.name,
                    username = user.username,
                    email = user.email,
                    role = storedRole.takeIf { role -> user.roles.contains(role) } ?: user.role,
                    photoUrl = user.photoUrl,
                    newPassword = "",
                    nameError = null,
                    usernameError = null,
                    emailError = null,
                    passwordError = null,
                    successMessage = null,
                    errorMessage = null
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, successMessage = null) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, successMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, successMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null, successMessage = null) }
    }

    fun onPhotoSelected(value: String?) {
        _uiState.update { it.copy(photoUrl = value, successMessage = null) }
    }

    fun saveProfile() {
        val current = validate(_uiState.value)
        _uiState.value = current

        if (current.hasErrors()) return

        val user = current.user ?: run {
            _uiState.update { it.copy(errorMessage = "Nao foi possivel guardar o perfil.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val updatedAt = System.currentTimeMillis()
            val updatedUser = user.copy(
                name = current.name.trim(),
                username = current.username.trim(),
                email = current.email.trim(),
                passwordHash = current.newPassword.takeIf { it.isNotBlank() } ?: user.passwordHash,
                photoUrl = current.photoUrl,
                role = current.role,
                updatedAt = updatedAt
            )

            when (val result = userRepository.updateProfileRemote(updatedUser, current.newPassword)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            name = result.data.name,
                            username = result.data.username,
                            email = result.data.email,
                            photoUrl = result.data.photoUrl,
                            role = result.data.role,
                            newPassword = "",
                            successMessage = "Perfil guardado com sucesso."
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.message ?: "Nao foi possivel guardar o perfil."
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: ProfileUiState): ProfileUiState =
        state.copy(
            nameError = if (state.name.isBlank()) "Campo obrigatorio." else null,
            usernameError = if (state.username.isBlank()) "Campo obrigatorio." else null,
            emailError = when {
                state.email.isBlank() -> "Campo obrigatorio."
                !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Email invalido."
                else -> null
            },
            passwordError = when {
                state.newPassword.isBlank() -> null
                state.newPassword.length < 8 -> "Minimo 8 caracteres."
                else -> null
            },
            successMessage = null,
            errorMessage = null
        )

    private fun ProfileUiState.hasErrors(): Boolean =
        listOf(nameError, usernameError, emailError, passwordError).any { it != null }

    private fun String.toUserRoleOrNull(): UserRole? =
        runCatching { UserRole.valueOf(this) }.getOrNull()
}
