package com.taskflow.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.FormError
import com.taskflow.app.ui.common.components.ProfileAvatar
import com.taskflow.app.ui.common.components.ProfileCard
import com.taskflow.app.ui.common.components.ProfileFormScreen
import com.taskflow.app.ui.common.locale.LanguageManager
import com.taskflow.app.ui.common.theme.Black
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Soft
import com.taskflow.app.ui.common.util.initial
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(nav: NavController, role: String, accent: Color) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedLanguage by remember {
        LanguageManager.languageFlow(context)
    }.collectAsState(initial = LanguageManager.PORTUGUESE)
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val hasChanges = state.user?.let { user ->
        state.name != user.name ||
            state.username != user.username ||
            state.email != user.email ||
            state.photoUrl != user.photoUrl ||
            state.newPassword.isNotBlank()
    } ?: false
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val upload = context.readProfilePhotoUpload(uri)
                viewModel.onPhotoSelected(uri.toString(), upload?.bytes, upload?.contentType)
            }
        }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val uri = cameraPhotoUri
            if (success && uri != null) {
                val upload = context.readProfilePhotoUpload(uri)
                viewModel.onPhotoSelected(uri.toString(), upload?.bytes, upload?.contentType)
            }
        }
    )

    ProfileFormScreen(
        title = stringResource(R.string.profile_title),
        onBack = { nav.popBackStack() },
        confirmOnBack = hasChanges
    ) {
        ProfileCard {
            if (state.isLoading && state.user == null) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accent)
                }
            } else {
                EditableProfileContent(
                    state = state,
                    role = role,
                    accent = accent,
                    onNameChange = viewModel::onNameChange,
                    onUsernameChange = viewModel::onUsernameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onGalleryClick = { photoPicker.launch("image/*") },
                    onCameraClick = {
                        val uri = context.createProfileImageUri()
                        cameraPhotoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { languageCode ->
                        coroutineScope.launch {
                            LanguageManager.setLanguage(context, languageCode)
                        }
                    },
                    onSave = viewModel::saveProfile,
                    onCancel = { nav.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.EditableProfileContent(
    state: ProfileUiState,
    role: String,
    accent: Color,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    ProfileAvatar(
        initial = state.name.initial().ifBlank { role },
        photoUrl = state.photoUrl,
        accent = accent,
        size = 80,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    Row(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.height(38.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Icon(Icons.Outlined.PhotoLibrary, null, modifier = Modifier.size(16.dp), tint = Blue)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.observation_btn_gallery), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(
            onClick = onCameraClick,
            modifier = Modifier.height(38.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = Blue)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.observation_btn_camera), style = MaterialTheme.typography.bodySmall)
        }
    }
    Spacer(Modifier.height(2.dp))
    ProfileFieldInline(
        label = stringResource(R.string.profile_label_name),
        value = state.name,
        onValueChange = onNameChange
    )
    state.nameError?.let { FormError(stringResource(it)) }
    ProfileFieldInline(
        label = stringResource(R.string.register_label_username),
        value = state.username,
        onValueChange = onUsernameChange
    )
    state.usernameError?.let { FormError(stringResource(it)) }
    ProfileFieldInline(
        label = stringResource(R.string.profile_label_email),
        value = state.email,
        onValueChange = onEmailChange
    )
    state.emailError?.let { FormError(stringResource(it)) }
    ProfileFieldInline(
        label = stringResource(R.string.user_label_role),
        value = if (role == "A") stringResource(R.string.dashboard_admin) else if (role == "G") stringResource(R.string.dashboard_manager) else stringResource(R.string.dashboard_user),
        enabled = false
    )
    ProfileFieldInline(
        label = stringResource(R.string.new_password),
        value = state.newPassword,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.keep_current_password)
    )
    state.passwordError?.let { FormError(stringResource(it)) }
    LanguageSelector(
        selectedLanguage = selectedLanguage,
        onLanguageSelected = onLanguageSelected
    )
    state.errorMessageRes?.let { FormError(stringResource(it)) }
    state.successMessageRes?.let {
        Text(stringResource(it), color = com.taskflow.app.ui.common.theme.Green, style = MaterialTheme.typography.bodySmall)
    }
    Button(
        onClick = onSave,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(Blue),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
        } else {
            Text(stringResource(R.string.save_changes))
        }
    }
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Text(stringResource(R.string.btn_cancel))
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.language_title),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageOptionButton(
                text = stringResource(R.string.language_portuguese),
                selected = selectedLanguage == LanguageManager.PORTUGUESE,
                onClick = { onLanguageSelected(LanguageManager.PORTUGUESE) },
                modifier = Modifier.weight(1f)
            )
            LanguageOptionButton(
                text = stringResource(R.string.language_english),
                selected = selectedLanguage == LanguageManager.ENGLISH,
                onClick = { onLanguageSelected(LanguageManager.ENGLISH) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) Blue else Color.White
    val contentColor = if (selected) Color.White else Muted
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) Blue else Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

private fun Context.createProfileImageUri(): Uri {
    val dir = File(cacheDir, "profile-photos").apply { mkdirs() }
    val file = File(dir, "profile_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

private data class ProfilePhotoUpload(
    val bytes: ByteArray,
    val contentType: String
)

private fun Context.readProfilePhotoUpload(uri: Uri): ProfilePhotoUpload? {
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val contentType = contentResolver.getType(uri) ?: "image/jpeg"
    return ProfilePhotoUpload(bytes = bytes, contentType = contentType)
}

@Composable
private fun ProfileFieldInline(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    placeholder: String = "",
    onValueChange: (String) -> Unit = {}
) {
    Column(modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            enabled = enabled,
            placeholder = {
                Text(placeholder, color = Muted, style = MaterialTheme.typography.bodySmall)
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Border,
                unfocusedBorderColor = Border,
                disabledBorderColor = Border,
                disabledContainerColor = Soft,
                disabledTextColor = Color.Black
            )
        )
    }
}
