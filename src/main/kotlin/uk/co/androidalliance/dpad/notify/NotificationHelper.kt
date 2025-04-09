package uk.co.androidalliance.dpad.notify

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

object NotificationHelper {
    private const val DEBUG = false
    private const val ADDON = "Dpad (Logging)"

    fun info(message: String) {
        if (!DEBUG) return
        sendNotification(
            message,
            NotificationType.INFORMATION,
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(ADDON)
        )
    }

    fun warn(message: String) {
        sendNotification(
            message,
            NotificationType.WARNING,
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(ADDON)
        )
    }

    // Function to send an error notification
    fun error(message: String) {
        sendNotification(
            message,
            NotificationType.ERROR,
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(ADDON)
        )
    }

    // Helper function to create and display a notification
    private fun sendNotification(
        message: String,
        notificationType: NotificationType,
        notificationGroup: NotificationGroup,
    ) {
        // Create the notification without a listener
        val notification = notificationGroup.createNotification(
            "Dpad",
            escapeString(message),
            notificationType,
        )

        // Display the notification
        notification.notify(null)
    }

    private fun escapeString(string: String) = string.replace(
        "\n".toRegex(),
        "\n<br />"
    )
}