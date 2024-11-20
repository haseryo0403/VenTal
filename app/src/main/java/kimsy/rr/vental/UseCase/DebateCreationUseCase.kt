package kimsy.rr.vental.UseCase

import android.net.Uri
import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject

class DebateCreationUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun execute(debate: Debate): Result<Unit> {
        return try {
            debateRepository.createDebate(debate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
