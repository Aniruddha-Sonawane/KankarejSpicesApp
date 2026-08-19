package com.kankarej.kankarejspices.notifications

data class OfflineNotification(
    val id: Int,
    val title: String = "",
    val message: String = "",
    val enabled: Boolean = true
)

data class NotificationSchedule(
    val morning: String = "08:30",
    val afternoon: String = "14:00",
    val evening: String = "20:00"
)

data class NotificationConfiguration(
    val notifications: List<OfflineNotification> = emptyList(),
    val schedule: NotificationSchedule = NotificationSchedule()
)
