package kimsy.rr.vental.ViewModel

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
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
import com.google.firebase.ktx.Firebase
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // 状態管理用の変数
    var isLoading by mutableStateOf(false)
        private set

    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> = _authResult


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
            }
        } catch (e: ApiException) {
            isLoading = false
            Log.e("TAG", "Google sign-in failed: ${e.statusCode}")
        }
    }
//TODO Delete
//    fun firebaseAuthWithGoogle(idToken: String) {
//        Log.d("TAG","firebase signIn executed")
//        viewModelScope.launch {
//            //FirebaseAuth
//            val firebaseResult = userRepository.firebaseAuthWithGoogle(idToken)
//            if (firebaseResult.isSuccess) {
//                Log.d("TAG", "FireBaseAuth Success")
//                var result: List<User>
//                viewModelScope.launch {
//                    //FireStoreからユーザー取得
//                    result = userRepository.getUser()
//                    Log.d("TAG", result.toString())
//                    if (result.isEmpty()) {
//                        // データを取得できなかった場合(初回ログイン)
//                        // 新規ユーザー登録
//                        Log.d("TAG", "New User")
//                        userRepository.saveUserToFirestore()
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
    fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("TAG","firebase signIn executed")
        viewModelScope.launch {
            //FirebaseAuth
            val firebaseResult = userRepository.firebaseAuthWithGoogle(idToken)
            if (firebaseResult.isSuccess) {
                Log.d("TAG", "FireBaseAuth Success")
                viewModelScope.launch {
                    //FireStoreからユーザー取得
                    val result = userRepository.getCurrentUser()
                    Log.d("TAG", result.toString())
                    if (result == null) {
                        // データを取得できなかった場合(初回ログイン)
                        // 新規ユーザー登録
                        Log.d("TAG", "No data found in firestore")
                        userRepository.saveUserToFirestore()
                        //TODO ユーザー情報登録に失敗した場合
                        _authResult.value = false

                    } else {
                        // データを取得できた場合(２回目以降ログイン)
                        // ログインした後一番最初に表示したい画面に移動
                        Log.d("TAG", "Old User")
                        _authResult.value = true
                    }
                }
            } else {
                isLoading = false
                // Firebaseサインイン失敗
                Log.d("TAG", "firebase fail")
            }
        }
    }
}