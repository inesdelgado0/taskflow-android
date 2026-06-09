package com.taskflow.app.ui.manager.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskflow.app.R
import com.taskflow.app.domain.model.Evaluation
import com.taskflow.app.domain.model.User

// ============================================================
// EvaluationScreen.kt
// app/src/main/java/com/taskflow/app/ui/manager/evaluation/
// ============================================================

@Composable
fun EvaluationScreen(
    projectId: Long,
    evaluatorId: Long,
    users: List<User>,               // utilizadores do projeto passados via nav
    onDone: () -> Unit,
    viewModel: EvaluationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId, evaluatorId)
    }

    // Navegar para trás após guardar todos
    LaunchedEffect(state.isSaved) {
        if (state.isSaved && state.selectedUser == null) onDone()
    }

    if (state.selectedUser != null) {
        // ── Ecrã de avaliação individual ─────────────────────
        EvaluationFormScreen(
            user        = state.selectedUser!!,
            rating      = state.rating,
            comment     = state.comment,
            isLoading   = state.isLoading,
            isSaved     = state.isSaved,
            errorMessage = state.errorMessageRes?.let { stringResource(it) },
            onRating    = viewModel::onRatingChange,
            onComment   = viewModel::onCommentChange,
            onSubmit    = { viewModel.submitEvaluation(projectId, evaluatorId) },
            onBack      = { viewModel.resetForm() }
        )
    } else {
        // ── Lista de utilizadores a avaliar ───────────────────
        UserListScreen(
            users               = users,
            existingEvaluations = state.existingEvaluations,
            onSelectUser        = { user -> viewModel.selectUser(user, projectId) }
        )
    }
}

// ── Lista de utilizadores ─────────────────────────────────────

@Composable
private fun UserListScreen(
    users: List<User>,
    existingEvaluations: List<Evaluation>,
    onSelectUser: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = "Avaliar utilizadores",
            style    = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.id }) { user ->
                val existing = existingEvaluations.find { it.evaluatedUserId == user.id }
                UserEvaluationCard(
                    user       = user,
                    evaluation = existing,
                    onClick    = { onSelectUser(user) }
                )
            }
        }
    }
}

@Composable
private fun UserEvaluationCard(
    user: User,
    evaluation: Evaluation?,
    onClick: () -> Unit
) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleMedium)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (evaluation != null) {
                Column(horizontalAlignment = Alignment.End) {
                    StarRow(rating = evaluation.rating, size = 16.dp)
                    Text(
                        text  = "Avaliado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text  = "Por avaliar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Formulário de avaliação ───────────────────────────────────

@Composable
private fun EvaluationFormScreen(
    user: User,
    rating: Int,
    comment: String,
    isLoading: Boolean,
    isSaved: Boolean,
    errorMessage: String?,
    onRating: (Int) -> Unit,
    onComment: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text  = "Avaliar ${user.name}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text  = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // Rating de estrelas 1-5
        Text("Classificação", style = MaterialTheme.typography.titleMedium)
        RatingSelector(
            current  = rating,
            onSelect = onRating
        )

        // Comentário
        Text("Comentário (opcional)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value         = comment,
            onValueChange = onComment,
            modifier      = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder   = { Text("Descreve o desempenho do utilizador...") },
            maxLines      = 5
        )

        // Mensagem de erro
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        // Botões
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick  = onBack,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.btn_cancel)) }

            Button(
                onClick  = onSubmit,
                enabled  = !isLoading && !isSaved,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.btn_save))
                }
            }
        }
    }
}

// ── Componentes reutilizáveis ─────────────────────────────────

@Composable
fun RatingSelector(
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { star ->
            IconButton(onClick = { onSelect(star) }) {
                Icon(
                    imageVector = if (star <= current) Icons.Filled.Star
                    else Icons.Outlined.StarOutline,
                    contentDescription = "$star estrelas",
                    tint   = if (star <= current) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun StarRow(
    rating: Int,
    size: androidx.compose.ui.unit.Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        (1..5).forEach { star ->
            Icon(
                imageVector      = if (star <= rating) Icons.Filled.Star
                else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint             = if (star <= rating) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier         = Modifier.size(size)
            )
        }
    }
}
