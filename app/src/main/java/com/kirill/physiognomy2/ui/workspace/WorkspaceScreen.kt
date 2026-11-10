package com.kirill.physiognomy2.ui.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel = viewModel(),
    onOpenChecklist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = viewModel.step
    val questionIndex = viewModel.questionIndex

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        val photoHeight = (maxHeight * 0.44f).coerceIn(260.dp, 420.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))

            PhotoStage(
                photos = viewModel.photos,
                currentIndex = viewModel.currentPhotoIndex,
                effectsEnabled = viewModel.effectsEnabled,
                onPickPhotos = viewModel::addPhotos,
                onSelectPhoto = viewModel::selectPhoto,
                onBrightness = viewModel::setBrightness,
                onContrast = viewModel::setContrast,
                onToggleGrayscale = viewModel::toggleGrayscale,
                onToggleMirror = viewModel::toggleMirror,
                onResetPhoto = viewModel::resetPhotoEffects,
                onDeletePhoto = viewModel::deleteCurrentPhoto,
                onToggleEffects = viewModel::toggleEffects,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoHeight),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 0.dp),
            ) {
                StepPanel(
                    step = step,
                    questionIndex = questionIndex,
                    answeredInStep = viewModel.answeredInStep(),
                    totalAnswered = viewModel.answeredTotal,
                    totalQuestions = viewModel.totalQuestions,
                    answerFor = viewModel::answerFor,
                    onFocusQuestion = viewModel::focusQuestion,
                    onSelect = viewModel::selectOption,
                    onBack = viewModel::previousQuestion,
                    onNext = viewModel::nextQuestion,
                    onOpenChecklist = onOpenChecklist,
                )
            }
        }
    }
}
