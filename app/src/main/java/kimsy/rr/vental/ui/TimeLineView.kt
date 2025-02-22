package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.SharedDebateViewModel
import kimsy.rr.vental.viewModel.TimeLineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun TimeLineView(
    timeLineViewModel: TimeLineViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    var showDialog = remember { mutableStateOf(false) }
    val currentUser by timeLineViewModel.currentUser.collectAsState()
    val isRefreshing by timeLineViewModel.isRefreshing.collectAsState()

    val recentTimeLineItems by timeLineViewModel.recentTimelineItems.collectAsState()

    val popularTimeLineItems by timeLineViewModel.popularTimelineItems.collectAsState()

    val recentItemScrollState = rememberLazyListState()
    val scrollState = rememberScrollState()
    val popularItemScrollState = rememberLazyListState()

    val hasFinishedLoadingAllRecentItems = timeLineViewModel.hasFinishedLoadingAllRecentItems
    val hasFinishedLoadingAllPopularItems = timeLineViewModel.hasFinishedLoadingAllPopularItems

    val getDebateItemState by timeLineViewModel.getDebateItemsState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("新着", "人気")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(Unit) {
        Log.d("TLV", currentUser.toString())
        if (currentUser.newUserFlag) {
            Log.d("TLV", "new true")
            showDialog.value = true
        }
        timeLineViewModel.getRecentTimeLineItems()
        timeLineViewModel.getPopularTimeLineItems()
        recentItemScrollState.scrollToItem(
            timeLineViewModel.recentItemSavedScrollIndex,
            timeLineViewModel.recentItemSavedScrollOffset
        )
        popularItemScrollState.scrollToItem(
            timeLineViewModel.popularItemSavedScrollIndex,
            timeLineViewModel.popularItemSavedScrollOffset
        )
    }

    // スクロール位置を保存
    LaunchedEffect(recentItemScrollState) {
        snapshotFlow { recentItemScrollState.firstVisibleItemIndex to recentItemScrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                timeLineViewModel.setRecentItemScrollState(index, offset)
            }
    }
    // スクロール位置を保存
    LaunchedEffect(popularItemScrollState) {
        snapshotFlow { popularItemScrollState.firstVisibleItemIndex to popularItemScrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                timeLineViewModel.setPopularItemScrollState(index, offset)
            }
    }

    LaunchedEffect(pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
    }

    TutorialDialog(dialogOpen = showDialog,
        viewModel = timeLineViewModel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .width(200.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier
                        .padding(8.dp),
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = tab,
                                modifier = Modifier
                                    .padding(8.dp),
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
            }
        }

        when {
            recentTimeLineItems.isNotEmpty() -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.background)
                ) { index ->
                    when (index) {
                        0 -> {

//                            PullToRefreshBox(
//                                isRefreshing = isRefreshing,
//                                onRefresh = { timeLineViewModel.onRefreshRecentItem() }
//                            ) {
//                                Column(
//                                    modifier = Modifier
//                                        .background(color = MaterialTheme.colorScheme.background)
//                                        .verticalScroll(scrollState),
//                                ) {
//                                    recentTimeLineItems.forEachIndexed { debateIndex, item ->
//                                        DebateCard(
//                                            sharedDebateViewModel,
//                                            toDebateView,
//                                            toAnotherUserPageView,
//                                            onLikeStateSuccess = { debateItem ->
//                                                timeLineViewModel.onRecentDebateLikeSuccess(debateItem)
//                                            },
//                                            item
//                                        )
//                                        //TODO　多分これ機能しない
//                                        if ((debateIndex +1) % 7 == 0) {
//                                            LoadRecentDebateItems(timeLineViewModel)
//                                        }
//                                    }
//                                    LoadingIndicator(timeLineViewModel)
//                                }
//                            }


                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { timeLineViewModel.onRefreshRecentItem() }
                            ) {
                                LazyColumn(
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                                    state = recentItemScrollState
                                ) {
                                    itemsIndexed(recentTimeLineItems) {debateIndex, item ->
                                        DebateCard(
                                            sharedDebateViewModel,
                                            toDebateView,
                                            toAnotherUserPageView,
                                            onLikeStateSuccess = { debateItem ->
                                                timeLineViewModel.onRecentDebateLikeSuccess(debateItem)
                                            },
                                            item
                                        )
                                        if ((debateIndex +1) % 7 == 0) {
                                            LoadRecentDebateItems(timeLineViewModel)
                                        }
                                    }
                                    item { LoadingIndicator(timeLineViewModel) }
                                }
                            }
//


                        }

                        1 -> {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = { timeLineViewModel.onRefreshPopularItem() }
                            ) {
                                LazyColumn(state = popularItemScrollState) {
                                    itemsIndexed(popularTimeLineItems) { debateIndex, item ->
                                        DebateCard(
                                            sharedDebateViewModel,
                                            toDebateView,
                                            toAnotherUserPageView,
                                            onLikeStateSuccess = { debateItem ->
                                                timeLineViewModel.onPopularDebateLikeSuccess(debateItem)
                                            },
                                            item
                                        )
                                        if ((debateIndex + 1) % 7 == 0) {
                                            LoadPopularDebateItems(timeLineViewModel)
                                        }
                                    }
                                    item { LoadingIndicator(timeLineViewModel) }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                when (getDebateItemState.status) {
                    Status.LOADING -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    Status.FAILURE -> {
                        ErrorView(
                            retry = {
                                timeLineViewModel.getRecentTimeLineItems()
                                timeLineViewModel.getPopularTimeLineItems()
                                recentItemScrollState.scrollToItem(
                                    timeLineViewModel.recentItemSavedScrollIndex,
                                    timeLineViewModel.recentItemSavedScrollOffset
                                )
                                popularItemScrollState.scrollToItem(
                                    timeLineViewModel.popularItemSavedScrollIndex,
                                    timeLineViewModel.popularItemSavedScrollOffset
                                )
                            }
                        )
                    }

                    else -> {
                        timeLineViewModel.resetGetDebateItemState()
                    }
                }
            }
        }
    }
}

