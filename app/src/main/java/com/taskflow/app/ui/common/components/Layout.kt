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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskflow.app.R
import com.taskflow.app.ui.common.TaskFlowDataUiState
import com.taskflow.app.ui.common.TaskFlowDataViewModel
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Border
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Page
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
    content: @Composable ColumnScope.() -> Unit
) {
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
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(22.dp))
                Box(modifier = Modifier.clickable(onClick = onProfile)) {
                    Avatar(role, accent, 34)
                }
                IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, null) }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@Composable
internal fun ListScreen(title: String, actionText: String?, onBack: () -> Unit, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack, actionText, onAction)
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@Composable
internal fun FormScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack)
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
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
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        if (actionText != null) {
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(Blue), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
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
internal fun ProfileFormScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page)) {
        TopBar(title, onBack)
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { content() }
        }
    }
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
