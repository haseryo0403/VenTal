package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetPopularTimeLineItemsUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
    ): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        lastVisible: DocumentSnapshot?,
        currentUser: User
    ): Resource<Pair<List<DebateItem>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            val result = debateRepository.fetchPopular10Debates(lastVisible)

            val timeLineItems = generateDebateItem(result.first, currentUser.uid)

            val newLastVisible = result.second

            Resource.success(Pair(timeLineItems, newLastVisible))
        }
    }

    // 並列処理を行う関数
    private suspend fun generateDebateItem(
        debates: List<Debate>,
        currentUserUid: String
    ): List<DebateItem> {
        return coroutineScope {
            debates.map { debate ->
                async {
                    generateDebateItemUseCase.execute(debate, currentUserUid)
                }
            }.awaitAll()
                .filterNotNull()
        }
    }
}