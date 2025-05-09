package kimsy.rr.vental.UseCase

import android.content.Context
import android.net.Uri
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.repository.LogRepository
import kimsy.rr.vental.data.repository.VentCardRepository
import javax.inject.Inject

class SaveVentCardUseCase @Inject constructor(
    private val saveImageUseCase: SaveImageUseCase,
    private val ventCardRepository: VentCardRepository,
    networkUtils: NetworkUtils,
    logRepository: LogRepository
): BaseUseCase(networkUtils, logRepository) {
    suspend fun execute(
        posterId: String,
        content: String,
        selectedImageUri: Uri?,
        tags: List<String>,
        context: Context
    ): Resource<Unit> {
        return executeWithLoggingAndNetworkCheck {
            validateUserId(posterId)
            if (selectedImageUri == null) {
                saveVentCardWithoutImage(posterId, content, tags)
                Resource.success(Unit)
            } else {
                saveVentCardWithImage(posterId, content, selectedImageUri, tags, context)
                Resource.success(Unit)
            }
        }
    }

    private suspend fun saveVentCardWithoutImage(
        posterId: String,
        content: String,
        tags: List<String>
    ){
        val ventCard = VentCard.createVentCard(
            posterId = posterId,
            swipeCardContent = content,
            swipeCardImageURL = "",
            tags = tags
        )
        ventCardRepository.saveVentCardToFireStore(ventCard)
    }

    private suspend fun saveVentCardWithImage(
        posterId: String,
        content: String,
        imageUri: Uri,
        tags: List<String>,
        context: Context
    ){
        val imageResourceURL = saveImageUseCase.execute(imageUri, context)
        return when (imageResourceURL.status) {
            Status.SUCCESS  -> {
                val ventCard = VentCard.createVentCard(
                    posterId = posterId,
                    swipeCardContent = content,
                    swipeCardImageURL = imageResourceURL.data ?: "",
                    tags = tags
                )
                ventCardRepository.saveVentCardToFireStore(ventCard)
            }
            else -> {}
        }
    }
}
