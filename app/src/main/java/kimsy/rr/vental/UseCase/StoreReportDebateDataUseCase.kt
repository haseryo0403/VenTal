package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.ReportData
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.ReportRepository
import javax.inject.Inject

class StoreReportDebateDataUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        debateId: String,
        reporterId: String,
        reason: Int
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val reportData = ReportData(
                contentId = debateId,
                reporterId = reporterId,
                reason = reason
            )
            reportRepository.storeReportDebateData(reportData)
            Resource.success(Unit)
        }
    }
}