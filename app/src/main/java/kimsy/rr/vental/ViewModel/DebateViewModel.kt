package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.UseCase.GetSwipeCardUseCase
import kimsy.rr.vental.data.DebateSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getSwipeCardUseCase: GetSwipeCardUseCase,
    private val getMessageUseCase: GetMessageUseCase
): ViewModel() {
//    var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

// 共有モデルから討論データを取得
var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

var ventCard = mutableStateOf<VentCard?>(null)

var messages = mutableStateListOf<Message>()
    private set

var isLoading = mutableStateOf(false)
    private set

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> get() = _errorState

    // 討論データをロードするメソッド（例: API呼び出しやデータ更新）
    fun loadDebate() {
        isLoading.value = true
        val debate = DebateSharedModel.getDebate()

        // 取得したデータがあれば状態を更新
        if (debate != null) {
            debateWithUsers.value = debate
            getVentCard(debate.posterId, debate.swipeCardId)
            getMessages(debate)
        } else {
            // データがない場合の処理（エラーハンドリング等）
            debateWithUsers.value = null
            isLoading.value = false
        }
    }
//TODO どっちがいいかな？
    //データ自体を渡してそのまま受け渡すバージョン
    private fun getVentCard(posterId: String, ventCardId: String) {
        viewModelScope.launch {
            try {
                ventCard.value = getSwipeCardUseCase.execute(posterId, ventCardId).getOrThrow()
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            }
        }
    }

    //データクラスを渡してそれを分解してUseCaseに受け渡すバージョン
    private fun getMessages(debateWithUsers: DebateWithUsers) {
        viewModelScope.launch {
            try {
                getMessageUseCase(debateWithUsers)
                    .onSuccess {gotMessages->
                        messages.addAll(gotMessages)
                    }
                    .onFailure {exception->
                        _errorState.value = exception.message?: "不明なエラー"
                    }
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleUnexpectedError(e)
            } finally {
                isLoading.value = false
            }
        }
    }

    // エラーメッセージをクリアするメソッド
    fun clearErrorState() {
        _errorState.value = null
    }

    private fun handleNetworkError(error: IOException) {
        _errorState.value = "ネットワーク接続に問題があります。" // UI向けメッセージ
        Log.e("DCVM", "Network error occurred", error)
    }

    private fun handleUnexpectedError(error: Exception) {
        _errorState.value = "予期しないエラーが発生しました。" // UI向けメッセージ
        Log.e("DCVM", "Unexpected error occurred", error)
    }


}