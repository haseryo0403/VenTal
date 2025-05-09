package kimsy.rr.vental.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.AuthViewModel

@Composable
fun SettingsItem(
    iconResId: Int,
    title: String,
    description: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .size(32.dp)
        )
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(5f)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (description != null) {
                Text(text = description)
            }
        }
    }
}

@Composable
fun SettingsView(
    authViewModel: AuthViewModel,
    toNotificationSettingsView: () -> Unit,
    toAppInfoView: () -> Unit,
    toAccountClosingView: () -> Unit
) {
    val logOutDialogOpen = remember { mutableStateOf(false)}
    val accountClosingDialogOpen = remember { mutableStateOf(false)}
    val context = LocalContext.current
    val signOutState by authViewModel.signOutState.collectAsState()

    when(signOutState.status) {
        Status.FAILURE -> {
            ErrorView(retry = {
                authViewModel.signOut(context)
            })
        }
        else -> {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            //TODO これは今は必要ない。アカウント関連で表示することができたら使う。
//            SettingsItem(
//                iconResId = R.drawable.outline_person_24,
//                title = stringResource(id = R.string.account),
//                description = stringResource(id = R.string.account_settings_description),
//                onClick = {
//                    // TODO: goto account page
//                }
//            )
            SettingsItem(
                iconResId = R.drawable.outline_notifications_24,
                title = stringResource(id = R.string.notification_settings),
                description = stringResource(id = R.string.notification_settings_description),
                onClick = {
                    toNotificationSettingsView()
                }
            )
            SettingsItem(
                iconResId = R.drawable.outline_pending_24,
                title = stringResource(id = R.string.about_app),
                description = null,
                onClick = {
                    toAppInfoView()
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                TextButton(onClick = {
                    logOutDialogOpen.value = true
                }) {
                    Text(
                        text = stringResource(id = R.string.logout),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                        )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ){
                TextButton(onClick = {
                    accountClosingDialogOpen.value = true
                }) {
                    Text(
                        text = stringResource(id = R.string.close_account),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    logoutDialog(dialogOpen = logOutDialogOpen, authViewModel = authViewModel, context = context)
    accountClosingDialog(
        dialogOpen = accountClosingDialogOpen,
        toAccountClosingView = toAccountClosingView
        )
}


@Composable
fun logoutDialog(
    dialogOpen: MutableState<Boolean>,
    authViewModel: AuthViewModel,
    context: Context
){

    if(dialogOpen.value){
        AlertDialog(
            onDismissRequest = {
                dialogOpen.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.signOut(context)
                        dialogOpen.value = false
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogOpen.value = false
                    }
                ) {
                    Text("いいえ")
                }
            },
            title = {
                Text(stringResource(id = R.string.logout_comfirmation))
            }
        )
    }
}
@Composable
fun accountClosingDialog(
    dialogOpen: MutableState<Boolean>,
    toAccountClosingView: () -> Unit
){

    if(dialogOpen.value){
        AlertDialog(
            onDismissRequest = {
                dialogOpen.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        toAccountClosingView()
                        dialogOpen.value = false
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        toAccountClosingView()
                        dialogOpen.value = false
                    }
                ) {
                    Text("いいえ")
                }
            },
            title = {
                Text(stringResource(id = R.string.close_account_comfirmation))
            }
        )
    }
}