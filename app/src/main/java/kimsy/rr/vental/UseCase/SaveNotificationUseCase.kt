package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.NotificationRepository
import javax.inject.Inject


class SaveNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(
        fromUserId: String,
        toUserId: String,
        targetId: String,
        body: String
        ): Resource<Unit> {
        return try {
            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }
            Log.d("SASNUC", "$fromUserId, $toUserId, $targetId, $body")

            val notificationData = NotificationData.createForDebateStart(fromUserId, targetId, body)
            // 通知をデータベースに保存
            notificationRepository.saveNotificationData(notificationData, toUserId)


            Resource.success(Unit)
        } catch (e: Exception) {
            Log.e("SASNUC", "Error in invoke: ${e.message}")
            Resource.failure(e.message)
        }
    }
}
