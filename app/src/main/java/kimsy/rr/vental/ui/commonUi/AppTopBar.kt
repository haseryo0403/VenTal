package kimsy.rr.vental.ui.commonUi


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kimsy.rr.vental.R
import kimsy.rr.vental.Screen
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarView(
    title: String,
    context: Context,
    navController: NavController,
    onBackNavClicked: () -> Unit = {},
    onSavingFailure: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: VentCardCreationViewModel
){
    val titleToDisableScroll = listOf("VCC", "VentCards")
    var isLoading by remember { mutableStateOf(false) }

    val navigationIcon: (@Composable () -> Unit)? =
        {
            if(title.contains("VCC")) {
                TextButton(
                    onClick = { onBackNavClicked() }
                ) {
                    Text(text = "キャンセル")
                }
            } else if(!title.contains("タイムライン")){
                IconButton(onClick = { onBackNavClicked() })
                {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                null
            }
        }

    val action: (@Composable () -> Unit)? =
        {
            if(title.contains("VCC")){
                OutlinedButton(onClick = {
                    //スワイプカード登録
                    Log.d("AppTopBar","送信ボタン押下")
                    if(viewModel.content.isBlank() && viewModel.selectedImageUri == null){
                        Toast.makeText(context, "内容を入力してください", Toast.LENGTH_LONG).show()
                    } else {
                        onBackNavClicked()
                        isLoading =true
                        viewModel.startSavingVentCard(
                            context,
                            onError = {
                                isLoading = false

                                CoroutineScope(Dispatchers.Main).launch {
                                    onSavingFailure() // メインスレッドでバックスタックを操作
                                    Toast.makeText(context, "送信に失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                                }
                            },
                            onComplete = {
                                isLoading = false
                            }
                        )
                    }
                     }) {
                    // テキストを追加
                    Text(
                        text = "送信",
                        modifier = Modifier // 右側のパディング
                    )
                }
            } else if(!title.contains("通知")){

                IconButton(onClick = {
                    navController.navigate(Screen.Notifications.route)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_notifications_24), contentDescription = "notifications")
                }

            } else {
                null
            }
        }

    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        CenterAlignedTopAppBar(
//            modifier = Modifier.height(48.dp),
            title = {
                if(title != "VCC"){
                    Text(
                        title,
//                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    null
                }
            },
            navigationIcon = {navigationIcon?.invoke()},
            actions = {action?.invoke()},
            scrollBehavior = if(titleToDisableScroll.any{title.contains(it)}){
                                TopAppBarDefaults.pinnedScrollBehavior()
                            } else {
                                scrollBehavior
                            }
        )
        if(isLoading){
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }

    }


}