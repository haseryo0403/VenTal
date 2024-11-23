package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.MessageRepository
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject

class MessageCreationUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend fun execute(
        debate: Debate?,
        debateWithUsers: DebateWithUsers?,
        userId: String,
        debateId: String,
        text: String,
        messageImageURL: String?
    ): Result<Unit> {
        return try {
            val userType = when {
                debate != null -> {
                    if (userId == debate.posterId) UserType.POSTER else UserType.DEBATER
                }
                debateWithUsers != null -> {
                    if (userId == debateWithUsers.posterId) UserType.POSTER else UserType.DEBATER
                }
                else -> throw IllegalArgumentException("Either debate or debateWithUsers must be provided.")
            }

            val message = createMessageInstance(text, messageImageURL, userType)
            messageRepository.sendMessage(debate?.posterId ?: debateWithUsers?.posterId!!, debate?.swipeCardId ?: debateWithUsers?.swipeCardId!!, debateId, message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun createMessageInstance(text: String, messageImageURL: String?, userType: UserType) :Message{
        return Message(
            userType = userType,
            text = text,
            imageURL = messageImageURL
        )
    }
}