package com.kirill.physiognomy2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kirill.physiognomy2.data.EyeShape

enum class FeatureArtKind {
    Forehead, Brows, Eyes, Eyelids, Lashes, Bags, Nose, Ears, Cheeks,
    Mouth, Lips, Teeth, Smile, Jaw, Chin, ChinBrows, Dimples, Wrinkles,
    Beard, FaceShape, FaceType, Zones, Profile, HeadShape,
}

@Composable
fun FeatureArt(
    kind: FeatureArtKind,
    variant: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    eyeShape: EyeShape? = null,
    strokeWidth: Float = 1.5f,
) {
    Canvas(modifier = modifier.size(width = 56.dp, height = 40.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = strokeWidth * density)
        val thin = Stroke(width = strokeWidth * 0.65f * density)

        when (kind) {
            FeatureArtKind.Forehead -> drawForehead(w, h, label, color, stroke, thin)
            FeatureArtKind.Brows -> drawBrows(w, h, label, color, stroke)
            FeatureArtKind.Eyes -> drawEyeArt(w, h, eyeShape, label, color, strokeWidth)
            FeatureArtKind.Eyelids -> drawEyelids(w, h, variant, color, stroke)
            FeatureArtKind.Lashes -> drawLashes(w, h, variant, color, stroke)
            FeatureArtKind.Bags -> drawBags(w, h, variant, color, stroke)
            FeatureArtKind.Nose -> drawNose(w, h, label, color, stroke)
            FeatureArtKind.Ears -> drawEars(w, h, label, color, stroke)
            FeatureArtKind.Cheeks -> drawCheeks(w, h, variant, color, stroke, thin)
            FeatureArtKind.Mouth, FeatureArtKind.Smile -> drawMouth(w, h, label, color, stroke)
            FeatureArtKind.Lips -> drawLips(w, h, variant, label, color, stroke)
            FeatureArtKind.Teeth -> drawTeeth(w, h, label, color, stroke)
            FeatureArtKind.Jaw -> drawJaw(w, h, label, color, stroke)
            FeatureArtKind.Chin -> drawChin(w, h, label, color, stroke)
            FeatureArtKind.ChinBrows -> drawChinBrows(w, h, label, color, stroke)
            FeatureArtKind.Dimples -> drawDimples(w, h, label, color, stroke)
            FeatureArtKind.Wrinkles -> drawWrinkles(w, h, label, color, stroke, thin)
            FeatureArtKind.Beard -> drawBeard(w, h, variant, color, stroke)
            FeatureArtKind.FaceShape -> drawFaceShape(w, h, variant, color, stroke)
            FeatureArtKind.FaceType -> drawFaceType(w, h, variant, color, stroke, thin)
            FeatureArtKind.Zones -> drawZones(w, h, variant, color, thin)
            FeatureArtKind.Profile -> drawProfile(w, h, label, color, stroke, thin)
            FeatureArtKind.HeadShape -> drawHeadShape(w, h, variant, color, stroke)
        }
    }
}

private fun DrawScope.outline(
    w: Float,
    h: Float,
    color: Color,
    stroke: Stroke,
    alpha: Float = 0.3f,
) {
    val r = minOf(w, h) * 0.9f
    drawOval(
        color = color.copy(alpha = alpha),
        topLeft = Offset((w - r * 0.72f) / 2f, (h - r) / 2f),
        size = Size(r * 0.72f, r),
        style = stroke,
    )
}

private fun DrawScope.drawForehead(
    w: Float, h: Float, label: String, color: Color, stroke: Stroke, thin: Stroke,
) {
    val bulky = label.contains("Крутой")
    val sloping = label.contains("Покатый")
    val flat = label.contains("Прямой")
    val ridge = label.contains("Надбровные дуги")
    val smooth = label.contains("Отсутствие")
    val will = label.contains("Волевая")

    val left = w * 0.18f
    val right = w * 0.82f
    val centerX = w * 0.5f

    val hairY = when {
        bulky -> 0.16f
        sloping -> 0.3f
        flat -> 0.22f
        else -> 0.22f
    }

    val face = Path().apply {
        moveTo(left, h * 0.94f)
        cubicTo(left, h * 0.66f, centerX - w * 0.3f, h * 0.5f, centerX - w * 0.24f, h * (hairY + 0.12f))
        cubicTo(centerX - w * 0.16f, h * (hairY - 0.06f), centerX + w * 0.16f, h * (hairY - 0.06f), centerX + w * 0.24f, h * (hairY + 0.12f))
        cubicTo(centerX + w * 0.3f, h * 0.5f, right, h * 0.66f, right, h * 0.94f)
    }
    drawPath(face, color = color.copy(alpha = 0.08f))
    drawPath(face, color = color, style = stroke)

    val hairline = Path().apply {
        moveTo(centerX - w * 0.24f, h * (hairY + 0.12f))
        when {
            sloping -> cubicTo(centerX - w * 0.1f, h * (hairY + 0.02f), centerX + w * 0.1f, h * (hairY + 0.02f), centerX + w * 0.24f, h * (hairY + 0.12f))
            bulky -> cubicTo(centerX - w * 0.12f, h * (hairY + 0.06f), centerX + w * 0.12f, h * (hairY + 0.06f), centerX + w * 0.24f, h * (hairY + 0.12f))
            else -> cubicTo(centerX - w * 0.08f, h * (hairY - 0.02f), centerX + w * 0.08f, h * (hairY - 0.02f), centerX + w * 0.24f, h * (hairY + 0.12f))
        }
    }
    drawPath(hairline, color = color.copy(alpha = 0.85f), style = Stroke(width = stroke.width * 1.3f))

    val browY = h * 0.52f
    if (ridge) {
        drawPath(
            Path().apply {
                moveTo(w * 0.26f, browY + h * 0.03f)
                quadraticTo(w * 0.38f, browY - h * 0.09f, w * 0.5f, browY)
                quadraticTo(w * 0.62f, browY - h * 0.09f, w * 0.74f, browY + h * 0.03f)
            },
            color = color,
            style = Stroke(width = stroke.width * 2.2f),
        )
    } else if (smooth) {
        drawLine(color.copy(alpha = 0.55f), Offset(w * 0.26f, browY), Offset(w * 0.74f, browY), stroke.width)
    } else {
        drawLine(color.copy(alpha = 0.7f), Offset(w * 0.28f, browY), Offset(w * 0.72f, browY), stroke.width * 0.9f)
    }

    if (will) {
        drawCircle(color, radius = w * 0.045f, center = Offset(centerX, h * 0.44f))
        drawCircle(color.copy(alpha = 0.4f), radius = w * 0.08f, center = Offset(centerX, h * 0.44f), style = thin)
    }

    if (flat && !ridge) {
        drawLine(color.copy(alpha = 0.5f), Offset(w * 0.3f, h * 0.34f), Offset(w * 0.7f, h * 0.34f), thin.width)
    }
}

