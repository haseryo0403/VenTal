package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class HandleLikeActionUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        userId: String,
        posterId: String,
        ventCardId: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val isLiked = ventCardRepository.checkIfLiked(userId, ventCardId)
            if (isLiked) {
                Resource.failure()
            } else {
                ventCardRepository.likeVentCard(userId, posterId, ventCardId)
                Resource.success(Unit)
            }
        }
    }
}
