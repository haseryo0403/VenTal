package kimsy.rr.vental.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.LoadLikedDebateIdsUseCase
import kimsy.rr.vental.UseCase.LoadLikedDebateItemsUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.SplitList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyLikedDebateViewModel @Inject constructor(
    private val loadLikedDebateIdsUseCase: LoadLikedDebateIdsUseCase,
    private val loadLikedDebateItemsUseCase: LoadLikedDebateItemsUseCase
): ViewModel(){

    private val _currentUser = MutableStateFlow<User?>(User.CurrentUserShareModel.getCurrentUserFromModel())
    val currentUser: StateFlow<User?> get() = _currentUser

    var likedDebateItemSavedScrollIndex by mutableStateOf(0)
    var likedDebateItemSavedScrollOffset by mutableStateOf(0)

    private var likedDebateIdsSplitBy10 by mutableStateOf<List<List<String>>>(emptyList())
    private var currentLikedDebateIdsIndex by mutableStateOf(0)

    private val _likedDebateItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val likedDebateItems: StateFlow<List<DebateItem>> get() = _likedDebateItems

    private val _loadLikedDebateItemsState = MutableStateFlow<Resource<List<DebateItem>>>(
        Resource.idle()
    )
    val loadLikedDebateItemsState: StateFlow<Resource<List<DebateItem>>>
        get() = _loadLikedDebateItemsState

    var hasFinishedLoadingAllLikedDebateItems by mutableStateOf(false)
        private set


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            currentLikedDebateIdsIndex = 0
//            if (hasFinishedLoadingAllLikedDebateItems) {
//                delay(500)
//                _isRefreshing.value = false
//            } else {
//                loadLikedDebates()
//            }
            hasFinishedLoadingAllLikedDebateItems = false
            loadLikedDebates()


        }
    }

    fun setLikedDebateItemScrollState(index: Int, offset: Int) {
        likedDebateItemSavedScrollIndex = index
        likedDebateItemSavedScrollOffset = offset
    }

    fun onLikeSuccess(debateItem: DebateItem) {
//        val myPageItemindex = _myPageItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
//        if (myPageItemindex != -1) {
//            _myPageItems.value = _myPageItems.value.toMutableList().apply {
//                this[myPageItemindex] = debateItem
//            }
//        }
        val likedDebateItemIndex = _likedDebateItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
        if (likedDebateItemIndex != -1) {
            _likedDebateItems.value = _likedDebateItems.value.toMutableList().apply {
                this[likedDebateItemIndex] = debateItem
            }
        }
    }

    suspend fun loadLikedDebates() {
        viewModelScope.launch {
            if (hasFinishedLoadingAllLikedDebateItems) {
                _loadLikedDebateItemsState.value = Resource.failure("status not success")
                return@launch
            }
            _loadLikedDebateItemsState.value = Resource.loading()
            val likedDebateIdsState = _currentUser.value?.let { loadLikedDebateIdsUseCase.execute(it.uid) }?: Resource.failure(
                R.string.no_user_found.toString())
            if (likedDebateIdsState.status == Status.SUCCESS) {
                val likedDebateIds = likedDebateIdsState.data!!
                likedDebateIdsSplitBy10 = SplitList(likedDebateIds, 10)
            } else {
                _loadLikedDebateItemsState.value = Resource.failure("status not success")
                return@launch
            }

            _loadLikedDebateItemsState.value =
                _currentUser.value?.let {
                    loadLikedDebateItemsUseCase.execute(likedDebateIdsSplitBy10[currentLikedDebateIdsIndex], it.uid)
                }?: Resource.failure(R.string.no_user_found.toString())

            when (_loadLikedDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _loadLikedDebateItemsState.value.data?.let { likedDebateItems ->
                        //TODO delete
                        if(likedDebateItems.isEmpty()) {
                            hasFinishedLoadingAllLikedDebateItems = true
                        }

                        if (_isRefreshing.value) {
                            _likedDebateItems.value = likedDebateItems
                            _isRefreshing.value = false
                        } else {
                            _likedDebateItems.value = _likedDebateItems.value + likedDebateItems
                        }
                        currentLikedDebateIdsIndex++
                        if (currentLikedDebateIdsIndex == likedDebateIdsSplitBy10.size) {
                            hasFinishedLoadingAllLikedDebateItems = true
                        }
                        //TODO delete?
                        _loadLikedDebateItemsState.value = Resource.idle()
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

}