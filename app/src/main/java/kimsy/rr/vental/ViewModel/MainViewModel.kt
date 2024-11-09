package kimsy.rr.vental.ViewModel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.Screen
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth):ViewModel() {

    private val _currentScreen:MutableState<Screen> = mutableStateOf(Screen.BottomScreen.TimeLine)
    private val _currentUser = MutableLiveData<User>()

    init {
        loadCurrentUser()
        Log.e("VM initialization", "MVM initialized")
    }
    val currentUser: LiveData<User> get() = _currentUser


    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen){
        _currentScreen.value = screen
    }

    @SuppressLint("SuspiciousIndentation")
    fun loadCurrentUser(){
        Log.d("TAG　MainViewModel", "load Current User")
        viewModelScope.launch {
            //FireStoreからユーザー取得
        val result = userRepository.getCurrentUser()
            Log.d("TAG", result.toString())
            if (result != null) {
                _currentUser.value = result

            } else {
                // データを取得できなかった場合
                _currentUser.value = null  // 必要に応じてnullをセット
            }
        }
    }

    fun signOut(
        onSignOutComplete: () -> Unit
    ) {
        auth.signOut()
        val result = userRepository.signOutFromGoogle()
        if (result.isSuccess){
            onSignOutComplete()
        } else{
            Log.e("SignOut", "Sign Out failed")
        }
    }




}