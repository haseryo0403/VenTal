package kimsy.rr.vental.UseCase

import kimsy.rr.vental.R
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.UserPageData
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject
class GetUserPageDataUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
) : BaseUseCase(networkUtils, logRepository) {

    suspend fun execute(userId: String, isMyPage: Boolean): Resource<UserPageData> {
        return executeWithLoggingAndNetworkCheck {
            val debatesCount = debateRepository.getDebatesCountRelatedUser(userId)

            val user = if (!isMyPage) {
                val userInfoState = getUserDetailsUseCase.execute(userId)
                if (userInfoState.status != Status.SUCCESS) {
                    return@executeWithLoggingAndNetworkCheck Resource.failure(
                        userInfoState.message ?: "${R.string.no_user_found}"
                    )
                }
                userInfoState.data
                    ?: return@executeWithLoggingAndNetworkCheck Resource.failure("${R.string.no_user_found}")
            } else {
                null // isMyPage が true の場合、user は不要
            }

            // TODO: フォロー機能実装時に followerCount を適切に取得
            val userPageData = UserPageData(
                user = user,
                debatesCount = debatesCount,
                followerCount = 10
            )

            Resource.success(userPageData)
        }
    }
}
