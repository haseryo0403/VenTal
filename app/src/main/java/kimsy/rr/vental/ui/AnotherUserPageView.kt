//package kimsy.rr.vental.ui
//
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresExtension
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.Divider
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.snapshotFlow
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import coil3.compose.rememberAsyncImagePainter
//import kimsy.rr.vental.ViewModel.MyPageViewModel
//import kimsy.rr.vental.ViewModel.SharedDebateViewModel
//import kimsy.rr.vental.data.Status
//import kimsy.rr.vental.data.UserPageData
//import kimsy.rr.vental.ui.CommonComposable.DebateCard
//
//@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//@Composable
//fun anotherUserPageView(
//    viewModel: MyPageViewModel,
//    sharedDebateViewModel: SharedDebateViewModel,
//    toDebateView: () -> Unit,
//    toProfileEditView: () -> Unit
//){
//    val myPageItems by sharedDebateViewModel.myPageItems.collectAsState()
//
//    val hasFinishedLoadingAllMyPageItems = sharedDebateViewModel.hasFinishedLoadingAllMyPageItems
//
//    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
//
//    val scrollState = rememberLazyListState()
//
//    val userPageDataState by viewModel.userPageDateState.collectAsState()
//
//    LaunchedEffect(Unit) {
//        viewModel.updateCurrentUser()
//        viewModel.loadUserPageData()
//        sharedDebateViewModel.getMyPageDebateItems()
//        scrollState.scrollToItem(
//            viewModel.savedScrollIndex,
//            viewModel.savedScrollOffset
//        )
//    }
//
//    // スクロール位置を保存
//    LaunchedEffect(scrollState) {
//        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
//            .collect { (index, offset) ->
//                viewModel.setScrollState(index, offset)
//            }
//    }
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(color = MaterialTheme.colorScheme.background)
//    ) {
//        when (userPageDataState.status) {
//            Status.LOADING -> {
//                Log.d("MPV", "upds loading")
//                //TODO Loading
//            }
//            Status.SUCCESS -> {
//                Log.d("MPV", "upds sccess")
//                userPageDataState.data?.let {
//                    AccountContent(
//                        userPageData = it,
//                        viewModel = viewModel,
//                        toProfileEditView = toProfileEditView
//                    )
//                }
//                Divider()
//            }
//            Status.FAILURE -> {
//                Log.d("MPV", "upds failure")
//                //TODO error-handling
//            }
//            else -> {}
//        }
//
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(color = MaterialTheme.colorScheme.background),
//            state = scrollState
//        ){
//            item {
//
//            }
//
//            when {
//                myPageItems.isNotEmpty() -> {
//                    items(myPageItems) {item->
//                        DebateCard(sharedDebateViewModel, toDebateView, item)
//                    }
//                    if (!hasFinishedLoadingAllMyPageItems) {
//                        item { MyPageLoadingIndicator(sharedDebateViewModel) }
//                    }
//                }
//                else -> {
//                    item {
//                        when (getDebateItemState.status){
//                            Status.LOADING -> {
//                                Box(
//                                    modifier = Modifier.fillMaxSize()
//                                ) {
//                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                                }
//                            }
//                            Status.FAILURE -> {
//                                Text(text = "討論の取得に失敗しまいた。")
//                                sharedDebateViewModel.resetGetDebateItemState()
//                            }
//                            else -> {sharedDebateViewModel.resetGetDebateItemState()}
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AccountContent(
//    userPageData: UserPageData,
//    viewModel: MyPageViewModel,
//    toProfileEditView: () -> Unit
//){
//    val currentUser by viewModel.currentUser.collectAsState()
//    val debatesCount = userPageData.debatesCount
//    val followerCount = userPageData.followerCount
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp),
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//
//                Log.d("TAG", "Image URL: ${currentUser?.photoURL}")
//                Image(
//                    painter = rememberAsyncImagePainter(currentUser?.photoURL),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(56.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//                currentUser?.name?.let { Text(text = it) }
//
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Text(text = debatesCount.toString())
//                    Text(text = "討論")
//                }
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Text(text = followerCount.toString())
//                    Text(text = "フォロワー")
//                }
//            }
//            Button(onClick = { toProfileEditView() },
//
//                ) {
//                Text(text = "プロフィールを編集")
//            }
//        }
//        Divider()
//    }
//}
//
//@Composable
//fun MyPageLoadingIndicator(sharedDebateViewModel: SharedDebateViewModel) {
//    val getDebateItemState by sharedDebateViewModel.getDebateItemsState.collectAsState()
//    Box(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        when (getDebateItemState.status){
//            Status.LOADING -> {
//                Box(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                }
//            }
//            Status.FAILURE -> Text(text = "討論の追加の取得に失敗しまいた。")
//            else -> {}
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        // 要素の追加読み込み
//        sharedDebateViewModel.getMyPageDebateItems()
//        Log.d("CUDUC", "LE")
//    }
//}
//
