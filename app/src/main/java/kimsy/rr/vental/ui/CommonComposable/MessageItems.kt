//package kimsy.rr.vental.ui.CommonComposable

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.ui.showLoadingIndicator
import kimsy.rr.vental.viewModel.DebateViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageItem(
    viewModel: DebateViewModel,
    debate: Debate
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
                    MessageView(messages = it)
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
fun MessageView(messages: List<Message>) {
    var previousDate: Date? by remember { mutableStateOf(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
            if (message.imageURL != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = if (message.userType == UserType.DEBATER) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(message.imageURL),
                        contentDescription = "message Image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .widthIn(max = 250.dp),
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
                        .padding(4.dp),
                    horizontalArrangement = if (message.userType == UserType.DEBATER) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyLarge,
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



            previousDate = message.sentDatetime
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