private fun DrawScope.drawBrows(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val arched = label.contains("зогнут")
    val broken = label.contains("оманые")
    val y = when {
        label.contains("Высокие") -> 0.3f
        label.contains("Низкие") -> 0.52f
        else -> 0.4f
    }
    val thickness = when {
        label.contains("Густые") -> 0.15f
        label.contains("Косматые") -> 0.12f
        label.contains("Разреженные") -> 0.045f
        label.contains("Тонкие") -> 0.04f
        label.contains("Хамелеон") -> 0.03f
        else -> 0.075f
    } * h

    listOf(0.1f to 0.44f, 0.56f to 0.9f).forEach { (startX, endX) ->
        val innerX = if (startX > 0.5f) startX else endX
        val outerX = if (startX > 0.5f) endX else startX
        val midX = (innerX + outerX) / 2f
        val path = Path().apply {
            moveTo(w * outerX, h * y)
            when {
                broken -> {
                    lineTo(w * (outerX + (midX - outerX) * 0.55f), h * (y - 0.03f))
                    lineTo(w * midX, h * (y - 0.16f))
                    lineTo(w * (midX + (innerX - midX) * 0.45f), h * (y - 0.05f))
                    lineTo(w * innerX, h * (y + 0.04f))
                }
                arched -> {
                    cubicTo(
                        w * (outerX + (midX - outerX) * 0.4f), h * (y - 0.24f),
                        w * (midX + (innerX - midX) * 0.6f), h * (y - 0.24f),
                        w * innerX, h * (y + 0.04f),
                    )
                }
                else -> {
                    cubicTo(
                        w * (outerX + (midX - outerX) * 0.4f), h * (y - 0.09f),
                        w * (midX + (innerX - midX) * 0.6f), h * (y - 0.09f),
                        w * innerX, h * (y + 0.03f),
                    )
                }
            }
        }
        drawPath(path, color = color, style = Stroke(width = thickness.coerceAtLeast(stroke.width)))
    }

    if (label.contains("Сходящиеся")) {
        drawLine(color, Offset(w * 0.45f, h * (y + 0.04f)), Offset(w * 0.55f, h * (y + 0.04f)), thickness)
    }
    if (label.contains("Крыловидные")) {
        drawLine(color, Offset(w * 0.44f, h * (y + 0.04f)), Offset(w * 0.41f, h * (y - 0.12f)), thickness)
        drawLine(color, Offset(w * 0.56f, h * (y + 0.04f)), Offset(w * 0.59f, h * (y - 0.12f)), thickness)
    }
    if (label.contains("Разреженные у основания")) {
        drawCircle(color, radius = w * 0.018f, center = Offset(w * 0.13f, h * (y + 0.06f)))
        drawCircle(color, radius = w * 0.018f, center = Offset(w * 0.87f, h * (y + 0.06f)))
    }
    if (label.contains("Разреженные на концах")) {
        drawCircle(color, radius = w * 0.018f, center = Offset(w * 0.42f, h * (y + 0.03f)))
        drawCircle(color, radius = w * 0.018f, center = Offset(w * 0.58f, h * (y + 0.03f)))
    }
    if (label.contains("Управленческие")) {
        drawLine(
            color = color.copy(alpha = 0.55f),
            start = Offset(w * 0.3f, h * (y + 0.3f)),
            end = Offset(w * 0.7f, h * (y + 0.3f)),
            strokeWidth = stroke.width * 0.7f,
        )
    }
}

private fun DrawScope.drawEyeArt(
    w: Float,
    h: Float,
    shape: EyeShape?,
    label: String,
    color: Color,
    strokeWidth: Float,
) {
    val centerY = h * 0.5f
    val wide = label.contains("Широко")
    val close = label.contains("Близко")
    val up = label.contains("вверх") || label.contains("приподнятым")
    val down = label.contains("вниз") || label.contains("опущенным")
    val bulge = label.contains("Навыкате")
    val deep = label.contains("Глубоко")
    val bigIris = label.contains("Большая")
    val smallIris = label.contains("Маленькая")

    val (openness, tilt) = when {
        shape == EyeShape.Round -> 1f to 0f
        shape == EyeShape.Narrow -> 0.34f to 0f
        shape == EyeShape.Drooping || down -> 0.55f to -10f
        shape == EyeShape.Raised || up -> 0.58f to 10f
        shape == EyeShape.Almond -> 0.62f to 0f
        bulge -> 0.95f to 0f
        deep -> 0.42f to 0f
        else -> 0.6f to 0f
    }

    val halfWidth = when {
        wide -> w * 0.3f
        close -> w * 0.2f
        else -> w * 0.24f
    }
    val left = w * 0.5f - halfWidth
    val right = w * 0.5f + halfWidth
    val halfHeight = h * 0.5f * openness
    val stroke = Stroke(width = strokeWidth * density)

    val path = Path().apply {
        moveTo(left, centerY)
        quadraticTo(w * 0.5f - halfWidth * 0.5f, centerY - halfHeight, w * 0.5f, centerY - halfHeight * 0.9f)
        quadraticTo(w * 0.5f + halfWidth * 0.5f, centerY - halfHeight, right, centerY)
        quadraticTo(w * 0.5f + halfWidth * 0.5f, centerY + halfHeight, w * 0.5f, centerY + halfHeight * 0.9f)
        quadraticTo(w * 0.5f - halfWidth * 0.5f, centerY + halfHeight, left, centerY)
        close()
    }
    rotate(degrees = tilt, pivot = Offset(w * 0.5f, centerY)) {
        drawPath(path, color = color, style = stroke)
        val irisRadius = when {
            bigIris -> halfHeight * 0.85f
            smallIris -> halfHeight * 0.4f
            else -> halfHeight * 0.62f
        }.coerceAtLeast(h * 0.07f)
        drawCircle(color, radius = irisRadius, center = Offset(w * 0.5f, centerY), style = stroke)
        drawCircle(color, radius = irisRadius * 0.34f, center = Offset(w * 0.5f, centerY))

        if (deep) {
            drawPath(
                Path().apply {
                    moveTo(left, centerY - halfHeight * 0.9f)
                    lineTo(left + w * 0.03f, centerY - halfHeight * 1.25f)
                    moveTo(right, centerY - halfHeight * 0.9f)
                    lineTo(right - w * 0.03f, centerY - halfHeight * 1.25f)
                },
                color = color.copy(alpha = 0.7f),
                style = Stroke(width = strokeWidth * 0.7f * density),
            )
        }
    }
}

