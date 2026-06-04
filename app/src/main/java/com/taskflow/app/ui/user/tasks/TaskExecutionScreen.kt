package com.taskflow.app.ui.user.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taskflow.app.R

private val PageBackground = Color(0xFFF6F7F9)
private val CardBorder = Color(0xFFE2E6EA)
private val PrimaryBlue = Color(0xFF2F7DF6)
private val TextSecondary = Color(0xFF667085)
private val ProgressTrack = Color(0xFFE5E7EB)
private val PriorityBackground = Color(0xFFFFEFE7)
private val PriorityText = Color(0xFFFF6A00)

@Composable
fun TaskExecutionScreen(
    nav: NavController,
    viewModel: TaskExecutionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.progress_saved_success)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar(successMessage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            UserBackTopBar(
                title = stringResource(R.string.task_detail_title),
                onBack = { nav.popBackStack() }
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }

                state.error != null && state.task == null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }

                state.task != null -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TaskDetailCard(task = requireNotNull(state.task))
                    ProgressFormCard(
                        formState = formState,
                        isSaving = state.isSaving,
                        onDateChanged = viewModel::onDateChanged,
                        onLocationChanged = viewModel::onLocationChanged,
                        onPercentageChanged = viewModel::onPercentageChanged,
                        onTimeSpentChanged = viewModel::onTimeSpentChanged,
                        onSave = viewModel::saveProgress
                    )
                    ObservationsPlaceholder()
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun UserBackTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.btn_back))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun TaskDetailCard(task: UserTaskItemUi) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    color = PriorityBackground,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = task.priority,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = PriorityText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = stringResource(R.string.project_prefix, task.projectName),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = task.description.ifBlank { stringResource(R.string.no_description) },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF344054)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DetailChip(icon = Icons.Outlined.CalendarToday, text = task.dateText)
                DetailChip(icon = Icons.Outlined.Group, text = stringResource(R.string.members_count, 0))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.progress_label), style = MaterialTheme.typography.bodySmall)
                Text("${task.progress}%", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                color = PrimaryBlue,
                trackColor = ProgressTrack
            )
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressFormCard(
    formState: TaskProgressFormState,
    isSaving: Boolean,
    onDateChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onPercentageChanged: (String) -> Unit,
    onTimeSpentChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.task_progress_register),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            UserTextField(
                label = stringResource(R.string.data_label),
                value = formState.date,
                onValueChange = onDateChanged,
                placeholder = stringResource(R.string.task_deadline_placeholder),
                error = formState.dateError
            )
            UserTextField(
                label = stringResource(R.string.location_label),
                value = formState.location,
                onValueChange = onLocationChanged,
                placeholder = stringResource(R.string.location_placeholder),
                error = null,
                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) }
            )
            UserTextField(
                label = stringResource(R.string.completion_percentage),
                value = formState.percentage,
                onValueChange = onPercentageChanged,
                placeholder = "0-100",
                error = formState.percentageError
            )
            UserTextField(
                label = stringResource(R.string.time_spent),
                value = formState.timeSpent,
                onValueChange = onTimeSpentChanged,
                placeholder = stringResource(R.string.time_spent_placeholder),
                error = formState.timeSpentError
            )

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), color = Color.White)
                } else {
                    Text(text = stringResource(R.string.save_progress))
                }
            }
        }
    }
}

@Composable
private fun UserTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFC9D0D8)
            )
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ObservationsPlaceholder() {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.observations_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.observations_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
