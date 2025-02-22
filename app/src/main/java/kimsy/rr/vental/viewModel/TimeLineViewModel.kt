package kimsy.rr.vental.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetPopularTimeLineItemsUseCase
import kimsy.rr.vental.UseCase.GetRecentTimeLineItemsUseCase
import kimsy.rr.vental.UseCase.MarkUserNotNewUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val getRecentTimeLineItemsUseCase: GetRecentTimeLineItemsUseCase,
    private val getPopularTimeLineItemsUseCase: GetPopularTimeLineItemsUseCase,
    private val markUserNotNewUseCase: MarkUserNotNewUseCase
): ViewModel() {
    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    var recentItemSavedScrollIndex by mutableStateOf(0)
    var recentItemSavedScrollOffset by mutableStateOf(0)

    var popularItemSavedScrollIndex by mutableStateOf(0)
    var popularItemSavedScrollOffset by mutableStateOf(0)

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private val _recentTimelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val recentTimelineItems: StateFlow<List<DebateItem>> get() = _recentTimelineItems

    private val _popularTimelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val popularTimelineItems: StateFlow<List<DebateItem>> get() = _popularTimelineItems

    private var recentItemLastVisible: DocumentSnapshot? = null
    private var popularItemLastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllRecentItems by mutableStateOf(false)
        private set

    var hasFinishedLoadingAllPopularItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    fun onRefreshRecentItem() {
        viewModelScope.launch {
            _isRefreshing.value = true
            recentItemLastVisible = null
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

    fun onRecentDebateLikeSuccess(debateItem: DebateItem) {
        val index = _recentTimelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            _recentTimelineItems.value = _recentTimelineItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    fun onPopularDebateLikeSuccess(debateItem: DebateItem) {
        val index = _popularTimelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            _popularTimelineItems.value = _popularTimelineItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    suspend fun getRecentTimeLineItems() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value = getRecentTimeLineItemsUseCase.execute(recentItemLastVisible, _currentUser.value.uid)
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
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
                    delay(500) //待機しないとローディング矢印が固まる
                    if (_isRefreshing.value) {
                        _isRefreshing.value = false
                    }
                }
                else -> {}
            }
        }
    }

    suspend fun getPopularTimeLineItems() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =getPopularTimeLineItemsUseCase.execute(popularItemLastVisible, _currentUser.value.uid)
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
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
                        delay(500) //待機しないとローディング矢印が固まる
                        if (_isRefreshing.value) {
                            _isRefreshing.value = false
                        }
                }
                else -> {}
            }
        }
    }

    fun markUserNotNew() {
        viewModelScope.launch {
            val result = markUserNotNewUseCase.execute(_currentUser.value.uid)
            if (result.status == Status.SUCCESS) {
                val user = _currentUser.value.copy(
                    newUserFlag = false
                )
                User.CurrentUserShareModel.setCurrentUserToModel(
                    user
                )
                _currentUser.value = user
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

    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()?: User()
    }

}