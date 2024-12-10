package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.VentCardRepository
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class HandleLikeActionUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(
        userId: String,
        posterId: String,
        ventCardId: String
    ): Resource<Unit> {
        return try {
            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }
            withTimeout(10000L) {
                val isLiked = ventCardRepository.checkIfLiked(userId, ventCardId).getOrThrow()
                if (isLiked) {
                    ventCardRepository.disLikeVentCard(userId, ventCardId)
                } else {
                    ventCardRepository.likeVentCard(userId, posterId, ventCardId)
                }
            }
        } catch (e: Exception) {
            Log.e("HandleLikeActionUseCase", "Error handling like action: $e")
            Resource.failure(e.message)
        }
    }
}
