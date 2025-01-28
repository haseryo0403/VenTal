package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class GetRelatedDebatesUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {

    suspend fun execute(ventCard: VentCard): Resource<List<DebateWithUsers>> {
        return executeWithLoggingAndNetworkCheck {
            val relatedDebates = debateRepository.fetchRelatedDebates(ventCard) // そのまま結果を返す
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
        }
    }
//    suspend fun execute(ventCard: VentCard): Resource<List<DebateWithUsers>> {
//        return try {
//
//            if (!networkUtils.isOnline()) {
//                return Resource.failure("インターネットの接続を確認してください")
//            }
//
//            val relatedDebates = debateRepository.fetchRelatedDebates(ventCard) // そのまま結果を返す
//            when (relatedDebates.status) {
//                Status.SUCCESS -> {
//                    val debateWithUsers = relatedDebates.data?.let { debates->
//                        getDebateUsers(debates)
//                    }?: emptyList()
//                    Resource.success(debateWithUsers)
//                }
//                Status.FAILURE -> {
//                    Resource.failure(relatedDebates.message)
//                }
//                else -> {Resource.idle()}
//            }
//        } catch (e: Exception) {
//            Log.e("GRDUC", "${e.message}")
//            Resource.failure(e.message)
//        }
//    }

    private suspend fun getDebateUsers(debates: List<Debate>): List<DebateWithUsers> {
        return debates.mapNotNull { debate ->
            val debaterState = getUserDetailsUseCase.execute(debate.debaterId)
            val posterState = getUserDetailsUseCase.execute(debate.posterId)

            if (debaterState.status == Status.SUCCESS && posterState.status == Status.SUCCESS) {
                val debater = debaterState.data
                val poster = posterState.data

                if (debater != null && poster != null) {
                    createDebateWithUsersInstance(debate, debater, poster)
                } else {
                    null // データが null の場合はスキップ
                }
            } else {
                null // ステータスが成功でない場合はスキップ
            }
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

