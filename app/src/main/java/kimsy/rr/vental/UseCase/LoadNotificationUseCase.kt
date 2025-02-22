package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.NotificationItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import javax.inject.Inject

class LoadNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val generateNotificationItemUseCase: GenerateNotificationItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute (
        currentUserId: String,
        lastVisible: DocumentSnapshot?
    ): Resource<Pair<List<NotificationItem>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            val notificationSettings = notificationSettingsRepository.getNotificationSettings(currentUserId)
            val result = notificationRepository.loadNotificationsData(currentUserId, lastVisible, notificationSettings)
            val notificationsData = result.first
            val newLastVisible = result.second
            val notificationItems = notificationsData.mapNotNull { notificationData ->
                generateNotificationItemUseCase.execute(notificationData)
            }
            Resource.success(Pair(notificationItems, newLastVisible))
        }
    }
}