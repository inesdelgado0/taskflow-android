package com.taskflow.app.ui.profile

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
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
    @StringRes val nameError: Int? = null,
    @StringRes val usernameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val successMessageRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private var pendingPhotoBytes: ByteArray? = null
    private var pendingPhotoContentType: String? = null

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, successMessageRes = null) }

            val userId = tokenManager.getUserId()
            val storedRole = tokenManager.getUserRole()?.toUserRoleOrNull() ?: UserRole.USER
            val user = when (val result = userRepository.refreshCurrentUser()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> userId?.let { userRepository.getUserById(it) }
            }

            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = storedRole,
                        errorMessageRes = R.string.error_profile_load
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
                    successMessageRes = null,
                    errorMessageRes = null
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, successMessageRes = null) }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, usernameError = null, successMessageRes = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, successMessageRes = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null, successMessageRes = null) }
    }

    fun onPhotoSelected(value: String?, bytes: ByteArray? = null, contentType: String? = null) {
        pendingPhotoBytes = bytes
        pendingPhotoContentType = contentType
        _uiState.update { it.copy(photoUrl = value, successMessageRes = null) }
    }

    fun saveProfile() {
        val current = validate(_uiState.value)
        _uiState.value = current

        if (current.hasErrors()) return

        val user = current.user ?: run {
            _uiState.update { it.copy(errorMessageRes = R.string.error_profile_save) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, successMessageRes = null) }

            val updatedAt = System.currentTimeMillis()
            val updatedUser = user.copy(
                name = current.name.trim(),
                username = current.username.trim(),
                email = current.email.trim(),
                passwordHash = current.newPassword.takeIf { it.isNotBlank() } ?: user.passwordHash,
                photoUrl = if (pendingPhotoBytes == null) current.photoUrl else user.photoUrl,
                role = current.role,
                updatedAt = updatedAt
            )

            when (val result = userRepository.updateProfileRemote(updatedUser, current.newPassword)) {
                is ApiResult.Success -> {
                    val photoBytes = pendingPhotoBytes
                    val photoContentType = pendingPhotoContentType ?: "image/jpeg"
                    val finalUser = if (photoBytes != null) {
                        when (val upload = userRepository.uploadCurrentUserPhoto(photoBytes, photoContentType)) {
                            is ApiResult.Success -> {
                                pendingPhotoBytes = null
                                pendingPhotoContentType = null
                                upload.data
                            }
                            is ApiResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessageRes = R.string.error_profile_photo_upload
                                    )
                                }
                                return@launch
                            }
                        }
                    } else {
                        result.data
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = finalUser,
                            name = finalUser.name,
                            username = finalUser.username,
                            email = finalUser.email,
                            photoUrl = finalUser.photoUrl,
                            role = finalUser.role,
                            newPassword = "",
                            successMessageRes = R.string.profile_saved_success
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.error_profile_save
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: ProfileUiState): ProfileUiState =
        state.copy(
            nameError = if (state.name.isBlank()) R.string.error_field_required else null,
            usernameError = if (state.username.isBlank()) R.string.error_field_required else null,
            emailError = when {
                state.email.isBlank() -> R.string.error_field_required
                !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> R.string.error_invalid_email
                else -> null
            },
            passwordError = when {
                state.newPassword.isBlank() -> null
                state.newPassword.length < 8 -> R.string.error_password_too_short
                else -> null
            },
            successMessageRes = null,
            errorMessageRes = null
        )

    private fun ProfileUiState.hasErrors(): Boolean =
        listOf(nameError, usernameError, emailError, passwordError).any { it != null }

    private fun String.toUserRoleOrNull(): UserRole? =
        runCatching { UserRole.valueOf(this) }.getOrNull()
}
