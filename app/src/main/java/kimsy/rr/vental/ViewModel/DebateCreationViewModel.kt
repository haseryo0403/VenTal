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
import kimsy.rr.vental.UseCase.GetRelatedDebatesUseCase
import kimsy.rr.vental.UseCase.GetUserDetailsUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateRepository
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.ImageRepository
import kimsy.rr.vental.data.User
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
    private val imageRepository: ImageRepository
): ViewModel() {
    private val _relatedDebates = MutableLiveData<List<DebateWithUsers>>()
    val relatedDebates: LiveData<List<DebateWithUsers>> get() = _relatedDebates

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> get() = _errorState

    private val _debateResult = MutableLiveData<Result<Unit>>()
    val debateResult: LiveData<Result<Unit>> get() = _debateResult
    var ventCardWithUser by mutableStateOf<VentCardWithUser?>(null)

    init {
        Log.d("DCVM", "initialized")
    }

//    fun getRelatedDebatess(ventCardWithUser: VentCardWithUser) {
//        viewModelScope.launch {
//            runCatching {
//                val debates = getRelatedDebatesUseCase.execute(ventCardWithUser).getOrThrow()
//                debates.map {debate ->
//                    val debater = getUserDetailsUseCase.execute(debate.debaterId).getOrThrow()
//                    //TODO たぶんposterの情報いらない
//                    val poster = getUserDetailsUseCase.execute(debate.posterId).getOrThrow()
//                    createDebateWithUserInstance(debate, debater, poster)
//                }
//            }.onSuccess { debatesWithUsers->
//                _relatedDebates.value = debatesWithUsers
//                Log.d("DCVM", "relatedDebates: $debatesWithUsers")
//            }.onFailure {exception->
//                Log.e("DCVM", "Failed to fetch related debates", exception)
//                //TODO 共通エラーハンドリング（必要に応じてカスタムメッセージやUI操作を追加）
//
//            }
//        }
//    }

    fun getRelatedDebates(ventCardWithUser: VentCardWithUser) {
        viewModelScope.launch {
            try {
                val debates = getRelatedDebatesUseCase.execute(ventCardWithUser).getOrThrow()
                val debatesWithUsers = createDebatesWithUsersUseCase.execute(debates)
                _relatedDebates.value = debatesWithUsers
                Log.d("DebateViewModel", "Fetched related debates: $debatesWithUsers")
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            }
        }
    }

    fun handleDebateCreation(text: String, imageUri: Uri?, debaterId: String, context: Context) {
        viewModelScope.launch {
            try {
                val ventCard = ventCardWithUser ?: throw IllegalStateException("No vent card available")

                // バリデーション実行
                val isValid = debateValidationUseCase.execute(ventCard.posterId, ventCard.swipeCardId).getOrThrow()
                if (!isValid) {
                    _debateResult.value = Result.failure(Exception("Validation failed"))
                    return@launch
                }

                // 画像アップロード（必要な場合）
                val imageUrl = if (imageUri != null) {
                    imageRepository.saveImageToStorage(imageUri, context).getOrThrow()
                } else null

                // 討論作成UseCaseの実行
                _debateResult.value = debateCreationUseCase.execute(text, ventCard, debaterId, imageUrl)

                // スワイプカードIDを保存
                addDebatingSwipeCardUseCase.execute(debaterId, ventCard.swipeCardId)
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            }
        }
    }

    // 画像アップロード処理
    private suspend fun uploadImage(imageUri: Uri, context: Context): String {
        return imageRepository.saveImageToStorage(imageUri, context).getOrThrow()
    }
    private fun handleNetworkError(error: IOException) {
        _errorState.value = "ネットワーク接続に問題があります。" // UI向けメッセージ
        Log.e("DCVM", "Network error occurred", error)
    }

    private fun handleUnexpectedError(error: Exception) {
        _errorState.value = "予期しないエラーが発生しました。" // UI向けメッセージ
        Log.e("DCVM", "Unexpected error occurred", error)
    }

//討論内容受け取り＞インスタンス化＞バリデーション（関連討論数確認）＞画像があれば保存＞討論作成＞ユーザーに討論しているスワイプカードID保存

//    fun handleDebateCreations(text: String, imageUri: Uri?, debaterId: String, context: Context) {
//        val ventCard = ventCardWithUser ?: return
//        viewModelScope.launch {
//            //スワイプカードが表示されている時点でバリデーションの失敗率は低いので先にインスタンス化
//            val debate = createDebateInstance(text, ventCard, debaterId)
//
//            val validationResult = validateDebate(debate)
//            validationResult.fold(
//                onSuccess = { isValid ->
//                    if (isValid) {
//                        handleImageUploadAndCreateDebate(debate, imageUri, context)
//                    } else {
//                        _debateResult.value = Result.failure(Exception("Validation failed"))
//                    }
//                },
//                onFailure = { exception ->
//                    _debateResult.value = Result.failure(exception)
//                }
//            )
//        }
//    }

//    private fun createDebateInstance(text: String, ventCard: VentCardWithUser, debaterId: String): Debate {
//        return Debate(
//            swipeCardImageURL = ventCard.swipeCardImageURL,
//            swipeCardId = ventCard.swipeCardId,
//            posterId = ventCard.posterId,
//            debaterId = debaterId,
//            firstMessage = text
//        )
//    }

//    private fun createDebateWithUserInstance(debate: Debate, debater: User, poster: User): DebateWithUsers {
//        return DebateWithUsers(
//            swipeCardImageURL = debate.swipeCardImageURL,
//            swipeCardId = debate.swipeCardId,
//            posterId = debate.posterId,
//            posterName = poster.name,
//            posterImageURL = poster.photoURL,
//            posterLikeCount = debate.posterLikeCount,
//            debaterId = debate.debaterId,
//            debaterName = debater.name,
//            debaterImageURL = debater.photoURL,
//            debaterLikeCount = debate.debaterLikeCount,
//            firstMessage = debate.firstMessage,
//            firstMessageImageURL = debate.firstMessageImageURL,
//            //TODO 討論作成時間
//        )
//    }

//    private suspend fun validateDebate(debate: Debate): Result<Boolean> {
//        return debateValidationUseCase.execute(debate)
//    }

//    private suspend fun handleImageUploadAndCreateDebate(debate: Debate, imageUri: Uri?, context: Context) {
//        try {
//            val updatedDebate = imageUri?.let {
//                val imageResult = imageRepository.saveImageToStorage(it, context)
//                imageResult.fold(
//                    onSuccess = { imageURL->
//                        Log.d("ImageUpload", "Image uploaded: $imageURL")
//                        debate.copy(firstMessageImageURL = imageURL)
//                    },
//                    onFailure = { exception ->
//                        Log.e("ImageUpload", "Image upload failed: ${exception.message}")
//                        //TODO 必要に応じてエラー処理を追加
//                        debate
//                    }
//                )
//            } ?: debate
//            _debateResult.value = debateCreationUseCase.execute(updatedDebate)
//            addDebatingSwipeCardUseCase.execute(debate)
//        } catch (e: Exception) {
//            _debateResult.value = Result.failure(e)
//        }
//    }
}