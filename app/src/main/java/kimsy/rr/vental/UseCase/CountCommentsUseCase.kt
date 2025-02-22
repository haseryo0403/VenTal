package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.CommentRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class CountCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        posterId: String,
        ventCardId: String,
        debateId: String
    ): Resource<Long> {
        return executeWithLoggingAndNetworkCheck {
            val count = commentRepository.countComments(
                posterId = posterId,
                swipeCardId = ventCardId,
                debateId = debateId
            )
            Resource.success(count)
        }
    }
}