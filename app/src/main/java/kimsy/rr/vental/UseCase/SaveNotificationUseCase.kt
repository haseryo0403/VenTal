package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationRepository
import javax.inject.Inject


class SaveNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        fromUserId: String,
        toUserId: String,
        targetItemId: String,
        notificationType: NotificationType,
        body: String
        ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val notificationData = NotificationData.createNotification(fromUserId, targetItemId, notificationType, body)
            // 通知をデータベースに保存
            notificationRepository.saveNotificationData(notificationData, toUserId)

            Resource.success(Unit)
        }
    }
}
