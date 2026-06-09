package com.taskflow.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R
import com.taskflow.app.domain.util.UserRole
import com.taskflow.app.ui.common.components.Avatar
import com.taskflow.app.ui.common.components.Field
import com.taskflow.app.ui.common.components.FormError
import com.taskflow.app.ui.common.components.FormScreen
import com.taskflow.app.ui.common.components.Label
import com.taskflow.app.ui.common.components.SectionCard
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.util.initial
import com.taskflow.app.ui.common.util.toDemoUser

@Composable
fun UserFormScreen(nav: NavController, edit: Boolean) {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val user = state.users.firstOrNull { it.id == state.selectedUserId } ?: state.users.firstOrNull()
    var pendingPhoto by rememberSaveable(user?.id, edit) { mutableStateOf(if (edit) user?.photoUrl else null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> pendingPhoto = uri?.toString() }
    )
    var name by rememberSaveable(user?.id, edit) { mutableStateOf(if (edit) user?.name.orEmpty() else "") }
    var username by rememberSaveable(user?.id, edit) { mutableStateOf(if (edit) user?.username.orEmpty() else "") }
    var email by rememberSaveable(user?.id, edit) { mutableStateOf(if (edit) user?.email.orEmpty() else "") }
    var role by rememberSaveable(user?.id, edit) { mutableStateOf(if (edit) user?.role ?: UserRole.USER else UserRole.USER) }
    var password by rememberSaveable(user?.id, edit) { mutableStateOf("") }
    var confirmPassword by rememberSaveable(user?.id, edit) { mutableStateOf("") }
    var passwordVisible by rememberSaveable(user?.id, edit) { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable(user?.id, edit) { mutableStateOf(false) }
    FormScreen(
        title = if (edit) stringResource(R.string.edit_user) else stringResource(R.string.create_user),
        onBack = { nav.popBackStack() },
        confirmOnBack = true
    ) {
        Avatar(if (edit) user?.name.initial() else "", if (edit) user?.toDemoUser()?.color ?: Green else androidx.compose.ui.graphics.Color(0xFFE5E7EB), size = 82, camera = !edit)
        TextButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)) {
            Text(if (edit) stringResource(R.string.change_photo) else stringResource(R.string.add_photo), color = Blue)
        }
        Field(stringResource(R.string.full_name), name, onValueChange = { name = it })
        Field(stringResource(R.string.register_label_username), username, onValueChange = { username = it })
        Field(stringResource(R.string.profile_label_email), email, onValueChange = { email = it })
        Label(stringResource(R.string.user_label_role))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            UserRole.entries.forEach { item ->
                FilterChip(selected = role == item, onClick = { role = item }, label = { Text(item.name) })
            }
        }
        PasswordField(
            label = if (edit) stringResource(R.string.new_password) else stringResource(R.string.password_label),
            value = password,
            visible = passwordVisible,
            onVisibilityChange = { passwordVisible = it },
            onValueChange = { password = it }
        )
        if (!edit) {
            PasswordField(
                label = stringResource(R.string.confirm_password_label),
                value = confirmPassword,
                visible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = it },
                onValueChange = { confirmPassword = it }
            )
        }
        if (!edit && password != confirmPassword && confirmPassword.isNotBlank()) {
            FormError("As palavras-passe nao coincidem.")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (edit || password == confirmPassword) {
                        viewModel.saveUser(
                            existing = if (edit) user else null,
                            name = name,
                            username = username,
                            email = email,
                            role = role,
                            password = password,
                            photoUrl = pendingPhoto,
                            onDone = { nav.popBackStack() }
                        )
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (edit) stringResource(R.string.save_changes) else stringResource(R.string.create_user))
            }
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit
) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Label(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { onVisibilityChange(!visible) }) {
                    Icon(
                        imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = label
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Border, unfocusedBorderColor = Border)
        )
    }
}
