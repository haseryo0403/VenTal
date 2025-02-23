package kimsy.rr.vental.ui.CommonComposable


import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ThresholdConfig
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardItem
import kimsy.rr.vental.ui.commonUi.VentCardBottomSheet
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun CardStack(modifier : Modifier = Modifier,
              ventCardItems: List<VentCardItem>,
              thresholdConfig: (Float, Float) -> ThresholdConfig = { _, _ -> FractionalThreshold(0.2f) },
              velocityThreshold: Dp = 125.dp,
              enableButtons: Boolean = false,
              onSwipeLeft : ( item : VentCard) -> Unit = {},
              onSwipeRight : ( item : VentCard) ->  Unit = {},
              onEmptyStack : () -> Unit = {},
              onLessStack : () -> Unit = {},
              toAnotherUserPageView: (user: User) -> Unit,
              toReportVentCardView: () -> Unit,
              toRequestVentCardDeletionView: () -> Unit
){
    var j by remember { mutableStateOf(0)}
    val i by remember { derivedStateOf{ventCardItems.size-1-j} }

    var hasOnLessStackCalled by remember { mutableStateOf(false) }

    LaunchedEffect(ventCardItems.size){
        hasOnLessStackCalled = false
    }

    if( i == -1 ){
        onEmptyStack()
    } else if (i <= 2 && !hasOnLessStackCalled){
        //ventCardをロード
        onLessStack()
        hasOnLessStackCalled = true
    }

    val cardStackController = rememberCardStackController()
    cardStackController.onSwipeLeft = {
        onSwipeLeft(ventCardItems[j].ventCard)
        j++
    }
    cardStackController.onSwipeRight = {
        onSwipeRight(ventCardItems[j].ventCard)
        j++
    }

    ConstraintLayout(modifier = modifier
        .fillMaxSize()
        .padding(20.dp)) {
        val (buttons, stack) = createRefs()

        if(enableButtons){
            Row( modifier = Modifier
                .fillMaxWidth()
                .constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                    top.linkTo(stack.bottom)
                }
                .zIndex(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                FloatingActionButton(
                    onClick = { if (i >= 0) cardStackController.swipeLeft() },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable._removebg_preview),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer( modifier = Modifier.width(70.dp))
                FloatingActionButton(
                    onClick = { if (i >= 0) cardStackController.swipeRight() },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_favorite_border_24),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Box(modifier = Modifier
            .constrainAs(stack) {
                top.linkTo(parent.top)
            }
            .draggableStack(
                controller = cardStackController,
                thresholdConfig = thresholdConfig,
                velocityThreshold = velocityThreshold
            )
            .fillMaxHeight(0.85f)
        ){
            ventCardItems.asReversed().forEachIndexed{ index, item ->
                Card(modifier = Modifier
                    .moveTo(
                        x = if (index == i) cardStackController.offsetX.value else 0f,
                        y = if (index == i) cardStackController.offsetY.value else 0f
                    )
                    .visible(visible = index == i || index == i - 1)
                    .graphicsLayer(
                        rotationZ = if (index == i) cardStackController.rotation.value else 0f,
                        scaleX = if (index < i) cardStackController.scale.value else 1f,
                        scaleY = if (index < i) cardStackController.scale.value else 1f
                    )
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    ,
                    item,
                    toAnotherUserPageView = toAnotherUserPageView,
                    toReportVentCardView = toReportVentCardView,
                    toRequestVentCardDeletionView = toRequestVentCardDeletionView
                )
            }
        }
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    ventCardItem: VentCardItem,
    toAnotherUserPageView: (user: User) -> Unit,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit
){
    val activity = LocalContext.current as Activity
    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()
    val ventCard = ventCardItem.ventCard
    val poster = ventCardItem.poster




    Surface(
        modifier
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()            .background(color = MaterialTheme.colorScheme.surface)

        ) {
            //スワイプカードのコンテント以外の要素
            item {
                Box(modifier = Modifier.fillMaxWidth()) {

                    Image(
                        painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        Row{
                            ExpandableTagRow(tags = ventCard.tags)

                            IconButton(onClick = {
                                activity.showAsBottomSheet { hideModal ->
                                    if (currentUser != null) {
                                        VentCardBottomSheet(
                                            modifier = Modifier.fillMaxWidth(),
                                            ventCard = ventCard,
                                            currentUserId = currentUser.uid,
                                            toReportVentCardView = toReportVentCardView,
                                            toRequestVentCardDeletionView = toRequestVentCardDeletionView,
                                            hideModal = hideModal
                                        )
                                    }
                                }
                            }){
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                    contentDescription = "option",
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.4f), shape = CircleShape)

                                )
                            }
                        }

                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(poster.photoURL),
                            contentDescription = "AccountIcon",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .clickable {
                                    toAnotherUserPageView(poster)
                                },
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            val textColor = if (ventCard.swipeCardImageURL.isEmpty()) {
                                MaterialTheme.colorScheme.onSurface// 画像がない場合のテキスト色（例: グレー）
                            } else {
                                Color.White // 画像がある場合のテキスト色（白）
                            }

                            val shadow = if (ventCard.swipeCardImageURL.isEmpty()) {
                                null // 画像がない場合はシャドウなし
                            } else {
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.6f), // シャドウの色を設定
                                    offset = Offset(1f, 1f), // シャドウの位置を調整
                                    blurRadius = 4f // シャドウのぼかし具合
                                )
                            }

                            androidx.compose.material3.Text(
                                text = poster.name,
                                modifier = Modifier
                                    .clickable {
                                        toAnotherUserPageView(poster)
                                    },
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    shadow = shadow // 影の有無を条件に応じて設定
                                )
                            )

                            val formattedDate = ventCard.swipeCardCreatedDateTime?.let {
                                formatTimeDifference(it)
                            } ?: "日付不明"
                            Text(
                                text = formattedDate,
                                color = textColor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    shadow = shadow // 影の有無を条件に応じて設定
                                )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = ventCard.swipeCardContent, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

fun Modifier.moveTo(
    x: Float,
    y: Float
) = this.then(Modifier.layout{measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height){
        placeable.placeRelative(x.roundToInt(),y.roundToInt())
    }
})

fun Modifier.visible(
    visible: Boolean = true
) = this.then(Modifier.layout{measurable, constraints ->
    val placeable = measurable.measure(constraints)
    if(visible){
        layout(placeable.width, placeable.height){
            placeable.placeRelative(0,0)
        }
    }else{
        layout(0, 0) {}
    }
})

