package kimsy.rr.vental.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.LoadVentCardsRelatedUserUseCase
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyVentCardViewModel @Inject constructor(
    private val loadVentCardsRelatedUserUseCase: LoadVentCardsRelatedUserUseCase
): ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(User.CurrentUserShareModel.getCurrentUserFromModel())
    val currentUser: StateFlow<User?> get() = _currentUser

    //index 1のventCards用
    var ventCardSavedScrollIndex by mutableStateOf(0)
    var ventCardSavedScrollOffset by mutableStateOf(0)

    private val _loadVentCardState = MutableStateFlow<Resource<Pair<List<VentCard>, DocumentSnapshot?>>>(
        Resource.idle())
    val loadVentCardState: StateFlow<Resource<Pair<List<VentCard>, DocumentSnapshot?>>> get() = _loadVentCardState

    private val _ventCards = MutableStateFlow<List<VentCard>>(emptyList())
    val ventCards: StateFlow<List<VentCard>> get() = _ventCards

    private var ventCardLastVisible: DocumentSnapshot? = null

    var hasFinishedLoadingAllVentCards by mutableStateOf(false)
        private set

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    fun setVentCardScrollState(index: Int, offset: Int) {
        ventCardSavedScrollIndex = index
        ventCardSavedScrollOffset = offset
    }

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadMyVentCards()
        }
    }

    suspend fun loadMyVentCards() {
        viewModelScope.launch {
            _loadVentCardState.value = Resource.loading()
            _loadVentCardState.value =
                _currentUser.value?.let {
                    loadVentCardsRelatedUserUseCase.execute(it.uid, ventCardLastVisible)
                }?: Resource.failure(R.string.no_user_found.toString())

            when (_loadVentCardState.value.status) {
                Status.SUCCESS -> {
                    Log.d("TLVM", "success")
                    _loadVentCardState.value.data?.let { (ventCards, newLastVisible) ->
                        if(ventCards.isEmpty()) {
                            hasFinishedLoadingAllVentCards = true
                        }
                        if (_isRefreshing.value) {
                            _ventCards.value = ventCards
                            _isRefreshing.value = false
                        } else {
                            _ventCards.value = _ventCards.value + ventCards
                        }
                        ventCardLastVisible = newLastVisible
                    }
                }
                else -> {}
            }
        }
    }

    fun updateCurrentUser() {
        _currentUser.value = User.CurrentUserShareModel.getCurrentUserFromModel()
    }

    fun  resetLoadVentCardState() {
        _loadVentCardState.value = Resource.loading()
    }

}