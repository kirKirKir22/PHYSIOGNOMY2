package com.kirill.physiognomy2.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirill.physiognomy2.data.AnswerOption
import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.data.Step
import com.kirill.physiognomy2.data.TraitReading
import com.kirill.physiognomy2.ui.components.FeatureArt
import com.kirill.physiognomy2.ui.components.FeatureArtKind

@Composable
fun StepPanel(
    step: Step,
    questionIndex: Int,
    answeredInStep: Int,
    totalAnswered: Int,
    totalQuestions: Int,
    answerFor: (String) -> String?,
    onFocusQuestion: (Int) -> Unit,
    onSelect: (String, String) -> Unit,
    onClear: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onOpenChecklist: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = step.questions[questionIndex.coerceIn(0, step.questions.lastIndex)]
    val selectedId = answerFor(question.id)
    val selectedOption = question.options.firstOrNull { it.id == selectedId }
    val artKind = featureKindFor(step.number)
    val visualOptions = step.number == 3 && questionIndex == 0
    val isLastStep = step.number >= PhysiognomyCatalog.steps.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ШАГ ${step.number} ИЗ ${PhysiognomyCatalog.steps.size}",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onOpenHelp,
                    shape = CircleShape,
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Box(modifier = Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Как пользоваться",
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = question.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            ReadingHeader(stepNumber = step.number, questionId = question.id)

            if (step.questions.size > 1) {
                Spacer(Modifier.height(10.dp))
                QuestionChips(
                    step = step,
                    currentIndex = questionIndex,
                    answerFor = answerFor,
                    onFocusQuestion = onFocusQuestion,
                )
            }

            Spacer(Modifier.height(12.dp))
            OptionGrid(
                options = question.options,
                selectedId = selectedId,
                artKind = artKind,
                visual = visualOptions,
                onSelect = { option ->
                    if (option.id == selectedId) onClear(question.id)
                    else onSelect(question.id, option.id)
                },
            )

            AnimatedVisibility(
                visible = selectedOption != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    selectedOption?.let {
                        InterpretationCard(option = it, onClear = { onClear(question.id) })
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            SelectedSummary(
                step = step,
                answerFor = answerFor,
                onOpenChecklist = onOpenChecklist,
            )
            Spacer(Modifier.height(16.dp))

            if (isLastStep) {
                Surface(
                    onClick = onOpenResult,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("ГОТОВО", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp))
                            Text(
                                "Собрать итоговый портрет",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            ProgressBlock(
                answeredInStep = answeredInStep,
                questionsInStep = step.questions.size,
                totalAnswered = totalAnswered,
                totalQuestions = totalQuestions,
                onOpenChecklist = onOpenChecklist,
            )

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ActionButton(
                    label = "НАЗАД",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    filled = false,
                    onClick = onBack,
                    modifier = Modifier.weight(0.9f),
                )
                if (selectedId == null) {
                    ActionButton(
                        label = "ПРОПУСК",
                        icon = Icons.Filled.SkipNext,
                        filled = false,
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (isLastStep) {
                    ActionButton(
                        label = "ИТОГ",
                        icon = Icons.Filled.Check,
                        filled = true,
                        onClick = onOpenResult,
                        modifier = Modifier.weight(1.15f),
                    )
                } else {
                    ActionButton(
                        label = "ДАЛЕЕ",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        iconAtEnd = true,
                        filled = true,
                        onClick = onNext,
                        modifier = Modifier.weight(1.15f),
                    )
                }
            }
        }
    }
}

fun featureKindFor(stepNumber: Int): FeatureArtKind = when (stepNumber) {
    1 -> FeatureArtKind.Forehead
    2 -> FeatureArtKind.Brows
    3 -> FeatureArtKind.Eyes
    4 -> FeatureArtKind.Eyelids
    5 -> FeatureArtKind.Lashes
    6 -> FeatureArtKind.Bags
    7 -> FeatureArtKind.Nose
    8 -> FeatureArtKind.Ears
    9 -> FeatureArtKind.Cheeks
    10 -> FeatureArtKind.Mouth
    11 -> FeatureArtKind.Lips
    12 -> FeatureArtKind.Teeth
    13 -> FeatureArtKind.Smile
    14 -> FeatureArtKind.Jaw
    15 -> FeatureArtKind.Chin
    16 -> FeatureArtKind.ChinBrows
    17 -> FeatureArtKind.Dimples
    18 -> FeatureArtKind.Wrinkles
    19 -> FeatureArtKind.Beard
    20 -> FeatureArtKind.FaceShape
    21 -> FeatureArtKind.FaceType
    22 -> FeatureArtKind.FaceType
    23 -> FeatureArtKind.Zones
    24 -> FeatureArtKind.Profile
    25 -> FeatureArtKind.Profile
    else -> FeatureArtKind.HeadShape
}

@Composable
private fun ReadingHeader(stepNumber: Int, questionId: String) {
    val area = TraitReading.areaTitleFor(stepNumber)
    val reading = TraitReading.readingFor(stepNumber, questionId)

    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                text = reading,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (TraitReading.isUnverified(questionId)) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Дополнение приложения",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
        }
        if (area.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = area,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun QuestionChips(
    step: Step,
    currentIndex: Int,
    answerFor: (String) -> String?,
    onFocusQuestion: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        step.questions.forEachIndexed { index, question ->
            val selected = answerFor(question.id) != null
            val active = index == currentIndex
            val background by animateColorAsState(
                targetValue = when {
                    active -> MaterialTheme.colorScheme.primary
                    selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                label = "chip",
            )
            val contentColor = when {
                active -> MaterialTheme.colorScheme.onPrimary
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Surface(
                onClick = { onFocusQuestion(index) },
                shape = RoundedCornerShape(50),
                color = background,
                contentColor = contentColor,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}. ${question.title}",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                    if (selected && !active) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionGrid(
    options: List<AnswerOption>,
    selectedId: String?,
    artKind: FeatureArtKind,
    visual: Boolean,
    onSelect: (AnswerOption) -> Unit,
) {
    val columns = if (visual) 3 else 1
    val rows = options.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    OptionCard(
                        option = option,
                        optionIndex = options.indexOf(option),
                        selected = option.id == selectedId,
                        artKind = artKind,
                        visual = visual,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    option: AnswerOption,
    optionIndex: Int,
    selected: Boolean,
    artKind: FeatureArtKind,
    visual: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "optionBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else Color.Transparent,
        label = "optionContainer",
    )
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(containerColor)
                .border(if (selected) 1.6.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (visual) {
                FeatureArt(
                    kind = artKind,
                    variant = optionIndex,
                    label = option.label,
                    color = contentColor,
                    eyeShape = option.eyeShape,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    FeatureArt(
                        kind = artKind,
                        variant = optionIndex,
                        label = option.label,
                        color = if (selected) contentColor
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        eyeShape = option.eyeShape,
                        modifier = Modifier.size(width = 38.dp, height = 28.dp),
                        strokeWidth = 1.2f,
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Column(
                modifier = if (visual) Modifier.fillMaxWidth() else Modifier.weight(1f),
                horizontalAlignment = if (visual) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                if (visual) Spacer(Modifier.height(6.dp))
                Text(
                    text = option.label,
                    style = if (visual) {
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.1.sp,
                        )
                    } else {
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (visual) TextAlign.Center else TextAlign.Start,
                )
                if (!visual && selected) {
                    Text(
                        text = option.traits.firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }
}

@Composable
private fun InterpretationCard(option: AnswerOption, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ЧТО ЭТО ЗНАЧИТ",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Сбросить",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        if (option.traits.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                option.traits.forEach { trait ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = trait,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = option.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SelectedSummary(
    step: Step,
    answerFor: (String) -> String?,
    onOpenChecklist: () -> Unit,
) {
    val answered = step.questions.mapNotNull { question ->
        val optionId = answerFor(question.id) ?: return@mapNotNull null
        val label = PhysiognomyCatalog.optionLabel(question.id, optionId) ?: return@mapNotNull null
        question to label
    }
    if (answered.isEmpty()) return

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "УЖЕ ВЫБРАНО",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Показать все",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenChecklist)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            answered.forEachIndexed { index, entry ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = "${index + 1}. ${entry.first.title}: ${entry.second}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressBlock(
    answeredInStep: Int,
    questionsInStep: Int,
    totalAnswered: Int,
    totalQuestions: Int,
    onOpenChecklist: () -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenChecklist)
                .padding(vertical = 2.dp),
        ) {
            Text(
                text = "ПРОГРЕСС",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$totalAnswered / $totalQuestions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Открыть чек-лист",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(questionsInStep) { index ->
                val filled = index < answeredInStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconAtEnd: Boolean = false,
) {
    if (filled) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 13.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!iconAtEnd) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (iconAtEnd) {
                    Spacer(Modifier.width(5.dp))
                    Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                }
            }
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = modifier,
        ) {
            Row(
                modifier = Modifier.padding(vertical = 13.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
