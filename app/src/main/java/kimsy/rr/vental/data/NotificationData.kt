package kimsy.rr.vental.data

import android.util.Log
import com.google.firebase.firestore.FieldValue

data class NotificationData(
    val fromUserId: String = "",
    val type: NotificationType = NotificationType.DEBATESTART,
    val targetId: String = "",
    val body: String = "",
    val readFlag: Boolean = false,
    val notificationDatetime: Any = FieldValue.serverTimestamp(),
    ){
    companion object {
        fun createForDebateStart(fromUserId: String, targetId: String, body: String): NotificationData {
            Log.d("ND", "createForDebateStart called")
            return NotificationData(
                fromUserId = fromUserId,
                type = NotificationType.DEBATESTART,
                targetId = targetId,
                body = body
            )
        }

        fun createForDebateMessage(fromUserId: String, targetId: String, body: String): NotificationData {
            return NotificationData(
                fromUserId = fromUserId,
                type = NotificationType.DEBATEMESSAGE,
                targetId = targetId,
                body = body
            )
        }

        fun createForDebateComment(fromUserId: String, targetId: String, body: String): NotificationData {
            return NotificationData(
                fromUserId = fromUserId,
                type = NotificationType.DEBATECOMMENT,
                targetId = targetId,
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