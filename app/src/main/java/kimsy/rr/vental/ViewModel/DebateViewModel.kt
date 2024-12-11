package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.GetSwipeCardUseCase
import kimsy.rr.vental.data.DebateSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.VentCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getMessageUseCase: GetMessageUseCase
): ViewModel() {
// 共有モデルから討論データを取得
var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

private val _fetchVentCardState = MutableStateFlow<Resource<VentCard>>(Resource.idle())
    val fetchVentCardState: StateFlow<Resource<VentCard>> get() = _fetchVentCardState

private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState

    // 討論データをロードするメソッド（例: API呼び出しやデータ更新）
    fun loadDebate() {
        val debate = DebateSharedModel.getDebate()

        // 取得したデータがあれば状態を更新
        if (debate != null) {
            debateWithUsers.value = debate
            getVentCard(debate.posterId, debate.swipeCardId)
            getMessages(debate)
        } else {
            // データがない場合の処理（エラーハンドリング等）
            debateWithUsers.value = null
        }
    }
//TODO どっちがいいかな？
    //データ自体を渡してそのまま受け渡すバージョン
    private fun getVentCard(posterId: String, ventCardId: String) {
        viewModelScope.launch {
            _fetchVentCardState.value = getSwipeCardUseCase.execute(posterId, ventCardId)
        }
    }

    //データクラスを渡してそれを分解してUseCaseに受け渡すバージョン
    private fun getMessages(debateWithUsers: DebateWithUsers) {
        viewModelScope.launch {
            _fetchMessageState.value = getMessageUseCase.execute(debateWithUsers)
        }
    }

    fun resetState() {
        _fetchVentCardState.value = Resource.idle()
        _fetchMessageState.value = Resource.idle()
    }
}