package kimsy.rr.vental.ui

import android.content.ClipData.Item
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kimsy.rr.vental.R
import kimsy.rr.vental.ui.CommonComposable.ImagePermissionAndSelection
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DebateCreationView(
    context: Context
){
    var text by remember { mutableStateOf("") }
    val isKeyboardVisible = WindowInsets.isImeVisible

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    LazyColumn(
        modifier = Modifier.weight(1f)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
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
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon",
                            modifier = Modifier
                                .size(48.dp)
                        )
                        Text(text = "User Name")

                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "AccountIcon",
                            modifier = Modifier
                                .size(40.dp)
                        )
                        Text(text = "64")

                    }
                }

                Text(
                    text = "基礎みたいなもんだろ",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp)
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
                        Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = "AccountIcon",
                            modifier = Modifier
                                .size(48.dp)
                        )
                        Text(text = "User Name")

                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "AccountIcon",
                            modifier = Modifier
                                .size(40.dp)
                        )
                        Text(text = "64")

                    }
                }

                Text(
                    text = "基礎みたいなもんだろ",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
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
                            // 画像URIが選択された場合の処理
//                            viewModel.selectedImageUri = uri
                        }
                    })
        MaxLengthOutlinedTextField(value = text, onValueChange = {text = it}, maxLength = 140, modifier = Modifier.weight(1f))

        IconButton(
            onClick = {

            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }
    }

    }



}

