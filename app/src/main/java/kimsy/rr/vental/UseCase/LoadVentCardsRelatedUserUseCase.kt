package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class LoadVentCardsRelatedUserUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        userId: String,
        lastVisible: DocumentSnapshot?,
    ): Resource<Pair<List<VentCard>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(userId)
            val result = ventCardRepository.fetchUserVentCards(userId, lastVisible)
            val ventCards = result.first
            val newLastVisible = result.second

            Resource.success(Pair(ventCards, newLastVisible))
        }
    }
}