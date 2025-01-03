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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.ViewModel.MyPageViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserPageData
import kimsy.rr.vental.ui.CommonComposable.DebateCard

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun MyPageView(
    viewModel: MyPageViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toProfileEditView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val currentUser by viewModel.currentUser.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val myPageItems by viewModel.myPageItems.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllItems

    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()

    val scrollState = rememberLazyListState()

    val userPageDataState by viewModel.userPageDateState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("討論", "カード", "いいね")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    LaunchedEffect(Unit) {
        viewModel.updateCurrentUser()
        viewModel.loadUserPageData()
        viewModel.getMyPageDebateItems()
        scrollState.scrollToItem(
            viewModel.savedScrollIndex,
            viewModel.savedScrollOffset
        )
    }

    // スクロール位置を保存
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.setScrollState(index, offset)
            }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        when (userPageDataState.status) {
            Status.LOADING -> {
                Log.d("MPV", "upds loading")
                //TODO Loading
            }
            Status.SUCCESS -> {
                Log.d("MPV", "upds sccess")
                userPageDataState.data?.let {
                    currentUser?.let { it1 ->
                        AccountContent(
                            userPageData = it,
                            user = it1,
                            toProfileEditView = toProfileEditView
                        )
                    }
                }
                Divider()
            }
            Status.FAILURE -> {
                Log.d("MPV", "upds failure")
                //TODO error-handling
            }
            else -> {}
        }

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

        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.onRefresh() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background),
                state = scrollState
            ){
                item {

                }

                when {
                    myPageItems.isNotEmpty() -> {
                        items(myPageItems) {item->
                            DebateCard(
                                sharedDebateViewModel,
                                toDebateView,
                                toAnotherUserPageView,
                                onLikeStateSuccess = {
                                        debateItem ->
                                    viewModel.onLikeSuccess(debateItem)
                                },
                                item)                        }
                        if (!hasFinishedLoadingAllItems) {
                            item { MyPageLoadingIndicator(viewModel) }
                        }
                    }
                    else -> {
                        item {
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
    //                                sharedDebateViewModel.resetGetDebateItemState()
                                    viewModel.resetGetDebateItemState()
                                }
                                else -> {viewModel.resetGetDebateItemState()}
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun AccountContent(
    userPageData: UserPageData,
    user: User,
    toProfileEditView: (() -> Unit)?
){
    val debatesCount = userPageData.debatesCount
    val followerCount = userPageData.followerCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Log.d("TAG", "Image URL: ${user?.photoURL}")
                Image(
                    painter = rememberAsyncImagePainter(user?.photoURL),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                user?.name?.let { Text(text = it) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = debatesCount.toString())
                    Text(text = "討論")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = followerCount.toString())
                    Text(text = "フォロワー")
                }
            }
            Button(onClick = { if (toProfileEditView != null) toProfileEditView() },

                ) {
                Text(text = "プロフィールを編集")
            }
        }
        Divider()
    }
}

@Composable
fun MyPageLoadingIndicator(viewModel: MyPageViewModel) {
//    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()
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
//        sharedDebateViewModel.getMyPageDebateItems()
        viewModel.getMyPageDebateItems()
        Log.d("CUDUC", "LE")
    }
}

