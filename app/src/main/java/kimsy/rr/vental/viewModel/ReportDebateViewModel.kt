package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.ReportUseCase
import kimsy.rr.vental.data.DebateShareModel
import kimsy.rr.vental.data.EntityType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDebateViewModel @Inject constructor(
    private val reportUseCase: ReportUseCase

): ViewModel() {

    val reportedDebate = DebateShareModel.getReportedDebateFromModel()

    private val _reportState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val reportState: StateFlow<Resource<Unit>> get() = _reportState

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    fun reportDebate(
        reasonInt: Int
    ) {
        viewModelScope.launch {
            _reportState.value = Resource.loading()

            val reportedDebate = reportedDebate
            val currentUserId = currentUser?.uid
            if (reportedDebate == null || currentUserId == null) {
                _reportState.value = Resource.failure()
                return@launch
            }

            _reportState.value = reportUseCase.execute(
                entityId = reportedDebate.debateId,
                entityType = EntityType.DEBATE,
                posterId = reportedDebate.posterId,
                ventCardId = reportedDebate.swipeCardId,
                requesterId = currentUserId,
                reasonInt = reasonInt,
                reportFlag = reportedDebate.debateReportFlag
            )
        }
    }

    fun resetState() {
        _reportState.value = Resource.idle()
    }


}