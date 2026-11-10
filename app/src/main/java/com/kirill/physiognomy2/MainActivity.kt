package com.kirill.physiognomy2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kirill.physiognomy2.ui.AppRoot
import com.kirill.physiognomy2.ui.theme.PhysiognomyTheme
import com.kirill.physiognomy2.ui.theme.rememberThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeController = rememberThemeController()
            PhysiognomyTheme(mode = themeController.mode) {
                AppRoot(themeController = themeController)
            }
        }
    }
}
