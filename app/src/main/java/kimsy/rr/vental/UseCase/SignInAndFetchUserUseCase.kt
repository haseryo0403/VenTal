package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.UserRepository
import javax.inject.Inject

class SignInAndFetchUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(idToken: String): Resource<User?> {
        return executeWithLoggingAndNetworkCheck {
            userRepository.firebaseAuthWithGoogle(idToken)
            val currentUser = userRepository.getCurrentUser()
            Resource.success(currentUser)
        }
    }
}
