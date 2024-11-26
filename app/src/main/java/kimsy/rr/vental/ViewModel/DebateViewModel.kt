package kimsy.rr.vental.ViewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.data.DebateSharedModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(): ViewModel() {
//    var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

// 共有モデルから討論データを取得
var debateWithUsers = mutableStateOf<DebateWithUsers?>(null)

var isLoading = mutableStateOf(false)
    private set

    // 討論データをロードするメソッド（例: API呼び出しやデータ更新）
    fun loadDebate() {
        isLoading.value = true
        val debate = DebateSharedModel.getDebate()

        // 取得したデータがあれば状態を更新
        if (debate != null) {
            debateWithUsers.value = debate
            isLoading.value = false
        } else {
            // データがない場合の処理（エラーハンドリング等）
            debateWithUsers.value = null
            isLoading.value = false
        }
    }

}