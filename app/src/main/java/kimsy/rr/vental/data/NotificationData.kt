package kimsy.rr.vental.data

import com.google.firebase.firestore.FieldValue

data class NotificationData(
    val fromUserId: String = "",
    val type: NotificationType = NotificationType.DEBATESTART,
    val targetItemId: String = "",
    val body: String = "",
    val readFlag: Boolean = false,
    val notificationDatetime: Any = FieldValue.serverTimestamp(),
    ){
    companion object {

        fun createNotification(fromUserId: String, targetItemId: String, targetItemType: NotificationType, body: String): NotificationData {
            return NotificationData(
                fromUserId = fromUserId,
                type = targetItemType,
                targetItemId = targetItemId,
                body = body
            )
        }
//
//        fun createForDebateStart(fromUserId: String, targetItemId: String, body: String): NotificationData {
//            Log.d("ND", "createForDebateStart called")
//            return NotificationData(
//                fromUserId = fromUserId,
//                type = NotificationType.DEBATESTART,
//                targetItemId = targetItemId,
//                body = body
//            )
//        }
//
//        fun createForDebateMessage(fromUserId: String, targetItemId: String, body: String): NotificationData {
//            return NotificationData(
//                fromUserId = fromUserId,
//                type = NotificationType.DEBATEMESSAGE,
//                targetItemId = targetItemId,
//                body = body
//            )
//        }
//
//        fun createForDebateComment(fromUserId: String, targetItemId: String, body: String): NotificationData {
//            return NotificationData(
//                fromUserId = fromUserId,
//                type = NotificationType.DEBATECOMMENT,
//                targetItemId = targetItemId,
//                body = body
//            )
//        }
    }
}

enum class NotificationType(val type: String) {
    DEBATESTART("debateStart"),
    DEBATEMESSAGE("debateMessage"),
    DEBATECOMMENT("debateComment")
}