package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.data.repository.DebateRepository
import javax.inject.Inject

class DebateCreationUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val messageCreationUseCase: MessageCreationUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val networkUtils: NetworkUtils
) {
        suspend fun execute(text: String, ventCard: VentCardWithUser, debaterId: String, firstMessageImageURL: String?): Resource<DebateWithUsers> {
        return try {

            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }

            val debate = createDebateInstance(text, ventCard, debaterId, firstMessageImageURL)
            val createdDebateWithUsers = debateRepository.createDebate(debate).getOrThrow()
            messageCreationUseCase.execute(
                debate = debate,
                debateWithUsers = null,
                userId = debaterId,
                debateId = createdDebateWithUsers.debateId,
                text = text,
                messageImageURL = firstMessageImageURL
                )
            getDebateInfo(createdDebateWithUsers)
        } catch (e: Exception) {
            Resource.failure(e.message)
        }
    }

    suspend fun getDebateInfo(debateWithUsers: DebateWithUsers): Resource<DebateWithUsers> {
        return try {
            val debaterState = getUserDetailsUseCase.execute(debateWithUsers.debaterId)
            val posterState = getUserDetailsUseCase.execute(debateWithUsers.posterId)
            when {
                debaterState.status == Status.SUCCESS && posterState.status == Status.SUCCESS -> {
                    if (debaterState.data != null && posterState.data != null) {
                        val result = debateWithUsers.copy(
                            debaterName = debaterState.data.name,
                            debaterImageURL = debaterState.data.photoURL,
                            posterName = posterState.data.name,
                            posterImageURL = posterState.data.photoURL
                        )
                        Resource.success(result)
                    } else {
                        Resource.failure("ユーザーの情報の取得に失敗しました。")
                    }
                }
                else -> {
                    Resource.failure("ユーザーの情報の取得に失敗しました。")
                }
            }
        } catch (e: Exception) {
            Log.e("DCUC", "${e.message}")
            Resource.failure(e.message)
        }
    }

    private fun createDebateInstance(text: String, ventCard: VentCardWithUser, debaterId: String, firstMessageImageURL: String?): Debate {
        return Debate(
            swipeCardImageURL = ventCard.swipeCardImageURL,
            swipeCardId = ventCard.swipeCardId,
            posterId = ventCard.posterId,
            debaterId = debaterId,
            firstMessage = text,
            firstMessageImageURL = firstMessageImageURL
        )
    }

}
