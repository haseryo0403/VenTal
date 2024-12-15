package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.data.repository.DebateRepository
import javax.inject.Inject

class DebateCreationUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val networkUtils: NetworkUtils
) {
        suspend fun execute(text: String, ventCard: VentCardWithUser, debaterId: String, firstMessageImageURL: String?): Resource<Debate> {

            return if (!networkUtils.isOnline()) {
                Resource.failure("インターネットの接続を確認してください")
            } else {
                val debate = createDebateInstance(text, ventCard, debaterId, firstMessageImageURL)

                debateRepository.createDebate(debate)
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
