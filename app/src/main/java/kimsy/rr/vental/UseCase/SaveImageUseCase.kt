package kimsy.rr.vental.UseCase

import android.content.Context
import android.net.Uri
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.ImageRepository
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val networkUtils: NetworkUtils
) {
    suspend fun execute(uri: Uri, context: Context):Resource<String> {
        if (!networkUtils.isOnline()) {
            return Resource.failure("インターネットの接続を確認してください")
        }
        return imageRepository.saveImageToStorages(uri, context)
    }
}