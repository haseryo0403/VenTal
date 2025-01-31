package kimsy.rr.vental.UseCase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class GetGoogleIdTokenUseCase @Inject constructor(
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(data: Intent?): Resource<String> {
        return executeWithLoggingWithoutNetworkCheck {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                Resource.success(idToken)
            } else {
                Resource.failure("idToken is null")
            }
        }
    }
}
