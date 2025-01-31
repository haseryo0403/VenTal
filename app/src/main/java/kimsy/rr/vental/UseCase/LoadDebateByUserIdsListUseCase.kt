package kimsy.rr.vental.UseCase

import com.google.firebase.Timestamp
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LoadDebateByUserIdsListUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        userIds: List<String>,
        currentUserId: String,
        startAfterDate: Timestamp,
        endAtDate: Timestamp
    ): Resource<List<DebateItem>> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(currentUserId)
            val debates = debateRepository.fetchDebateByUserIdList(
                userIds, startAfterDate, endAtDate
            )
            val debateItem = coroutineScope {
                debates.map { debate ->
                    async {
                        generateDebateItemUseCase.execute(debate, currentUserId)
                    }
                }
            }.awaitAll().filterNotNull()

            Resource.success(debateItem)
        }
    }
}