private fun DrawScope.drawEyelids(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    val centerY = h * 0.56f
    val path = Path().apply {
        moveTo(w * 0.06f, centerY)
        quadraticTo(w * 0.5f, centerY - h * 0.44f, w * 0.94f, centerY)
        quadraticTo(w * 0.5f, centerY + h * 0.26f, w * 0.06f, centerY)
    }
    drawPath(path, color = color, style = stroke)
    val lid = when (variant % 3) {
        0 -> Path().apply {
            moveTo(w * 0.08f, centerY)
            quadraticTo(w * 0.5f, centerY - h * 0.22f, w * 0.92f, centerY)
        }
        1 -> Path().apply {
            moveTo(w * 0.08f, centerY)
            quadraticTo(w * 0.5f, centerY - h * 0.36f, w * 0.92f, centerY)
        }
        else -> Path().apply {
            moveTo(w * 0.08f, centerY)
            quadraticTo(w * 0.5f, centerY - h * 0.42f, w * 0.92f, centerY)
        }
    }
    drawPath(lid, color = color.copy(alpha = 0.9f), style = stroke)
    drawCircle(color, radius = h * 0.1f, center = Offset(w * 0.5f, centerY))
}

private fun DrawScope.drawLashes(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    val centerY = h * 0.62f
    val path = Path().apply {
        moveTo(w * 0.06f, centerY)
        quadraticTo(w * 0.5f, centerY - h * 0.36f, w * 0.94f, centerY)
    }
    drawPath(path, color = color, style = stroke)
    val dense = variant % 2 == 0
    val count = if (dense) 9 else 4
    val length = if (dense) h * 0.18f else h * 0.09f
    for (index in 0..count) {
        val t = index.toFloat() / count
        val x = w * (0.1f + 0.8f * t)
        val arc = 1f - (2f * t - 1f) * (2f * t - 1f)
        val baseY = centerY - h * 0.36f * arc * 0.9f
        drawLine(
            color = color,
            start = Offset(x, baseY),
            end = Offset(x - w * 0.02f, baseY - length),
            strokeWidth = if (dense) stroke.width * 1.1f else stroke.width * 0.8f,
        )
    }
}

private fun DrawScope.drawBags(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    val centerY = h * 0.4f
    drawPath(
        Path().apply {
            moveTo(w * 0.1f, centerY)
            quadraticTo(w * 0.5f, centerY - h * 0.3f, w * 0.9f, centerY)
        },
        color = color,
        style = stroke,
    )
    drawCircle(color, radius = h * 0.09f, center = Offset(w * 0.5f, centerY + h * 0.05f))
    val heavy = variant % 2 != 0
    val bag = Path().apply {
        moveTo(w * 0.12f, centerY + h * 0.1f)
        quadraticTo(w * 0.5f, centerY + h * (if (heavy) 0.46f else 0.28f), w * 0.88f, centerY + h * 0.1f)
    }
    drawPath(bag, color = color, style = stroke)
    if (heavy) {
        drawPath(
            Path().apply {
                moveTo(w * 0.16f, centerY + h * 0.2f)
                quadraticTo(w * 0.5f, centerY + h * 0.3f, w * 0.84f, centerY + h * 0.18f)
            },
            color = color.copy(alpha = 0.6f),
            style = Stroke(width = stroke.width * 0.7f),
        )
    }
}

private fun DrawScope.drawNose(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val bridge = when {
        label.contains("Высокий и широкий") -> 0.20f
        label.contains("Высокий") || label.contains("Большой") -> 0.15f
        label.contains("Без хребта") -> 0.03f
        label.contains("Утолщение") -> 0.08f
        else -> 0.09f
    }
    val tipY = when {
        label.contains("Длинный") -> 0.82f
        label.contains("Короткий") -> 0.62f
        else -> 0.72f
    }
    val tipX = 0.56f
    val hump = label.contains("горбинк")

    val back = Path().apply {
        moveTo(w * 0.44f, h * 0.12f)
        if (hump) {
            cubicTo(
                w * (0.44f - bridge * 0.5f), h * 0.34f,
                w * (tipX + bridge * 0.8f), h * 0.44f,
                w * tipX, h * (tipY - 0.12f),
            )
        } else {
            cubicTo(
                w * (0.44f - bridge * 0.3f), h * 0.38f,
                w * (tipX - bridge * 0.4f), h * 0.52f,
                w * tipX, h * tipY,
            )
        }
    }
    drawPath(back, color = color, style = stroke)

    val wing = when {
        label.contains("Очень маленькие") -> 0.05f
        label.contains("Огромные") || label.contains("Очень большие") -> 0.12f
        label.contains("Длинные и узкие") -> 0.075f
        else -> 0.085f
    }
    drawPath(
        Path().apply {
            moveTo(w * (tipX - wing * 0.6f), h * (tipY - 0.03f))
            quadraticTo(w * (tipX - wing), h * (tipY + 0.02f), w * (tipX + wing), h * (tipY + 0.02f))
            quadraticTo(w * (tipX + wing * 1.6f), h * (tipY - 0.04f), w * (tipX + wing * 1.4f), h * (tipY - 0.1f))
        },
        color = color,
        style = stroke,
    )
    drawLine(
        color = color.copy(alpha = 0.6f),
        start = Offset(w * tipX, h * (tipY + 0.02f)),
        end = Offset(w * tipX, h * (tipY + 0.1f)),
        strokeWidth = stroke.width * 0.8f,
    )
    if (label.contains("Пригнут книзу")) {
        drawLine(
            color = color,
            start = Offset(w * (tipX - 0.04f), h * (tipY + 0.03f)),
            end = Offset(w * (tipX + 0.05f), h * (tipY + 0.12f)),
            strokeWidth = stroke.width,
        )
    }
    if (label.contains("Задран")) {
        drawLine(
            color = color,
            start = Offset(w * (tipX - 0.04f), h * (tipY - 0.01f)),
            end = Offset(w * (tipX + 0.05f), h * (tipY - 0.11f)),
            strokeWidth = stroke.width,
        )
    }
    if (label.contains("Раздвоенный")) {
        drawLine(color, Offset(w * (tipX - 0.03f), h * (tipY - 0.02f)), Offset(w * (tipX - 0.03f), h * (tipY + 0.05f)), stroke.width)
        drawLine(color, Offset(w * (tipX + 0.03f), h * (tipY - 0.02f)), Offset(w * (tipX + 0.03f), h * (tipY + 0.05f)), stroke.width)
    }
    if (label.contains("Складка на крыле") || label.contains("Желоб")) {
        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(w * (tipX - wing * 1.1f), h * (tipY - 0.02f)),
            end = Offset(w * (tipX - wing * 1.4f), h * (tipY + 0.08f)),
            strokeWidth = stroke.width * 0.8f,
        )
    }
}

