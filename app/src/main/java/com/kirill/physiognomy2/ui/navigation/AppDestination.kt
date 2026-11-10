package com.kirill.physiognomy2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    Workspace("workspace", "Чек-лист", Icons.AutoMirrored.Filled.Assignment),
    Profiles("profiles", "Профили", Icons.Filled.PersonOutline),
    Reference("reference", "Справка", Icons.AutoMirrored.Filled.MenuBook),
    Settings("settings", "Настройки", Icons.Filled.Settings);

    companion object {
        fun fromRoute(route: String?): AppDestination =
            entries.firstOrNull { it.route == route } ?: Workspace
    }
}
