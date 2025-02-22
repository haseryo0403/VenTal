package kimsy.rr.vental.data

data class NotificationSettings(
    val deviceToken: String = "",
    val debateStartNotification: Boolean = true,
    val messageNotification: Boolean = true,
    val commentNotification: Boolean = true
)

