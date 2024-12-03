package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.repository.MessageRepository
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(debateWithUsers: DebateWithUsers): Result<List<Message>> {
        return messageRepository.fetchMessages(debateWithUsers.posterId, debateWithUsers.swipeCardId, debateWithUsers.debateId)
    }
}