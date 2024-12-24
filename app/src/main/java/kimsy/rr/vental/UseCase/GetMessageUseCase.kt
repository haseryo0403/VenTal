package kimsy.rr.vental.UseCase

import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {

    //TODO BaseUseCase使う。使いたい...
    suspend fun execute(posterId: String, ventCardId: String, debateId: String): Flow<Resource<List<Message>>>  = flow {
        try {
            // インターネット接続の確認
            val networkState = checkNetwork()
            if (networkState.status != Status.SUCCESS) {
                emit(Resource.failure("インターネットの接続を確認してください"))
                return@flow
            }

            messageRepository.observeMessages(posterId, ventCardId, debateId).collect {
                emit(Resource.success(it))
            }
        } catch (e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            emit(Resource.failure("エラーが発生しました"))
        }
    }

}