package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.CloseAccountData
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class CloseAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        currentUserId: String,
        reasonNumber: Int
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            userRepository.updateAccountClosingFlagToTrue(currentUserId)
            val closeAccountData = CloseAccountData(
                userId = currentUserId,
                reasonNumber = reasonNumber
            )
            userRepository.saveCloseAccountData(closeAccountData)
            Resource.success(Unit)
        }
    }
}