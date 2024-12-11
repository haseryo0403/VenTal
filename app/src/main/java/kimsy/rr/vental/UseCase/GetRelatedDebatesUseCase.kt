package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.data.repository.DebateRepository
import javax.inject.Inject

class GetRelatedDebatesUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(ventCardWithUser: VentCardWithUser): Resource<List<DebateWithUsers>> {
        return try {

            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }

            val relatedDebates = debateRepository.fetchRelatedDebates(ventCardWithUser) // そのまま結果を返す
            when (relatedDebates.status) {
                Status.SUCCESS -> {
                    val debateWithUsers = relatedDebates.data?.let { debates->
                        getDebateUsers(debates)
                    }?: emptyList()
                    Resource.success(debateWithUsers)
                }
                Status.FAILURE -> {
                    Resource.failure(relatedDebates.message)
                }
                else -> {Resource.idle()}
            }
        } catch (e: Exception) {
            Log.e("GRDUC", "${e.message}")
            Resource.failure(e.message)
        }
    }

    private suspend fun getDebateUsers(debates: List<Debate>): List<DebateWithUsers> {
        return debates.map { debate ->
            val debater = getUserDetailsUseCase.execute(debate.debaterId).getOrThrow()
            val poster = getUserDetailsUseCase.execute(debate.posterId).getOrThrow()
            createDebateWithUsersInstance(debate, debater, poster)
        }
    }

    private fun createDebateWithUsersInstance(debate: Debate, debater: User, poster: User): DebateWithUsers {
        return DebateWithUsers(
            swipeCardImageURL = debate.swipeCardImageURL,
            swipeCardId = debate.swipeCardId,
            posterId = debate.posterId,
            posterName = poster.name,
            posterImageURL = poster.photoURL,
            posterLikeCount = debate.posterLikeCount,
            debaterId = debate.debaterId,
            debaterName = debater.name,
            debaterImageURL = debater.photoURL,
            debaterLikeCount = debate.debaterLikeCount,
            firstMessage = debate.firstMessage,
            firstMessageImageURL = debate.firstMessageImageURL,
            //TODO 討論作成時間
        )
    }
}

