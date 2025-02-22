package kimsy.rr.vental.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.LoadNotificationUseCase
import kimsy.rr.vental.UseCase.MarkNotificationAsReadUseCase
import kimsy.rr.vental.UseCase.ObserveNotificationCountUseCase
import kimsy.rr.vental.data.NotificationItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val loadNotificationUseCase: LoadNotificationUseCase,
    private val observeNotificationCountUseCase: ObserveNotificationCountUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _loadNotificationDataState = MutableStateFlow<Resource<Pair<List<NotificationItem>, DocumentSnapshot?>>>(Resource.idle())
    val loadNotificationDataState: StateFlow<Resource<Pair<List<NotificationItem>, DocumentSnapshot?>>> get() = _loadNotificationDataState

    private var lastVisible: DocumentSnapshot? = null

    private val _notificationItems = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationItems: StateFlow<List<NotificationItem>> get() = _notificationItems

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    private val _notificationCountState = MutableStateFlow<Resource<Int>>(Resource.idle())
    val notificationCountState: StateFlow<Resource<Int>> get() = _notificationCountState

    suspend fun loadNotificationItems() {
        viewModelScope.launch {
            _loadNotificationDataState.value = Resource.loading()
            _loadNotificationDataState.value = loadNotificationUseCase.execute(currentUser.value.uid, lastVisible)

            when (_loadNotificationDataState.value.status) {
                Status.SUCCESS -> {
                    _loadNotificationDataState.value.data?.let { (notificationItems, newLastVisible) ->
                        if (notificationItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        _notificationItems.value = _notificationItems.value + notificationItems
                        lastVisible = newLastVisible
                    }
                }
                else -> {
                }
            }
        }
    }

    fun observeNotificationCount() {
        viewModelScope.launch {
            observeNotificationCountUseCase.execute(_currentUser.value.uid)
                .collect{ resource ->
                    _notificationCountState.value = resource
                    Log.d("NVM", resource.data.toString())
                }
        }
    }

    fun markNotificationAsRead(notificationItem: NotificationItem) {
        viewModelScope.launch {
            markNotificationAsReadUseCase.execute(_currentUser.value.uid, notificationItem.notification.notificationId)
        }
    }

    fun resetState() {
        _loadNotificationDataState.value = Resource.idle()
    }

}