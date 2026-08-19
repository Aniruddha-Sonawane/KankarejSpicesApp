package com.kankarej.kankarejspices.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit
) {

    val context =
        LocalContext.current

    val configuration by
        OfflineNotificationManager
            .configuration
            .collectAsState()

    var refreshKey = configuration

    LaunchedEffect(Unit) {
        OfflineNotificationManager
            .syncFromFirebase(context)
    }

    val notifications =
        configuration.notifications
            .sortedBy { it.id }

    val schedule =
        configuration.schedule

    val scheduleTimes =
        listOf(
            1 to ("Morning" to schedule.morning),
            2 to ("Afternoon" to schedule.afternoon),
            3 to ("Evening" to schedule.evening)
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Scheduled Notifications",
                fontWeight = FontWeight.Bold
            )
        },
        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {

                Text(
                    "Fetched from Firebase",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color = Color.Gray
                )

                Spacer(
                    Modifier.height(12.dp)
                )

                scheduleTimes.forEach { item ->

                    val notification =
                        notifications
                            .firstOrNull {
                                it.id == item.first
                            }

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 6.dp
                                )
                    ) {

                        Text(
                            item.second.first,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            "Time: ${item.second.second}",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                            fontWeight =
                                FontWeight.Medium
                        )

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        if (
                            notification == null ||
                            notification.title.isBlank()
                        ) {

                            Text(
                                "No notification configured.",
                                color = Color.Gray
                            )

                        } else {

                            Text(
                                notification.title,
                                fontWeight =
                                    FontWeight.Medium
                            )

                            Text(
                                notification.message,
                                color = Color.Gray
                            )

                            Text(
                                if (
                                    notification.enabled
                                ) {
                                    "Enabled"
                                } else {
                                    "Disabled"
                                },
                                color =
                                    if (
                                        notification.enabled
                                    ) {
                                        Color(0xFF4CAF50)
                                    } else {
                                        Color.Gray
                                    },
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }
                    }

                    if (
                        item.first != 3
                    ) {
                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 6.dp
                                ),
                            color =
                                Color.LightGray
                                    .copy(
                                        alpha = 0.3f
                                    )
                        )
                    }
                }

                Spacer(
                    Modifier.height(12.dp)
                )

                val exactAllowed =
                    OfflineNotificationManager
                        .isExactAlarmAllowed(
                            context
                        )

                Text(
                    if (exactAllowed) {
                        "Precise scheduling: Enabled"
                    } else {
                        "Precise scheduling: Not enabled"
                    },
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        if (exactAllowed) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFE67E22)
                        }
                )

                if (!exactAllowed) {

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        "Android may delay notifications without exact-alarm access.",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color = Color.Gray
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {
                            OfflineNotificationManager
                                .openExactAlarmSettings(
                                    context
                                )
                        }
                    ) {
                        Text(
                            "Enable Precise Scheduling"
                        )
                    }
                }
            }
        },
        confirmButton = {

            Row(
                horizontalArrangement =
                    Arrangement.End
            ) {

                Button(
                    onClick = {
                        OfflineNotificationManager
                            .syncFromFirebase(
                                context
                            )
                    }
                ) {
                    Text("Refresh")
                }

                Spacer(
                    Modifier.padding(
                        horizontal = 4.dp
                    )
                )

                Button(
                    onClick = onDismiss
                ) {
                    Text("Close")
                }
            }
        }
    )
}
