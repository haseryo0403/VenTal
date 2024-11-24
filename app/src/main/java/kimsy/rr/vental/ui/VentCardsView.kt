package kimsy.rr.vental.ui


import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CardStack
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@Composable
fun VentCardsView(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    swipeThreshold: Float = 400f,
    sensitivityFactor: Float = 3f,
    content: @Composable () -> Unit
){
    var offset by remember { mutableStateOf(0f) }
    var dismissRight by remember { mutableStateOf(false) }
    var dismissLeft by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    LaunchedEffect(dismissRight) {
        if (dismissRight) {
            delay(300)
            onSwipeRight.invoke()
            dismissRight = false
        }
    }

    LaunchedEffect(dismissLeft) {
        if (dismissLeft) {
            delay(300)
            onSwipeLeft.invoke()
            dismissLeft = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .offset { IntOffset(offset.roundToInt(), 0) }
            .padding(top = 16.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(onDragEnd = {
                    offset = 0f
                }) { change, dragAmount ->

                    offset += (dragAmount / density) * sensitivityFactor
                    when {
                        offset > swipeThreshold -> {
                            dismissRight = true
                        }

                        offset < -swipeThreshold -> {
                            dismissLeft = true
                        }
                    }
                    if (change.positionChange() != Offset.Zero) change.consume()
                }
            }
            .graphicsLayer(
                alpha = 10f - animateFloatAsState(if (dismissRight) 1f else 0f).value,
                rotationZ = animateFloatAsState(offset / 50).value
            )) {
                content()

            }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            ElevatedButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(60.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp) // アイコンをボタンのサイズに収める
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_back_hand_24),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            ElevatedButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(width = 140.dp, height = 60.dp)
            ) {
                Text(text = "スキップ")
            }

            ElevatedButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .size(60.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp) // アイコンをボタンのサイズに収める
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_favorite_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        }
    }
}

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
    val isLoading by ventCardsViewModel.isLoading


    // currentUserをobserveしてStateとして取得
    val user by authViewModel.currentUser.observeAsState(User())

    // LaunchedEffectを使用して画面遷移時にのみloadVentCardsを呼び出す
    LaunchedEffect(user.uid) {
        ventCardsViewModel.loadVentCards(user.uid)
    }
    val ventCards by remember { derivedStateOf { ventCardsViewModel.ventCards } }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            // データがまだロードされていない場合、ローディングインジケーターを表示
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if(ventCardsViewModel.hasFinishedLoadingAllCards) {
            Text(text = "No ventCards available")//TODO design
        } else if (ventCards.isEmpty()) {
            //TODO 初回はlastVisibleがNUllつまり初回でemptyの場合はループするかも　Repositoryで空ならそれで判断しちゃっていいかもlike抜いたあとじゃなくてDB取得直後
            ventCardsViewModel.loadVentCards(user.uid)
        } else {
            // データがロードされた場合、CardStackを表示
            CardStack(
                modifier = Modifier,
                enableButtons = true,
                items = ventCards,
                onSwipeRight = {ventCard ->
                    ventCardsViewModel.handleLikeAction(
                        userId = user.uid,
                        posterId = ventCard.posterId,
                        ventCardId = ventCard.swipeCardId
                    )
                },
                onSwipeLeft = {ventCard->
                    debateCreationViewModel.ventCardWithUser = ventCard
                    Log.e("VCV", "ventCardId: ${ventCard.swipeCardId}")
                    debateCreationViewModel.getRelatedDebates(ventCard)
                    toDebateCreationView()
                },
                onEmptyStack = {},
                onLessStack = {
                    //カード少なくなったら補充
                    ventCardsViewModel.loadVentCards(user.uid)
                }
            )
        }
    }
}