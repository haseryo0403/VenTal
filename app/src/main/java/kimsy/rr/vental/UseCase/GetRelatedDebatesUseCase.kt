package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetRelatedDebatesUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {

    suspend fun execute(
        ventCard: VentCard
    ): Resource<List<DebateItem>> {
        return executeWithLoggingAndNetworkCheck {
            val currentUserId = User.CurrentUserShareModel.getCurrentUserFromModel()?.uid
                ?: return@executeWithLoggingAndNetworkCheck Resource.failure()
            val relatedDebates = debateRepository.fetchRelatedDebates(ventCard)
            val debateItems = coroutineScope {
                relatedDebates.map { debate ->
                    async {
                        generateDebateItemUseCase.execute(debate, currentUserId)
                    }
                }.awaitAll().filterNotNull()
            }
            Resource.success(debateItems)
        }
    }
}

