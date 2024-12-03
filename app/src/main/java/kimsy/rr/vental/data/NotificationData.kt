package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class NotificationData(
    val fromUserId: String = "",
    val type: NotificationType = NotificationType.DEBATESTART,
    val body: String = "",
    val readFlag: Boolean = false,
    val notificationDatetime: Any = FieldValue.serverTimestamp(),
    ){
    companion object {
        fun createForDebateStart(actionUserId: String, body: String): NotificationData {
            return NotificationData(
                fromUserId = actionUserId,
                type = NotificationType.DEBATESTART,
                body = body
            )
        }

        fun createForDebateMessage(actionUserId: String, body: String): NotificationData {
            return NotificationData(
                fromUserId = actionUserId,
                type = NotificationType.DEBATEMESSAGE,
                body = body
            )
        }

        fun createForDebateComment(actionUserId: String, body: String): NotificationData {
            return NotificationData(
                fromUserId = actionUserId,
                type = NotificationType.DEBATECOMMENT,
                body = body
            )
        }
    }
}

enum class NotificationType(val type: String) {
    DEBATESTART("debateStart"),
    DEBATEMESSAGE("debateMessage"),
    DEBATECOMMENT("debateComment")
}