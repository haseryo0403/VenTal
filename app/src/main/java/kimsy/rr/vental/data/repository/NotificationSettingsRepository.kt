package kimsy.rr.vental.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.NotificationSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationSettingsRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getNotificationSettings(userId: String): NotificationSettings {
        val docRef = db
            .collection("notificationSettings")
            .document(userId)

        val notificationSettingsSnapshot = docRef.get().await()
        val notificationSettings = notificationSettingsSnapshot.toObject(NotificationSettings::class.java)
        return notificationSettings?: throw NoSuchElementException("notification settings not found")
    }

    suspend fun updateNotificationSettings (
        userId: String,
        notificationSettings: NotificationSettings
    ) {
        val query = db
            .collection("notificationSettings")
            .document(userId)

        query
            .set(notificationSettings)
            .await()
    }

    suspend fun setNotificationSettings (userId: String){
        val defaultNotificationSettings = NotificationSettings()
        db
            .collection("notificationSettings")
            .document(userId)
            .set(defaultNotificationSettings)
            .await()
    }
}