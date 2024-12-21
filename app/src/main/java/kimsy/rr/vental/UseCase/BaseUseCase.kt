package kimsy.rr.vental.UseCase

import android.util.Log
import kimsy.rr.vental.data.ErrorLog
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

open class BaseUseCase @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val logRepository: LogRepository
) {

    suspend fun <T> executeWithLogging(action: suspend () -> Resource<T>): Resource<T> {
        return try {
            val netWorkState = checkNetwork()
            when (netWorkState.status) {
                Status.SUCCESS -> {
                    action()
                }
                else -> {
                    Resource.failure(netWorkState.message)
                }
            }
        } catch (e: Exception) {
            //TODO 開発中はあまり使いたくないのでコメントに
//            saveErrorLog(e)
            Log.e(this::class.simpleName, "Error occurred: ${e.message}", e)
            Resource.failure(e.message)
        }
    }

    private fun checkNetwork(): Resource<Unit> {
        return if (!networkUtils.isOnline()) {
            Resource.failure("インターネットの接続を確認してください")
        } else {
            Resource.success(Unit)
        }
    }

    private fun saveErrorLog(e: Exception) {
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


}