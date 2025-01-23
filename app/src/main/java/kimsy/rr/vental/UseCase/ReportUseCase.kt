package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.ReportData
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.ReportRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class ReportUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val ventCardRepository: VentCardRepository,
    private val reportRepository: ReportRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        ventCardId: String?,
        requesterId: String,
        reasonInt: Int,
        reportFlag: Boolean
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            val result = when(entityType) {
                EntityType.DEBATE -> {
                    if (ventCardId == null) {
                        Resource.failure()
                    } else {
                        reportDebate(
                            entityId,
                            entityType,
                            posterId,
                            ventCardId,
                            requesterId,
                            reasonInt,
                            reportFlag
                        )
                        Resource.success(Unit)
                    }
                }
                EntityType.VENTCARD -> {
                    reportVentCard(
                        entityId,
                        entityType,
                        posterId,
                        requesterId,
                        reasonInt,
                        reportFlag
                    )
                    Resource.success(Unit)
                }
                EntityType.MESSAGE -> {
                    Resource.failure()
                }
            }
            result
        }
    }

    private suspend fun reportDebate(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        ventCardId: String,
        reporterId: String,
        reasonNumber: Int,
        reportFlag: Boolean
    ) {
        if (!reportFlag) {
            debateRepository.updateDebateReportFlag(entityId, ventCardId, posterId)
        }

        val reportData = ReportData(
            entityId = entityId,
            entityType = entityType,
            reporterId = reporterId,
            reasonNumber = reasonNumber
        )
        reportRepository.storeReportData(reportData)
    }

    private suspend fun reportVentCard(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        reporterId: String,
        reasonNumber: Int,
        reportFlag: Boolean
    ) {
        if (!reportFlag) {
            ventCardRepository.updateReportFlag(entityId, posterId)
        }

        val reportData = ReportData(
            entityId = entityId,
            entityType = entityType,
            reporterId = reporterId,
            reasonNumber = reasonNumber
        )
        reportRepository.storeReportData(reportData)
    }

    private suspend fun reportDebateMessage(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        ventCardId: String,
        reporterId: String,
        reasonNumber: Int,
        reportFlag: Boolean
    ) {
//        if (!reportFlag) {
//            debateRepository.updateDebateReportFlag(entityId, ventCardId, posterId)
//        }
//
//        val reportData = ReportData(
//            entityId = entityId,
//            entityType = entityType,
//            reporterId = reporterId,
//            reasonNumber = reasonNumber
//        )
//        reportRepository.storeReportData(reportData)
    }
}