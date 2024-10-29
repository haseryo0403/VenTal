package kimsy.rr.vental.ui

import android.widget.Toast
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppBarView(
    title: String,
    onBackNavClicked: () -> Unit = {}
){
    val navigationIcon: (@Composable () -> Unit)? =
        {
            if(!title.contains("タイムライン")){
                IconButton(onClick = { onBackNavClicked() })
                {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                null
            }

        }

    TopAppBar(
        title = { Text(title) },
        elevation = 3.dp,
        navigationIcon = navigationIcon
    )
}