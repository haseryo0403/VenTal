package kimsy.rr.vental.ui

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentSnapshot
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.NotificationsViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.data.NotificationItem
import kimsy.rr.vental.data.NotificationType
import kimsy.rr.vental.data.Resource
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator

@Composable
fun NotificationsView(
    sharedDebateViewModel: SharedDebateViewModel,
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
){
    val hasFinishedAllItems = notificationsViewModel.hasFinishedLoadingAllItems
    val loadNotificationDataState by notificationsViewModel.loadNotificationDataState.collectAsState()
    val notificationItems by notificationsViewModel.notificationItems.collectAsState()
    val generateDebateItemState by sharedDebateViewModel.generateDebateItemState.collectAsState()

    LaunchedEffect(Unit) {
        //戻るボタンで実行されてしまうと同じ内容をどんどんストックしてしまうため
        if (notificationItems.isEmpty()) {
            notificationsViewModel.loadNotificationItems()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ){
        when {
            generateDebateItemState.status == Status.LOADING -> {
                item {
                    CustomCircularProgressIndicator()
                }
            }
            generateDebateItemState.status == Status.SUCCESS -> {
                toDebateView()
                sharedDebateViewModel.resetGenerateDebateItemState()
            }
            //TODO error handling
            notificationItems.isNotEmpty() -> {
                items(notificationItems) {item ->
                    notificationRow(
                        notificationItem = item,
                        toDebateView = {

                            sharedDebateViewModel.generateAndSetDebateItemByDebateId(item.notification.targetItemId)
                                       },
                        toAnotherUserPageView = toAnotherUserPageView
                        )
                }
                if (!hasFinishedAllItems) {
                    item {
                        NotificationLoadingIndicator(loadNotificationDataState, notificationsViewModel)
                    }
                }
            }
            else -> {
                item {
                    when (loadNotificationDataState.status) {
                        Status.LOADING -> {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        Status.FAILURE -> {
                            //TODO error handling
                            notificationsViewModel.resetState()
                        }
                        Status.SUCCESS -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = stringResource(id = R.string.no_notifications))
                            }
                        }
                        else -> notificationsViewModel.resetState()
                    }
                }
            }
        }
    }
}

@Composable
fun notificationRow(
    notificationItem: NotificationItem,
    toDebateView: () -> Unit,
    toAnotherUserPageView: (user: User) -> Unit
    ) {
    Log.d("NV", "notification row called")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
                toDebateView()
            },
    ) {
        Image(
            painter = rememberAsyncImagePainter(notificationItem.user.photoURL),
            contentDescription = "AccountIcon",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    toAnotherUserPageView(notificationItem.user)
                },
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(5f)
        ) {
            val action = when(notificationItem.notification.type) {
                NotificationType.DEBATESTART -> stringResource(id = R.string.debate_started)
                NotificationType.DEBATEMESSAGE -> stringResource(id = R.string.recieved_message)
                NotificationType.DEBATECOMMENT -> stringResource(id = R.string.recieved_comment)
            }
            Text(text = notificationItem.user.name + action)
        }
    }
    Divider()
}


@Composable
fun NotificationLoadingIndicator(
    loadNotificationDataState: Resource<Pair<List<NotificationItem>, DocumentSnapshot?>>,
    notificationsViewModel: NotificationsViewModel
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (loadNotificationDataState.status){
            Status.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Status.FAILURE -> Text(text = stringResource(id = R.string.load_notification_failed))
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // 要素の追加読み込み
        notificationsViewModel.loadNotificationItems()
        Log.d("CUDUC", "LE")
    }
}



//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun NotificaitonsPrev(){
//    NotificationsView()
//}