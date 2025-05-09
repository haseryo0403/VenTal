package kimsy.rr.vental.ui.commonUi


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kimsy.rr.vental.R
import kimsy.rr.vental.Screen
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.viewModel.NotificationsViewModel
import kimsy.rr.vental.viewModel.VentCardCreationViewModel
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
    ventCardCreationViewModel: VentCardCreationViewModel,
    notificationsViewModel: NotificationsViewModel,
    toNotificationView: () -> Unit
){
    val titleToDisableScroll = listOf("VCC", "VentCards")
    val saveState = ventCardCreationViewModel.saveState.collectAsState()

    val notificationCountState = notificationsViewModel.notificationCountState.collectAsState()

    LaunchedEffect(Unit) {
        notificationsViewModel.observeNotificationCount()
    }


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
                    if(ventCardCreationViewModel.content.isBlank() && ventCardCreationViewModel.selectedImageUri == null){
                        Toast.makeText(context, "内容を入力してください", Toast.LENGTH_LONG).show()
                    } else {
                        onBackNavClicked()
                        ventCardCreationViewModel.startSavingVentCard(context)
                    }
                     }) {
                    Text(
                        text = "送信",
                        modifier = Modifier
                    )
                }
            }
            else if(!title.contains("通知")){

                    BadgedBox(
                    badge = {
                        if (notificationCountState.value.status == Status.SUCCESS) {
                            when(notificationCountState.value.data) {
                                0 -> {}
                                else -> {
                                    Badge(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .offset(x = (-10).dp, y = 4.dp)
                                    ){
                                        Text(text = notificationCountState.value.data.toString())
                                    }
                                }
                            }
                        } else {

                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ){
                    IconButton(onClick = {
                        toNotificationView()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_notifications_24),
                            contentDescription = "notifications",
                        )
                    }
                }


                IconButton(onClick = {
                    navController.navigate(Screen.SettingsScreen.route)
                }) {
                    Icon(painter = painterResource(id = R.drawable.baseline_settings_24), contentDescription = "settings")
                }
            }
            else {
                null
            }
        }
    Box(

        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    //            .height(48.dp)
    ){
            val appTitle = buildAnnotatedString {
                withStyle(style = SpanStyle(MaterialTheme.colorScheme.primary)){
                    append("VEN")
                }
                withStyle(style = SpanStyle(MaterialTheme.colorScheme.secondary)){
                    append("TAL")
                }
            }
        TopAppBar(
            title = {
                if(title == "VCC"){
                    null
                } else if(title == "タイムライン") {
                    Text(text = appTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        title,
                    )
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
                scrolledContainerColor = MaterialTheme.colorScheme.background,
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
            Status.SUCCESS -> {ventCardCreationViewModel.resetValues()}
            Status.FAILURE -> {
                Log.e("ATB", "${saveState.value.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    onSavingFailure() // メインスレッドでventCardCreationViewに移動
                    Toast.makeText(context, "送信に失敗しました。通信環境の良いところで再度お試しください。", Toast.LENGTH_LONG).show()
                }
                ventCardCreationViewModel.resetStatus()
            }
            else -> {}
        }
    }
}