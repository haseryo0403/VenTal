package kimsy.rr.vental.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.FollowUseCase
import kimsy.rr.vental.UseCase.GetFollowingUserIdsUseCase
import kimsy.rr.vental.UseCase.GetUserInfoByUserIdListUseCase
import kimsy.rr.vental.UseCase.LoadDebateByUserIdsListUseCase
import kimsy.rr.vental.UseCase.ObserveFollowingUserIdUseCase
import kimsy.rr.vental.UseCase.UnFollowUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowPageViewModel @Inject constructor(
    private val getFollowingUserIdsUseCase: GetFollowingUserIdsUseCase,
    private val loadDebateByUserIdsListUseCase: LoadDebateByUserIdsListUseCase,
    private val getUserInfoByUserIdListUseCase: GetUserInfoByUserIdListUseCase,
    private val observeFollowingUserIdUseCase: ObserveFollowingUserIdUseCase,
    private val followUseCase: FollowUseCase,
    private val unFollowUseCase: UnFollowUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    val currentUserId = _currentUser.value.uid

    private val _followingUser = MutableStateFlow<List<User>>(emptyList())
    val followingUser: StateFlow<List<User>> get() = _followingUser

    private val _followingUserIds = MutableStateFlow<List<String>>(emptyList())
    val followingUserIds: StateFlow<List<String>> get() = _followingUserIds

    private val _followingUserIdsState = MutableStateFlow<Resource<List<String>>>(Resource.idle())
    val followingUserIdsState: StateFlow<Resource<List<String>>> get() = _followingUserIdsState

    private val _followState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val followState: StateFlow<Resource<Unit>> get() = _followState

    private val _followingUserState = MutableStateFlow<Resource<List<User>>>(Resource.idle())
    val followingUserState: StateFlow<Resource<List<User>>> get() = _followingUserState

    var startAfterDate: Timestamp? = null
        private set

    var _loadingDebateItems: List<DebateItem?> = emptyList()
        private set

    private val _debateItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val debateItems: StateFlow<List<DebateItem>> get() = _debateItems

    //index 0のdebateItem用
    var debateItemSavedScrollIndex by mutableStateOf(0)
    var debateItemSavedScrollOffset by mutableStateOf(0)

    private val _getDebateItemsState = MutableStateFlow<Resource<List<DebateItem>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<List<DebateItem>>> get() = _getDebateItemsState

    var hasFinishedLoadingAllDebateItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            startAfterDate = null
            loadFollowingUserDebates()
        }
    }

    suspend fun loadFollowingUserDebates() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            val success = getFollowingUserIds(currentUserId)
            if (success) {
                val start = startAfterDate?: Timestamp(Timestamp.now().seconds + 1, Timestamp.now().nanoseconds)
                // 今は仮で2週間先
                val end = Timestamp(start.seconds - 14 * 24 * 60 * 60, start.nanoseconds)
                if (_followingUserIds.value.isEmpty()) {
                    _getDebateItemsState.value = Resource.success(emptyList())
                    return@launch
                } else {
                    val chunks = _followingUserIds.value.chunked(10)
                    chunks.forEach { chunk ->
                        _getDebateItemsState.value = loadDebateByUserIdsListUseCase.execute(
                            userIds = chunk,
                            currentUserId = currentUserId,
                            startAfterDate = start,
                            endAtDate = end
                        )
                        when (_getDebateItemsState.value.status) {
                            Status.SUCCESS -> {
                                //TODO たぶん共通だとよくない
                                _getDebateItemsState.value.data?.let { debateItems ->
                                    _loadingDebateItems = _loadingDebateItems + debateItems
                                }
                            }
                            else -> {}
                        }
                    }

                }
                if (_loadingDebateItems.isNotEmpty()) {
                    val sortedDebateItem =  _loadingDebateItems
                        .filterNotNull()
                        .sortedWith( compareBy<DebateItem> { it.debate.debateCreatedDatetime}.reversed() )
                    if (_isRefreshing.value) {
                        _debateItems.value = sortedDebateItem
                        _isRefreshing.value = false
                    } else {
                        _debateItems.value = _debateItems.value + sortedDebateItem
                    }
                    _loadingDebateItems = emptyList()
                    startAfterDate = Timestamp(end.seconds - 1, end.nanoseconds)
                } else {
                    hasFinishedLoadingAllDebateItems = true
                }
            } else {
                return@launch
            }
        }
    }

    suspend fun getFollowingUserInfo() {
        viewModelScope.launch {
            if (_followingUserIds.value.isEmpty()) {
                _followingUserState.value = Resource.success(emptyList())
            } else {
                _followingUserState.value = Resource.loading()
                val userIds = _followingUserIds.value
                _followingUserState.value = getUserInfoByUserIdListUseCase.execute(userIds)
                if (_followingUserState.value.status == Status.SUCCESS) {
                    _followingUserState.value.data?.let{_followingUser.value = it}
                }
            }
        }
    }

    private suspend fun getFollowingUserIds(currentUserId: String): Boolean {
        val getFollowingUserIdsState = getFollowingUserIdsUseCase.execute(currentUserId)
        return when (getFollowingUserIdsState.status) {
            Status.SUCCESS -> {
                _followingUserIds.value = getFollowingUserIdsState.data?: emptyList()
                getFollowingUserInfo()
                true
            }

            else -> {
                _getDebateItemsState.value = Resource.failure()
                false
            }
        }
    }

    fun onLikeSuccess(debateItem: DebateItem) {
        val index = _debateItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            _debateItems.value = _debateItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    fun observeFollowingUserIds() {
//        viewModelScope.launch {
//            observeFollowingUserIdUseCase.execute(currentUserId)
//                .collect{ resource ->
//                    _followingUserIdsState.value = resource
//                }
//        }
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

    fun setScrollState(index: Int, offset: Int) {
        debateItemSavedScrollIndex = index
        debateItemSavedScrollOffset = offset
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }



}