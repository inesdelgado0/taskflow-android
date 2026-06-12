package com.taskflow.app.ui.user.tasks

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.taskflow.app.R
import com.taskflow.app.domain.model.Observation
import java.io.File

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
    val observationSuccessMessage = stringResource(R.string.observation_saved_success)
    var showObservationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar(successMessage)
        }
    }

    LaunchedEffect(state.observationSaved) {
        if (state.observationSaved) {
            showObservationDialog = false
            snackbarHostState.showSnackbar(observationSuccessMessage)
            viewModel.consumeObservationSaved()
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

                state.errorRes != null && state.task == null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(requireNotNull(state.errorRes)), color = MaterialTheme.colorScheme.error)
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
                    ObservationsCard(
                        observations = state.observations,
                        onNewObservation = { showObservationDialog = true }
                    )
                }
            }
        }

        if (showObservationDialog) {
            ObservationFormDialog(
                isSaving = state.isSavingObservation,
                error = state.observationErrorRes?.let { stringResource(it) },
                onDismiss = { showObservationDialog = false },
                onSave = viewModel::saveObservation
            )
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
            .statusBarsPadding()
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
                DetailChip(icon = Icons.Outlined.CalendarToday, text = stringResource(R.string.deadline_label) + ": " + task.deadlineText)
                DetailChip(icon = Icons.Outlined.Group, text = stringResource(R.string.members_count, task.memberCount))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DetailText(label = stringResource(R.string.status_label), value = task.status.name)
                DetailText(label = stringResource(R.string.data_label), value = task.dateText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DetailText(label = stringResource(R.string.location_label), value = task.location.ifBlank { "-" })
                DetailText(label = stringResource(R.string.time_spent), value = "${task.timeSpentMinutes} min")
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
private fun DetailText(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color(0xFF344054))
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
                error = formState.dateError?.let { stringResource(it) }
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
                placeholder = stringResource(R.string.task_completion_placeholder),
                error = formState.percentageError?.let { stringResource(it) }
            )
            UserTextField(
                label = stringResource(R.string.time_spent),
                value = formState.timeSpent,
                onValueChange = onTimeSpentChanged,
                placeholder = stringResource(R.string.time_spent_placeholder),
                error = formState.timeSpentError?.let { stringResource(it) }
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
private fun ObservationsCard(
    observations: List<Observation>,
    onNewObservation: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.observations_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = onNewObservation,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = stringResource(R.string.new_action))
                }
            }

            if (observations.isEmpty()) {
                Text(
                    text = stringResource(R.string.observations_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                observations.forEach { observation ->
                    ObservationListItem(observation = observation)
                }
            }
        }
    }
}

@Composable
private fun ObservationListItem(observation: Observation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            observation.text?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
            observation.photoPath?.takeIf { it.isNotBlank() }?.let {
                ObservationPhoto(photoPath = it)
            }
            Text(
                text = observation.createdAt.toRelativeTime(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ObservationPhoto(photoPath: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AsyncImage(
            model = photoPath,
            contentDescription = stringResource(R.string.observation_photo_attached),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE9EEF5))
        )
        PhotoAttachmentLabel(text = stringResource(R.string.observation_photo_attached))
    }
}

@Composable
private fun ObservationFormDialog(
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String, ByteArray?, String?) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedPhotoUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri = pendingCameraUri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.observations_btn_new)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ObservationTextField(
                    value = text,
                    onValueChange = { text = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.observation_btn_gallery))
                    }
                    OutlinedButton(
                        onClick = {
                            val uri = context.createObservationImageUri()
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.observation_btn_camera))
                    }
                }
                selectedPhotoUri?.let { uri ->
                    SelectedPhotoRow(
                        uriText = context.displayName(uri) ?: stringResource(R.string.observation_photo_attached),
                        onClear = { selectedPhotoUri = null }
                    )
                }
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val photo = selectedPhotoUri?.let { context.readPhotoUpload(it) }
                    onSave(text, photo?.bytes, photo?.contentType)
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.observation_btn_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun ObservationTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(R.string.observation_label_text), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text(stringResource(R.string.observation_text_placeholder)) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFC9D0D8)
            )
        )
    }
}

@Composable
private fun SelectedPhotoRow(
    uriText: String,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = uriText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.btn_delete))
            }
        }
    }
}

@Composable
private fun PhotoAttachmentLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color(0xFFE9EEF5),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.padding(8.dp).size(18.dp),
                tint = TextSecondary
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

private fun Context.createObservationImageUri(): Uri {
    val dir = File(cacheDir, "observations").apply { mkdirs() }
    val file = File(dir, "observation_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

private data class PhotoUpload(
    val bytes: ByteArray,
    val contentType: String
)

private fun Context.readPhotoUpload(uri: Uri): PhotoUpload? {
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val contentType = contentResolver.getType(uri) ?: "image/jpeg"
    return PhotoUpload(bytes = bytes, contentType = contentType)
}

private fun Context.displayName(uri: Uri): String? {
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}

private fun Long.toRelativeTime(): String {
    val elapsed = (System.currentTimeMillis() - this).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        elapsed < minute -> "agora"
        elapsed < hour -> "ha ${elapsed / minute} min"
        elapsed < day -> "ha ${elapsed / hour} h"
        else -> "ha ${elapsed / day} dia(s)"
    }
}
