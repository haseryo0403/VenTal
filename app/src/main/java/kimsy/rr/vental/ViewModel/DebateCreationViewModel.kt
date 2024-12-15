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
import kimsy.rr.vental.UseCase.GetSwipeCardUseCase
import kimsy.rr.vental.UseCase.GetUserDetailsUseCase
import kimsy.rr.vental.UseCase.MessageCreationUseCase
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.SaveNotificationUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateItemSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.NetworkUtils
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
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
    private val messageCreationUseCase: MessageCreationUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val networkUtils: NetworkUtils
): ViewModel() {

    private val _fetchRelatedDebateState = MutableStateFlow<Resource<List<DebateWithUsers>>>(Resource.idle())
    val fetchRelatedDebateState: StateFlow<Resource<List<DebateWithUsers>>> get() = _fetchRelatedDebateState

    private val _debateCreationState = MutableStateFlow<Resource<Debate>>(Resource.idle())
    val debateCreationState: StateFlow<Resource<Debate>> get() = _debateCreationState

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

            val debateValidation = debateValidate(ventCard)

            if (!debateValidation) {
                return@launch
            }

            val imageUrl = imageUri?.let { uri ->
                val imageURLState = saveImageUseCase.execute(imageUri, context)
                if (imageURLState.status == Status.SUCCESS) imageURLState.data else null
            }

            _debateCreationState.value =
                debateCreationUseCase.execute(text, ventCard, debaterId, imageUrl)

            when (debateCreationState.value.status) {
                Status.SUCCESS -> {
                    //TODO 条件分岐
                    addDebatingSwipeCardUseCase.execute(debaterId, ventCard.swipeCardId)
                    val createdDebate = debateCreationState.value.data
                    if (createdDebate != null) {
                        createMessage(createdDebate, text)
                    }
                    val debater = getUser(debaterId)
                    val poster = getUser(ventCard.posterId)

                    //TODO delete ventCardがwithUserになってしまっているため。修正して削除
                    val ventCard2 =
                        getSwipeCardUseCase.execute(ventCard.posterId, ventCard.swipeCardId).data

                    if (createdDebate != null && debater != null && poster != null && ventCard2 != null) {
                        DebateItemSharedModel.setDebateItem(
                            DebateItem(createdDebate, ventCard2, poster, debater)
                        )
                        onCreationSuccess()
                        handleNotification(
                            debaterId,
                            ventCard.posterId,
                            createdDebate.debateId,
                            text
                        )
                        resetDebateCreationState()
                        resetFetchRelatedDebateState()
                    }
                }

                else -> {}
            }
        }
    }

    //TODO FIX to ventCard without user
    private suspend fun debateValidate(ventCard: VentCardWithUser): Boolean {
        return try {
            val isValid = debateValidationUseCase.execute(ventCard.posterId, ventCard.swipeCardId).getOrThrow()
            if (!isValid) {
                _debateCreationState.value = Resource.failure("バリデーションエラー。入力を確認してください。")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            _debateCreationState.value = Resource.failure("エラー。インターネットを確認してください。")
            false
        }
    }

    private fun createMessage(createdDebate: Debate, text: String) {
        viewModelScope.launch {
            val messageCreationState = messageCreationUseCase.execute(
                debate = createdDebate,
                //TODO delete
                debateWithUsers = null,
                userId = createdDebate.debaterId,
                debateId = createdDebate.debateId,
                text = text,
                messageImageURL = createdDebate.firstMessageImageURL
            )
            messageCreationState
                .onSuccess {

                }
                .onFailure {
                    _debateCreationState.value = Resource.failure(it.message)
                }
        }
    }

    private suspend fun getUser(uid: String): User? {
        val getUserState = getUserDetailsUseCase.execute(uid)
        return when (getUserState.status) {
            Status.SUCCESS -> getUserState.data
            Status.FAILURE -> {
                _debateCreationState.value = Resource.failure(getUserState.message)
                null
            }
            else -> null
        }
    }

    private fun handleNotification(
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