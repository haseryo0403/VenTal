package kimsy.rr.vental.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GenerateDebateItemByDebateIdUseCase
import kimsy.rr.vental.UseCase.GetDebatesRelatedUserUseCase
import kimsy.rr.vental.UseCase.GetTimeLineItemsUseCase
import kimsy.rr.vental.UseCase.HandleDebateLikeActionUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedDebateViewModel @Inject constructor(
    private val getTimeLineItemsUseCase: GetTimeLineItemsUseCase,
    private val handleDebateLikeActionUseCase: HandleDebateLikeActionUseCase,
    private val hetDebatesRelatedUserUseCase: GetDebatesRelatedUserUseCase,
    private val generateDebateItemByDebateIdUseCase: GenerateDebateItemByDebateIdUseCase

): ViewModel() {
    //TODO
    //Viewのlaunchedeffectで画面を指定例：currentView =
    // lastvisibleやdebateitemやhasfinishなどをmapofにする
    //もしくは

    //TODO itemsは各画面のVMで保持する。
    //そして、likeStateをSVMで保持し、全てのVMで監視して、SUCCESSなら
    //val updatedDebate: DebateItem がヌルでなければそれのidと各VMで保持しているitemsと照合して、
    // それのitemをアップデートする
    //たぶんlikeStateを監視するためにはviewModelが引数にsvmをもつ必要があると思う
    //TODO これだ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！

    //各VM
    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    //TimelineView関連
    //各VM
    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    //各VM
    private val _timelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
        val timelineItems: StateFlow<List<DebateItem>> get() = _timelineItems

    //keep
    private val _currentDebateItem = MutableStateFlow<DebateItem?>(null)
        val currentDebateItem: StateFlow<DebateItem?> get() = _currentDebateItem

    //各VM
    private var lastVisible: DocumentSnapshot? = null

    //各VM
    var hasFinishedLoadingAllItems by mutableStateOf(false)
    private set

    //keep
    private val _likeState = MutableStateFlow<Map<DebateItem, Resource<DebateItem>>>(emptyMap())
    val likeState: StateFlow<Map<DebateItem, Resource<DebateItem>>> get() = _likeState

    //各VM
    //各画面固有にするか、共通にするか
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    //mypageView関連

    //各VM
    private var lastVisibleForMyPage: DocumentSnapshot? = null

    //各VM
    private val _myPageItems = MutableStateFlow<List<DebateItem>>(emptyList())
    val myPageItems: StateFlow<List<DebateItem>> get() = _myPageItems

    //各VM
    var hasFinishedLoadingAllMyPageItems by mutableStateOf(false)
        private set

    //NotificationView関連

    //各VM
    private val _generateDebateItemState = MutableStateFlow<Resource<DebateItem>>(
        Resource.idle())
    val generateDebateItemState: StateFlow<Resource<DebateItem>> get() = _generateDebateItemState

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            lastVisible = null
            getTimeLineItems()
        }
    }



    suspend fun getTimeLineItems() {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
//            if (_timelineItems.value.isEmpty()) {
//            }
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }?: Resource.failure(R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        if (_isRefreshing.value) {
                            _timelineItems.value = timelineItems
                            _isRefreshing.value = false
                        } else {
                            _timelineItems.value = _timelineItems.value + timelineItems
                        }
//                        _timelineItems.value = _timelineItems.value + timelineItems

                        lastVisible = newLastVisible
                    }
                }
                Status.FAILURE -> {
                    Log.d("TLVM", "failure")
                }
                else -> {}
            }
        }
    }

    suspend fun loadTimeLineItems() {
        Log.d("TLVM", "getTLT called")
        viewModelScope.launch {
            //keep
            if (_timelineItems.value.isEmpty()) {
                _getDebateItemsState.value = Resource.loading()
            }
            //keep or use new state
           _getDebateItemsState.value =
                currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }?: Resource.failure(R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    //keep
                    _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                        if (timelineItems.isEmpty()) {
                            hasFinishedLoadingAllItems = true
                        }
                        _timelineItems.value = timelineItems
                        lastVisible = newLastVisible
                    }
                }
                else -> {}
            }
        }
    }

    suspend fun getMyPageDebateItems() {
        viewModelScope.launch {
            _getDebateItemsState.value = Resource.loading()
            _getDebateItemsState.value =
                currentUser?.let { hetDebatesRelatedUserUseCase.execute(lastVisibleForMyPage, it.uid) }?: Resource.failure(R.string.no_user_found.toString())
            when (_getDebateItemsState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _getDebateItemsState.value.data?.let { (myPageItems, newLastVisible) ->
                        if(myPageItems.isEmpty()) {
                            hasFinishedLoadingAllMyPageItems = true
                        }
                        _myPageItems.value = _myPageItems.value +myPageItems
                        lastVisibleForMyPage = newLastVisible
                    }
                }
                Status.FAILURE -> {
                    Log.d("TLVM", "failure")
                }
                else -> {}
            }
        }
    }

    fun setCurrentDebateItem(debateItem: DebateItem) {
        _currentDebateItem.value = debateItem
    }

    //変更
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun handleLikeAction(
        debateItem: DebateItem,
        userType: UserType
    ) {
        viewModelScope.launch {

            val result = currentUser?.let {
                handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, userType)
            } ?: Resource.failure("User not logged in")

            _likeState.update { currentState ->
                currentState.toMutableMap().apply {
                    this[debateItem] = result
                }
            }


            if (_likeState.value[debateItem]?.status == Status.SUCCESS) {
                val index = _timelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
                if (index != -1) {
                    // 変更: _timelineItemsのStateFlowを更新
                    _timelineItems.value = _timelineItems.value.toMutableList().apply {
                        this[index] = _likeState.value[debateItem]?.data!!
                    }
                }
                if (debateItem.debate.debateId == _currentDebateItem.value?.debate?.debateId) {
                    setCurrentDebateItem(_likeState.value[debateItem]?.data!!)
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun generateAndSetDebateItemByDebateId(
        debateId: String,
//        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _generateDebateItemState.value = Resource.loading()
            _generateDebateItemState.value =
                currentUser?.let { generateDebateItemByDebateIdUseCase.execute(debateId, it.uid) }!!
                when (_generateDebateItemState.value.status) {
                    Status.SUCCESS -> {
                        _generateDebateItemState.value.data?.let { setCurrentDebateItem(it) }
//                        onSuccess()
                    }
                    else -> {}
                }
        }
    }

    fun showLikeFailedToast(context: Context) {
        Toast.makeText(context, R.string.like_fail, Toast.LENGTH_SHORT).show()
    }

    fun resetLikeState(debateItem: DebateItem) {
        _likeState.value = _likeState.value.toMutableMap().apply {
            this[debateItem] = Resource.idle()
        }
    }

    fun resetGetDebateItemState() {
        _getDebateItemsState.value = Resource.idle()
    }

    fun resetGenerateDebateItemState() {
        _generateDebateItemState.value = Resource.idle()
    }

}