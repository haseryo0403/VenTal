package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.MyLikedDebateViewModel
import kimsy.rr.vental.viewModel.SharedDebateViewModel


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLikedDebateView(
    viewModel: MyLikedDebateViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit,
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllLikedDebateItems

    val getDebateItemState by viewModel.loadLikedDebateItemsState.collectAsState()

    val scrollState = rememberLazyListState()

    val myLikedDebateItems by viewModel.likedDebateItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateCurrentUser()
        if (myLikedDebateItems.isEmpty()) {
            viewModel.loadLikedDebates()
        }
        scrollState.scrollToItem(
            viewModel.likedDebateItemSavedScrollIndex,
            viewModel.likedDebateItemSavedScrollOffset
        )
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.setLikedDebateItemScrollState(index, offset)
            }
    }

//    PullToRefreshBox(
//        //TODO fix
//        isRefreshing = isRefreshing,
//        onRefresh = { viewModel.onRefresh() }
//    ) {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // LazyColumn Content
            when {
                myLikedDebateItems.isNotEmpty() -> {
                    itemsIndexed(myLikedDebateItems) {index, item->
                        DebateCard(
                            sharedDebateViewModel,
                            toDebateView,
                            toAnotherUserPageView,
                            onLikeStateSuccess = {
                                    debateItem ->
                                viewModel.onLikeSuccess(debateItem)
                            },
                            item)
                        if ((index + 1) % 7 == 0) {
                            LoadMyLikedDebate(viewModel)
                        }
                    }
                    item { MyLikedDebateLoadingIndicator(viewModel) }
                }
                else -> {
                    item {
                        Log.d("MLDV", "empty: ${getDebateItemState.status}")
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
                                    viewModel.loadLikedDebates()
                                    scrollState.scrollToItem(
                                        viewModel.likedDebateItemSavedScrollIndex,
                                        viewModel.likedDebateItemSavedScrollOffset
                                    )
                                })
                            }
                            Status.SUCCESS -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(20.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = stringResource(id = R.string.no_likeddebate_available))
                                }                            }
                            else -> {}
                        }
                    }
                }
            }
        }
//    }
}

@Composable
fun LoadMyLikedDebate(viewModel: MyLikedDebateViewModel) {
    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllLikedDebateItems
    if (!hasFinishedLoadingAllItems) {
        LaunchedEffect(Unit) {
            viewModel.loadLikedDebates()
            Log.d("CUDUC", "LE")
        }
    }

}
@Composable
fun MyLikedDebateLoadingIndicator(viewModel: MyLikedDebateViewModel) {
    val getDebateItemState by viewModel.loadLikedDebateItemsState.collectAsState()
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
}