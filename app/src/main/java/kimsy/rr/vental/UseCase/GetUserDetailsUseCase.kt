package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(uid: String): Resource<User> {
        return executeWithLoggingAndNetworkCheck {
            val user = userRepository.fetchUserInformation(uid)
            Resource.success(user)
        }
    }
}