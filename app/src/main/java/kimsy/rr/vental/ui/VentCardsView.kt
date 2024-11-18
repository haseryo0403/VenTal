package kimsy.rr.vental.ui


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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
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

@Composable
fun MySwipeCardDemo(
    ventCardsViewModel: VentCardsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
    ) {
    // currentUserをobserveしてStateとして取得
    val user by authViewModel.currentUser.observeAsState(User())

    // LaunchedEffectを使用して画面遷移時にのみloadVentCardsを呼び出す
    LaunchedEffect(user.uid) {
        ventCardsViewModel.loadVentCards(user.uid)
    }
    val ventCards by remember { derivedStateOf { ventCardsViewModel.ventCards } }

    ventCards.forEach { ventCard->
        VentCardsView(
            onSwipeLeft = {

            },
            onSwipeRight = {
                ventCardsViewModel.handleLikeAction(userId = user.uid, posterId = ventCard.posterId, ventCardId = ventCard.swipeCardId)
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f))
                {
                    ElevatedCard(
                        onClick = {},
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        ){
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ){
                                    //TODO　DBから自分が投稿したもの意外を取得
                                    Image(
                                        painter = rememberAsyncImagePainter(ventCard.posterImageURL),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(5f)
                                            .fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = ventCard.posterName)
                                            Text(
//                                                text = ventCard.swipeCardCreatedDateTime.toString()
                                                text = ventCard.swipeCardCreatedDateTime?.let {
                                                    formatTimeDifference(it)
                                                } ?: "日付不明"
                                            )
                                        }
                                        Text(text = ventCard.swipeCardContent)
                                        ventCard.tags.forEach {tag ->
                                            Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        //TODO color choose
                                        Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                                            contentDescription = "Image",
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                                                contentDescription = "haert")
                                            Text(text = ventCard.likeCount.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeCardsView(
    ventCardsViewModel: VentCardsViewModel = hiltViewModel(),
    toDebateCreationView: () -> Unit,
    authViewModel: AuthViewModel
){
    // currentUserをobserveしてStateとして取得
    val user by authViewModel.currentUser.observeAsState(User())

    // LaunchedEffectを使用して画面遷移時にのみloadVentCardsを呼び出す
    LaunchedEffect(user.uid) {
        ventCardsViewModel.loadVentCards(user.uid)
    }
    val ventCards by remember { derivedStateOf { ventCardsViewModel.ventCards } }

    if (
        ventCards.isEmpty()
        ) {
        // データがまだロードされていない場合、ローディングインジケーターを表示
        CircularProgressIndicator(

        )
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
            onSwipeLeft = {
                toDebateCreationView()
            },
            onEmptyStack = {},
            onLessStack = {
                //カード少なくなったら補充
                ventCardsViewModel.loadVentCards(user.uid)
            }
        )
    }}