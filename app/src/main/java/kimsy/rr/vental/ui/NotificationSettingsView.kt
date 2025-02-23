package kimsy.rr.vental.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.ui.commonUi.ErrorView
import kimsy.rr.vental.viewModel.NotificationSettingsViewModel


@Composable
fun NotificationSettingsView(
    notificationSettingsViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val notificationSettingsState by notificationSettingsViewModel.notificationSettingsState.collectAsState()

    LaunchedEffect(Unit) {
        notificationSettingsViewModel.loadNotificationSettings()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {

            when (notificationSettingsState.status) {
                Status.LOADING -> {
                    CustomCircularProgressIndicator()
                }
                Status.SUCCESS -> {
                    Text(text = "討論やアクティビティ")

                    val settings = notificationSettingsState.data!!
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.notification_settings_debate_start)) },
                        trailingContent = {
                            Switch(checked = settings.debateStartNotification?: true, onCheckedChange = {
                                notificationSettingsViewModel.updateNotificationSettings(settings.copy(debateStartNotification = it))
                            })
                        }
                    )
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.notification_settings_debate_message)) },
                        trailingContent = {
                            Switch(checked = settings.messageNotification?: true, onCheckedChange = {
                                notificationSettingsViewModel.updateNotificationSettings(settings.copy(messageNotification = it))
                            })
                        }
                    )
                    ListItem(
                        headlineContent = { Text(text = stringResource(id = R.string.notification_settings_debate_comment)) },
                        trailingContent = {
                            Switch(checked = settings.commentNotification?: true, onCheckedChange = {
                                notificationSettingsViewModel.updateNotificationSettings(settings.copy(commentNotification = it))
                            })
                        }
                    )
                }
                Status.FAILURE -> {
                    ErrorView(retry = {
                        notificationSettingsViewModel.loadNotificationSettings()
                    })
                }
                else -> {}
            }


        }
    }
}