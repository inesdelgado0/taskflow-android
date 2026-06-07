package com.taskflow.app.ui.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.Red

@Composable
internal fun FormError(text: String) {
    Text(text, color = Red, style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun EmptyData() {
    Text(
        text = androidx.compose.ui.res.stringResource(com.taskflow.app.R.string.empty_synced_data),
        color = Muted,
        style = MaterialTheme.typography.bodyMedium
    )
}
