package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.MessageRepository
import javax.inject.Inject

class MessageCreationUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        debate: Debate?,
        debateWithUsers: DebateWithUsers?,
        userId: String,
        debateId: String,
        text: String,
        messageImageURL: String?
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
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
            Resource.success(Unit)
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