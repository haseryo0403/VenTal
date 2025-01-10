package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.ViewModel.AnotherUserPageViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun AnotherUserPageView(
    viewModel: AnotherUserPageViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val anotherUser by viewModel.anotherUser.collectAsState()
    val anotherUserPageItems by viewModel.anotherUserPageItems.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllItems

    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()

    val scrollState = rememberLazyListState()

    val userPageDataState by viewModel.userPageDateState.collectAsState()

    val followingUserIdsState by viewModel.followingUserIdsState.collectAsState()

    val followingUserIds = when (followingUserIdsState.status) {
        Status.SUCCESS -> {
            followingUserIdsState.data
        }
        else -> {null}
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserPageData()
        viewModel.observeFollowingUserIds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    anotherUser?.let { it1 ->
                        AccountContent(
                            userPageData = it,
                            user = it1,
                            toProfileEditView = null,
                            followUser = {viewModel.followUser(it1.uid)},
                            unFollowUser = {viewModel.unFollowUser(it1.uid)},
                            isFollowing = followingUserIds?.contains(it1.uid)
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

        AnotherUserDebateView(
            viewModel = viewModel,
            sharedDebateViewModel = sharedDebateViewModel,
            toDebateView = toDebateView,
            toAnotherUserPageView = toAnotherUserPageView
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun AnotherUserDebateView(
    viewModel: AnotherUserPageViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val debateItems by viewModel.debateItems.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllItems

    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.getAnotherUserPageDebateItems()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                debateItems.isNotEmpty() -> {
                    items(debateItems) { item->
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
                        item { AnotherUserPageLoadingIndicator(viewModel) }
                    }
                }
                else -> {
                    item {
                        when (getDebateItemState.status) {
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
                                Text(text = "討論の取得に失敗しました。")
                                viewModel.resetGetDebateItemState()
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
fun AnotherUserPageLoadingIndicator(viewModel: AnotherUserPageViewModel) {
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
        viewModel.getAnotherUserPageDebateItems()
        Log.d("CUDUC", "LE")
    }
}

