package kimsy.rr.vental.UseCase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class LoadVentCardsUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository
) {
    suspend fun execute(
        userId: String,
        lastVisible: DocumentSnapshot?
    ): Result<Pair<List<VentCardWithUser>, DocumentSnapshot?>> {
        return try {
            val likedVentCard = ventCardRepository.fetchLikedVentCardIds(userId)
            val debatingVentCard = ventCardRepository.fetchDebatingVentCardIds(userId)
            ventCardRepository.getVentCardsWithUser(userId, likedVentCard, debatingVentCard, lastVisible)
        } catch (e: Exception) {
            Log.e("LoadVentCardsUseCase", "Error loading vent cards: $e")
            Result.failure(e)
        }
    }
}
