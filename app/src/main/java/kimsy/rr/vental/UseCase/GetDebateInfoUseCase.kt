package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.User
import javax.inject.Inject

class GetDebateInfoUseCase @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase

){
    suspend fun execute(debateWithUsers: DebateWithUsers): Result<DebateWithUsers> {
        return try {
            val debater = getUserDetailsUseCase.execute(debateWithUsers.debaterId).getOrThrow()
            val poster = getUserDetailsUseCase.execute(debateWithUsers.posterId).getOrThrow()
            val result = debateWithUsers.copy(
                debaterName = debater.name,
                debaterImageURL = debater.photoURL,
                posterName = poster.name,
                posterImageURL = poster.photoURL
                )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}