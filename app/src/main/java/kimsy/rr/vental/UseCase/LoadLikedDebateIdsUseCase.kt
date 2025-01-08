package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class LoadLikedDebateIdsUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        userId: String
    ): Resource<List<String>> {
        return executeWithLoggingAndNetworkCheck {
            val likedDebateIds = debateRepository.fetchLikedDebateIds(userId)
            Resource.success(likedDebateIds)
        }
    }
}