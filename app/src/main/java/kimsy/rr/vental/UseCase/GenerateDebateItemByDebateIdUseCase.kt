package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.LikeStatus
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class GenerateDebateItemByDebateIdUseCase @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        debateId: String,
        currentUserId: String
    ): Resource<DebateItem> {
        return executeWithLoggingAndNetworkCheck {
            val debate = getDebateByDebateId(debateId)
            val ventCard = getVentCard(debate)
            val poster = getPosterInfo(debate)
            val debater = getDebaterInfo(debate)
            val likeState = debateRepository.fetchLikeState(currentUserId, debateId)
            val likeUserType = when (likeState.status) {
                Status.SUCCESS -> likeState.data?.let { handleSuccess(it) }
                Status.FAILURE -> null
                else -> null
            }

            if (ventCard != null && poster != null && debater != null) {
                Resource.success(DebateItem(debate, ventCard, poster, debater, likeUserType))
            } else {
                Resource.failure("")
            }
        }
    }

    private suspend fun getDebateByDebateId(debateId: String): Debate {
        return debateRepository.fetchDebateByDebateId(debateId)
    }

    private fun handleSuccess(likeStatus: LikeStatus): UserType? {
        return when (likeStatus) {
            LikeStatus.LIKED_POSTER -> UserType.POSTER
            LikeStatus.LIKED_DEBATER -> UserType.DEBATER
            LikeStatus.LIKE_NOT_EXIST -> null
        }
    }

    private suspend fun getVentCard(debate: Debate): VentCard? {
        val ventCardResource = getSwipeCardUseCase.execute(debate.posterId, debate.swipeCardId)
        return ventCardResource.data.takeIf { ventCardResource.status == Status.SUCCESS }
    }

    private suspend fun getPosterInfo(debate: Debate): User? {
        val fetchPosterInfoState = getUserDetailsUseCase.execute(debate.posterId)
        return fetchPosterInfoState.data.takeIf { fetchPosterInfoState.status == Status.SUCCESS }
    }

    private suspend fun getDebaterInfo(debate: Debate): User? {
        val fetchDebaterInfoState = getUserDetailsUseCase.execute(debate.debaterId)
        return fetchDebaterInfoState.data.takeIf { fetchDebaterInfoState.status == Status.SUCCESS }
    }
}
