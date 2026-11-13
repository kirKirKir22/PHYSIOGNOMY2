package com.kirill.physiognomy2.ui.workspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlin.math.abs

@Composable
fun PhotoStage(
    photos: List<PhotoItem>,
    currentIndex: Int,
    effectsEnabled: Boolean,
    photosRemaining: Int,
    onPickPhotos: (List<android.net.Uri>) -> Unit,
    onSelectPhoto: (Int) -> Unit,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onCommitEdits: () -> Unit,
    onToggleGrayscale: () -> Unit,
    onToggleMirror: () -> Unit,
    onResetPhoto: () -> Unit,
    onDeletePhoto: () -> Unit,
    onToggleEffects: () -> Unit,
    onLimitReached: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photo = photos.getOrNull(currentIndex)
    var fullscreen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_PICK_COUNT),
    ) { uris -> onPickPhotos(uris) }

    val canPick = photosRemaining > 0
    val launchPicker = {
        if (canPick) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            onLimitReached()
        }
    }

    val activePath = photo?.path
    val scaleState = remember(activePath) { mutableFloatStateOf(1f) }
    val rotationState = remember(activePath) { mutableFloatStateOf(0f) }
    val offsetState = remember(activePath) { mutableStateOf(Offset.Zero) }
    val zoomed = scaleState.value > 1.01f

    val resetView = {
        scaleState.value = 1f
        rotationState.value = 0f
        offsetState.value = Offset.Zero
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0B0B10))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    RoundedCornerShape(22.dp),
                ),
        ) {
            if (photo == null) {
                EmptyPhotoState(onPick = { launchPicker() })
            } else {
                PhotoCanvas(
                    photo = photo,
                    effectsEnabled = effectsEnabled,
                    photosCount = photos.size,
                    currentIndex = currentIndex,
                    onSelectPhoto = onSelectPhoto,
                    scaleState = scaleState,
                    rotationState = rotationState,
                    offsetState = offsetState,
                    modifier = Modifier.fillMaxSize(),
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.55f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = "${currentIndex + 1} / ${photos.size}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }

                RoundIconButton(
                    icon = Icons.Filled.DeleteOutline,
                    description = "Удалить фото",
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RoundIconButton(
                        icon = if (effectsEnabled) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        description = if (effectsEnabled) "Показать оригинал" else "Показать эффекты",
                        active = effectsEnabled,
                        onClick = onToggleEffects,
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EditorTool(
                        icon = if (zoomed) Icons.Filled.ZoomOut else Icons.Filled.ZoomIn,
                        label = if (zoomed) "Уменьшить" else "Увеличить",
                        active = zoomed,
                        onClick = {
                            if (zoomed) {
                                resetView()
                            } else {
                                scaleState.value = 2f
                            }
                        },
                    )
                    EditorTool(
                        icon = Icons.Filled.FilterBAndW,
                        label = "Ч/Б",
                        active = photo.grayscale,
                        onClick = onToggleGrayscale,
                    )
                    EditorTool(
                        icon = Icons.Filled.Flip,
                        label = "Зеркало",
                        active = photo.mirrored,
                        onClick = onToggleMirror,
                    )
                    EditorTool(
                        icon = Icons.Filled.RestartAlt,
                        label = "Сброс",
                        onClick = {
                            resetView()
                            onResetPhoto()
                        },
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .padding(end = 8.dp, top = 56.dp, bottom = 62.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    VerticalSlider(
                        icon = Icons.Filled.BrightnessMedium,
                        label = "Яркость",
                        value = photo.brightness,
                        range = -1f..1f,
                        onValueChange = onBrightness,
                        onValueChangeFinished = onCommitEdits,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalSlider(
                        icon = Icons.Filled.Contrast,
                        label = "Контраст",
                        value = photo.contrast,
                        range = 0.4f..2.2f,
                        onValueChange = onContrast,
                        onValueChangeFinished = onCommitEdits,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EditorBarButton(
                        icon = Icons.Filled.AddPhotoAlternate,
                        label = if (photosRemaining > 0) "Загрузить фото" else "Лимит",
                        onClick = { launchPicker() },
                    )
                    EditorBarButton(
                        icon = Icons.Filled.Fullscreen,
                        label = "На весь экран",
                        onClick = { fullscreen = true },
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить фото?") },
            text = { Text("Снимок будет удалён с устройства без возможности восстановления.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeletePhoto()
                    },
                ) { Text("Удалить") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (fullscreen && photo != null) {
        Dialog(
            onDismissRequest = { fullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                PhotoCanvas(
                    photo = photo,
                    effectsEnabled = effectsEnabled,
                    photosCount = photos.size,
                    currentIndex = currentIndex,
                    onSelectPhoto = onSelectPhoto,
                    scaleState = scaleState,
                    rotationState = rotationState,
                    offsetState = offsetState,
                    modifier = Modifier.fillMaxSize(),
                )
                RoundIconButton(
                    icon = Icons.Filled.FullscreenExit,
                    description = "Свернуть",
                    onClick = { fullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun EditorTool(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = {
            if (enabled) onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (active) MaterialTheme.colorScheme.primary
        else Color.Black.copy(alpha = 0.45f),
        contentColor = when {
            !enabled -> Color.White.copy(alpha = 0.35f)
            active -> MaterialTheme.colorScheme.onPrimary
            else -> Color.White.copy(alpha = 0.85f)
        },
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary
            else Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier
                .width(78.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun VerticalSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val trackColor = Color.White.copy(alpha = 0.18f)
    val fillColor = MaterialTheme.colorScheme.primary
    val start = range.start
    val end = range.endInclusive
    val changeValue by rememberUpdatedState(onValueChange)
    val changeFinished by rememberUpdatedState(onValueChangeFinished)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.55f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .width(52.dp)
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(start, end) {
                        fun update(y: Float) {
                            val h = size.height.toFloat()
                            if (h <= 0f) return
                            val f = 1f - (y.coerceIn(0f, h) / h)
                            changeValue(start + f * (end - start))
                        }
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            update(down.position.y)
                            down.consume()
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.pressed } ?: break
                                update(change.position.y)
                                change.consume()
                            }
                            changeFinished()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val x = size.width / 2f
                    val top = 7.dp.toPx()
                    val bottom = size.height - 7.dp.toPx()
                    drawLine(
                        color = trackColor,
                        start = Offset(x, top),
                        end = Offset(x, bottom),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    val knobY = bottom - fraction * (bottom - top)
                    drawLine(
                        color = fillColor,
                        start = Offset(x, knobY),
                        end = Offset(x, bottom),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, knobY))
                }
            }
        }
    }
}

@Composable
private fun EditorBarButton(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = {
            if (enabled) onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        else Color.Black.copy(alpha = 0.5f),
        contentColor = when {
            !enabled -> Color.White.copy(alpha = 0.35f)
            active -> Color(0xFFB9A9FF)
            else -> Color.White.copy(alpha = 0.9f)
        },
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else Color.White.copy(alpha = 0.12f),
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary
        else Color.Black.copy(alpha = 0.5f),
        contentColor = if (active) MaterialTheme.colorScheme.onPrimary else Color.White,
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary
            else Color.White.copy(alpha = 0.12f),
        ),
        modifier = modifier,
    ) {
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun PhotoCanvas(
    photo: PhotoItem,
    effectsEnabled: Boolean,
    photosCount: Int,
    currentIndex: Int,
    onSelectPhoto: (Int) -> Unit,
    scaleState: MutableFloatState,
    rotationState: MutableFloatState,
    offsetState: MutableState<Offset>,
    modifier: Modifier = Modifier,
) {
    val scale by scaleState
    val rotation by rotationState
    val offset by offsetState
    var lastTapAt by remember(photo.path) { mutableLongStateOf(0L) }
    val swipeThreshold = with(LocalDensity.current) { 26.dp.toPx() }

    val toggleZoom = {
        if (scaleState.floatValue > 1.01f) {
            scaleState.floatValue = 1f
            offsetState.value = Offset.Zero
            rotationState.floatValue = 0f
        } else {
            scaleState.floatValue = 2f
        }
    }

    val handleTap = {
        val now = System.currentTimeMillis()
        if (now - lastTapAt <= DOUBLE_TAP_MS) {
            lastTapAt = 0L
            toggleZoom()
        } else {
            lastTapAt = now
        }
    }

    val colorFilter = remember(photo, effectsEnabled) {
        if (!effectsEnabled) null else photo.toColorFilter()
    }

    Box(
        modifier = modifier
            .pointerInput(photo.path, photosCount) {
                var lastSwipeAt = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var zoomed = scaleState.floatValue > 1.01f
                    var swiped = false
                    var accumulatedX = 0f
                    var tapped = true

                    do {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        val pressedCount = changes.count { it.pressed }
                        val zoom = event.calculateZoom()
                        val rotationDelta = event.calculateRotation()
                        val pan = event.calculatePan()

                        if (pressedCount >= 2) {
                            tapped = false
                            zoomed = true
                            scaleState.floatValue = (scaleState.floatValue * zoom).coerceIn(1f, 5f)
                            rotationState.floatValue += rotationDelta
                            offsetState.value += pan
                            changes.forEach { if (it.pressed) it.consume() }
                        } else if (pressedCount == 1) {
                            val change = changes.first { it.pressed }
                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                tapped = false
                                if (zoomed || scaleState.floatValue > 1.01f) {
                                    offsetState.value += delta
                                    change.consume()
                                } else {
                                    accumulatedX += delta.x
                                    if (abs(accumulatedX) > swipeThreshold && !swiped) {
                                        swiped = true
                                        val now = System.currentTimeMillis()
                                        if (now - lastSwipeAt > 180) {
                                            lastSwipeAt = now
                                            val target = if (accumulatedX > 0) currentIndex - 1 else currentIndex + 1
                                            if (target in 0 until photosCount) onSelectPhoto(target)
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (tapped) {
                        handleTap()
                    }

                    if (scaleState.floatValue <= 1.01f) {
                        offsetState.value = Offset.Zero
                        rotationState.floatValue = 0f
                    }
                }
            },
    ) {
        AsyncImage(
            model = photo.path,
            contentDescription = "Фото лица",
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale * if (photo.mirrored && effectsEnabled) -1f else 1f,
                    scaleY = scale,
                    rotationZ = rotation,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )

        AxisLine(
            color = Color(0xFFB9A9FF).copy(alpha = 0.8f),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AxisLine(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val x = size.width / 2f
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
        )
    }
}

private fun PhotoItem.toColorFilter(): ColorFilter {    val brightness = this.brightness * 255f
    val contrast = this.contrast
    val shift = 128f * (1f - contrast) + brightness
    val values = if (grayscale) {
        val lr = 0.213f * contrast
        val lg = 0.715f * contrast
        val lb = 0.072f * contrast
        floatArrayOf(
            lr, lg, lb, 0f, shift,
            lr, lg, lb, 0f, shift,
            lr, lg, lb, 0f, shift,
            0f, 0f, 0f, 1f, 0f,
        )
    } else {
        floatArrayOf(
            contrast, 0f, 0f, 0f, shift,
            0f, contrast, 0f, 0f, shift,
            0f, 0f, contrast, 0f, shift,
            0f, 0f, 0f, 1f, 0f,
        )
    }
    return ColorFilter.colorMatrix(ColorMatrix(values))
}

private const val MAX_PICK_COUNT = 10
private const val DOUBLE_TAP_MS = 280L

@Composable
private fun EmptyPhotoState(onPick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(52.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Добавьте фото лица",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.92f),
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "До 10 фото, портрет строго анфас.\nЛицо должно быть хорошо освещено.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            onClick = onPick,
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.14f),
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Загрузить фото", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
