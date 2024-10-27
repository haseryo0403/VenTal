package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kimsy.rr.vental.Screen
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel():ViewModel() {

    private val _currentScreen:MutableState<Screen> = mutableStateOf(Screen.BottomScreen.VentCards)
//    private val _currentUser = MutableLiveData<User>()
//    val currentUser: LiveData<User> get() = _currentUser


    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen){
        _currentScreen.value = screen
    }

//    private fun loadCurentUser(){
//        viewModelScope.launch {
//            //FireStoreからユーザー取得
//            val result = userRepository.getUser()
//            Log.d("TAG", result.toString())
//            if (!result.isEmpty()) {
//                Log.d("TAG", "New User")
//                _currentUser.value = result.data
//
//            } else {
//                // データを取得できなかった場合
//            }
//        }
//    }


}