package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.MessageRepository
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(posterId: String, ventCardId: String, debateId: String): Resource<List<Message>> {
        if (!networkUtils.isOnline()) {
            return Resource.failure("インターネットの接続を確認してください")
        }
        return messageRepository.fetchMessages(posterId, ventCardId, debateId)
    }
}