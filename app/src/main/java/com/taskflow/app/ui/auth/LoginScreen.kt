package com.taskflow.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.rememberWindowInfo
import com.taskflow.app.ui.navigation.Routes

private val PrimaryBlue = Color(0xFF3B6FF0)
private val ScreenBlue = Color(0xFFEEF2FF)
private val BorderGray = Color(0xFFD1D5DB)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val windowInfo = rememberWindowInfo()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedRole by rememberSaveable { mutableStateOf(UserRole.ADMIN) }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AuthUiState.Success) {
            val route = when (state.user.role) {
                UserRole.ADMIN -> Routes.ADMIN_DASHBOARD
                UserRole.MANAGER -> Routes.managerDashboard(state.user.id)
                UserRole.USER -> Routes.USER_DASHBOARD
            }
            viewModel.clearState()
            navController.navigate(route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBlue)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val cardModifier = if (windowInfo.isLandscape) {
            Modifier
                .fillMaxWidth(0.72f)
                .widthIn(max = 720.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        }

        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            val contentModifier = Modifier.padding(24.dp)

            if (windowInfo.isLandscape) {
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginHeader(modifier = Modifier.weight(0.8f))
                    LoginForm(
                        modifier = Modifier.weight(1.2f),
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        selectedRole = selectedRole,
                        formState = formState,
                        uiState = uiState,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityChange = { passwordVisible = it },
                        onRoleChange = { selectedRole = it },
                        onLogin = { viewModel.login(email, password, selectedRole) },
                        onRegisterClick = { navController.navigate(Routes.REGISTER) }
                    )
                }
            } else {
                Column(modifier = contentModifier) {
                    LoginHeader(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(20.dp))
                    LoginForm(
                        modifier = Modifier.fillMaxWidth(),
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        selectedRole = selectedRole,
                        formState = formState,
                        uiState = uiState,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onPasswordVisibilityChange = { passwordVisible = it },
                        onRoleChange = { selectedRole = it },
                        onLogin = { viewModel.login(email, password, selectedRole) },
                        onRegisterClick = { navController.navigate(Routes.REGISTER) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginForm(
    modifier: Modifier,
    email: String,
    password: String,
    passwordVisible: Boolean,
    selectedRole: UserRole,
    formState: AuthFormState,
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onLogin: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(modifier = modifier) {
        TaskFlowTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.login_label_email),
            error = formState.emailError,
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(10.dp))
        TaskFlowTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.login_label_password),
            error = formState.passwordError,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingContent = {
                PasswordVisibilityIcon(
                    visible = passwordVisible,
                    onClick = { onPasswordVisibilityChange(!passwordVisible) }
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.login_select_profile),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        RoleSelector(
            selectedRole = selectedRole,
            onRoleChange = onRoleChange
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onLogin,
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.login_btn_submit), color = Color.White)
            }
        }
        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.login_no_account),
                color = PrimaryBlue,
                style = MaterialTheme.typography.bodySmall
            )
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
        RoleToggleButton(
            text = stringResource(R.string.user_role_admin_short),
            selected = selectedRole == UserRole.ADMIN,
            onClick = { onRoleChange(UserRole.ADMIN) },
            modifier = Modifier.weight(1f)
        )
        RoleToggleButton(
            text = stringResource(R.string.user_role_manager_short),
            selected = selectedRole == UserRole.MANAGER,
            onClick = { onRoleChange(UserRole.MANAGER) },
            modifier = Modifier.weight(1f)
        )
        RoleToggleButton(
            text = stringResource(R.string.user_role_user_short),
            selected = selectedRole == UserRole.USER,
            onClick = { onRoleChange(UserRole.USER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PasswordVisibilityIcon(
    visible: Boolean,
    onClick: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick)
    ) {
        val stroke = Stroke(width = 2.8f)
        val center = Offset(size.width / 2f, size.height / 2f)
        val iconColor = TextSecondary

        drawOval(
            color = iconColor,
            topLeft = Offset(size.width * 0.12f, size.height * 0.28f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.44f),
            style = stroke
        )
        drawCircle(
            color = iconColor,
            radius = if (visible) size.minDimension * 0.14f else size.minDimension * 0.10f,
            center = center,
            style = if (visible) Stroke(width = 2.8f) else Stroke(width = 5f)
        )
        if (!visible) {
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.18f, size.height * 0.82f),
                end = Offset(size.width * 0.82f, size.height * 0.18f),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
private fun RoleToggleButton(
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
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
private fun TaskFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        isError = error != null,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingContent,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderGray
        )
    )
    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
