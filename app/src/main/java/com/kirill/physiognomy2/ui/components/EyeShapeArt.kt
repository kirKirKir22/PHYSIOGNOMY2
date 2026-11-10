package com.kirill.physiognomy2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kirill.physiognomy2.data.EyeShape

@Composable
fun EyeShapeArt(
    shape: EyeShape,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 1.6f,
) {
    Canvas(modifier = modifier.size(width = 76.dp, height = 40.dp)) {
        val w = size.width
        val h = size.height
        val centerY = h * 0.5f

        val (openness, tilt) = when (shape) {
            EyeShape.Almond -> 0.62f to 0f
            EyeShape.Round -> 1f to 0f
            EyeShape.Narrow -> 0.34f to 0f
            EyeShape.Drooping -> 0.55f to -9f
            EyeShape.Raised -> 0.55f to 9f
        }

        val halfHeight = h * 0.5f * openness
        val stroke = Stroke(width = strokeWidth * density)

        val path = Path().apply {
            moveTo(w * 0.02f, centerY)
            quadraticTo(w * 0.28f, centerY - halfHeight, w * 0.5f, centerY - halfHeight * 0.9f)
            quadraticTo(w * 0.72f, centerY - halfHeight, w * 0.98f, centerY)
            quadraticTo(w * 0.72f, centerY + halfHeight, w * 0.5f, centerY + halfHeight * 0.9f)
            quadraticTo(w * 0.28f, centerY + halfHeight, w * 0.02f, centerY)
            close()
        }

        rotate(degrees = tilt, pivot = Offset(w / 2f, centerY)) {
            drawPath(path, color = color, style = stroke)

            val irisRadius = (halfHeight * 0.62f).coerceAtLeast(h * 0.1f)
            drawCircle(
                color = color,
                radius = irisRadius,
                center = Offset(w * 0.48f, centerY),
                style = stroke,
            )
            drawCircle(
                color = color,
                radius = irisRadius * 0.34f,
                center = Offset(w * 0.48f, centerY),
            )
            drawArc(
                color = color.copy(alpha = 0.55f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.3f, centerY - halfHeight * 0.8f),
                size = Size(w * 0.42f, halfHeight * 1.6f),
                style = Stroke(width = strokeWidth * 0.9f * density),
            )
        }
    }
}
