package kimsy.rr.vental.ui.commonUi


import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kimsy.rr.vental.ViewModel.VentCardCreationViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarView(
    title: String,
    context: Context,
    onBackNavClicked: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: VentCardCreationViewModel
){
    val titleToDisableScroll = listOf("VCC", "VentCards")

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
                val coroutineScope = rememberCoroutineScope() // コルーチンのスコープを取得
                OutlinedButton(onClick = {
                    //スワイプカード登録
                    Log.d("AppTopBar","送信ボタン押下")
                    coroutineScope.launch {
                        viewModel.saveImage(context)
                    }
                     }) {
                    // テキストを追加
                    Text(
                        text = "送信",
                        modifier = Modifier // 右側のパディング
                    )
                }
            } else {
                null
            }
        }

    CenterAlignedTopAppBar(
        title = {
            if(title != "VCC"){
                Text(title)
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
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//            containerColor = MaterialTheme.colorScheme.tertiary // TextField の背景色と同じにする
//            )

    )
}