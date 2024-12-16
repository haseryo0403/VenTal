package kimsy.rr.vental.UseCase

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
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
        context: Context,
        fromUserId: String,
        posterId: String,
        debateId: String,
        ventCardId: String,
        userType: UserType
    ): Resource<Unit> {
        return try {
            if (!networkUtils.isOnline()) {
                return Resource.failure("インターネットの接続を確認してください")
            }
            withTimeout(10000L) {

                val likeState = debateRepository
                    .fetchLikeState(
                        debateId = debateId,
                        fromUserId = fromUserId
                    )

                val debateContext = DebateContext(fromUserId, posterId, debateId, userType, ventCardId)

                return@withTimeout when (likeState.status) {
                    Status.SUCCESS -> likeState.data?.let { handleSuccess(it, debateContext) }
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
        likeStatus: LikeStatus,
        debateContext: DebateContext
    ): Resource<Unit> {
        val debateLikeData = DebateLikeData(userType = debateContext.userType)
        return try {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                when (likeStatus) {
                    LikeStatus.LIKE_NOT_EXIST -> {
                        // LIKE_NOT_EXISTの場合は、いいねを追加
                        handleLikeNotExist(debateContext, debateLikeData, transaction)
                    }

                    LikeStatus.LIKED_POSTER -> {
                        // LIKED_POSTERの場合の処理
//                        handleLikedPoster(debateContext, transaction)
                    }

                    LikeStatus.LIKED_DEBATER -> {
                        // LIKED_DEBATERの場合の処理
//                        handleLikedDebater(debateContext, transaction)
                    }
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
        context: DebateContext,
        debateLikeData: DebateLikeData,
        transaction: Transaction
    ) {
        debateRepository.setLikeDebateToUser(context.fromUserId, context.debateId, debateLikeData, transaction)
        debateRepository.likeCountUp(context.posterId, context.ventCardId, context.debateId, context.userType, transaction)
    }

//    private fun handleLikedPoster(
//
//    ):Resource<Unit> {
//        // check usertype if its same dislike, if not add like, countup, count down
//    }
//
//    private fun handleLikedDebater():Resource<Unit> {
//        // check usertype if its same dislike, if not add like, countup, count down
//
//    }


}


data class DebateContext(
    val fromUserId: String,
    val posterId: String,
    val debateId: String,
    val userType: UserType,
    val ventCardId: String
)