private fun DrawScope.drawEars(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val big = label.contains("Большие")
    val vertical = if (big) 0.78f else 0.56f
    val width = if (big) 0.44f else 0.32f
    val centerX = w * 0.5f
    val centerY = h * 0.5f
    val top = centerY - h * vertical / 2f
    val bottom = centerY + h * vertical / 2f

    val outline = Path().apply {
        moveTo(centerX, top)
        cubicTo(centerX + w * width * 0.7f, top, centerX + w * width * 0.8f, centerY, centerX + w * width * 0.5f, bottom - h * 0.06f)
        cubicTo(centerX + w * width * 0.3f, bottom + h * 0.02f, centerX - w * 0.02f, bottom, centerX, bottom)
        cubicTo(centerX - w * 0.1f, centerY, centerX - w * 0.06f, top + h * 0.1f, centerX, top)
    }
    drawPath(outline, color = color, style = stroke)

    val inner = Path().apply {
        moveTo(centerX + w * 0.02f, top + h * 0.08f)
        cubicTo(centerX + w * width * 0.4f, top + h * 0.06f, centerX + w * width * 0.45f, centerY + h * 0.04f, centerX + w * 0.02f, bottom - h * 0.08f)
    }
    drawPath(inner, color = color.copy(alpha = 0.75f), style = Stroke(width = stroke.width * 0.8f))

    if (label.contains("Оттопыренные")) {
        drawLine(color.copy(alpha = 0.5f), Offset(w * 0.06f, h * 0.2f), Offset(w * 0.06f, h * 0.8f), stroke.width * 0.7f)
    }
    if (label.contains("Прижаты")) {
        drawLine(color.copy(alpha = 0.5f), Offset(w * 0.94f, h * 0.2f), Offset(w * 0.94f, h * 0.8f), stroke.width * 0.7f)
    }
    if (label.contains("угол наклона")) {
        drawLine(color.copy(alpha = 0.6f), Offset(centerX, top - h * 0.05f), Offset(centerX + w * 0.16f, bottom - h * 0.1f), stroke.width * 0.7f)
    }
    if (label.contains("дипломата")) {
        drawLine(color, Offset(centerX + w * 0.1f, top + h * 0.02f), Offset(centerX + w * 0.1f, top + h * 0.1f), stroke.width)
        drawLine(color, Offset(centerX + w * 0.12f, bottom - h * 0.04f), Offset(centerX + w * 0.2f, bottom - h * 0.02f), stroke.width)
    }
}

private fun DrawScope.drawCheeks(
    w: Float, h: Float, variant: Int, color: Color, stroke: Stroke, thin: Stroke,
) {
    outline(w, h, color, stroke, alpha = 0.25f)
    val y = h * 0.56f
    when (variant % 6) {
        0 -> {
            drawLine(color, Offset(w * 0.1f, y - h * 0.08f), Offset(w * 0.26f, y + h * 0.06f), stroke.width * 1.4f)
            drawLine(color, Offset(w * 0.9f, y - h * 0.08f), Offset(w * 0.74f, y + h * 0.06f), stroke.width * 1.4f)
        }
        1 -> {
            drawCircle(color.copy(alpha = 0.35f), w * 0.1f, Offset(w * 0.22f, y))
            drawCircle(color.copy(alpha = 0.35f), w * 0.1f, Offset(w * 0.78f, y))
        }
        2 -> {
            drawLine(color, Offset(w * 0.26f, y - h * 0.1f), Offset(w * 0.2f, y + h * 0.12f), stroke.width)
            drawLine(color, Offset(w * 0.74f, y - h * 0.1f), Offset(w * 0.8f, y + h * 0.12f), stroke.width)
        }
        3 -> {
            drawLine(color, Offset(w * 0.08f, y), Offset(w * 0.3f, y + h * 0.02f), stroke.width)
            drawLine(color, Offset(w * 0.92f, y), Offset(w * 0.7f, y + h * 0.02f), stroke.width)
        }
        4 -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.2f, y - h * 0.1f)
                    quadraticTo(w * 0.16f, y + h * 0.1f, w * 0.3f, y + h * 0.1f)
                    moveTo(w * 0.8f, y - h * 0.1f)
                    quadraticTo(w * 0.84f, y + h * 0.1f, w * 0.7f, y + h * 0.1f)
                },
                color = color,
                style = stroke,
            )
        }
        else -> {
            drawArc(color, 200f, 140f, false, Offset(w * 0.14f, y - h * 0.12f), Size(w * 0.2f, h * 0.24f), style = stroke)
            drawArc(color, 200f, 140f, false, Offset(w * 0.66f, y - h * 0.12f), Size(w * 0.2f, h * 0.24f), style = stroke)
        }
    }
    drawLine(color.copy(alpha = 0.4f), Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.84f), thin.width)
}

private fun DrawScope.drawMouth(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val y = h * 0.5f
    val up = label.contains("вверх") || label.contains("Естественная")
    val down = label.contains("вниз")
    val wide = label.contains("Большой")
    val halfWidth = if (wide) w * 0.34f else w * 0.2f
    val curve = when {
        up -> -h * 0.12f
        down -> h * 0.12f
        else -> 0f
    }
    drawPath(
        Path().apply {
            moveTo(w * 0.5f - halfWidth, y)
            quadraticTo(w * 0.5f, y + curve, w * 0.5f + halfWidth, y)
        },
        color = color,
        style = stroke,
    )
    if (label.contains("десны")) {
        drawPath(
            Path().apply {
                moveTo(w * 0.5f - halfWidth * 0.7f, y - h * 0.02f)
                quadraticTo(w * 0.5f, y - h * 0.2f, w * 0.5f + halfWidth * 0.7f, y - h * 0.02f)
            },
            color = color.copy(alpha = 0.6f),
            style = stroke,
        )
    }
    if (label.contains("Натянутые")) {
        drawLine(color.copy(alpha = 0.6f), Offset(w * 0.5f - halfWidth, y), Offset(w * 0.5f - halfWidth, y - h * 0.06f), stroke.width * 0.7f)
        drawLine(color.copy(alpha = 0.6f), Offset(w * 0.5f + halfWidth, y), Offset(w * 0.5f + halfWidth, y - h * 0.06f), stroke.width * 0.7f)
    }
    if (label.contains("Кривая")) {
        drawLine(color.copy(alpha = 0.8f), Offset(w * 0.5f - halfWidth, y - h * 0.04f), Offset(w * 0.5f + halfWidth, y + h * 0.03f), stroke.width * 0.8f)
    }
    if (label.contains("Сомкнутые")) {
        drawLine(color.copy(alpha = 0.6f), Offset(w * 0.5f - halfWidth * 0.5f, y), Offset(w * 0.5f + halfWidth * 0.5f, y), stroke.width * 0.8f)
    }
}

