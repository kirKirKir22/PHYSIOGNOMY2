package com.kirill.physiognomy2.ui.workspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlin.math.abs

@Composable
fun PhotoStage(
    photos: List<PhotoItem>,
    currentIndex: Int,
    effectsEnabled: Boolean,
    onPickPhotos: (List<android.net.Uri>) -> Unit,
    onSelectPhoto: (Int) -> Unit,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onToggleGrayscale: () -> Unit,
    onToggleMirror: () -> Unit,
    onResetPhoto: () -> Unit,
    onDeletePhoto: () -> Unit,
    onToggleEffects: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photo = photos.getOrNull(currentIndex)
    var fullscreen by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10),
    ) { uris -> onPickPhotos(uris) }

    val launchPicker = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0B0B10)),
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
                    onClick = onDeletePhoto,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                )

                RoundIconButton(
                    icon = if (fullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    description = "На весь экран",
                    onClick = { fullscreen = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )

                Surface(
                    onClick = onToggleEffects,
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = if (effectsEnabled) Color(0xFF9D8CFF) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (effectsEnabled) "Эффекты вкл." else "Оригинал",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        if (photo != null) {
            Spacer(Modifier.height(8.dp))
            PhotoToolsBar(
                photo = photo,
                onPick = { launchPicker() },
                onBrightness = onBrightness,
                onContrast = onContrast,
                onToggleGrayscale = onToggleGrayscale,
                onToggleMirror = onToggleMirror,
                onResetPhoto = onResetPhoto,
            )
        }
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
private fun PhotoToolsBar(
    photo: PhotoItem,
    onPick: () -> Unit,
    onBrightness: (Float) -> Unit,
    onContrast: (Float) -> Unit,
    onToggleGrayscale: () -> Unit,
    onToggleMirror: () -> Unit,
    onResetPhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ToolChip(Icons.Filled.AddPhotoAlternate, "Сменить", onClick = onPick)
            ToolChip(
                Icons.Filled.FilterBAndW,
                "Ч/Б",
                active = photo.grayscale,
                onClick = onToggleGrayscale,
            )
            ToolChip(
                Icons.Filled.Flip,
                "Зеркало",
                active = photo.mirrored,
                onClick = onToggleMirror,
            )
            ToolChip(Icons.Filled.RestartAlt, "Сброс", onClick = onResetPhoto)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactSlider(
                icon = Icons.Filled.BrightnessMedium,
                label = "Яркость",
                value = photo.brightness,
                range = -1f..1f,
                onValueChange = onBrightness,
                modifier = Modifier.weight(1f),
            )
            CompactSlider(
                icon = Icons.Filled.Contrast,
                label = "Контраст",
                value = photo.contrast,
                range = 0.4f..2.2f,
                onValueChange = onContrast,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ToolChip(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
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
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.5f),
        contentColor = Color.White,
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
    modifier: Modifier = Modifier,
) {
    var scale by remember(photo.path) { mutableFloatStateOf(1f) }
    var rotation by remember(photo.path) { mutableFloatStateOf(0f) }
    var offset by remember(photo.path) { mutableStateOf(Offset.Zero) }
    val swipeThreshold = with(LocalDensity.current) { 26.dp.toPx() }

    val colorFilter = remember(photo, effectsEnabled) {
        if (!effectsEnabled) null else photo.toColorFilter()
    }

    Box(
        modifier = modifier
            .pointerInput(photo.path, scale) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offset = Offset.Zero
                            rotation = 0f
                        } else {
                            scale = 2f
                        }
                    },
                )
            }
            .pointerInput(photosCount) {
                awaitEachGesture {
                    awaitPointerEvent()
                    var accumulatedSwipe = 0f
                    var multiTouch = false
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) multiTouch = true

                        if (multiTouch && scale > 1f) {
                            val zoom = event.calculateZoom()
                            val rotate = event.calculateRotation()
                            val pan = event.calculatePan()
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            rotation += rotate
                            offset += pan
                            event.changes.forEach { if (it.pressed) it.consume() }
                        } else if (multiTouch) {
                            val zoom = event.calculateZoom()
                            val rotate = event.calculateRotation()
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            rotation += rotate
                            event.changes.forEach { if (it.pressed) it.consume() }
                        } else if (pressedCount == 1) {
                            val change = event.changes.first { it.pressed }
                            val delta = change.positionChange()
                            if (scale > 1.01f) {
                                if (delta != Offset.Zero) {
                                    offset += delta
                                    change.consume()
                                }
                            } else {
                                accumulatedSwipe += delta.x
                                if (abs(accumulatedSwipe) > swipeThreshold) {
                                    val direction = if (accumulatedSwipe > 0) 1 else -1
                                    val target = currentIndex - direction
                                    if (target in 0 until photosCount) onSelectPhoto(target)
                                    accumulatedSwipe = 0f
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale <= 1.01f) {
                        offset = Offset.Zero
                        rotation = 0f
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
                    scaleX = scale * if (photo.mirrored) -1f else 1f,
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

private fun PhotoItem.toColorFilter(): ColorFilter {
    val brightness = this.brightness * 255f
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
