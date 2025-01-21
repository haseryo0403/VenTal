package kimsy.rr.vental.UseCase

import kimsy.rr.vental.R
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class GetUserInfoByUserIdListUseCase @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        userIds: List<String>
    ): Resource<List<User>> {
        return executeWithLoggingAndNetworkCheck {
            val users = mutableListOf<User>()
            userIds.forEach { userId->
                val userInfoState = getUserDetailsUseCase.execute(userId)
                if (userInfoState.status != Status.SUCCESS) {
                    return@executeWithLoggingAndNetworkCheck Resource.failure(
                        userInfoState.message ?: "${R.string.no_user_found}"
                    )
                }
                val user = userInfoState.data
                    ?: return@executeWithLoggingAndNetworkCheck Resource.failure("${R.string.no_user_found}")
                users.add(user)
            }
            Resource.success(users)
        }
    }
}