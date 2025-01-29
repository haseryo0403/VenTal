package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.RequestDeletionUseCase
import kimsy.rr.vental.data.DebateShareModel
import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestDebateDeletionViewModel @Inject constructor(
    private val requestDeletionUseCase: RequestDeletionUseCase
):ViewModel() {
    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val deleteRequestedDebate = DebateShareModel.getDeleteRequestedDebateFromModel()

    private val _requestState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val requestState: StateFlow<Resource<Unit>> get() = _requestState

    fun requestDebateDeletion(
        reasonInt: Int
    ) {
        viewModelScope.launch {
            _requestState.value = Resource.loading()

            val requestedDebate = deleteRequestedDebate
            val currentUserId = currentUser.value.uid
            if (requestedDebate == null) {
                _requestState.value = Resource.failure()
                return@launch
            }

            _requestState.value = requestDeletionUseCase.execute(
                entityId = requestedDebate.debateId,
                entityType = EntityType.DEBATE,
                posterId = requestedDebate.posterId,
                ventCardId = requestedDebate.swipeCardId,
                requesterId = currentUserId,
                reasonInt = reasonInt,
                deletionRequestFlag = requestedDebate.debateDeletionRequestFlag
            )
        }
    }

    fun resetState() {
        _requestState.value = Resource.idle()
    }

}