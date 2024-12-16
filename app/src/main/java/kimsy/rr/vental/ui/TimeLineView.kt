package kimsy.rr.vental.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.TimeLineViewModel
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference

@Composable
fun TimeLineView(
    timeLineViewModel: TimeLineViewModel = hiltViewModel(),
    toDebateView: () -> Unit
){
    val timeLineItems = timeLineViewModel.timelineItems

    val hasFinishedLoadingAllItems = timeLineViewModel.hasFinishedLoadingAllItems

    val getDebateItemState by timeLineViewModel.getDebateItemsState.collectAsState()

    LaunchedEffect(Unit) {
        timeLineViewModel.getTimeLineItems()
    }

    //とりあえずこれ。 TODO resource分岐追加

//    when {
//        timeLineItems.isNotEmpty() -> {
//            LazyColumn(){
//                items(timeLineItems) {item->
//                    timeLineItem(timeLineViewModel, toDebateView, item)
//                }
//                if (!hasFinishedLoadingAllItems) {
//                    item { LoadingIndicator(timeLineViewModel) }
//                }
//            }
//        }
//        else -> {
//            Text(text = "items are null")
//        }
//    }
    when {
        timeLineItems.isNotEmpty() -> {
            LazyColumn(){
                items(timeLineItems) {item->
                    timeLineItem(timeLineViewModel, toDebateView, item)
                }
                if (!hasFinishedLoadingAllItems) {
                    item { LoadingIndicator(timeLineViewModel) }
                }
            }
        }
        else -> {
            when (getDebateItemState.status){
                Status.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                Status.FAILURE -> Text(text = "討論の取得に失敗しまいた。")
                else -> {}
            }

        }
    }

}

@Composable
fun LoadingIndicator(timeLineViewModel: TimeLineViewModel) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.BottomCenter))
    }

    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        timeLineViewModel.getTimeLineItems()
    }
}

@Composable
fun timeLineItem(
    timeLineViewModel: TimeLineViewModel,
    toDebateView: () -> Unit,
    debateItem: DebateItem
) {
    val debate = debateItem.debate
    val ventCard = debateItem.ventCard
    val poster = debateItem.poster
    val debater = debateItem.debater
        Column(
            modifier = Modifier
                .clickable {
                    //TODO go to detail
                    timeLineViewModel.setDebateItemToModel(debateItem)
                    toDebateView()
                }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ){
                Image(
                    painter = rememberAsyncImagePainter(poster.photoURL),
                    contentDescription = "AccountIcon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            //TODO go to user
                        },
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = poster.name,
                            modifier = Modifier
                                .clickable {
                                    //TODO go to user
                                }
                        )
                    }
                    Text(text = ventCard.swipeCardContent)
                    ventCard.tags.forEach { tag->
                        Text(text = tag, color = MaterialTheme.colorScheme.primary)
                    }
                    Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                        contentDescription = "Image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )

                    Divider()

                }
            }

            // ここ2つアイコンのライン
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(debater.photoURL),
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                //TODO go to user
                            },
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = debater.name,
                        modifier = Modifier
                            .clickable {
                                //TODO go to user
                            }
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "heart")
                    }
                    Text(text = debate.debaterLikeCount.toString())
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "heart")
                    }
                    Text(text = debate.posterLikeCount.toString())
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(poster.photoURL),
                        contentDescription = "AccountIcon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                //TODO go to user
                            },
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = poster.name,
                        modifier = Modifier
                            .clickable {
                                //TODO go to user
                            }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .widthIn(max = 250.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = debate.firstMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Text(
                    text = debate.debateCreatedDatetime?.let {
                        formatTimeDifference(it)
                    } ?: "日付不明",
                )

            }
        }
        Divider()
}

//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun TimeLinePrev(){
//    TimeLineView()
//}
