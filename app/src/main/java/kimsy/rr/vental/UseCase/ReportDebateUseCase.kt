package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class ReportDebateUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        debateId: String,
        swipeCardId: String,
        posterId: String
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            debateRepository.updateDebateReportFlag(debateId, swipeCardId, posterId)
            Resource.success(Unit)
        }
    }

}