package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class GetSwipeCardUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
) {
    //UseCaseからのみ使用されるのでここではtry catchしない
    suspend fun execute(posterId: String, ventCardId: String): VentCard{
        return ventCardRepository.fetchVentCard(posterId, ventCardId)
    }
}