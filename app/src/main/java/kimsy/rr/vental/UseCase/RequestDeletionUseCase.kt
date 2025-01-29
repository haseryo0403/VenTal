package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.DeleteRequestData
import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.DebateRepository
import kimsy.rr.vental.data.repository.DeleteRequestRepository
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class RequestDeletionUseCase @Inject constructor(
    private val debateRepository: DebateRepository,
    private val ventCardRepository: VentCardRepository,
    private val deleteRequestRepository: DeleteRequestRepository,
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
        deletionRequestFlag: Boolean


    ): Resource<Unit> {

        return executeWithLoggingAndNetworkCheck {
            validateUserId(requesterId)
           val result = when(entityType) {
                EntityType.DEBATE -> {
                    if (ventCardId == null) {
                        Resource.failure()
                    } else {
                        requestDebateDeletion(
                            entityId,
                            entityType,
                            posterId,
                            ventCardId,
                            requesterId,
                            reasonInt,
                            deletionRequestFlag
                        )
                        Resource.success(Unit)
                    }
                }
                EntityType.VENTCARD -> {
                    requestVentCardDeletion(
                        entityId,
                        entityType,
                        posterId,
                        requesterId,
                        reasonInt,
                        deletionRequestFlag
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

    private suspend fun requestDebateDeletion(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        ventCardId: String,
        requesterId: String,
        reasonNumber: Int,
        deletionRequestFlag: Boolean
    ) {
        if (!deletionRequestFlag) {
            debateRepository.updateDeletionRequestFlag(entityId, ventCardId, posterId)
        }

        val requestData = DeleteRequestData(
                entityId = entityId,
                entityType = entityType,
                requesterId = requesterId,
                reasonNumber = reasonNumber
            )
        deleteRequestRepository.storeDeleteRequestData(requestData)
    }
    private suspend fun requestVentCardDeletion(
        entityId: String,
        entityType: EntityType,
        posterId: String,
        requesterId: String,
        reasonNumber: Int,
        deletionRequestFlag: Boolean
    ) {
        if (!deletionRequestFlag) {
            ventCardRepository.updateDeletionRequestFlag(entityId, posterId)
        }

        val requestData = DeleteRequestData(
                entityId = entityId,
                entityType = entityType,
                requesterId = requesterId,
                reasonNumber = reasonNumber
            )
        deleteRequestRepository.storeDeleteRequestData(requestData)
    }
}