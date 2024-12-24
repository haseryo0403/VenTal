package kimsy.rr.vental.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import kimsy.rr.vental.data.Message
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.UserType
import kimsy.rr.vental.ui.CommonComposable.CustomLinearProgressIndicator
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.ui.CommonComposable.MaxLengthTextField
import kimsy.rr.vental.ui.CommonComposable.MessageItem
import kimsy.rr.vental.ui.CommonComposable.formatTimeDifference


@OptIn(ExperimentalLayoutApi::class)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebateView(
    debateViewModel: DebateViewModel = hiltViewModel(),
    sharedDebateViewModel: SharedDebateViewModel
    ){

    val currentUser = sharedDebateViewModel.currentUser

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isKeyboardVisible = WindowInsets.isImeVisible
    var text by remember { mutableStateOf("") }

    val context = LocalContext.current// is this Right?
    val fetchMessageState by debateViewModel.fetchMessageState.collectAsState()
    val currentDebateItem by sharedDebateViewModel.currentDebateItem.collectAsState()

    val likeState by sharedDebateViewModel.likeState.collectAsState()
    val createMessageState by debateViewModel.createMessageState.collectAsState()

    LaunchedEffect(Unit) {
        currentDebateItem?.let { debateViewModel.getMessages(it.debate) }
    }

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

    when (createMessageState.status) {
        Status.LOADING -> CustomLinearProgressIndicator()
        Status.SUCCESS -> {
            imageUri = null
            text = ""
            debateViewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(context, R.string.message_creation_fail, Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // メッセージリスト
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (imageUri == null) 120.dp else 0.dp) // TextField の高さ分の余白を確保
            ) {
                item {
                    if (currentDebateItem != null) {
                        DebateContent(
                            debateItem = currentDebateItem!!,
                            sharedDebateViewModel = sharedDebateViewModel
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "読み込みに失敗しました。通信環境の良いところで再度お試しください。",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_mode_comment_24),
                                contentDescription = "comment"
                            )
                        }
                        Text(text = "16")
                    }

                    Divider()

                    when (fetchMessageState.status) {
                        Status.LOADING -> {
                            showLoadingIndicator()
                        }
                        Status.SUCCESS -> {
                            if (fetchMessageState.data != null) {
                                MessageItem(messages = fetchMessageState.data!!)
                            } else {
                                Text(text = "どうやらメッセージが無いようです。")
                            }
                        }
                        Status.FAILURE -> {
                            Toast.makeText(
                                context,
                                "読み込みに失敗しました。通信環境の良いところで再度お試しください。",
                                Toast.LENGTH_LONG
                            ).show()
                            debateViewModel.resetState()
                            Log.e("DV", "${fetchMessageState.message}")
                        }
                        else -> {}
                    }

                    if (imageUri != null) {
                        debateTextFieldWithImage(
                            imageUri = imageUri!!,
                            onImageDelete = { imageUri = null },
                            text = text,
                            onTextChange = { text = it }
                        ) {
                            currentUser?.let {
                                currentDebateItem?.let {
                                    debateViewModel.createMessage(
                                        debate = it.debate,
                                        text = text,
                                        imageUri = imageUri,
                                        context = context
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // テキストフィールド（固定位置）
            if (imageUri == null) {
                debateTextFieldWithOutImage(
                    isKeyboardVisible = isKeyboardVisible,
                    context = context,
                    onImageSelected = { imageUri = it },
                    text = text,
                    onTextChange = { text = it }
                ) {
                    currentDebateItem?.let {
                        debateViewModel.createMessage(
                            debate = it.debate,
                            text = text,
                            imageUri = null,
                            context = context
                        )
                    }
                }
            }
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
            Divider()
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

//TODO Delete 別ファイルにうつした。複雑になりそうなので
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

// TODO　参加者以外非表示
@Composable
fun debateTextFieldWithImage(
    imageUri: Uri,
    onImageDelete: () -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Divider()
        MaxLengthTextField(
            value = text,
            onValueChange = onTextChange,
            maxLength = 140,
            modifier = Modifier
                .fillMaxWidth(),
            placeHolder = { Text(text = "怒りをぶつけろ！") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent, // 背景を透明に設定
                focusedContainerColor = Color.Transparent, // フォーカス時の背景を透明に設定
                unfocusedIndicatorColor = Color.Transparent, // 未選択時の下線を透明に設定
                focusedIndicatorColor = Color.Transparent // 選択時の下線を透明に設定
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ){
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth,
            )
            androidx.compose.material3.IconButton(
                onClick = { onImageDelete() },
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (8).dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_clear_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(32.dp),

                    )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedButton(
                onClick = onSendClick ,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)) {
                Text(text = "送信する")
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun debateTextFieldWithOutImage(
    isKeyboardVisible: Boolean,
    context: Context,
    onImageSelected: (Uri?) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize(), // 親レイアウトを画面全体に拡張
        contentAlignment = Alignment.BottomCenter
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
//                    .wrapContentHeight()
                    .heightIn(max = if (isKeyboardVisible) 140.dp else 56.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MaxLengthOutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    maxLength = 140,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),

                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImagePermissionAndSelection(
                    context = context,
                    onImageSelected = onImageSelected
                )
                IconButton(onClick = onSendClick) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

