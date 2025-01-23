package kimsy.rr.vental.ui.CommonComposable


import android.app.Activity
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.ui.commonUi.VentCardBottomSheet
import kotlin.math.roundToInt

/**
 * A stack of cards that can be dragged.
 * If they are dragged after a [thresholdConfig] or exceed the [velocityThreshold] the card is swiped.
 *
 * @param items Cards to show in the stack.
 * @param thresholdConfig Specifies where the threshold between the predefined Anchors is. This is represented as a lambda
 * that takes two float and returns the threshold between them in the form of a [ThresholdConfig].
 * @param velocityThreshold The threshold (in dp per second) that the end velocity has to exceed
 * in order to swipe, even if the positional [thresholds] have not been reached.
 * @param enableButtons Show or not the buttons to swipe or not
 * @param onSwipeLeft Lambda that executes when the animation of swiping left is finished
 * @param onSwipeRight Lambda that executes when the animation of swiping right is finished
 * @param onEmptyRight Lambda that executes when the cards are all swiped
 */

@ExperimentalMaterialApi
@Composable
fun CardStack(modifier : Modifier = Modifier,
              items: List<VentCardWithUser>,
              thresholdConfig: (Float, Float) -> ThresholdConfig = { _, _ -> FractionalThreshold(0.2f) },
              velocityThreshold: Dp = 125.dp,
              enableButtons: Boolean = false,
              onSwipeLeft : ( item : VentCardWithUser) -> Unit = {},
              onSwipeRight : ( item : VentCardWithUser) ->  Unit = {},
              onEmptyStack : () -> Unit = {},
              onLessStack : () -> Unit = {},
              toReportVentCardView: () -> Unit,
              toRequestVentCardDeletionView: () -> Unit
){
    var j by remember { mutableStateOf(0)}
    val i by remember { derivedStateOf{items.size-1-j} }

    var hasOnLessStackCalled by remember { mutableStateOf(false) }
    Log.e("CS", "index: $i")

    LaunchedEffect(items.size){
        Log.d("CS", "i size changed")
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
        onSwipeLeft(items[j])
        j++
    }
    cardStackController.onSwipeRight = {
        onSwipeRight(items[j])
        j++
        Log.e("CS", "index: $i")
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
                    backgroundColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(5.dp)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "", tint = Color.Green
                    )
                }
                Spacer( modifier = Modifier.width(70.dp))
                FloatingActionButton(
                    onClick = { if (i >= 0) cardStackController.swipeRight() },
                    backgroundColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(5.dp)
                ) {
                    Icon(Icons.Outlined.Favorite,contentDescription = "", tint = Color.Red)
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
            items.asReversed().forEachIndexed{ index, item ->
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
                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                    item,
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
    item: VentCardWithUser = VentCardWithUser(),
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit
){
    val activity = LocalContext.current as Activity
    val currentUser = User.CurrentUserShareModel.getCurrentUserFromModel()

    //TODO delete
    val ventCard = VentCard(
        swipeCardId = item.swipeCardId,
        posterId = item.posterId,
        swipeCardContent = item.swipeCardContent,
        swipeCardImageURL = item.swipeCardImageURL,
        likeCount = item.likeCount,
        tags = item.tags,
        swipeCardReportFlag = item.swipeCardReportFlag,
        swipeCardDeletionRequestFlag = item.swipeCardDeletionRequestFlag,
        debateCount = item.debateCount,
        swipeCardCreatedDateTime = item.swipeCardCreatedDateTime
    )


    Surface(
        modifier
    ){
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
                        painter = rememberAsyncImagePainter(item.posterImageURL),
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
                            androidx.compose.material3.Text(text = item.posterName)
                            androidx.compose.material3.Text(
                                text = item.swipeCardCreatedDateTime?.let {
                                    formatTimeDifference(it)
                                } ?: "日付不明"
                            )
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

                            }) {
                                androidx.compose.material3.Icon(
                                    painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                    contentDescription = "option"
                                )
                            }
                        }
                        androidx.compose.material3.Text(text = item.swipeCardContent)
                        item.tags.forEach {tag ->
                            Text(text = tag, color = MaterialTheme.colorScheme.primary)
                        }
                        //TODO color choose
                        Image(painter = rememberAsyncImagePainter(item.swipeCardImageURL),
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
                            androidx.compose.material3.Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                                contentDescription = "haert")
                            androidx.compose.material3.Text(text = item.likeCount.toString())
                        }
                    }
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

