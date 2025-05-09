//package kimsy.rr.vental.ui.CommonComposable

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kimsy.rr.vental.data.Debate
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.User
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.ui.AccountIcon
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.ui.showLoadingIndicator
import kimsy.rr.vental.viewModel.DebateViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageView(
    viewModel: DebateViewModel,
    debate: Debate,
    poster: User,
    debater: User,
    toAnotherUserPageView: (user: User) -> Unit
) {

    val fetchMessageState by viewModel.fetchMessageState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.getMessages(debate)
        viewModel.observeFollowingUserIds()
    }

    when (fetchMessageState.status) {
        Status.LOADING -> {
            showLoadingIndicator()
        }
        Status.SUCCESS -> {
            if (fetchMessageState.data != null) {
                fetchMessageState.data?.let {
                    MessageItems(
                        messages = it,
                        poster = poster,
                        debater = debater,
                        toAnotherUserPageView = toAnotherUserPageView
                        )
                }
            }
        }
        Status.FAILURE -> {
            ErrorView(retry = {
                viewModel.getMessages(debate)
                viewModel.observeFollowingUserIds()
            })
        }
        else -> {}
    }


}

@Composable
fun MessageItems(
    messages: List<Message>,
    poster: User,
    debater: User,
    toAnotherUserPageView: (user: User) -> Unit
) {
    var previousDate: Date? by remember { mutableStateOf(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(8.dp)
    ) {

        messages.forEachIndexed { index, message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 最初のメッセージの場合に日付を表示
                if (index == 0 || !isSameDay(previousDate, message.sentDatetime)) {
                    DateDisplay(message.sentDatetime)
                }
            }
            if (message.userType == UserType.POSTER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start // 左寄せ
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f), // 80%の幅
                        contentAlignment = Alignment.CenterStart
                    ) {
                        PosterMessage(
                            message = message,
                            poster = poster,
                            toAnotherUserPageView = toAnotherUserPageView
                            )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // 右寄せ
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f), // 80%の幅
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        DebaterMessage(
                            message = message,
                            debater = debater,
                            toAnotherUserPageView = toAnotherUserPageView
                            )
                    }
                }
            }

            previousDate = message.sentDatetime
        }

    }
}


@Composable
fun PosterMessage(
    message: Message,
    poster: User,
    toAnotherUserPageView: (user: User) -> Unit
){
    if (message.imageURL != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {

            IconButton(onClick = { toAnotherUserPageView(poster) }) {
                AccountIcon(imageUrl = poster.photoURL)
            }

            Image(
                painter = rememberAsyncImagePainter(message.imageURL),
                contentDescription = "message Image",
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .weight(1f),
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = message.sentDatetime?.let {
                    formatTime(it)
                } ?: "不明",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (message.text.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = { toAnotherUserPageView(poster) }) {
                AccountIcon(imageUrl = poster.photoURL)
            }
            Surface(
                modifier = Modifier
                    .padding(4.dp)
//                    .widthIn(max = 250.dp)
                    .weight(1f), // メッセージ部分を可変に                ,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Text(
                text = message.sentDatetime?.let {
                    formatTime(it)
                } ?: "不明",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DebaterMessage(
    message: Message,
    debater: User,
    toAnotherUserPageView: (user: User) -> Unit
){
    //TODO 右と左で時間とアイコンを表示する場所を左右変えなければならない
    if (message.imageURL != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = message.sentDatetime?.let {
                    formatTime(it)
                } ?: "不明",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Image(
                painter = rememberAsyncImagePainter(message.imageURL),
                contentDescription = "message Image",
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .weight(1f),
                contentScale = ContentScale.FillWidth
            )
            IconButton(onClick = { toAnotherUserPageView(debater) }) {
                AccountIcon(imageUrl = debater.photoURL)
            }
        }
    }
    if (message.text.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = message.sentDatetime?.let {
                    formatTime(it)
                } ?: "不明",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                modifier = Modifier
                    .padding(4.dp)
//                    .widthIn(max = 250.dp)
                    .weight(1f), // メッセージ部分を可変に
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            IconButton(onClick = { toAnotherUserPageView(debater) }) {
                AccountIcon(imageUrl = debater.photoURL)
            }
        }
    }

}

fun isSameDay(date1: Date?, date2: Date?): Boolean {
    if (date1 == null || date2 == null) return false
    val calendar1 = Calendar.getInstance().apply { time = date1 }
    val calendar2 = Calendar.getInstance().apply { time = date2 }
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun DateDisplay(date: Date?) {
    val formattedDate = date?.let { formatDate(it) } ?: "日付不明"

    Surface(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .wrapContentWidth()
            .padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(8.dp),
            maxLines = 1
        )
    }
}

fun formatDate(date: Date): String {

    val calendarNow = Calendar.getInstance()
    val calendarCreated = Calendar.getInstance().apply { time = date }

    return when {

        // 同日の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) == calendarCreated.get(Calendar.DAY_OF_YEAR) -> "今日"

        // 昨日の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) &&
                calendarNow.get(Calendar.DAY_OF_YEAR) - calendarCreated.get(Calendar.DAY_OF_YEAR) == 1 -> "昨日"

        // 同年の日付
        calendarNow.get(Calendar.YEAR) == calendarCreated.get(Calendar.YEAR) -> {
            SimpleDateFormat("MM/dd", java.util.Locale.getDefault()).format(date)
        }

        else -> {
            SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(date)
        }
    }
}

fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}