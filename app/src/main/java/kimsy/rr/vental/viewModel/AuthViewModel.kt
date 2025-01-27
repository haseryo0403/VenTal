package kimsy.rr.vental.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kimsy.rr.vental.UseCase.FinishMainActivityUseCase
import kimsy.rr.vental.UseCase.LoadCurrentUserUseCase
import kimsy.rr.vental.UseCase.SaveDeviceTokenUseCase
import kimsy.rr.vental.UseCase.SaveUserUseCase
import kimsy.rr.vental.UseCase.SignInAndFetchUserUseCase
import kimsy.rr.vental.UseCase.SignOutUseCase
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
    private val saveUserUseCase: SaveUserUseCase


    ) : ViewModel() {

        //?を入れてみた
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser

    // 状態管理用の変数
    var isLoading by mutableStateOf(false)
        private set

    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }

    //TODO これとエラーメッセージを統合できそう？しなくてもいいかもだけど
//    private val _authResult = MutableLiveData<Boolean>()
//    val authResult: LiveData<Boolean> = _authResult

    private val _authResult = MutableStateFlow(false)
    val authResult: StateFlow<Boolean> get() = _authResult

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        Log.d("AVM","AVM is initialized")
        loadCurrentUser()
    }

    // Googleサインインを開始するメソッド
    fun signInWithGoogle(activityResultLauncher: ActivityResultLauncher<Intent>) {
        isLoading = true
        Log.d("TAG","signInWith Google executed")
        userRepository.signInWithGoogle(activityResultLauncher)
    }

    fun handleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                Log.e("TAG","idToken is null")
                _errorMessage.value = "idToken is null"
            }
        } catch (e: ApiException) {
            Log.e("TAG", "Google sign-in failed: ${e.statusCode}")
            setErrorMessage(e.message)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            signInAndFetchUserUseCase.execute(idToken)
                .onSuccess {user->
                    if (user == null) {
                        saveNewUser()
                    } else {
                        _authResult.value = true
                    }
                }.onFailure {exception->
                    Log.e("AVM", "signInAndFetchUserUseCase.execute failure")
                    _authResult.value = false
                    setErrorMessage(exception.message)

                }
        }
    }

    private fun saveNewUser() {
        viewModelScope.launch {
            saveUserUseCase.execute()
                .onSuccess {
                    _authResult.value = true
                }
                .onFailure {exception->
                    _authResult.value = false
                    setErrorMessage(exception.message)
                }
        }
    }

    private fun saveDeviceToken() {
        viewModelScope.launch {
            currentUser.value?.let { saveDeviceTokenUseCase.execute(it.uid) }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun loadCurrentUser(){
        Log.d("TAG　AuthViewModel", "load Current User")
        viewModelScope.launch {
            //FireStoreからユーザー取得
            loadCurrentUserUseCase.execute()
                .onSuccess {user->
                    Log.e("AVM", "loadUser Onsuccess")
                    if (user != null) {
                        _currentUser.value = user
                        User.CurrentUserShareModel.setCurrentUserToModel(user)
                        saveDeviceToken()
                    }
                }.onFailure {exception->
                    setErrorMessage(exception.message)
//                    Log.e("AVM", "${exception.message}")
                }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            signOutUseCase.execute()
                .onSuccess {
                    Log.d("AVM", "Sign out success")
                    finishMainActivityUseCase.execute(context)
                }
                .onFailure {exception->
                    Log.e("SignOut", "Sign Out failed")
                    setErrorMessage(exception.message)

                }
        }
    }

    private fun setErrorMessage(message: String?) {
        isLoading = false
        _errorMessage.value = message?: "不明なエラーが発生しました"
    }

    // エラーメッセージのリセット
    fun resetErrorMessage() {
        _errorMessage.value = null
    }

}