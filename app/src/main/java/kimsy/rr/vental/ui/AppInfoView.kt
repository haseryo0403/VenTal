package kimsy.rr.vental.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kimsy.rr.vental.R

@Composable
fun AppInfoView(
    toTermsOfServiceView: () -> Unit,
    toGuidelineView: () -> Unit,
    toPrivacyPolicyView: () -> Unit,
){
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        item {
            SettingsItem(
                iconResId = R.drawable.outline_info_24,
                title = stringResource(id = R.string.terms_of_service),
                description = null,
                onClick = {
                    toTermsOfServiceView()
                }
            )
            SettingsItem(
                iconResId = R.drawable.outline_info_24,
                title = stringResource(id = R.string.guideline),
                description = null,
                onClick = {
                    toGuidelineView()
                }
            )
            SettingsItem(
                iconResId = R.drawable.outline_info_24,
                title = stringResource(id = R.string.privacy_policy),
                description = null,
                onClick = {
                    toPrivacyPolicyView()
                }
            )
        }
    }
}
