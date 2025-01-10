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
import kimsy.rr.vental.UseCase.FollowUseCase
import kimsy.rr.vental.UseCase.GetDebatesRelatedUserUseCase
import kimsy.rr.vental.UseCase.GetUserPageDataUseCase
import kimsy.rr.vental.UseCase.ObserveFollowingUserIdUseCase
import kimsy.rr.vental.UseCase.UnFollowUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserPageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnotherUserPageViewModel @Inject constructor(
    private val getUserPageDataUseCase: GetUserPageDataUseCase,
    private val observeFollowingUserIdUseCase: ObserveFollowingUserIdUseCase,
    private val followUseCase: FollowUseCase,
    private val unFollowUseCase: UnFollowUseCase,
    private val getDebatesRelatedUserUseCase: GetDebatesRelatedUserUseCase
): ViewModel() {

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _anotherUser = MutableStateFlow(User.AnotherUserShareModel.getAnotherUser())
    val anotherUser: StateFlow<User?> get() = _anotherUser

    private val _userPageDataState = MutableStateFlow<Resource<UserPageData>>(Resource.idle())
    val userPageDateState: StateFlow<Resource<UserPageData>> get() = _userPageDataState

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private val _debateItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val debateItems: StateFlow<List<DebateItem>> get() = _debateItems

    private val _anotherUserPageItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val anotherUserPageItems: StateFlow<List<DebateItem>> get() = _anotherUserPageItems

    private var lastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    private val _followingUserIdsState = MutableStateFlow<Resource<List<String>>>(Resource.idle())
    val followingUserIdsState: StateFlow<Resource<List<String>>> get() = _followingUserIdsState

    private val _followState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val followState: StateFlow<Resource<Unit>> get() = _followState

    var savedScrollIndex by mutableStateOf(0)
    var savedScrollOffset by mutableStateOf(0)

    fun setScrollState(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            lastVisible = null
//            getAnotherUserPageDebateItems()
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

    suspend fun loadUserPageData() {
        viewModelScope.launch {
            _userPageDataState.value = Resource.loading()
            if (_anotherUser.value != null) {
                val result = getUserPageDataUseCase.execute(_anotherUser.value!!.uid, true)
                _userPageDataState.value = result
            } else {
                _userPageDataState.value = Resource.failure("${R.string.no_user_found}")
            }
        }
    }

    fun observeFollowingUserIds() {
        viewModelScope.launch {
            currentUser?.let {
                observeFollowingUserIdUseCase.execute(it.uid)
                    .collect{ resource ->
                        _followingUserIdsState.value = resource
                    }
            }
        }
    }

    fun followUser(toUserId: String) {
        viewModelScope.launch {
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                currentUser?.let {
                    _followState.value = followUseCase.execute(it.uid, toUserId)
                }
            }
        }
    }

    fun unFollowUser(toUserId: String) {
        viewModelScope.launch {
            //一旦フォローアンフォロー共有State
            _followState.value = Resource.loading()
            if (_followingUserIdsState.value.status == Status.SUCCESS) {
                currentUser?.let {
                    _followState.value = unFollowUseCase.execute(it.uid, toUserId)
                }
            }
        }
    }

    suspend fun getAnotherUserPageDebateItems() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =

                anotherUser.value?.let { getDebatesRelatedUserUseCase.execute(lastVisible, it.uid) } ?: Resource.failure(
                    R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (debateItems, newLastVisible) ->
                        if(debateItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        if (_isRefreshing.value) {
                            _debateItems.value = debateItems
                            _isRefreshing.value = false
                        } else {
                            _debateItems.value = _debateItems.value + debateItems
                        }
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


//
//    fun updateAnotherUser() {
//        _anotherUser.value = User.AnotherUserShareModel.getAnotherUser()
//    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

    fun resetState() {
        _followState.value = Resource.idle()
        _followingUserIdsState.value = Resource.idle()
    }

}