private fun DrawScope.drawLips(w: Float, h: Float, variant: Int, label: String, color: Color, stroke: Stroke) {
    val y = h * 0.52f
    val upper = when {
        label.contains("Полные") -> 0.2f
        label.contains("Лук") -> 0.16f
        label.contains("Тонкие") -> 0.05f
        else -> 0.13f
    }
    val lower = when {
        label.contains("Полные") -> 0.2f
        label.contains("Тонкие") -> 0.06f
        label.contains("Полная нижняя") -> 0.24f
        label.contains("Полная верхняя") -> 0.1f
        else -> 0.14f
    }
    drawPath(
        Path().apply {
            moveTo(w * 0.2f, y)
            cubicTo(w * 0.34f, y - h * upper, w * 0.44f, y - h * upper * 0.2f, w * 0.5f, y - h * upper * 0.35f)
            cubicTo(w * 0.56f, y - h * upper * 0.2f, w * 0.66f, y - h * upper, w * 0.8f, y)
        },
        color = color,
        style = stroke,
    )
    drawPath(
        Path().apply {
            moveTo(w * 0.2f, y)
            cubicTo(w * 0.34f, y + h * lower, w * 0.66f, y + h * lower, w * 0.8f, y)
        },
        color = color,
        style = stroke,
    )
    if (label.contains("Лук")) {
        drawPath(
            Path().apply {
                moveTo(w * 0.4f, y - h * 0.02f)
                quadraticTo(w * 0.5f, y + h * 0.05f, w * 0.6f, y - h * 0.02f)
            },
            color = color.copy(alpha = 0.7f),
            style = stroke,
        )
    }
}

private fun DrawScope.drawTeeth(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val y = h * 0.34f
    val width = w * 0.56f
    val left = w * 0.5f - width / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(left, y),
        size = Size(width, h * 0.34f),
        cornerRadius = CornerRadius(5f, 5f),
        style = stroke,
    )
    val gap = label.contains("Щель")
    val bigFront = label.contains("Крупные")
    val crooked = label.contains("Кривые")
    val count = 4
    for (index in 1 until count) {
        val x = left + width * index / count
        if (gap && index == 2) continue
        drawLine(color.copy(alpha = 0.6f), Offset(x, y), Offset(x, y + h * 0.34f), stroke.width * 0.6f)
    }
    if (gap) {
        drawLine(color, Offset(w * 0.5f - w * 0.02f, y), Offset(w * 0.5f - w * 0.02f, y + h * 0.34f), stroke.width * 2.2f)
        drawLine(color, Offset(w * 0.5f + w * 0.02f, y), Offset(w * 0.5f + w * 0.02f, y + h * 0.34f), stroke.width * 2.2f)
    }
    if (bigFront) {
        drawRect(color.copy(alpha = 0.18f), Offset(w * 0.5f - width / 4f, y), Size(width / 2f, h * 0.34f))
    }
    if (crooked) {
        drawLine(color, Offset(left + width * 0.25f, y), Offset(left + width * 0.2f, y + h * 0.34f), stroke.width)
        drawLine(color, Offset(left + width * 0.75f, y), Offset(left + width * 0.8f, y + h * 0.34f), stroke.width)
    }
    if (label.contains("Выступающие")) {
        drawLine(color, Offset(left, y + h * 0.02f), Offset(left - w * 0.06f, y + h * 0.1f), stroke.width)
    }
}

private fun DrawScope.drawJaw(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val wide = label.contains("Большая") || label.contains("Двойной")
    val half = if (wide) 0.3f else 0.16f
    drawPath(
        Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            lineTo(w * (0.5f - half), h * 0.4f)
            cubicTo(w * (0.5f - half), h * 0.84f, w * 0.34f, h * 0.9f, w * 0.5f, h * 0.92f)
            cubicTo(w * 0.66f, h * 0.9f, w * (0.5f + half), h * 0.84f, w * (0.5f + half), h * 0.4f)
            close()
        },
        color = color.copy(alpha = 0.1f),
    )
    drawPath(
        Path().apply {
            moveTo(w * (0.5f - half), h * 0.36f)
            cubicTo(w * (0.5f - half), h * 0.84f, w * 0.34f, h * 0.9f, w * 0.5f, h * 0.92f)
            cubicTo(w * 0.66f, h * 0.9f, w * (0.5f + half), h * 0.84f, w * (0.5f + half), h * 0.36f)
        },
        color = color,
        style = stroke,
    )
    if (label.contains("Двойной")) {
        drawArc(color.copy(alpha = 0.7f), 200f, 140f, false, Offset(w * 0.3f, h * 0.78f), Size(w * 0.4f, h * 0.2f), style = stroke)
    }
    if (label.contains("Желваки")) {
        drawCircle(color.copy(alpha = 0.5f), w * 0.06f, Offset(w * 0.22f, h * 0.62f))
        drawCircle(color.copy(alpha = 0.5f), w * 0.06f, Offset(w * 0.78f, h * 0.62f))
    }
}

private fun DrawScope.drawChin(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val normalized = label.lowercase()
    val width = when {
        normalized.contains("очень широкий") -> 0.62f
        normalized.contains("широкий") -> 0.52f
        normalized.contains("маленький") -> 0.16f
        normalized.contains("прямоугольный") -> 0.34f
        normalized.contains("круглый") -> 0.3f
        normalized.contains("заострённый") -> 0.12f
        normalized.contains("длинный") -> 0.24f
        else -> 0.28f
    }
    val pointed = normalized.contains("заострённый")
    val top = h * 0.18f
    val bottom = if (normalized.contains("маленький")) h * 0.62f else h * 0.9f
    val side = w * (0.5f - width / 2f)
    val sideRight = w * (0.5f + width / 2f)
    val shoulder = bottom - (bottom - top) * 0.45f

    val outline = Path().apply {
        moveTo(side, top)
        lineTo(side, shoulder)
        if (pointed) {
            quadraticTo(w * 0.5f, bottom + h * 0.02f, sideRight, shoulder)
        } else {
            cubicTo(side, bottom, w * (0.5f - width * 0.28f), bottom, w * 0.5f, bottom)
            cubicTo(w * (0.5f + width * 0.28f), bottom, sideRight, bottom, sideRight, shoulder)
        }
        lineTo(sideRight, top)
    }
    drawPath(outline, color = color.copy(alpha = 0.08f))
    drawPath(outline, color = color, style = stroke)

    if (normalized.contains("срезанный")) {
        drawLine(color, Offset(side, bottom - h * 0.04f), Offset(sideRight, bottom - h * 0.14f), stroke.width * 1.4f)
    }
    if (normalized.contains("круглый")) {
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(side, bottom - h * 0.2f),
            size = Size(w * width, h * 0.26f),
            style = stroke,
        )
    }
    if (normalized.contains("прямоугольный")) {
        drawLine(color, Offset(side, bottom), Offset(sideRight, bottom), stroke.width * 1.2f)
    }
    if (normalized.contains("выдаётся вперёд")) {
        drawLine(color.copy(alpha = 0.7f), Offset(sideRight, bottom - h * 0.16f), Offset(sideRight + w * 0.14f, bottom - h * 0.08f), stroke.width)
    }
    if (normalized.contains("мощный")) {
        drawLine(color.copy(alpha = 0.7f), Offset(side - w * 0.05f, bottom - h * 0.14f), Offset(side - w * 0.12f, bottom - h * 0.02f), stroke.width)
        drawLine(color.copy(alpha = 0.7f), Offset(sideRight + w * 0.05f, bottom - h * 0.14f), Offset(sideRight + w * 0.12f, bottom - h * 0.02f), stroke.width)
    }
}

