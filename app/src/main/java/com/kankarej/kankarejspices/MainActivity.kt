package com.kankarej.kankarejspices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kankarej.kankarejspices.navigation.RootNav
import com.kankarej.kankarejspices.ui.AppSystemBars
import com.kankarej.kankarejspices.ui.theme.KankarejSpicesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen (safe even if you later remove it)
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            KankarejSpicesTheme(
                darkTheme = darkTheme
            ) {
                AppSystemBars()     // Status bar styling

                RootNav(
                    onToggleTheme = {
                        darkTheme = !darkTheme
                    }
                )
            }
        }
    }
}
