package kimsy.rr.vental.viewModel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.HandleVentCardLikeActionUseCase
import kimsy.rr.vental.UseCase.LoadVentCardsUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VentCardsViewModel @Inject constructor(
    private val loadVentCardsUseCase: LoadVentCardsUseCase,
    private val handleVentCardLikeActionUseCase: HandleVentCardLikeActionUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    var hasFinishedLoadingAllCards by mutableStateOf(false)
        private set

    private val _ventCardItems = mutableStateListOf<VentCardItem>()
    val ventCardItems: List<VentCardItem> get() = _ventCardItems

    private val _loadCardsState = MutableStateFlow<Resource<Pair<List<VentCardItem>, DocumentSnapshot?>>>(Resource.idle())
    val loadCardsState: StateFlow<Resource<Pair<List<VentCardItem>, DocumentSnapshot?>>> get() = _loadCardsState

    private val _likeState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val likeState: StateFlow<Resource<Unit>> get() = _likeState

    private var lastVisible: DocumentSnapshot? = null

    fun loadVentCards(userId: String) {
        viewModelScope.launch {
            if (ventCardItems.isEmpty()) {
                _loadCardsState.value = Resource.loading()
            }
            _loadCardsState.value = loadVentCardsUseCase.execute(userId, lastVisible)
            when(_loadCardsState.value.status) {
                Status.SUCCESS -> {
                    _loadCardsState.value.data?. let { (ventCardItems, newLastVisible) ->
                        if (ventCardItems.isEmpty()) {
                            hasFinishedLoadingAllCards = true
                        }
                        _ventCardItems.addAll(ventCardItems)
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
            _likeState.value  = handleVentCardLikeActionUseCase.execute(userId, posterId, ventCardId)
        }
    }
}