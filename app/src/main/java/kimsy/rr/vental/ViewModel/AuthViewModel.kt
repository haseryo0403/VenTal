package kimsy.rr.vental.ViewModel

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kimsy.rr.vental.UseCase.LoadCurrentUserUseCase
import kimsy.rr.vental.UseCase.SaveDeviceTokenUseCase
import kimsy.rr.vental.UseCase.SignInAndFetchUserUseCase
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.repository.UserRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loadCurrentUserUseCase: LoadCurrentUserUseCase,
    private val saveDeviceTokenUseCase: SaveDeviceTokenUseCase,
    private val auth: FirebaseAuth,
    private val signInAndFetchUserUseCase: SignInAndFetchUserUseCase


    ) : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser
    init {
        loadCurrentUser()
    }

    // 状態管理用の変数
    var isLoading by mutableStateOf(false)
        private set

    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }

    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> = _authResult

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> get() = _signOutResult

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage


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
            isLoading = false
            Log.e("TAG", "Google sign-in failed: ${e.statusCode}")
            _errorMessage.value = e.message ?: "不明なエラーが発生しました"
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
//            try {
                signInAndFetchUserUseCase.execute(idToken).onSuccess {
                    _authResult.value = true
                }.onFailure {exception->
                    Log.e("AVM", "signInAndFetchUserUseCase.execute failure")
                    _authResult.value = false
                    isLoading = false
                    _errorMessage.value = exception.message ?: "不明なエラーが発生しました"
                }
//            } catch (e: Exception) {
//                Log.d("TAG", "firebase fail")
//                isLoading = false
//            }
        }
    }
//
//    fun firebaseAuthWithGoogles(idToken: String) {
//        Log.d("TAG","firebase signIn executed")
//        viewModelScope.launch {
//            //FirebaseAuth
//            val firebaseResult = userRepository.firebaseAuthWithGoogle(idToken)
//            if (firebaseResult.isSuccess) {
//                Log.d("TAG", "FireBaseAuth Success")
//                viewModelScope.launch {
//                    //FireStoreからユーザー取得
//                    var result = userRepository.getCurrentUser()
//                    Log.d("TAG", result.toString())
//                    if (result == null) {
//                        // データを取得できなかった場合(初回ログイン)
//                        // 新規ユーザー登録
//                        Log.d("TAG", "No data found in firestore")
//                        userRepository.saveUserToFirestore()
//                        //TODO ユーザー情報登録に失敗した場合
//                        _authResult.value = false
//
//                    } else {
//                        // データを取得できた場合(２回目以降ログイン)
//                        // ログインした後一番最初に表示したい画面に移動
//                        Log.d("TAG", "Old User")
//                        _authResult.value = true
//                    }
//                }
//            } else {
//                isLoading = false
//                // Firebaseサインイン失敗
//                Log.d("TAG", "firebase fail")
//            }
//        }
//    }
//
//    @SuppressLint("SuspiciousIndentation")
//    fun loadCurrentUsers(){
//        Log.d("TAG　AuthViewModel", "load Current User")
//        viewModelScope.launch {
//            //FireStoreからユーザー取得
//            val result = userRepository.getCurrentUsers()//TODO いじった1127
//            Log.d("TAG", result.toString())
//            if (result != null) {
//                _currentUser.value = result
//
//            } else {
//                // データを取得できなかった場合
//                _currentUser.value = null  // 必要に応じてnullをセット
//            }
//        }
//    }

    private fun saveDeviceToken() {
        viewModelScope.launch {
            try {
                currentUser.value?.let { saveDeviceTokenUseCase(it.uid) }
                Log.d("AuthViewModel", "Device token saved")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to save device token: ${e.message}", e)
            }
        }
    }

    fun saveDeviceTokenToFirestore() {
        val user = Firebase.auth.currentUser ?: return
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Firebase.firestore.collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error saving token to Firestore", e)
                }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun loadCurrentUser(){
        Log.d("TAG　AuthViewModel", "load Current User")
        viewModelScope.launch {
            //FireStoreからユーザー取得
            val result = loadCurrentUserUseCase.execute()
            result.onSuccess {user->
                _currentUser.value = user
                saveDeviceToken()
            }.onFailure {exception->
                //TODO error handling
//                _errorMessage.value = exception.message ?: "不明なエラーが発生しました"
            }
        }
    }

    fun signOut(
        onSignOutComplete: () -> Unit
    ) {
        auth.signOut()
        val result = userRepository.signOutFromGoogle()
        if (result.isSuccess){
            Log.e("Sign out", "sign out success")
            onSignOutComplete()
        } else{
            Log.e("SignOut", "Sign Out failed")
        }
    }

}