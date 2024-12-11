package kimsy.rr.vental.UseCase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.data.repository.VentCardRepository
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class LoadVentCardsUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    private val networkUtils: NetworkUtils
){
    suspend fun execute(
        userId: String,
        lastVisible: DocumentSnapshot?
    ): Resource<Pair<List<VentCardWithUser>, DocumentSnapshot?>> {
        return try {
            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }
            withTimeout(10000L) {
                val likedVentCard = ventCardRepository.fetchLikedVentCardIds(userId)
                val debatingVentCard = ventCardRepository.fetchDebatingVentCardIds(userId)

                if (likedVentCard.status == Status.FAILURE || debatingVentCard.status == Status.FAILURE) {
                    return@withTimeout Resource.failure("Failed to fetch necessary data for vent cards.")
                }

                val result = ventCardRepository.getVentCardsWithUser(
                    userId,
                    likedVentCard.data ?: emptyList(),
                    debatingVentCard.data ?: emptyList(),
                    lastVisible
                )
                result
            }
        } catch (e: Exception) {
            Log.e("LoadVentCardsUseCase", "Error loading vent cards: $e")
            Resource.failure("Error loading vent cards: ${e.message}")
        }
    }
}
