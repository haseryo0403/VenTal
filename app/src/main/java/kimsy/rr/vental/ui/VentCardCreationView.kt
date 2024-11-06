package kimsy.rr.vental.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.ui.CommonComposable.MaxLengthOutlinedTextField
import kimsy.rr.vental.ui.CommonComposable.MaxLengthTextField


@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("SuspiciousIndentation")
@Composable
fun VentCardCreationView(
    viewModel: VentCardCreationViewModel,
    context: Context
){
    val selectedUri = viewModel.selectedImageUri
    val dialogOpen = remember { mutableStateOf(false)}


    LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(start = 16.dp, end = 16.dp)
    ) {
        item {
            //TODO このTextfieldを背景と同じ色にすればあたかも大きなTextfieldに見えるはず
            MaxLengthTextField(
                value = viewModel.content,
                onValueChange = { newText -> viewModel.content = newText},
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


            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_tag_24),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                viewModel.tags.forEach { tag ->
                    // Rowを1つの要素としてまとめ、FlowRow内で改行されにくくする
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .widthIn(max = 200.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        IconButton(
                            onClick = { viewModel.tags.remove(tag) },
                            modifier = Modifier.size(24.dp) // IconButtonのサイズを調整
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_clear_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp) // アイコンのサイズを小さめに設定
                            )
                        }
                    }
                }

                IconButton(onClick = {
                    if(viewModel.tags.size < 5) {
                        dialogOpen.value = true
                    } else {
                        Toast.makeText(context, "タグは5つまでしか追加できません。", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_control_point_24),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }

            }

            if (selectedUri != null) {
                Log.d("VCCV", "uri: $selectedUri")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                ){
                    Image(
                        painter = rememberAsyncImagePainter(selectedUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit,
                    )
                    IconButton(
                        onClick = { viewModel.selectedImageUri = null },
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
            } else {
                Log.d("TAG", "selectedUri is null")

            }
        }
    }
    tagDialog(dialogOpen = dialogOpen, viewModel = viewModel, context )
}

@Composable
fun tagDialog(
    dialogOpen: MutableState<Boolean>,
    viewModel: VentCardCreationViewModel,
    context: Context
){
    var text by remember { mutableStateOf("")}

    if(dialogOpen.value){
        Dialog(
            onDismissRequest = { dialogOpen.value = false },
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "追加したいタグを入力してください。タグは最大5つまで追加できます",
                        modifier = Modifier.padding(16.dp),
                    )
                    MaxLengthOutlinedTextField(
                        value = text,
                        onValueChange = {newText -> text = newText},
                        maxLength = 50
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                dialogOpen.value = false
                            },
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text("キャンセル")
                        }
                        TextButton(
                            onClick = {
                                if(viewModel.tags.contains(text)){
                                    Toast.makeText(context, "同じタグは追加できません", Toast.LENGTH_SHORT).show()
                                } else {
                                    dialogOpen.value = false
                                    viewModel.tags.add(text)
                                    text = ""
                                }
                            },
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text("追加")
                        }
                    }
                }
            }
        }
    }
}

//@Preview(
//    device = Devices.PIXEL_7,
//    showSystemUi = true,
//    showBackground = true,
//)
//@Composable
//fun VCCPre(){
//    VentCardCreationView()
//}