package kimsy.rr.vental.ui

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.DebateViewModel
import kimsy.rr.vental.ViewModel.SharedDebateViewModel
import kimsy.rr.vental.data.DebateItem
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.data.VentCard
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DebateView(
    debateViewModel: DebateViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel
    ){

    val context = LocalContext.current// is this Right?
    val fetchMessageState by debateViewModel.fetchMessageState.collectAsState()
    val currentDebateItem by sharedDebateViewModel.currentDebateItem.collectAsState()

    val likeState by sharedDebateViewModel.likeState.collectAsState()

    when (likeState[currentDebateItem]?.status) {
        Status.SUCCESS -> {
            //TODO もし必要なUIの処理があれば。
        }
        Status.FAILURE -> {
            sharedDebateViewModel.showLikeFailedToast(LocalContext.current)
            currentDebateItem?.let { sharedDebateViewModel.resetLikeState(it) }
        }
        else -> {}
    }


    LaunchedEffect(Unit) {
        currentDebateItem?.let { debateViewModel.getMessages(it.debate) }
    }

    LazyColumn(

    ){
        item {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {

                if (currentDebateItem != null) {
                    DebateContent(debateItem = currentDebateItem!!, sharedDebateViewModel = sharedDebateViewModel)
                } else {
                    Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
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

                when(fetchMessageState.status) {
                    Status.LOADING -> { showLoadingIndicator() }
                    Status.SUCCESS -> {
                        if (fetchMessageState.data != null) {
                            ChatMessageItem(messages = fetchMessageState.data!!)
                        } else {
                            Text(text = "どうやらメッセージが無いようです。")
                        }
                    }
                    Status.FAILURE -> {
                        Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                        debateViewModel.resetState()
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun DebateContents(debateWithUsers: DebateWithUsers, ventCard: VentCard) {
    val heartIcon = painterResource(id = R.drawable.baseline_favorite_24)
    Log.d("DV", "$debateWithUsers, $ventCard")
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
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun DebateContent(
    debateItem: DebateItem,
    sharedDebateViewModel: SharedDebateViewModel
                  ) {
    val debate = debateItem.debate
    val ventCard = debateItem.ventCard
    val debater = debateItem.debater
    val poster = debateItem.poster
    val heartIcon = painterResource(id = R.drawable.baseline_favorite_24)
    Log.d("DV", "$debateItem, $ventCard")
    Row(
        modifier = Modifier.fillMaxWidth()
    ){
        AccountIcon(imageUrl = poster.photoURL)

        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = poster.name)
                Text(
                    text = ventCard.swipeCardCreatedDateTime?.let {
                        formatTimeDifference(it)
                    } ?: "日付不明",
                )
            }

            Text(text = ventCard.swipeCardContent)
            ventCard.tags.forEach { tag->
                Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)

            }
            Image(
                painter = rememberAsyncImagePainter(ventCard.swipeCardImageURL),
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
            AccountIcon(imageUrl = debater.photoURL)

            Text(text = debater.name)
        }
        // ひだりいいね debater
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(onClick = {
//                debateViewModel.handleLikeAction(debateItem, UserType.DEBATER)
                sharedDebateViewModel.handleLikeAction(debateItem, UserType.DEBATER)

            }) {
                Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                    contentDescription = "heart",
                    tint = if (debateItem.likedUserType == UserType.DEBATER) Color.Red else Color.Gray
                )
            }
            Text(text = debate.debaterLikeCount.toString())
        }
        // みぎいいね poster
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(onClick = {
//                debateViewModel.handleLikeAction(debateItem, UserType.POSTER)
                sharedDebateViewModel.handleLikeAction(debateItem, UserType.POSTER)
            }) {
                Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                    contentDescription = "heart",
                    tint = if (debateItem.likedUserType == UserType.POSTER) Color.Red else Color.Gray

                )
            }
            Text(text = debate.posterLikeCount.toString())
        }



        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(4.dp)
                .weight(2f)
        ) {
            AccountIcon(imageUrl = poster.photoURL)

            Text(text = poster.name)
        }
    }
}

@Composable
fun AccountIcon(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessageItem(messages: List<Message>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        messages.forEach { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = if (message.userType == UserType.DEBATER) Arrangement.Start else Arrangement.End
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
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Text(
                text = message.sentDatetime?.let {
                    formatTimeDifference(it)
                } ?: "日付不明",
            )
        }
    }
}

@Composable
fun showLoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxHeight(0.4f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun DebatePrev(){
//    DebateContent(
//        debateWithUsers = DebateWithUsers(
//            debateId = "n9Ztc16AYFBbDoN7zNWR",
//            swipeCardImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F03a046fd-8db3-4f9d-aa2e-b3b7fffb283a_IMG_20240807_003726.jpg?alt=media&token=aa622168-f205-495f-94a7-c4bcd7015f5d",
//            swipeCardId = "RknZdyxaOk6x1p85LSAK",
//            posterId = "ILJTOzQXkCPMY1UBACO0b0O4pIz2",
//            posterName = "hasegawa",
//            posterImageURL = "https://lh3.googleusercontent.com/a/ACg8ocJKcXHgamc1Bo5pD6d36LlCssxdHNPrS2ys3l8bVZai9TqZS_U=s96-c",
//            posterLikeCount = 0,
//            debaterId = "Xv2IvOYtkMe4m7TEOGGEgu9IkaE3",
//            debaterName = "Teacher Haku",
//            debaterImageURL = "https://lh3.googleusercontent.com/a/ACg8ocLlS-gC1-5j54LZ9Q45b9PNX97ocT_JNzIMy4Rhop8W_uBFGwI=s96-c",
//            debaterLikeCount = 0,
//            firstMessage = "これて",
//            firstMessageImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F12bb1dd7-5ee7-4e36-9c3b-16540b52373f_DSC_0002.JPG?alt=media&token=75c24714-3642-4bc8-9a21-36316fe184cb",
//            debateReportFlag = false,
//            debateDeletionRequestFlag = false,
//            debateCreatedDatetime = Date(1731385711000)
//        ),
//
//     ventCard = VentCard(
//        posterId = "ILJTOzQXkCPMY1UBACO0b0O4pIz2",
//        swipeCardContent = "これかひとつめえこ",
//        swipeCardImageURL = "https://firebasestorage.googleapis.com/v0/b/vental-4eb3c.firebasestorage.app/o/images%2F03a046fd-8db3-4f9d-aa2e-b3b7fffb283a_IMG_20240807_003726.jpg?alt=media&token=aa622168-f205-495f-94a7-c4bcd7015f5d",
//        likeCount = 6,
//        tags = listOf("31"),
//        swipeCardReportFlag = false,
//        swipeCardDeletionRequestFlag = false,
//        debateCount = 1,
//        swipeCardCreatedDateTime = com.google.firebase.Timestamp(
//            seconds = 1731385840,
//            nanoseconds = 239000000
//        )
//    )
//    )
//}

