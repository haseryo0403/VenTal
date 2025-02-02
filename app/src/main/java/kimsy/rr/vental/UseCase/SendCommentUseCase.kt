package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Comment
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.CommentRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class SendCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        posterId: String,
        ventCardId: String,
        debateId: String,
        commenterId: String,
        commentContent: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val comment = Comment(
                commenterId = commenterId,
                commentContent = commentContent
            )
            commentRepository.saveComment(
                posterId = posterId,
                swipeCardId = ventCardId,
                debateId = debateId,
                comment = comment
            )
            Resource.success(Unit)
        }
    }

}