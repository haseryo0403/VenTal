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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kimsy.rr.vental.ViewModel.AnotherUserPageViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.DebateCard

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun AnotherUserPageView(
    viewModel: AnotherUserPageViewModel,
    sharedDebateViewModel: SharedDebateViewModel,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val anotherUser by viewModel.anotherUser.collectAsState()
    val myPageItems by sharedDebateViewModel.myPageItems.collectAsState()

    val hasFinishedLoadingAllMyPageItems = sharedDebateViewModel.hasFinishedLoadingAllMyPageItems

    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()

    val scrollState = rememberLazyListState()

    val userPageDataState by viewModel.userPageDateState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateAnotherUser()
        viewModel.loadUserPageData()
//        sharedDebateViewModel.getMyPageDebateItems()
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
                    anotherUser?.let { it1 ->
                        AccountContent(
                            userPageData = it,
                            user = it1,
                            toProfileEditView = null
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
                        DebateCard(sharedDebateViewModel, toDebateView, toAnotherUserPageView, item)
                    }
                    if (!hasFinishedLoadingAllMyPageItems) {
                        item { MyPageLoadingIndicator(sharedDebateViewModel) }
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
                                sharedDebateViewModel.resetGetDebateItemState()
                            }
                            else -> {sharedDebateViewModel.resetGetDebateItemState()}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnotherUserPageLoadingIndicator(sharedDebateViewModel: SharedDebateViewModel) {
    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
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
        Log.d("CUDUC", "LE")
    }
}

