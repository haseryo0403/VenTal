package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.RememberScrollState
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.MyDebateViewModel
import kimsy.rr.vental.viewModel.MyLikedDebateViewModel
import kimsy.rr.vental.viewModel.MyPageViewModel
import kimsy.rr.vental.viewModel.MyVentCardViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel

@OptIn(ExperimentalFoundationApi::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun MyPageView(
    viewModel: MyPageViewModel,
    myDebateViewModel: MyDebateViewModel,
    myVentCardViewModel: MyVentCardViewModel,
    myLikedDebateViewModel: MyLikedDebateViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toProfileEditView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit
){
    var accountContentHeight by remember { mutableStateOf(400.dp) } // 初期値は仮


    val (nestedScrollConnection, offset) = RememberScrollState(
        maxOffset = accountContentHeight,
        initialOffset = accountContentHeight
    )


// accountContentHeightが決まった後に呼び出すようにする
//    val (nestedScrollConnection, offset) = remember(accountContentHeight) {
//        RememberScrollState(
//            maxOffset = accountContentHeight,
//            initialOffset = accountContentHeight
//        )
//    }

    val density = LocalDensity.current
//    var scrollState = remember {
//        ConnectionSample(
//            maxOffset = accountContentHeight,
//            initialOffset = accountContentHeight,
//            density = density,
//        )
//    }
//    val nestedScrollConnection = scrollState.nestedScrollConnection
//    val offset = scrollState.offset




    val parentScrollState = rememberLazyListState()

    val currentUser by viewModel.currentUser.collectAsState()

    val debateCountsState by viewModel.debateCountsState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("討論", "カード", "いいね")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // ProfileSections のスクロール状態
    val accountContentScrollState = rememberScrollableState { delta ->
        // Offset を更新してスクロール連携
        nestedScrollConnection.onPreScroll(Offset(0f, delta), NestedScrollSource.Drag).y
    }

    LaunchedEffect(accountContentHeight){
        Log.d("MyPageView", "AccountContent Height: $accountContentHeight")
    }


    LaunchedEffect(key1 = selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
        Log.d("MyPageView", "Selected tab index: $selectedTabIndex")

    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        Log.d("MyPageView", "Selected tab index: $selectedTabIndex")
    }

    LaunchedEffect(Unit) {
        viewModel.updateCurrentUser()
        viewModel.loadUserPageData()
        Log.d("MyPageView", "Selected tab index: ${tabs.size}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(offset)
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = accountContentScrollState
                ) // スクロール可能にする

        ) {
            when (debateCountsState.status) {
                Status.LOADING -> {
                    // TODO: Loading
                }
                Status.SUCCESS -> {
                    debateCountsState.data?.let {
                        currentUser?.let { user ->
                            AccountContent(
                                debateCounts = it,
                                user = user,
                                toProfileEditView = toProfileEditView,
                                followUser = null,
                                unFollowUser = null,
                                isFollowing = null,
                                returnHeight = {
                                    accountContentHeight = it
                                }
                            )
                        }
                    }
                }
                Status.FAILURE -> {
                    //TODO　データが取得できない場合の仮の討論数、フォロワー数を表示する
                    ErrorView(retry = {
                        viewModel.updateCurrentUser()
                        viewModel.loadUserPageData()
                    })
                }
                else -> {}
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offset)
//                .background(color = MaterialTheme.colorScheme.background)
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
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.padding(8.dp),
                        content = {
                            Text(
                                text = tab,
                                color = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(color = MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.Top
            ) { index ->
                when (index) {
                    0 -> {
                        MyDebateView(
                            viewModel = myDebateViewModel,
                            sharedDebateViewModel = sharedDebateViewModel,
                            toDebateView = toDebateView,
                            toAnotherUserPageView = toAnotherUserPageView
                        )
                    }
                    1 -> {
                        MyVentCardView(
                            viewModel = myVentCardViewModel,
                            toReportVentCardView = toReportVentCardView,
                            toRequestVentCardDeletionView = toRequestVentCardDeletionView
                        )
                    }
                    2 -> {
                        MyLikedDebateView(
                            viewModel = myLikedDebateViewModel,
                            sharedDebateViewModel = sharedDebateViewModel,
                            toDebateView = toDebateView,
                            toAnotherUserPageView = toAnotherUserPageView
                        )
                    }
                }
            }
        }
    }

//
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(color = MaterialTheme.colorScheme.background)
//    ) {
//            when (debateCountsState.status) {
//                Status.LOADING -> {
//                    // TODO: Loading
//                }
//                Status.SUCCESS -> {
//                    debateCountsState.data?.let {
//                        currentUser?.let { user ->
//                            AccountContent(
//                                debateCounts = it,
//                                user = user,
//                                toProfileEditView = toProfileEditView,
//                                followUser = null,
//                                unFollowUser = null,
//                                isFollowing = null
//                            )
//                        }
//                    }
//                }
//                Status.FAILURE -> {
//                    //TODO　データが取得できない場合の仮の討論数、フォロワー数を表示する
//                    ErrorView(retry = {
//                        viewModel.updateCurrentUser()
//                        viewModel.loadUserPageData()
//                    })
//                }
//                else -> {}
//            }
//            TabRow(
//                selectedTabIndex = selectedTabIndex,
//                indicator = { tabPositions ->
//                    TabRowDefaults.Indicator(
//                        modifier = Modifier
//                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
//                            .width(200.dp)
//                    )
//                },
//                containerColor = MaterialTheme.colorScheme.background
//            ) {
//                tabs.forEachIndexed { index, tab ->
//                    Tab(
//                        selected = selectedTabIndex == index,
//                        onClick = { selectedTabIndex = index },
//                        modifier = Modifier.padding(8.dp),
//                        content = {
//                            Text(
//                                text = tab,
//                                color = if (selectedTabIndex == index)
//                                    MaterialTheme.colorScheme.primary
//                                else
//                                    MaterialTheme.colorScheme.onBackground
//                            )
//                        }
//                    )
//                }
//            }
//
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier
//                .fillMaxSize()
//                .weight(1f)
//
//        ) { index ->
//            when (index) {
//                0 -> {
//                    MyDebateView(
//                        viewModel = myDebateViewModel,
//                        sharedDebateViewModel = sharedDebateViewModel,
//                        toDebateView = toDebateView,
//                        toAnotherUserPageView = toAnotherUserPageView
//                    )
//                }
//                1 -> {
//                    MyVentCardView(
//                        viewModel = myVentCardViewModel,
//                        toReportVentCardView = toReportVentCardView,
//                        toRequestVentCardDeletionView = toRequestVentCardDeletionView
//                    )
//                }
//                2 -> {
//                    MyLikedDebateView(
//                        viewModel = myLikedDebateViewModel,
//                        sharedDebateViewModel = sharedDebateViewModel,
//                        toDebateView = toDebateView,
//                        toAnotherUserPageView = toAnotherUserPageView
//                    )
//                }
//            }
//        }
//    }
//


}

@Composable
fun AccountContent(
    debateCounts: Int,
    user: User,
    toProfileEditView: (() -> Unit)?,
    followUser: (() -> Unit)?,
    unFollowUser: (() -> Unit)?,
    isFollowing: Boolean?,
    returnHeight: (height: Dp) -> Unit
){
    val density = LocalDensity.current


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .onGloballyPositioned { coordinates ->
                returnHeight(
                    with(density) {
                        coordinates.size.height.toDp() // 高さをdpで取得
                        // coordinates.size.width.toDp() // こちらは幅をdpで取得
                    }
                )

            }
        ,
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

                Image(
                    painter = rememberAsyncImagePainter(user.photoURL),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                user.name.let { Text(text = it) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = debateCounts.toString())
                    Text(text = stringResource(id = R.string.debate))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = user.followerCount.toString())
                    Text(text = stringResource(id = R.string.follower))
                }
            }
            user.selfIntroduction?.let { Text(text = it) }

            if (toProfileEditView != null) {
                Button(
                    onClick = { toProfileEditView() },
                    modifier = Modifier.padding(top = 4.dp)
                    ) {
                    Text(text = "プロフィールを編集")
                }
            } else {
                when (isFollowing) {
                    true -> {
                        Button(
                            onClick = {
                                if (unFollowUser != null) {
                                    unFollowUser()
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.unfollow))
                        }
                    }
                    false -> {
                        OutlinedButton(
                            onClick = {
                                if (followUser != null) {
                                    followUser()
                                }
                            }
                        ) {
                            Text(text = stringResource(id = R.string.follow))
                        }
                    }
                    null ->{}
                }
            }
        }
        Divider()
    }
}

