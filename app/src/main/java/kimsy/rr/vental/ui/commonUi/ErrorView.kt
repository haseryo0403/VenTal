package kimsy.rr.vental.ui.commonUi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ErrorView(
    retry: (suspend () -> Unit)?
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.error_occurred_try_again))
        Icon(painter = painterResource(id = R.drawable.baseline_back_hand_24), contentDescription = "")
        if (retry != null) {
            Button(onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        retry()
                    }
                },
                modifier = Modifier.width(240.dp)
            ) {
                Text(stringResource(id = R.string.reload))
            }
        }
    }
}