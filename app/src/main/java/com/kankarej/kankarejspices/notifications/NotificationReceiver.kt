package com.kankarej.kankarejspices.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTIFICATION_ID =
            "notification_id"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val notificationId =
            intent.getIntExtra(
                EXTRA_NOTIFICATION_ID,
                -1
            )

        if (notificationId !in 1..3) {
            return
        }

        OfflineNotificationManager.showNotification(
            context,
            notificationId
        )

        // The current notification uses the local buffer,
        // so this works without internet.
        //
        // Then Firebase is synchronized in the background.
        // Any updated content will therefore be available
        // for the next scheduled notification.
        OfflineNotificationManager.syncFromFirebase(
            context
        )

        // Android alarms are one-shot, so schedule the
        // next occurrence after every trigger.
        OfflineNotificationManager.scheduleAll(
            context
        )
    }
}
