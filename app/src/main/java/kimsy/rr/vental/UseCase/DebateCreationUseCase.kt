package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class DebateCreationUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(text: String, ventCard: VentCard, debaterId: String, firstMessageImageURL: String?): Resource<Debate> {

        return executeWithLoggingAndNetworkCheck {
            val debate = createDebateInstance(text, ventCard, debaterId, firstMessageImageURL)

            debateRepository.createDebate(debate)
        }
    }

    private fun createDebateInstance(text: String, ventCard: VentCard, debaterId: String, firstMessageImageURL: String?): Debate {
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
