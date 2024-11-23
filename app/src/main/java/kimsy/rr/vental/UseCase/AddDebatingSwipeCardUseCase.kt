package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import javax.inject.Inject

class AddDebatingSwipeCardUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun execute(debaterId: String, swipeCardId: String): Result<Unit> {
        return try {
            debateRepository.addDebatingSwipeCardUseCase(debaterId, swipeCardId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ADSCUC", "討論IDをユーザーに保持失敗")
            Result.failure(e)
        }
    }
}