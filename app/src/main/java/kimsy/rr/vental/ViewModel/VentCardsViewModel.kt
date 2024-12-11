package kimsy.rr.vental.ViewModel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.HandleLikeActionUseCase
import kimsy.rr.vental.UseCase.LoadVentCardsUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCardWithUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentCardsViewModel @Inject constructor(
    private val loadVentCardsUseCase: LoadVentCardsUseCase,
    private val handleLikeActionUseCase: HandleLikeActionUseCase
) : ViewModel() {
    var hasFinishedLoadingAllCards by mutableStateOf(false)
        private set

    private val _ventCards = mutableStateListOf<VentCardWithUser>()
    val ventCards: List<VentCardWithUser> get() = _ventCards

    private val _loadCardsState = MutableStateFlow<Resource<Pair<List<VentCardWithUser>, DocumentSnapshot?>>>(Resource.idle())
    val loadCardsState: StateFlow<Resource<Pair<List<VentCardWithUser>, DocumentSnapshot?>>> get() = _loadCardsState

    private val _likeState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val likeState: StateFlow<Resource<Unit>> get() = _likeState

    private var lastVisible: DocumentSnapshot? = null

    fun loadVentCards(userId: String) {
        viewModelScope.launch {
            if (ventCards.isEmpty()) {
                _loadCardsState.value = Resource.loading()
            }
            _loadCardsState.value = loadVentCardsUseCase.execute(userId, lastVisible)
            when(_loadCardsState.value.status) {
                Status.SUCCESS -> {
                    _loadCardsState.value.data?. let { (cards, newLastVisible) ->
                        if (cards.isEmpty()) {
                            hasFinishedLoadingAllCards = true
                        }
                        _ventCards.addAll(cards)
                        lastVisible = newLastVisible
                    }
                }
                else -> {}
            }
        }
    }
    fun resetState() {
        _loadCardsState.value = Resource.idle()
        _likeState.value = Resource.idle()
    }

    fun handleLikeAction(userId: String, posterId: String, ventCardId: String) {
        viewModelScope.launch {
            _likeState.value  = handleLikeActionUseCase.execute(userId, posterId, ventCardId)
        }
    }
}