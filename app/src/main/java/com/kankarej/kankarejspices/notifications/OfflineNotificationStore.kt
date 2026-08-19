package com.kankarej.kankarejspices.notifications

import android.content.Context

class OfflineNotificationStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "offline_notification_buffer",
        Context.MODE_PRIVATE
    )

    fun save(notification: OfflineNotification) {
        prefs.edit()
            .putString(
                "title_${notification.id}",
                notification.title
            )
            .putString(
                "message_${notification.id}",
                notification.message
            )
            .putBoolean(
                "enabled_${notification.id}",
                notification.enabled
            )
            .apply()
    }

    fun get(id: Int): OfflineNotification {
        return OfflineNotification(
            id = id,
            title = prefs.getString(
                "title_$id",
                ""
            ) ?: "",
            message = prefs.getString(
                "message_$id",
                ""
            ) ?: "",
            enabled = prefs.getBoolean(
                "enabled_$id",
                true
            )
        )
    }

    fun saveAll(
        notifications: List<OfflineNotification>
    ) {
        val editor = prefs.edit()

        notifications.forEach { notification ->
            editor
                .putString(
                    "title_${notification.id}",
                    notification.title
                )
                .putString(
                    "message_${notification.id}",
                    notification.message
                )
                .putBoolean(
                    "enabled_${notification.id}",
                    notification.enabled
                )
        }

        editor.apply()
    }

    fun saveSchedule(
        schedule: NotificationSchedule
    ) {
        prefs.edit()
            .putString(
                "schedule_morning",
                schedule.morning
            )
            .putString(
                "schedule_afternoon",
                schedule.afternoon
            )
            .putString(
                "schedule_evening",
                schedule.evening
            )
            .apply()
    }

    fun getSchedule(): NotificationSchedule {
        return NotificationSchedule(
            morning = prefs.getString(
                "schedule_morning",
                "08:30"
            ) ?: "08:30",

            afternoon = prefs.getString(
                "schedule_afternoon",
                "14:00"
            ) ?: "14:00",

            evening = prefs.getString(
                "schedule_evening",
                "20:00"
            ) ?: "20:00"
        )
    }

    fun getConfiguration(): NotificationConfiguration {
        return NotificationConfiguration(
            notifications = listOf(
                get(1),
                get(2),
                get(3)
            ),
            schedule = getSchedule()
        )
    }
}