private fun DrawScope.drawChinBrows(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    val chin = label.substringBefore(" + ")
    val brows = label.substringAfter(" + ")
    val y = h * 0.3f
    val path = Path().apply {
        moveTo(w * 0.12f, y)
        when {
            brows.contains("изогнут") -> quadraticTo(w * 0.28f, y - h * 0.12f, w * 0.44f, y)
            brows.contains("ломан") -> {
                lineTo(w * 0.28f, y - h * 0.1f)
                lineTo(w * 0.44f, y + h * 0.02f)
            }
            else -> quadraticTo(w * 0.28f, y - h * 0.03f, w * 0.44f, y)
        }
    }
    drawPath(path, color = color, style = stroke)
    drawPath(
        Path().apply {
            moveTo(w * 0.56f, y)
            when {
                brows.contains("изогнут") -> quadraticTo(w * 0.72f, y - h * 0.12f, w * 0.88f, y)
                brows.contains("ломан") -> {
                    lineTo(w * 0.72f, y - h * 0.1f)
                    lineTo(w * 0.88f, y + h * 0.02f)
                }
                else -> quadraticTo(w * 0.72f, y - h * 0.03f, w * 0.88f, y)
            }
        },
        color = color,
        style = stroke,
    )

    val width = when {
        chin.contains("Прямоугольный") -> 0.4f
        chin.contains("Заострённый") -> 0.16f
        else -> 0.32f
    }
    drawPath(
        Path().apply {
            moveTo(w * (0.5f - width / 2f), h * 0.5f)
            lineTo(w * (0.5f - width / 2f), h * 0.78f)
            cubicTo(
                w * (0.5f - width / 2f), h * 0.94f,
                w * (0.5f + width / 2f), h * 0.94f,
                w * (0.5f + width / 2f), h * 0.78f,
            )
            lineTo(w * (0.5f + width / 2f), h * 0.5f)
        },
        color = color,
        style = stroke,
    )
    drawLine(color.copy(alpha = 0.4f), Offset(w * 0.3f, h * 0.48f), Offset(w * 0.7f, h * 0.48f), stroke.width * 0.6f)
}

private fun DrawScope.drawDimples(w: Float, h: Float, label: String, color: Color, stroke: Stroke) {
    outline(w, h, color, stroke, alpha = 0.28f)
    when {
        label.contains("Раздвоенный") -> {
            drawLine(color, Offset(w * 0.5f, h * 0.66f), Offset(w * 0.5f, h * 0.8f), stroke.width)
            drawCircle(color.copy(alpha = 0.6f), w * 0.025f, Offset(w * 0.47f, h * 0.73f))
            drawCircle(color.copy(alpha = 0.6f), w * 0.025f, Offset(w * 0.53f, h * 0.73f))
        }
        label.contains("кончике носа") -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.44f, h * 0.34f)
                    lineTo(w * 0.5f, h * 0.52f)
                    lineTo(w * 0.56f, h * 0.34f)
                },
                color = color,
                style = stroke,
            )
            drawCircle(color, w * 0.03f, Offset(w * 0.5f, h * 0.54f), style = stroke)
        }
        label.contains("щеке") -> {
            drawCircle(color, w * 0.035f, Offset(w * 0.28f, h * 0.56f), style = stroke)
            drawCircle(color, w * 0.035f, Offset(w * 0.72f, h * 0.56f), style = stroke)
        }
        else -> {
            drawCircle(color, w * 0.04f, Offset(w * 0.5f, h * 0.74f), style = stroke)
        }
    }
}

