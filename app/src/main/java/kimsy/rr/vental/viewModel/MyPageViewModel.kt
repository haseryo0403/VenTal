package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetDebateCountsRelatedUserUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getDebateCountsRelatedUserUseCase: GetDebateCountsRelatedUserUseCase
    ): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _debateCountsState = MutableStateFlow<Resource<Int>>(Resource.idle())
        val debateCountsState: StateFlow<Resource<Int>> get() = _debateCountsState

    suspend fun loadUserPageData() {
        viewModelScope.launch {
            _debateCountsState.value = Resource.loading()
            _debateCountsState.value = getDebateCountsRelatedUserUseCase.execute(currentUser.value.uid)
        }
    }

    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()?: User()
    }
}