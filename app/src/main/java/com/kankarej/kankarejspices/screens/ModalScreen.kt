package com.kankarej.kankarejspices.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.imageLoader
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.AppSettings
import com.kankarej.kankarejspices.notifications.NotificationSettingsDialog
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {

    val context = LocalContext.current
    val repo = remember {
        ProductRepository()
    }

    val appSettings by repo
        .getAppSettingsFlow()
        .collectAsState(
            initial = AppSettings()
        )

    var showNotificationDialog by
        remember {
            mutableStateOf(false)
        }

    fun openUrl(
        url: String,
        unavailableMessage: String
    ) {

        val cleanUrl =
            url.trim()

        if (cleanUrl.isBlank()) {
            Toast.makeText(
                context,
                unavailableMessage,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {

            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(cleanUrl)
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                context,
                "Couldn't open the link.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun shareApp() {

        val appLink =
            appSettings.appLink.trim()

        if (appLink.isBlank()) {

            Toast.makeText(
                context,
                "App sharing link is not configured yet.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val sendIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Kankarej Spices"
                )

                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out Kankarej Spices!\n\n$appLink"
                )
            }

        try {

            context.startActivity(
                Intent.createChooser(
                    sendIntent,
                    "Share Kankarej Spices"
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                context,
                "Couldn't open the share menu.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surface,
                            titleContentColor =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
            )
        },

        containerColor =
            MaterialTheme
                .colorScheme
                .background

    ) { padding ->

        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(16.dp)
        ) {

            SettingsSectionTitle(
                "Appearance"
            )

            SettingsCard {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleTheme()
                            }
                            .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.DarkMode,
                        null,
                        tint =
                            KankarejGreen,
                        modifier =
                            Modifier.size(24.dp)
                    )

                    Spacer(
                        Modifier.width(16.dp)
                    )

                    Column(
                        Modifier.weight(1f)
                    ) {

                        Text(
                            "Dark Mode",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            fontWeight =
                                FontWeight.Medium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )

                        Text(
                            if (isDarkTheme)
                                "On"
                            else
                                "Off",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked =
                            isDarkTheme,
                        onCheckedChange = {
                            onToggleTheme()
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor =
                                    Color.White,
                                checkedTrackColor =
                                    KankarejGreen
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(24.dp)
            )

            SettingsSectionTitle(
                "General"
            )

            SettingsCard {

                SettingsItem(
                    Icons.Default.Language,
                    "Language",
                    "English (Default)"
                ) {
                    Toast.makeText(
                        context,
                        "More languages coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                HorizontalDivider(
                    color =
                        Color.LightGray.copy(
                            alpha = 0.3f
                        )
                )

                SettingsItem(
                    Icons.Default.Notifications,
                    "Scheduled Notifications",
                    "View fetched notifications and schedule times"
                ) {
                    showNotificationDialog =
                        true
                }
            }

            Spacer(
                Modifier.height(24.dp)
            )

            SettingsSectionTitle(
                "Support"
            )

            SettingsCard {

                SettingsItem(
                    Icons.Default.Share,
                    "Share App",
                    "Tell your friends about us"
                ) {
                    shareApp()
                }

                HorizontalDivider(
                    color =
                        Color.LightGray.copy(
                            alpha = 0.3f
                        )
                )

                SettingsItem(
                    Icons.Default.Star,
                    "Rate Us",
                    "Rate us on Play Store"
                ) {
                    openUrl(
                        appSettings.rateUsLink,
                        "Rate Us link is not configured yet."
                    )
                }
            }

            Spacer(
                Modifier.height(24.dp)
            )

            SettingsSectionTitle(
                "Data"
            )

            SettingsCard {

                SettingsItem(
                    Icons.Default.CleaningServices,
                    "Clear Image Cache",
                    "Free up space"
                ) {

                    context
                        .imageLoader
                        .memoryCache
                        ?.clear()

                    context
                        .imageLoader
                        .diskCache
                        ?.clear()

                    Toast.makeText(
                        context,
                        "Cache Cleared!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                HorizontalDivider(
                    color =
                        Color.LightGray.copy(
                            alpha = 0.3f
                        )
                )

                SettingsItem(
                    Icons.Default.PrivacyTip,
                    "Privacy Policy"
                ) {
                    openUrl(
                        appSettings.privacyPolicyLink,
                        "Privacy Policy link is not configured yet."
                    )
                }
            }

            Spacer(
                Modifier.height(32.dp)
            )

            Box(
                Modifier.fillMaxWidth(),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    "Version 1.0.0",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    color = Color.Gray
                )
            }

            Spacer(
                Modifier.height(20.dp)
            )
        }
    }

    if (showNotificationDialog) {

        NotificationSettingsDialog(
            onDismiss = {
                showNotificationDialog =
                    false
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(
    title: String
) {

    Text(
        text = title,
        style =
            MaterialTheme
                .typography
                .titleSmall,
        color =
            KankarejGreen,
        fontWeight =
            FontWeight.Bold,
        modifier =
            Modifier.padding(
                bottom = 8.dp,
                start = 4.dp
            )
    )
}

@Composable
fun SettingsCard(
    content:
        @Composable ColumnScope.() -> Unit
) {

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults
                .cardElevation(2.dp),
        shape =
            RoundedCornerShape(12.dp),
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            content = content
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .padding(16.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            icon,
            null,
            tint = Color.Gray,
            modifier =
                Modifier.size(24.dp)
        )

        Spacer(
            Modifier.width(16.dp)
        )

        Column(
            Modifier.weight(1f)
        ) {

            Text(
                title,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                fontWeight =
                    FontWeight.Medium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            if (subtitle != null) {

                Text(
                    subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color = Color.Gray
                )
            }
        }

        if (showArrow) {

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                null,
                tint = Color.LightGray,
                modifier =
                    Modifier.size(16.dp)
            )
        }
    }
}
