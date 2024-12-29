package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NotificationData
import kimsy.rr.vental.data.NotificationItem
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import javax.inject.Inject

class GenerateNotificationItemUseCase @Inject constructor(
    private val getUerDetailsUseCase: GetUserDetailsUseCase
) {
    suspend fun execute(notificationData: NotificationData): NotificationItem? {
        val user = getUserInfo(notificationData.fromUserId)
        return if (user != null) NotificationItem(notificationData, user) else null
    }

    private suspend fun getUserInfo(userId: String): User? {
        val fetchUserInfoState = getUerDetailsUseCase.execute(userId)
        return fetchUserInfoState.data.takeIf { fetchUserInfoState.status == Status.SUCCESS }
    }
}
