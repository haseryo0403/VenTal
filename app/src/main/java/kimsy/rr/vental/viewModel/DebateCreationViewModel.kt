package kimsy.rr.vental.viewModel

import android.content.Context
import android.net.Uri
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
import kimsy.rr.vental.UseCase.GetUserDetailsUseCase
import kimsy.rr.vental.UseCase.MessageCreationUseCase
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.SaveNotificationUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
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
    private val getUserDetailsUseCase: GetUserDetailsUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _fetchRelatedDebateState = MutableStateFlow<Resource<List<DebateItem>>>(Resource.idle())
    val fetchRelatedDebateState: StateFlow<Resource<List<DebateItem>>> get() = _fetchRelatedDebateState

    private val _debateCreationState = MutableStateFlow<Resource<Debate>>(Resource.idle())
    val debateCreationState: StateFlow<Resource<Debate>> get() = _debateCreationState

    var ventCard by mutableStateOf(VentCard())

    fun getRelatedDebates(ventCard: VentCard) {
        viewModelScope.launch {
            _fetchRelatedDebateState.value = Resource.loading()
            _fetchRelatedDebateState.value = getRelatedDebatesUseCase.execute(ventCard)
        }
    }

    fun handleDebateCreation(text: String,
                             imageUri: Uri?,
                             debaterId: String,
                             context: Context,
                             onCreationSuccess: (DebateItem) -> Unit
    ) {
        viewModelScope.launch {
            _debateCreationState.value = Resource.loading()

            //関連debateの数を確認。３未満ならtrue
            val debateValidation = debateValidate(ventCard)

            if (!debateValidation) {
                _debateCreationState.value = Resource.failure()
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
                    onDebateCreationSuccess(text, debaterId, ventCard, onCreationSuccess)
                }

                else -> {}
            }
        }
    }

    private suspend fun debateValidate(ventCard: VentCard): Boolean {
        val validationResult = debateValidationUseCase.execute(ventCard.posterId, ventCard.swipeCardId)
        return when (validationResult.status) {
            Status.SUCCESS -> validationResult.data?: false
            else -> false
        }
    }

    private suspend fun onDebateCreationSuccess(
        text: String,
        debaterId: String,
        ventCard: VentCard,
        onCreationSuccess: (DebateItem) -> Unit
    ) {
        val result = addDebatingSwipeCardUseCase.execute(debaterId, ventCard.swipeCardId)
        if (result.status == Status.FAILURE) {
            _debateCreationState.value = Resource.failure()
        }
        val createdDebate = debateCreationState.value.data
        if (createdDebate != null) {
            createMessage(createdDebate, text)
        }
        val debater = getUser(debaterId)
        val poster = getUser(ventCard.posterId)

        if (createdDebate != null && debater != null && poster != null) {
            val createdDebateItem = DebateItem(createdDebate, ventCard, poster, debater, null)
            onCreationSuccess(createdDebateItem)
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

    private fun createMessage(createdDebate: Debate, text: String) {
        viewModelScope.launch {
            val messageCreationState = messageCreationUseCase.execute(
                debate = createdDebate,
                userId = createdDebate.debaterId,
                debateId = createdDebate.debateId,
                text = text,
                messageImageURL = createdDebate.firstMessageImageURL
            )
            when (messageCreationState.status) {
                Status.FAILURE -> {
                    _debateCreationState.value = Resource.failure(messageCreationState.message)
                }
                else -> {}
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
            saveNotificationUseCase.execute(fromUserId, toUserId, debateId, NotificationType.DEBATESTART, body)
        }
    }

    fun resetDebateCreationState() {
        _debateCreationState.value = Resource.idle()
    }

    fun resetFetchRelatedDebateState() {
        _fetchRelatedDebateState.value = Resource.idle()
    }
}