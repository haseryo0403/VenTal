package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class HandleLikeActionUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository
) {
    suspend fun execute(
        userId: String,
        posterId: String,
        ventCardId: String
    ): Result<Unit> {
        return try {
            val isLiked = ventCardRepository.checkIfLiked(userId, ventCardId).getOrThrow()
            if (isLiked) {
                ventCardRepository.disLikeVentCard(userId, ventCardId)
            } else {
                ventCardRepository.likeVentCard(userId, posterId, ventCardId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("HandleLikeActionUseCase", "Error handling like action: $e")
            Result.failure(e)
        }
    }
}
