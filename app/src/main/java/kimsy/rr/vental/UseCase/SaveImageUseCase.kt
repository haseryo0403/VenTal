package kimsy.rr.vental.UseCase

import android.content.Context
import android.net.Uri
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.ImageRepository
import kimsy.rr.vental.data.repository.LogRepository
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(uri: Uri, context: Context):Resource<String> {
        return executeWithLoggingAndNetworkCheck {
            val downloadUrl = imageRepository.saveImageToStorage(uri, context)
            Resource.success(downloadUrl)
        }
    }
}