package com.taskflow.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(
    val title: String,
    val description: String,
    val accent: Color,
    val icon: OnboardingIcon
)

private enum class OnboardingIcon {
    Board,
    Team,
    Progress
}

private val pages = listOf(
    OnboardingPage(
        title = "Bem-vindo ao TaskFlow",
        description = "Gerir os seus projetos e tarefas de forma eficiente",
        accent = Color(0xFF2F7DF6),
        icon = OnboardingIcon.Board
    ),
    OnboardingPage(
        title = "Trabalho em Equipa",
        description = "Colabore com a sua equipa em tempo real",
        accent = Color(0xFF00C853),
        icon = OnboardingIcon.Team
    ),
    OnboardingPage(
        title = "Acompanhe o Progresso",
        description = "Monitorize tarefas e avalie o desempenho",
        accent = Color(0xFFA855F7),
        icon = OnboardingIcon.Progress
    )
)

@Composable
fun OnboardingScreen(
    onLoginRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPage by remember { mutableIntStateOf(0) }
    val page = pages[selectedPage]
    val isLastPage = selectedPage == pages.lastIndex

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFEAF4FF)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                        fadeOut(animationSpec = tween(160)) using
                        SizeTransform(clip = false)
                },
                label = "onboarding_page"
            ) { currentPage ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(8.dp),
                            ambientColor = Color(0x33000000),
                            spotColor = Color(0x33000000)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OnboardingIconView(
                        icon = currentPage.icon,
                        accent = currentPage.accent
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = currentPage.title,
                        color = Color(0xFF111827),
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentPage.description,
                        color = Color(0xFF536171),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    PageIndicator(
                        pageCount = pages.size,
                        selectedPage = selectedPage
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedPage > 0) {
                            OutlinedButton(
                                onClick = { selectedPage = (selectedPage - 1).coerceAtLeast(0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF111827)
                                )
                            ) {
                                Text(
                                    text = "Anterior",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isLastPage) {
                                    onLoginRequested()
                                } else {
                                    selectedPage += 1
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F7DF6))
                        ) {
                            Text(
                                text = if (isLastPage) "Começar" else "Próximo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIconView(
    icon: OnboardingIcon,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(56.dp)
            .padding(4.dp)
    ) {
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        when (icon) {
            OnboardingIcon.Board -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(10.dp.toPx(), 13.dp.toPx()),
                    size = Size(34.dp.toPx(), 29.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                    style = stroke
                )
                drawLine(accent, Offset(18.dp.toPx(), 24.dp.toPx()), Offset(18.dp.toPx(), 34.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(27.dp.toPx(), 27.dp.toPx()), Offset(27.dp.toPx(), 34.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(36.dp.toPx(), 22.dp.toPx()), Offset(36.dp.toPx(), 34.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(20.dp.toPx(), 13.dp.toPx()), Offset(16.dp.toPx(), 8.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(20.dp.toPx(), 13.dp.toPx()), Offset(31.dp.toPx(), 13.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            }

            OnboardingIcon.Team -> {
                drawCircle(accent, radius = 7.dp.toPx(), center = Offset(23.dp.toPx(), 17.dp.toPx()), style = stroke)
                drawCircle(accent, radius = 5.dp.toPx(), center = Offset(37.dp.toPx(), 21.dp.toPx()), style = stroke)
                drawArc(accent, 190f, 160f, false, topLeft = Offset(10.dp.toPx(), 27.dp.toPx()), size = Size(27.dp.toPx(), 22.dp.toPx()), style = stroke)
                drawArc(accent, 205f, 130f, false, topLeft = Offset(30.dp.toPx(), 31.dp.toPx()), size = Size(18.dp.toPx(), 17.dp.toPx()), style = stroke)
            }

            OnboardingIcon.Progress -> {
                drawLine(accent, Offset(12.dp.toPx(), 12.dp.toPx()), Offset(12.dp.toPx(), 42.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(12.dp.toPx(), 42.dp.toPx()), Offset(44.dp.toPx(), 42.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(23.dp.toPx(), 34.dp.toPx()), Offset(23.dp.toPx(), 24.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(32.dp.toPx(), 34.dp.toPx()), Offset(32.dp.toPx(), 18.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent, Offset(41.dp.toPx(), 34.dp.toPx()), Offset(41.dp.toPx(), 28.dp.toPx()), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == selectedPage
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF2F7DF6)
                        else Color(0xFFD1D7DF)
                    )
            )
        }
    }
}
