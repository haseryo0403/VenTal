package kimsy.rr.vental.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.VentCardsViewModel
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


private enum class OpenedSwipeableState {
    INITIAL,
    OPENED,
    OVER_SWIPED
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableRow(
    onSwipe: () -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val constraintsScope = this
        // 画面の横幅
        val maxWidthPx = with(LocalDensity.current) {
            constraintsScope.maxWidth.toPx()
        }
        // 削除ボタンの横幅
        val deleteButtonWidth = 64.dp
        val deleteButtonWidthPx = with(LocalDensity.current) {
            deleteButtonWidth.toPx()
        }
        val anchors = DraggableAnchors {
            OpenedSwipeableState.INITIAL at 0f
            OpenedSwipeableState.OPENED at deleteButtonWidthPx
            OpenedSwipeableState.OVER_SWIPED at maxWidthPx
        }
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()
        val anchorDraggableState = remember {
            AnchoredDraggableState(
                initialValue = OpenedSwipeableState.INITIAL,
                confirmValueChange = {
                    when (it) {
                        OpenedSwipeableState.INITIAL   -> {
                            // do nothing
                        }
                        OpenedSwipeableState.OPENED      -> {
                            // Opened Event
                        }
                        OpenedSwipeableState.OVER_SWIPED -> {
                            // Over Swipe Event
                            // todo delete
                            onSwipe.invoke()
                        }
                    }
                    true
                },
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { 5000f }, // 横スワイプですぐに消えてしまうため、大きい数値を設定
                snapAnimationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                decayAnimationSpec = decayAnimationSpec,
            )
        }

        Box(
            Modifier.anchoredDraggable(
                state = anchorDraggableState,
                reverseDirection = true,
                orientation = Orientation.Horizontal,
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset { IntOffset(-anchorDraggableState.offset.roundToInt(), 0) }
            ) {
                content()
            }
        }
    }
}



@Composable
fun MySwipeCardDemo(viewModel: VentCardsViewModel) {
    // currentUserをobserveしてStateとして取得
    val user by viewModel.currentUser.observeAsState()
    viewModel.loadVentCards()
    val ventCards by viewModel.ventCards.observeAsState(emptyList())

    VentCardsView(
        onSwipeLeft = { /* 左にスワイプしたときの処理 */ },
        onSwipeRight = { /* 右にスワイプしたときの処理 */ },
        content = {
            ElevatedCard(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
//                    .padding(top = 32.dp)
            ){
                Box(modifier = Modifier.fillMaxSize()){
                    LazyColumn(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        items(ventCards){ventCard ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                        modifier = Modifier.weight(5f)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = ventCard.posterName)
                                            Text(text = ventCard.swipeCardCreatedDateTime.toString())
                                        }
                                        Text(text = ventCard.swipeCardContent)
                                        ventCard.tags.forEach {tag ->
                                            Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        //TODO color choose
                                        Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                                            contentDescription = "Image",
                                            modifier = Modifier.clip(RoundedCornerShape(16.dp)))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                                                contentDescription = "haert")
                                            Text(text = ventCard.likeCount.toString())
                                        }
                                    }
                                }

                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ){
                                //TODO　DBから自分が投稿したもの意外を取得
                                Image(
                                    painter = rememberAsyncImagePainter(user?.photoURL),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier.weight(5f)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        user?.name?.let { Text(text = it) }
                                        Text(text = "23時間")
                                    }
                                    Text(text = "アストンマーチンは、英国発の高級スポーツカーメーカーで、美しいデザインと圧倒的なパフォーマンスが特徴です。映画『007』シリーズでのボンドカーとしても有名で、エレガンスと力強さを融合させた独自のスタイルが世界中の愛車家に支持されています。文字数を最大の140文字に設定してあります")
                                    Text(text = "#学校", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    //TODO color choose
                                    Image(painter = painterResource(id = R.drawable.aston_martin),
                                        contentDescription = "Image",
                                        modifier = Modifier.clip(RoundedCornerShape(16.dp)))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Icon(painter = painterResource(id = R.drawable.baseline_heart_broken_24),
                                            contentDescription = "haert")
                                        Text(text = "64")
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

//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun VentCardsPrev(){
//    MySwipeCardDemo()
//}