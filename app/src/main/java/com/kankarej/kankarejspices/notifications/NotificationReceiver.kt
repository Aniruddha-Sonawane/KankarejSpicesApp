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

        // Display from the local buffer first.
        // Internet is NOT required here.
        OfflineNotificationManager.showNotification(
            context,
            notificationId
        )

        // Refresh Firebase after local delivery.
        // If offline, the cached data remains unchanged.
        OfflineNotificationManager.syncFromFirebase(
            context
        )

        // Schedule the next occurrence using the
        // current locally cached schedule.
        OfflineNotificationManager.scheduleAll(
            context
        )
    }
}
