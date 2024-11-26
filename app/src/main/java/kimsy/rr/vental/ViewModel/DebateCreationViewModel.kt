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
import kimsy.rr.vental.UseCase.AddDebatingSwipeCardUseCase
import kimsy.rr.vental.UseCase.CreateDebatesWithUsersUseCase
import kimsy.rr.vental.UseCase.DebateCreationUseCase
import kimsy.rr.vental.UseCase.DebateValidationUseCase
import kimsy.rr.vental.UseCase.GetDebateInfoUseCase
import kimsy.rr.vental.UseCase.GetRelatedDebatesUseCase
import kimsy.rr.vental.data.DebateSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.repository.ImageRepository
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DebateCreationViewModel @Inject constructor(
    private val getRelatedDebatesUseCase: GetRelatedDebatesUseCase,
    private val createDebatesWithUsersUseCase: CreateDebatesWithUsersUseCase,
    private val debateCreationUseCase: DebateCreationUseCase,
    private val debateValidationUseCase: DebateValidationUseCase,
    private val addDebatingSwipeCardUseCase: AddDebatingSwipeCardUseCase,
    private val getDebateInfoUseCase: GetDebateInfoUseCase,
    private val imageRepository: ImageRepository
): ViewModel() {

    var isLoading = mutableStateOf(true)
        private set

    private val _relatedDebates = MutableLiveData<List<DebateWithUsers>>()
    val relatedDebates: LiveData<List<DebateWithUsers>> get() = _relatedDebates

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> get() = _errorState

    private val _createdDebateWithUsers = MutableLiveData<Result<DebateWithUsers>>()
    val createdDebateWithUsers: LiveData<Result<DebateWithUsers>> get() = _createdDebateWithUsers
    var ventCardWithUser by mutableStateOf<VentCardWithUser?>(null)

    init {
        Log.d("DCVM", "initialized")
    }

    fun getRelatedDebates(ventCardWithUser: VentCardWithUser) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val debates = getRelatedDebatesUseCase.execute(ventCardWithUser).getOrThrow()
                val debatesWithUsers = createDebatesWithUsersUseCase.execute(debates)
                _relatedDebates.value = debatesWithUsers
                Log.d("DebateViewModel", "Fetched related debates: $debatesWithUsers")
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun handleDebateCreation(text: String,
                             imageUri: Uri?,
                             debaterId: String,
                             context: Context,
                             onCreationSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val ventCard = ventCardWithUser ?: throw IllegalStateException("No vent card available")

                // バリデーション実行
                val isValid = debateValidationUseCase.execute(ventCard.posterId, ventCard.swipeCardId).getOrThrow()
                if (!isValid) {
                    _createdDebateWithUsers.value = Result.failure(Exception("Validation failed"))
                    return@launch
                }

                // 画像アップロード（必要な場合）
                val imageUrl = if (imageUri != null) {
                    imageRepository.saveImageToStorage(imageUri, context).getOrThrow()
                } else null

                // 討論作成UseCaseの実行
                val createdDebate = debateCreationUseCase.execute(text, ventCard, debaterId, imageUrl).getOrThrow()
                val createdDebateWithUsersInfo = getDebateInfoUseCase.execute(createdDebate).getOrThrow()
                //これでviewから触れる
//                _createdDebateWithUsers.value = getDebateInfoUseCase.execute(createdDebate)

                // スワイプカードIDを保存
                addDebatingSwipeCardUseCase.execute(debaterId, ventCard.swipeCardId)
                DebateSharedModel.setDebate(createdDebateWithUsersInfo)

                onCreationSuccess()
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun handleNetworkError(error: IOException) {
        _errorState.value = "ネットワーク接続に問題があります。" // UI向けメッセージ
        Log.e("DCVM", "Network error occurred", error)
    }

    private fun handleUnexpectedError(error: Exception) {
        _errorState.value = "予期しないエラーが発生しました。" // UI向けメッセージ
        Log.e("DCVM", "Unexpected error occurred", error)
    }
}