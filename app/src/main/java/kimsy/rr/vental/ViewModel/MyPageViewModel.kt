package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GetUserPageDataUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserPageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getUserPageDataUseCase: GetUserPageDataUseCase
): ViewModel() {
    //    val currentUser: LiveData<User> = mainViewModel.currentUser
//    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _currentUser = MutableStateFlow<User?>(User.CurrentUserShareModel.getCurrentUserFromModel())
    val currentUser: StateFlow<User?> get() = _currentUser

    private val _userPageDataState = MutableStateFlow<Resource<UserPageData>>(Resource.idle())
        val userPageDateState: StateFlow<Resource<UserPageData>> get() = _userPageDataState


    var savedScrollIndex by mutableStateOf(0)
    var savedScrollOffset by mutableStateOf(0)

    fun setScrollState(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
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

    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()
    }

}