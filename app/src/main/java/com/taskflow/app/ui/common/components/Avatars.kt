package com.taskflow.app.ui.common.components

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.taskflow.app.ui.common.theme.Muted
import com.taskflow.app.ui.common.theme.White

@Composable
internal fun Avatar(text: String, color: Color, size: Int, camera: Boolean = false) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        if (camera) Icon(Icons.Default.CameraAlt, null, tint = Muted)
        else Text(text, color = White, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun ProfileAvatar(
    initial: String,
    photoUrl: String?,
    accent: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(accent),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageURI(Uri.parse(photoUrl))
                    }
                },
                update = { image ->
                    image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setImageURI(Uri.parse(photoUrl))
                }
            )
        } else {
            Text(initial, color = White, fontWeight = FontWeight.Bold)
        }
    }
}
