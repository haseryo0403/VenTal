package kimsy.rr.vental.ui


import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.DebateCreationViewModel
import kimsy.rr.vental.ViewModel.VentCardsViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CardStack
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeCardsView(
    ventCardsViewModel: VentCardsViewModel = hiltViewModel(),
    debateCreationViewModel: DebateCreationViewModel,
    context: Context,
    toDebateCreationView: () -> Unit,
    authViewModel: AuthViewModel
){
//    val isLoading by ventCardsViewModel.isLoading
//    val errorMessage by ventCardsViewModel.errorMessage
//    var showDialog by remember { mutableStateOf(false)}
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
                Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                ventCardsViewModel.resetState()
            }

            loadCardsState.value.status == Status.SUCCESS && noCardsLeft -> {
                Text(text = "No ventCards available", modifier = Modifier.align(Alignment.Center)) // TODO: デザインを追加
            }

            else -> {
                // ここに共通のCardStackを配置
                if (ventCards.isNotEmpty()) {
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
                        }
                    )
                }
            }
        }

//        when (loadCardsState.value.status) {
//            Status.LOADING -> {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }
//            Status.FAILURE -> {
//                if (noCardsLeft || ventCards.isEmpty()) {
//                    Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
//                    //TODO ここに読み込みエラー画面の表示をする
//                ventCardsViewModel.resetState()
//                }
//            }
//            Status.SUCCESS -> {
//
////                if (noCardsLeft) {
//                if (ventCardsViewModel.hasFinishedLoadingAllCards && noCardsLeft) {
//                    Text(text = "No ventCards available")//TODO design
//                } else {
//                    CardStack(
//                        modifier = Modifier,
//                        enableButtons = true,
//                        items = ventCards,
//                        onSwipeRight = {ventCard ->
////                            ventCardsViewModel.handleLikeAction(
////                                userId = user.uid,
////                                posterId = ventCard.posterId,
////                                ventCardId = ventCard.swipeCardId
////                            )
//                        },
//                        onSwipeLeft = {ventCard->
//                            debateCreationViewModel.ventCardWithUser = ventCard
//                            Log.e("VCV", "ventCardId: ${ventCard.swipeCardId}")
//                            debateCreationViewModel.getRelatedDebates(ventCard)
//                            toDebateCreationView()
//                        },
//                        onEmptyStack = {
//                            noCardsLeft = true
////                            if (ventCardsViewModel.hasFinishedLoadingAllCards) {
////                                noCardsLeft = true
////                            } else {
////                                ventCardsViewModel.updateLoadingStatus()
////                            }
//                        },
//                        onLessStack = {
//                            //カード少なくなったら補充
//                            ventCardsViewModel.loadVentCards(user.uid)
//                        }
//                    )
//                }
//            }
//            else -> {
//                Text(text = "エラーーーーーーー") //TODO design
//            }
//        }
        if (likeCardState.value.status == Status.FAILURE) {
            Toast.makeText(context, "いいねに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
            ventCardsViewModel.resetState()
        }
//
//
//        if(showDialog){
//            AlertDialog(onDismissRequest = { showDialog = false },
//                confirmButton = { /*TODO*/ },
//                title = { Text(text = "ERROR")},
//                text = { Text(text = errorMessage?: "不明なエラーが発生しました。")}
//            )
//        }
//        if (isLoading && ventCards.isEmpty()) {
//            // データがまだロードされていない場合、ローディングインジケーターを表示
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//        } else if (noCardsLeft) {
//            Text(text = "No ventCards available")//TODO design
//        } else if (errorMessage != null) {
//            showDialog = true
//        } else {
//            // データがロードされた場合、CardStackを表示
//
//            CardStack(
//                modifier = Modifier,
//                enableButtons = true,
//                items = ventCards,
//                onSwipeRight = {ventCard ->
//                    ventCardsViewModel.handleLikeAction(
//                        userId = user.uid,
//                        posterId = ventCard.posterId,
//                        ventCardId = ventCard.swipeCardId
//                    )
//                },
//                onSwipeLeft = {ventCard->
//                    debateCreationViewModel.ventCardWithUser = ventCard
//                    Log.e("VCV", "ventCardId: ${ventCard.swipeCardId}")
//                    debateCreationViewModel.getRelatedDebates(ventCard)
//                    toDebateCreationView()
//                },
//                onEmptyStack = {
//                    if (ventCardsViewModel.hasFinishedLoadingAllCards) {
//                        noCardsLeft = true
//                    }
//                },
//                onLessStack = {
//                    //カード少なくなったら補充
//                    ventCardsViewModel.loadVentCards(user.uid)
//                }
//            )
//        }

    }
}
