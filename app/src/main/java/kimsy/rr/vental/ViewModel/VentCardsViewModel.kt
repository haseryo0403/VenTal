package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardRepository
import kimsy.rr.vental.data.VentCardWithUser
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class VentCardsViewModel @Inject constructor(
    private val authViewModel: AuthViewModel,
    private val ventCardRepository: VentCardRepository
):ViewModel() {

    init {
        Log.e("VM initialization", "VCVM initialized")
    }

    val currentUser: LiveData<User> = authViewModel.currentUser
    private val _ventCards = MutableLiveData<List<VentCardWithUser>>()
    val ventCards: LiveData<List<VentCardWithUser>> get() = _ventCards

    fun loadVentCards(){
        viewModelScope.launch{
            val result = ventCardRepository.getVentCardsWithUser()
            result
                .onSuccess {data ->
                    Log.d("loadVentCards", "Vent Cards: $data")  // dataの中身をログに出力
                    //TODO　スワイプカードのIDが必要ならVentCardにIDを足す、作成関数も変更。
                    _ventCards.value = data
                }
                .onFailure {
                    Log.e("loadVC fail", "error:$it")
                }
        }
    }





}