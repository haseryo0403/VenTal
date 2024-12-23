package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
): ViewModel() {

    init {
        Log.d("TLVM" , "initialized")
    }

    var savedScrollIndex by mutableStateOf(0)
    var savedScrollOffset by mutableStateOf(0)

    fun setScrollState(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

}