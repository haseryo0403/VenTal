package kimsy.rr.vental.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.SaveVentCardUseCase
import kimsy.rr.vental.data.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentCardCreationViewModel @Inject constructor(
    private val authViewModel: AuthViewModel,
    private val saveVentCardUseCase: SaveVentCardUseCase
): ViewModel(){
    init {
        Log.d("VentCardCreationViewModel", "ViewModel instance created: $this")
    }

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var content by mutableStateOf("")
    var tags =  mutableStateListOf<String>()
    private val _saveState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val saveState: StateFlow<Resource<Unit>> get() = _saveState

    fun startSavingVentCard(
        context: Context
    ){
        viewModelScope.launch(Dispatchers.IO) {
            _saveState.value = Resource.loading()
            val result = saveVentCardUseCase.execute(
                posterId = authViewModel.currentUser.value?.uid.toString(),
                content = content,
                selectedImageUri = selectedImageUri,
                tags = tags.toList(),
                context = context
            )
            _saveState.value = result
        }
    }

    fun resetStatus() {
        _saveState.value = Resource.idle()
    }

    fun resetValues() {
        content = ""
        selectedImageUri = null
        tags.clear()
    }
}
