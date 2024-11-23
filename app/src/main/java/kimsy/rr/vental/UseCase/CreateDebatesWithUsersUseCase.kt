package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.User
import javax.inject.Inject

class CreateDebatesWithUsersUseCase @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase
) {
    suspend fun execute(debates: List<Debate>): List<DebateWithUsers> {
        return debates.map { debate ->
            val debater = getUserDetailsUseCase.execute(debate.debaterId).getOrThrow()
            val poster = getUserDetailsUseCase.execute(debate.posterId).getOrThrow()
            createDebateWithUsersInstance(debate, debater, poster)
        }
    }
}

private fun createDebateWithUsersInstance(debate: Debate, debater: User, poster: User): DebateWithUsers {
    return DebateWithUsers(
        swipeCardImageURL = debate.swipeCardImageURL,
        swipeCardId = debate.swipeCardId,
        posterId = debate.posterId,
        posterName = poster.name,
        posterImageURL = poster.photoURL,
        posterLikeCount = debate.posterLikeCount,
        debaterId = debate.debaterId,
        debaterName = debater.name,
        debaterImageURL = debater.photoURL,
        debaterLikeCount = debate.debaterLikeCount,
        firstMessage = debate.firstMessage,
        firstMessageImageURL = debate.firstMessageImageURL,
        //TODO 討論作成時間
    )
}