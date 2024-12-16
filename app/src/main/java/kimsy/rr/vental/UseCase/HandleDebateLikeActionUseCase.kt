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
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class HandleDebateLikeActionUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val networkUtils: NetworkUtils
) {
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun execute(
        fromUserId: String,
        debateItem: DebateItem,
        userType: UserType
    ): Resource<Unit> {
        return try {
            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }
            withTimeout(10000L) {

                val likeState = debateRepository
                    .fetchLikeState(
                        debateId = debateItem.debate.debateId,
                        fromUserId = fromUserId
                    )

                val debateContext =
                    likeState.data?.let { DebateContext(fromUserId, it, debateItem, userType) }

                return@withTimeout when (likeState.status) {
                    Status.SUCCESS -> debateContext?.let { handleSuccess(it) }
                    Status.FAILURE -> Resource.failure(likeState.message)
                    else -> Resource.failure("無効なステータスが返されました")
                }
            }
            Resource.success(Unit)
        } catch (e: Exception) {
            Log.e("HandleLikeActionUseCase", "Error handling like action: $e")
            Resource.failure(e.message)
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun handleSuccess(
        debateContext: DebateContext
    ): Resource<Unit> {
        val debateLikeData = DebateLikeData(userType = debateContext.userType)
        return try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                when (debateContext.likeStatus) {
                    LikeStatus.LIKE_NOT_EXIST -> handleLikeNotExist(debateContext, debateLikeData, transaction)
                    LikeStatus.LIKED_POSTER -> handleLikeChange(debateContext, debateLikeData, transaction, UserType.DEBATER, UserType.POSTER)
                    LikeStatus.LIKED_DEBATER -> handleLikeChange(debateContext, debateLikeData, transaction, UserType.POSTER, UserType.DEBATER)
                }
            }
            // トランザクションが成功した場合
            Resource.success(Unit)
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
}

data class DebateContext(
    val fromUserId: String,
    val likeStatus: LikeStatus,
    val debateItem: DebateItem,
    val userType: UserType,
)
