package kimsy.rr.vental.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.GetSwipeCardUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateItemSharedModel
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getMessageUseCase: GetMessageUseCase
): ViewModel() {

private val _getDebateItemState = MutableStateFlow<Resource<DebateItem>>(Resource.idle())
    val getDebateItemState: StateFlow<Resource<DebateItem>> get() = _getDebateItemState

private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState


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