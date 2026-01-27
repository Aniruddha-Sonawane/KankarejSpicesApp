package com.kankarej.kankarejspices

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kankarej.kankarejspices.navigation.RootNav
import com.kankarej.kankarejspices.ui.AppSystemBars
import com.kankarej.kankarejspices.ui.theme.KankarejSpicesTheme

private const val PREFS_NAME = "app_prefs"
private const val KEY_DARK_THEME = "dark_theme"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // ---- Synchronous theme load (NO flicker) ----
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // If key does not exist yet → follow system theme (first install)
        val systemDark = isSystemInDarkThemeCompat()
        val initialDarkTheme = prefs.getBoolean(KEY_DARK_THEME, systemDark)

        setContent {

            var darkTheme by remember { mutableStateOf(initialDarkTheme) }

            KankarejSpicesTheme(
                darkTheme = darkTheme
            ) {
                AppSystemBars()

                RootNav(
                    onToggleTheme = {
                        val newValue = !darkTheme
                        darkTheme = newValue

                        // Persist synchronously
                        prefs.edit()
                            .putBoolean(KEY_DARK_THEME, newValue)
                            .apply()
                    }
                )
            }
        }
    }

    /**
     * We cannot call isSystemInDarkTheme() outside Compose,
     * so this helper mirrors it using system configuration.
     */
    private fun isSystemInDarkThemeCompat(): Boolean {
        val uiMode = resources.configuration.uiMode
        val nightMask = uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
