package kimsy.rr.vental.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.DebateCreationUseCase
import kimsy.rr.vental.UseCase.DebateValidationUseCase
import kimsy.rr.vental.UseCase.GetRelatedDebatesUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import kimsy.rr.vental.data.ImageRepository
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateCreationViewModel @Inject constructor(
    private val getRelatedDebatesUseCase: GetRelatedDebatesUseCase,
    private val debateCreationUseCase: DebateCreationUseCase,
    private val debateValidationUseCase: DebateValidationUseCase,
    private val imageRepository: ImageRepository
): ViewModel() {
    private val _relatedDebates = MutableLiveData<List<Debate>>()
    val relatedDebates: LiveData<List<Debate>> get() = _relatedDebates

    private val _debateResult = MutableLiveData<Result<Unit>>()
    val debateResult: LiveData<Result<Unit>> get() = _debateResult
    var ventCardWithUser by mutableStateOf<VentCardWithUser?>(null)

    init {
        Log.d("DCVM", "initialized")
    }

    fun getRelatedDebates(ventCardWithUser: VentCardWithUser){
        viewModelScope.launch {
            try {
                val result = getRelatedDebatesUseCase.execute(ventCardWithUser)
                result.onSuccess {debates->
                    _relatedDebates.value = debates
                    Log.d("DCVM", "relatedDebates: $debates")

                }.onFailure {
                    Log.d("DCVM", "relatedDebates failed")
                    //TODO handling
                }
            } catch (e: Exception) {
                //TODO error handling
            }
        }
    }

    fun handleDebateCreation(text: String, imageUri: Uri?, debaterId: String, context: Context) {
        val ventCard = ventCardWithUser ?: return
        viewModelScope.launch {
            //スワイプカードが表示されている時点でバリデーションの失敗率は低いので先にインスタンス化
            val debate = createDebateInstance(text, ventCard, debaterId)

            val validationResult = validateDebate(debate)
            validationResult.fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        handleImageUploadAndCreateDebate(debate, imageUri, context)
                    } else {
                        _debateResult.value = Result.failure(Exception("Validation failed"))
                    }
                },
                onFailure = { exception ->
                    _debateResult.value = Result.failure(exception)
                }
            )
        }
    }

    private fun createDebateInstance(text: String, ventCard: VentCardWithUser, debaterId: String): Debate {
        return Debate(
            swipeCardImageURL = ventCard.swipeCardImageURL,
            swipeCardId = ventCard.swipeCardId,
            posterId = ventCard.posterId,
            debaterId = debaterId,
            firstMessage = text
        )
    }

    private suspend fun validateDebate(debate: Debate): Result<Boolean> {
        return debateValidationUseCase.execute(debate)
    }

    private suspend fun handleImageUploadAndCreateDebate(debate: Debate, imageUri: Uri?, context: Context) {
        try {
            val updatedDebate = imageUri?.let {
                val imageResult = imageRepository.saveImageToStorage(it, context)
                imageResult.fold(
                    onSuccess = { imageURL->
                        Log.d("ImageUpload", "Image uploaded: $imageURL")
                        debate.copy(firstMessageImageURL = imageURL)
                    },
                    onFailure = { exception ->
                        Log.e("ImageUpload", "Image upload failed: ${exception.message}")
                        //TODO 必要に応じてエラー処理を追加
                        debate
                    }
                )
            } ?: debate
            _debateResult.value = debateCreationUseCase.execute(updatedDebate)
        } catch (e: Exception) {
            _debateResult.value = Result.failure(e)
        }
    }
}