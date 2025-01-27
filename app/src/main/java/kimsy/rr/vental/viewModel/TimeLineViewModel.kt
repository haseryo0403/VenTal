package kimsy.rr.vental.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GetPopularTimeLineItemsUseCase
import kimsy.rr.vental.UseCase.GetRecentTimeLineItemsUseCase
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
    private val getRecentTimeLineItemsUseCase: GetRecentTimeLineItemsUseCase,
    private val getPopularTimeLineItemsUseCase: GetPopularTimeLineItemsUseCase
): ViewModel() {

    init {
        Log.d("TLVM" , "initialized")
    }

    var recentItemSavedScrollIndex by mutableStateOf(0)
    var recentItemSavedScrollOffset by mutableStateOf(0)

    var popularItemSavedScrollIndex by mutableStateOf(0)
    var popularItemSavedScrollOffset by mutableStateOf(0)

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()


    //各VM
    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    //各VM
    private val _recentTimelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val recentTimelineItems: StateFlow<List<DebateItem>> get() = _recentTimelineItems

    //各VM
    private val _popularTimelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val popularTimelineItems: StateFlow<List<DebateItem>> get() = _popularTimelineItems

    private var recentItemLastVisible: DocumentSnapshot? = null
    private var popularItemLastVisible: DocumentSnapshot? = null

    //各VM
    var hasFinishedLoadingAllRecentItems by mutableStateOf(false)
        private set
    //各VM
    var hasFinishedLoadingAllPopularItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    //各VM
    fun onRefreshRecentItem() {
        viewModelScope.launch {
            _isRefreshing.value = true
            recentItemLastVisible = null
            getRecentTimeLineItems()
        }
    }

    fun onRefreshPopularItem() {
        viewModelScope.launch {
            _isRefreshing.value = true
            popularItemLastVisible = null
            getPopularTimeLineItems()
        }
    }

    fun onLikeSuccess(debateItem: DebateItem) {
        val index = _recentTimelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            _recentTimelineItems.value = _recentTimelineItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    //各VM
    suspend fun getRecentTimeLineItems() {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser?.let { getRecentTimeLineItemsUseCase.execute(recentItemLastVisible, it) }?: Resource.failure(
                    R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllRecentItems = true
                        }
                        if (_isRefreshing.value) {
                            _recentTimelineItems.value = timelineItems
                            _isRefreshing.value = false
                        } else {
                            _recentTimelineItems.value = _recentTimelineItems.value + timelineItems
                        }
                        recentItemLastVisible = newLastVisible
                    }
                }
                Status.FAILURE -> {
                   Log.d("TLVM", "failure")


                }
                else -> {}
            }
        }
    }

    suspend fun getPopularTimeLineItems() {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser?.let { getPopularTimeLineItemsUseCase.execute(popularItemLastVisible, it) }?: Resource.failure(
                    R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllPopularItems = true
                        }
                        if (_isRefreshing.value) {
                            _popularTimelineItems.value = timelineItems
                            _isRefreshing.value = false
                        } else {
                            _popularTimelineItems.value = _popularTimelineItems.value + timelineItems
                        }
                        popularItemLastVisible = newLastVisible
                    }
                }
                Status.FAILURE -> {
                   Log.d("TLVM", "failure")


                }
                else -> {}
            }
        }
    }



    fun setRecentItemScrollState(index: Int, offset: Int) {
        recentItemSavedScrollIndex = index
        recentItemSavedScrollOffset = offset
    }

    fun setPopularItemScrollState(index: Int, offset: Int) {
        popularItemSavedScrollIndex = index
        popularItemSavedScrollOffset = offset
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

}