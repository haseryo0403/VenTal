package kimsy.rr.vental.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel


@SuppressLint("SuspiciousIndentation")
@Composable
fun VentCardCreationView(
    viewModel: VentCardCreationViewModel
){
    var text by remember { mutableStateOf("") }

    val selectedUri = viewModel.selectedImageUri


        LazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 40.dp, end = 16.dp)
    ) {
        item {
            //TODO このTextfieldを背景と同じ色にすればあたかも大きなTextfieldに見えるはず
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(text = "怒りをぶつけろ！") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent, // 背景を透明に設定
                    unfocusedIndicatorColor = Color.Transparent, // 未選択時の下線を透明に設定
                    focusedIndicatorColor = Color.Transparent // 選択時の下線を透明に設定
                )
            )

            if (selectedUri != null) {
                Log.d("VCCV", "uri: $selectedUri")
                Image(
                    painter = rememberAsyncImagePainter(selectedUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Log.d("TAG", "selectedUri is null")

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