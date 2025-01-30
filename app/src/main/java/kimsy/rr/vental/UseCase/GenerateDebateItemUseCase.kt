package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.LikeStatus
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.repository.DebateRepository
import javax.inject.Inject

class GenerateDebateItemUseCase @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val debateRepository: DebateRepository
) {
    //UseCaseからのみ使用されるのでここではtry catchしない
    suspend fun execute(debate: Debate, userId: String): DebateItem? {
        val ventCard = getSwipeCardUseCase.execute(debate.posterId, debate.swipeCardId)
        val poster = getPosterInfo(debate)
        val debater = getDebaterInfo(debate)
        val likeState = debateRepository.fetchLikeState(userId, debate.debateId)
        val likeUserType = when (likeState.status) {
            Status.SUCCESS -> likeState.data?.let { handleSuccess(it) }
            Status.FAILURE -> null
            else -> null
        }
        return if (poster != null && debater != null) {
            DebateItem(debate, ventCard, poster, debater, likeUserType)
        } else {
            null // 失敗した場合はスキップ
        }

    }

    private fun handleSuccess(likeStatus: LikeStatus): UserType? {
        return when (likeStatus) {
            LikeStatus.LIKED_POSTER -> UserType.POSTER
            LikeStatus.LIKED_DEBATER -> UserType.DEBATER
            LikeStatus.LIKE_NOT_EXIST -> null
        }
    }

//    private suspend fun getVentCard(debate: Debate): VentCard? {
//        val ventCardResource = getSwipeCardUseCase.execute(debate.posterId, debate.swipeCardId)
//        return ventCardResource.data.takeIf { ventCardResource.status == Status.SUCCESS }
//    }

    private suspend fun getPosterInfo(debate: Debate): User? {
        val fetchPosterInfoState = getUserDetailsUseCase.execute(debate.posterId)
        return fetchPosterInfoState.data.takeIf { fetchPosterInfoState.status == Status.SUCCESS }
    }

    private suspend fun getDebaterInfo(debate: Debate): User? {
        val fetchDebaterInfoState = getUserDetailsUseCase.execute(debate.debaterId)
        return fetchDebaterInfoState.data.takeIf { fetchDebaterInfoState.status == Status.SUCCESS }
    }
}
