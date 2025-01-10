package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetDebatesRelatedUserUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        lastVisible: DocumentSnapshot?,
        userId: String
    ): Resource<Pair<List<DebateItem>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            val result = debateRepository.fetch10DebatesRelatedUser(userId, lastVisible)
            val debates = result.first
            val newLastVisible = result.second
            val relatedDebateItems = coroutineScope {
                debates.map { debate ->
                    async {
                        generateDebateItemUseCase.execute(debate, userId)
                    }
                }.awaitAll().filterNotNull()

            }

            Resource.success(Pair(relatedDebateItems, newLastVisible))
        }
    }
}