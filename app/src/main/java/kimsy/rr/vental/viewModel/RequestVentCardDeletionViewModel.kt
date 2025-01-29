package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.RequestDeletionUseCase
import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCardShareModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestVentCardDeletionViewModel @Inject constructor(
    private val requestDeletionUseCase: RequestDeletionUseCase
): ViewModel() {
    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val deleteRequestedVentCard = VentCardShareModel.getDeleteRequestedVentCardFromModel()

    private val _requestState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val requestState: StateFlow<Resource<Unit>> get() = _requestState

    fun requestVentCardDeletion(
        reasonInt: Int
    ) {
        viewModelScope.launch {
            _requestState.value = Resource.loading()

            val requestedVentCard = deleteRequestedVentCard
            val currentUserId = currentUser.value.uid
            if (requestedVentCard == null) {
                _requestState.value = Resource.failure()
                return@launch
            }

            _requestState.value = requestDeletionUseCase.execute(
                entityId = requestedVentCard.swipeCardId,
                entityType = EntityType.VENTCARD,
                posterId = requestedVentCard.posterId,
                ventCardId = requestedVentCard.swipeCardId,
                requesterId = currentUserId,
                reasonInt = reasonInt,
                deletionRequestFlag = requestedVentCard.swipeCardDeletionRequestFlag
            )
        }
    }

    fun resetState() {
        _requestState.value = Resource.idle()
    }
}