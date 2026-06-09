package com.taskflow.app.ui.admin.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.model.User
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.rememberWindowInfo

private val PrimaryBlue = Color(0xFF3B6FF0)
private val ScreenBlue = Color(0xFFEEF2FF)
private val BorderGray = Color(0xFFD1D5DB)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val windowInfo = rememberWindowInfo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBlue)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.users_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = stringResource(R.string.btn_back), color = PrimaryBlue)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (windowInfo.isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UserFormCard(
                    modifier = Modifier.weight(0.9f),
                    formState = formState,
                    uiState = uiState,
                    viewModel = viewModel
                )
                UsersListCard(
                    modifier = Modifier.weight(1.1f),
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    UserFormCard(
                        modifier = Modifier.fillMaxWidth(),
                        formState = formState,
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                item {
                    UsersListCard(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun UserFormCard(
    modifier: Modifier,
    formState: AdminUserFormState,
    uiState: AdminUsersUiState,
    viewModel: AdminUsersViewModel
) {
    var passwordVisible by rememberSaveable(formState.id) { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (uiState.editingUserId == null) {
                    stringResource(R.string.users_btn_new)
                } else {
                    stringResource(R.string.user_btn_edit)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            UserTextField(
                label = stringResource(R.string.register_label_name),
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                error = formState.nameError
            )
            UserTextField(
                label = stringResource(R.string.register_label_username),
                value = formState.username,
                onValueChange = viewModel::onUsernameChange,
                error = formState.usernameError
            )
            UserTextField(
                label = stringResource(R.string.register_label_email),
                value = formState.email,
                onValueChange = viewModel::onEmailChange,
                error = formState.emailError,
                keyboardType = KeyboardType.Email
            )
            UserTextField(
                label = stringResource(R.string.register_label_password),
                value = formState.password,
                onValueChange = viewModel::onPasswordChange,
                error = formState.passwordError,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = it }
            )
            Text(
                text = stringResource(R.string.user_label_role),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            RoleSelector(
                selectedRole = formState.role,
                onRoleChange = viewModel::onRoleChange
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.admin_user_active))
                Switch(
                    checked = formState.isActive,
                    onCheckedChange = viewModel::onActiveChange
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = viewModel::saveUser,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = stringResource(R.string.btn_save), color = Color.White)
                }
            }
            TextButton(
                onClick = viewModel::clearForm,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.btn_cancel), color = TextSecondary)
            }
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun UsersListCard(
    modifier: Modifier,
    uiState: AdminUsersUiState,
    viewModel: AdminUsersViewModel
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.users.isEmpty()) {
                Text(
                    text = stringResource(R.string.users_empty),
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.users, key = { it.id }) { user ->
                        UserRow(
                            user = user,
                            onEdit = { viewModel.startEdit(user) },
                            onDelete = { viewModel.deleteUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.name, fontWeight = FontWeight.Bold)
                    Text(text = user.email, color = TextSecondary)
                    Text(text = roleLabel(user.role), color = PrimaryBlue)
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEdit) {
                        Text(text = stringResource(R.string.btn_edit), color = PrimaryBlue)
                    }
                    TextButton(onClick = onDelete) {
                        Text(text = stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSelector(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RoleButton(
            text = stringResource(R.string.user_role_admin),
            selected = selectedRole == UserRole.ADMIN,
            onClick = { onRoleChange(UserRole.ADMIN) },
            modifier = Modifier.weight(1f)
        )
        RoleButton(
            text = stringResource(R.string.user_role_manager),
            selected = selectedRole == UserRole.MANAGER,
            onClick = { onRoleChange(UserRole.MANAGER) },
            modifier = Modifier.weight(1f)
        )
        RoleButton(
            text = stringResource(R.string.user_role_user),
            selected = selectedRole == UserRole.USER,
            onClick = { onRoleChange(UserRole.USER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) PrimaryBlue else Color.White,
            contentColor = if (selected) Color.White else TextSecondary
        ),
        border = BorderStroke(1.dp, if (selected) PrimaryBlue else BorderGray)
    ) {
        Text(text = text, maxLines = 1, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun UserTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {}
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = label
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderGray
            )
        )
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun roleLabel(role: UserRole): String =
    when (role) {
        UserRole.ADMIN -> stringResource(R.string.user_role_admin)
        UserRole.MANAGER -> stringResource(R.string.user_role_manager)
        UserRole.USER -> stringResource(R.string.user_role_user)
    }

