package kimsy.rr.vental.ui

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.FollowPageViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun FollowPageView(
    viewModel: FollowPageViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
    toFollowListView: () -> Unit
){
    val followingUserState by viewModel.followingUserState.collectAsState()
    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()
    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadFollowingUserDebates()
        scrollState.scrollToItem(
            viewModel.debateItemSavedScrollIndex,
            viewModel.debateItemSavedScrollOffset
        )
    }

    LaunchedEffect(scrollState) {
        snapshotFlow{ scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset}
            .collect{(index, offset) ->
            viewModel.setScrollState(index, offset)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        if (followingUserState.status == Status.FAILURE || getDebateItemState.status == Status.FAILURE) {
            ErrorView(retry = {
                viewModel.loadFollowingUserDebates()
            })
        } else {
            FollowingUserView(
                viewModel = viewModel,
                toAnotherUserPageView = toAnotherUserPageView,
                toFollowListView = toFollowListView
            )

            Divider()

            FollowUserDebateView(
                viewModel = viewModel,
                sharedDebateViewModel = sharedDebateViewModel,
                toDebateView = toDebateView,
                toAnotherUserPageView = toAnotherUserPageView,
                scrollState = scrollState
            )
        }


    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun FollowingUserView(
    viewModel: FollowPageViewModel,
    toAnotherUserPageView: (user: User) -> Unit,
    toFollowListView: () -> Unit
) {
    val followingUserState by viewModel.followingUserState.collectAsState()
    val followingUser by viewModel.followingUser.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (followingUser.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.weight(1f)
            ) {
                items(followingUser) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(user.photoURL),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(text = user.name)
                    }
                }
            }
            TextButton(onClick = { toFollowListView() }) {
                Text(text = stringResource(id = R.string.all))
            }
        } else {
            when(followingUserState.status) {
                Status.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Status.FAILURE -> {
                    ErrorView(retry = {
                        viewModel.loadFollowingUserDebates()
                    })
                }
                Status.SUCCESS -> {
                    Text(text = stringResource(id = R.string.please_follow_to_show_debates))
                }
                else -> {}
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUserDebateView(
    viewModel: FollowPageViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
    scrollState: LazyListState
){
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val debateItems by viewModel.debateItems.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllDebateItems

    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()
    

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = scrollState
        ) {
            when {
                debateItems.isNotEmpty() -> {
                    items(debateItems) { item->
                        DebateCard(
                            sharedDebateViewModel,
                            toDebateView,
                            toAnotherUserPageView,
                            onLikeStateSuccess = { debateItem ->
                                viewModel.onLikeSuccess(debateItem)
                            },
                            item)                        }
                    if (!hasFinishedLoadingAllItems) {
                        item { FollowPageLoadingIndicator(viewModel = viewModel) }
                    }
                }
                else -> {
                    item {
                        when (getDebateItemState.status) {
                            Status.LOADING -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            Status.FAILURE -> {
                                ErrorView(retry = {
                                    viewModel.loadFollowingUserDebates()
                                })
                            }
                            Status.SUCCESS -> {
                                Text(text = stringResource(id = R.string.please_follow_to_show_debates) )
                            }
                            else -> viewModel.resetGetDebateItemState()
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun FollowPageLoadingIndicator(viewModel: FollowPageViewModel) {
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
            Status.FAILURE -> {
                Text(text = stringResource(id = R.string.get_extra_debate_failure))
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        viewModel.loadFollowingUserDebates()
    }
}


//
//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun FollowsPrev(){
//    FollowPageView()
//}