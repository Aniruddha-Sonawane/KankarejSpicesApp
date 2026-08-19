package com.kankarej.kankarejspices.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

object OfflineNotificationManager {

    private const val CHANNEL_ID =
        "kankarej_spices_notifications"

    private const val CHANNEL_NAME =
        "Kankarej Spices"

    private const val CHANNEL_DESCRIPTION =
        "Kankarej Spices scheduled notifications"

    private const val MORNING_ID = 1
    private const val AFTERNOON_ID = 2
    private const val EVENING_ID = 3

    private const val REQUEST_MORNING = 1001
    private const val REQUEST_AFTERNOON = 1002
    private const val REQUEST_EVENING = 1003

    private const val FIREBASE_URL =
        "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val configurationState =
        MutableStateFlow(
            NotificationConfiguration()
        )

    val configuration: StateFlow<NotificationConfiguration> =
        configurationState

    private fun database() =
        FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .reference

    fun initialize(context: Context) {
        createNotificationChannel(context)

        // First schedule using the last known local buffer.
        // This makes the system work offline.
        loadCachedConfiguration(context)
        scheduleAll(context)

        // Then try to refresh Firebase.
        syncFromFirebase(context)
    }

    private fun loadCachedConfiguration(
        context: Context
    ) {
        configurationState.value =
            OfflineNotificationStore(context)
                .getConfiguration()
    }

    fun syncFromFirebase(
        context: Context
    ) {
        scope.launch {

            try {

                val root =
                    database().get().await()

                val notificationSnapshot =
                    root.child("notifications")

                val notifications =
                    mutableListOf<OfflineNotification>()

                notificationSnapshot.children.forEach { child ->

                    val id =
                        child.key
                            ?.toIntOrNull()
                            ?: return@forEach

                    if (id !in 1..3) {
                        return@forEach
                    }

                    val title =
                        child.child("title")
                            .getValue(String::class.java)
                            ?.trim()
                            .orEmpty()

                    val message =
                        child.child("message")
                            .getValue(String::class.java)
                            ?.trim()
                            .orEmpty()

                    val enabled =
                        when (
                            val value =
                                child.child("enabled").value
                        ) {
                            null -> true
                            is Boolean -> value
                            is String ->
                                value.toBooleanStrictOrNull()
                                    ?: true
                            else -> true
                        }

                    notifications.add(
                        OfflineNotification(
                            id = id,
                            title = title,
                            message = message,
                            enabled = enabled
                        )
                    )
                }

                val scheduleSnapshot =
                    root.child("notification_schedule")

                val schedule =
                    NotificationSchedule(
                        morning =
                            validTimeOrDefault(
                                scheduleSnapshot
                                    .child("morning")
                                    .getValue(String::class.java),
                                "08:30"
                            ),

                        afternoon =
                            validTimeOrDefault(
                                scheduleSnapshot
                                    .child("afternoon")
                                    .getValue(String::class.java),
                                "14:00"
                            ),

                        evening =
                            validTimeOrDefault(
                                scheduleSnapshot
                                    .child("evening")
                                    .getValue(String::class.java),
                                "20:00"
                            )
                    )

                val store =
                    OfflineNotificationStore(context)

                if (notifications.isNotEmpty()) {
                    store.saveAll(notifications)
                }

                store.saveSchedule(schedule)

                configurationState.value =
                    store.getConfiguration()

                // IMPORTANT:
                // Firebase changes are applied immediately to
                // the next scheduled alarm.
                scheduleAll(context)

            } catch (_: Exception) {

                // No internet / Firebase unavailable.
                //
                // Keep the existing local buffer and schedule.
                loadCachedConfiguration(context)
                scheduleAll(context)
            }
        }
    }

    private fun validTimeOrDefault(
        value: String?,
        defaultValue: String
    ): String {
        val clean =
            value?.trim().orEmpty()

        if (!Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
                .matches(clean)
        ) {
            return defaultValue
        }

        return clean
    }

    fun scheduleAll(context: Context) {

        val schedule =
            OfflineNotificationStore(context)
                .getSchedule()

        schedule(
            context = context,
            notificationId = MORNING_ID,
            time = schedule.morning,
            requestCode = REQUEST_MORNING
        )

        schedule(
            context = context,
            notificationId = AFTERNOON_ID,
            time = schedule.afternoon,
            requestCode = REQUEST_AFTERNOON
        )

        schedule(
            context = context,
            notificationId = EVENING_ID,
            time = schedule.evening,
            requestCode = REQUEST_EVENING
        )
    }

    private fun schedule(
        context: Context,
        notificationId: Int,
        time: String,
        requestCode: Int
    ) {
        val parsed =
            parseTime(time)
                ?: return

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                NotificationReceiver::class.java
            ).apply {
                putExtra(
                    NotificationReceiver
                        .EXTRA_NOTIFICATION_ID,
                    notificationId
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(pendingIntent)

        val triggerTime =
            nextTriggerTime(
                parsed.first,
                parsed.second
            )

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
                alarmManager.canScheduleExactAlarms()
            ) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

            } else {

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

        } catch (_: SecurityException) {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun parseTime(
        value: String
    ): Pair<Int, Int>? {

        val parts =
            value.split(":")

        if (parts.size != 2) {
            return null
        }

        val hour =
            parts[0].toIntOrNull()
                ?: return null

        val minute =
            parts[1].toIntOrNull()
                ?: return null

        if (
            hour !in 0..23 ||
            minute !in 0..59
        ) {
            return null
        }

        return Pair(hour, minute)
    }

    private fun nextTriggerTime(
        hour: Int,
        minute: Int
    ): Long {

        val now =
            Calendar.getInstance()

        val target =
            Calendar.getInstance().apply {
                set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )
                set(
                    Calendar.MINUTE,
                    minute
                )
                set(
                    Calendar.SECOND,
                    0
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )
            }

        if (!target.after(now)) {
            target.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        return target.timeInMillis
    }

    fun showNotification(
        context: Context,
        notificationId: Int
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification =
            OfflineNotificationStore(context)
                .get(notificationId)

        if (
            !notification.enabled ||
            notification.title.isBlank() ||
            notification.message.isBlank()
        ) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val launchIntent =
            context.packageManager
                .getLaunchIntentForPackage(
                    context.packageName
                )

        val contentIntent =
            launchIntent?.let {
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
                )
            }

        val builder =
            NotificationCompat
                .Builder(
                    context,
                    CHANNEL_ID
                )
                .setSmallIcon(
                    com.kankarej.kankarejspices
                        .R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    notification.title
                )
                .setContentText(
                    notification.message
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            notification.message
                        )
                )
                .setPriority(
                    NotificationCompat
                        .PRIORITY_DEFAULT
                )
                .setAutoCancel(true)

        if (contentIntent != null) {
            builder.setContentIntent(
                contentIntent
            )
        }

        manager.notify(
            notificationId,
            builder.build()
        )
    }

    private fun createNotificationChannel(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    CHANNEL_DESCRIPTION
            }
        )
    }

    fun isExactAlarmAllowed(
        context: Context
    ): Boolean {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            return true
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        return alarmManager
            .canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            return
        }

        try {

            val intent =
                Intent(
                    Settings
                        .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {
                    data =
                        Uri.parse(
                            "package:${context.packageName}"
                        )
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

        } catch (_: Exception) {
        }
    }
}
