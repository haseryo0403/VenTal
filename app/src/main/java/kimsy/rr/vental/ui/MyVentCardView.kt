package kimsy.rr.vental.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.viewModel.MyVentCardViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import kimsy.rr.vental.ui.CommonComposable.showAsBottomSheet
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.ui.commonUi.VentCardBottomSheet

@Composable
fun MyVentCardView(
    viewModel: MyVentCardViewModel,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val currentUser by viewModel.currentUser.collectAsState()
    val profileURL = currentUser?.photoURL
    val currentUserName = currentUser?.name?: "unknown"

    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllVentCards

    val loadItemState by viewModel.loadVentCardState.collectAsState()

    val scrollState = rememberLazyListState()

    val myVentCards by viewModel.ventCards.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateCurrentUser()
        viewModel.loadMyVentCards()
        scrollState.scrollToItem(
            viewModel.ventCardSavedScrollIndex,
            viewModel.ventCardSavedScrollOffset
        )
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.setVentCardScrollState(index, offset)
            }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            myVentCards.isNotEmpty() -> {
                items(myVentCards) {item->

                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            Image(
                                painter = rememberAsyncImagePainter(profileURL),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Top
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = currentUserName)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically // 中央揃え
                                    ) {
                                        Text(
                                            text = item.swipeCardCreatedDateTime?.let {
                                                formatTimeDifference(it)
                                            } ?: "日付不明"
                                        )
                                        IconButton(onClick = {
                                            activity.showAsBottomSheet { hideModal ->
                                                currentUser?.let {
                                                    VentCardBottomSheet(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        ventCard = item,
                                                        currentUserId = it.uid,
                                                        toReportVentCardView = toReportVentCardView,
                                                        toRequestVentCardDeletionView = toRequestVentCardDeletionView,
                                                        hideModal = hideModal
                                                    )
                                                }
                                            }

                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                                contentDescription = "option"
                                            )
                                        }
                                        IconButton(
                                            onClick = { /*TODO*/ },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                                contentDescription = "HORIZONTAL ELLIPSIS",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .padding(top = 8.dp)
                                            )
                                        }
                                    }

                                }
                                Text(text = item.swipeCardContent)
                                item.tags.forEach {tag ->
                                    Text(
                                        text = tag,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Image(painter = rememberAsyncImagePainter(item.swipeCardImageURL),
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                                        contentDescription = "haert")
                                    Text(text = item.likeCount.toString())
                                }
                            }
                        }
                        Divider()
                    }


                }
                if (!hasFinishedLoadingAllItems) {
                    item { MyVentCardLoadingIndicator(viewModel = viewModel) }
                }
            }
            else -> {
                item {
                    when (loadItemState.status) {
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
                                viewModel.loadMyVentCards()
                                scrollState.scrollToItem(
                                    viewModel.ventCardSavedScrollIndex,
                                    viewModel.ventCardSavedScrollOffset
                                )
                            })
                        }
                        else -> viewModel.resetLoadVentCardState()
                    }
                }
            }
        }
    }
}

@Composable
fun MyVentCardLoadingIndicator(viewModel: MyVentCardViewModel) {
    val loadVentCardState by viewModel.loadVentCardState.collectAsState()
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (loadVentCardState.status){
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
        viewModel.loadMyVentCards()
        Log.d("CUDUC", "LE")
    }
}
