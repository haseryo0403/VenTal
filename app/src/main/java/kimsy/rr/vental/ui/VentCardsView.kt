package kimsy.rr.vental.ui


import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CardStack
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.DebateCreationViewModel
import kimsy.rr.vental.viewModel.VentCardsViewModel


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun SwipeCardsView(
    ventCardsViewModel: VentCardsViewModel = hiltViewModel(),
    debateCreationViewModel: DebateCreationViewModel,
    context: Context,
    toDebateCreationView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit,
){
    var noCardsLeft by remember { mutableStateOf(false) }
    val currentUser by ventCardsViewModel.currentUser.collectAsState()

    // LaunchedEffectを使用して画面遷移時にのみloadVentCardsを呼び出す
    LaunchedEffect(currentUser.uid) {
        ventCardsViewModel.loadVentCards(currentUser.uid)
    }
    val ventCardItems by remember { derivedStateOf { ventCardsViewModel.ventCardItems } }

    val loadCardsState = ventCardsViewModel.loadCardsState.collectAsState()
    val likeCardState = ventCardsViewModel.likeState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {
            loadCardsState.value.status == Status.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            loadCardsState.value.status == Status.FAILURE && (noCardsLeft || ventCardItems.isEmpty()) -> {
                ErrorView(
                    retry = {
                        ventCardsViewModel.loadVentCards(currentUser.uid)
                    }
                )
            }

            loadCardsState.value.status == Status.SUCCESS && (noCardsLeft || ventCardItems.isEmpty()) -> {
                FlowRow(
                    Modifier.padding(16.dp).align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(id = R.string.no_ventCard_available), style = MaterialTheme.typography.titleLarge)
                }
            }

            loadCardsState.value.status == Status.IDLE -> {
            }

            else -> {
                // ここに共通のCardStackを配置
                if (ventCardItems.isNotEmpty()) {
                    Log.d("ventcardview", "ventCard is not empty")
                    Log.d("ventcardview", "${ventCardItems}")


                    CardStack(
                        modifier = Modifier,
                        enableButtons = true,
                        ventCardItems = ventCardItems,
                        onSwipeRight = { ventCard ->
                            ventCardsViewModel.handleLikeAction(
                                userId = currentUser.uid,
                                posterId = ventCard.posterId,
                                ventCardId = ventCard.swipeCardId
                            )
                        },
                        onSwipeLeft = { ventCard ->
                            debateCreationViewModel.ventCard = ventCard
                            Log.e("VCV", "ventCardId: ${ventCard.swipeCardId}")
                            debateCreationViewModel.getRelatedDebates(ventCard)
                            toDebateCreationView()
                        },
                        onEmptyStack = {
                            noCardsLeft = true
                        },
                        onLessStack = {
                            ventCardsViewModel.loadVentCards(currentUser.uid)
                        },
                        toAnotherUserPageView = toAnotherUserPageView,
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
