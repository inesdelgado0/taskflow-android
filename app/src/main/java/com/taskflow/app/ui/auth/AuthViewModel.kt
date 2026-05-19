package com.taskflow.app.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.usecase.auth.LoginUseCase
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
    data class Error(val message: String) : AuthUiState()
}

data class AuthFormState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val usernameError: String? = null,
    val confirmPasswordError: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
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
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Invalid credentials.")
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
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Unable to register.")
                }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
        _formState.update { AuthFormState() }
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
            nameError = if (name.isBlank()) "This field is required." else null,
            usernameError = if (username.isBlank()) "This field is required." else null,
            emailError = validateEmail(email),
            passwordError = validatePassword(password),
            confirmPasswordError = when {
                confirmPassword.isBlank() -> "This field is required."
                password != confirmPassword -> "Passwords do not match."
                else -> null
            }
        )

    private fun validateEmail(email: String): String? =
        when {
            email.isBlank() -> "This field is required."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email address."
            else -> null
        }

    private fun validatePassword(password: String): String? =
        when {
            password.isBlank() -> "This field is required."
            password.length < 8 -> "Password must be at least 8 characters."
            else -> null
        }

    private fun AuthFormState.hasErrors(): Boolean =
        listOf(emailError, passwordError, nameError, usernameError, confirmPasswordError)
            .any { it != null }
}

