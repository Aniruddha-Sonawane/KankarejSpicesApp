package com.kankarej.kankarejspices.notifications

import android.content.Context

class OfflineNotificationStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "offline_notification_buffer",
        Context.MODE_PRIVATE
    )

    fun save(notification: OfflineNotification) {
        prefs.edit()
            .putString("title_${notification.id}", notification.title)
            .putString("message_${notification.id}", notification.message)
            .putBoolean("enabled_${notification.id}", notification.enabled)
            .apply()
    }

    fun get(id: Int): OfflineNotification {
        return OfflineNotification(
            id = id,
            title = prefs.getString("title_$id", "") ?: "",
            message = prefs.getString("message_$id", "") ?: "",
            enabled = prefs.getBoolean("enabled_$id", true)
        )
    }

    fun saveAll(notifications: List<OfflineNotification>) {
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
}
