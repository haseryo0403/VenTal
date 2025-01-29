package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveNotificationCountUseCase @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val notificationRepository: NotificationRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(currentUserId: String): Flow<Resource<Int>> {
        return executeFlowWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            val notificationSettings = notificationSettingsRepository.getNotificationSettings(currentUserId)
            notificationRepository.observeNotificationCount(currentUserId, notificationSettings).map { count ->
                Resource.success(count)
            }
        }
    }
}