package kimsy.rr.vental.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kimsy.rr.vental.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLineScreen(){

    val scaffoldState: ScaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "VenTal")},
                navigationIcon = { IconButton(onClick = {
                /*TODO Back*/
                }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Text("Text", modifier = Modifier.padding(it))
    }
}