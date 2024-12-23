package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

//TODO change to resource
class AddDebatingSwipeCardUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(debaterId: String, swipeCardId: String): Result<Unit> {
        return try {
            debateRepository.addDebatingSwipeCard(debaterId, swipeCardId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ADSCUC", "討論IDをユーザーに保持失敗")
            Result.failure(e)
        }
    }
}