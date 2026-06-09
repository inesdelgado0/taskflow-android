package com.taskflow.app.ui.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.usecase.auth.LoginUseCase
import com.taskflow.app.domain.usecase.auth.LogoutUseCase
import com.taskflow.app.domain.usecase.auth.RegisterUseCase
import com.taskflow.app.domain.util.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(@StringRes val messageRes: Int) : AuthUiState()
}

data class AuthFormState(
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val nameError: Int? = null,
    @StringRes val usernameError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun login(email: String, password: String, role: UserRole) {
        val validation = validateLogin(email, password)
        _formState.value = validation

        if (validation.hasErrors()) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password, role)
                .onSuccess { user -> _uiState.value = AuthUiState.Success(user) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(R.string.error_invalid_credentials)
                }
        }
    }

    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val validation = validateRegister(name, username, email, password, confirmPassword)
        _formState.value = validation

        if (validation.hasErrors()) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(name, username, email, password)
                .onSuccess { user -> _uiState.value = AuthUiState.Success(user) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(R.string.error_unknown)
                }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
        _formState.update { AuthFormState() }
    }

    fun logout(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { logoutUseCase() }
            _uiState.value = AuthUiState.Idle
            onDone()
        }
    }

    private fun validateLogin(email: String, password: String): AuthFormState =
        AuthFormState(
            emailError = validateEmail(email),
            passwordError = validatePassword(password)
        )

    private fun validateRegister(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthFormState =
        AuthFormState(
            nameError = if (name.isBlank()) R.string.error_field_required else null,
            usernameError = if (username.isBlank()) R.string.error_field_required else null,
            emailError = validateEmail(email),
            passwordError = validatePassword(password),
            confirmPasswordError = when {
                confirmPassword.isBlank() -> R.string.error_field_required
                password != confirmPassword -> R.string.error_passwords_no_match
                else -> null
            }
        )

    private fun validateEmail(email: String): Int? =
        when {
            email.isBlank() -> R.string.error_field_required
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.error_invalid_email
            else -> null
        }

    private fun validatePassword(password: String): Int? =
        when {
            password.isBlank() -> R.string.error_field_required
            password.length < 8 -> R.string.error_password_too_short
            else -> null
        }

    private fun AuthFormState.hasErrors(): Boolean =
        listOf(emailError, passwordError, nameError, usernameError, confirmPasswordError)
            .any { it != null }
}

