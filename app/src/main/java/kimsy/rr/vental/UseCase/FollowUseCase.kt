package kimsy.rr.vental.UseCase

import com.google.firebase.firestore.FirebaseFirestore
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.FollowRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class FollowUseCase @Inject constructor(
    private val followRepository: FollowRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        fromUserId: String,
        toUserId: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            //DEbateVIEWと他人ページだけ
            //TODO repositoryでfollowの状況を取得して条件を分岐 OR UIでボタンを２種類用意するので、そこのonclickでVMのfollowとunfollowの関数を配置。
            //前者コードが完結。矛盾が起こりにくい。後者コードが多くなる。ただ、簡単。follow状況把握のクエリいらなくなるので早い。x
            //後者にする
            FirebaseFirestore.getInstance().runTransaction {  transaction ->
                followRepository.addUserIdToFollowingUserId(
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    transaction = transaction
                )
                followRepository.followerCountUp(
                    userId = toUserId,
                    transaction = transaction
                )
            }
            Resource.success(Unit)
        }
    }
}