package kimsy.rr.vental.UseCase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.DebateRepository
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class GetTimeLineItemsUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getSwipeCardUseCase: GetSwipeCardUseCase
) {
    suspend fun execute(lastVisible: DocumentSnapshot?): Resource<Pair<List<DebateItem>, DocumentSnapshot?>>  {
        return try {
            withTimeout(10000L) {
                val debatesState = debateRepository.fetch10Debates(lastVisible)

                when (debatesState.status) {
                    Status.SUCCESS -> {
                        val debates = debatesState.data?.first ?: return@withTimeout Resource.failure("討論データが空です。")
                        val timeLineItems = debates.mapNotNull { debate ->
                            val ventCard = getVentCard(debate)
                            val poster = getPosterInfo(debate)
                            val debater = getDebaterInfo(debate)

                            if (ventCard != null && poster != null && debater != null) {
                                DebateItem(debate, ventCard, poster, debater)
                            } else {
                                null // 失敗した場合はスキップ
                            }
                        }
                        val newLastVisible = debatesState.data.second
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
        } catch (e: Exception) {
            Log.e("GTLIUC", "exception: ${e.message}")
            Resource.failure("討論の取得に失敗しました。：${e.message}")
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