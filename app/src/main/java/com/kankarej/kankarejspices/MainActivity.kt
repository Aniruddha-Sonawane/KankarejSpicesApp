package com.kankarej.kankarejspices

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import com.kankarej.kankarejspices.data.PresenceManager
import com.kankarej.kankarejspices.navigation.RootNav
import com.kankarej.kankarejspices.notifications.OfflineNotificationManager
import com.kankarej.kankarejspices.ui.AppSystemBars
import com.kankarej.kankarejspices.ui.theme.KankarejSpicesTheme

private const val PREFS_NAME = "app_prefs"
private const val KEY_DARK_THEME = "dark_theme"
private const val REQUEST_NOTIFICATION_PERMISSION = 7001

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Refresh notification content whenever the app is opened.
        OfflineNotificationManager.syncFromFirebase(this)

        // Make sure the daily alarms exist.
        OfflineNotificationManager.scheduleAll(this)

        requestNotificationPermissionIfNeeded()

        // ---- Initialize Presence System (Live User Count) ----
        val presenceManager = PresenceManager()
        presenceManager.startTracking()

        // ---- Synchronous theme load (NO flicker) ----
        val prefs = getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val initialDarkTheme =
            prefs.getBoolean(
                KEY_DARK_THEME,
                false
            )

        setContent {

            var darkTheme by remember {
                mutableStateOf(initialDarkTheme)
            }

            KankarejSpicesTheme(
                darkTheme = darkTheme
            ) {

                AppSystemBars()

                RootNav(
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        val newValue = !darkTheme
                        darkTheme = newValue

                        prefs.edit()
                            .putBoolean(
                                KEY_DARK_THEME,
                                newValue
                            )
                            .apply()
                    }
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun isSystemInDarkThemeCompat(): Boolean {
        val uiMode =
            resources.configuration.uiMode

        val nightMask =
            uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        return nightMask ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
