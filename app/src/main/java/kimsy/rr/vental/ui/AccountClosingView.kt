package kimsy.rr.vental.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kimsy.rr.vental.R
import kimsy.rr.vental.data.Status
import kimsy.rr.vental.ui.CommonComposable.CustomCircularProgressIndicator
import kimsy.rr.vental.viewModel.AccountClosingViewModel

@Composable
fun AccountClosingView(
    viewModel: AccountClosingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val radioOptions = listOf(0,1,2,3,4,5,6)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0] ) }
    val (checkedState, onCheckedStateChange) = remember { mutableStateOf(false) }
    val (uncheckedState, onUncheckedStateChange) = remember { mutableStateOf(false) }

    val accountClosingState by viewModel.accountClosingState.collectAsState()
    when(accountClosingState.status) {
        Status.LOADING -> {
            CustomCircularProgressIndicator()
        }
        Status.SUCCESS -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.close_account_success), Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
        Status.FAILURE -> {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.close_account_failure), Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
        else -> {}
    }
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{
            Text(text = stringResource(id = R.string.close_account_warning),
                modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, end = 24.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(text = stringResource(id = R.string.close_account_takes_30days),
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(id = R.string.choose_close_account_reason),
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
        items(radioOptions) {reasonNumber ->
            val reasonTitleId = R.string::class.java.getField("close_account_reason_title_$reasonNumber").getInt(null)
            val reasonDescriptionId = R.string::class.java.getField("close_account_reason_description_$reasonNumber").getInt(null)
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .toggleable(
                        value = checkedState,
                        onValueChange = { onCheckedStateChange(!checkedState) },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(id = R.string.close_account_agreement),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            if (uncheckedState){
                Text(text = stringResource(id = R.string.close_account_agreement_required),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium)
            }
            Button(
                modifier = Modifier.width(240.dp),
                onClick = {
                    if (checkedState) {
                        viewModel.closeAccount(selectedOption, context)
                    } else {
                        onUncheckedStateChange(true)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.close_account))
            }
        }
    }
}