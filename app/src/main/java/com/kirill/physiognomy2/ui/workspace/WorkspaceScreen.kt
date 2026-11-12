package com.kirill.physiognomy2.ui.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkspaceScreen(
    onOpenChecklist: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenResult: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkspaceViewModel = viewModel(),
) {
    val step = viewModel.step
    val questionIndex = viewModel.questionIndex
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.photoNotice) {
        val notice = viewModel.photoNotice ?: return@LaunchedEffect
        snackbar.showSnackbar(notice)
        viewModel.consumePhotoNotice()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        val photoHeight = if (viewModel.currentPhoto == null) {
            (maxHeight * 0.3f).coerceIn(190.dp, 250.dp)
        } else {
            (maxHeight * 0.4f).coerceIn(240.dp, 400.dp)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            PhotoStage(
                photos = viewModel.photos,
                currentIndex = viewModel.currentPhotoIndex,
                effectsEnabled = viewModel.effectsEnabled,
                photosRemaining = viewModel.photosRemaining,
                onPickPhotos = viewModel::addPhotos,
                onSelectPhoto = viewModel::selectPhoto,
                onBrightness = viewModel::setBrightness,
                onContrast = viewModel::setContrast,
                onToggleGrayscale = viewModel::toggleGrayscale,
                onToggleMirror = viewModel::toggleMirror,
                onResetPhoto = viewModel::resetPhotoEffects,
                onDeletePhoto = viewModel::deleteCurrentPhoto,
                onToggleEffects = viewModel::toggleEffects,
                onLimitReached = {
                    viewModel.showPhotoNotice("Достигнут лимит: 10 фото. Удалите лишние, чтобы добавить новые.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoHeight),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    onClear = viewModel::clearOption,
                    onBack = viewModel::previousQuestion,
                    onNext = viewModel::nextQuestion,
                    onSkip = viewModel::skipQuestion,
                    onOpenChecklist = onOpenChecklist,
                    onOpenHelp = onOpenHelp,
                    onOpenResult = onOpenResult,
                )
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )
    }
}
