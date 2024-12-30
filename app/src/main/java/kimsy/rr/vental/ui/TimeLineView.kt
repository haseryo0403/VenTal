package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.ViewModel.TimeLineViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun TimeLineView(
    timeLineViewModel: TimeLineViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
//    val isRefreshing by sharedDebateViewModel.isRefreshing.collectAsState()
    val isRefreshing by timeLineViewModel.isRefreshing.collectAsState()

//    val timeLineItems by sharedDebateViewModel.timelineItems.collectAsState()
    val timeLineItems by timeLineViewModel.timelineItems.collectAsState()

    val scrollState = rememberLazyListState()

//    val hasFinishedLoadingAllItems = sharedDebateViewModel.hasFinishedLoadingAllItems
    val hasFinishedLoadingAllItems = timeLineViewModel.hasFinishedLoadingAllItems

//    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
    val getDebateItemState by timeLineViewModel.getDebateItemsState.collectAsState()



    LaunchedEffect(Unit) {
//        sharedDebateViewModel.getTimeLineItems()
        timeLineViewModel.getTimeLineItems()

        scrollState.scrollToItem(
            timeLineViewModel.savedScrollIndex,
            timeLineViewModel.savedScrollOffset
        )
    }

    // スクロール位置を保存
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                timeLineViewModel.setScrollState(index, offset)
            }
    }



    val onRefresh: () -> Unit = {
        //ここでVMのロードする関数を呼びたいが、
        // ・loadingにはしたくない
        // ・いまロードしたものはいらないかな？　lastvisibleなしでロードして、ロードに成功したらtimelineitemsにそのままぶちこむ！
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // 2秒待機
        }

    }

    when {
        timeLineItems.isNotEmpty() -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
//                onRefresh = {sharedDebateViewModel.onRefresh()}
                onRefresh = {timeLineViewModel.onRefresh()}
            ) {
                LazyColumn(state = scrollState){
                    items(timeLineItems) {item->
                        DebateCard(
                            sharedDebateViewModel,
                            toDebateView,
                            toAnotherUserPageView,
                            onLikeStateSuccess = {
                                debateItem ->
                                    timeLineViewModel.onLikeSuccess(debateItem)
                            },
                            item)
                    }
                    if (!hasFinishedLoadingAllItems) {
//                        item { LoadingIndicator(sharedDebateViewModel) }
                        item { LoadingIndicator(timeLineViewModel) }
                    }
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
                Status.FAILURE -> {
                    Text(text = "討論の取得に失敗しまいた。")
                    timeLineViewModel.resetGetDebateItemState()
                }
                else -> {timeLineViewModel.resetGetDebateItemState()}
            }
        }
    }
}

