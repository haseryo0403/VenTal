package kimsy.rr.vental.UseCase

import android.content.Context
import android.net.Uri
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.repository.ImageRepository
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend fun execute(uri: Uri, context: Context):Resource<String> {
        return imageRepository.saveImageToStorages(uri, context)
    }
}