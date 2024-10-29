package kimsy.rr.vental.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kimsy.rr.vental.R


@Composable
fun VentCardCreationView(){
    var text by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            //TODO このTextfieldを背景と同じ色にすればあたかも大きなTextfieldに見えるはず
            TextField(
                value = text,
                onValueChange = {text = it},
                placeholder = { Text(text = "怒りをぶつけろ！")},
                modifier = Modifier.fillMaxSize()
            )
            Image(painter = painterResource(id = R.drawable.aston_martin),
                contentDescription = "Image")
        }
    }
}

@Preview(
    device = Devices.PIXEL_7,
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun VCCPre(){
    VentCardCreationView()
}