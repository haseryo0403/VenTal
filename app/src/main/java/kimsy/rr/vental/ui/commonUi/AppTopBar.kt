package kimsy.rr.vental.ui.commonUi


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import kimsy.rr.vental.R
import kimsy.rr.vental.Screen
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kimsy.rr.vental.data.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val saveState = viewModel.saveState.collectAsState()

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
                        viewModel.startSavingVentCard(context)
                    }
                     }) {
                    Text(
                        text = "送信",
                        modifier = Modifier
                    )
                }
            } else if (title.contains("マイページ")) {
                IconButton(onClick = {
                    navController.navigate(Screen.Notifications.route)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_notifications_24), contentDescription = "notifications")
                }

                IconButton(onClick = {
                    navController.navigate(Screen.SettingsScreen.route)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_settings_24), contentDescription = "notifications")
                }
            } else if(!title.contains("通知")){

                //TODO isReadの個数をチェックして反映？もしくは通知画面をリアルタイムアップデートにして、もし新しいのがあれば反映？どっちだ

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
//            .height(48.dp)
    ){
        TopAppBar(
            title = {
                if(title != "VCC"){
                    Text(
                        title,
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
                            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )

        )
        when (saveState.value.status) {
            Status.LOADING -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )            }
            Status.SUCCESS -> {viewModel.resetValues()}
            Status.FAILURE -> {
                Log.e("ATB", "${saveState.value.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    onSavingFailure() // メインスレッドでventCardCreationViewに移動
                    Toast.makeText(context, "送信に失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                }
                viewModel.resetStatus()
            }
            else -> {}
        }

    }


}