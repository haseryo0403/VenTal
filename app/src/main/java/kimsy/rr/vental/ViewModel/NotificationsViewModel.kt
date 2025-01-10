package kimsy.rr.vental.ViewModel

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
    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

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
            _loadNotificationDataState.value =
                currentUser?.let {
                    loadNotificationUseCase.execute(currentUser.uid, lastVisible)
                }?: Resource.failure("no user found")

            when (_loadNotificationDataState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
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
            currentUser?.let {
                observeNotificationCountUseCase.execute(it.uid)
                    .collect{ resource ->
                        _notificationCountState.value = resource
                    }
            }
        }
    }

    fun markNotificationAsRead(notificationItem: NotificationItem) {
        viewModelScope.launch {
            currentUser?.let {
                Log.d("NVM", "mNAR called")
                markNotificationAsReadUseCase.execute(it.uid, notificationItem.notification.notificationId)
            }
        }
    }

    fun resetState() {
        _loadNotificationDataState.value = Resource.idle()
    }

}