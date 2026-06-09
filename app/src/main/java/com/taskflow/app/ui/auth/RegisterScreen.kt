package com.taskflow.app.ui.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.taskflow.app.ui.common.rememberWindowInfo
import com.taskflow.app.ui.navigation.Routes

private val PrimaryBlue = Color(0xFF3B6FF0)
private val ScreenBlue = Color(0xFFEEF2FF)
private val BorderGray = Color(0xFFD1D5DB)
private val TextSecondary = Color(0xFF6B7280)

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val windowInfo = rememberWindowInfo()

    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel.clearState()
            navController.navigate(Routes.LOGIN)
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
                .fillMaxWidth(0.78f)
                .widthIn(max = 860.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
        }

        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                RegisterHeader(onBackClick = { navController.popBackStack() })
                Spacer(modifier = Modifier.height(18.dp))
                RegisterAvatar()
                Spacer(modifier = Modifier.height(16.dp))

                if (windowInfo.isLandscape) {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterIdentityFields(
                                name = name,
                                username = username,
                                email = email,
                                formState = formState,
                                onNameChange = { name = it },
                                onUsernameChange = { username = it },
                                onEmailChange = { email = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterPasswordFields(
                                password = password,
                                confirmPassword = confirmPassword,
                                passwordVisible = passwordVisible,
                                confirmPasswordVisible = confirmPasswordVisible,
                                formState = formState,
                                onPasswordChange = { password = it },
                                onConfirmPasswordChange = { confirmPassword = it },
                                onPasswordVisibilityChange = { passwordVisible = it },
                                onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it }
                            )
                        }
                    }
                } else {
                    RegisterIdentityFields(
                        name = name,
                        username = username,
                        email = email,
                        formState = formState,
                        onNameChange = { name = it },
                        onUsernameChange = { username = it },
                        onEmailChange = { email = it }
                    )
                    RegisterPasswordFields(
                        password = password,
                        confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible,
                        confirmPasswordVisible = confirmPasswordVisible,
                        formState = formState,
                        onPasswordChange = { password = it },
                        onConfirmPasswordChange = { confirmPassword = it },
                        onPasswordVisibilityChange = { passwordVisible = it },
                        onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        viewModel.register(name, username, email, password, confirmPassword)
                    },
                    enabled = uiState !is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(text = stringResource(R.string.register_title), color = Color.White)
                    }
                }
                if (uiState is AuthUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource((uiState as AuthUiState.Error).messageRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { navController.navigate(Routes.LOGIN) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.register_already_account),
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawLine(
                    color = Color.Black,
                    start = Offset(size.width * 0.65f, size.height * 0.15f),
                    end = Offset(size.width * 0.30f, size.height * 0.50f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.Black,
                    start = Offset(size.width * 0.30f, size.height * 0.50f),
                    end = Offset(size.width * 0.65f, size.height * 0.85f),
                    strokeWidth = 3f
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RegisterAvatar() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            CameraGlyph()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.register_photo_add),
            color = PrimaryBlue,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CameraGlyph() {
    Canvas(modifier = Modifier.size(30.dp)) {
        val stroke = Stroke(width = 3f)
        drawRoundRect(
            color = TextSecondary,
            topLeft = Offset(size.width * 0.18f, size.height * 0.32f),
            size = Size(size.width * 0.64f, size.height * 0.46f),
            cornerRadius = CornerRadius(4f, 4f),
            style = stroke
        )
        drawRoundRect(
            color = TextSecondary,
            topLeft = Offset(size.width * 0.34f, size.height * 0.22f),
            size = Size(size.width * 0.32f, size.height * 0.14f),
            cornerRadius = CornerRadius(3f, 3f),
            style = stroke
        )
        drawCircle(
            color = TextSecondary,
            radius = size.minDimension * 0.13f,
            center = Offset(size.width * 0.5f, size.height * 0.55f),
            style = stroke
        )
    }
}

@Composable
private fun RegisterIdentityFields(
    name: String,
    username: String,
    email: String,
    formState: AuthFormState,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {
    RegisterTextField(
        label = stringResource(R.string.register_label_name),
        value = name,
        onValueChange = onNameChange,
        placeholder = stringResource(R.string.register_placeholder_name),
        error = formState.nameError?.let { stringResource(it) }
    )
    RegisterTextField(
        label = stringResource(R.string.register_label_username),
        value = username,
        onValueChange = onUsernameChange,
        placeholder = stringResource(R.string.register_placeholder_username),
        error = formState.usernameError?.let { stringResource(it) }
    )
    RegisterTextField(
        label = stringResource(R.string.register_label_email),
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(R.string.register_placeholder_email),
        error = formState.emailError?.let { stringResource(it) },
        keyboardType = KeyboardType.Email
    )
}

@Composable
private fun RegisterPasswordFields(
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    formState: AuthFormState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit
) {
    RegisterTextField(
        label = stringResource(R.string.register_label_password),
        value = password,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.register_placeholder_password),
        error = formState.passwordError?.let { stringResource(it) },
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
    RegisterTextField(
        label = stringResource(R.string.register_label_confirm_password),
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        placeholder = stringResource(R.string.register_placeholder_confirm_password),
        error = formState.confirmPasswordError?.let { stringResource(it) },
        keyboardType = KeyboardType.Password,
        visualTransformation = if (confirmPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            PasswordVisibilityIcon(
                visible = confirmPasswordVisible,
                onClick = { onConfirmPasswordVisibilityChange(!confirmPasswordVisible) }
            )
        }
    )
}

@Composable
private fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
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

