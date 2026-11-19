package com.breadcrumbradar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breadcrumbradar.app.ui.theme.DarkBorder
import com.breadcrumbradar.app.ui.theme.DarkSurfaceElevated
import com.breadcrumbradar.app.ui.theme.DarkSurfaceGlass

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    glowColor: Color? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
        )
    )

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = Color.Transparent,
        border = border ?: BorderStroke(
            1.dp,
            glowColor?.copy(alpha = 0.35f) ?: MaterialTheme.colorScheme.outline
        ),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(bgBrush)
                .padding(contentPadding),
            content = content
        )
    }
}
