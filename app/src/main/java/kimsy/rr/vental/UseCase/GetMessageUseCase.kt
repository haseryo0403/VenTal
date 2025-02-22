package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(posterId: String, ventCardId: String, debateId: String): Flow<Resource<List<Message>>> {
        return executeFlowWithLoggingAndNetworkCheck {
            messageRepository.observeMessages(posterId, ventCardId, debateId)
                .map { messages ->
                    Resource.success(messages)
                }
        }
    }
}