package kimsy.rr.vental.UseCase

import android.content.Context
import android.net.Uri
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.ImageRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class SaveVentCardUseCase @Inject constructor(
    private val saveImageUseCase: SaveImageUseCase,
    private val ventCardRepository: VentCardRepository
) {
    suspend fun execute(
        posterId: String,
        content: String,
        selectedImageUri: Uri?,
        tags: List<String>,
        context: Context
    ): Resource<Unit> {
        return try {
            if (selectedImageUri == null) {
                saveVentCardWithoutImage(posterId, content, tags)
            } else {
                saveVentCardWithImage(posterId, content, selectedImageUri, tags, context)
            }
        } catch (e: Exception) {
            Resource.failure(message = e.message ?: "VentCardの保存に失敗しました")
        }
    }

    private suspend fun saveVentCardWithoutImage(
        posterId: String,
        content: String,
        tags: List<String>
    ): Resource<Unit> {
        val ventCard = VentCard.createVentCard(
            posterId = posterId,
            swipeCardContent = content,
            swipeCardImageURL = "",
            tags = tags
        )
        return ventCardRepository.saveVentCardToFireStores(ventCard)
    }

    private suspend fun saveVentCardWithImage(
        posterId: String,
        content: String,
        imageUri: Uri,
        tags: List<String>,
        context: Context
    ): Resource<Unit> {
        val imageResourceURL = saveImageUseCase.execute(imageUri, context)
        return when (imageResourceURL.status) {
            Status.SUCCESS  -> {
                val ventCard = VentCard.createVentCard(
                    posterId = posterId,
                    swipeCardContent = content,
                    swipeCardImageURL = imageResourceURL.data ?: "",
                    tags = tags
                )
                return ventCardRepository.saveVentCardToFireStores(ventCard)
            }
            Status.ERROR -> Resource.failure(message = imageResourceURL.message)
            Status.LOADING -> Resource.loading()
            Status.IDLE -> Resource.idle()
        }
    }
}
