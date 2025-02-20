package kimsy.rr.vental.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.ExpandableTagRow
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import kimsy.rr.vental.ui.CommonComposable.showAsBottomSheet
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.ui.commonUi.VentCardBottomSheet
import kimsy.rr.vental.viewModel.MyVentCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SuspiciousIndentation")
@Composable
fun MyVentCardView(
    viewModel: MyVentCardViewModel,
    toReportVentCardView: () -> Unit,
    toRequestVentCardDeletionView: () -> Unit
) {
    val activity = LocalContext.current as Activity
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val profileURL = currentUser.photoURL
    val currentUserName = currentUser.name

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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onRefresh() }
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                myVentCards.isNotEmpty() -> {
                    itemsIndexed(myVentCards) {index, ventCard->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable { /* Handle Click Action */ }
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Column(

                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {

                                    Image(
                                        painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
                                        contentDescription = "Image",
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.FillWidth
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Row{
                                            ExpandableTagRow(tags = ventCard.tags)

                                            androidx.compose.material.IconButton(onClick = {
                                                activity.showAsBottomSheet { hideModal ->
                                                    VentCardBottomSheet(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        ventCard = ventCard,
                                                        currentUserId = currentUser.uid,
                                                        toReportVentCardView = toReportVentCardView,
                                                        toRequestVentCardDeletionView = toRequestVentCardDeletionView,
                                                        hideModal = hideModal
                                                    )
                                                }
                                            }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_more_vert_24),
                                                    contentDescription = "option",
                                                    modifier = Modifier
                                                        .background(
                                                            Color.White.copy(alpha = 0.4f),
                                                            shape = CircleShape
                                                        )

                                                )
                                            }
                                        }

                                    }

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp),
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(profileURL),
                                            contentDescription = "AccountIcon",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column {
                                            val textColor = if (ventCard.swipeCardImageURL.isEmpty()) {
                                                MaterialTheme.colorScheme.onBackground// 画像がない場合のテキスト色（例: グレー）
                                            } else {
                                                Color.White // 画像がある場合のテキスト色（白）
                                            }

                                            val shadow = if (ventCard.swipeCardImageURL.isEmpty()) {
                                                null // 画像がない場合はシャドウなし
                                            } else {
                                                Shadow(
                                                    color = Color.Black.copy(alpha = 0.6f), // シャドウの色を設定
                                                    offset = Offset(1f, 1f), // シャドウの位置を調整
                                                    blurRadius = 4f // シャドウのぼかし具合
                                                )
                                            }

                                            Text(
                                                text = currentUserName,
                                                color = textColor,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    shadow = shadow // 影の有無を条件に応じて設定
                                                )
                                            )

                                            val formattedDate = ventCard.swipeCardCreatedDateTime?.let {
                                                formatTimeDifference(it)
                                            } ?: "日付不明"
                                            androidx.compose.material.Text(
                                                text = formattedDate,
                                                color = textColor,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    shadow = shadow // 影の有無を条件に応じて設定
                                                )
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(text = ventCard.swipeCardContent)
                                }

                            }
                        }
                        if ((index + 1) % 7 == 0)
                            LoadMyVentCard(viewModel)
                    }
                    item { MyVentCardLoadingIndicator(viewModel = viewModel) }
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
                            Status.SUCCESS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = stringResource(id = R.string.no_mycard_available))
                                    Text(text = stringResource(id = R.string.if_want_make_card))
                                    Image(painter = painterResource(id = R.drawable._vs1sns0xff9725290xff7f1d58__1_), contentDescription = "how to make card",modifier = Modifier.fillMaxWidth())
                                }                        }
                            else -> viewModel.resetLoadVentCardState()
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun LoadMyVentCard(viewModel: MyVentCardViewModel) {
    val hasFinishedLoadingAllItems = viewModel.hasFinishedLoadingAllVentCards
    if (!hasFinishedLoadingAllItems) {
        LaunchedEffect(Unit) {
            viewModel.loadMyVentCards()
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
}
