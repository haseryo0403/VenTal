package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Comment
import kimsy.rr.vental.data.CommentItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.repository.CommentRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetCommentItemUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        posterId: String,
        ventCardId: String,
        debateId: String
    ): Resource<List<CommentItem>> {
        return executeWithLoggingAndNetworkCheck {
            val comments = commentRepository.fetchComments(
                posterId = posterId,
                swipeCardId = ventCardId,
                debateId = debateId
            )

            val commentItems = coroutineScope {
                comments.map { comment ->
                    async {
                        generateCommentItem(comment)
                    }
                }.awaitAll()
            }

            Resource.success(commentItems)
        }
    }

    private suspend fun generateCommentItem(comment: Comment): CommentItem {
        val fetchUserInfoState =getUserDetailsUseCase.execute(comment.commenterId)
        val commenter = fetchUserInfoState.data.takeIf { fetchUserInfoState.status == Status.SUCCESS }
        val commentItem = commenter?.let {
            CommentItem(
                comment = comment,
                user = it
            )
        }
        return commentItem?: throw IllegalArgumentException("commentItemの生成に失敗しました。")
    }
}