package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.NotificationRepository
import kimsy.rr.vental.data.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveNotificationCountUseCase @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val notificationRepository: NotificationRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(currentUserId: String): Flow<Resource<Int>> = flow {
        try {
            val networkState = checkNetwork()
            if (networkState.status != Status.SUCCESS) {
                emit(Resource.failure("インターネットの接続を確認してください"))
                return@flow
            }
            val notificationSettings = notificationSettingsRepository.getNotificationSettings(currentUserId)
            notificationRepository.observeNotificationCount(currentUserId, notificationSettings).collect {count ->
                emit(Resource.success(count))
            }


        } catch(e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            emit(Resource.failure(e.message))
        }

    }
}