package kimsy.rr.vental

import android.R.attr.src
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val TAG = "push notification"

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        // ユーザーがログイン中である場合のみトークンを保存
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            userRef.update("deviceToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Device token saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to save device token: ${e.message}", e)
                }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived: " + remoteMessage.data["message"])

        //Push通知タップ時に起動するActivityを指定
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        //通知を表示するchannelIdを指定
        val bitmap = getBitmapFromURL(remoteMessage.data["fromUserImageURL"])
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null, URL may be invalid or loading failed.")
        } else {
            Log.d(TAG, "Bitmap loaded successfully.")
        }
        val channelId = "Default"
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            //通知に表示されるアイコンを指定
            .setSmallIcon(R.mipmap.ic_launcher)
            //通知のタイトルを指定
            .setContentTitle(remoteMessage.data["title"])
            //通知の本文を指定
            .setContentText(remoteMessage.data["body"])
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(remoteMessage.data["body"])).setAutoCancel(true)
            .setLargeIcon(bitmap)
            //trueにしていると自動的に通知バッジを削除してくれる
            .setContentIntent(pendingIntent)
        //TODO ここでdebate取得＞shareModelに追加＞画面遷移　ーどのタイプでもただ討論詳細に飛べば良いと思う
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "Default channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        //指定したIDの通知チャンネルが存在しない場合に新しく作成される
        manager.createNotificationChannel(channel)
        //指定したIDでPush通知を表示する(同じIDを利用していると上書きされて1つしか表示されない)
        manager.notify(0, builder.build())
    }

    private fun getBitmapFromURL(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}