package com.kirill.physiognomy2.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.kirill.physiognomy2.ui.help.HelpScreen
import com.kirill.physiognomy2.ui.navigation.AppDestination
import com.kirill.physiognomy2.ui.reference.ReferenceScreen
import com.kirill.physiognomy2.ui.result.ResultScreen
import com.kirill.physiognomy2.ui.settings.SettingsScreen
import com.kirill.physiognomy2.ui.theme.ThemeController
import com.kirill.physiognomy2.ui.workspace.ChecklistScreen
import com.kirill.physiognomy2.ui.workspace.ProfilesScreen
import com.kirill.physiognomy2.ui.workspace.WorkspaceScreen
import com.kirill.physiognomy2.ui.workspace.WorkspaceViewModel

private const val CHECKLIST_ROUTE = "checklist"
private const val RESULT_ROUTE = "result"
private const val HELP_ROUTE = "help"

@Composable
fun AppRoot(themeController: ThemeController) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = AppDestination.fromRoute(currentRoute)
    val isOverlay = currentRoute == CHECKLIST_ROUTE || currentRoute == RESULT_ROUTE || currentRoute == HELP_ROUTE

    val workspaceViewModel: WorkspaceViewModel = viewModel()

    val openDestination: (AppDestination) -> Unit = { destination ->
        if (destination.route != currentRoute) {
            navController.navigate(
                destination.route,
                navOptions {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                },
            )
        }
    }

    val openChecklist: () -> Unit = { navController.navigate(CHECKLIST_ROUTE) }
    val openResult: () -> Unit = { navController.navigate(RESULT_ROUTE) }
    val openHelp: () -> Unit = { navController.navigate(HELP_ROUTE) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                )
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                ) {
                    AppDestination.entries.forEach { destination ->
                        val selected = destination == currentDestination && !isOverlay
                        NavigationBarItem(
                            selected = selected,
                            onClick = { openDestination(destination) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Workspace.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars),
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable(AppDestination.Workspace.route) {
                WorkspaceScreen(
                    onOpenChecklist = openChecklist,
                    onOpenHelp = openHelp,
                    onOpenResult = openResult,
                    viewModel = workspaceViewModel,
                )
            }
            composable(AppDestination.Profiles.route) {
                ProfilesScreen(
                    profiles = workspaceViewModel.profiles,
                    answered = workspaceViewModel.answeredTotal,
                    total = workspaceViewModel.totalQuestions,
                    activeProfileId = workspaceViewModel.activeProfileId,
                    activeProfileName = workspaceViewModel.activeProfileName,
                    onSave = { name -> workspaceViewModel.saveCurrentProfile(name) },
                    onOpen = { profile ->
                        workspaceViewModel.loadProfile(profile)
                        openDestination(AppDestination.Workspace)
                    },
                    onDelete = workspaceViewModel::deleteProfile,
                    onRenameActive = workspaceViewModel::renameActiveProfile,
                    onStartNew = {
                        workspaceViewModel.startNewProfile()
                        workspaceViewModel.resetAnswers()
                        openDestination(AppDestination.Workspace)
                    },
                )
            }
            composable(AppDestination.Reference.route) {
                ReferenceScreen()
            }
            composable(AppDestination.Settings.route) {
                SettingsScreen(
                    themeController = themeController,
                    photosCount = workspaceViewModel.photos.size,
                    profilesCount = workspaceViewModel.profiles.size,
                    answered = workspaceViewModel.answeredTotal,
                    total = workspaceViewModel.totalQuestions,
                    onDeletePhotos = workspaceViewModel::deleteAllPhotos,
                    onResetAnswers = workspaceViewModel::resetAnswers,
                )
            }
            composable(CHECKLIST_ROUTE) {
                ChecklistScreen(
                    answers = workspaceViewModel.answers,
                    currentStep = workspaceViewModel.stepNumber,
                    onOpenStep = { step ->
                        workspaceViewModel.openStep(step)
                        navController.popBackStack()
                    },
                    onResetAnswers = workspaceViewModel::resetAnswers,
                    onBack = { navController.popBackStack() },
                    onOpenHelp = openHelp,
                    onOpenResult = openResult,
                )
            }
            composable(RESULT_ROUTE) {
                ResultScreen(
                    answers = workspaceViewModel.answers,
                    activeProfileName = workspaceViewModel.activeProfileName,
                    isSavedProfile = workspaceViewModel.activeProfileId != null,
                    onBack = { navController.popBackStack() },
                    onSaveProfile = { name -> workspaceViewModel.saveCurrentProfile(name) },
                )
            }
            composable(HELP_ROUTE) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
