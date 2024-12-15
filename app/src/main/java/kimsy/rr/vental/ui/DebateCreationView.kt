package kimsy.rr.vental.ui

import android.content.ClipData.Item
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.AuthViewModel
import kimsy.rr.vental.ViewModel.DebateCreationViewModel
//import kimsy.rr.vental.ViewModel.DebateViewModel
import kimsy.rr.vental.data.DebateWithUsers
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.ui.CommonComposable.MaxLengthTextField

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebateCreationView(
    context: Context,
    authViewModel: AuthViewModel,
    debateCreationViewModel: DebateCreationViewModel,
    toDebateView: () -> Unit
){
    val user by authViewModel.currentUser.observeAsState()
    val fetchRelatedDebateState by debateCreationViewModel.fetchRelatedDebateState.collectAsState()
    val debateCreationState by debateCreationViewModel.debateCreationState.collectAsState()
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isKeyboardVisible = WindowInsets.isImeVisible

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        when {

            fetchRelatedDebateState.status == Status.FAILURE-> {
                Toast.makeText(context, "読み込みに失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                debateCreationViewModel.resetFetchRelatedDebateState()
            }

            debateCreationState.status == Status.FAILURE -> {
                Toast.makeText(context, "登録に失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                debateCreationViewModel.resetDebateCreationState()
            }

            fetchRelatedDebateState.status == Status.LOADING || debateCreationState.status == Status.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            fetchRelatedDebateState.status == Status.SUCCESS && imageUri == null -> {
                fetchRelatedDebateState.data?.let {
                    viewWithOutImage(
                        relatedDebates = it,
                        isKeyboardVisible = isKeyboardVisible,
                        context = context,
                        onImageSelected = {imageUri = it},
                        text = text,
                        onTextChange = {text = it}
                    ) {
                        user?.let { currentUser ->
                            debateCreationViewModel.handleDebateCreation(
                                text, imageUri, currentUser.uid, context, onCreationSuccess = toDebateView
                            )
                        }
                    }
                }
            }
            fetchRelatedDebateState.status == Status.SUCCESS && imageUri != null -> {
                viewWithImage(
                    imageUri = imageUri!!,
                    onImageDelete = { imageUri = null },
                    text = text,
                    onTextChange = {text = it}
                ) {
                    user?.let { currentUser ->
                        debateCreationViewModel.handleDebateCreation(
                            text, imageUri, currentUser.uid, context, onCreationSuccess = toDebateView
                        )
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun viewWithImage(
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
                .weight(1f)
        ) {
            item {
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
                        IconButton(
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
fun viewWithOutImage(
    relatedDebates: List<DebateWithUsers>,
    isKeyboardVisible: Boolean,
    context: Context,
    onImageSelected: (Uri?) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //TODO　これをcarouselにしたい
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(relatedDebates) {relatedDebate->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Image(
                                painter = rememberAsyncImagePainter(relatedDebate.debaterImageURL),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(text = relatedDebate.debaterName)

                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                                contentDescription = "AccountIcon",
                                modifier = Modifier
                                    .size(40.dp)
                            )
                            Text(text = relatedDebate.debaterLikeCount.toString())

                        }
                    }

                    Image(painter = rememberAsyncImagePainter(relatedDebate.firstMessageImageURL),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillWidth
                    )

                    Text(
                        text = relatedDebate.firstMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        if (relatedDebates.size <3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .heightIn(max = if (isKeyboardVisible) 160.dp else 48.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ImagePermissionAndSelection(
                        context = context,
                        onImageSelected = onImageSelected
                    )

                    MaxLengthOutlinedTextField(
                        value = text,
                        onValueChange = onTextChange,
                        maxLength = 140,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onSendClick) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        } else {
            //TODO 戻るボタンで表示されないようにする
            Toast.makeText(context, "討論が上限数に達したため、これ以上作成できません。関連討論にタップして移動できます", Toast.LENGTH_SHORT).show()
        }

    }
}

