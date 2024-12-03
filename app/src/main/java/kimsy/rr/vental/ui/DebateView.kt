package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.DebateViewModel
import kimsy.rr.vental.ViewModel.VentCardsViewModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference
import java.net.URL
import java.sql.Timestamp
import java.util.Date


@Composable
fun DebateView(
    debateViewModel: DebateViewModel = hiltViewModel(),
    ){

    val debateWithUsers = debateViewModel.debateWithUsers.value
    val ventCard = debateViewModel.ventCard.value
    val isLoading by debateViewModel.isLoading
    val errorMessage by debateViewModel.errorState.observeAsState()

    LaunchedEffect(Unit) {
        debateViewModel.loadDebate()
    }

    when {
        isLoading-> LoadingView()
        errorMessage != null -> ErrorDialog(errorMessage) { debateViewModel.clearErrorState() }
        debateWithUsers != null && ventCard != null -> DebateContent(debateWithUsers, ventCard)

    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorDialog(errorMessage: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { /* TODO: 実装 */ },
        title = { Text(text = "エラー") },
        text = { Text(text = errorMessage ?: "不明なエラーが発生しました") }
    )
}

@Composable
fun DebateContent(debateWithUsers: DebateWithUsers, ventCard: VentCard) {
    val heartIcon = painterResource(id = R.drawable.baseline_favorite_24)
    Log.d("DV", "$debateWithUsers, $ventCard")
    LazyColumn(

    ) {
        item {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    AccountIcon(imageUrl = debateWithUsers.posterImageURL)

                    Column(
                        modifier = Modifier
                            .weight(5f)
                            .fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = debateWithUsers.posterName)
                            Text(text = debateWithUsers.debateCreatedDatetime?.let { formatTimeDifference(it) }?: "日付不明")
                        }

                        Text(text = ventCard.swipeCardContent)
                        ventCard.tags.forEach { tag->
                            Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        }
                        Image(
                            painter = rememberAsyncImagePainter(debateWithUsers.swipeCardImageURL),
                            contentDescription = "ventCardImage",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(2f)
                    ) {
                        AccountIcon(imageUrl = debateWithUsers.debaterImageURL)

                        Text(text = debateWithUsers.debaterName)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Icon(painter = heartIcon,
                            contentDescription = "haert")
                        Text(text = debateWithUsers.debaterLikeCount.toString())
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Icon(painter = heartIcon,
                            contentDescription = "haert")
                        Text(text = debateWithUsers.posterLikeCount.toString())
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(2f)
                    ) {
                        AccountIcon(imageUrl = debateWithUsers.posterImageURL)

                        Text(text = debateWithUsers.posterName)
                    }
                }

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End

                ){
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.outline_mode_comment_24), contentDescription = "comment")
                    }
                    Text(text = "16")
                }

                Divider()




                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Text(text = "2024/07/29 12:00")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 250.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "基礎みたいなもんだろ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountIcon(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessageItem(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
//        horizontalAlignment = if (message.isSentByCurrentUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
//                .background(
//                    if (message.isSentByCurrentUser) colorResource(id = R.color.purple_700) else Color.Gray,
//                    shape = RoundedCornerShape(8.dp)
//                )
                .padding(8.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                style = TextStyle(fontSize = 16.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = message.senderFirstName,
//            style = TextStyle(
//                fontSize = 12.sp,
//                color = Color.Gray
//            )
//        )
//        Text(
//            text = formatTimestamp(message.timestamp), // Replace with actual timestamp logic
//            style = TextStyle(
//                fontSize = 12.sp,
//                color = Color.Gray
//            )
//        )
    }
}
//
@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun DebatePrev(){
    DebateContent(
        debateWithUsers = DebateWithUsers(
            debateId = "n9Ztc16AYFBbDoN7zNWR",
            swipeCardImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F03a046fd-8db3-4f9d-aa2e-b3b7fffb283a_IMG_20240807_003726.jpg?alt=media&token=aa622168-f205-495f-94a7-c4bcd7015f5d",
            swipeCardId = "RknZdyxaOk6x1p85LSAK",
            posterId = "ILJTOzQXkCPMY1UBACO0b0O4pIz2",
            posterName = "hasegawa",
            posterImageURL = "https://lh3.googleusercontent.com/a/ACg8ocJKcXHgamc1Bo5pD6d36LlCssxdHNPrS2ys3l8bVZai9TqZS_U=s96-c",
            posterLikeCount = 0,
            debaterId = "Xv2IvOYtkMe4m7TEOGGEgu9IkaE3",
            debaterName = "Teacher Haku",
            debaterImageURL = "https://lh3.googleusercontent.com/a/ACg8ocLlS-gC1-5j54LZ9Q45b9PNX97ocT_JNzIMy4Rhop8W_uBFGwI=s96-c",
            debaterLikeCount = 0,
            firstMessage = "これて",
            firstMessageImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F12bb1dd7-5ee7-4e36-9c3b-16540b52373f_DSC_0002.JPG?alt=media&token=75c24714-3642-4bc8-9a21-36316fe184cb",
            debateReportFlag = false,
            debateDeletionRequestFlag = false,
            debateCreatedDatetime = Date(1731385711000)
        ),

     ventCard = VentCard(
        posterId = "ILJTOzQXkCPMY1UBACO0b0O4pIz2",
        swipeCardContent = "これかひとつめえこ",
        swipeCardImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F03a046fd-8db3-4f9d-aa2e-b3b7fffb283a_IMG_20240807_003726.jpg?alt=media&token=aa622168-f205-495f-94a7-c4bcd7015f5d",
        likeCount = 6,
        tags = listOf("31"),
        swipeCardReportFlag = false,
        swipeCardDeletionRequestFlag = false,
        debateCount = 1,
        swipeCardCreatedDateTime = com.google.firebase.Timestamp(
            seconds = 1731385840,
            nanoseconds = 239000000
        )
    )
    )
}

