package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationSettings
import kimsy.rr.vental.data.NotificationType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun saveNotificationData(
        notificationData: NotificationData,
        toUserId: String
    ){
        val docRef = db
            .collection("users")
            .document(toUserId)
            .collection("notifications")
            .document()

        val notificationDataWithId = notificationData.copy(notificationId = docRef.id)

        docRef.set(notificationDataWithId).await()
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

    suspend fun observeNotificationCount(
        currentUserId: String,
        notificationSettings: NotificationSettings
    ): Flow<Int> = callbackFlow{

        val excludedNotificationType = mutableListOf<NotificationType>().apply {
            if (!notificationSettings.debateStartNotification) add(NotificationType.DEBATESTART)
            if (!notificationSettings.messageNotification) add(NotificationType.DEBATEMESSAGE)
            if (!notificationSettings.commentNotification) add(NotificationType.DEBATECOMMENT)
        }

        val baseDocRef = db
            .collection("users")
            .document(currentUserId)
            .collection("notifications")

        val docRef = if (excludedNotificationType.isNotEmpty()) {
            baseDocRef.whereNotIn("type", excludedNotificationType).whereEqualTo("readFlag", false)
        } else {
            baseDocRef.whereEqualTo("readFlag", false)
        }

        val subscription = docRef.addSnapshotListener{ querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                val count = querySnapshot.size()
                trySend(count)
            }
        }
        awaitClose{ subscription.remove() }
    }

    suspend fun markNotificationAsRead(
        currentUserId: String,
        notificationId: String
    ){
        val query = db
            .collection("users")
            .document(currentUserId)
            .collection("notifications")
            .document(notificationId)

        query.update("readFlag", true)
    }

}
