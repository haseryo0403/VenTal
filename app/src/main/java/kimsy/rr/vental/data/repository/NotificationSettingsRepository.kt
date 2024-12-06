package kimsy.rr.vental.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kimsy.rr.vental.data.NotificationSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class NotificationSettingsRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    //TODO deviceTokenをnotificationSettingsに入れるのはどう？
    suspend fun getNotificationSettings(userId: String): Result<NotificationSettings> {
        return try {
            withTimeout(10000L) {
                val docRef = db
                    .collection("notificationSettings")
                    .document(userId)

                val notificationSettingsSnapshot = docRef.get().await()
                val notificationSettings = notificationSettingsSnapshot.toObject(NotificationSettings::class.java)
                if (notificationSettings != null) {
                    Log.d("notificationSettings","$notificationSettings")
                    Result.success(notificationSettings)
                } else {
                    Result.failure(Exception("Notification settings is not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setNotificationSettings (userId: String):Result<Unit> {
        return try {
            val defaultNotificationSettings = NotificationSettings()
            withTimeout(10000L) {
                db
                    .collection("notificationSettings")
                    .document(userId)
                    .set(defaultNotificationSettings)
                    .await()

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}