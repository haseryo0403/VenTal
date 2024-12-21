package kimsy.rr.vental.UseCase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateLikeData
import kimsy.rr.vental.data.LikeStatus
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class HandleDebateLikeActionUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun execute(
        fromUserId: String,
        debateItem: DebateItem,
        userType: UserType
    ): Resource<DebateItem> {
        return executeWithLogging {
            withTimeout(10000L) {

                val likeState = debateRepository
                    .fetchLikeState(
                        debateId = debateItem.debate.debateId,
                        fromUserId = fromUserId
                    )

                val debateContext =
                    likeState.data?.let { DebateContext(fromUserId, it, debateItem, userType) }

                when (likeState.status) {
                    Status.SUCCESS -> debateContext?.let { handleSuccess(it) } ?: Resource.failure("無効なデータ")
                    Status.FAILURE -> Resource.failure(likeState.message)
                    else -> Resource.failure("無効なステータスが返されました")
                }
            }

        }


//        return try {
//            if (!networkUtils.isOnline()) {
//                return Resource.failure("インターネットの接続を確認してください")
//            }
//            withTimeout(10000L) {
//
//                val likeState = debateRepository
//                    .fetchLikeState(
//                        debateId = debateItem.debate.debateId,
//                        fromUserId = fromUserId
//                    )
//
//                val debateContext =
//                    likeState.data?.let { DebateContext(fromUserId, it, debateItem, userType) }
//
//                when (likeState.status) {
//                    Status.SUCCESS -> debateContext?.let { handleSuccess(it) } ?: Resource.failure("無効なデータ")
//                    Status.FAILURE -> Resource.failure(likeState.message)
//                    else -> Resource.failure("無効なステータスが返されました")
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("HandleLikeActionUseCase", "Error handling like action: $e")
//            Resource.failure(e.message)
//        }


    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun handleSuccess(
        debateContext: DebateContext
    ): Resource<DebateItem> {
        val debateLikeData = DebateLikeData(userType = debateContext.userType)
        return try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                when (debateContext.likeStatus) {
                    LikeStatus.LIKE_NOT_EXIST -> handleLikeNotExist(debateContext, debateLikeData, transaction)
                    LikeStatus.LIKED_POSTER -> handleLikeChange(debateContext, debateLikeData, transaction, UserType.DEBATER, UserType.POSTER)
                    LikeStatus.LIKED_DEBATER -> handleLikeChange(debateContext, debateLikeData, transaction, UserType.POSTER, UserType.DEBATER)
                }
            }
            val updatedDebateItem = createUpdatedDebateItem(debateContext.debateItem, debateContext.userType)
            // トランザクションが成功した場合
//            Resource.success(Unit)
            Resource.success(updatedDebateItem)
        } catch (e: Exception) {
            // エラーが発生した場合
            Log.e("HandleLikeActionUseCase", "Error during transaction: $e")
            Resource.failure(e.message)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun handleLikeNotExist(
        debateContext: DebateContext,
        debateLikeData: DebateLikeData,
        transaction: Transaction
    ) {
        debateRepository.setLikeDebateToUser(debateContext.fromUserId, debateContext.debateItem.debate.debateId, debateLikeData, transaction)
        debateRepository.likeCountUp(debateContext.debateItem.debate.posterId, debateContext.debateItem.debate.swipeCardId, debateContext.debateItem.debate.debateId, debateContext.userType, transaction)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun handleLikeChange(
        debateContext: DebateContext,
        debateLikeData: DebateLikeData,
        transaction: Transaction,
        likeableUserType: UserType,
        dislikeableUserType: UserType
    ) {
        if (debateContext.userType == dislikeableUserType) {
            // いいねしようとしているユーザータイプが同じなのでdislike
            debateRepository.deleteLikeDebateFromUser(debateContext.fromUserId, debateContext.debateItem.debate.debateId, transaction)
            debateRepository.likeCountDown(debateContext.debateItem.debate.posterId, debateContext.debateItem.debate.swipeCardId, debateContext.debateItem.debate.debateId, dislikeableUserType, transaction)
        } else {
            debateRepository.setLikeDebateToUser(debateContext.fromUserId, debateContext.debateItem.debate.debateId, debateLikeData, transaction)
            debateRepository.likeCountUp(debateContext.debateItem.debate.posterId, debateContext.debateItem.debate.swipeCardId, debateContext.debateItem.debate.debateId, likeableUserType, transaction)
            debateRepository.likeCountDown(debateContext.debateItem.debate.posterId, debateContext.debateItem.debate.swipeCardId, debateContext.debateItem.debate.debateId, dislikeableUserType, transaction)
        }
    }

    //TODO 上で使う　ViewModelではindexらへんからやってもらう
    private fun createUpdatedDebateItem(debateItem: DebateItem, userType: UserType): DebateItem {
        val newPosterLikeCount = when (debateItem.likedUserType) {
            UserType.POSTER -> debateItem.debate.posterLikeCount - 1
            //つまり　いいね済み　-> いいねしたいユーザータイプがposter　else　debater
            UserType.DEBATER -> if (userType == UserType.POSTER) debateItem.debate.posterLikeCount + 1 else debateItem.debate.posterLikeCount
            null -> if (userType == UserType.POSTER) debateItem.debate.posterLikeCount + 1 else debateItem.debate.posterLikeCount
        }

        val newDebaterLikeCount = when (debateItem.likedUserType) {
            UserType.DEBATER -> debateItem.debate.debaterLikeCount - 1
            //つまり　いいね済み　-> いいねしたいユーザータイプがdebater　else　poster
            UserType.POSTER -> if (userType == UserType.DEBATER) debateItem.debate.debaterLikeCount + 1 else debateItem.debate.debaterLikeCount
            null -> if (userType == UserType.DEBATER) debateItem.debate.debaterLikeCount + 1 else debateItem.debate.debaterLikeCount
        }

        val newLikeUserType = when (debateItem.likedUserType) {
            userType -> null // 取り消し
            else -> userType // 新規または変更
        }

        return debateItem.copy(
            debate = debateItem.debate.copy(
                posterLikeCount = newPosterLikeCount,
                debaterLikeCount = newDebaterLikeCount
            ),
            likedUserType = newLikeUserType
        )
    }
}

data class DebateContext(
    val fromUserId: String,
    val likeStatus: LikeStatus,
    val debateItem: DebateItem,
    val userType: UserType,
)
