package com.taskflow.app.ui.user

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PageBackground = Color(0xFFF6F7F9)
private val CardBorder = Color(0xFFE2E6EA)
private val PrimaryBlue = Color(0xFF2F7DF6)
private val Orange = Color(0xFFFF6A00)
private val TextSecondary = Color(0xFF667085)
private val ProgressTrack = Color(0xFFE5E7EB)

@Composable
fun UserDashboardScreen(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        UserTopBar(onLogout = onLogout)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Bem-vindo,",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "João Silva",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Tarefas Ativas",
                    value = "8",
                    detail = "3 com prazo próximo",
                    accentColor = Orange,
                    icon = StatIcon.Tasks
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Tempo Total",
                    value = "24h",
                    detail = "Esta semana",
                    accentColor = PrimaryBlue,
                    icon = StatIcon.Clock
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tarefas Pendentes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PendingTaskCard(
                        title = "Desenvolver página de login",
                        project = "Website Redesign",
                        deadline = "2 dias",
                        progress = 0.60f
                    )
                    PendingTaskCard(
                        title = "Testar integração API",
                        project = "Mobile App",
                        deadline = "5 dias",
                        progress = 0.30f
                    )
                    PendingTaskCard(
                        title = "Documentar endpoints",
                        project = "API Development",
                        deadline = "1 semana",
                        progress = 0f
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(text = "Ver Histórico de Tarefas Concluídas")
            }
        }
    }
}

@Composable
private fun UserTopBar(
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "TaskFlow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            NotificationIcon()
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Orange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            LogoutIcon(onClick = onLogout)
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    detail: String,
    accentColor: Color,
    icon: StatIcon
) {
    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StatIconView(icon = icon, color = accentColor)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PendingTaskCard(
    title: String,
    project: String,
    deadline: String,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = project,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = deadline,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressBar(
                    progress = progress,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .background(ProgressTrack, RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .background(PrimaryBlue, RoundedCornerShape(50))
        )
    }
}

@Composable
private fun NotificationIcon() {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.4f)
        drawArc(
            color = Color.Black,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
            size = Size(size.width * 0.56f, size.height * 0.58f),
            style = stroke
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.26f, size.height * 0.68f),
            end = Offset(size.width * 0.74f, size.height * 0.68f),
            strokeWidth = 2.4f
        )
        drawCircle(
            color = Color.Black,
            radius = 2.2f,
            center = Offset(size.width * 0.5f, size.height * 0.80f)
        )
    }
}

@Composable
private fun LogoutIcon(
    onClick: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick)
    ) {
        val stroke = Stroke(width = 2.3f)
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.18f, size.height * 0.20f),
            end = Offset(size.width * 0.18f, size.height * 0.80f),
            strokeWidth = 2.3f
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.18f, size.height * 0.20f),
            end = Offset(size.width * 0.44f, size.height * 0.20f),
            strokeWidth = 2.3f
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.18f, size.height * 0.80f),
            end = Offset(size.width * 0.44f, size.height * 0.80f),
            strokeWidth = 2.3f
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.36f, size.height * 0.50f),
            end = Offset(size.width * 0.82f, size.height * 0.50f),
            strokeWidth = 2.3f
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.66f, size.height * 0.34f),
            end = Offset(size.width * 0.82f, size.height * 0.50f),
            strokeWidth = 2.3f
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width * 0.66f, size.height * 0.66f),
            end = Offset(size.width * 0.82f, size.height * 0.50f),
            strokeWidth = 2.3f
        )
    }
}

@Composable
private fun StatIconView(
    icon: StatIcon,
    color: Color
) {
    Canvas(modifier = Modifier.size(30.dp)) {
        when (icon) {
            StatIcon.Tasks -> {
                val stroke = Stroke(width = 2.8f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.18f),
                    size = Size(size.width * 0.58f, size.height * 0.62f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.48f),
                    end = Offset(size.width * 0.46f, size.height * 0.60f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.46f, size.height * 0.60f),
                    end = Offset(size.width * 0.84f, size.height * 0.24f),
                    strokeWidth = 3f
                )
            }
            StatIcon.Clock -> {
                val stroke = Stroke(width = 2.8f)
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.38f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.5f),
                    end = Offset(size.width * 0.5f, size.height * 0.28f),
                    strokeWidth = 2.8f
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.5f),
                    end = Offset(size.width * 0.66f, size.height * 0.58f),
                    strokeWidth = 2.8f
                )
            }
        }
    }
}

private enum class StatIcon {
    Tasks,
    Clock
}

