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
import kimsy.rr.vental.viewModel.MyDebateViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard
import kimsy.rr.vental.ui.commonUi.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun MyDebateView(
    viewModel: MyDebateViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val myPageItems by viewModel.myPageItems.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllDebateItems

    val getDebateItemState by viewModel.getDebateItemsState.collectAsState()

    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.updateCurrentUser()
        viewModel.getMyPageDebateItems()
        scrollState.scrollToItem(
            viewModel.debateItemSavedScrollIndex,
            viewModel.debateItemSavedScrollOffset
        )
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.setDebateItemScrollState(index, offset)
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() }
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                                ErrorView(retry = {
                                    viewModel.updateCurrentUser()
                                    viewModel.getMyPageDebateItems()
                                    scrollState.scrollToItem(
                                        viewModel.debateItemSavedScrollIndex,
                                        viewModel.debateItemSavedScrollOffset
                                    )
                                })
//                                Text(text = "討論の取得に失敗しました。")
//                                viewModel.resetGetDebateItemState()
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
fun MyPageLoadingIndicator(viewModel: MyDebateViewModel) {
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
        viewModel.getMyPageDebateItems()
        Log.d("CUDUC", "LE")
    }
}