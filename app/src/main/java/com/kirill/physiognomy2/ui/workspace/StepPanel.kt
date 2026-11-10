package com.kirill.physiognomy2.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirill.physiognomy2.data.AnswerOption
import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.data.Step
import com.kirill.physiognomy2.ui.components.EyeShapeArt

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
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenChecklist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = step.questions[questionIndex.coerceIn(0, step.questions.lastIndex)]
    val selectedId = answerFor(question.id)

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
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Подсказка",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
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
                onSelect = { option -> onSelect(question.id, option.id) },
            )

            Spacer(Modifier.height(14.dp))
            SelectedSummary(
                step = step,
                answerFor = answerFor,
                onOpenChecklist = onOpenChecklist,
            )
            Spacer(Modifier.height(16.dp))
        }

        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            ProgressBlock(
                answeredInStep = answeredInStep,
                questionsInStep = step.questions.size,
                totalAnswered = totalAnswered,
                totalQuestions = totalQuestions,
                onOpenChecklist = onOpenChecklist,
            )

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ActionButton(
                    label = "НАЗАД",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    filled = false,
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                )
                ActionButton(
                    label = "ДАЛЕЕ",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    iconAtEnd = true,
                    filled = true,
                    onClick = onNext,
                    modifier = Modifier.weight(1.25f),
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
                Text(
                    text = "${index + 1}. ${question.title}",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun OptionGrid(
    options: List<AnswerOption>,
    selectedId: String?,
    onSelect: (AnswerOption) -> Unit,
) {
    val visual = options.all { it.eyeShape != null } && options.size <= 6
    val longest = options.maxOfOrNull { it.label.length } ?: 0
    val columns = when {
        visual -> 3
        longest <= 22 -> 2
        else -> 1
    }
    val rows = options.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    OptionCard(
                        option = option,
                        selected = option.id == selectedId,
                        showArt = visual,
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
    selected: Boolean,
    showArt: Boolean,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(containerColor)
                .border(if (selected) 1.6.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = if (showArt) 12.dp else 9.dp),
            horizontalAlignment = if (showArt) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showArt && option.eyeShape != null) {
                EyeShapeArt(shape = option.eyeShape, color = contentColor)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = option.label,
                style = if (showArt) {
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
                textAlign = if (showArt) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start,
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
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
                val label = entry.second
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = "${index + 1}. ${entry.first.title}: $label",
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                modifier = Modifier.padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!iconAtEnd) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                if (iconAtEnd) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
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
                modifier = Modifier.padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
