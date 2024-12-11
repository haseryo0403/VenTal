package kimsy.rr.vental.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.AddDebatingSwipeCardUseCase
import kimsy.rr.vental.UseCase.DebateCreationUseCase
import kimsy.rr.vental.UseCase.DebateValidationUseCase
import kimsy.rr.vental.UseCase.GetRelatedDebatesUseCase
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.SaveNotificationUseCase
import kimsy.rr.vental.data.DebateSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateCreationViewModel @Inject constructor(
    private val getRelatedDebatesUseCase: GetRelatedDebatesUseCase,
    private val debateCreationUseCase: DebateCreationUseCase,
    private val debateValidationUseCase: DebateValidationUseCase,
    private val addDebatingSwipeCardUseCase: AddDebatingSwipeCardUseCase,
    private val saveNotificationUseCase: SaveNotificationUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val networkUtils: NetworkUtils
): ViewModel() {

    private val _fetchRelatedDebateState = MutableStateFlow<Resource<List<DebateWithUsers>>>(Resource.idle())
    val fetchRelatedDebateState: StateFlow<Resource<List<DebateWithUsers>>> get() = _fetchRelatedDebateState

    private val _debateCreationState = MutableStateFlow<Resource<DebateWithUsers>>(Resource.idle())
    val debateCreationState: StateFlow<Resource<DebateWithUsers>> get() = _debateCreationState

    var ventCardWithUser by mutableStateOf<VentCardWithUser?>(null)

    init {
        Log.d("DCVM", "initialized")
    }


    fun getRelatedDebates(ventCardWithUser: VentCardWithUser) {
        viewModelScope.launch {
            _fetchRelatedDebateState.value = Resource.loading()
            _fetchRelatedDebateState.value = getRelatedDebatesUseCase.execute(ventCardWithUser)

        }
    }

    fun handleDebateCreation(text: String,
                             imageUri: Uri?,
                             debaterId: String,
                             context: Context,
                             onCreationSuccess: () -> Unit = {}) {
        viewModelScope.launch {

            if (!networkUtils.isOnline()) {
                _debateCreationState.value = Resource.failure("インターネットの接続を確認してください")
                return@launch
            }

            _debateCreationState.value = Resource.loading()
            val ventCard = ventCardWithUser ?: throw IllegalStateException("No vent card available")

            debateValidationUseCase.execute(ventCard.posterId, ventCard.swipeCardId)
                .onSuccess { isValid->
                    if (!isValid) {
                        _debateCreationState.value = Resource.failure("バリデーションエラー。入力を確認してください。")
                        return@launch
                    }
                }
                .onFailure {
                    _debateCreationState.value = Resource.failure("エラー。インターネットを確認してください。")

                }

            val imageUrl = imageUri?.let { uri ->
                val imageURLState =saveImageUseCase.execute(imageUri, context)
                if (imageURLState.status == Status.SUCCESS) imageURLState.data else null
            }

            val createdDebate = debateCreationUseCase.execute(text, ventCard, debaterId, imageUrl)
            when (createdDebate.status) {
                Status.SUCCESS ->{
                    addDebatingSwipeCardUseCase.execute(debaterId, ventCard.swipeCardId)
                    createdDebate.data?.let { DebateSharedModel.setDebate(it) }
                    createdDebate.data?.let { handleNotification(debaterId, ventCard.posterId, it.debateId, text) }
                    onCreationSuccess()
                    resetDebateCreationState()
                    resetFetchRelatedDebateState()
                }
                Status.FAILURE -> {
                    _debateCreationState.value = Resource.failure(createdDebate.message)
                }
                else -> {}
            }
        }
    }

    fun handleNotification(
        fromUserId: String,
        toUserId: String,
        debateId: String,
        body: String
        ){
        viewModelScope.launch {
            saveNotificationUseCase.execute(fromUserId, toUserId, debateId, body)
        }
    }

    fun resetDebateCreationState() {
        _debateCreationState.value = Resource.idle()
    }

    fun resetFetchRelatedDebateState() {
        _fetchRelatedDebateState.value = Resource.idle()
    }
}