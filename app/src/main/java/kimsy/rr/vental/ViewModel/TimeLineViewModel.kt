package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GetTimeLineItemsUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val getTimeLineItemsUseCase: GetTimeLineItemsUseCase,
): ViewModel() {

    init {
        Log.d("TLVM" , "initialized")
    }

    var savedScrollIndex by mutableStateOf(0)
    var savedScrollOffset by mutableStateOf(0)

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()


    //各VM
    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    //各VM
    private val _timelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val timelineItems: StateFlow<List<DebateItem>> get() = _timelineItems

    private var lastVisible: DocumentSnapshot? = null

    //各VM
    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    //各VM
    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            lastVisible = null
            getTimeLineItems()
        }
    }

    fun onLikeSuccess(debateItem: DebateItem) {
        val index = _timelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            // 変更: _timelineItemsのStateFlowを更新
            _timelineItems.value = _timelineItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    //各VM
    suspend fun getTimeLineItems() {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
//            if (_timelineItems.value.isEmpty()) {
//            }
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }?: Resource.failure(
                    R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        if (_isRefreshing.value) {
                            _timelineItems.value = timelineItems
                            _isRefreshing.value = false
                        } else {
                            _timelineItems.value = _timelineItems.value + timelineItems
                        }
//                        _timelineItems.value = _timelineItems.value + timelineItems

                        lastVisible = newLastVisible
                    }
                }
                Status.FAILURE -> {
                    Log.d("TLVM", "failure")
                }
                else -> {}
            }
        }
    }

    fun setScrollState(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

}