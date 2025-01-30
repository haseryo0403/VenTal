package kimsy.rr.vental.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.FollowUseCase
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.MessageCreationUseCase
import kimsy.rr.vental.UseCase.ObserveFollowingUserIdUseCase
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.SaveNotificationUseCase
import kimsy.rr.vental.UseCase.UnFollowUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getMessageUseCase: GetMessageUseCase,
    private val messageCreationUseCase: MessageCreationUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val saveNotificationUseCase: SaveNotificationUseCase,
    private val observeFollowingUserIdUseCase: ObserveFollowingUserIdUseCase,
    private val followUseCase: FollowUseCase,
    private val unFollowUseCase: UnFollowUseCase
    ): ViewModel() {

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState

    private val _createMessageState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val createMessageState: StateFlow<Resource<Unit>> get() = _createMessageState

    private val _followingUserIdsState = MutableStateFlow<Resource<List<String>>>(Resource.idle())
    val followingUserIdsState: StateFlow<Resource<List<String>>> get() = _followingUserIdsState

    private val _followState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val followState: StateFlow<Resource<Unit>> get() = _followState

    fun getMessages(debate: Debate) {
        viewModelScope.launch {
             getMessageUseCase.execute(
                debate.posterId,
                debate.swipeCardId,
                debate.debateId
            ).collect {resource ->
                _fetchMessageState.value = resource
            }
        }
    }

    fun createMessage(debate: Debate, text: String, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            _createMessageState.value = Resource.loading()
            val imageUrl = imageUri?.let { uri ->
                val imageURLState = saveImageUseCase.execute(imageUri, context)
                if (imageURLState.status == Status.SUCCESS) imageURLState.data else null
            }
            val messageCreationState = currentUser?.let {
                messageCreationUseCase.execute(
                    debate = debate,
                    userId = it.uid,
                    debateId = debate.debateId,
                    text = text,
                    messageImageURL = imageUrl
                )
            }
            if (messageCreationState != null) {
                when(messageCreationState.status) {
                    Status.SUCCESS -> {
                        _createMessageState.value = Resource.success(Unit)
                        currentUser?.let { user ->
                            saveNotificationUseCase.execute(
                                fromUserId = user.uid,
                                if (user.uid == debate.posterId) debate.debaterId else debate.posterId,
                                debate.debateId,
                                NotificationType.DEBATEMESSAGE,
                                text)
                        }
                    }
                    Status.FAILURE -> {
                        _createMessageState.value = Resource.failure(messageCreationState.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun observeFollowingUserIds() {
        viewModelScope.launch {
            currentUser?.let {
                observeFollowingUserIdUseCase.execute(it.uid)
                    .collect{ resource ->
                        _followingUserIdsState.value = resource
                    }
            }
        }
    }

    fun followUser(toUserId: String) {
        viewModelScope.launch {
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                currentUser?.let {
                    _followState.value = followUseCase.execute(it.uid, toUserId)
                }
            }
        }
    }

    fun unFollowUser(toUserId: String) {
        viewModelScope.launch {
            //一旦フォローアンフォロー共有State
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                currentUser?.let {
                    _followState.value = unFollowUseCase.execute(it.uid, toUserId)
                }
            }
        }
    }

    fun resetState() {
        _fetchMessageState.value = Resource.idle()
        _createMessageState.value = Resource.idle()
        _followState.value = Resource.idle()
        _followingUserIdsState.value = Resource.idle()
    }
}