package com.kankarej.kankarejspices.notifications

data class OfflineNotification(
    val id: Int,
    val title: String = "",
    val message: String = "",
    val enabled: Boolean = true
)
