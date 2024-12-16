package kimsy.rr.vental.ViewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetTimeLineItemsUseCase
import kimsy.rr.vental.UseCase.HandleDebateLikeActionUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateItemSharedModel
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val getTimeLineItemsUseCase: GetTimeLineItemsUseCase,
    private val handleDebateLikeActionUseCase: HandleDebateLikeActionUseCase,
): ViewModel() {

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private val _timelineItems = mutableStateListOf<DebateItem>()
    val timelineItems: List<DebateItem> get() = _timelineItems

    private var lastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllItems by mutableStateOf(false)
        private set

    private val _likeState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val likeState: StateFlow<Resource<Unit>> get() = _likeState

    suspend fun getTimeLineItems () {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
            if (timelineItems.isEmpty()) {
                _getDebateItemsState.value = Resource.loading()
            }
            _getDebateItemsState.value =
                currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }!!
            when(_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let {(timelineItems, newLasVisible)->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        _timelineItems.addAll(timelineItems)
                        lastVisible = newLasVisible
                    }
                }
                Status.FAILURE -> {
                    Log.d("TLVM", "failure")
                }
                else -> {}
            }
        }
    }

    fun setDebateItemToModel(debateItem: DebateItem) {
        DebateItemSharedModel.setDebateItem(debateItem)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun handleLikePosterAction(
        debateItem: DebateItem
    ) {
        viewModelScope.launch {
            _likeState.value = currentUser?.let { handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, UserType.POSTER ) }!!
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun handleLikeDebaterAction(
        debateItem: DebateItem
    ) {
        viewModelScope.launch {
            _likeState.value = currentUser?.let { handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, UserType.DEBATER ) }!!
        }
    }

}