package kimsy.rr.vental.ViewModel

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.GetSwipeCardUseCase
import kimsy.rr.vental.UseCase.HandleDebateLikeActionUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateItemSharedModel
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getMessageUseCase: GetMessageUseCase,
    private val handleDebateLikeActionUseCase: HandleDebateLikeActionUseCase,
    ): ViewModel() {

val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

private val _getDebateItemState = MutableStateFlow<Resource<DebateItem>>(Resource.idle())
    val getDebateItemState: StateFlow<Resource<DebateItem>> get() = _getDebateItemState

private val _currentDebateItem = MutableStateFlow<DebateItem?>(null)
    val currentDebateItem: StateFlow<DebateItem?> get() = _currentDebateItem

private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState

// likeStateをMapに変更して、各DebateItemごとに管理
private val _likeStateMap = mutableMapOf<DebateItem, MutableStateFlow<Resource<DebateItem>>>()
    val likeStateMap: Map<DebateItem, StateFlow<Resource<DebateItem>>> get() = _likeStateMap

//    val currentDebateItem: StateFlow<DebateItem?> = DebateItemSharedModel.currentDebateItem

//    fun loadDebateItem() {
//        currentDebateItem.value?.let { getMessages(it.debate) }
//    }
    // 討論データをロードするメソッド（例: API呼び出しやデータ更新）
    fun loadDebateItem() {
        _getDebateItemState.value = Resource.loading()
        val debateItemFromModel = DebateItemSharedModel.getDebateItem()

        if (debateItemFromModel != null) {
            _getDebateItemState.value = Resource.success(debateItemFromModel)
            getMessages(debateItemFromModel.debate)
        } else {
            // データがない場合の処理（エラーハンドリング等）
            _getDebateItemState.value = Resource.failure("表示する討論が見つかりません。")
        }
    }


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

            DebateItemSharedModel.setDebateItem(_likeStateMap[debateItem]?.value?.data!!)

            loadDebateItem()

//            if (_likeStateMap[debateItem]?.value?.status == Status.SUCCESS) {
//                val index = _timelineItems.indexOfFirst { it.debate.debateId == debateItem.debate.debateId }
//                if (index != -1) {
//                    _timelineItems[index] = _likeStateMap[debateItem]?.value?.data!!
//                }
//            }
        }
    }

    // DebateItemに対応するStateFlowを初期化する
    private fun initializeLikeStateForDebateItem(debateItem: DebateItem) {
        if (_likeStateMap[debateItem] == null) {
            _likeStateMap[debateItem] = MutableStateFlow(Resource.idle()) // 初期化
        }
    }

    private fun getMessages(debate: Debate) {
        viewModelScope.launch {
            _fetchMessageState.value = getMessageUseCase.execute(
                debate.posterId,
                debate.swipeCardId,
                debate.debateId
            )
        }
    }

    fun resetState() {
        _fetchMessageState.value = Resource.idle()
        _getDebateItemState.value = Resource.idle()
    }
}