@Composable
fun LoadingIndicator(timeLineViewModel: TimeLineViewModel) {
//    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
    val getDebateItemState by timeLineViewModel.getDebateItemsState.collectAsState()
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (getDebateItemState.status){
            Status.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Status.FAILURE -> Text(text = "討論の追加の取得に失敗しまいた。")
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // 要素の追加読み込み
//        sharedDebateViewModel.getTimeLineItems()
        timeLineViewModel.getTimeLineItems()
        Log.d("CUDUC", "LE")
    }
}
//
//@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//@Composable
//fun timeLineItem(
//    sharedDebateViewModel: SharedDebateViewModel,
//    toDebateView: () -> Unit,
//    debateItem: DebateItem
//) {
//    var debate = debateItem.debate
//    val ventCard = debateItem.ventCard
//    val poster = debateItem.poster
//    val debater = debateItem.debater
//
//    val likeState by sharedDebateViewModel.likeState.collectAsState()
//
//    when (likeState[debateItem]?.status) {
//        Status.SUCCESS -> {
//            //TODO もし必要なUIの処理があれば。
//        }
//        Status.FAILURE -> {
//            sharedDebateViewModel.showLikeFailedToast(LocalContext.current)
//            sharedDebateViewModel.resetLikeState(debateItem)
//        }
//        else -> {}
//    }
//
//
//
//    Column(
//            modifier = Modifier
//                .clickable {
//                    sharedDebateViewModel.setCurrentDebateItem(debateItem)
//                    toDebateView()
//                }
//                .padding(8.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth()
//            ){
//                Image(
//                    painter = rememberAsyncImagePainter(poster.photoURL),
//                    contentDescription = "AccountIcon",
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .clickable {
//                            //TODO go to user
//                        },
//                    contentScale = ContentScale.Crop
//                )
//
//                Column(
//                    modifier = Modifier.weight(5f)
//                ) {
//                    Row(modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween) {
//                        Text(
//                            text = poster.name,
//                            modifier = Modifier
//                                .clickable {
//                                    //TODO go to user
//                                }
//                        )
//                    }
//                    Text(text = ventCard.swipeCardContent)
//                    ventCard.tags.forEach { tag->
//                        Text(text = tag, color = MaterialTheme.colorScheme.primary)
//                    }
//                    Image(painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
//                        contentDescription = "Image",
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(16.dp))
//                            .fillMaxWidth(),
//                        contentScale = ContentScale.FillWidth
//                    )
//
//                    Divider()
//
//                }
//            }
//
//            // ここ2つアイコンのライン
//            Row(modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    Image(
//                        painter = rememberAsyncImagePainter(debater.photoURL),
//                        contentDescription = "AccountIcon",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .clickable {
//                                //TODO go to user
//                            },
//                        contentScale = ContentScale.Crop
//                    )
//                    Text(
//                        text = debater.name,
//                        modifier = Modifier
//                            .clickable {
//                                //TODO go to user
//                            }
//                    )
//                }
//                // ひだりいいね debater
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    IconButton(onClick = {
////                        timeLineViewModel.handleLikeDebaterAction(debateItem)
//                        sharedDebateViewModel.handleLikeAction(debateItem, UserType.DEBATER)
//                    }) {
//                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
//                            contentDescription = "heart",
//                            tint = if (debateItem.likedUserType == UserType.DEBATER) Color.Red else Color.Gray
//                            )
//                    }
//                    Text(text = debate.debaterLikeCount.toString())
//                }
//                // みぎいいね poster
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    IconButton(onClick = {
////                        timeLineViewModel.handleLikePosterAction(debateItem)
//                        sharedDebateViewModel.handleLikeAction(debateItem, UserType.POSTER)
//                    }) {
//                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
//                            contentDescription = "heart",
//                            tint = if (debateItem.likedUserType == UserType.POSTER) Color.Red else Color.Gray
//
//                        )
//                    }
//                    Text(text = debate.posterLikeCount.toString())
//                }
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.padding(8.dp)
//                ) {
//                    Image(
//                        painter = rememberAsyncImagePainter(poster.photoURL),
//                        contentDescription = "AccountIcon",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .clickable {
//                                //TODO go to user
//                            },
//                        contentScale = ContentScale.Crop
//                    )
//                    Text(
//                        text = poster.name,
//                        modifier = Modifier
//                            .clickable {
//                                //TODO go to user
//                            }
//                    )
//                }
//            }
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(4.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Bottom
//            ) {
//                Surface(
//                    modifier = Modifier
//                        .padding(4.dp)
//                        .widthIn(max = 250.dp),
//                    shape = MaterialTheme.shapes.medium,
//                    color = MaterialTheme.colorScheme.surfaceVariant,
//                    tonalElevation = 4.dp
//                ) {
//                    Column(
//                        modifier = Modifier.padding(12.dp)
//                    ) {
//                        Text(
//                            text = debate.firstMessage,
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                    }
//                }
//
//                Text(
//                    text = debate.debateCreatedDatetime?.let {
//                        formatTimeDifference(it)
//                    } ?: "日付不明",
//                )
//
//            }
//        }
//        Divider()
//}

//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun TimeLinePrev(){
//    TimeLineView()
//}
