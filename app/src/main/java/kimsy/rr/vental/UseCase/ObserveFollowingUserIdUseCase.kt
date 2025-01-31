package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.FollowRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveFollowingUserIdUseCase @Inject constructor(
    private val followRepository: FollowRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(currentUserId: String): Flow<Resource<List<String>>> {
        return executeFlowWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            followRepository.observeFollowingUserIds(currentUserId)
                .map { followingIds ->
                    Resource.success(followingIds)
                }
        }
    }
}