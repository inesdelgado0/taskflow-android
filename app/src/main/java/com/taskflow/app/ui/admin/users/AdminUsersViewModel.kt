package com.taskflow.app.ui.admin.users

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.R
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
    @StringRes val nameError: Int? = null,
    @StringRes val usernameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null
)

data class AdminUsersUiState(
    val users: List<User> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    @StringRes val successMessageRes: Int? = null,
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
            errorMessageRes = null,
            successMessageRes = null
        )
    }

    fun saveUser() {
        val validation = validateForm(_formState.value)
        _formState.value = validation
        if (validation.hasErrors()) return

        viewModelScope.launch {
            transientState.value = transientState.value.copy(isLoading = true, errorMessageRes = null)
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
                        successMessageRes = R.string.user_saved_success,
                        errorMessageRes = null
                    )
                }
                .onFailure {
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_user_save
                    )
                }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            transientState.value = transientState.value.copy(isLoading = true, errorMessageRes = null)
            deleteUserUseCase(userId)
                .onSuccess {
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        successMessageRes = R.string.user_deleted_success
                    )
                }
                .onFailure {
                    transientState.value = transientState.value.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_user_delete
                    )
                }
        }
    }

    private fun validateForm(form: AdminUserFormState): AdminUserFormState =
        form.copy(
            nameError = if (form.name.isBlank()) R.string.error_field_required else null,
            usernameError = if (form.username.isBlank()) R.string.error_field_required else null,
            emailError = when {
                form.email.isBlank() -> R.string.error_field_required
                !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches() -> R.string.error_invalid_email
                else -> null
            },
            passwordError = when {
                form.id == 0L && form.password.length < 8 -> R.string.error_password_too_short
                form.id != 0L && form.password.isNotBlank() && form.password.length < 8 -> {
                    R.string.error_password_too_short
                }
                else -> null
            }
        )

    private fun AdminUserFormState.hasErrors(): Boolean =
        listOf(nameError, usernameError, emailError, passwordError).any { it != null }
}

