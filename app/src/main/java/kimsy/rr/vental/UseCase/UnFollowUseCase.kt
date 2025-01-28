package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.FollowRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class UnFollowUseCase @Inject constructor(
    private val followRepository: FollowRepository,
    private val db: FirebaseFirestore,

    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        fromUserId: String,
        toUserId: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            db.runTransaction { transaction ->
                followRepository.deleteUserIdFromFollowingUserId(
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    transaction = transaction
                )
                followRepository.followerCountDown(
                    userId = toUserId,
                    transaction = transaction
                )
            }
            Resource.success(Unit)
        }
    }
}