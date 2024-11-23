package kimsy.rr.vental.UseCase

import android.net.Uri
import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import java.net.URL
import javax.inject.Inject

class DebateCreationUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun executes(debate: Debate): Result<Unit> {
        return try {
            debateRepository.createDebate(debate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun execute(text: String, ventCard: VentCardWithUser, debaterId: String, firstMessageImageURL: String?): Result<Unit> {
        return try {
            val debate = createDebateInstance(text, ventCard, debaterId, firstMessageImageURL)
            debateRepository.createDebate(debate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
