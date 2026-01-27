package com.kankarej.kankarejspices.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun AppSystemBars() {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    SideEffect {
        val context = view.context
        if (context is Activity) {
            val window = context.window

            // Status bar color
            window.statusBarColor = colorScheme.primary.toArgb()

            // Status bar icon color (true = dark icons)
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars =
                colorScheme.primary.luminance() > 0.5
        }
    }
}
