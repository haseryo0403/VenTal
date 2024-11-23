package kimsy.rr.vental.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardRepository
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class VentCardsViewModel @Inject constructor(
    private val authViewModel: AuthViewModel,
    private val ventCardRepository: VentCardRepository
):ViewModel() {

    var isLoading = mutableStateOf(true)
        private set

    init {
        Log.e("VM initialization", "VCVM initialized")
    }
    private val _ventCards = mutableStateListOf<VentCardWithUser>()
    val ventCards: List<VentCardWithUser> get() = _ventCards

    private var lastVisible: DocumentSnapshot? = null

    fun loadVentCards(userId: String){
        viewModelScope.launch{
            val likedVentCard = ventCardRepository.fetchLikedVentCardIds(userId)
            val debatingVentCard = ventCardRepository.fetchDebatingVentCardIds(userId)
            val result = ventCardRepository.getVentCardsWithUser(userId, likedVentCard, debatingVentCard,lastVisible)
            Log.d("VCVM", "lastVisible: $lastVisible")
            result
                .onSuccess {data ->
                    Log.d("loadVentCards", "Vent Cards: $data")  // dataの中身をログに出力
                    _ventCards.addAll(data.first)
                    lastVisible = data.second
                    isLoading.value = false

                }
                .onFailure {
                    Log.e("loadVC fail", "error:$it")
                    isLoading.value = false
                }
        }
    }

    fun handleLikeAction(userId: String,posterId: String, ventCardId: String) {
        viewModelScope.launch {
            val isLiked = ventCardRepository.checkIfLiked(userId, ventCardId)
            try {
                if (!isLiked) {
                    ventCardRepository.likeVentCard(userId, posterId, ventCardId)
//TODO　使うとサイコンポーズされてしまう                    _ventCards.removeIf{it.swipeCardId == ventCardId}
                } else {
                    //TODO dislike
                    ventCardRepository.disLikeVentCard(userId,ventCardId)
//TODO　使うとサイコンポーズされてしまう                        _ventCards.removeIf{it.swipeCardId == ventCardId}

                    Log.e("VCVM", "like exists")
                }
            } catch (e: Exception) {
                Log.e("VCVM", "error : $e")
            }
        }
    }





}