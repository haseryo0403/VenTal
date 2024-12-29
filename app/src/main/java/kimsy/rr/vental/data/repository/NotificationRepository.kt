package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationSettings
import kimsy.rr.vental.data.NotificationType
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun saveNotificationData(
        notificationData: NotificationData,
        toUserId: String
    ): Result<Unit> {
        return try {
            Log.d("NR", "saveNotificationData called")
            withTimeout(10000L) {
                val docRef = db
                    .collection("users")
                    .document(toUserId)
                    .collection("notifications")

                docRef.add(notificationData).await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadNotificationsData(
        currentUserId: String,
        lastVisible: DocumentSnapshot? = null,
        notificationSettings: NotificationSettings
    ): Pair<List<NotificationData>, DocumentSnapshot?> {
        val excludedNotificationType = mutableListOf<NotificationType>().apply {
            if (!notificationSettings.debateStartNotification) add(NotificationType.DEBATESTART)
            if (!notificationSettings.messageNotification) add(NotificationType.DEBATEMESSAGE)
            if (!notificationSettings.commentNotification) add(NotificationType.DEBATECOMMENT)
        }

        val baseQuery = db
            .collection("users")
            .document(currentUserId)
            .collection("notifications")
            .orderBy("notificationDatetime", Query.Direction.DESCENDING)

        // `whereNotIn` を追加する条件をチェック
        val query = if (excludedNotificationType.isNotEmpty()) {
            baseQuery.whereNotIn("type", excludedNotificationType)
        } else {
            baseQuery
        }

        val querySnapshot = if (lastVisible == null) {
            query.limit(10).get().await()
        } else {
            query.startAfter(lastVisible).limit(10).get().await()
        }

        if (querySnapshot.isEmpty) {
            // データがない場合、空リストとnullを返す
            return Pair(emptyList(), null)
        }

        val newLastVisible = querySnapshot.documents.lastOrNull()

        val notificationsData = querySnapshot.documents.mapNotNull { document ->
            document.toObject(NotificationData::class.java)
        }
        return Pair(notificationsData, newLastVisible)
    }
}
