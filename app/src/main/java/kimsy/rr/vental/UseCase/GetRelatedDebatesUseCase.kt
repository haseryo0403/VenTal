package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject

class GetRelatedDebatesUseCase @Inject constructor(
    private val debateRepository: DebateRepository
) {
    suspend fun execute(ventCardWithUser: VentCardWithUser): Result<List<Debate>> {
        return try {
            debateRepository.fetchRelatedDebates(ventCardWithUser) // そのまま結果を返す
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
