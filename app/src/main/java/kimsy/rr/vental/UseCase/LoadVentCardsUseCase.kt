package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCardItem
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class LoadVentCardsUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        currentUserId: String,
        lastVisible: DocumentSnapshot?
    ): Resource<Pair<List<VentCardItem>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            val likedVentCard = ventCardRepository.fetchLikedVentCardIds(currentUserId)
            val debatingVentCard = ventCardRepository.fetchDebatingVentCardIds(currentUserId)
            val ventCardItem = ventCardRepository.getVentCardItems(
                currentUserId,
                likedVentCard,
                debatingVentCard,
                lastVisible
            )
            Resource.success(ventCardItem)
        }
    }
}
