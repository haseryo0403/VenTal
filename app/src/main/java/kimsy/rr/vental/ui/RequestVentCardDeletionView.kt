package kimsy.rr.vental.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.ViewModel.RequestVentCardDeletionViewModel
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator

@Composable
fun RequestVentCardDeletionView(
    viewModel: RequestVentCardDeletionViewModel = hiltViewModel(),
    toMyPageView: () -> Unit
) {
    val radioOptions = listOf(0,1,2,3,4)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0] ) }

    val reportState by viewModel.requestState.collectAsState()
    when(reportState.status) {
        Status.LOADING -> {
            CustomCircularProgressIndicator()
        }
        Status.SUCCESS -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.request_success), Toast.LENGTH_SHORT).show()
            toMyPageView()
            viewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.request_failure), Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
        else -> {}
    }
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(radioOptions) {reasonNumber ->
            val reasonTitleId = R.string::class.java.getField("request_reason_title_$reasonNumber").getInt(null)
            val reasonDescriptionId = R.string::class.java.getField("request_reason_description_$reasonNumber").getInt(null)
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (reasonNumber == selectedOption),
                        onClick = {
                            onOptionSelected(reasonNumber)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                RadioButton(
                    selected = (reasonNumber == selectedOption),
                    onClick = { onOptionSelected(reasonNumber) }
                )
                Column {
                    Text(
                        text = stringResource(id = reasonTitleId),
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = reasonDescriptionId),
                        modifier = Modifier.padding(start = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Button(
                modifier = Modifier.width(240.dp),
                onClick = { viewModel.requestVentCardDeletion(selectedOption) }
            ) {
                Text(text = stringResource(id = R.string.request_button))
            }
        }
    }
}