@Composable
fun LoadRecentDebateItems(timeLineViewModel: TimeLineViewModel) {
    val hasFinishedLoadingAllRecentItems = timeLineViewModel.hasFinishedLoadingAllRecentItems
    LaunchedEffect(Unit) {
        Log.d("TLV", "LRDI called")
        // 要素の追加読み込み
        if (!hasFinishedLoadingAllRecentItems){
            timeLineViewModel.getRecentTimeLineItems()
        }
    }
}
@Composable
fun LoadPopularDebateItems(timeLineViewModel: TimeLineViewModel) {
    val hasFinishedLoadingAllPopularItems = timeLineViewModel.hasFinishedLoadingAllPopularItems
    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        if (!hasFinishedLoadingAllPopularItems){
            timeLineViewModel.getPopularTimeLineItems()
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

//    LaunchedEffect(Unit) {
//        // 要素の追加読み込み
////        sharedDebateViewModel.getTimeLineItems()
//        timeLineViewModel.getRecentTimeLineItems()
//        Log.d("CUDUC", "LE")
//    }
}

//TODO ここでチュートリアル用の表示。新規登録時のみなので、サインアップで条件分岐かユーザードキュメントに新規フラグを作成するか。
@Composable
fun TutorialDialog(
    dialogOpen: MutableState<Boolean>,
    viewModel: TimeLineViewModel
    ) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.guide0),
        stringResource(id = R.string.guide1),
        stringResource(id = R.string.guide2),
        stringResource(id = R.string.guide3), 
        stringResource(id = R.string.guide4)
        )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
        Log.d("MyPageView", "Selected tab index: $selectedTabIndex")

    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        Log.d("MyPageView", "Selected tab index: $selectedTabIndex")
    }
    if (dialogOpen.value) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                if (selectedTabIndex == tabs.size-1) {
                    Button(onClick = {
                        viewModel.markUserNotNew()
                        Log.e("TLV", User.CurrentUserShareModel.getCurrentUserFromModel().toString())
                        dialogOpen.value = false
                    }) {
                        Text(text = stringResource(id = R.string.start))
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "アプリ使い方",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = tabs[selectedTabIndex]
                        )
                    }
                    
                    IconButton(onClick = {
                        viewModel.markUserNotNew()
                        dialogOpen.value = false
                    }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_clear_24), contentDescription = "clear")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxSize()
                ){
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth(),

                    ) { index ->
                        when (index) {
                            0 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.this_app_is),
                                    contentDescription = "guide1",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            1 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.create_card), 
                                    contentDescription = "guide1",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            2 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.swipe_view),
                                    contentDescription = "guide2",
                                    modifier = Modifier.fillMaxWidth()
                                )                           
                            }
                            3 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.debate_start),
                                    contentDescription = "guide3",
                                    modifier = Modifier.fillMaxWidth()
                                )                           
                            }
                            4 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.timeline_view),
                                    contentDescription = "guide4",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Row(
                            Modifier
                                .height(50.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(tabs.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(12.dp)
                                )
                            }
                        }
                    }

                }

            }
        )
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
