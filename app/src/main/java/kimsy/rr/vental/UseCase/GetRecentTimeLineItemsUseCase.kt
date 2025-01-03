package kimsy.rr.vental.UseCase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.LikeStatus
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class GetRecentTimeLineItemsUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val generateDebateItemUseCase: GenerateDebateItemUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
    ): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        lastVisible: DocumentSnapshot?,
        currentUser: User
    ): Resource<Pair<List<DebateItem>, DocumentSnapshot?>> {
        return executeWithLoggingAndNetworkCheck {
            withTimeout(10000L) {
                val debatesState = debateRepository.fetch10Debates(lastVisible)

                when (debatesState.status) {
                    Status.SUCCESS -> {
                        val startTime = System.currentTimeMillis()
                        val debates = debatesState.data?.first
                            ?: return@withTimeout Resource.failure("討論データが空です。")

                        val timeLineItems = generateDebateItem(debates, currentUser.uid)


                        val newLastVisible = debatesState.data.second

                        val endTime = System.currentTimeMillis()
                        Log.d("Performance", "Firestore クエリ時間: ${endTime - startTime}ms")

                        Resource.success(Pair(timeLineItems, newLastVisible))
                    }

                    Status.FAILURE -> {
                        Log.e("GTLIUC", "failure")
                        Resource.failure("討論の取得に失敗しました")
                    }

                    else -> {
                        Log.e("GTLIUC", "その他")
                        Resource.failure("予期しないステータス: ${debatesState.status}")
                    }
                }
            }
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

    private fun handleSuccess(likeStatus: LikeStatus): UserType? {
        return when (likeStatus) {
            LikeStatus.LIKED_POSTER -> UserType.POSTER
            LikeStatus.LIKED_DEBATER -> UserType.DEBATER
            LikeStatus.LIKE_NOT_EXIST -> null
        }
    }

    private suspend fun getVentCard(debate: Debate): VentCard? {
        val ventCardResource = getSwipeCardUseCase.execute(debate.posterId, debate.swipeCardId)
        return when (ventCardResource.status) {
            Status.SUCCESS-> {
                ventCardResource.data
            }

            else-> {
                null
            }
        }
    }

    private suspend fun getPosterInfo(debate: Debate): User? {
        val fetchPosterInfoState = getUserDetailsUseCase.execute(debate.posterId)
        return when (fetchPosterInfoState.status) {
            Status.SUCCESS -> {
                fetchPosterInfoState.data
            }

            else -> {
                null
            }
        }
    }

    private suspend fun getDebaterInfo(debate: Debate): User? {
        val fetchDebaterInfoState = getUserDetailsUseCase.execute(debate.debaterId)
        return when (fetchDebaterInfoState.status) {
            Status.SUCCESS -> {
                fetchDebaterInfoState.data
            }
            else -> {
                null
            }
        }
    }
}