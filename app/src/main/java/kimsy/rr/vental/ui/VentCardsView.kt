package kimsy.rr.vental.ui


import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.DebateCreationViewModel
import kimsy.rr.vental.ViewModel.VentCardsViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CardStack
import kimsy.rr.vental.ui.commonUi.ErrorView


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeCardsView(
    ventCardsViewModel: VentCardsViewModel = hiltViewModel(),
    debateCreationViewModel: DebateCreationViewModel,
    context: Context,
    toDebateCreationView: () -> Unit,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit,
    authViewModel: AuthViewModel
){
    var noCardsLeft by remember { mutableStateOf(false) }
    val user by authViewModel.currentUser.observeAsState(User())

    // LaunchedEffectを使用して画面遷移時にのみloadVentCardsを呼び出す
    LaunchedEffect(user.uid) {
        ventCardsViewModel.loadVentCards(user.uid)
    }
    val ventCards by remember { derivedStateOf { ventCardsViewModel.ventCards } }

    val loadCardsState = ventCardsViewModel.loadCardsState.collectAsState()
    val likeCardState = ventCardsViewModel.likeState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {
            loadCardsState.value.status == Status.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            loadCardsState.value.status == Status.FAILURE && (noCardsLeft || ventCards.isEmpty()) -> {
//                Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
//                ventCardsViewModel.resetState()
                ErrorView(
                    retry = {
                        ventCardsViewModel.loadVentCards(user.uid)
                    }
                )
            }

            loadCardsState.value.status == Status.SUCCESS && (noCardsLeft || ventCards.isEmpty()) -> {
                Text(text = "No ventCards available", modifier = Modifier.align(Alignment.Center)) // TODO: デザインを追加
            }

            loadCardsState.value.status == Status.IDLE -> {
                Text(text = "EROORRRRRRRRRRR")
            }

            else -> {
                // ここに共通のCardStackを配置
                if (ventCards.isNotEmpty()) {
                    Log.d("ventcardview", "ventCard is not empty")
                    Log.d("ventcardview", "${ventCards}")


                    CardStack(
                        modifier = Modifier,
                        enableButtons = true,
                        items = ventCards,
                        onSwipeRight = { ventCard ->
                            ventCardsViewModel.handleLikeAction(
                                userId = user.uid,
                                posterId = ventCard.posterId,
                                ventCardId = ventCard.swipeCardId
                            )
                        },
                        onSwipeLeft = { ventCard ->
                            debateCreationViewModel.ventCardWithUser = ventCard
                            Log.e("VCV", "ventCardId: ${ventCard.swipeCardId}")
                            debateCreationViewModel.getRelatedDebates(ventCard)
                            toDebateCreationView()
                        },
                        onEmptyStack = {
                            noCardsLeft = true
                        },
                        onLessStack = {
                            ventCardsViewModel.loadVentCards(user.uid)
                        },
                        toReportVentCardView = toReportVentCardView,
                        toRequestVentCardDeletionView = toRequestVentCardDeletionView
                    )
                } else {
                    Log.d("ventcardview", "ventCard is empty")
                }
            }
        }

        if (likeCardState.value.status == Status.FAILURE) {
            Toast.makeText(context, "いいねに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
            ventCardsViewModel.resetState()
        }

    }
}
