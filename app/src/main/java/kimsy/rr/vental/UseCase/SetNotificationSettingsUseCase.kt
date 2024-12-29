package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.NotificationSettings
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import javax.inject.Inject

class SetNotificationSettingsUseCase @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(userId: String, notificationSettings: NotificationSettings): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            notificationSettingsRepository.updateNotificationSettings(userId, notificationSettings)
            Resource.success(Unit)
        }
    }
}