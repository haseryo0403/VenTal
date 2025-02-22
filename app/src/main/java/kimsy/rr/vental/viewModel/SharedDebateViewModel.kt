package kimsy.rr.vental.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.UseCase.GenerateDebateItemByDebateIdUseCase
import kimsy.rr.vental.UseCase.HandleDebateLikeActionUseCase
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedDebateViewModel @Inject constructor(
    private val handleDebateLikeActionUseCase: HandleDebateLikeActionUseCase,
    private val generateDebateItemByDebateIdUseCase: GenerateDebateItemByDebateIdUseCase

): ViewModel() {
    private val _currentUser = MutableStateFlow(User.CurrentUserShareModel.getCurrentUserFromModel()?: User())
    val currentUser: StateFlow<User> get() = _currentUser

    private val _currentDebateItem = MutableStateFlow<DebateItem?>(null)
        val currentDebateItem: StateFlow<DebateItem?> get() = _currentDebateItem

    private val _likeState = MutableStateFlow<Map<DebateItem, Resource<DebateItem>>>(emptyMap())
    val likeState: StateFlow<Map<DebateItem, Resource<DebateItem>>> get() = _likeState

    private val _generateDebateItemState = MutableStateFlow<Resource<DebateItem>>(
        Resource.idle())
    val generateDebateItemState: StateFlow<Resource<DebateItem>> get() = _generateDebateItemState

    fun setCurrentDebateItem(debateItem: DebateItem) {
        _currentDebateItem.value = debateItem
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun handleLikeAction(
        debateItem: DebateItem,
        userType: UserType
    ) {
        viewModelScope.launch {

            val result = handleDebateLikeActionUseCase.execute(fromUserId = _currentUser.value.uid, debateItem, userType)

            _likeState.update { currentState ->
                currentState.toMutableMap().apply {
                    this[debateItem] = result
                }
            }


            if (_likeState.value[debateItem]?.status == Status.SUCCESS) {
                if (debateItem.debate.debateId == _currentDebateItem.value?.debate?.debateId) {
                    setCurrentDebateItem(_likeState.value[debateItem]?.data!!)
                }
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    fun generateAndSetDebateItemByDebateId(
        debateId: String,
    ) {
        viewModelScope.launch {
            _generateDebateItemState.value = Resource.loading()
            _generateDebateItemState.value = generateDebateItemByDebateIdUseCase.execute(debateId, _currentUser.value.uid)
                when (_generateDebateItemState.value.status) {
                    Status.SUCCESS -> {
                        _generateDebateItemState.value.data?.let { setCurrentDebateItem(it) }
                    }
                    else -> {}
                }
        }
    }

    fun showLikeFailedToast(context: Context) {
        Toast.makeText(context, R.string.like_fail, Toast.LENGTH_SHORT).show()
    }

    fun resetLikeState(debateItem: DebateItem) {
        _likeState.value = _likeState.value.toMutableMap().apply {
            this[debateItem] = Resource.idle()
        }
    }

    fun resetGenerateDebateItemState() {
        _generateDebateItemState.value = Resource.idle()
    }

}