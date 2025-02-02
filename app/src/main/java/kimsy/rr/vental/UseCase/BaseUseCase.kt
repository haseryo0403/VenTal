package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.ErrorLog
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.repository.LogRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

open class BaseUseCase @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val logRepository: LogRepository
) {

    suspend fun <T> executeWithLoggingAndNetworkCheck(action: suspend () -> Resource<T>): Resource<T> {
        return try {
            withTimeout(20000L) {
                val netWorkState = checkNetwork()
                when (netWorkState.status) {
                    Status.SUCCESS -> {
                        action()
                    }

                    else -> {
                        Resource.failure(netWorkState.message)
                    }
                }
            }
        } catch (e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            Log.e(this::class.simpleName, "Error occurred: ${e.message}", e)
            Resource.failure(e.message)
        }
    }

    suspend fun <T> executeWithLoggingWithoutNetworkCheck(action: suspend () -> Resource<T>): Resource<T> {
        return try {
            action()
        } catch (e: TimeoutCancellationException) {
            //タイムアウトのエクセプションはログ取らなくてもいいかな？
            Log.e(this::class.simpleName, "Error occurred: ${e.message}", e)
            Resource.failure(e.message)
        } catch (e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            Log.e(this::class.simpleName, "Error occurred: ${e.message}", e)
            Resource.failure(e.message)
        }
    }

    suspend fun <T> executeFlowWithLoggingAndNetworkCheck(action: suspend () -> Flow<Resource<T>>): Flow<Resource<T>> = flow {
        try {
            val netWorkState = checkNetwork()
            when (netWorkState.status) {
                Status.SUCCESS -> {
                    action().collect { resource ->
                        emit(resource)
                    }
                }

                else -> {
                    emit(Resource.failure(netWorkState.message))
                }
            }
        } catch (e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            Log.e(this::class.simpleName, "Error occurred: ${e.message}", e)
            emit(Resource.failure(e.message))
        }
    }



    fun checkNetwork(): Resource<Unit> {
        return if (!networkUtils.isOnline()) {
            Resource.failure("インターネットの接続を確認してください")
        } else {
            Resource.success(Unit)
        }
    }

    fun saveErrorLog(e: Exception) {
        try {
            val errorLog = ErrorLog(
                message = e.message,
                stackTrace = e.stackTraceToString()
                //他に必要な情報あればここに追加
            )
            logRepository.saveErrorLogToFirestore(errorLog)
        } catch (e: Exception) {

        }
    }

    // ユーザーIDが空でないかをチェックする関数
    protected fun validateUserId(userId: String) {
        if (userId.isEmpty()) {
            throw IllegalArgumentException("ユーザーIDが空です")
        }
    }


}