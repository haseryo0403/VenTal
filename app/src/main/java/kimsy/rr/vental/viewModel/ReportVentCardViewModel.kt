package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.ReportUseCase
import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCardShareModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportVentCardViewModel @Inject constructor(
    private val reportUseCase: ReportUseCase
): ViewModel(){

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val reportedVentCard = VentCardShareModel.getReportedVentCardFromModel()

    private val _reportState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val reportState: StateFlow<Resource<Unit>> get() = _reportState

    fun reportVentCard(
        reasonInt: Int
    ) {
        viewModelScope.launch {
            _reportState.value = Resource.loading()

            val reportedVentCard = reportedVentCard
            val currentUserId = currentUser.value.uid
            if (reportedVentCard == null) {
                _reportState.value = Resource.failure()
                return@launch
            }

            _reportState.value = reportUseCase.execute(
                entityId = reportedVentCard.swipeCardId,
                entityType = EntityType.VENTCARD,
                posterId = reportedVentCard.posterId,
                ventCardId = reportedVentCard.swipeCardId,
                requesterId = currentUserId,
                reasonInt = reasonInt,
                reportFlag = reportedVentCard.swipeCardReportFlag
            )
        }
    }

    fun resetState() {
        _reportState.value = Resource.idle()
    }
}