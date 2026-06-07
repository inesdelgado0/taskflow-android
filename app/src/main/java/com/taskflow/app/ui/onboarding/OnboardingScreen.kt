package com.taskflow.app.ui.onboarding

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taskflow.app.R
import com.taskflow.app.ui.common.components.Dots
import com.taskflow.app.ui.common.theme.Blue
import com.taskflow.app.ui.common.theme.Green
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.OnboardingPage
import com.taskflow.app.ui.common.theme.Purple

private data class OnboardData(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
fun TaskFlowOnboardingScreen(step: Int, onNext: () -> Unit, onBack: () -> Unit) {
    val data = listOf(
        OnboardData(stringResource(R.string.onboarding_title_1), stringResource(R.string.onboarding_desc_1), Icons.Default.Work),
        OnboardData(stringResource(R.string.onboarding_title_2), stringResource(R.string.onboarding_desc_2), Icons.Default.Group),
        OnboardData(stringResource(R.string.onboarding_title_3), stringResource(R.string.onboarding_desc_3), Icons.Default.CheckBox)
    )
    val item = data[step.coerceIn(data.indices)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingPage)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(item.icon, null, tint = if (step == 1) Green else if (step == 2) Purple else Blue, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(36.dp))
                Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(item.subtitle, color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                Dots(step)
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    if (step > 0) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Text(stringResource(R.string.btn_back))
                        }
                    }
                    Button(onClick = onNext, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(Blue)) {
                        Text(if (step == 2) stringResource(R.string.onboarding_btn_start) else stringResource(R.string.onboarding_btn_next))
                    }
                }
            }
        }
    }
}
