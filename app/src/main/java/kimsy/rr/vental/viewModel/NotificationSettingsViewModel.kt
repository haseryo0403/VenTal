package kimsy.rr.vental.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.LoadNotificationSettingsUseCase
import kimsy.rr.vental.UseCase.SetNotificationSettingsUseCase
import kimsy.rr.vental.data.NotificationSettings
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val loadNotificationSettingsUseCase: LoadNotificationSettingsUseCase,
    private val setNotificationSettingsUseCase: SetNotificationSettingsUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _notificationSettingsState = MutableStateFlow<Resource<NotificationSettings>>(
        Resource.idle())
    val notificationSettingsState: StateFlow<Resource<NotificationSettings>> get() = _notificationSettingsState

    suspend fun loadNotificationSettings() {
        viewModelScope.launch {
            _notificationSettingsState.value = Resource.loading()
            _notificationSettingsState.value = loadNotificationSettingsUseCase.execute(currentUser.value.uid)
//            if (currentUser == null) {
//                _notificationSettingsState.value = Resource.failure("user not found")
//            } else {
//                _notificationSettingsState.value = loadNotificationSettingsUseCase.execute(currentUser.uid)
//            }
        }
    }

    fun updateNotificationSettings(notificationSettings: NotificationSettings) {
        viewModelScope.launch {
            val result = setNotificationSettingsUseCase.execute(currentUser.value.uid, notificationSettings)
            when(result.status) {
                Status.SUCCESS -> {
                    _notificationSettingsState.value = Resource.success(notificationSettings)
                }
                Status.FAILURE -> {
                    _notificationSettingsState.value = Resource.failure("update fail")
                }
                else -> {}
            }
//            if (currentUser == null) {
//                _notificationSettingsState.value = Resource.failure("user not found")
//            } else {
//                val result = setNotificationSettingsUseCase.execute(currentUser.uid, notificationSettings)
//                when(result.status) {
//                    Status.SUCCESS -> {
//                        _notificationSettingsState.value = Resource.success(notificationSettings)
//                    }
//                    Status.FAILURE -> {
//                        _notificationSettingsState.value = Resource.failure("update fail")
//                    }
//                    else -> {}
//                }
//            }
        }
    }



}