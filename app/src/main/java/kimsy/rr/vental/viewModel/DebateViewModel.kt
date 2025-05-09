package kimsy.rr.vental.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.CountCommentsUseCase
import kimsy.rr.vental.UseCase.FollowUseCase
import kimsy.rr.vental.UseCase.GetCommentItemUseCase
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.MessageCreationUseCase
import kimsy.rr.vental.UseCase.ObserveFollowingUserIdUseCase
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.SaveNotificationUseCase
import kimsy.rr.vental.UseCase.SendCommentUseCase
import kimsy.rr.vental.UseCase.UnFollowUseCase
import kimsy.rr.vental.data.CommentItem
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
    private val unFollowUseCase: UnFollowUseCase,
    private val sendCommentUseCase: SendCommentUseCase,
    private val getCommentItemUseCase: GetCommentItemUseCase,
    private val countCommentsUseCase: CountCommentsUseCase
    ): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    val currentUserId = _currentUser.value.uid

    private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState

    private val _createMessageState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val createMessageState: StateFlow<Resource<Unit>> get() = _createMessageState

    private val _followingUserIdsState = MutableStateFlow<Resource<List<String>>>(Resource.idle())
    val followingUserIdsState: StateFlow<Resource<List<String>>> get() = _followingUserIdsState

    private val _followState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val followState: StateFlow<Resource<Unit>> get() = _followState

    private val _getCommentsCountState = MutableStateFlow<Resource<Long>>(Resource.idle())
    val getCommentsCountState: StateFlow<Resource<Long>> get() = _getCommentsCountState

    private val _fetchCommentItemState = MutableStateFlow<Resource<Pair<List<CommentItem>, DocumentSnapshot?>>>(Resource.idle())
    val fetchCommentItemState: StateFlow<Resource<Pair<List<CommentItem>, DocumentSnapshot?>>> get() = _fetchCommentItemState

    private val _sendCommentState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val sendCommentState: StateFlow<Resource<Unit>> get() = _sendCommentState

    private var commentItemLastVisible: DocumentSnapshot? = null
    var hasFinishedLoadingAllCommentItems by mutableStateOf(false)
        private set

    private val _commentItems = MutableStateFlow<List<CommentItem>>(emptyList())
    val commentItems: StateFlow<List<CommentItem>> get() = _commentItems


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
            val messageCreationState =
                messageCreationUseCase.execute(
                    debate = debate,
                    userId = currentUserId,
                    debateId = debate.debateId,
                    text = text,
                    messageImageURL = imageUrl
                )
            when(messageCreationState.status) {
                Status.SUCCESS -> {
                    _createMessageState.value = Resource.success(Unit)
                        saveNotificationUseCase.execute(
                            fromUserId = currentUserId,
                            if (currentUserId == debate.posterId) debate.debaterId else debate.posterId,
                            debate.debateId,
                            NotificationType.DEBATEMESSAGE,
                            text)
                }
                Status.FAILURE -> {
                    _createMessageState.value = Resource.failure(messageCreationState.message)
                }
                else -> {}
            }
        }
    }

    fun observeFollowingUserIds() {
        viewModelScope.launch {
            observeFollowingUserIdUseCase.execute(currentUserId)
                .collect{ resource ->
                    _followingUserIdsState.value = resource
                }
        }
    }

    fun followUser(toUserId: String) {
        viewModelScope.launch {
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                _followState.value = followUseCase.execute(currentUserId, toUserId)
            }
        }
    }

    fun unFollowUser(toUserId: String) {
        viewModelScope.launch {
            //一旦フォローアンフォロー共有State
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                _followState.value = unFollowUseCase.execute(currentUserId, toUserId)
            }
        }
    }

    fun getCommentsCount(
        debate: Debate
    ) {
        viewModelScope.launch {
            _getCommentsCountState.value = Resource.loading()
            _getCommentsCountState.value = countCommentsUseCase.execute(
                debate.posterId,
                debate.swipeCardId,
                debate.debateId
            )
        }
    }

    fun getComments(
        debate: Debate
    ) {
        viewModelScope.launch {
            Log.d("DVM", "getComment called lastV: $commentItemLastVisible")
            _fetchCommentItemState.value = Resource.loading()
            _fetchCommentItemState.value = getCommentItemUseCase.execute(
                debate.posterId,
                debate.swipeCardId,
                debate.debateId,
                commentItemLastVisible
            )
            when (_fetchCommentItemState.value.status) {
                Status.SUCCESS -> {
                    _fetchCommentItemState.value.data?.let { (commentItems, newLastVisible) ->
                        if (commentItems.isEmpty()) {
                            Log.d("DVM", "empty")
                            hasFinishedLoadingAllCommentItems = true
                        }

                        _commentItems.value = _commentItems.value + commentItems
                        commentItemLastVisible = newLastVisible
                    }
                }
                Status.FAILURE -> {}
                else -> {}
            }
        }
    }

    fun sendComment(debate: Debate, text: String, context: Context) {
        viewModelScope.launch {
            _sendCommentState.value = Resource.loading()

           _sendCommentState.value =
                sendCommentUseCase.execute(
                    posterId = debate.posterId,
                    ventCardId = debate.swipeCardId,
                    debateId = debate.debateId,
                    commenterId = currentUserId,
                    commentContent = text
                )
            if (_sendCommentState.value.status == Status.SUCCESS) {
                if (currentUserId == debate.posterId || currentUserId == debate.debaterId) {
                    saveNotificationToDebateParticipants(debate, text)
                } else {
                    saveNotificationToDebateBothParticipants(debate, text)
                }
            }
        }
    }

    fun addCommentLocally(comment: CommentItem) {
        _commentItems.value = listOf(comment) + _commentItems.value
    }

    private suspend fun saveNotificationToDebateParticipants(debate: Debate, text: String) {
        saveNotificationUseCase.execute(
            fromUserId = currentUserId,
            toUserId = if (currentUserId == debate.posterId) debate.debaterId else debate.posterId,
            debate.debateId,
            NotificationType.DEBATECOMMENT,
            text)
    }

    private suspend fun saveNotificationToDebateBothParticipants(debate: Debate, text: String) {
        saveNotificationUseCase.execute(
            fromUserId = currentUserId,
            toUserId = debate.posterId,
            debate.debateId,
            NotificationType.DEBATECOMMENT,
            text)
        saveNotificationUseCase.execute(
            fromUserId = currentUserId,
            toUserId = debate.debaterId,
            debate.debateId,
            NotificationType.DEBATECOMMENT,
            text)
    }

    fun resetState() {
        _fetchMessageState.value = Resource.idle()
        _createMessageState.value = Resource.idle()
        _followState.value = Resource.idle()
        _followingUserIdsState.value = Resource.idle()
        _sendCommentState.value = Resource.idle()
    }
}