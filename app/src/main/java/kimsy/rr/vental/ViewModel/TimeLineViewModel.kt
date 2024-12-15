package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetTimeLineItemsUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val getTimeLineItemsUseCase: GetTimeLineItemsUseCase
): ViewModel() {

    private val _getDebatesWithVentCardState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(Resource.idle())
    val getDebatesWithVentCardState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebatesWithVentCardState

    private val _timelineItems = mutableStateListOf<DebateItem>()
    val timelineItems: List<DebateItem> get() = _timelineItems

    private var lastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    suspend fun getTimeLineItems () {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
            if (timelineItems.isEmpty()) {
                _getDebatesWithVentCardState.value = Resource.loading()
            }
            _getDebatesWithVentCardState.value = getTimeLineItemsUseCase.execute(lastVisible)
            when(_getDebatesWithVentCardState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebatesWithVentCardState.value.data?.let {(timelineItems, newLasVisible)->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        _timelineItems.addAll(timelineItems)
                        lastVisible = newLasVisible
                    }
                }
                Status.FAILURE -> {
                    Log.d("TLVM", "failure")
                }
                else -> {}
            }
        }

    }

}