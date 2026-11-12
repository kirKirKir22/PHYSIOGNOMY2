package com.kirill.physiognomy2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import com.kirill.physiognomy2.ui.AppRoot
import com.kirill.physiognomy2.ui.theme.PhysiognomyTheme
import com.kirill.physiognomy2.ui.theme.isDark
import com.kirill.physiognomy2.ui.theme.rememberThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeController = rememberThemeController()
            val dark = themeController.mode.isDark()

            DisposableEffect(dark) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
                onDispose { }
            }

            PhysiognomyTheme(mode = themeController.mode) {
                AppRoot(themeController = themeController)
            }
        }
    }
}
