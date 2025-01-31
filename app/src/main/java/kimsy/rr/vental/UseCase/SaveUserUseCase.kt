package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val newUserId = userRepository.saveUserToFirestore()
            notificationSettingsRepository.setNotificationSettings(newUserId)
            Resource.success(Unit)
        }
    }
}