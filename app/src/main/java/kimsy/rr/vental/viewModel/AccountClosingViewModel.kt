package kimsy.rr.vental.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.CloseAccountUseCase
import kimsy.rr.vental.UseCase.FinishMainActivityUseCase
import kimsy.rr.vental.UseCase.SignOutUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountClosingViewModel @Inject constructor(
    private val closeAccountUseCase: CloseAccountUseCase,
    private val finishMainActivityUseCase: FinishMainActivityUseCase,
    private val signOutUseCase: SignOutUseCase
    ): ViewModel(){
    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _accountClosingState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val accountClosingState: StateFlow<Resource<Unit>> get() = _accountClosingState

    private val _signOutState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val signOutState: StateFlow<Resource<Unit>> get() = _signOutState

    fun closeAccount(reasonNumber: Int, context: Context) {
        viewModelScope.launch {
            _accountClosingState.value = Resource.idle()
            _accountClosingState.value =
                closeAccountUseCase.execute(_currentUser.value.uid ,reasonNumber)
            if (_accountClosingState.value.status == Status.SUCCESS) {
                signOut(context)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun signOut(context: Context) {
        viewModelScope.launch {
            _signOutState.value = signOutUseCase.execute()
            when(_signOutState.value.status) {
                Status.SUCCESS -> {
                    finishMainActivityUseCase.execute(context)
                }
                else -> {

                }
            }
        }
    }

    fun resetState() {
        _accountClosingState.value = Resource.idle()
    }
}