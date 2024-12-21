package kimsy.rr.vental.ViewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.getValue
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
class SharedDebateViewModel @Inject constructor(
    private val getTimeLineItemsUseCase: GetTimeLineItemsUseCase,
    private val handleDebateLikeActionUseCase: HandleDebateLikeActionUseCase,
): ViewModel() {

    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    private val _getDebateItemsState = MutableStateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>>(
        Resource.idle())
    val getDebateItemsState: StateFlow<Resource<Pair<List<DebateItem>, DocumentSnapshot?>>> get() = _getDebateItemsState

    private val _timelineItems = MutableStateFlow<List<DebateItem>>(emptyList())
        val timelineItems: StateFlow<List<DebateItem>> get() = _timelineItems

    private val _currentDebateItem = MutableStateFlow<DebateItem?>(null)
        val currentDebateItem: StateFlow<DebateItem?> get() = _currentDebateItem

    private var lastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllItems by mutableStateOf(false)
    private set

            // likeStateをMapに変更して、各DebateItemごとに管理
            private val _likeStateMap = mutableMapOf<DebateItem, MutableStateFlow<Resource<DebateItem>>>()
    val likeStateMap: Map<DebateItem, StateFlow<Resource<DebateItem>>> get() = _likeStateMap

        suspend fun getTimeLineItems() {
            Log.d("TLVM", "getTLT called")
            viewModelScope.launch {
                if (_timelineItems.value.isEmpty()) {
                    _getDebateItemsState.value = Resource.loading()
                }
                _getDebateItemsState.value =
                    currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }!!
                when (_getDebateItemsState.value.status) {
                    Status.SUCCESS -> {
                        Log.d("TLVM", "success")
                        _getDebateItemsState.value.data?.let { (timelineItems, newLastVisible) ->
                            if (timelineItems.isEmpty()) {
                                hasFinishedLoadingAllItems = true
                            }
                            _timelineItems.value = _timelineItems.value + timelineItems
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


        //変更前
//    suspend fun getTimeLineItems () {
//        Log.d("TLVM", "getTLT called")
//        viewModelScope.launch {
//            if (timelineItems.isEmpty()) {
//                _getDebateItemsState.value = Resource.loading()
//            }
//            _getDebateItemsState.value =
//                currentUser?.let { getTimeLineItemsUseCase.execute(lastVisible, it) }!!
//            when(_getDebateItemsState.value.status) {
//                Status.SUCCESS -> {
//                    Log.d("TLVM", "success")
//                    _getDebateItemsState.value.data?.let {(timelineItems, newLasVisible)->
//                        if (timelineItems.isEmpty()) {
//                            hasFinishedLoadingAllItems = true
//                        }
//                        _timelineItems.addAll(timelineItems)
//                        lastVisible = newLasVisible
//                    }
//                }
//                Status.FAILURE -> {
//                    Log.d("TLVM", "failure")
//                }
//                else -> {}
//            }
//        }
//    }

        fun setDebateItemToModel(debateItem: DebateItem) {
            DebateItemSharedModel.setDebateItem(debateItem)
        }

        //変更
        @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
        fun handleLikeAction(
            debateItem: DebateItem,
            userType: UserType
        ) {
            viewModelScope.launch {
                initializeLikeStateForDebateItem(debateItem)
                _likeStateMap[debateItem]?.value = currentUser?.let {
                    handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, userType)
                }!!

                if (_likeStateMap[debateItem]?.value?.status == Status.SUCCESS) {
                    val index = _timelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
                    if (index != -1) {
                        // 変更: _timelineItemsのStateFlowを更新
                        _timelineItems.value = _timelineItems.value.toMutableList().apply {
                            this[index] = _likeStateMap[debateItem]?.value?.data!!
                        }
                    }
                }
            }
        }


        //変更前
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    fun handleLikeAction(
//        debateItem: DebateItem,
//        userType: UserType
//    ) {
//        viewModelScope.launch {
//            initializeLikeStateForDebateItem(debateItem)
//            _likeStateMap[debateItem]?.value = currentUser?.let {
//                handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, userType)
//            }!!
//
//            if (_likeStateMap[debateItem]?.value?.status == Status.SUCCESS) {
//                val index = _timelineItems.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
//                if (index != -1) {
//                    _timelineItems[index] = _likeStateMap[debateItem]?.value?.data!!
//                }
//            }
//        }
//    }


//
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    fun handleLikeAction(
//        debateItem: DebateItem,
//        userType: UserType
//    ) {
//        viewModelScope.launch {
//            initializeLikeStateForDebateItem(debateItem)
//            _likeStateMap[debateItem]?.value = currentUser?.let {
//                handleDebateLikeActionUseCase.execute(fromUserId = it.uid, debateItem, userType)
//            }!!
//
//            if (_likeStateMap[debateItem]?.value?.status == Status.SUCCESS) {
//                val index = _timelineItems.value.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
//                if (index != -1) {
//                    _timelineItems.value[index] = _likeStateMap[debateItem]?.value?.data!!
//                }
//            }
//        }
//    }

        // DebateItemに対応するStateFlowを初期化する
        private fun initializeLikeStateForDebateItem(debateItem: DebateItem) {
            if (_likeStateMap[debateItem] == null) {
                _likeStateMap[debateItem] = MutableStateFlow(Resource.idle()) // 初期化
            }
        }

        private fun resetLikeStateMap(debateItem: DebateItem) {
            _likeStateMap[debateItem]?.value = Resource.idle()
        }

    }