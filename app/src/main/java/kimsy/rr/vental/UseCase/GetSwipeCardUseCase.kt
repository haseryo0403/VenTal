package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class GetSwipeCardUseCase @Inject constructor(
    private val ventCardRepository: VentCardRepository,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(posterId: String, ventCardId: String): Resource<VentCard>{
//        return try {
//            val result = ventCardRepository.fetchVentCard(posterId, ventCardId)
//            Result.success(result)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
        if (!networkUtils.isOnline()) {
            return Resource.failure("インターネットの接続を確認してください")
        }
        return ventCardRepository.fetchVentCard(posterId, ventCardId)
    }
}