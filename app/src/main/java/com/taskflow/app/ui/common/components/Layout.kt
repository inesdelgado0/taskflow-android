package com.taskflow.app.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskflow.app.R
import com.taskflow.app.ui.common.TaskFlowDataUiState
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.rememberWindowInfo
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Page
import com.taskflow.app.ui.common.theme.Red
import com.taskflow.app.ui.common.theme.White

@Composable
internal fun taskFlowState(): State<TaskFlowDataUiState> {
    val viewModel: TaskFlowDataViewModel = hiltViewModel()
    return viewModel.uiState.collectAsState()
}

@Composable
internal fun AppScaffold(
    role: String,
    accent: Color,
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onNotificationClick: (Long?) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val windowInfo = rememberWindowInfo()
    val state by taskFlowState()
    val notifications = state.inAppNotifications()
    var notificationsExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val contentModifier = Modifier
        .fillMaxWidth()
        .widthIn(max = if (windowInfo.isLandscape) 920.dp else 560.dp)
    val openProfileDescription = stringResource(R.string.cd_open_profile)

    Column(Modifier.fillMaxSize().background(Page)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .background(White)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TaskFlow", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box {
                    IconButton(onClick = { notificationsExpanded = true }) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge { Text(notifications.size.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                stringResource(R.string.cd_notifications),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = notificationsExpanded,
                        onDismissRequest = { notificationsExpanded = false }
                    ) {
                        if (notifications.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(stringResource(R.string.sync_status_synced), fontWeight = FontWeight.SemiBold)
                                        Text("Sem alertas no momento", color = Muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = { notificationsExpanded = false }
                            )
                        } else {
                            notifications.forEach { notification ->
                                DropdownMenuItem(
                                    text = {
                                        Column(Modifier.widthIn(min = 220.dp, max = 320.dp)) {
                                            Text(notification.title, fontWeight = FontWeight.SemiBold)
                                            Text(notification.message, color = Muted, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        notificationsExpanded = false
                                        onNotificationClick(notification.taskId)
                                    }
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .semantics { contentDescription = openProfileDescription }
                        .clickable(onClick = onProfile)
                ) {
                    Avatar(role, accent, 34)
                }
                IconButton(onClick = { showLogoutDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, stringResource(R.string.cd_logout))
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }

    ConfirmActionDialog(
        visible = showLogoutDialog,
        title = stringResource(R.string.dialog_logout_title),
        message = stringResource(R.string.dialog_logout_message),
        confirmText = stringResource(R.string.profile_btn_logout),
        onConfirm = {
            showLogoutDialog = false
            onLogout()
        },
        onDismiss = { showLogoutDialog = false }
    )
}

@Composable
internal fun ListScreen(title: String, actionText: String?, onBack: () -> Unit, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val windowInfo = rememberWindowInfo()
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack, actionText, onAction)
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = if (windowInfo.isLandscape) 920.dp else 560.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
internal fun FormScreen(
    title: String,
    onBack: () -> Unit,
    confirmOnBack: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val windowInfo = rememberWindowInfo()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val handleBack = {
        if (confirmOnBack) showDiscardDialog = true else onBack()
    }
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, handleBack)
        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = if (windowInfo.isLandscape) 720.dp else 560.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )
            }
        }
    }

    ConfirmActionDialog(
        visible = showDiscardDialog,
        title = stringResource(R.string.dialog_discard_changes_title),
        message = stringResource(R.string.dialog_discard_changes_message),
        confirmText = stringResource(R.string.btn_discard),
        onConfirm = {
            showDiscardDialog = false
            onBack()
        },
        onDismiss = { showDiscardDialog = false }
    )
}

@Composable
internal fun TopBar(title: String, onBack: () -> Unit, actionText: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back)) }
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (actionText != null) {
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(Blue), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(actionText)
            }
        }
    }
}

@Composable
internal fun Welcome(name: String) {
    Column {
        Text(stringResource(R.string.dashboard_welcome), color = Muted)
        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
internal fun ProfileFormScreen(
    title: String,
    onBack: () -> Unit,
    confirmOnBack: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val windowInfo = rememberWindowInfo()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val handleBack = {
        if (confirmOnBack) showDiscardDialog = true else onBack()
    }
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, handleBack)
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = if (windowInfo.isLandscape) 560.dp else 430.dp),
                    content = content
                )
            }
        }
    }

    ConfirmActionDialog(
        visible = showDiscardDialog,
        title = stringResource(R.string.dialog_discard_changes_title),
        message = stringResource(R.string.dialog_discard_changes_message),
        confirmText = stringResource(R.string.btn_discard),
        onConfirm = {
            showDiscardDialog = false
            onBack()
        },
        onDismiss = { showDiscardDialog = false }
    )
}

@Composable
internal fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 430.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(White),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
internal fun ConfirmActionDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
