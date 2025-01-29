package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.NotificationSettings
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import javax.inject.Inject

class LoadNotificationSettingsUseCase @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        currentUserId: String,
    ): Resource<NotificationSettings> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            val result = notificationSettingsRepository.getNotificationSettings(currentUserId)
            Resource.success(result)
        }
    }
}