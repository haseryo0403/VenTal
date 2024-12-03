package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationDataUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        notificationData: NotificationData,
        toUserId: String
    ): Result<Unit>{
        return notificationRepository.saveNotificationData(notificationData, toUserId)
    }
}