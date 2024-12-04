package kimsy.rr.vental.data.repository

import android.R
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kimsy.rr.vental.data.NotificationData
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    // 通知を送信する
    fun sendNotification(notificationData: NotificationData, title: String, deviceToken: String): Result<Unit> {
        return try {
//            // RemoteMessageを作成して通知送信
//            val message = RemoteMessage.Builder(deviceToken)
//                .setMessageId(System.currentTimeMillis().toInt().toString())  // 一意のメッセージID
//                .addData("title", title)
//                .addData("body", notificationData.body)
//                .addData("fromUserId", notificationData.fromUserId)
//                .addData("notificationDatetime", notificationData.notificationDatetime.toString())
//                .build()


            val message = RemoteMessage.Builder("cYDlub-JRrCn-JIMR9oqC5:APA91bE2LpuPDO6AjJauF-Y1E_EaLHW4taVCStdAH3glOllKsDwrtm1yAao_HMQIqfxMhGyvMhrE64iZHIspjDr5ux2pikO1CLV06PPemCJsXg_oiTrftlE")
                .addData("title", "debatestart")
                .addData("body", "text")
                .build()

            val response = FirebaseMessaging.getInstance().send(message)
            // Response is a message ID string.
            println("Successfully sent message: $response")
            // FirebaseMessagingで送信

            Result.success(Unit)  // 成功時
        } catch (e: Exception) {
            Log.e("FCM", "Failed to send notification: ${e.message}")
            Result.failure(e)  // 失敗時
        }
    }

    suspend fun saveNotificationData(notificationData: NotificationData, toUserId: String): Result<Unit> {
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
}
