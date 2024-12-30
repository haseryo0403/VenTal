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
import kimsy.rr.vental.UseCase.GetDebatesRelatedUserUseCase
import kimsy.rr.vental.UseCase.GetUserPageDataUseCase
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
class MyPageViewModel @Inject constructor(
    private val getUserPageDataUseCase: GetUserPageDataUseCase,
    private val getDebatesRelatedUserUseCase: GetDebatesRelatedUserUseCase,

    ): ViewModel() {
    //    val currentUser: LiveData<User> = mainViewModel.currentUser
//    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _currentUser = MutableStateFlow<User?>(User.CurrentUserShareModel.getCurrentUserFromModel())
    val currentUser: StateFlow<User?> get() = _currentUser

    private val _userPageDataState = MutableStateFlow<Resource<UserPageData>>(Resource.idle())
        val userPageDateState: StateFlow<Resource<UserPageData>> get() = _userPageDataState

    var savedScrollIndex by mutableStateOf(0)
    var savedScrollOffset by mutableStateOf(0)

    private val _myPageItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val myPageItems: StateFlow<List<DebateItem>> get() = _myPageItems

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private var lastVisible: DocumentSnapshot? = null

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    fun setScrollState(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            lastVisible = null
            getMyPageDebateItems()
        }
    }

    //myPageではすでにcurrentUserを持っているのでUserPageDataのuserは使わない
    suspend fun loadUserPageData() {
        viewModelScope.launch {
            _userPageDataState.value = Resource.loading()
            if (currentUser.value != null) {
                val result = getUserPageDataUseCase.execute(currentUser.value!!.uid, true)
                _userPageDataState.value = result
            } else {
                _userPageDataState.value = Resource.failure("${R.string.no_user_found}")
            }
        }
    }

    fun onLikeSuccess(debateItem: DebateItem) {
        val index = _myPageItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (index != -1) {
            _myPageItems.value = _myPageItems.value.toMutableList().apply {
                this[index] = debateItem
            }
        }
    }

    suspend fun getMyPageDebateItems() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser.value?.let { getDebatesRelatedUserUseCase.execute(lastVisible, it.uid) } ?: Resource.failure(R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (myPageItems, newLastVisible) ->
                        if(myPageItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        if (_isRefreshing.value) {
                            _myPageItems.value = myPageItems
                            _isRefreshing.value = false
                        } else {
                            _myPageItems.value = _myPageItems.value +myPageItems
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

    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

}