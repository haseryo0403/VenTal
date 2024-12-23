package kimsy.rr.vental.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.UseCase.GetMessageUseCase
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val getMessageUseCase: GetMessageUseCase,
    ): ViewModel() {

private val _fetchMessageState = MutableStateFlow<Resource<List<Message>>>(Resource.idle())
    val fetchMessageState: StateFlow<Resource<List<Message>>> get() = _fetchMessageState

    fun getMessages(debate: Debate) {
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
    }
}