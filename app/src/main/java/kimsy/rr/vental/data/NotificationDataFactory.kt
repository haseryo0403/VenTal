package kimsy.rr.vental.data

class NotificationDataFactory {
    fun create(notificationType: NotificationType, fromUserId: String, body: String): NotificationData {
        return when (notificationType) {
            NotificationType.DEBATESTART -> NotificationData.createForDebateStart(fromUserId, body)
            NotificationType.DEBATEMESSAGE -> NotificationData.createForDebateMessage(fromUserId, body)
            NotificationType.DEBATECOMMENT -> NotificationData.createForDebateComment(fromUserId, body)
        }
    }
}