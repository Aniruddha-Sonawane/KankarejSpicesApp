package com.kankarej.kankarejspices.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object OfflineNotificationManager {

    private const val CHANNEL_ID = "kankarej_spices_notifications"
    private const val CHANNEL_NAME = "Kankarej Spices"
    private const val CHANNEL_DESCRIPTION = "Kankarej Spices notifications"

    private const val REQUEST_MORNING = 1001
    private const val REQUEST_AFTERNOON = 1002
    private const val REQUEST_EVENING = 1003

    private const val MORNING_ID = 1
    private const val AFTERNOON_ID = 2
    private const val EVENING_ID = 3

    private const val MORNING_HOUR = 8
    private const val MORNING_MINUTE = 30

    private const val AFTERNOON_HOUR = 14
    private const val AFTERNOON_MINUTE = 0

    private const val EVENING_HOUR = 20
    private const val EVENING_MINUTE = 0

    private const val FIREBASE_URL =
        "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private fun database() =
        FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .reference

    fun initialize(context: Context) {
        createNotificationChannel(context)
        scheduleAll(context)
        syncFromFirebase(context)
    }

    fun syncFromFirebase(context: Context) {
        scope.launch {
            try {
                val snapshot = database()
                    .child("notifications")
                    .get()
                    .await()

                val notifications = mutableListOf<OfflineNotification>()

                snapshot.children.forEach { child ->

                    val id = child.key
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
                                child.child("enabled")
                                    .value
                        ) {
                            null -> true
                            is Boolean -> value
                            is String -> value.toBooleanStrictOrNull() ?: true
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

                if (notifications.isNotEmpty()) {
                    OfflineNotificationStore(context)
                        .saveAll(notifications)
                }

            } catch (_: Exception) {
                // Offline or Firebase unavailable.
                // Existing local notification buffer remains intact.
            }
        }
    }

    fun scheduleAll(context: Context) {
        schedule(
            context = context,
            notificationId = MORNING_ID,
            hour = MORNING_HOUR,
            minute = MORNING_MINUTE,
            requestCode = REQUEST_MORNING
        )

        schedule(
            context = context,
            notificationId = AFTERNOON_ID,
            hour = AFTERNOON_HOUR,
            minute = AFTERNOON_MINUTE,
            requestCode = REQUEST_AFTERNOON
        )

        schedule(
            context = context,
            notificationId = EVENING_ID,
            hour = EVENING_HOUR,
            minute = EVENING_MINUTE,
            requestCode = REQUEST_EVENING
        )
    }

    private fun schedule(
        context: Context,
        notificationId: Int,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent = Intent(
            context,
            NotificationReceiver::class.java
        ).apply {
            putExtra(
                NotificationReceiver.EXTRA_NOTIFICATION_ID,
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
            nextTriggerTime(hour, minute)

        try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
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

    private fun nextTriggerTime(
        hour: Int,
        minute: Int
    ): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
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

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(
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
            androidx.core.app.NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(
                    com.kankarej.kankarejspices.R.drawable.ic_launcher_foreground
                )
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setStyle(
                    androidx.core.app.NotificationCompat
                        .BigTextStyle()
                        .bigText(notification.message)
                )
                .setPriority(
                    androidx.core.app.NotificationCompat
                        .PRIORITY_DEFAULT
                )
                .setAutoCancel(true)

        if (contentIntent != null) {
            builder.setContentIntent(contentIntent)
        }

        notificationManager.notify(
            notificationId,
            builder.build()
        )
    }

    private fun createNotificationChannel(
        context: Context
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

        manager.createNotificationChannel(channel)
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    ).apply {
                        data =
                            android.net.Uri.parse(
                                "package:${context.packageName}"
                            )
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                )
            } catch (_: Exception) {
            }
        }
    }
}
