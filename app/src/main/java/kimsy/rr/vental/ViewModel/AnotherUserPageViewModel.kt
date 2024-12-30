package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GetUserPageDataUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserPageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnotherUserPageViewModel @Inject constructor(
    private val getUserPageDataUseCase: GetUserPageDataUseCase
): ViewModel() {
    //    val currentUser: LiveData<User> = mainViewModel.currentUser
//    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _anotherUser = MutableStateFlow(User.AnotherUserShareModel.getAnotherUser())
    val anotherUser: StateFlow<User?> get() = _anotherUser

    private val _userPageDataState = MutableStateFlow<Resource<UserPageData>>(Resource.idle())
    val userPageDateState: StateFlow<Resource<UserPageData>> get() = _userPageDataState

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private val _anotherUserPageItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val anotherUserPageItems: StateFlow<List<DebateItem>> get() = _anotherUserPageItems

    private var lastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

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

    fun updateAnotherUser() {
        _anotherUser.value = User.AnotherUserShareModel.getAnotherUser()
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

}