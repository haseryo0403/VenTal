package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class GetSwipeCardUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository
) {
    suspend fun execute(posterId: String, ventCardId: String): Result<VentCard?>{
        return try {
            val result = ventCardRepository.fetchVentCard(posterId, ventCardId)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}