package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject

class DebateValidationUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun execute(posterId: String, swipeCardId: String): Result<Boolean> {
        return try {
            val countResult = debateRepository.getRelatedDebatesCount(posterId, swipeCardId)
            countResult.fold(
                onSuccess = {count->
                    if (count <= 2) {
                        Result.success(true)
                    } else {
                        Result.success(false)
                    }
                },
                onFailure = {exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
