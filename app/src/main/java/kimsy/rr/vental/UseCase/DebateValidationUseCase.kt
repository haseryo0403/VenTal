package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import javax.inject.Inject

class DebateValidationUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun execute(debate: Debate): Result<Boolean> {
        return try {
            val countResult = debateRepository.getRelatedDebatesCount(debate)
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
