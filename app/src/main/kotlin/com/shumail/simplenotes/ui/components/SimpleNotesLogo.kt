package com.shumail.simplenotes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom minimal geometric logo for SimpleNotes that dynamically adapts to Material You 3 color tokens.
 */
@Composable
fun SimpleNotesLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val outlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(containerColor)
            .border(1.dp, outlineColor, RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height

            // Folded minimal note sheet silhouette
            val sheetPath = Path().apply {
                moveTo(w * 0.14f, h * 0.08f)
                lineTo(w * 0.66f, h * 0.08f)
                lineTo(w * 0.88f, h * 0.30f)
                lineTo(w * 0.88f, h * 0.92f)
                lineTo(w * 0.14f, h * 0.92f)
                close()
            }
            drawPath(
                path = sheetPath,
                color = primaryColor.copy(alpha = 0.18f)
            )
            drawPath(
                path = sheetPath,
                color = onContainerColor,
                style = Stroke(width = w * 0.075f, cap = StrokeCap.Round)
            )

            // Corner dog-ear fold
            val foldPath = Path().apply {
                moveTo(w * 0.64f, h * 0.09f)
                lineTo(w * 0.64f, h * 0.31f)
                lineTo(w * 0.86f, h * 0.31f)
            }
            drawPath(
                path = foldPath,
                color = primaryColor,
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )

            // Minimalist fountain pen nib / editorial mark inside sheet
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.33f, h * 0.50f),
                end = Offset(w * 0.68f, h * 0.50f),
                strokeWidth = w * 0.08f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = onContainerColor,
                start = Offset(w * 0.33f, h * 0.68f),
                end = Offset(w * 0.56f, h * 0.68f),
                strokeWidth = w * 0.08f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(w * 0.31f, h * 0.27f),
                size = Size(w * 0.18f, h * 0.10f),
                cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
            )
        }
    }
}
