package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.MyDebateViewModel
import kimsy.rr.vental.viewModel.MyLikedDebateViewModel
import kimsy.rr.vental.viewModel.MyPageViewModel
import kimsy.rr.vental.viewModel.MyVentCardViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val currentUser by viewModel.currentUser.collectAsState()

    val scrollState = rememberLazyListState()

    val debateCountsState by viewModel.debateCountsState.collectAsState()

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
    }

    // TODO 修正
    val isScrolled = remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset > 0 } }
    val accountContentHeight by animateDpAsState(if (isScrolled.value) 0.dp else 140.dp)
    val tabRowHeight by animateDpAsState(if (isScrolled.value) 0.dp else 48.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(accountContentHeight)
                .background(MaterialTheme.colorScheme.background)
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
                                isFollowing = null
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

        // TabRow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tabRowHeight)
                .background(MaterialTheme.colorScheme.background)
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
                        onClick = { /* Handle Tab Click */ },
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
        }

        // HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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

@Composable
fun AccountContent(
    debateCounts: Int,
    user: User,
    toProfileEditView: (() -> Unit)?,
    followUser: (() -> Unit)?,
    unFollowUser: (() -> Unit)?,
    isFollowing: Boolean?
){
//    val debateCounts = userPageData.debatesCount

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
                    Text(text = debateCounts.toString())
                    Text(text = "討論")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = user.followerCount.toString())
                    Text(text = "フォロワー")
                }
            }

            if (toProfileEditView != null) {
                Button(onClick = { toProfileEditView() },

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
                            Text(text = "フォロー解除")
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
                            Text(text = "フォローする")
                        }
                    }
                    null ->{}
                }
            }
        }
        Divider()
    }
}

