package kimsy.rr.vental.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.ReportDebateUseCase
import kimsy.rr.vental.UseCase.StoreReportDebateDataUseCase
import kimsy.rr.vental.data.DebateShareModel
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDebateViewModel @Inject constructor(
    private val reportDebateUseCase: ReportDebateUseCase,
    private val storeReportDebateDataUseCase: StoreReportDebateDataUseCase

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
            val userId = currentUser?.uid
            if (reportedDebate == null || userId == null) {
                _reportState.value = Resource.failure()
                return@launch
            }

            if (!reportedDebate.debateReportFlag) {
                val reportResult = reportDebateUseCase.execute(
                    debateId = reportedDebate.debateId,
                    swipeCardId = reportedDebate.swipeCardId,
                    posterId = reportedDebate.posterId
                )

                if (reportResult.status != Status.SUCCESS) {
                    _reportState.value = Resource.failure()
                    return@launch
                }
            }

            _reportState.value = storeReportDebateDataUseCase.execute(
                debateId = reportedDebate.debateId,
                reporterId = userId,
                reason = reasonInt
            )
        }
    }

    fun resetState() {
        _reportState.value = Resource.idle()
    }


}