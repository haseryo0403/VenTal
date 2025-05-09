package kimsy.rr.vental.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.SaveImageUseCase
import kimsy.rr.vental.UseCase.UpdateUserUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val updateUserUseCase: UpdateUserUseCase,
    private val saveImageUseCase: SaveImageUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _updateUserState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val updateUserState: StateFlow<Resource<Unit>> get() = _updateUserState

    fun updateUser(name: String, imageUri: Uri?, selfIntroduction: String?, context: Context){
        viewModelScope.launch {
            _updateUserState.value = Resource.loading()
            val imageUrl = imageUri?.let { uri ->
                val imageURLState = saveImageUseCase.execute(imageUri, context)
                if (imageURLState.status == Status.SUCCESS) imageURLState.data else null
            }
            val user = currentUser.value.copy(
                name = name,
                photoURL = imageUrl?: currentUser.value.photoURL,
                selfIntroduction = selfIntroduction
            )
            _updateUserState.value = updateUserUseCase.execute(user)
            if (_updateUserState.value.status == Status.SUCCESS) {
                User.CurrentUserShareModel.setCurrentUserToModel(user)
            }
        }
    }

    fun resetState() {
        _updateUserState.value = Resource.idle()
    }
}