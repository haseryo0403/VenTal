package kimsy.rr.vental.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetDebatesRelatedUserUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyDebateViewModel @Inject constructor(
    private val getDebatesRelatedUserUseCase: GetDebatesRelatedUserUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _myPageItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val myPageItems: StateFlow<List<DebateItem>> get() = _myPageItems

    //index 0のdebateItem用
    var debateItemSavedScrollIndex by mutableStateOf(0)
    var debateItemSavedScrollOffset by mutableStateOf(0)

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private var debateItemLastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllDebateItems by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing


    fun setDebateItemScrollState(index: Int, offset: Int) {
        debateItemSavedScrollIndex = index
        debateItemSavedScrollOffset = offset
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            debateItemLastVisible = null
            getMyPageDebateItems()
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
            _getDebateItemsState.value = getDebatesRelatedUserUseCase.execute(debateItemLastVisible, _currentUser.value.uid)
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    _getDebateItemsState.value.data?.let { (myPageItems, newLastVisible) ->
                        if(myPageItems.isEmpty()) {
                            hasFinishedLoadingAllDebateItems = true
                        }
                        if (_isRefreshing.value) {
                            _myPageItems.value = myPageItems
                            _isRefreshing.value = false
                        } else {
                            _myPageItems.value = _myPageItems.value +myPageItems
                        }
                        debateItemLastVisible = newLastVisible
                    }
                }
                else -> {}
            }
        }
    }


    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()?: User()
    }


    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

}