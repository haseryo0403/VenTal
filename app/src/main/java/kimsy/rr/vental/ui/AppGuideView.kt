package kimsy.rr.vental.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppGuideView(
    toMainView: () -> Unit
) {
    Column{
        Text(text = "guide")
        Button(onClick = { toMainView() }) {
            Text(text = "いけいけ")
        }
    }
}