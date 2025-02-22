package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class MarkUserNotNewUseCase  @Inject constructor(
    private val userRepository: UserRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        currentUserId: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            userRepository.updateNewUserFlag(currentUserId)
            Resource.success(Unit)
        }
    }
}