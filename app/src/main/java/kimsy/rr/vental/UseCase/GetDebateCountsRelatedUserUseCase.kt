package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject
class GetDebateCountsRelatedUserUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
) : BaseUseCase(networkUtils, logRepository) {

    suspend fun execute(userId: String): Resource<Int> {
        return executeWithLoggingAndNetworkCheck {
            val debateCounts = debateRepository.getDebatesCountRelatedUser(userId)

            Resource.success(debateCounts)
        }
    }
}