private fun DrawScope.drawWrinkles(
    w: Float, h: Float, label: String, color: Color, stroke: Stroke, thin: Stroke,
) {
    outline(w, h, color, stroke, alpha = 0.22f)
    val mid = h * 0.4f
    when {
        label.contains("сосредоточенности") || label.contains("перфекциониста") || label.contains("мудреца") -> {
            drawLine(color, Offset(w * 0.46f, mid), Offset(w * 0.46f, mid + h * 0.16f), thin.width)
            drawLine(color, Offset(w * 0.54f, mid), Offset(w * 0.54f, mid + h * 0.16f), thin.width)
            if (label.contains("перфекциониста")) {
                drawLine(color, Offset(w * 0.42f, mid), Offset(w * 0.42f, mid + h * 0.14f), thin.width)
                drawLine(color, Offset(w * 0.58f, mid), Offset(w * 0.58f, mid + h * 0.14f), thin.width)
            }
            if (label.contains("мудреца")) {
                drawLine(color, Offset(w * 0.46f, mid + h * 0.16f), Offset(w * 0.54f, mid + h * 0.16f), thin.width)
            }
        }
        label.contains("умственного развития") -> {
            repeat(3) { index ->
                val y = h * (0.26f + index * 0.07f)
                drawLine(color, Offset(w * 0.2f, y), Offset(w * 0.8f, y), thin.width)
            }
        }
        label.contains("умственного напряжения") -> {
            drawLine(color, Offset(w * 0.3f, h * 0.24f), Offset(w * 0.22f, h * 0.42f), thin.width)
            drawLine(color, Offset(w * 0.7f, h * 0.24f), Offset(w * 0.78f, h * 0.42f), thin.width)
        }
        label.contains("целеустремлённости") -> {
            drawLine(color, Offset(w * 0.5f, h * 0.3f), Offset(w * 0.5f, h * 0.58f), stroke.width * 1.6f)
        }
        label.contains("ответственности") -> {
            repeat(3) { index ->
                drawLine(color, Offset(w * (0.44f + index * 0.06f), h * 0.42f), Offset(w * (0.44f + index * 0.06f), h * 0.5f), thin.width)
            }
        }
        label.contains("истощения") -> {
            drawLine(color, Offset(w * 0.4f, h * 0.5f), Offset(w * 0.6f, h * 0.5f), thin.width)
        }
        label.contains("кругозора") -> {
            drawLine(color, Offset(w * 0.38f, h * 0.44f), Offset(w * 0.2f, h * 0.4f), thin.width)
            drawLine(color, Offset(w * 0.38f, h * 0.52f), Offset(w * 0.2f, h * 0.54f), thin.width)
            drawLine(color, Offset(w * 0.62f, h * 0.44f), Offset(w * 0.8f, h * 0.4f), thin.width)
            drawLine(color, Offset(w * 0.62f, h * 0.52f), Offset(w * 0.8f, h * 0.54f), thin.width)
        }
        label.contains("мужества") -> {
            drawLine(color, Offset(w * 0.28f, h * 0.52f), Offset(w * 0.22f, h * 0.7f), thin.width)
            drawLine(color, Offset(w * 0.72f, h * 0.52f), Offset(w * 0.78f, h * 0.7f), thin.width)
        }
        label.contains("балагура") -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.28f, h * 0.66f)
                    quadraticTo(w * 0.4f, h * 0.72f, w * 0.5f, h * 0.86f)
                    quadraticTo(w * 0.6f, h * 0.72f, w * 0.72f, h * 0.66f)
                },
                color = color,
                style = thin,
            )
        }
        label.contains("разочарования") -> {
            drawLine(color, Offset(w * 0.42f, h * 0.52f), Offset(w * 0.36f, h * 0.72f), thin.width)
            drawLine(color, Offset(w * 0.58f, h * 0.52f), Offset(w * 0.64f, h * 0.72f), thin.width)
        }
        label.contains("скорби") -> {
            drawLine(color, Offset(w * 0.36f, h * 0.72f), Offset(w * 0.46f, h * 0.88f), thin.width)
            drawLine(color, Offset(w * 0.64f, h * 0.72f), Offset(w * 0.54f, h * 0.88f), thin.width)
        }
        label.contains("принуждённой") -> {
            drawLine(color, Offset(w * 0.42f, h * 0.66f), Offset(w * 0.58f, h * 0.66f), thin.width)
        }
        label.contains("весельчака") -> {
            drawLine(color, Offset(w * 0.4f, h * 0.42f), Offset(w * 0.36f, h * 0.34f), thin.width)
            drawLine(color, Offset(w * 0.6f, h * 0.42f), Offset(w * 0.64f, h * 0.34f), thin.width)
        }
        label.contains("поддержки") -> {
            drawCircle(color, w * 0.03f, Offset(w * 0.3f, h * 0.58f), style = thin)
            drawCircle(color, w * 0.03f, Offset(w * 0.7f, h * 0.58f), style = thin)
        }
        label.contains("либидо") -> {
            drawLine(color, Offset(w * 0.45f, h * 0.62f), Offset(w * 0.45f, h * 0.74f), thin.width)
            drawLine(color, Offset(w * 0.55f, h * 0.62f), Offset(w * 0.55f, h * 0.74f), thin.width)
        }
        label.contains("катастрофу") -> {
            drawLine(color, Offset(w * 0.44f, h * 0.62f), Offset(w * 0.44f, h * 0.72f), thin.width)
            drawLine(color, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.74f), thin.width)
            drawLine(color, Offset(w * 0.56f, h * 0.62f), Offset(w * 0.56f, h * 0.72f), thin.width)
        }
        label.contains("желанности") -> {
            drawArc(color, 200f, 140f, false, Offset(w * 0.38f, h * 0.66f), Size(w * 0.24f, h * 0.14f), style = thin)
        }
        label.contains("сердца") -> {
            drawLine(color, Offset(w * 0.24f, h * 0.6f), Offset(w * 0.24f, h * 0.7f), thin.width)
            drawLine(color, Offset(w * 0.76f, h * 0.6f), Offset(w * 0.76f, h * 0.7f), thin.width)
        }
        label.contains("тайны") -> {
            drawCircle(color, w * 0.025f, Offset(w * 0.4f, h * 0.6f), style = thin)
            drawCircle(color, w * 0.025f, Offset(w * 0.6f, h * 0.6f), style = thin)
        }
        else -> {
            drawPath(
                Path().apply {
                    moveTo(w * 0.4f, h * 0.42f)
                    lineTo(w * 0.4f, h * 0.66f)
                    cubicTo(w * 0.4f, h * 0.82f, w * 0.6f, h * 0.82f, w * 0.6f, h * 0.66f)
                    lineTo(w * 0.6f, h * 0.42f)
                },
                color = color,
                style = stroke,
            )
        }
    }
}

private fun DrawScope.drawBeard(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    val path = when (variant % 3) {
        0 -> Path().apply {
            moveTo(w * 0.18f, h * 0.36f)
            cubicTo(w * 0.18f, h * 0.94f, w * 0.82f, h * 0.94f, w * 0.82f, h * 0.36f)
            cubicTo(w * 0.72f, h * 0.58f, w * 0.28f, h * 0.58f, w * 0.18f, h * 0.36f)
        }
        1 -> Path().apply {
            moveTo(w * 0.24f, h * 0.34f)
            lineTo(w * 0.24f, h * 0.8f)
            quadraticTo(w * 0.5f, h * 0.94f, w * 0.76f, h * 0.8f)
            lineTo(w * 0.76f, h * 0.34f)
            quadraticTo(w * 0.5f, h * 0.52f, w * 0.24f, h * 0.34f)
        }
        else -> Path().apply {
            moveTo(w * 0.24f, h * 0.38f)
            quadraticTo(w * 0.32f, h * 0.9f, w * 0.5f, h * 0.96f)
            quadraticTo(w * 0.68f, h * 0.9f, w * 0.76f, h * 0.38f)
        }
    }
    drawPath(path, color = color.copy(alpha = 0.12f))
    drawPath(path, color = color, style = stroke)
    repeat(4) { index ->
        val x = w * (0.3f + index * 0.13f)
        drawLine(color.copy(alpha = 0.4f), Offset(x, h * 0.6f), Offset(x, h * 0.8f), stroke.width * 0.6f)
    }
}

