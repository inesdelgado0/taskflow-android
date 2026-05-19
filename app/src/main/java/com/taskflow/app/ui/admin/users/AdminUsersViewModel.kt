package com.taskflow.app.ui.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.usecase.admin.users.CreateUserUseCase
import com.taskflow.app.domain.usecase.admin.users.DeleteUserUseCase
import com.taskflow.app.domain.usecase.admin.users.GetUsersUseCase
import com.taskflow.app.domain.usecase.admin.users.UpdateUserUseCase
import com.taskflow.app.domain.util.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminUserFormState(
    val id: Long = 0,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val existingPasswordHash: String = "",
    val createdAt: Long = 0,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val nameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

data class AdminUsersUiState(
    val users: List<User> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val editingUserId: Long? = null
)

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    getUsersUseCase: GetUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val transientState = MutableStateFlow(AdminUsersUiState())

    val uiState: StateFlow<AdminUsersUiState> =
        combine(getUsersUseCase(), query, transientState) { users, search, transient ->
            val filteredUsers = if (search.isBlank()) {
                users
            } else {
                users.filter { user ->
                    user.name.contains(search, ignoreCase = true) ||
                        user.username.contains(search, ignoreCase = true) ||
                        user.email.contains(search, ignoreCase = true)
                }
            }

            transient.copy(users = filteredUsers, query = search)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminUsersUiState(isLoading = true)
        )

    private val _formState = MutableStateFlow(AdminUserFormState())
    val formState: StateFlow<AdminUserFormState> = _formState.asStateFlow()

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onNameChange(value: String) {
        _formState.value = _formState.value.copy(name = value, nameError = null)
    }

    fun onUsernameChange(value: String) {
        _formState.value = _formState.value.copy(username = value, usernameError = null)
    }

    fun onEmailChange(value: String) {
        _formState.value = _formState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) {
        _formState.value = _formState.value.copy(password = value, passwordError = null)
    }

    fun onRoleChange(value: UserRole) {
        _formState.value = _formState.value.copy(role = value)
    }

    fun onActiveChange(value: Boolean) {
        _formState.value = _formState.value.copy(isActive = value)
    }

    fun startEdit(user: User) {
        _formState.value = AdminUserFormState(
            id = user.id,
            name = user.name,
            username = user.username,
            email = user.email,
            existingPasswordHash = user.passwordHash,
            createdAt = user.createdAt,
            role = user.role,
            isActive = user.isActive
        )
        transientState.value = transientState.value.copy(editingUserId = user.id)
    }

    fun clearForm() {
        _formState.value = AdminUserFormState()
        transientState.value = transientState.value.copy(
            editingUserId = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun saveUser() {
        val validation = validateForm(_formState.value)
        _formState.value = validation
        if (validation.hasErrors()) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(isLoading = true, errorMessage = null)
            val now = System.currentTimeMillis()
            val result = if (validation.id == 0L) {
                createUserUseCase(
                    User(
                        name = validation.name.trim(),
                        username = validation.username.trim(),
                        email = validation.email.trim(),
                        passwordHash = validation.password,
                        role = validation.role,
                        isActive = validation.isActive,
                        createdAt = now,
                        updatedAt = now
                    )
                ).map { Unit }
            } else {
                updateUserUseCase(
                    User(
                        id = validation.id,
                        name = validation.name.trim(),
                        username = validation.username.trim(),
                        email = validation.email.trim(),
                        passwordHash = validation.password
                            .takeIf { it.isNotBlank() }
                            ?: validation.existingPasswordHash,
                        role = validation.role,
                        isActive = validation.isActive,
                        createdAt = validation.createdAt,
                        updatedAt = now
                    )
                )
            }

            result
                .onSuccess {
                    _formState.value = AdminUserFormState()
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        editingUserId = null,
                        successMessage = "User saved.",
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to save user."
                    )
                }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            transientState.value = transientState.value.copy(isLoading = true, errorMessage = null)
            deleteUserUseCase(userId)
                .onSuccess {
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        successMessage = "User deleted."
                    )
                }
                .onFailure { error ->
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to delete user."
                    )
                }
        }
    }

    private fun validateForm(form: AdminUserFormState): AdminUserFormState =
        form.copy(
            nameError = if (form.name.isBlank()) "Required field." else null,
            usernameError = if (form.username.isBlank()) "Required field." else null,
            emailError = when {
                form.email.isBlank() -> "Required field."
                !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches() -> "Invalid email."
                else -> null
            },
            passwordError = when {
                form.id == 0L && form.password.length < 8 -> "Minimum 8 characters."
                form.id != 0L && form.password.isNotBlank() && form.password.length < 8 -> {
                    "Minimum 8 characters."
                }
                else -> null
            }
        )

    private fun AdminUserFormState.hasErrors(): Boolean =
        listOf(nameError, usernameError, emailError, passwordError).any { it != null }
}

