package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LoadLikedDebateItemsUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        likedDebateIds: List<String>,
        currentUserId: String
    ): Resource<List<DebateItem>> {
        return executeWithLoggingAndNetworkCheck {
            val debates = debateRepository.fetch10DebatesByIdList(likedDebateIds)
            val debateItems = coroutineScope {
                debates.map { debate ->
                    async {
                        generateDebateItemUseCase.execute(debate, currentUserId)
                    }
                }.awaitAll().filterNotNull()
            }

            Resource.success(debateItems)
        }
    }
}