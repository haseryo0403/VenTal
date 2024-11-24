package kimsy.rr.vental.ui

import android.content.ClipData.Item
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kimsy.rr.vental.ViewModel.DebateViewModel
import kimsy.rr.vental.data.VentCardWithUser
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebateCreationView(
    context: Context,
    authViewModel: AuthViewModel,
    debateCreationViewModel: DebateCreationViewModel,
    debateViewModel: DebateViewModel,
    toDebateView: () -> Unit
){
    val user by authViewModel.currentUser.observeAsState()
    val relatedDebates by debateCreationViewModel.relatedDebates.observeAsState(emptyList())
    val createdDebateWithUsers by debateCreationViewModel.createdDebateWithUsers.observeAsState(null)
    val isLoading by debateCreationViewModel.isLoading
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(createdDebateWithUsers){
        createdDebateWithUsers?.onSuccess {debateWithUsers->
            debateViewModel.debateWithUsers.value = debateWithUsers
            toDebateView()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
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
                                    onImageSelected = {uri ->
                                        // 選択された画像URIをここで処理
                                        if (uri != null) {
                                            imageUri = uri
                                        }
                                    })
                        MaxLengthOutlinedTextField(value = text, onValueChange = {text = it}, maxLength = 140, modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                user?.let { debateCreationViewModel.handleDebateCreation(text,imageUri, it.uid, context) }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                    if (imageUri != null) {
//                        Image(
//                            painter = rememberAsyncImagePainter(imageUri),
//                            contentDescription = "Selected Image",
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(RoundedCornerShape(16.dp)),
//                            contentScale = ContentScale.FillWidth,
//                        )
                    }
                }
            } else {
                //TODO 戻るボタンで表示されないようにする
                Toast.makeText(context, "討論が上限数に達したため、これ以上作成できません。関連討論にタップして移動できます", Toast.LENGTH_SHORT).show()
            }

            }
        }

    }



}

