package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kimsy.rr.vental.Screen

class MainViewModel:ViewModel() {
    private val _currentScreen:MutableState<Screen> = mutableStateOf(Screen.BottomScreen.VentCards)

    val currentScreen: MutableState<Screen>
        get() = _currentScreen

    fun setCurrentScreen(screen: Screen){
        _currentScreen.value = screen
    }
}