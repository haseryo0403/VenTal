package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.data.DebateWithUsers
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(): ViewModel() {
    var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

    suspend fun loadDebate(){

    }

}