package kimsy.rr.vental.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kimsy.rr.vental.UseCase.FinishMainActivityUseCase
import kimsy.rr.vental.UseCase.GetGoogleIdTokenUseCase
import kimsy.rr.vental.UseCase.LoadCurrentUserUseCase
import kimsy.rr.vental.UseCase.ReLoginUseCase
import kimsy.rr.vental.UseCase.SaveDeviceTokenUseCase
import kimsy.rr.vental.UseCase.SaveUserUseCase
import kimsy.rr.vental.UseCase.SignInAndFetchUserUseCase
import kimsy.rr.vental.UseCase.SignOutUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loadCurrentUserUseCase: LoadCurrentUserUseCase,
    private val saveDeviceTokenUseCase: SaveDeviceTokenUseCase,
    private val signInAndFetchUserUseCase: SignInAndFetchUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val finishMainActivityUseCase: FinishMainActivityUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val getGoogleIdTokenUseCase: GetGoogleIdTokenUseCase,
    private val reLoginUseCase: ReLoginUseCase
    ) : ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    val currentUserId = _currentUser.value.uid

    //trueですでにFirebaseにユーザーがあるか、新規ユーザー登録が完了したということ
    private val _authState = MutableStateFlow<Resource<Boolean>>(Resource.idle())
    val authState: StateFlow<Resource<Boolean>> get() = _authState

    private val _signOutState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val signOutState: StateFlow<Resource<Unit>> get() = _signOutState

    init {
        loadCurrentUser()
    }

    // Googleサインインを開始するメソッド
    fun signInWithGoogle(activityResultLauncher: ActivityResultLauncher<Intent>) {
        _authState.value = Resource.loading()
        userRepository.signInWithGoogle(activityResultLauncher)
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            val googleIdTokenState = getGoogleIdTokenUseCase.execute(data)
            when(googleIdTokenState.status) {
                Status.SUCCESS -> {
                    if (googleIdTokenState.data != null) {
                        firebaseAuthWithGoogle(googleIdTokenState.data)
                    } else {
                        _authState.value = Resource.failure()
                    }
                }
                Status.FAILURE -> {
                    _authState.value = Resource.failure()
                }
                else -> {}
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            val signInState = signInAndFetchUserUseCase.execute(idToken)
            when(signInState.status) {
                Status.SUCCESS -> {
                    val user = signInState.data
                    if (user == null) {
                        saveNewUser()
                    } else {
                        if (user.accountClosingFlag) {
                            reLogin(user)
                        } else {
                            User.CurrentUserShareModel.setCurrentUserToModel(user)
                            _authState.value = Resource.success(true)
                        }

                    }
                }
                Status.FAILURE -> {
                    _authState.value = Resource.failure()
                }
                else -> {}
            }
        }
    }

    private fun saveNewUser() {
        viewModelScope.launch {
            val saveUserState = saveUserUseCase.execute()
            when(saveUserState.status) {
                Status.SUCCESS -> {
                    if (saveUserState.data != null) {
                        User.CurrentUserShareModel.setCurrentUserToModel(saveUserState.data)
                        _authState.value = Resource.success(true)
                    } else {
                        _authState.value = Resource.failure()
                    }

                }
                Status.FAILURE -> {
                    _authState.value = Resource.failure()
                }
                else -> {}
            }
        }
    }

    private fun reLogin(user: User) {
        viewModelScope.launch {
            val reLogInState = reLoginUseCase.execute(user.uid)
            when(reLogInState.status) {
                Status.SUCCESS -> {
                    User.CurrentUserShareModel.setCurrentUserToModel(user)
                    _authState.value = Resource.success(true)
                }
                Status.FAILURE -> {
                    _authState.value = Resource.failure()
                }
                else -> {}
            }
        }
    }

    private fun saveDeviceToken() {
        viewModelScope.launch {
            saveDeviceTokenUseCase.execute(_currentUser.value.uid)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun loadCurrentUser(){
        viewModelScope.launch {
            val loadCurrentUserState = loadCurrentUserUseCase.execute()
            when(loadCurrentUserState.status) {
                Status.SUCCESS -> {
                    val user = loadCurrentUserState.data
                    if (user != null) {
                        _currentUser.value = user
                        User.CurrentUserShareModel.setCurrentUserToModel(user)
                        saveDeviceToken()
                    }
                }
                Status.FAILURE -> {
                    _authState.value = Resource.failure()
                }
                else -> {}
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun signOut(context: Context) {
        viewModelScope.launch {
            _signOutState.value = signOutUseCase.execute()
                when(_signOutState.value.status) {
                    Status.SUCCESS -> {
                        User.CurrentUserShareModel.resetCurrentUserOnModel()
                        finishMainActivityUseCase.execute(context)
                    }
                    else -> {

                    }
                }
        }
    }

    fun resetState() {
        _authState.value = Resource.idle()
    }

}