private fun DrawScope.drawFaceShape(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    val path = Path()
    when (variant % 3) {
        0 -> {
            path.moveTo(w * 0.5f, h * 0.06f)
            path.lineTo(w * 0.68f, h * 0.5f)
            path.lineTo(w * 0.5f, h * 0.94f)
            path.lineTo(w * 0.32f, h * 0.5f)
            path.close()
        }
        1 -> {
            path.moveTo(w * 0.5f, h * 0.06f)
            path.lineTo(w * 0.76f, h * 0.3f)
            path.lineTo(w * 0.82f, h * 0.82f)
            path.lineTo(w * 0.5f, h * 0.94f)
            path.lineTo(w * 0.18f, h * 0.82f)
            path.lineTo(w * 0.24f, h * 0.3f)
            path.close()
        }
        else -> {
            path.moveTo(w * 0.5f, h * 0.06f)
            path.lineTo(w * 0.8f, h * 0.22f)
            path.lineTo(w * 0.84f, h * 0.7f)
            path.lineTo(w * 0.5f, h * 0.94f)
            path.lineTo(w * 0.16f, h * 0.7f)
            path.lineTo(w * 0.2f, h * 0.22f)
            path.close()
        }
    }
    drawPath(path, color = color.copy(alpha = 0.1f))
    drawPath(path, color = color, style = stroke)
}

private fun DrawScope.drawFaceType(
    w: Float, h: Float, variant: Int, color: Color, stroke: Stroke, thin: Stroke,
) {
    outline(w, h, color, stroke, alpha = 0.3f)
    when (variant % 3) {
        0 -> drawRect(color.copy(alpha = 0.22f), Offset(w * 0.26f, h * 0.14f), Size(w * 0.48f, h * 0.26f))
        1 -> drawRect(color.copy(alpha = 0.22f), Offset(w * 0.22f, h * 0.6f), Size(w * 0.56f, h * 0.26f))
        else -> drawCircle(color.copy(alpha = 0.22f), w * 0.16f, Offset(w * 0.5f, h * 0.58f))
    }
    drawLine(color.copy(alpha = 0.5f), Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.88f), thin.width)
}

private fun DrawScope.drawZones(w: Float, h: Float, variant: Int, color: Color, thin: Stroke) {
    outline(w, h, color, thin, alpha = 0.3f)
    val wide = when (variant % 6) {
        0, 3 -> 0
        1, 4 -> 1
        2, 5 -> 2
        else -> -1
    }
    val small = variant % 6 >= 3
    val bands = listOf(0.12f to 0.38f, 0.38f to 0.66f, 0.66f to 0.9f)
    bands.forEachIndexed { index, (top, bottom) ->
        val height = if (small) {
            if (index == wide) (bottom - top) * 0.45f else (bottom - top) * 1.05f
        } else {
            if (index == wide) (bottom - top) * 1.3f else (bottom - top) * 0.85f
        }
        val alpha = if (index == wide) 0.28f else 0.07f
        val center = (top + bottom) / 2f
        val start = (center - height / 2f).coerceIn(0.06f, 0.94f)
        val end = (center + height / 2f).coerceIn(0.08f, 0.96f)
        val inset = 0.3f - height * 0.15f
        drawRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(w * inset, h * start),
            size = Size(w * (1f - inset * 2f), h * (end - start)),
        )
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(w * inset, h * start),
            end = Offset(w * (1f - inset), h * start),
            strokeWidth = thin.width,
        )
    }
}

private fun DrawScope.drawProfile(
    w: Float, h: Float, label: String, color: Color, stroke: Stroke, thin: Stroke,
) {
    val normalized = label.lowercase()
    val convex = normalized.contains("выпукл")
    val strong = normalized.contains("сильно")
    val balanced = normalized.contains("сбалансированный")
    val noseOut = when {
        balanced -> 0.14f
        strong && convex -> 0.3f
        convex -> 0.22f
        strong -> 0.02f
        else -> 0.08f
    }
    val browOut = when {
        balanced -> 0.32f
        convex -> 0.36f
        else -> 0.24f
    }
    val chinOut = if (convex) 0.34f else 0.42f

    val face = Path().apply {
        moveTo(w * 0.32f, h * 0.08f)
        cubicTo(
            w * (browOut - 0.06f), h * 0.12f,
            w * browOut, h * 0.24f,
            w * (browOut - 0.02f), h * 0.34f,
        )
        cubicTo(
            w * (browOut - 0.08f), h * 0.44f,
            w * (0.36f + noseOut * 0.5f), h * 0.5f,
            w * (0.4f + noseOut), h * 0.56f,
        )
        lineTo(w * 0.35f, h * 0.62f)
        lineTo(w * 0.37f, h * 0.68f)
        cubicTo(
            w * 0.33f, h * 0.74f,
            w * (chinOut - 0.04f), h * 0.78f,
            w * chinOut, h * 0.86f,
        )
        lineTo(w * 0.62f, h * 0.94f)
        lineTo(w * 0.68f, h * 0.94f)
    }
    drawPath(face, color = color, style = stroke)

    val skull = Path().apply {
        moveTo(w * 0.32f, h * 0.08f)
        cubicTo(
            w * 0.62f, h * 0.02f,
            w * 0.82f, h * 0.22f,
            w * 0.78f, h * 0.46f,
        )
        cubicTo(
            w * 0.76f, h * 0.66f,
            w * 0.72f, h * 0.82f,
            w * 0.68f, h * 0.94f,
        )
    }
    drawPath(skull, color = color.copy(alpha = 0.55f), style = thin)

    drawLine(
        color = color.copy(alpha = 0.25f),
        start = Offset(w * 0.32f, h * 0.08f),
        end = Offset(w * 0.62f, h * 0.94f),
        strokeWidth = thin.width,
    )
}

private fun DrawScope.drawHeadShape(w: Float, h: Float, variant: Int, color: Color, stroke: Stroke) {
    when (variant % 4) {
        0 -> drawPath(
            Path().apply {
                moveTo(w * 0.5f, h * 0.04f)
                cubicTo(w * 0.84f, h * 0.18f, w * 0.84f, h * 0.6f, w * 0.62f, h * 0.94f)
                lineTo(w * 0.38f, h * 0.94f)
                cubicTo(w * 0.16f, h * 0.6f, w * 0.16f, h * 0.18f, w * 0.5f, h * 0.04f)
            },
            color = color,
            style = stroke,
        )
        1 -> drawPath(
            Path().apply {
                moveTo(w * 0.5f, h * 0.18f)
                cubicTo(w * 0.86f, h * 0.26f, w * 0.86f, h * 0.62f, w * 0.62f, h * 0.94f)
                lineTo(w * 0.38f, h * 0.94f)
                cubicTo(w * 0.14f, h * 0.62f, w * 0.14f, h * 0.26f, w * 0.5f, h * 0.18f)
            },
            color = color,
            style = stroke,
        )
        2 -> drawOval(color, Offset(w * 0.2f, h * 0.04f), Size(w * 0.6f, h * 0.92f), style = stroke)
        else -> drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.12f),
            size = Size(w * 0.6f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
            style = stroke,
        )
    }
    if (variant % 4 <= 1) {
        drawLine(color.copy(alpha = 0.4f), Offset(w * 0.24f, h * 0.3f), Offset(w * 0.76f, h * 0.3f), stroke.width * 0.6f)
    }
}
