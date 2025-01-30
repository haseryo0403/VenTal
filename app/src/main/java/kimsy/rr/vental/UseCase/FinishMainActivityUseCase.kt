package kimsy.rr.vental.UseCase

import android.app.Activity
import android.content.Context
import android.content.Intent
import kimsy.rr.vental.MainActivity
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class FinishMainActivityUseCase @Inject constructor(
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(context: Context) {
        executeWithLoggingAndNetworkCheck {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            (context as Activity).finish()
            Resource.success(Unit)
        }
    }
}