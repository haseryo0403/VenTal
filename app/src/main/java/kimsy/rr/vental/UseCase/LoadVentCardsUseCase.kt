package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
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

            if (likedVentCard.status == Status.FAILURE || debatingVentCard.status == Status.FAILURE) {
                Resource.failure("Failed to fetch necessary data for vent cards.")
            } else {
                val ventCardItem = ventCardRepository.getVentCardItems(
                    currentUserId,
                    likedVentCard.data ?: emptyList(),
                    debatingVentCard.data ?: emptyList(),
                    lastVisible
                )
                Resource.success(ventCardItem)
            }
        }
    }

//    suspend fun execute(
//        userId: String,
//        lastVisible: DocumentSnapshot?
//    ): Resource<Pair<List<VentCardWithUser>, DocumentSnapshot?>> {
//        return try {
//            if (!networkUtils.isOnline()) {
//                return Resource.failure("インターネットの接続を確認してください")
//            }
//            withTimeout(10000L) {
//                val likedVentCard = ventCardRepository.fetchLikedVentCardIds(userId)
//                val debatingVentCard = ventCardRepository.fetchDebatingVentCardIds(userId)
//
//                if (likedVentCard.status == Status.FAILURE || debatingVentCard.status == Status.FAILURE) {
//                    return@withTimeout Resource.failure("Failed to fetch necessary data for vent cards.")
//                }
//
//                val result = ventCardRepository.getVentCardsWithUser(
//                    userId,
//                    likedVentCard.data ?: emptyList(),
//                    debatingVentCard.data ?: emptyList(),
//                    lastVisible
//                )
//                result
//            }
//        } catch (e: Exception) {
//            Log.e("LoadVentCardsUseCase", "Error loading vent cards: $e")
//            Resource.failure("Error loading vent cards: ${e.message}")
//        }
//    }


}
